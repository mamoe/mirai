package net.mamoe.mirai.network.protocol.tim

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.*
import net.mamoe.mirai.*
import net.mamoe.mirai.event.EventScope
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BeforePacketSendEvent
import net.mamoe.mirai.event.events.BotLoginSucceedEvent
import net.mamoe.mirai.event.events.PacketSentEvent
import net.mamoe.mirai.event.events.ServerPacketReceivedEvent
import net.mamoe.mirai.event.subscribe
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.handler.*
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.*


/**
 * [BotNetworkHandler] 的 TIM PC 协议实现
 *
 * @see BotNetworkHandler
 */
internal class TIMBotNetworkHandler internal constructor(private val bot: Bot) : BotNetworkHandler<TIMBotNetworkHandler.BotSocketAdapter>, PacketHandlerList() {
    override val NetworkScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    override lateinit var socket: BotSocketAdapter
        private set

    internal val temporaryPacketHandlers = mutableListOf<TemporaryPacketHandler<*>>()
    private val handlersLock = Mutex()

    private var heartbeatJob: Job? = null


    override suspend fun addHandler(temporaryPacketHandler: TemporaryPacketHandler<*>) {
        handlersLock.withLock {
            temporaryPacketHandlers.add(temporaryPacketHandler)
        }
        temporaryPacketHandler.send(this[ActionPacketHandler].session)
    }

    override suspend fun login(configuration: LoginConfiguration): LoginResult {
        TIMProtocol.SERVER_IP.forEach {
            bot.logger.logInfo("Connecting server $it")
            this.socket = BotSocketAdapter(it, configuration)

            loginResult = CompletableDeferred()

            val state = socket.resendTouch()

            if (state != LoginResult.TIMEOUT) {
                return state
            }
            bot.logger.logPurple("Timeout. Retrying next server")

            socket.close()
        }
        return LoginResult.TIMEOUT
    }

    internal var loginResult: CompletableDeferred<LoginResult> = CompletableDeferred()


    //private | internal
    private fun onLoggedIn(sessionKey: ByteArray) {
        require(size == 0) { "Already logged in" }
        val session = BotSession(bot, sessionKey, socket, NetworkScope)

        add(EventPacketHandler(session).asNode(EventPacketHandler))
        add(ActionPacketHandler(session).asNode(ActionPacketHandler))
        bot.logger.logPurple("Successfully logged in")
    }

    private lateinit var sessionKey: ByteArray

    override fun close() {
        super.close()

        this.heartbeatJob?.cancel(CancellationException("handler closed"))
        this.heartbeatJob = null

        if (!this.loginResult.isCompleted && !this.loginResult.isCancelled) {
            this.loginResult.cancel(CancellationException("socket closed"))
        }

        this.forEach {
            it.instance.close()
        }

        this.socket.close()
    }

    override suspend fun sendPacket(packet: ClientPacket) = socket.sendPacket(packet)

