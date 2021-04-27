/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal.network.handler

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.deviceName
import net.mamoe.mirai.contact.platform
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.logMessageReceived
import net.mamoe.mirai.internal.contact.replaceMagicCodes
import net.mamoe.mirai.internal.createOtherClient
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.KnownPacketFactories.PacketFactoryIllegalStateException
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetMsg
import net.mamoe.mirai.internal.network.protocol.packet.login.ConfigPushSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.Heartbeat
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.*
import net.mamoe.mirai.internal.utils.NoRouteToHostException
import net.mamoe.mirai.internal.utils.PlatformSocket
import net.mamoe.mirai.internal.utils.SocketException
import net.mamoe.mirai.internal.utils.UnknownHostException
import net.mamoe.mirai.network.*
import net.mamoe.mirai.utils.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

@Suppress("MemberVisibilityCanBePrivate")
internal class QQAndroidBotNetworkHandler(coroutineContext: CoroutineContext, bot: QQAndroidBot) : BotNetworkHandler() {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    override val supervisor: CompletableJob = SupervisorJob(coroutineContext[Job])
    override val logger: MiraiLogger get() = bot.configuration.networkLoggerSupplier(bot)

    override val coroutineContext: CoroutineContext = coroutineContext + CoroutineExceptionHandler { _, throwable ->
        logger.error("Exception in NetworkHandler", throwable)
    } + supervisor

    private lateinit var channel: PlatformSocket

    private var _packetReceiverJob: Job? = null
    private var heartbeatJob: Job? = null
    private var statHeartbeatJob: Job? = null

    private val packetReceiveLock: Mutex = Mutex()

    override fun areYouOk(): Boolean {
        return kotlin.runCatching {
            this.isActive && ::channel.isInitialized && channel.isOpen
                    && heartbeatJob?.isActive == true && _packetReceiverJob?.isActive == true
        }.getOrElse { false }
    }

    private suspend fun startPacketReceiverJobOrKill(cancelCause: CancellationException? = null): Job {
        _packetReceiverJob?.cancel(cancelCause)

        return this.launch(CoroutineName("Incoming Packet Receiver")) {
            while (channel.isOpen && isActive) {
                val rawInput = try {
                    channel.read()
                } catch (e: CancellationException) {
                    return@launch
                } catch (e: Throwable) {
                    logger.verbose { "Channel closed." }
                    if (this@QQAndroidBotNetworkHandler.isActive) {
                        bot.launch { BotOfflineEvent.Dropped(bot, e).broadcast() }
                    }
                    return@launch
                }
                packetReceiveLock.withLock {
                    processPacket(rawInput)
                }
            }
        }.also { _packetReceiverJob = it }
    }

    private fun startStatHeartbeatJobOrKill(cancelCause: CancellationException? = null): Job {
        statHeartbeatJob?.cancel(cancelCause)

        return this@QQAndroidBotNetworkHandler.launch(CoroutineName("statHeartbeatJob")) statHeartbeatJob@{
            while (this.isActive) {
                delay(bot.configuration.statHeartbeatPeriodMillis)
                val failException = doStatHeartbeat()
                if (failException != null) {
                    delay(bot.configuration.firstReconnectDelayMillis)

                    bot.launch {
                        BotOfflineEvent.Dropped(bot, failException).broadcast()
                    }
                    return@statHeartbeatJob
                }
            }
        }.also { statHeartbeatJob = it }
    }

    private fun startHeartbeatJobOrKill(cancelCause: CancellationException? = null): Job {
        heartbeatJob?.cancel(cancelCause)

        return this@QQAndroidBotNetworkHandler.launch(CoroutineName("Heartbeat")) heartBeatJob@{
            while (this.isActive) {
                delay(bot.configuration.heartbeatPeriodMillis)
                val failException = doHeartBeat()
                if (failException != null) {
                    delay(bot.configuration.firstReconnectDelayMillis)

                    bot.launch {
                        BotOfflineEvent.Dropped(bot, failException).broadcast()
                    }
                    return@heartBeatJob
                }
            }
        }.also { heartbeatJob = it }
    }

