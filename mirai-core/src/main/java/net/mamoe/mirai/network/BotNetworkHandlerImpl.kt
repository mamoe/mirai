package net.mamoe.mirai.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.MiraiServer
import net.mamoe.mirai.event.events.bot.BotLoginSucceedEvent
import net.mamoe.mirai.event.events.network.BeforePacketSendEvent
import net.mamoe.mirai.event.events.network.PacketSentEvent
import net.mamoe.mirai.event.events.network.ServerPacketReceivedEvent
import net.mamoe.mirai.event.hookWhile
import net.mamoe.mirai.network.handler.*
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.network.packet.login.*
import net.mamoe.mirai.task.MiraiThreadPool
import net.mamoe.mirai.utils.*
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.reflect.KClass

/**
 * [BotNetworkHandler] 的内部实现, 该类不会有帮助理解的注解, 请查看 [BotNetworkHandler] 以获取帮助
 *
 * @see BotNetworkHandler
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")//to simplify code
internal class BotNetworkHandlerImpl(private val bot: Bot) : BotNetworkHandler {
    override val socket: BotSocket = BotSocket()

    lateinit var login: Login

    override lateinit var message: MessagePacketHandler
    override lateinit var action: ActionPacketHandler

    val packetHandlers: PacketHandlerList = PacketHandlerList()


    //private | internal
    /**
     * 尝试登录. 多次重复登录
     *
     * @param touchingTimeoutMillis 连接每个服务器的 timeout
     */
    @JvmOverloads
    internal fun tryLogin(touchingTimeoutMillis: Long = 200): CompletableFuture<LoginState> {
        val ipQueue: LinkedList<String> = LinkedList(Protocol.SERVER_IP)
        val future = CompletableFuture<LoginState>()

        fun login() {
            this.socket.close()
            val ip = ipQueue.poll()
            if (ip == null) {
                future.complete(LoginState.UNKNOWN)//所有服务器均返回 UNKNOWN
                return
            }

            this.socket.touch(ip, touchingTimeoutMillis).get().let { state ->
                if (state == LoginState.UNKNOWN || state == LoginState.TIMEOUT) {
                    login()//超时或未知, 重试连接下一个服务器
                } else {
                    future.complete(state)
                }
            }
        }
        login()
        return future
    }

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


    internal inner class BotSocket : Closeable, DataPacketSocket {
        override fun distributePacket(packet: ServerPacket) {
            try {
                packet.decode()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                bot.debugPacket(packet)
                return
            }

            //For debug
            kotlin.run {
                if (!packet.javaClass.name.endsWith("Encrypted") && !packet.javaClass.name.endsWith("Raw")) {
                    bot.notice("Packet received: $packet")
                }

                if (packet is ServerEventPacket) {
                    sendPacket(ClientMessageResponsePacket(bot.account.qqNumber, packet.packetId, sessionKey, packet.eventIdentity))
                }
            }

            if (ServerPacketReceivedEvent(bot, packet).broadcast().isCancelled) {
                return
            }

            login.onPacketReceived(packet)
            packetHandlers.forEach {
                it.instance.onPacketReceived(packet)
            }
        }

        private var socket: DatagramSocket? = null

        internal var serverIP: String = ""
            set(value) {
                field = value

                restartSocket()
            }

        internal var loginFuture: CompletableFuture<LoginState>? = null

        @Synchronized
        private fun restartSocket() {
            socket?.close()
            socket = DatagramSocket(0)
            socket!!.connect(InetSocketAddress(serverIP, 8000))
            Thread {
                while (socket!!.isConnected) {
                    val packet = DatagramPacket(ByteArray(2048), 2048)
                    kotlin.runCatching { socket?.receive(packet) }
                            .onSuccess {
                                MiraiThreadPool.getInstance().submit {
                                    try {
                                        distributePacket(ServerPacket.ofByteArray(packet.data.removeZeroTail()))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }.onFailure {
                                if (it.message == "Socket closed" || it.message == "socket closed") {
                                    return@Thread
                                }
                                it.printStackTrace()
                            }

                }
            }.start()
        }

        /**
         * Start network and touch the server
         */
        fun touch(serverAddress: String, timeoutMillis: Long): CompletableFuture<LoginState> {
            bot.info("Connecting server: $serverAddress")
            if (this@BotNetworkHandlerImpl::login.isInitialized) {
                login.close()
            }
            login = Login()
            this.loginFuture = CompletableFuture()

            serverIP = serverAddress
            waitForPacket(ServerPacket::class, timeoutMillis) {
                loginFuture!!.complete(LoginState.TIMEOUT)
            }
            sendPacket(ClientTouchPacket(bot.account.qqNumber, serverIP))

            return this.loginFuture!!
        }

        /**
         * Not async
         */
        @Synchronized

        override fun sendPacket(packet: ClientPacket) {
            checkNotNull(socket) { "network closed" }
            if (socket!!.isClosed) {
                return
            }

            try {
                packet.encodePacket()

                if (BeforePacketSendEvent(bot, packet).broadcast().isCancelled) {
                    return
                }

                val data = packet.toByteArray()
                socket!!.send(DatagramPacket(data, data.size))
                bot.cyanL("Packet sent:     $packet")

                PacketSentEvent(bot, packet).broadcast()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        @Suppress("UNCHECKED_CAST")
        internal fun <P : ServerPacket> waitForPacket(packetClass: KClass<P>, timeoutMillis: Long, timeout: () -> Unit) {
            var got = false
            ServerPacketReceivedEvent::class.hookWhile {
                if (packetClass.isInstance(it.packet) && it.bot === bot) {
                    got = true
                    true
                } else {
                    false
                }
            }


            MiraiThreadPool.getInstance().submit {
                val startingTime = System.currentTimeMillis()
                while (!got) {
                    if (System.currentTimeMillis() - startingTime > timeoutMillis) {
                        timeout.invoke()
                        return@submit
                    }
                    Thread.sleep(10)
                }
            }
        }

        override fun close() {
            this.socket?.close()
            if (this.loginFuture != null) {
                if (!this.loginFuture!!.isDone) {
                    this.loginFuture!!.cancel(true)
                }
                this.loginFuture = null
            }
        }

        override fun isClosed(): Boolean {
            return this.socket?.isClosed ?: true
        }
    }

    /**
     * 处理登录过程
     */
    inner class Login : Closeable {
        private lateinit var token00BA: ByteArray
        private lateinit var token0825: ByteArray
        private var loginTime: Int = 0
        private lateinit var loginIP: String
        private var tgtgtKey: ByteArray = getRandomByteArray(16)

        /**
         * 0828_decr_key
         */
        private lateinit var sessionResponseDecryptionKey: ByteArray

        private var captchaSectionId: Int = 1
        private var captchaCache: ByteArray? = byteArrayOf()//每次包只发一部分验证码来

        private var heartbeatFuture: ScheduledFuture<*>? = null


        fun onPacketReceived(packet: ServerPacket) {
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
                        socket.sendPacket(ClientPasswordSubmissionPacket(bot.account.qqNumber, bot.account.password, packet.loginTime, packet.loginIP, this.tgtgtKey, packet.token0825))
                    }
                }

                is ServerLoginResponseFailedPacket -> {
                    socket.loginFuture?.complete(packet.loginState)
                    return
                }

                is ServerVerificationCodeCorrectPacket -> {
                    this.tgtgtKey = getRandomByteArray(16)
                    this.token00BA = packet.token00BA
                    socket.sendPacket(ClientLoginResendPacket3105(bot.account.qqNumber, bot.account.password, this.loginTime, this.loginIP, this.tgtgtKey, this.token0825, this.token00BA))
                }

                is ServerLoginResponseVerificationCodeInitPacket -> {
                    //[token00BA]来源之一: 验证码
                    this.token00BA = packet.token00BA
                    this.captchaCache = packet.verifyCodePart1

                    if (packet.unknownBoolean != null && packet.unknownBoolean!!) {
                        this.captchaSectionId = 1
                        socket.sendPacket(ClientVerificationCodeTransmissionRequestPacket(1, bot.account.qqNumber, this.token0825, this.captchaSectionId++, this.token00BA))
                    }
                }

                is ServerVerificationCodeTransmissionPacket -> {
                    if (packet is ServerVerificationCodeWrongPacket) {
                        bot.error("验证码错误, 请重新输入")
                        captchaSectionId = 1
                        this.captchaCache = byteArrayOf()
                    }

                    this.captchaCache = this.captchaCache!! + packet.captchaSectionN
                    this.token00BA = packet.token00BA

                    if (packet.transmissionCompleted) {
                        bot.notice(CharImageUtil.createCharImg(ImageIO.read(this.captchaCache!!.inputStream())))
                        bot.notice("需要验证码登录, 验证码为 4 字母")
                        try {
                            (MiraiServer.getInstance().parentFolder + "VerificationCode.png").writeBytes(this.captchaCache!!)
                            bot.notice("若看不清字符图片, 请查看 Mirai 根目录下 VerificationCode.png")
                        } catch (e: Exception) {
                            bot.notice("无法写出验证码文件, 请尝试查看以上字符图片")
                        }
                        bot.notice("若要更换验证码, 请直接回车")
                        val code = Scanner(System.`in`).nextLine()
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
                        this.tgtgtKey = packet.tgtgtKey
                        socket.sendPacket(ClientLoginResendPacket3104(bot.account.qqNumber, bot.account.password, loginTime, loginIP, tgtgtKey, token0825, packet.tokenUnknown
                                ?: this.token00BA, packet.tlv0006))
                    } else {
                        socket.sendPacket(ClientLoginResendPacket3106(bot.account.qqNumber, bot.account.password, loginTime, loginIP, tgtgtKey, token0825, packet.tokenUnknown
                                ?: token00BA, packet.tlv0006))
                    }
                }

                is ServerSessionKeyResponsePacket -> {
                    sessionKey = packet.sessionKey
                    heartbeatFuture = MiraiThreadPool.getInstance().scheduleWithFixedDelay({
                        socket.sendPacket(ClientHeartbeatPacket(bot.account.qqNumber, sessionKey))
                    }, 90000, 90000, TimeUnit.MILLISECONDS)

                    socket.loginFuture!!.complete(LoginState.SUCCESS)

                    login.changeOnlineStatus(ClientLoginStatus.ONLINE)
                }

                is ServerLoginSuccessPacket -> {
                    BotLoginSucceedEvent(bot).broadcast()

                    //登录成功后会收到大量上次的消息, 忽略掉
                    MiraiThreadPool.getInstance().schedule({
                        message.ignoreMessage = false
                    }, 3, TimeUnit.SECONDS)


                    onLoggedIn(sessionKey)
                }


                is ServerVerificationCodePacket.Encrypted -> socket.distributePacket(packet.decrypt())
                is ServerLoginResponseVerificationCodeInitPacket.Encrypted -> socket.distributePacket(packet.decrypt())
                is ServerLoginResponseKeyExchangePacket.Encrypted -> socket.distributePacket(packet.decrypt(this.tgtgtKey))
                is ServerLoginResponseSuccessPacket.Encrypted -> socket.distributePacket(packet.decrypt(this.tgtgtKey))
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

        fun changeOnlineStatus(status: ClientLoginStatus) {
            socket.sendPacket(ClientChangeOnlineStatusPacket(bot.account.qqNumber, sessionKey, status))
        }

        override fun close() {
            this.captchaCache = null

            this.heartbeatFuture?.cancel(true)

            this.heartbeatFuture = null
        }
    }
}
