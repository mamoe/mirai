@file:JvmMultifileClass
@file:JvmName("RobotNetworkHandler")

package net.mamoe.mirai.network

import net.mamoe.mirai.MiraiServer
import net.mamoe.mirai.Robot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.events.network.BeforePacketSendEvent
import net.mamoe.mirai.event.events.network.PacketSentEvent
import net.mamoe.mirai.event.events.network.ServerPacketReceivedEvent
import net.mamoe.mirai.event.events.qq.FriendMessageEvent
import net.mamoe.mirai.event.events.robot.RobotLoginSucceedEvent
import net.mamoe.mirai.event.hookWhile
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.network.RobotNetworkHandler.*
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.network.packet.action.*
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
import java.util.function.Supplier
import javax.imageio.ImageIO
import kotlin.reflect.KClass


/**
 * Mirai 的网络处理器, 它处理所有数据包([Packet])的发送和接收.
 * [RobotNetworkHandler] 是全程异步和线程安全的.
 *
 * [RobotNetworkHandler] 由 2 个模块构成:
 * - [SocketHandler]: 处理数据包底层的发送([ByteArray])
 * - [PacketHandler]: 制作 [Packet] 并传递给 [SocketHandler] 继续处理; 分析来自服务器的数据包并处理
 *
 * 其中, [PacketHandler] 由 4 个子模块构成:
 * - [DebugHandler] 输出 [Packet.toString]
 * - [LoginHandler] 处理 touch/login/verification code 相关
 * - [MessageHandler] 处理消息相关(群消息/好友消息)([ServerEventPacket])
 * - [ActionHandler] 处理动作相关(踢人/加入群/好友列表等)
 *
 * A RobotNetworkHandler is used to connect with Tencent servers.
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE")//to simplify code
class RobotNetworkHandler(private val robot: Robot) : Closeable {
    private val socketHandler: SocketHandler = SocketHandler()

    val debugHandler = DebugHandler()
    val loginHandler = LoginHandler()
    val messageHandler = MessageHandler()
    val actionHandler = ActionHandler()

    private val packetHandlers: Map<KClass<out PacketHandler>, PacketHandler> = linkedMapOf(
            DebugHandler::class to debugHandler,
            LoginHandler::class to loginHandler,
            MessageHandler::class to messageHandler,
            ActionHandler::class to actionHandler
    )

    /**
     * Not async
     */
    @ExperimentalUnsignedTypes
    fun sendPacket(packet: ClientPacket) {
        socketHandler.sendPacket(packet)
    }

    override fun close() {
        this.packetHandlers.values.forEach {
            it.close()
        }
        this.socketHandler.close()
    }


    //private | internal

    /**
     * 仅当 [LoginState] 非 [LoginState.UNKNOWN] 且非 [LoginState.TIMEOUT] 才会调用 [loginHook].
     * 如果要输入验证码, 那么会以参数 [LoginState.VERIFICATION_CODE] 调用 [loginHandler], 登录完成后再以 [LoginState.SUCCESS] 调用 [loginHandler]
     *
     * @param touchingTimeoutMillis 连接每个服务器的 timeout
     */
    @JvmOverloads
    internal fun tryLogin(touchingTimeoutMillis: Long = 200): CompletableFuture<LoginState> {
        val ipQueue: LinkedList<String> = LinkedList(Protocol.SERVER_IP)
        val future = CompletableFuture<LoginState>()

        fun login() {
            this.socketHandler.close()
            val ip = ipQueue.poll()
            if (ip == null) {
                future.complete(LoginState.UNKNOWN)//所有服务器均返回 UNKNOWN
                return
            }

            this@RobotNetworkHandler.socketHandler.touch(ip, touchingTimeoutMillis).get().let { state ->
                if (state == LoginState.UNKNOWN || state == LoginState.TIMEOUT) {
                    login()
                } else {
                    future.complete(state)
                }
            }
        }
        login()
        return future
    }

    /**
     * 分配收到的数据包
     */
    @ExperimentalUnsignedTypes
    internal fun distributePacket(packet: ServerPacket) {
        try {
            packet.decode()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            robot.debug("Packet=$packet")
            robot.debug("Packet size=" + packet.input.goto(0).readAllBytes().size)
            robot.debug("Packet data=" + packet.input.goto(0).readAllBytes().toUHexString())
            return
        }

        if (ServerPacketReceivedEvent(robot, packet).broadcast().isCancelled) {
            debugHandler.onPacketReceived(packet)
            return
        }
        this.packetHandlers.values.forEach {
            it.onPacketReceived(packet)
        }
    }


    private inner class SocketHandler : Closeable {
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
        internal fun touch(serverAddress: String, timeoutMillis: Long): CompletableFuture<LoginState> {
            robot.info("Connecting server: $serverAddress")
            this.loginFuture = CompletableFuture()

            socketHandler.serverIP = serverAddress
            waitForPacket(ServerPacket::class, timeoutMillis) {
                loginFuture!!.complete(LoginState.TIMEOUT)
            }
            sendPacket(ClientTouchPacket(robot.account.qqNumber, socketHandler.serverIP))

            return this.loginFuture!!
        }

        /**
         * Not async
         */
        @Synchronized
        @ExperimentalUnsignedTypes
        internal fun sendPacket(packet: ClientPacket) {
            checkNotNull(socket) { "network closed" }
            if (socket!!.isClosed) {
                return
            }

            try {
                packet.encodePacket()

                if (BeforePacketSendEvent(robot, packet).broadcast().isCancelled) {
                    return
                }

                val data = packet.toByteArray()
                socket!!.send(DatagramPacket(data, data.size))
                robot cyanL "Packet sent:     $packet"

                PacketSentEvent(robot, packet).broadcast()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        @Suppress("UNCHECKED_CAST")
        internal fun <P : ServerPacket> waitForPacket(packetClass: KClass<P>, timeoutMillis: Long, timeout: () -> Unit) {
            var got = false
            ServerPacketReceivedEvent::class.hookWhile {
                if (packetClass.isInstance(it.packet) && it.robot == robot) {
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

        fun isClosed(): Boolean {
            return this.socket?.isClosed ?: true
        }
    }


    private lateinit var sessionKey: ByteArray

    abstract inner class PacketHandler : Closeable {
        abstract fun onPacketReceived(packet: ServerPacket)

        override fun close() {

        }
    }

    /**
     * Kind of [PacketHandler] that prints all packets received in the format of hex byte array.
     */
    inner class DebugHandler : PacketHandler() {
        override fun onPacketReceived(packet: ServerPacket) {
            if (!packet.javaClass.name.endsWith("Encrypted") && !packet.javaClass.name.endsWith("Raw")) {
                robot notice "Packet received: $packet"
            }
            if (packet is ServerEventPacket) {
                sendPacket(ClientMessageResponsePacket(robot.account.qqNumber, packet.packetId, sessionKey, packet.eventIdentity))
            }
        }
    }

    /**
     * 处理登录过程
     */
    inner class LoginHandler : PacketHandler() {
        private lateinit var token00BA: ByteArray
        private lateinit var token0825: ByteArray
        private var loginTime: Int = 0
        private lateinit var loginIP: String
        private var tgtgtKey: ByteArray = getRandomByteArray(16)

        private var tlv0105: ByteArray = lazyEncode {
            it.writeHex("01 05 00 30")
            it.writeHex("00 01 01 02 00 14 01 01 00 10")
            it.writeRandom(16)
            it.writeHex("00 14 01 02 00 10")
            it.writeRandom(16)
        }

        /**
         * 0828_decr_key
         */
        private lateinit var sessionResponseDecryptionKey: ByteArray

        private var captchaSectionId: Int = 1
        private var captchaCache: ByteArray? = byteArrayOf()//每次包只发一部分验证码来


        private var heartbeatFuture: ScheduledFuture<*>? = null
        private var sKeyRefresherFuture: ScheduledFuture<*>? = null

        override fun onPacketReceived(packet: ServerPacket) {
            when (packet) {
                is ServerTouchResponsePacket -> {
                    if (packet.serverIP != null) {//redirection
                        socketHandler.serverIP = packet.serverIP!!
                        //connect(packet.serverIP!!)
                        sendPacket(ClientServerRedirectionPacket(packet.serverIP!!, robot.account.qqNumber))
                    } else {//password submission
                        this.loginIP = packet.loginIP
                        this.loginTime = packet.loginTime
                        this.token0825 = packet.token0825
                        sendPacket(ClientPasswordSubmissionPacket(robot.account.qqNumber, robot.account.password, packet.loginTime, packet.loginIP, this.tgtgtKey, packet.token0825))
                    }
                }

                is ServerLoginResponseFailedPacket -> {
                    socketHandler.loginFuture?.complete(packet.loginState)
                    return
                }

                is ServerVerificationCodeCorrectPacket -> {
                    this.tgtgtKey = getRandomByteArray(16)
                    this.token00BA = packet.token00BA
                    sendPacket(ClientLoginResendPacket3105(robot.account.qqNumber, robot.account.password, this.loginTime, this.loginIP, this.tgtgtKey, this.token0825, this.token00BA))
                }

                is ServerLoginResponseVerificationCodeInitPacket -> {
                    //[token00BA]来源之一: 验证码
                    this.token00BA = packet.token00BA
                    this.captchaCache = packet.verifyCodePart1

                    if (packet.unknownBoolean != null && packet.unknownBoolean!!) {
                        this.captchaSectionId = 1
                        sendPacket(ClientVerificationCodeTransmissionRequestPacket(1, robot.account.qqNumber, this.token0825, this.captchaSectionId++, this.token00BA))
                    }
                }

                is ServerVerificationCodeTransmissionPacket -> {
                    if (packet is ServerVerificationCodeWrongPacket) {
                        robot error "验证码错误, 请重新输入"
                        captchaSectionId = 1
                        this.captchaCache = byteArrayOf()
                    }

                    this.captchaCache = this.captchaCache!! + packet.captchaSectionN
                    this.token00BA = packet.token00BA

                    if (packet.transmissionCompleted) {
                        robot notice (CharImageUtil.createCharImg(ImageIO.read(this.captchaCache!!.inputStream())))
                        robot notice ("需要验证码登录, 验证码为 4 字母")
                        try {
                            (MiraiServer.getInstance().parentFolder + "VerificationCode.png").writeBytes(this.captchaCache!!)
                            robot notice ("若看不清字符图片, 请查看 Mirai 根目录下 VerificationCode.png")
                        } catch (e: Exception) {
                            robot notice "无法写出验证码文件, 请尝试查看以上字符图片"
                        }
                        robot notice ("若要更换验证码, 请直接回车")
                        val code = Scanner(System.`in`).nextLine()
                        if (code.isEmpty() || code.length != 4) {
                            this.captchaCache = byteArrayOf()
                            this.captchaSectionId = 1
                            sendPacket(ClientVerificationCodeRefreshPacket(packet.packetIdLast + 1, robot.account.qqNumber, token0825))
                        } else {
                            sendPacket(ClientVerificationCodeSubmitPacket(packet.packetIdLast + 1, robot.account.qqNumber, token0825, code, packet.verificationToken))
                        }
                    } else {
                        sendPacket(ClientVerificationCodeTransmissionRequestPacket(packet.packetIdLast + 1, robot.account.qqNumber, token0825, captchaSectionId++, token00BA))
                    }
                }

                is ServerLoginResponseSuccessPacket -> {
                    this.sessionResponseDecryptionKey = packet.sessionResponseDecryptionKey
                    sendPacket(ClientSessionRequestPacket(robot.account.qqNumber, socketHandler.serverIP, packet.token38, packet.token88, packet.encryptionKey, this.tlv0105))
                }

                //是ClientPasswordSubmissionPacket之后服务器回复的
                is ServerLoginResponseKeyExchangePacket -> {
                    //if (packet.tokenUnknown != null) {
                    //this.token00BA = packet.token00BA!!
                    //println("token00BA changed!!! to " + token00BA.toUByteArray())
                    //}
                    if (packet.flag == ServerLoginResponseKeyExchangePacket.Flag.`08 36 31 03`) {
                        this.tgtgtKey = packet.tgtgtKey
                        sendPacket(ClientLoginResendPacket3104(robot.account.qqNumber, robot.account.password, loginTime, loginIP, tgtgtKey, token0825, packet.tokenUnknown
                                ?: this.token00BA, packet.tlv0006))
                    } else {
                        sendPacket(ClientLoginResendPacket3106(robot.account.qqNumber, robot.account.password, loginTime, loginIP, tgtgtKey, token0825, packet.tokenUnknown
                                ?: token00BA, packet.tlv0006))
                    }
                }

                is ServerSessionKeyResponsePacket -> {
                    sessionKey = packet.sessionKey
                    heartbeatFuture = MiraiThreadPool.getInstance().scheduleWithFixedDelay({
                        sendPacket(ClientHeartbeatPacket(robot.account.qqNumber, sessionKey))
                    }, 90000, 90000, TimeUnit.MILLISECONDS)

                    RobotLoginSucceedEvent(robot).broadcast()

                    //登录成功后会收到大量上次的消息, 忽略掉
                    MiraiThreadPool.getInstance().schedule({
                        messageHandler.ignoreMessage = false
                    }, 2, TimeUnit.SECONDS)

                    this.tlv0105 = packet.tlv0105
                    sendPacket(ClientChangeOnlineStatusPacket(robot.account.qqNumber, sessionKey, ClientLoginStatus.ONLINE))
                }

                is ServerLoginSuccessPacket -> {
                    socketHandler.loginFuture!!.complete(LoginState.SUCCESS)
                    sendPacket(ClientSKeyRequestPacket(robot.account.qqNumber, sessionKey))
                }

                is ServerSKeyResponsePacket -> {
                    actionHandler.sKey = packet.sKey
                    actionHandler.cookies = "uin=o" + robot.account.qqNumber + ";skey=" + actionHandler.sKey + ";"

                    sKeyRefresherFuture = MiraiThreadPool.getInstance().scheduleWithFixedDelay({
                        sendPacket(ClientSKeyRefreshmentRequestPacket(robot.account.qqNumber, sessionKey))
                    }, 1800000, 1800000, TimeUnit.MILLISECONDS)

                    actionHandler.gtk = getGTK(actionHandler.sKey)
                    sendPacket(ClientAccountInfoRequestPacket(robot.account.qqNumber, sessionKey))
                }

                is ServerEventPacket.Raw -> distributePacket(packet.distribute())

                is ServerVerificationCodePacket.Encrypted -> distributePacket(packet.decrypt())
                is ServerLoginResponseVerificationCodeInitPacket.Encrypted -> distributePacket(packet.decrypt())
                is ServerLoginResponseKeyExchangePacket.Encrypted -> distributePacket(packet.decrypt(this.tgtgtKey))
                is ServerLoginResponseSuccessPacket.Encrypted -> distributePacket(packet.decrypt(this.tgtgtKey))
                is ServerSessionKeyResponsePacket.Encrypted -> distributePacket(packet.decrypt(this.sessionResponseDecryptionKey))
                is ServerTouchResponsePacket.Encrypted -> distributePacket(packet.decrypt())
                is ServerSKeyResponsePacket.Encrypted -> distributePacket(packet.decrypt(sessionKey))
                is ServerAccountInfoResponsePacket.Encrypted -> distributePacket(packet.decrypt(sessionKey))
                is ServerEventPacket.Raw.Encrypted -> distributePacket(packet.decrypt(sessionKey))


                is ServerAccountInfoResponsePacket,
                is ServerHeartbeatResponsePacket,
                is UnknownServerPacket -> {
                    //ignored
                }
                else -> {

                }
            }
        }

        override fun close() {
            this.captchaCache = null

            this.heartbeatFuture?.cancel(true)
            this.sKeyRefresherFuture?.cancel(true)

            this.heartbeatFuture = null
            this.sKeyRefresherFuture = null
        }
    }

    /**
     * 处理消息事件, 承担消息发送任务.
     */
    inner class MessageHandler : PacketHandler() {
        internal var ignoreMessage: Boolean = false

        init {
            //todo for test
            FriendMessageEvent::class.hookWhile {
                if (socketHandler.isClosed()) {
                    return@hookWhile false
                }
                if (it.message() valueEquals "你好") {
                    it.qq.sendMessage("你好!")
                } else if (it.message().toString().startsWith("复读")) {
                    it.qq.sendMessage(it.message())
                }

                return@hookWhile true
            }
        }

        override fun onPacketReceived(packet: ServerPacket) {
            when (packet) {
                is ServerGroupUploadFileEventPacket -> {
                    //todo
                }

                is ServerFriendMessageEventPacket -> {
                    if (ignoreMessage) {
                        return
                    }

                    FriendMessageEvent(robot, robot.contacts.getQQ(packet.qq), packet.message).broadcast()
                }

                is ServerGroupMessageEventPacket -> {
                    //todo message chain
                    //GroupMessageEvent(this.robot, robot.contacts.getGroupByNumber(packet.groupNumber), robot.contacts.getQQ(packet.qq), packet.message)
                }

                is UnknownServerEventPacket -> {
                    //todo
                }

                is ServerSendFriendMessageResponsePacket,
                is ServerSendGroupMessageResponsePacket -> {
                    //ignored
                }
                else -> {
                    //ignored
                }
            }
        }

        fun sendFriendMessage(qq: QQ, message: MessageChain) {
            sendPacket(ClientSendFriendMessagePacket(robot.account.qqNumber, qq.number, sessionKey, message))
        }

        fun sendGroupMessage(group: Group, message: Message): Unit {
            TODO()
            //sendPacket(ClientSendGroupMessagePacket(group.groupId, robot.account.qqNumber, sessionKey, message))
        }
    }

    /**
     * 动作: 获取好友列表, 点赞, 踢人等.
     * 处理动作事件, 承担动作任务.
     */
    inner class ActionHandler : PacketHandler() {
        internal lateinit var cookies: String
        internal var sKey: String = ""
            set(value) {
                field = value
                gtk = getGTK(value)
            }
        internal var gtk: Int = 0

        private val addFriendSessions = Collections.synchronizedCollection(mutableListOf<AddFriendSession>())

        override fun onPacketReceived(packet: ServerPacket) {
            when (packet) {
                is ServerCanAddFriendResponsePacket -> {
                    this.addFriendSessions.forEach {
                        it.onPacketReceived(packet)
                    }
                }
                else -> {
                }
            }
        }

        fun addFriend(qqNumber: Long, message: Supplier<String>) {
            addFriend(qqNumber, lazy { message.get() })
        }

        @JvmSynthetic
        fun addFriend(qqNumber: Long, message: Lazy<String> = lazyOf("")): CompletableFuture<AddFriendResult> {
            val future = CompletableFuture<AddFriendResult>()
            val session = AddFriendSession(qqNumber, future, message)
            addFriendSessions.add(session)
            session.sendAddRequest();
            return future
        }

        override fun close() {

        }

        private inner class AddFriendSession(
                private val qq: Long,
                private val future: CompletableFuture<AddFriendResult>,
                private val message: Lazy<String>
        ) : Closeable {
            lateinit var id: ByteArray

            fun onPacketReceived(packet: ServerPacket) {
                if (!::id.isInitialized) {
                    return
                }

                when (packet) {
                    is ServerCanAddFriendResponsePacket -> {
                        if (!(packet.idByteArray[2] == id[0] && packet.idByteArray[3] == id[1])) {
                            return
                        }

                        when (packet.state) {
                            ServerCanAddFriendResponsePacket.State.FAILED -> {
                                future.complete(AddFriendResult.FAILED)
                                close()
                            }

                            ServerCanAddFriendResponsePacket.State.ALREADY_ADDED -> {
                                future.complete(AddFriendResult.ALREADY_ADDED)
                                close()
                            }

                            ServerCanAddFriendResponsePacket.State.REQUIRE_VERIFICATION -> {
                                sendPacket(ClientAddFriendPacket(robot.account.qqNumber, qq, sessionKey))
                            }

                            ServerCanAddFriendResponsePacket.State.NOT_REQUIRE_VERIFICATION -> {

                            }
                        }
                    }


                }
            }

            fun sendAddRequest() {
                sendPacket(ClientCanAddFriendPacket(robot.account.qqNumber, qq, sessionKey).also { this.id = it.packetIdLast })
            }

            override fun close() {
                addFriendSessions.remove(this)
            }
        }
    }
}