    // @param step
    //  0 -> 初始状态, 其他函数调用应永远传入 0
    //  1 -> 代表滑块验证已禁用
    override suspend fun closeEverythingAndRelogin(host: String, port: Int, cause: Throwable?, step: Int) {
        heartbeatJob?.cancel(CancellationException("relogin", cause))
        heartbeatJob?.join()
        statHeartbeatJob?.cancel(CancellationException("relogin", cause))
        statHeartbeatJob?.join()
        _packetReceiverJob?.cancel(CancellationException("relogin", cause))
        _packetReceiverJob?.join()
        if (::channel.isInitialized) {
            // if (channel.isOpen) {
            //     kotlin.runCatching {
            //         registerClientOnline(500)
            //     }.exceptionOrNull() ?: return
            //     logger.info("Cannot do fast relogin. Trying slow relogin")
            // }
            channel.close()
        }

        channel = PlatformSocket()

        while (isActive) {
            try {
                channel.connect(host, port)
                break
            } catch (e: SocketException) {
                if (e is NoRouteToHostException || e.message?.contains("Network is unreachable") == true) {
                    logger.warning { "No route to host (Mostly due to no Internet connection). Retrying in 3s..." }
                    delay(3000)
                } else {
                    throw e
                }
            } catch (e: UnknownHostException) {
                if (e is NoRouteToHostException || e.message?.contains("Network is unreachable") == true) {
                    logger.warning { "No route to host (Mostly due to no Internet connection). Retrying in 3s..." }
                    delay(3000)
                } else {
                    throw e
                }
            }
        }

        logger.info { "Connected to server $host:$port" }
        if (bot.client.wLoginSigInfoInitialized) {
            // do fast login
        } else {
            bot.initClient()
        }

        startPacketReceiverJobOrKill(CancellationException("relogin", cause))

        if (bot.client.wLoginSigInfoInitialized) {
            // do fast login
            kotlin.runCatching {
                doFastLogin()
            }.onFailure {
                bot.initClient()
                doSlowLogin(host, port, cause, step)
            }
        } else {
            doSlowLogin(host, port, cause, step)
        }


        // println("d2key=${bot.client.wLoginSigInfo.d2Key.toUHexString()}")
        registerClientOnline()
        startStatHeartbeatJobOrKill()
        startHeartbeatJobOrKill()
        bot.eventChannel.subscribeOnce<BotOnlineEvent>(this.coroutineContext) {
            val bot = (bot as QQAndroidBot)
            if (bot.firstLoginSucceed && bot.client.wLoginSigInfoInitialized) {
                launch {
                    while (isActive) {
                        bot.client.wLoginSigInfo.vKey.run {
                            //由过期时间最短的且不会被skey更换更新的vkey计算重新登录的时间
                            val delay = (expireTime - creationTime - 5).times(1000)
                            logger.info { "Scheduled refresh login session in ${delay.millisToHumanReadableString()}." }
                            delay(delay)
                        }
                        runCatching {
                            doFastLogin()
                            registerClientOnline()
                        }.onFailure {
                            logger.warning("Failed to refresh login session.", it)
                        }
                    }
                }
                launch {
                    while (isActive) {
                        bot.client.wLoginSigInfo.sKey.run {
                            val delay = (expireTime - creationTime - 5).times(1000)
                            logger.info { "Scheduled key refresh in ${delay.millisToHumanReadableString()}." }
                            delay(delay)
                        }
                        runCatching {
                            refreshKeys()
                        }.onFailure {
                            logger.error("Failed to refresh key.", it)
                        }
                    }
                }
            }
        }
    }

    private val fastLoginOrSendPacketLock = Mutex()

