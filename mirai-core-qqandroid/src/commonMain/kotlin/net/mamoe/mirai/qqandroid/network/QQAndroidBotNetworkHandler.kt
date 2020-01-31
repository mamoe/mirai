package net.mamoe.mirai.qqandroid.network

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import kotlinx.io.core.buildPacket
import kotlinx.io.core.use
import net.mamoe.mirai.data.MultiPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.QQImpl
import net.mamoe.mirai.qqandroid.event.ForceOfflineEvent
import net.mamoe.mirai.qqandroid.event.PacketReceivedEvent
import net.mamoe.mirai.qqandroid.network.protocol.packet.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
@UseExperimental(MiraiInternalAPI::class)
internal class QQAndroidBotNetworkHandler(bot: QQAndroidBot) : BotNetworkHandler() {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    override val supervisor: CompletableJob = SupervisorJob(bot.coroutineContext[Job])

    override val coroutineContext: CoroutineContext = bot.coroutineContext + CoroutineExceptionHandler { _, throwable ->
        throwable.logStacktrace("Exception in NetworkHandler")
    }

    private lateinit var channel: PlatformSocket

    override suspend fun login() {
        if (::channel.isInitialized) {
            channel.close()
        }
        channel = PlatformSocket()
        channel.connect("113.96.13.208", 8080)
        this.launch(CoroutineName("Incoming Packet Receiver")) { processReceive() }

        // bot.logger.info("Trying login")
        var response: LoginPacket.LoginPacketResponse = LoginPacket.SubCommand9(bot.client).sendAndExpect()
        mainloop@ while (true) {
            when (response) {
                is LoginPacket.LoginPacketResponse.UnsafeLogin -> {
                    bot.configuration.loginSolver.onSolveUnsafeDeviceLoginVerify(bot, response.url)
                    response = LoginPacket.SubCommand9(bot.client).sendAndExpect()
                }

                is LoginPacket.LoginPacketResponse.Captcha -> when (response) {
                    is LoginPacket.LoginPacketResponse.Captcha.Picture -> {
                        var result = response.data.withUse {
                            bot.configuration.loginSolver.onSolvePicCaptcha(bot, this)
                        }
                        if (result == null || result.length != 4) {
                            //refresh captcha
                            result = "ABCD"
                        }
                        response = LoginPacket.SubCommand2.SubmitPictureCaptcha(bot.client, response.sign, result).sendAndExpect()
                        continue@mainloop
                    }
                    is LoginPacket.LoginPacketResponse.Captcha.Slider -> {
                        var ticket = bot.configuration.loginSolver.onSolveSliderCaptcha(bot, response.url)
                        if (ticket == null) {
                            ticket = ""
                        }
                        response = LoginPacket.SubCommand2.SubmitSliderCaptcha(bot.client, ticket).sendAndExpect()
                        continue@mainloop
                    }
                }

                is LoginPacket.LoginPacketResponse.Error -> error(response.toString())

                is LoginPacket.LoginPacketResponse.DeviceLockLogin -> {
                    response = LoginPacket.SubCommand20(
                        bot.client,
                        response.t402,
                        response.t403
                    ).sendAndExpect()
                    continue@mainloop
                }

                is LoginPacket.LoginPacketResponse.Success -> {
                    bot.logger.info("Login successful")
                    break@mainloop
                }
            }
        }

        println("d2key=${bot.client.wLoginSigInfo.d2Key.toUHexString()}")
        StatSvc.Register(bot.client).sendAndExpect<StatSvc.Register.Response>(6000)
    }

    override suspend fun init() {
        bot.logger.info("开始加载好友信息")

        this@QQAndroidBotNetworkHandler.subscribeAlways<ForceOfflineEvent> {
            if (this@QQAndroidBotNetworkHandler.bot == this.bot) {
                close()
            }
        }
        /*
        * 开始加载Contact表
        * */
        var currentFriendCount = 0
        var totalFriendCount: Short
        while (true) {
            val data = FriendList.GetFriendGroupList(
                bot.client,
                currentFriendCount,
                10,
                0,
                0
            ).sendAndExpect<FriendList.GetFriendGroupList.Response>()
            totalFriendCount = data.totalFriendCount
            data.friendList.forEach {
                // atomic add
                bot.qqs.delegate.addLast(QQImpl(bot, EmptyCoroutineContext, it.friendUin).also {
                    currentFriendCount++
                })
            }
            bot.logger.verbose("正在加载好友信息 ${currentFriendCount}/${totalFriendCount}")
            if (currentFriendCount >= totalFriendCount) {
                break
            }
        }
        bot.logger.info("好友信息加载完成, 共 ${currentFriendCount}个")
        //发送事件

        /**
        val data = FriendList.GetTroopList(
        bot.client
        ).sendAndExpect<FriendList.GetTroopList.Response>(100000)
        println(data.contentToString())
         */
    }

