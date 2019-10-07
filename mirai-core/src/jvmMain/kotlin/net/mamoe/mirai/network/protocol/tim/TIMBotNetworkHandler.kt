package net.mamoe.mirai.network.protocol.tim

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.bot.BotLoginSucceedEvent
import net.mamoe.mirai.event.events.network.BeforePacketSendEvent
import net.mamoe.mirai.event.events.network.PacketSentEvent
import net.mamoe.mirai.event.events.network.ServerPacketReceivedEvent
import net.mamoe.mirai.event.subscribe
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.NetworkScope
import net.mamoe.mirai.network.protocol.tim.handler.*
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.*
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO

/**
 * [BotNetworkHandler] 的内部实现, 该类不会有帮助理解的注解, 请查看 [BotNetworkHandler] 以获取帮助
 *
 * @see BotNetworkHandler
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")//to simplify code
internal class TIMBotNetworkHandler(private val bot: Bot) : BotNetworkHandler {
    override val socket: BotSocket = BotSocket()

    lateinit var loginHandler: LoginHandler

    override lateinit var message: MessagePacketHandler
    override lateinit var action: ActionPacketHandler

    val packetHandlers: PacketHandlerList = PacketHandlerList()

    internal val temporaryPacketHandlers = Collections.synchronizedList(mutableListOf<TemporaryPacketHandler<*>>())


    override suspend fun addHandler(temporaryPacketHandler: TemporaryPacketHandler<*>) {
        temporaryPacketHandler.send(action.session)
        temporaryPacketHandlers.add(temporaryPacketHandler)
    }

    override suspend fun login(): LoginState {
        return loginInternal(LinkedList(TIMProtocol.SERVER_IP))
    }

    private suspend fun loginInternal(ipQueue: LinkedList<String>): LoginState {
        this.socket.close()
        val ip = ipQueue.poll() ?: return LoginState.UNKNOWN//所有服务器均返回 UNKNOWN

        return socket.touch(ip).let { state ->
            if (state == LoginState.UNKNOWN || state == LoginState.TIMEOUT) {
                loginInternal(ipQueue)//超时或未知, 重试连接下一个服务器
            } else {
                state
            }
        }
    }

    //private | internal
    private fun onLoggedIn(sessionKey: ByteArray) {
        val session = LoginSession(bot, sessionKey, socket)
        message = MessagePacketHandler(session)
        action = ActionPacketHandler(session)

        packetHandlers.add(message.asNode())
        packetHandlers.add(action.asNode())
    }

    private lateinit var sessionKey: ByteArray

    override fun close() {
        this.packetHandlers.forEach {
            it.instance.close()
        }
        this.socket.close()
    }


    internal inner class BotSocket : DataPacketSocket {
        override suspend fun distributePacket(packet: ServerPacket) {
            try {
                packet.decode()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                bot.debugPacket(packet)
                return
            }

            with(temporaryPacketHandlers.iterator()) {
                while (hasNext()) {
                    if (next().onPacketReceived(action.session, packet)) {
                        remove()
                    }
                }
            }

            //For debug
            kotlin.run {
                if (!packet.javaClass.name.endsWith("Encrypted") && !packet.javaClass.name.endsWith("Raw")) {
                    bot.notice("Packet received: $packet")
                }
            }

            if (packet is ServerEventPacket) {
                //no need to sync acknowledgement packets
                NetworkScope.launch {
                    sendPacket(ClientEventResponsePacket(bot.account.qqNumber, packet.packetId, sessionKey, packet.eventIdentity))
                }
            }

            if (ServerPacketReceivedEvent(bot, packet).broadcast().cancelled) {
                return
            }

            withContext(NetworkScope.coroutineContext + CoroutineExceptionHandler { _, e -> e.printStackTrace() }) {
                launch(this.coroutineContext) {
                    loginHandler.onPacketReceived(packet)
                }


                packetHandlers.forEach {
                    launch(this.coroutineContext) {
                        it.instance.onPacketReceived(packet)
                    }
                }
            }//awaits all coroutines launched in this block
        }

        private var socket: DatagramSocket? = null

        internal var serverIP: String = ""
            set(value) {
                field = value

                restartSocket()
            }

        internal lateinit var loginResult: CompletableDeferred<LoginState>

        @Synchronized
        private fun restartSocket() {
            socket?.close()
            socket = DatagramSocket(0)
            socket!!.connect(InetSocketAddress(serverIP, 8000))
            NetworkScope.launch {
                while (socket?.isConnected == true) {
                    val packet = DatagramPacket(ByteArray(2048), 2048)
                    kotlin.runCatching { withContext(Dispatchers.IO) { socket?.receive(packet) } }
                            .onSuccess {
                                NetworkScope.launch {
                                    distributePacket(ServerPacket.ofByteArray(packet.data.removeZeroTail()))
                                }
                            }.onFailure {
                                if (it.message == "Socket closed" || it.message == "socket closed") {
                                    return@launch
                                }
                                it.printStackTrace()
                            }

                }
            }
        }

        internal suspend fun touch(serverAddress: String): LoginState {
            bot.info("Connecting server: $serverAddress")
            restartSocket()
            if (this@TIMBotNetworkHandler::loginHandler.isInitialized) {
                loginHandler.close()
            }
            loginHandler = LoginHandler()
            this.loginResult = CompletableDeferred()

            serverIP = serverAddress
            //bot.waitForPacket(ServerTouchResponsePacket::class, timeoutMillis) {
            //    loginResult?.complete(LoginState.TIMEOUT)
            //}
            val received = AtomicBoolean(false)
            ServerPacketReceivedEvent.subscribe {
                if (it.packet is ServerTouchResponsePacket && it.bot === bot) {
                    received.set(true)
                    ListeningStatus.STOPPED
                } else
                    ListeningStatus.LISTENING
            }
            NetworkScope.launch {
                delay(2000)
                if (!received.get()) {
                    loginResult.complete(LoginState.TIMEOUT)
                }
            }
            sendPacket(ClientTouchPacket(bot.account.qqNumber, serverIP))

            return withContext(Dispatchers.IO) {
                loginResult.await()
            }
        }

        override suspend fun sendPacket(packet: ClientPacket) {
            checkNotNull(socket) { "network closed" }
            if (socket!!.isClosed) {
                return
            }

            try {
                packet.encodePacket()

                if (BeforePacketSendEvent(bot, packet).broadcast().cancelled) {
                    return
                }

                val data = packet.toByteArray()
                withContext(Dispatchers.IO) {
                    socket!!.send(DatagramPacket(data, data.size))
                }
                bot.cyan("Packet sent:     $packet")

                PacketSentEvent(bot, packet).broadcast()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        override fun getOwner(): Bot = this@TIMBotNetworkHandler.bot


        override fun close() {
            this.socket?.close()
            if (this::loginResult.isInitialized) {
                if (!this.loginResult.isCompleted && !this.loginResult.isCancelled) {
                    this.loginResult.cancel(CancellationException("socket closed"))
                }
            }
        }

        override fun isClosed(): Boolean {
            return this.socket?.isClosed ?: true
        }
    }

    companion object {
        val captchaLock = Mutex()
    }

    /**
     * 处理登录过程
     */
    inner class LoginHandler {
        private lateinit var token00BA: ByteArray
        private lateinit var token0825: ByteArray//56
        private var loginTime: Int = 0
        private lateinit var loginIP: String
        private var randomprivateKey: ByteArray = getRandomByteArray(16)

        /**
         * 0828_decr_key
         */
        private lateinit var sessionResponseDecryptionKey: ByteArray

        private var captchaSectionId: Int = 1
        private var captchaCache: ByteArray? = byteArrayOf()//每次包只发一部分验证码来

        private var heartbeatJob: Job? = null


        suspend fun onPacketReceived(packet: ServerPacket) {
            when (packet) {
                is ServerTouchResponsePacket -> {
                    if (packet.serverIP != null) {//redirection
                        socket.serverIP = packet.serverIP!!
                        //connect(packet.serverIP!!)
                        socket.sendPacket(ClientServerRedirectionPacket(packet.serverIP!!, bot.account.qqNumber))
                    } else {//password submission
                        this.loginIP = packet.loginIP
                        this.loginTime = packet.loginTime
                        this.token0825 = packet.token0825
                        println("token0825=" + this.token0825.toUHexString())
                        socket.sendPacket(ClientPasswordSubmissionPacket(bot.account.qqNumber, bot.account.password, packet.loginTime, packet.loginIP, this.randomprivateKey, packet.token0825))
                    }
                }

                is ServerLoginResponseFailedPacket -> {
                    socket.loginResult.complete(packet.loginState)
                    bot.close()
                    return
                }

                is ServerCaptchaCorrectPacket -> {
                    this.randomprivateKey = getRandomByteArray(16)
                    this.token00BA = packet.token00BA
                    socket.sendPacket(ClientLoginResendPacket3105(bot.account.qqNumber, bot.account.password, this.loginTime, this.loginIP, this.randomprivateKey, this.token0825, this.token00BA))
                }

                is ServerLoginResponseVerificationCodeInitPacket -> {
                    //[token00BA]来源之一: 验证码
                    this.token00BA = packet.token00BA
                    this.captchaCache = packet.verifyCodePart1

                    if (packet.unknownBoolean == true) {
                        this.captchaSectionId = 1
                        socket.sendPacket(ClientVerificationCodeTransmissionRequestPacket(1, bot.account.qqNumber, this.token0825, this.captchaSectionId++, this.token00BA))
                    }
                }

                is ServerCaptchaTransmissionPacket -> {
                    if (packet is ServerCaptchaWrongPacket) {
                        bot.error("验证码错误, 请重新输入")
                        captchaSectionId = 1
                        this.captchaCache = byteArrayOf()
                    }

                    this.captchaCache = this.captchaCache!! + packet.captchaSectionN
                    this.token00BA = packet.token00BA

                    if (packet.transmissionCompleted) {
                        //todo 验证码多样化处理

                        val code = captchaLock.withLock {
                            withContext(Dispatchers.IO) {
                                bot.notice(ImageIO.read(captchaCache!!.inputStream()).createCharImg())
                            }
                            bot.notice("需要验证码登录, 验证码为 4 字母")
                            try {
                                File(System.getProperty("user.dir") + "/temp/Captcha.png")
                                        .also { withContext(Dispatchers.IO) { it.createNewFile() } }
                                        .writeBytes(this.captchaCache!!)
                                bot.notice("若看不清字符图片, 请查看 Mirai 目录下 /temp/Captcha.png")
                            } catch (e: Exception) {
                                bot.notice("无法写出验证码文件, 请尝试查看以上字符图片")
                            }
                            this.captchaCache = null
                            bot.notice("若要更换验证码, 请直接回车")
                            Scanner(System.`in`).nextLine()
                        }
                        if (code.isEmpty() || code.length != 4) {
                            this.captchaCache = byteArrayOf()
                            this.captchaSectionId = 1
                            socket.sendPacket(ClientVerificationCodeRefreshPacket(packet.packetIdLast + 1, bot.account.qqNumber, token0825))
                        } else {
                            socket.sendPacket(ClientVerificationCodeSubmitPacket(packet.packetIdLast + 1, bot.account.qqNumber, token0825, code, packet.verificationToken))
                        }
                    } else {
                        socket.sendPacket(ClientVerificationCodeTransmissionRequestPacket(packet.packetIdLast + 1, bot.account.qqNumber, token0825, captchaSectionId++, token00BA))
                    }
                }

                is ServerLoginResponseSuccessPacket -> {
                    this.sessionResponseDecryptionKey = packet.sessionResponseDecryptionKey
                    socket.sendPacket(ClientSessionRequestPacket(bot.account.qqNumber, socket.serverIP, packet.token38, packet.token88, packet.encryptionKey))
                }

                //是ClientPasswordSubmissionPacket之后服务器回复的
                is ServerLoginResponseKeyExchangePacket -> {
                    //if (packet.tokenUnknown != null) {
                    //this.token00BA = packet.token00BA!!
                    //println("token00BA changed!!! to " + token00BA.toUByteArray())
                    //}
                    if (packet.flag == ServerLoginResponseKeyExchangePacket.Flag.`08 36 31 03`) {
                        this.randomprivateKey = packet.privateKey
                        socket.sendPacket(ClientLoginResendPacket3104(bot.account.qqNumber, bot.account.password, loginTime, loginIP, randomprivateKey, token0825, packet.tokenUnknown
                                ?: this.token00BA, packet.tlv0006))
                    } else {
                        socket.sendPacket(ClientLoginResendPacket3106(bot.account.qqNumber, bot.account.password, loginTime, loginIP, randomprivateKey, token0825, packet.tokenUnknown
                                ?: token00BA, packet.tlv0006))
                    }
                }

                is ServerSessionKeyResponsePacket -> {
                    sessionKey = packet.sessionKey

                    heartbeatJob = NetworkScope.launch {
                        delay(90000)
                        socket.sendPacket(ClientHeartbeatPacket(bot.account.qqNumber, sessionKey))
                    }

                    socket.loginResult.complete(LoginState.SUCCESS)

                    loginHandler.changeOnlineStatus(ClientLoginStatus.ONLINE)//required
                }

                is ServerLoginSuccessPacket -> {
                    BotLoginSucceedEvent(bot).broadcast()

                    //登录成功后会收到大量上次的消息, 忽略掉 todo 优化
                    NetworkScope.launch {
                        delay(3000)
                        message.ignoreMessage = false
                    }


                    onLoggedIn(sessionKey)
                }


                is ServerCaptchaPacket.Encrypted -> socket.distributePacket(packet.decrypt())
                is ServerLoginResponseVerificationCodeInitPacket.Encrypted -> socket.distributePacket(packet.decrypt())
                is ServerLoginResponseKeyExchangePacket.Encrypted -> socket.distributePacket(packet.decrypt(this.randomprivateKey))
                is ServerLoginResponseSuccessPacket.Encrypted -> socket.distributePacket(packet.decrypt(this.randomprivateKey))
                is ServerSessionKeyResponsePacket.Encrypted -> socket.distributePacket(packet.decrypt(this.sessionResponseDecryptionKey))
                is ServerTouchResponsePacket.Encrypted -> socket.distributePacket(packet.decrypt())


                is ServerHeartbeatResponsePacket,
                is UnknownServerPacket -> {
                    //ignored
                }
                else -> {

                }
            }
        }

        @Suppress("MemberVisibilityCanBePrivate")
        suspend fun changeOnlineStatus(status: ClientLoginStatus) {
            socket.sendPacket(ClientChangeOnlineStatusPacket(bot.account.qqNumber, sessionKey, status))
        }

        fun close() {
            this.captchaCache = null

            this.heartbeatJob?.cancel(CancellationException("handler closed"))

            this.heartbeatJob = null
        }
    }
}