    private suspend fun doFastLogin() {
        fastLoginOrSendPacketLock.withLock {
            val login10 = WtLogin10(bot.client).sendAndExpect(ignoreLock = true)
            check(login10 is WtLogin.Login.LoginPacketResponse.Success) { "Fast login failed: $login10" }
        }
    }

    private suspend fun doSlowLogin(host: String, port: Int, cause: Throwable?, step: Int) {

        fun LoginSolver?.notnull(): LoginSolver {
            checkNotNull(this) {
                "No LoginSolver found. Please provide by BotConfiguration.loginSolver. " +
                        "For example use `BotFactory.newBot(...) { loginSolver = yourLoginSolver}` in Kotlin, " +
                        "use `BotFactory.newBot(..., new BotConfiguration() {{ setLoginSolver(yourLoginSolver) }})` in Java."
            }
            return this
        }

        val isSliderCaptchaSupport = bot.configuration.loginSolver?.isSliderCaptchaSupported ?: false
        val allowSlider = isSliderCaptchaSupport
                || bot.configuration.protocol == BotConfiguration.MiraiProtocol.ANDROID_PHONE
                || step == 0

        fun loginSolverNotNull() = bot.configuration.loginSolver.notnull()

        var response: WtLogin.Login.LoginPacketResponse =
            WtLogin9(bot.client, allowSlider).sendAndExpect()
        mainloop@ while (true) {
            when (response) {
                is WtLogin.Login.LoginPacketResponse.UnsafeLogin -> {
                    loginSolverNotNull().onSolveUnsafeDeviceLoginVerify(bot, response.url)
                    response = WtLogin9(bot.client, allowSlider).sendAndExpect()
                }

                is WtLogin.Login.LoginPacketResponse.Captcha -> when (response) {
                    is WtLogin.Login.LoginPacketResponse.Captcha.Picture -> {
                        var result = loginSolverNotNull().onSolvePicCaptcha(bot, response.data)
                        if (result == null || result.length != 4) {
                            //refresh captcha
                            result = "ABCD"
                        }
                        response = WtLogin2.SubmitPictureCaptcha(bot.client, response.sign, result)
                            .sendAndExpect()
                        continue@mainloop
                    }
                    is WtLogin.Login.LoginPacketResponse.Captcha.Slider -> {
                        if (!isSliderCaptchaSupport) {
                            if (step == 0) {
                                return closeEverythingAndRelogin(host, port, cause, 1)
                            }
                            throw UnsupportedSliderCaptchaException(
                                buildString {
                                    append("Mirai 无法完成滑块验证.")
                                    if (allowSlider) {
                                        append(" 使用协议 ")
                                        append(bot.configuration.protocol)
                                        append(" 强制要求滑块验证, 请更换协议后重试.")
                                    }
                                    append(" 另请参阅: https://github.com/project-mirai/mirai-login-solver-selenium")
                                }
                            )
                        }
                        val ticket = try {
                            loginSolverNotNull().onSolveSliderCaptcha(bot, response.url)
                                ?.takeIf { it.isNotEmpty() }
                                ?: return closeEverythingAndRelogin(host, port, cause, step)
                        } catch (lfe: LoginFailedException) {
                            throw lfe
                        } catch (error: Throwable) {
                            if (step == 0) {
                                logger.warning(error)
                                return closeEverythingAndRelogin(host, port, error, 1)
                            }
                            throw error
                        }
                        response = WtLogin2.SubmitSliderCaptcha(bot.client, ticket).sendAndExpect()
                        continue@mainloop
                    }
                }

                is WtLogin.Login.LoginPacketResponse.Error -> {
                    if (response.message.contains("0x9a")) { //Error(title=登录失败, message=请你稍后重试。(0x9a), errorInfo=)
                        throw RetryLaterException()
                    }
                    val msg = response.toString()
                    throw WrongPasswordException(buildString(capacity = msg.length) {
                        append(msg)
                        if (msg.contains("当前上网环境异常")) { // Error(title=禁止登录, message=当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。, errorInfo=)
                            append(", tips=若频繁出现, 请尝试开启设备锁")
                        }
                    })
                }

                is WtLogin.Login.LoginPacketResponse.DeviceLockLogin -> {
                    response = WtLogin20(
                        bot.client
                    ).sendAndExpect()
                    continue@mainloop
                }

                is WtLogin.Login.LoginPacketResponse.Success -> {
                    logger.info { "Login successful" }
                    break@mainloop
                }

                is WtLogin.Login.LoginPacketResponse.SMSVerifyCodeNeeded -> {
                    val message = "SMS required: $response, which isn't yet supported"
                    logger.error(message)
                    throw UnsupportedSMSLoginException(message)
                }
            }
        }

    }

