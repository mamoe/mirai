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
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.MultiPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.GroupImpl
import net.mamoe.mirai.qqandroid.MemberImpl
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.QQImpl
import net.mamoe.mirai.qqandroid.event.ForceOfflineEvent
import net.mamoe.mirai.qqandroid.event.PacketReceivedEvent
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.*
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Volatile

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
                        response.t402
                    ).sendAndExpect()
                    continue@mainloop
                }

                is LoginPacket.LoginPacketResponse.Success -> {
                    bot.logger.info("Login successful")
                    break@mainloop
                }
            }
        }

        // println("d2key=${bot.client.wLoginSigInfo.d2Key.toUHexString()}")
        StatSvc.Register(bot.client).sendAndExpect<StatSvc.Register.Response>(6000) // it's slow
    }

    override suspend fun init() {
        this@QQAndroidBotNetworkHandler.subscribeAlways<ForceOfflineEvent> {
            if (this@QQAndroidBotNetworkHandler.bot == this.bot) {
                close()
            }
        }

        //val msg = MessageSvc.PbGetMsg(bot.client, MsgSvc.SyncFlag.START, currentTimeSeconds).sendAndExpect<MessageSvc.PbGetMsg.Response>()
        //println(msg.contentToString())
        bot.qqs.delegate.clear()
        bot.groups.delegate.clear()

        val startTime = currentTimeMillis
        try {
            bot.logger.info("开始加载好友信息")
            var currentFriendCount = 0
            var totalFriendCount: Short
            while (true) {
                val data = FriendList.GetFriendGroupList(
                    bot.client,
                    currentFriendCount,
                    150,
                    0,
                    0
                ).sendAndExpect<FriendList.GetFriendGroupList.Response>(timeoutMillis = 5000, retry = 2)

                totalFriendCount = data.totalFriendCount
                data.friendList.forEach {
                    // atomic add
                    bot.qqs.delegate.addLast(QQImpl(bot, bot.coroutineContext, it.friendUin)).also {
                        currentFriendCount++
                    }
                }
                bot.logger.verbose("正在加载好友列表 ${currentFriendCount}/${totalFriendCount}")
                if (currentFriendCount >= totalFriendCount) {
                    break
                }
                // delay(200)
            }
            bot.logger.info("好友列表加载完成, 共 ${currentFriendCount}个")
        } catch (e: Exception) {
            bot.logger.error("加载好友列表失败|一般这是由于加载过于频繁导致/将以热加载方式加载好友列表")
        }

        val friendLoadFinish = currentTimeMillis
        val groupInfo = mutableMapOf<Long, Int>()
        try {
            bot.logger.info("开始加载群组列表与群成员列表")
            val troopData = FriendList.GetTroopListSimplify(
                bot.client
            ).sendAndExpect<FriendList.GetTroopListSimplify.Response>(timeoutMillis = 5000)
            // println("获取到群数量" + troopData.groups.size)
            val toGet: MutableMap<GroupImpl, ContactList<Member>> = mutableMapOf()
            troopData.groups.forEach {
                val contactList = ContactList(LockFreeLinkedList<Member>())
                val group =
                    GroupImpl(
                        bot = bot,
                        coroutineContext = this.coroutineContext,
                        id = it.groupCode,
                        uin = it.groupUin,
                        initName = it.groupName,
                        initAnnouncement = it.groupMemo,
                        initAllowMemberInvite = false,
                        initConfessTalk = false,
                        initMuteAll = false,
                        initAutoApprove = false,
                        members = contactList
                    )
                group.owner =
                    MemberImpl(
                        qq = bot.QQ(it.dwGroupOwnerUin) as QQImpl,
                        groupCard = "",//unknown now
                        group = group,
                        coroutineContext = group.coroutineContext,
                        permission = MemberPermission.OWNER
                    )
                if (it.dwGroupOwnerUin == bot.uin) {
                    group.botPermission = MemberPermission.OWNER
                }
                toGet[group] = contactList
                bot.groups.delegate.addLast(group)
            }
            coroutineScope {
                toGet.forEach {
                    launch {
                        try {
                            getTroopMemberList(it.key, it.value, it.key.owner.id)
                            groupInfo[it.key.id] = it.value.size
                        } catch (e: Exception) {
                            groupInfo[it.key.id] = -1
                            bot.logger.info("群${it.key.uin}的列表拉取失败, 将采用动态加入")
                        }
                    }
                    //delay(200)
                }
            }
            bot.logger.info("群组列表与群成员加载完成, 共 ${troopData.groups.size}个")
        } catch (e: Exception) {
            bot.logger.error("加载组信息失败|一般这是由于加载过于频繁导致/将以热加载方式加载群列表")
            println(e.message)
            println(e.logStacktrace())
        }

        //===log===//
        fun fillUntil(long: Number, size: Int): String {
            val x = long.toString()
            return x + " ".repeat(
                if (size - x.length > 0) {
                    size - x.length
                } else {
                    0
                }
            )
        }

        bot.logger.info("====================Mirai Bot List初始化完毕====================")
        bot.logger.info("好友数量: ${fillUntil(bot.qqs.size, 9)}\t\t\t 加载时间: ${friendLoadFinish - startTime}ms")
        bot.logger.info("加入群组: ${fillUntil(bot.groups.size, 9)}\t\t\t 加载时间: ${currentTimeMillis - friendLoadFinish}ms")
        groupInfo.forEach {
            if (it.value == -1) {
                bot.logger.error("群组号码: ${fillUntil(it.key, 9)}\t 成员数量加载失败")
            } else {
                bot.logger.info(
                    "群组号码: ${fillUntil(it.key, 9)}\t 成员数量: ${fillUntil(
                        it.value,
                        4
                    )}\t BOT权限: " + bot.groups[it.key].botPermission.toString() + ""
                )
            }
        }
        bot.logger.info("====================Mirai Bot List初始化完毕====================")
        return
        MessageSvc.PbGetMsg(bot.client, MsgSvc.SyncFlag.START, currentTimeSeconds).sendWithoutExpect()
    }

    suspend fun getGroupInfo(uin: Long) {
        val data = TroopManagement.getGroupInfo(
            client = bot.client,
            groupCode = uin
        ).sendAndExpect<TroopManagement.getGroupInfo.Response>(timeoutMillis = 3000)
    }

    suspend fun getTroopMemberList(group: GroupImpl, list: ContactList<Member>, owner: Long): ContactList<Member> {
        bot.logger.info("开始获取群[${group.uin}]成员列表")
        var size = 0
        var nextUin = 0L
        while (true) {
            val data = FriendList.GetTroopMemberList(
                client = bot.client,
                targetGroupUin = group.uin,
                targetGroupCode = group.id,
                nextUin = nextUin
            ).sendAndExpect<FriendList.GetTroopMemberList.Response>(timeoutMillis = 3000)
            data.members.forEach {
                if (it.memberUin != bot.uin) {
                    list.delegate.addLast(
                        MemberImpl(
                            qq = bot.QQ(it.memberUin) as QQImpl,
                            groupCard = it.autoRemark ?: it.nick,
                            group = group,
                            coroutineContext = group.coroutineContext,
                            permission = when {
                                it.memberUin == owner -> MemberPermission.OWNER
                                it.dwFlag == 1L -> MemberPermission.ADMINISTRATOR
                                else -> MemberPermission.MEMBER
                            }
                        )
                    )
                } else {
                    group.owner.groupCard = it.autoRemark ?: it.nick
                    if (it.dwFlag == 1L) {
                        group.botPermission = MemberPermission.ADMINISTRATOR
                    }
                }
            }
            size += data.members.size
            nextUin = data.nextUin
            if (nextUin == 0L) {
                break
            }
            //println("已获取群[${group.uin}]成员列表前" + size + "个成员")
        }
        //println("群[${group.uin}]成员全部获取完成, 共${list.size}个成员")
        return list
    }

    /**
     * 缓存超时处理的 [Job]. 超时后将清空缓存, 以免阻碍后续包的处理
     */
    @Volatile
    private var cachedPacketTimeoutJob: Job? = null
    /**
     * 缓存的包
     */
    private val cachedPacket: AtomicRef<ByteReadPacket?> = atomic(null)
    /**
     * 缓存的包还差多少长度
     */
    @Volatile
    private var expectingRemainingLength: Long = 0

    /**
     * 解析包内容.
     *
     * @param input 一个完整的包的内容, 去掉开头的 int 包长度
     */
    @UseExperimental(ExperimentalCoroutinesApi::class)
    fun parsePacketAsync(input: Input): Job {
        return this.launch(start = CoroutineStart.ATOMIC) {
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
            when (this) {
                is OutgoingPacketFactory<P> -> bot.handle(packet)
                is IncomingPacketFactory<P> -> bot.handle(packet, sequenceId)?.sendWithoutExpect()
            }
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
            if (rawInput.remaining == length.toLong()) {
                // 捷径: 当包长度正好, 直接传递剩余数据.
                cachedPacketTimeoutJob?.cancel()
                parsePacketAsync(rawInput)
                return
            }
            // 循环所有完整的包
            while (rawInput.remaining >= length) {
                parsePacketAsync(rawInput.readPacket(length))

                if (rawInput.remaining == 0L) {
                    cachedPacket.value = null // 表示包长度正好
                    cachedPacketTimeoutJob?.cancel()
                    return
                }
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
            val expectingLength = expectingRemainingLength
            if (rawInput.remaining >= expectingLength) {
                // 剩余长度够, 连接上去, 处理这个包.
                parsePacketAsync(buildPacket {
                    writePacket(cache)
                    writePacket(rawInput, expectingLength)
                })
                cachedPacket.value = null // 缺少的长度已经给上了.
                cachedPacketTimeoutJob?.cancel()

                if (rawInput.remaining != 0L) {
                    return processPacket(rawInput) // 继续处理剩下内容
                } else {
                    // 处理好了.
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
                bot.tryReinitializeNetworkHandler(e)
                return
            } catch (e: ReadPacketInternalException) {
                bot.logger.error("Socket channel read failed: ${e.message}")
                bot.tryReinitializeNetworkHandler(e)
                return
            } catch (e: CancellationException) {
                return
            } catch (e: Throwable) {
                bot.logger.error("Caught unexpected exceptions", e)
                bot.tryReinitializeNetworkHandler(e)
                return
            }
            packetReceiveLock.withLock {
                processPacket(rawInput)
            }
        }
    }

    private val packetReceiveLock: Mutex = Mutex()

    /**
     * 发送一个包, 但不期待任何返回.-
     */
    suspend fun OutgoingPacket.sendWithoutExpect() {
        bot.logger.info("Send: ${this.commandName}")
        withContext(this@QQAndroidBotNetworkHandler.coroutineContext + CoroutineName("Packet sender")) {
            channel.send(delegate)
        }
    }

    /**
     * 发送一个包, 并挂起直到接收到指定的返回包或超时(3000ms)
     */
    suspend fun <E : Packet> OutgoingPacket.sendAndExpect(timeoutMillis: Long = 3000, retry: Int = 0): E {
        require(timeoutMillis > 0) { "timeoutMillis must > 0" }
        require(retry >= 0) { "retry must >= 0" }

        var lastException: Exception? = null
        if (retry == 0) {
            val handler = PacketListener(commandName = commandName, sequenceId = sequenceId)
            packetListeners.addLast(handler)
            try {
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
            } finally {
                packetListeners.remove(handler)
            }
        } else this.delegate.useBytes { data, length ->
            repeat(retry + 1) {
                val handler = PacketListener(commandName = commandName, sequenceId = sequenceId)
                packetListeners.addLast(handler)
                try {
                    withContext(this@QQAndroidBotNetworkHandler.coroutineContext + CoroutineName("Packet sender")) {
                        channel.send(data, 0, length)
                    }
                    bot.logger.info("Send: ${this.commandName}")
                    return withTimeoutOrNull(timeoutMillis) {
                        @Suppress("UNCHECKED_CAST")
                        handler.await() as E
                        // 不要 `withTimeout`. timeout 的异常会不知道去哪了.
                    } ?: net.mamoe.mirai.qqandroid.utils.inline {
                        error("timeout when receiving response of $commandName")
                    }
                } catch (e: Exception) {
                    lastException = e
                } finally {
                    packetListeners.remove(handler)
                }
            }
            throw lastException!!
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