    /**
     * 缓存超时处理的 [Job]. 超时后将清空缓存, 以免阻碍后续包的处理
     */
    private var cachedPacketTimeoutJob: Job? = null
    /**
     * 缓存的包
     */
    private val cachedPacket: AtomicRef<ByteReadPacket?> = atomic(null)
    /**
     * 缓存的包还差多少长度
     */
    private var expectingRemainingLength: Long = 0

    /**
     * 解析包内容.
     *
     * @param input 一个完整的包的内容, 去掉开头的 int 包长度
     */
    fun parsePacketAsync(input: Input): Job {
        return this.launch {
            input.use { parsePacket(it) }
        }
    }

    /**
     * 解析包内容
     * **注意**: 需要函数调用者 close 这个 [input]
     *
     * @param input 一个完整的包的内容, 去掉开头的 int 包长度
     */
    suspend fun parsePacket(input: Input) {
        generifiedParsePacket<Packet>(input)
    }

    // with generic type, less mistakes
    private suspend inline fun <P : Packet> generifiedParsePacket(input: Input) {
        KnownPacketFactories.parseIncomingPacket(bot, input) { packetFactory: PacketFactory<P>, packet: P, commandName: String, sequenceId: Int ->
            handlePacket(packetFactory, packet, commandName, sequenceId)
            if (packet is MultiPacket<*>) {
                packet.forEach {
                    handlePacket(null, it, commandName, sequenceId)
                }
            }
        }
    }

    /**
     * 处理解析完成的包.
     */
    suspend fun <P : Packet> handlePacket(packetFactory: PacketFactory<P>?, packet: P, commandName: String, sequenceId: Int) {
        // highest priority: pass to listeners (attached by sendAndExpect).
        packetListeners.forEach { listener ->
            if (listener.filter(commandName, sequenceId) && packetListeners.remove(listener)) {
                listener.complete(packet)
            }
        }

        // check top-level cancelling
        if (PacketReceivedEvent(packet).broadcast().cancelled) {
            return
        }


        // broadcast
        if (packet is Subscribable) {
            if (packet is BroadcastControllable) {
                if (packet.shouldBroadcast) packet.broadcast()
            } else {
                packet.broadcast()
            }

            if (packet is Cancellable && packet.cancelled) return
        }

        bot.logger.info("Received packet: $packet")

        packetFactory?.run {
            bot.handle(packet)
        }
    }

    /**
     * 处理从服务器接收过来的包. 这些包可能是粘在一起的, 也可能是不完整的. 将会自动处理.
     * 处理后的包会调用 [parsePacketAsync]
     */
    internal fun processPacket(rawInput: ByteReadPacket) {
        if (rawInput.remaining == 0L) {
            return
        }

        val cache = cachedPacket.value
        if (cache == null) {
            // 没有缓存
            var length: Int = rawInput.readInt() - 4
            if (length < 0) {
                // 丢包了. 后半部分包提前到达
                return
            }
            if (rawInput.remaining == length.toLong()) {
                // 捷径: 当包长度正好, 直接传递剩余数据.
                cachedPacketTimeoutJob?.cancel()
                parsePacketAsync(rawInput)
                return
            }
            // 循环所有完整的包
            while (rawInput.remaining > length) {
                parsePacketAsync(rawInput.readPacket(length))

                length = rawInput.readInt() - 4
            }

            if (rawInput.remaining != 0L) {
                // 剩余的包长度不够, 缓存后接收下一个包
                expectingRemainingLength = length - rawInput.remaining
                cachedPacket.value = rawInput
            } else {
                cachedPacket.value = null // 表示包长度正好
                cachedPacketTimeoutJob?.cancel()
                return
            }
        } else {
            // 有缓存

            if (rawInput.remaining >= expectingRemainingLength) {
                // 剩余长度够, 连接上去, 处理这个包.
                parsePacketAsync(buildPacket {
                    writePacket(cache)
                    writePacket(rawInput, expectingRemainingLength)
                })
                cachedPacket.value = null // 缺少的长度已经给上了.

                if (rawInput.remaining != 0L) {
                    return processPacket(rawInput) // 继续处理剩下内容
                } else {
                    // 处理好了.
                    cachedPacketTimeoutJob?.cancel()
                    return
                }
            } else {
                // 剩余不够, 连接上去
                expectingRemainingLength -= rawInput.remaining
                // do not inline `packet`. atomicfu unsupported
                val packet = buildPacket {
                    writePacket(cache)
                    writePacket(rawInput)
                }
                cachedPacket.value = packet
            }
        }

        cachedPacketTimeoutJob?.cancel()
        cachedPacketTimeoutJob = launch {
            delay(1000)
            if (cachedPacketTimeoutJob == this.coroutineContext[Job] && cachedPacket.getAndSet(null) != null) {
                PacketLogger.verbose("等待另一部分包时超时. 将舍弃已接收的半个包")
            }
        }
    }