    suspend fun refreshKeys() {
        WtLogin15(bot.client).sendAndExpect()
    }

    private suspend fun registerClientOnline(): StatSvc.Register.Response {
//        object : OutgoingPacketFactory<Packet?>("push.proxyUnRegister") {
//            override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Packet? {
//                return null
//            }
//        }.buildOutgoingUniPacket(bot.client) {}.sendWithoutExpect()
        //  kotlin.runCatching {
        //      StatSvc.Register.offline(bot.client).sendAndExpect()
        //  }.getOrElse { logger.warning(it) }

        return StatSvc.Register.online(bot.client).sendAndExpect().also {
            lastRegisterResp = it
        }
    }

    private suspend fun updateOtherClientsList() {
        val list = Mirai.getOnlineOtherClientsList(bot)
        bot.otherClients.delegate.clear()
        bot.otherClients.delegate.addAll(list.map { bot.createOtherClient(it) })

        if (bot.otherClients.isEmpty()) {
            bot.logger.info { "No OtherClient online." }
        } else {
            bot.logger.info { "Online OtherClients: " + bot.otherClients.joinToString { "${it.deviceName}(${it.platform?.name ?: "unknown platform"})" } }
        }
    }

    // caches
    private val _pendingEnabled = atomic(true)
    internal val pendingEnabled get() = _pendingEnabled.value

    @JvmField
    @Volatile
    internal var pendingIncomingPackets: ConcurrentLinkedQueue<KnownPacketFactories.IncomingPacket<*>>? =
        ConcurrentLinkedQueue()

    private val contactUpdater: ContactUpdater by lazy { ContactUpdaterImpl(bot) }
    private lateinit var lastRegisterResp: StatSvc.Register.Response

    override suspend fun init(): Unit = coroutineScope {
        check(bot.isActive) { "bot is dead therefore network can't init." }
        check(this@QQAndroidBotNetworkHandler.isActive) { "network is dead therefore can't init." }

        contactUpdater.closeAllContacts(CancellationException("re-init"))

        if (!pendingEnabled) {
            pendingIncomingPackets = ConcurrentLinkedQueue()
            _pendingEnabled.value = true
        }

        this@QQAndroidBotNetworkHandler.launch(
            CoroutineName("Awaiting ConfigPushSvc.PushReq"),
            block = ConfigPushSyncer()
        )

        launch {
            syncMessageSvc()
        }

        launch {
            bot.otherClientsLock.withLock {
                updateOtherClientsList()
            }
        }

        contactUpdater.loadAll(lastRegisterResp.origin)

        bot.firstLoginSucceed = true
        postInitActions()
    }