    internal inner class BotSocketAdapter(override val serverIp: String, val configuration: LoginConfiguration) : DataPacketSocketAdapter {
        override val channel: PlatformDatagramChannel = PlatformDatagramChannel(serverIp, 8000)

        override val isOpen: Boolean get() = channel.isOpen

        private lateinit var loginHandler: LoginHandler

        private suspend fun processReceive() {
            while (channel.isOpen) {
                val buffer = IoBuffer.Pool.borrow()

                try {
                    channel.read(buffer)//JVM: withContext(IO)
                } catch (e: ReadPacketInternalException) {
                    //read failed, continue and reread
                    continue
                } catch (e: Throwable) {
                    e.log()//other unexpected exceptions caught.
                    continue
                }

                if (!buffer.canRead() || buffer.readRemaining == 0) {//size==0
                    buffer.release(IoBuffer.Pool)
                    continue
                }

                NetworkScope.launch {
                    try {
                        //`.use`: Ensure that the packet is consumed totally so that all the buffers are released
                        ByteReadPacket(buffer, IoBuffer.Pool).use {
                            distributePacket(it.parseServerPacket(buffer.readRemaining))
                        }
                    } catch (e: Throwable) {
                        e.log()
                    }
                }
            }
        }

        internal suspend fun resendTouch(): LoginResult {
            if (::loginHandler.isInitialized) loginHandler.close()

            loginHandler = LoginHandler()


            val expect = expectPacket<ServerTouchResponsePacket>()
            NetworkScope.launch { processReceive() }
            NetworkScope.launch {
                if (withTimeoutOrNull(configuration.touchTimeoutMillis) { expect.join() } == null) {
                    loginResult.complete(LoginResult.TIMEOUT)
                }
            }
            sendPacket(ClientTouchPacket(bot.qqAccount, this.serverIp))

            return loginResult.await()
        }

        private inline fun <reified P : ServerPacket> expectPacket(): CompletableDeferred<P> {
            val receiving = CompletableDeferred<P>()
            subscribe<ServerPacketReceivedEvent> {
                if (it.packet is P && it.bot === bot) {
                    receiving.complete(it.packet)
                    ListeningStatus.STOPPED
                } else
                    ListeningStatus.LISTENING
            }
            return receiving
        }

        override suspend fun distributePacket(packet: ServerPacket) {
            try {
                packet.decode()
            } catch (e: Exception) {
                e.log()
                bot.printPacketDebugging(packet)
                packet.close()
                throw e
            }

            packet.use {
                //coz removeIf is not inline
                handlersLock.withLock {
                    temporaryPacketHandlers.removeIfInlined {
                        it.shouldRemove(this@TIMBotNetworkHandler[ActionPacketHandler].session, packet)
                    }
                }

                val name = packet::class.simpleName
                if (name != null && !name.endsWith("Encrypted") && !name.endsWith("Raw")) {
                    bot.cyan("Packet received: $packet")
                }

                if (packet is ServerEventPacket) {
                    //no need to sync acknowledgement packets
                    NetworkScope.launch {
                        sendPacket(packet.ResponsePacket(bot.qqAccount, sessionKey))
                    }
                }

                if (ServerPacketReceivedEvent(bot, packet).broadcast().cancelled) {
                    return
                }

                loginHandler.onPacketReceived(packet)
                this@TIMBotNetworkHandler.forEach {
                    it.instance.onPacketReceived(packet)
                }
            }
        }

        /* todo 修改为这个模式是否更好?

        interface Pk

        object TestPacket : Pk {
            operator fun invoke(bot: UInt): TestPacket.(BytePacketBuilder) -> Unit {

            }
        }

        override inline fun <reified P : Pk> send(p: P.(BytePacketBuilder) -> Unit): UShort {
            val encoded = with(P::class.objectInstance!!){
                buildPacket {
                    this@with.p(this)
                }
            }
        }*/

        override suspend fun sendPacket(packet: ClientPacket) = withContext(NetworkScope.coroutineContext) {
            check(channel.isOpen) { "channel is not open" }

            if (BeforePacketSendEvent(bot, packet).broadcast().cancelled) {
                return@withContext
            }

            packet.packet.use { build ->
                val buffer = IoBuffer.Pool.borrow()
                try {
                    build.readAvailable(buffer)
                    channel.send(buffer)//JVM: withContext(IO)
                } catch (e: SendPacketInternalException) {
                    bot.logger.logError("Caught SendPacketInternalException: ${e.cause?.message}")
                    bot.reinitializeNetworkHandler(configuration)
                    return@withContext
                } finally {
                    buffer.release(IoBuffer.Pool)
                }
            }

            bot.green("Packet sent:     $packet")

            EventScope.launch { PacketSentEvent(bot, packet).broadcast() }
        }

        override val owner: Bot get() = this@TIMBotNetworkHandler.bot

        override fun close() {
            if (::loginHandler.isInitialized) loginHandler.close()
            this.channel.close()
        }
    }


