package net.mamoe.mirai.network.protocol.tim

import kotlinx.coroutines.*
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
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.protocol.tim.handler.*
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.*


/**
 * [BotNetworkHandler] 的 TIM PC 协议实现
 *
 * @see BotNetworkHandler
 */
internal class TIMBotNetworkHandler internal constructor(private val bot: Bot) : BotNetworkHandler<TIMBotNetworkHandler.BotSocket>, PacketHandlerList() {
    override val NetworkScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    override lateinit var socket: BotSocket

    internal val temporaryPacketHandlers = mutableListOf<TemporaryPacketHandler<*>>()

    private var heartbeatJob: Job? = null


    override suspend fun addHandler(temporaryPacketHandler: TemporaryPacketHandler<*>) {
        temporaryPacketHandlers.add(temporaryPacketHandler)
        temporaryPacketHandler.send(this[ActionPacketHandler].session)
    }

    override suspend fun login(configuration: LoginConfiguration): LoginResult {
        TIMProtocol.SERVER_IP.forEach {
            bot.logger.logInfo("Connecting server $it")
            this.socket = BotSocket(it, configuration)

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
        val session = LoginSession(bot, sessionKey, socket, NetworkScope)

        add(EventPacketHandler(session).asNode(EventPacketHandler))
        add(ActionPacketHandler(session).asNode(ActionPacketHandler))
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

    internal inner class BotSocket(override val serverIp: String, val configuration: LoginConfiguration) : DataPacketSocket {
        override val channel: PlatformDatagramChannel = PlatformDatagramChannel(serverIp, 8000)

        override val isOpen: Boolean get() = channel.isOpen

        private lateinit var loginHandler: LoginHandler

        private suspend fun processReceive() {
            while (channel.isOpen) {
                val buffer = IoBuffer.Pool.borrow()

                try {
                    channel.read(buffer)//JVM: withContext(IO)
                } catch (e: ReadPacketInternalException) {

                } catch (e: Exception) {
                    e.log()
                    continue
                }

                if (!buffer.canRead() || buffer.readRemaining == 0) {//size==0
                    buffer.release(IoBuffer.Pool)
                    continue
                }

                NetworkScope.launch {
                    try {
                        //Ensure the packet is consumed totally so that all buffers are released
                        ByteReadPacket(buffer, IoBuffer.Pool).use {
                            distributePacket(it.parseServerPacket(buffer.readRemaining))
                        }
                    } catch (e: Exception) {
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
            sendPacket(ClientTouchPacket(bot.qqNumber, this.serverIp))

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
                bot.printPacketDebugging(packet)
                packet.close()
                throw e
            }

            packet.use {
                //coz removeIf is not inline
                temporaryPacketHandlers.removeIfInlined {
                    it.onPacketReceived(this@TIMBotNetworkHandler[ActionPacketHandler].session, packet)
                }

                val name = packet::class.simpleName
                if (name != null && !name.endsWith("Encrypted") && !name.endsWith("Raw")) {
                    bot.cyan("Packet received: $packet")
                }

                if (packet is ServerEventPacket) {
                    //no need to sync acknowledgement packets
                    NetworkScope.launch {
                        sendPacket(packet.ResponsePacket(bot.qqNumber, sessionKey))
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
                } catch (e: Throwable) {
                    e.log()
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
                        socket = BotSocket(packet.serverIP!!, socket.configuration)
                        bot.logger.logPurple("Redirecting to ${packet.serverIP}")
                        loginResult.complete(socket.resendTouch())
                    } else {//password submission
                        this.loginIP = packet.loginIP
                        this.loginTime = packet.loginTime
                        this.token0825 = packet.token0825
                        socket.sendPacket(ClientPasswordSubmissionPacket(bot.qqNumber, bot.account.password, packet.loginTime, packet.loginIP, this.privateKey, packet.token0825, socket.configuration.randomDeviceName))
                    }
                }

                is ServerLoginResponseFailedPacket -> {
                    loginResult.complete(packet.loginResult)
                    bot.close()
                    return
                }

                is ServerCaptchaCorrectPacket -> {
                    this.privateKey = getRandomByteArray(16)//似乎是必须的
                    this.token00BA = packet.token00BA

                    socket.sendPacket(ClientLoginResendPacket3105(bot.qqNumber, bot.account.password, this.loginTime, this.loginIP, this.privateKey, this.token0825, packet.token00BA, socket.configuration.randomDeviceName))
                }

                is ServerLoginResponseVerificationCodeInitPacket -> {
                    //[token00BA]来源之一: 验证码
                    this.token00BA = packet.token00BA
                    this.captchaCache = packet.verifyCodePart1

                    if (packet.unknownBoolean == true) {
                        this.captchaSectionId = 1
                        socket.sendPacket(ClientVerificationCodeTransmissionRequestPacket(1, bot.qqNumber, this.token0825, this.captchaSectionId++, packet.token00BA))
                    }
                }

                is ServerCaptchaTransmissionPacket -> {
                    if (packet is ServerCaptchaWrongPacket) {
                        bot.error("验证码错误, 请重新输入")
                        captchaSectionId = 1
                        this.captchaCache = null
                    }

                    this.captchaCache!!.writeFully(packet.captchaSectionN)
                    this.token00BA = packet.token00BA

                    if (packet.transmissionCompleted) {
                        val code = solveCaptcha(captchaCache!!)
                        if (code == null) {
                            this.captchaCache = null
                            this.captchaSectionId = 1
                            socket.sendPacket(ClientVerificationCodeRefreshPacket(packet.packetIdLast + 1, bot.qqNumber, token0825))
                        } else {
                            socket.sendPacket(ClientVerificationCodeSubmitPacket(packet.packetIdLast + 1, bot.qqNumber, token0825, code, packet.verificationToken))
                        }
                    } else {
                        socket.sendPacket(ClientVerificationCodeTransmissionRequestPacket(packet.packetIdLast + 1, bot.qqNumber, token0825, captchaSectionId++, packet.token00BA))
                    }
                }

                is ServerLoginResponseSuccessPacket -> {
                    this.sessionResponseDecryptionKey = packet.sessionResponseDecryptionKey
                    socket.sendPacket(ClientSessionRequestPacket(bot.qqNumber, socket.serverIp, packet.token38, packet.token88, packet.encryptionKey))
                }

                //是ClientPasswordSubmissionPacket之后服务器回复的
                is ServerLoginResponseKeyExchangePacket -> {
                    //if (packet.tokenUnknown != null) {
                    //this.token00BA = packet.token00BA!!
                    //println("token00BA changed!!! to " + token00BA.toUByteArray())
                    //}
                    if (packet.flag == ServerLoginResponseKeyExchangePacket.Flag.`08 36 31 03`) {
                        this.privateKey = packet.privateKeyUpdate
                        socket.sendPacket(ClientLoginResendPacket3104(bot.qqNumber, bot.account.password, loginTime, loginIP, privateKey, token0825, packet.tokenUnknown
                                ?: token00BA, socket.configuration.randomDeviceName, packet.tlv0006))
                    } else {
                        socket.sendPacket(ClientLoginResendPacket3106(bot.qqNumber, bot.account.password, loginTime, loginIP, privateKey, token0825, packet.tokenUnknown
                                ?: token00BA, socket.configuration.randomDeviceName, packet.tlv0006))
                    }
                }

                is ServerSessionKeyResponsePacket -> {
                    sessionKey = packet.sessionKey

                    heartbeatJob = NetworkScope.launch {
                        while (socket.isOpen) {
                            delay(90000)
                            socket.sendPacket(ClientHeartbeatPacket(bot.qqNumber, sessionKey))
                        }
                    }

                    loginResult.complete(LoginResult.SUCCESS)

                    setOnlineStatus(OnlineStatus.ONLINE)//required
                }

                is ServerLoginSuccessPacket -> {
                    BotLoginSucceedEvent(bot).broadcast()

                    //登录成功后会收到大量上次的消息, 忽略掉 todo 优化
                    NetworkScope.launch {
                        delay(3000)
                        this@TIMBotNetworkHandler[EventPacketHandler].ignoreMessage = false
                    }

                    onLoggedIn(sessionKey)

                    this.close()//The LoginHandler is useless since then
                }


                is ServerCaptchaPacket.Encrypted -> socket.distributePacket(packet.decrypt())
                is ServerLoginResponseVerificationCodeInitPacket.Encrypted -> socket.distributePacket(packet.decrypt())
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
            socket.sendPacket(ClientChangeOnlineStatusPacket(bot.qqNumber, sessionKey, status))
        }

        fun close() {
            this.captchaCache = null

            if (::sessionResponseDecryptionKey.isInitialized) this.sessionResponseDecryptionKey.release(IoBuffer.Pool)
        }
    }
}
