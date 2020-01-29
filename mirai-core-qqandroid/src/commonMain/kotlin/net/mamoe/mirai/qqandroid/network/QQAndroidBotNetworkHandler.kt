package net.mamoe.mirai.qqandroid.network

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.io.core.*
import kotlinx.io.pool.ObjectPool
import net.mamoe.mirai.data.MultiPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.event.PacketReceivedEvent
import net.mamoe.mirai.qqandroid.network.protocol.packet.KnownPacketFactories
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketLogger
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket.LoginPacketResponse.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.*
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
@UseExperimental(MiraiInternalAPI::class)
internal class QQAndroidBotNetworkHandler(bot: QQAndroidBot) : BotNetworkHandler() {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    override val supervisor: CompletableJob = SupervisorJob(bot.coroutineContext[Job])

    private lateinit var channel: PlatformSocket

    override suspend fun login() {
        channel = PlatformSocket()
        channel.connect("113.96.13.208", 8080)
        launch(CoroutineName("Incoming Packet Receiver")) { processReceive() }

        bot.logger.info("Trying login")
        var response: LoginPacket.LoginPacketResponse = LoginPacket.SubCommand9(bot.client).sendAndExpect()
        mainloop@ while (true) {
            when (response) {
                is UnsafeLogin -> {
                    bot.logger.info("Login unsuccessful, device auth is needed")
                    bot.logger.info("登录失败, 原因为非常用设备登录")
                    bot.logger.info("Open the following URL in QQ browser and complete the verification")
                    bot.logger.info("将下面这个链接在QQ浏览器中打开并完成认证后尝试再次登录")
                    bot.logger.info(response.url)
                    return
                }

                is Captcha -> when (response) {
                    is Captcha.Picture -> {
                        bot.logger.info("需要图片验证码")
                        var result = bot.configuration.loginSolver.onSolvePicCaptcha(bot, response.data)
                        if (result === null || result.length != 4) {
                            //refresh captcha
                            result = "ABCD"
                        }
                        bot.logger.info("提交验证码")
                        response = LoginPacket.SubCommand2(bot.client, response.sign, result).sendAndExpect()
                        continue@mainloop
                    }
                    is Captcha.Slider -> {
                        bot.logger.info("需要滑动验证码")
                        TODO("滑动验证码")
                    }
                }

                is Error -> error(response.toString())

                is SMSVerifyCodeNeeded -> {
                    val result = bot.configuration.loginSolver.onGetPhoneNumber()
                    response = LoginPacket.SubCommand7(
                        bot.client,
                        response.t174,
                        response.t402,
                        result
                    ).sendAndExpect()
                    continue@mainloop
                }

                is Success -> {
                    bot.logger.info("Login successful")
                    break@mainloop
                }
            }
        }

        println("d2key=${bot.client.wLoginSigInfo.d2Key.toUHexString()}")
        StatSvc.Register(bot.client).sendAndExpect<StatSvc.Register.Response>()
    }

    /**
     * 单线程处理包的接收, 分割和连接.
     */
    @Suppress("PrivatePropertyName")
    private val PacketReceiveDispatcher = newCoroutineDispatcher(1)

    /**
     * 单线程处理包的解析 (协程挂起效率够)
     */
    @Suppress("PrivatePropertyName")
    private val PacketProcessDispatcher = newCoroutineDispatcher(1)

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
     * 在 [PacketProcessDispatcher] 调度器中解析包内容.
     * [input] 将会被 [ObjectPool.recycle].
     *
     * @param input 一个完整的包的内容, 去掉开头的 int 包长度
     */
    fun parsePacketAsync(input: IoBuffer, pool: ObjectPool<IoBuffer> = IoBuffer.Pool): Job =
        this.launch(PacketProcessDispatcher) {
            try {
                parsePacket(input)
            } finally {
                input.discard()
                input.release(pool)
            }
        }

    /**
     * 在 [PacketProcessDispatcher] 调度器中解析包内容.
     * [input] 将会被 [Input.close], 因此 [input] 不能为 [IoBuffer]
     *
     * @param input 一个完整的包的内容, 去掉开头的 int 包长度
     */
    fun parsePacketAsync(input: Input): Job {
        require(input !is IoBuffer) { "input cannot be IoBuffer" }
        return this.launch(PacketProcessDispatcher) {
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

    private suspend inline fun <P : Packet> generifiedParsePacket(input: Input) {
        try {
            KnownPacketFactories.parseIncomingPacket(bot, input) { packetFactory: PacketFactory<P>, packet: P, commandName: String, sequenceId: Int ->
                handlePacket(packetFactory, packet, commandName, sequenceId)
                if (packet is MultiPacket<*>) {
                    packet.forEach {
                        handlePacket(null, it, commandName, sequenceId)
                    }
                }
            }
        } finally {
            println()
            println() // separate for debugging
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

        bot.logger.info(packet)

        packetFactory?.run {
            bot.handle(packet)
        }
    }

    /**
     * 处理从服务器接收过来的包. 这些包可能是粘在一起的, 也可能是不完整的. 将会自动处理.
     * 处理后的包会调用 [parsePacketAsync]
     */
    @UseExperimental(ExperimentalCoroutinesApi::class)
    internal fun processPacket(rawInput: ByteReadPacket) {
        if (rawInput.remaining == 0L) {
            return
        }

        val cache = cachedPacket.value
        if (cache == null) {
            // 没有缓存
            var length: Int = rawInput.readInt() - 4
            if (rawInput.remaining == length.toLong()) {
                // 捷径: 当包长度正好, 直接传递剩余数据.
                cachedPacketTimeoutJob?.cancel()
                parsePacketAsync(rawInput)
                return
            }
            // 循环所有完整的包
            while (rawInput.remaining > length) {
                parsePacketAsync(rawInput.readIoBuffer(length))

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
                dispose()
                bot.tryReinitializeNetworkHandler(e)
                return
            } catch (e: ReadPacketInternalException) {
                bot.logger.error("Socket channel read failed: ${e.message}")
                dispose()
                bot.tryReinitializeNetworkHandler(e)
                return
            } catch (e: CancellationException) {
                return
            } catch (e: Throwable) {
                bot.logger.error("Caught unexpected exceptions", e)
                dispose()
                bot.tryReinitializeNetworkHandler(e)
                return
            }
            launch(context = PacketReceiveDispatcher + CoroutineName("Incoming Packet handler"), start = CoroutineStart.ATOMIC) {
                processPacket(rawInput)
            }
        }
    }

    /**
     * 发送一个包, 并挂起直到接收到指定的返回包或超时(3000ms)
     */
    suspend fun <E : Packet> OutgoingPacket.sendAndExpect(): E {
        val handler = PacketListener(commandName = commandName, sequenceId = sequenceId)
        packetListeners.addLast(handler)
        channel.send(delegate)
        return withTimeout(3000) {
            @Suppress("UNCHECKED_CAST")
            handler.await() as E
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

    override suspend fun awaitDisconnection() = supervisor.join()

    override val coroutineContext: CoroutineContext = bot.coroutineContext
}