    /**
     * 处理登录过程
     */
    inner class LoginHandler {
        private lateinit var token00BA: ByteArray
        private lateinit var token0825: ByteArray//56
        private var loginTime: Int = 0
        private lateinit var loginIP: String
        private var privateKey: ByteArray = getRandomByteArray(16)

        /**
         * 0828_decr_key
         */
        private lateinit var sessionResponseDecryptionKey: IoBuffer

        private var captchaSectionId: Int = 1
        private var captchaCache: IoBuffer? = null
            get() {
                if (field == null) field = IoBuffer.Pool.borrow()
                return field
            }
            set(value) {
                if (value == null) {
                    field?.release(IoBuffer.Pool)
                }
                field = value
            }

        suspend fun onPacketReceived(packet: ServerPacket) {
            when (packet) {
                is ServerTouchResponsePacket -> {
                    if (packet.serverIP != null) {//redirection
                        socket.close()
                        socket = BotSocketAdapter(packet.serverIP!!, socket.configuration)
                        bot.logger.logPurple("Redirecting to ${packet.serverIP}")
                        loginResult.complete(socket.resendTouch())
                    } else {//password submission
                        this.loginIP = packet.loginIP
                        this.loginTime = packet.loginTime
                        this.token0825 = packet.token0825

                        socket.sendPacket(ClientPasswordSubmissionPacket(
                                bot = bot.qqAccount,
                                password = bot.account.password,
                                loginTime = loginTime,
                                loginIP = loginIP,
                                privateKey = privateKey,
                                token0825 = token0825,
                                token00BA = null,
                                randomDeviceName = socket.configuration.randomDeviceName
                        ))
                    }
                }

                is ServerLoginResponseFailedPacket -> {
                    loginResult.complete(packet.loginResult)
                    return
                }

                is ServerCaptchaCorrectPacket -> {
                    this.privateKey = getRandomByteArray(16)//似乎是必须的
                    this.token00BA = packet.token00BA

                    socket.sendPacket(ClientPasswordSubmissionPacket(
                            bot = bot.qqAccount,
                            password = bot.account.password,
                            loginTime = loginTime,
                            loginIP = loginIP,
                            privateKey = privateKey,
                            token0825 = token0825,
                            token00BA = packet.token00BA,
                            randomDeviceName = socket.configuration.randomDeviceName
                    ))
                }

                is ServerLoginResponseCaptchaInitPacket -> {
                    //[token00BA]来源之一: 验证码
                    this.token00BA = packet.token00BA
                    this.captchaCache = packet.captchaPart1

                    if (packet.unknownBoolean == true) {
                        this.captchaSectionId = 1
                        socket.sendPacket(ClientCaptchaTransmissionRequestPacket(bot.qqAccount, this.token0825, this.captchaSectionId++, packet.token00BA))
                    }
                }

                is ServerCaptchaTransmissionPacket -> {
                    //packet is ServerCaptchaWrongPacket
                    if (this.captchaSectionId == 0) {
                        bot.error("验证码错误, 请重新输入")
                        this.captchaSectionId = 1
                        this.captchaCache = null
                    }

                    this.captchaCache!!.writeFully(packet.captchaSectionN)
                    this.token00BA = packet.token00BA

                    if (packet.transmissionCompleted) {
                        val code = solveCaptcha(captchaCache!!)

                        this.captchaCache = null
                        if (code == null) {
                            this.captchaSectionId = 1//意味着正在刷新验证码
                            socket.sendPacket(ClientCaptchaRefreshPacket(bot.qqAccount, token0825))
                        } else {
                            this.captchaSectionId = 0//意味着已经提交验证码
                            socket.sendPacket(ClientCaptchaSubmitPacket(bot.qqAccount, token0825, code, packet.verificationToken))
                        }
                    } else {
                        socket.sendPacket(ClientCaptchaTransmissionRequestPacket(bot.qqAccount, token0825, captchaSectionId++, packet.token00BA))
                    }
                }

                is ServerLoginResponseSuccessPacket -> {
                    this.sessionResponseDecryptionKey = packet.sessionResponseDecryptionKey
                    socket.sendPacket(ClientSessionRequestPacket(bot.qqAccount, socket.serverIp, packet.token38, packet.token88, packet.encryptionKey))
                }

                //是ClientPasswordSubmissionPacket之后服务器回复的可能之一
                is ServerLoginResponseKeyExchangePacket -> {
                    this.privateKey = packet.privateKeyUpdate

                    socket.sendPacket(ClientPasswordSubmissionPacket(
                            bot = bot.qqAccount,
                            password = bot.account.password,
                            loginTime = loginTime,
                            loginIP = loginIP,
                            privateKey = privateKey,
                            token0825 = token0825,
                            token00BA = packet.tokenUnknown ?: token00BA,
                            randomDeviceName = socket.configuration.randomDeviceName,
                            tlv0006 = packet.tlv0006
                    ))
                }

                is ServerSessionKeyResponsePacket -> {
                    sessionKey = packet.sessionKey
                    bot.logger.logPurple("sessionKey = ${sessionKey.toUHexString()}")

                    heartbeatJob = NetworkScope.launch {
                        while (socket.isOpen) {
                            delay(90000)
                            socket.sendPacket(ClientHeartbeatPacket(bot.qqAccount, sessionKey))
                        }
                    }

                    loginResult.complete(LoginResult.SUCCESS)

                    setOnlineStatus(OnlineStatus.ONLINE)//required
                }

                is ServerLoginSuccessPacket -> {
                    BotLoginSucceedEvent(bot).broadcast()

                    onLoggedIn(sessionKey)
                    this.close()//The LoginHandler is useless since then
                }


                is ServerCaptchaPacket.Encrypted -> socket.distributePacket(packet.decrypt())
                is ServerLoginResponseCaptchaInitPacket.Encrypted -> socket.distributePacket(packet.decrypt())
                is ServerLoginResponseKeyExchangePacket.Encrypted -> socket.distributePacket(packet.decrypt(this.privateKey))
                is ServerLoginResponseSuccessPacket.Encrypted -> socket.distributePacket(packet.decrypt(this.privateKey))
                is ServerSessionKeyResponsePacket.Encrypted -> socket.distributePacket(packet.decrypt(this.sessionResponseDecryptionKey))
                is ServerTouchResponsePacket.Encrypted -> socket.distributePacket(packet.decrypt())


                is ServerHeartbeatResponsePacket -> {

                }

                is UnknownServerPacket.Encrypted -> socket.distributePacket(packet.decrypt(sessionKey))
                else -> {

                }
            }
        }

        @Suppress("MemberVisibilityCanBePrivate")
        suspend fun setOnlineStatus(status: OnlineStatus) {
            socket.sendPacket(ClientChangeOnlineStatusPacket(bot.qqAccount, sessionKey, status))
        }

        fun close() {
            this.captchaCache = null

            if (::sessionResponseDecryptionKey.isInitialized) this.sessionResponseDecryptionKey.release(IoBuffer.Pool)
        }
    }
}