    @UseExperimental(ExperimentalCoroutinesApi::class)
    private suspend fun processReceive() {
        while (channel.isOpen) {
            val rawInput = try {
                channel.read()
            } catch (e: ClosedChannelException) {
                close()
                bot.tryReinitializeNetworkHandler(e)
                return
            } catch (e: ReadPacketInternalException) {
                bot.logger.error("Socket channel read failed: ${e.message}")
                close()
                bot.tryReinitializeNetworkHandler(e)
                return
            } catch (e: CancellationException) {
                return
            } catch (e: Throwable) {
                bot.logger.error("Caught unexpected exceptions", e)
                close()
                bot.tryReinitializeNetworkHandler(e)
                return
            }
            launch(CoroutineName("Incoming Packet handler"), start = CoroutineStart.ATOMIC) {
                packetReceiveLock.withLock {
                    processPacket(rawInput)
                }
            }
        }
    }

    private val packetReceiveLock: Mutex = Mutex()

    /**
     * 发送一个包, 并挂起直到接收到指定的返回包或超时(3000ms)
     */
    suspend fun <E : Packet> OutgoingPacket.sendAndExpect(timeoutMillis: Long = 3000, retry: Int = 1): E {
        require(timeoutMillis > 0) { "timeoutMillis must > 0" }
        require(retry >= 0) { "retry must >= 0" }

        val handler = PacketListener(commandName = commandName, sequenceId = sequenceId)
        packetListeners.addLast(handler)
        var lastException: Exception? = null
        repeat(retry + 1) {
            try {
                return doSendAndReceive(timeoutMillis, handler)
            } catch (e: Exception) {
                lastException = e
            }
        }
        packetListeners.remove(handler)
        throw lastException!!
    }

    private suspend inline fun <E : Packet> OutgoingPacket.doSendAndReceive(timeoutMillis: Long = 3000, handler: PacketListener): E {
        withContext(this@QQAndroidBotNetworkHandler.coroutineContext + CoroutineName("Packet sender")) {
            channel.send(delegate)
        }
        bot.logger.info("Send: ${this.commandName}")
        return withTimeoutOrNull(timeoutMillis) {
            @Suppress("UNCHECKED_CAST")
            handler.await() as E
            // 不要 `withTimeout`. timeout 的异常会不知道去哪了.
        } ?: net.mamoe.mirai.qqandroid.utils.inline {
            error("timeout when receiving response of $commandName")
        }
    }

    @PublishedApi
    internal val packetListeners = LockFreeLinkedList<PacketListener>()

    @PublishedApi
    internal inner class PacketListener( // callback
        val commandName: String,
        val sequenceId: Int
    ) : CompletableDeferred<Packet> by CompletableDeferred(supervisor) {
        fun filter(commandName: String, sequenceId: Int) = this.commandName == commandName && this.sequenceId == sequenceId
    }

    override fun close(cause: Throwable?) {
        if (::channel.isInitialized) {
            channel.close()
        }
        super.close(cause)
    }

    override suspend fun awaitDisconnection() = supervisor.join()
}