    @Suppress("FunctionName", "UNUSED_VARIABLE")
    private fun BotNetworkHandler.ConfigPushSyncer(): suspend CoroutineScope.() -> Unit = launch@{
        logger.info { "Awaiting ConfigPushSvc.PushReq." }
        when (val resp: ConfigPushSvc.PushReq.PushReqResponse? = nextEventOrNull(20_000)) {
            null -> {
                val hasSession = bot.bdhSyncer.hasSession
                kotlin.runCatching { bot.bdhSyncer.bdhSession.completeExceptionally(CancellationException("Timeout waiting for ConfigPushSvc.PushReq")) }
                if (!hasSession) {
                    logger.warning { "Missing ConfigPushSvc.PushReq. Switching server..." }
                    bot.launch { BotOfflineEvent.RequireReconnect(bot).broadcast() }
                } else {
                    logger.warning { "Missing ConfigPushSvc.PushReq. Using the latest response. File uploading may be affected." }
                }
            }
            is ConfigPushSvc.PushReq.PushReqResponse.ConfigPush -> {
                logger.info { "ConfigPushSvc.PushReq: Config updated." }
            }
            is ConfigPushSvc.PushReq.PushReqResponse.ServerListPush -> {
                logger.info { "ConfigPushSvc.PushReq: Server updated." }
                // handled in ConfigPushSvc
                return@launch
            }
        }
    }

    override suspend fun postInitActions() {
        _pendingEnabled.value = false
        pendingIncomingPackets?.forEach {
            runCatching {
                @Suppress("UNCHECKED_CAST")
                KnownPacketFactories.handleIncomingPacket(
                    it as KnownPacketFactories.IncomingPacket<Packet>,
                    bot,
                    it.flag2,
                    it.consumer.cast() // IDE false positive warning
                )
            }.getOrElse {
                logger.error("Exception on processing pendingIncomingPackets.", it)
            }
        }

        val list = pendingIncomingPackets
        pendingIncomingPackets = null // release, help gc
        list?.clear() // help gc

        runCatching {
            BotOnlineEvent(bot).broadcast()
        }.getOrElse {
            logger.error("Exception on broadcasting BotOnlineEvent.", it)
        }
    }

    init {
        @Suppress("RemoveRedundantQualifierName")
        val listener = bot.eventChannel
            .parentJob(supervisor)
            .subscribeAlways<BotReloginEvent>(priority = EventPriority.MONITOR) {
                this@QQAndroidBotNetworkHandler.launch { syncMessageSvc() }
            }
        supervisor.invokeOnCompletion { listener.cancel() }
    }

    private suspend fun syncMessageSvc() {
        logger.info { "Syncing friend message history..." }
        withTimeoutOrNull(30000) {
            launch(CoroutineName("Syncing friend message history")) { nextEvent<MessageSvcPbGetMsg.GetMsgSuccess> { it.bot == this@QQAndroidBotNetworkHandler.bot } }
            MessageSvcPbGetMsg(bot.client, MsgSvc.SyncFlag.START, null).sendAndExpect<Packet>()

        } ?: error("timeout syncing friend message history.")
        logger.info { "Syncing friend message history: Success." }
    }

    private suspend fun doStatHeartbeat(): Throwable? {
        return retryCatching(2) {
            StatSvc.SimpleGet(bot.client)
                .sendAndExpect<StatSvc.SimpleGet.Response>(
                    timeoutMillis = bot.configuration.heartbeatTimeoutMillis,
                    retry = 2
                )
            return null
        }.exceptionOrNull()
    }

    private suspend fun doHeartBeat(): Throwable? {
        return retryCatching(2) {
            Heartbeat.Alive(bot.client)
                .sendAndExpect<Heartbeat.Alive.Response>(
                    timeoutMillis = bot.configuration.heartbeatTimeoutMillis,
                    retry = 2
                )
            return null
        }.exceptionOrNull()
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
    @OptIn(ExperimentalCoroutinesApi::class)
    fun parsePacketAsync(input: ByteReadPacket): Job {
        return this.launch(
            start = CoroutineStart.ATOMIC
        ) {
            input.use {
                try {
                    parsePacket(it)
                } catch (e: PacketFactoryIllegalStateException) {
                    logger.warning { "Network force offline: ${e.message}" }
                    bot.launch { BotOfflineEvent.PacketFactoryErrorCode(e.code, bot, e).broadcast() }
                }
            }
        }
    }

    /**
     * 解析包内容
     * **注意**: 需要函数调用者 close 这个 [input]
     *
     * @param input 一个完整的包的内容, 去掉开头的 int 包长度
     */
    @Throws(ForceOfflineException::class)
    suspend fun parsePacket(input: ByteReadPacket) {
        if (input.isEmpty) return
        generifiedParsePacket<Packet>(input)
    }

    // with generic type, less mistakes
    private suspend fun <P : Packet?> generifiedParsePacket(input: ByteReadPacket) {
        KnownPacketFactories.parseIncomingPacket(
            bot,
            input
        ) { packetFactory: PacketFactory<P>, packet: P, commandName: String, sequenceId: Int ->
            if (packet is MultiPacket<*>) {
                packet.forEach {
                    handlePacket(null, it, commandName, sequenceId)
                }
            }
            handlePacket(packetFactory, packet, commandName, sequenceId)
        }
    }

    /**
     * 处理解析完成的包.
     */
    suspend fun <P : Packet?> handlePacket(
        packetFactory: PacketFactory<P>?,
        packet: P,
        commandName: String,
        sequenceId: Int
    ) {
        // highest priority: pass to listeners (attached by sendAndExpect).
        if (packet != null && (bot.logger.isEnabled || logger.isEnabled)) {
            when {
                packet is ParseErrorPacket -> {
                    packet.direction.getLogger(bot).error(packet.error)
                }
                packet is Packet.NoLog -> {
                    // nothing to do
                }
                packet is MessageEvent -> packet.logMessageReceived()
                packet is Event && packet !is Packet.NoEventLog -> bot.logger.verbose {
                    "Event: $packet".replaceMagicCodes()
                }
                else -> logger.verbose { "Recv: $packet".replaceMagicCodes() }
            }
        }

        packetListeners.forEach { listener ->
            if (listener.filter(commandName, sequenceId) && packetListeners.remove(listener)) {
                listener.complete(packet)
            }
        }

        packetFactory?.run {
            when (this) {
                is OutgoingPacketFactory<P> -> bot.handle(packet)
                is IncomingPacketFactory<P> -> bot.handle(packet, sequenceId)?.sendWithoutExpect()
            }
        }

        if (packet is Event) {
            if ((packet as? BroadcastControllable)?.shouldBroadcast != false) {
                if (packet is BotEvent) {
                    withContext(bot.coroutineContext[CoroutineExceptionHandler] ?: CoroutineExceptionHandler { _, t ->
                        bot.logger.warning(
                            """
                            Event processing: An exception occurred but no CoroutineExceptionHandler found in coroutineContext of bot
                        """.trimIndent(), t
                        )
                    }) {
                        packet.broadcast()
                    }
                } else {
                    packet.broadcast()
                }
            }

            if (packet is CancellableEvent && packet.isCancelled) return
        }
    }

    /**
     * 处理从服务器接收过来的包. 这些包可能是粘在一起的, 也可能是不完整的. 将会自动处理.
     * 处理后的包会调用 [parsePacketAsync]
     */
    private fun processPacket(rawInput: ByteReadPacket) {
        if (rawInput.remaining == 0L) {
            return
        }

        val cache = cachedPacket.value
        if (cache == null) {
            kotlin.runCatching {
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
                    parsePacketAsync(rawInput.readPacketExact(length))

                    if (rawInput.remaining == 0L) {
                        cachedPacket.value = null // 表示包长度正好
                        cachedPacketTimeoutJob?.cancel()
                        rawInput.close()
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
                    rawInput.close()
                    return
                }
            }.getOrElse {
                cachedPacket.value = null
                cachedPacketTimeoutJob?.cancel()
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
                cache.close()

                cachedPacket.value = null // 缺少的长度已经给上了.
                cachedPacketTimeoutJob?.cancel()

                if (rawInput.remaining != 0L) {
                    return processPacket(rawInput) // 继续处理剩下内容
                } else {
                    rawInput.close()
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
            val get = cachedPacket.getAndSet(null)
            get?.close()
            if (cachedPacketTimeoutJob == this.coroutineContext[Job] && get != null) {
                logger.warning { "等待另一部分包时超时. 将舍弃已接收的半个包" }
            }
        }
    }


    /**
     * 发送一个包, 但不期待任何返回.
     * 不推荐使用它, 可能产生意外的情况.
     */
    suspend fun OutgoingPacket.sendWithoutExpect() {
        check(bot.isActive) { "bot is dead therefore can't send ${this.commandName}" }
        check(this@QQAndroidBotNetworkHandler.isActive) { "network is dead therefore can't send ${this.commandName}" }
        check(channel.isOpen) { "network channel is closed therefore can't send ${this.commandName}" }

        logger.verbose { "Send: ${this.commandName}" }

        delegate.withUse {
            channel.send(delegate)
        }
    }

    suspend inline fun <E : Packet> OutgoingPacketWithRespType<E>.sendAndExpect(
        timeoutMillis: Long = 5000,
        retry: Int = 2,
        ignoreLock: Boolean = false,
    ): E {
        return (this as OutgoingPacket).sendAndExpect(timeoutMillis, retry, ignoreLock)
    }

    /**
     * 发送一个包, 挂起协程直到接收到指定的返回包或超时
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <E : Packet> OutgoingPacket.sendAndExpect(
        timeoutMillis: Long = 5000,
        retry: Int = 2,
        ignoreLock: Boolean = false
    ): E {
        return if (!ignoreLock) fastLoginOrSendPacketLock.withLock {
            sendAndExpectImpl(timeoutMillis, retry)
        } else sendAndExpectImpl(timeoutMillis, retry)
    }

    private suspend fun <E : Packet> OutgoingPacket.sendAndExpectImpl(timeoutMillis: Long, retry: Int): E {
        require(timeoutMillis > 100) { "timeoutMillis must > 100" }
        require(retry in 0..10) { "retry must in 0..10" }

        if (!bot.isActive) {
            throw CancellationException("bot is dead therefore can't send ${this.commandName}")
        }
        if (!this@QQAndroidBotNetworkHandler.isActive) {
            throw CancellationException("network is dead therefore can't send any packet")
        }
        if (!channel.isOpen) {
            throw CancellationException("network channel is closed")
        }

        val data = this.delegate.withUse { readBytes() }

        return retryCatchingExceptions(
            retry + 1,
            except = CancellationException::class.cast() // explicit cast due for stupid IDE.
            // CancellationException means network closed so don't retry
        ) {
            withPacketListener(commandName, sequenceId) { listener ->
                @Suppress("UNCHECKED_CAST")
                return withTimeout(timeoutMillis) { // may throw CancellationException
                    channel.send(data, 0, data.size)
                    logger.verbose { "Send: $commandName" }

                    listener.await()
                } as E
            }
        }.getOrThrow<E>()
    }

    private inline fun <R> withPacketListener(commandName: String, sequenceId: Int, block: (PacketListener) -> R): R {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        val handler = PacketListener(commandName = commandName, sequenceId = sequenceId)
        packetListeners.add(handler)
        try {
            return block(handler)
        } finally {
            kotlin.runCatching { if (handler.isActive) handler.cancel() } // ensure coroutine completion
            packetListeners.remove(handler)
        }
    }

    @PublishedApi
    internal val packetListeners: ConcurrentLinkedQueue<PacketListener> = ConcurrentLinkedQueue()

    @PublishedApi
    internal inner class PacketListener(
        // callback
        val commandName: String,
        val sequenceId: Int
    ) : CompletableDeferred<Packet?> by CompletableDeferred(supervisor) {
        fun filter(commandName: String, sequenceId: Int) =
            this.commandName == commandName && this.sequenceId == sequenceId
    }

    init {
        this.supervisor.invokeOnCompletion {
            close(it)
        }
    }

    override fun close(cause: Throwable?) {
        if (::channel.isInitialized) {
            channel.close()
        }
        super.close(cause)
    }

    override suspend fun join() = supervisor.join()
}