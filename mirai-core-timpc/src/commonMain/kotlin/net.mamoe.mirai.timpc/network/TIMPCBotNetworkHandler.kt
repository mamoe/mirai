@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network

import kotlinx.coroutines.*
import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.LoginResult
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotLoginSucceedEvent
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.timpc.TIMPCBot
import net.mamoe.mirai.timpc.network.handler.DataPacketSocketAdapter
import net.mamoe.mirai.timpc.network.handler.TemporaryPacketHandler
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.timpc.network.packet.login.*
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.LoginFailedException
import net.mamoe.mirai.utils.NoLog
import net.mamoe.mirai.utils.cryptor.Decrypter
import net.mamoe.mirai.utils.cryptor.NoDecrypter
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

/**
 * 包处理协程调度器.
 *
 * JVM: 独立的 4 thread 调度器
 */
internal expect val NetworkDispatcher: CoroutineDispatcher

/**
 * [BotNetworkHandler] 的 TIM PC 协议实现
 *
 * @see BotNetworkHandler
 */
internal class TIMPCBotNetworkHandler internal constructor(coroutineContext: CoroutineContext, bot: TIMPCBot) :
    BotNetworkHandler(), CoroutineScope {
    override val bot: TIMPCBot by bot.unsafeWeakRef()
    override val supervisor: CompletableJob = SupervisorJob(coroutineContext[Job])

    override val coroutineContext: CoroutineContext =
        coroutineContext + NetworkDispatcher + CoroutineExceptionHandler { context, e ->
            bot.logger.error("An exception was thrown in ${context[CoroutineName]?.let { "coroutine $it" }
                ?: "an unnamed coroutine"} under TIMBotNetworkHandler", e)
        } + supervisor

    lateinit var socket: BotSocketAdapter
        private set

    internal val temporaryPacketHandlers = LockFreeLinkedList<TemporaryPacketHandler<*, *>>()

    private var heartbeatJob: Job? = null

    override suspend fun login() {

        TIMProtocol.SERVER_IP.sortedBy { Random.nextInt() }.forEach { ip ->
            bot.logger.info("Connecting server $ip")
            try {
                withTimeout(3000) {
                    socket = BotSocketAdapter(ip)
                }
            } catch (e: Exception) {
                throw  LoginFailedException(LoginResult.NETWORK_UNAVAILABLE)
            }

            loginResult = CompletableDeferred()

            val result = socket.resendTouch() ?: return // success
            result.takeIf { it != LoginResult.TIMEOUT }?.let { throw  LoginFailedException(it) }

            bot.logger.warning("Timeout. Retrying next server")

            socket.close()
        }
        throw  LoginFailedException(LoginResult.TIMEOUT)
    }

    internal var loginResult: CompletableDeferred<LoginResult?> = CompletableDeferred()

    //private | internal

    private var _sessionKey: SessionKey? = null
    internal val sessionKey: SessionKey get() = _sessionKey ?: error("sessionKey is not yet initialized")

    override suspend fun awaitDisconnection() {
        heartbeatJob?.join()
    }

    override fun dispose(cause: Throwable?) {
        super.dispose(cause)

        this.heartbeatJob?.cancel(CancellationException("handler closed"))
        this.heartbeatJob = null

        if (!this.loginResult.isCompleted && !this.loginResult.isCancelled) {
            this.loginResult.cancel(CancellationException("socket closed"))
        }

        this.socket.close()
    }

    internal inner class BotSocketAdapter(override val serverIp: String) :
        DataPacketSocketAdapter {

        override val channel: PlatformDatagramChannel = PlatformDatagramChannel(serverIp, 8000)

        override val isOpen: Boolean get() = channel.isOpen

        private var loginHandler: LoginHandler? = null

        private suspend fun processReceive() {
            while (channel.isOpen) {
                val buffer = IoBuffer.Pool.borrow()

                try {
                    channel.read(buffer)// JVM: withContext(IO)
                } catch (e: ClosedChannelException) {
                    close()
                    return
                } catch (e: ReadPacketInternalException) {
                    bot.logger.error("Socket channel read failed: ${e.message}")
                    continue
                } catch (e: CancellationException) {
                    return
                } catch (e: Throwable) {
                    bot.logger.error("Caught unexpected exceptions", e)
                    continue
                } finally {
                    if (!buffer.canRead() || buffer.readRemaining == 0) {//size==0
                        //bot.logger.debug("processReceive: Buffer cannot be read")
                        buffer.release(IoBuffer.Pool)
                        continue
                    }// sometimes exceptions are thrown without this `if` clause
                }

                //buffer.resetForRead()
                launch(CoroutineName("handleServerPacket")) {
                    // `.use`: Ensure that the packet is consumed **totally**
                    // so that all the buffers are released
                    ByteArrayPool.useInstance {
                        val length = buffer.readRemaining - 1
                        buffer.readFully(it, 0, length)
                        buffer.resetForWrite()
                        buffer.writeFully(it, 0, length)
                    }
                    ByteReadPacket(buffer, IoBuffer.Pool).use { input ->
                        try {
                            input.discardExact(3)

                            val id = matchPacketId(input.readUShort())
                            val sequenceId = input.readUShort()

                            input.discardExact(7)//4 for qq number, 3 for 0x00 0x00 0x00

                            val packet = try {
                                with(id.factory) {
                                    loginHandler!!.provideDecrypter(id.factory)
                                        .decrypt(input)
                                        .decode(id, sequenceId, this@TIMPCBotNetworkHandler)
                                }
                            } finally {
                                input.close()
                            }


                            handlePacket0(sequenceId, packet, id.factory)
                        } catch (e: Exception) {
                            bot.logger.error(e)
                        }
                    }
                }
            }
        }

        internal suspend fun resendTouch(): LoginResult? /* = coroutineScope */ {
            loginHandler?.close()

            loginHandler = LoginHandler()


            expectingTouchResponse = Job(supervisor)
            try {
                launch { processReceive() }
                launch {
                    if (withTimeoutOrNull(bot.configuration.touchTimeoutMillis) { expectingTouchResponse!!.join() } == null) {
                        loginResult.complete(LoginResult.TIMEOUT)
                    }
                }
                sendPacket(TouchPacket(bot.qqAccount, serverIp, false))

                return loginResult.await()
            } finally {
                expectingTouchResponse = null
            }
        }

        private var expectingTouchResponse: CompletableJob? = null

        private suspend fun <TPacket : Packet> handlePacket0(
            sequenceId: UShort,
            packet: TPacket,
            factory: PacketFactory<TPacket, *>
        ) {
            if (packet is TouchPacket.TouchResponse) {
                expectingTouchResponse?.complete()
            }

            if (!packet::class.annotations.filterIsInstance<NoLog>().any()) {
                if ((packet as? BroadcastControllable)?.shouldBroadcast != false) {
                    bot.logger.verbose("Packet received: ${packet.toString()
                        .replace("\n\r", """\n""")
                        .replace("\n", """\n""")
                        .replace("\r", """\n""")}")
                }
            }

            if (packet is Subscribable) {
                if (packet is BroadcastControllable) {
                    if (packet.shouldBroadcast) packet.broadcast()
                } else {
                    packet.broadcast()
                }

                if (packet is Cancellable && packet.cancelled) return
            }

            temporaryPacketHandlers.forEach {
                if (it.filter(packet, sequenceId) && temporaryPacketHandlers.remove(it)) {
                    it.doReceivePassingExceptionsToDeferred(packet)
                }
            }

            if (factory is SessionPacketFactory<*>) {
                with(factory as SessionPacketFactory<TPacket>) {
                    handlePacket(packet)
                }
            }

            loginHandler?.onPacketReceived(packet)
        }

        internal suspend fun sendPacket(packet: OutgoingPacket): Unit = withContext(coroutineContext + CoroutineName("sendPacket")) {
            check(channel.isOpen) { "channel is not open" }

            packet.delegate.use { built ->
                val buffer = IoBuffer.Pool.borrow()
                try {
                    built.readAvailable(buffer)
                    val shouldBeSent = buffer.readRemaining
                    check(channel.send(buffer) == shouldBeSent) {
                        "Buffer is not entirely sent. " +
                                "Required sent length=$shouldBeSent, but after channel.send, " +
                                "buffer remains ${buffer.readBytes().toUHexString()}"
                    }//JVM: withContext(IO)
                } catch (e: SendPacketInternalException) {
                    if (e.cause !is CancellationException) {
                        bot.logger.error("Caught SendPacketInternalException: ${e.cause?.message}")
                    }
                    delay(bot.configuration.firstReconnectDelayMillis)
                    bot.tryReinitializeNetworkHandler(e)
                    return@withContext
                } finally {
                    buffer.release(IoBuffer.Pool)
                }
            }

            packet.takeUnless { _ ->
                packet.packetId is KnownPacketId && packet.packetId.factory.let {
                    it::class.annotations.filterIsInstance<NoLog>().any()
                }
            }?.let {
                bot.logger.verbose("Packet sent:     ${it.name}")
            }

            Unit
        }

        override val owner: Bot get() = this@TIMPCBotNetworkHandler.bot

        override fun close() {
            loginHandler?.close()
            loginHandler = null
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
        private var privateKey: PrivateKey = PrivateKey(getRandomByteArray(16))

        private var sessionResponseDecryptionKey: SessionResponseDecryptionKey? = null

        private var captchaSectionId: Int = 1
        private var captchaCache: IoBuffer? = null
            //set 为 null 时自动 release; get 为 null 时自动 borrow
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

        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        internal fun <D : Decrypter> provideDecrypter(factory: PacketFactory<*, D>): D =
            when (factory.decrypterType) {
                TouchKey -> TouchKey
                CaptchaKey -> CaptchaKey
                ShareKey -> ShareKey

                NoDecrypter -> NoDecrypter

                SessionResponseDecryptionKey -> sessionResponseDecryptionKey!!
                SubmitPasswordResponseDecrypter -> SubmitPasswordResponseDecrypter(privateKey)
                PrivateKey -> privateKey
                SessionKey -> sessionKey

                else -> error("No decrypter is found")
            } as? D ?: error("Internal error: could not cast decrypter which is found for factory to class Decrypter")

        suspend fun onPacketReceived(packet: Any) {//complex function, but it doesn't matter
            when (packet) {
                is TouchPacket.TouchResponse.OK -> {
                    loginIP = packet.loginIP
                    loginTime = packet.loginTime
                    token0825 = packet.token0825

                    socket.sendPacket(
                        SubmitPasswordPacket(
                            bot = bot.qqAccount,
                            passwordMd5 = bot.account.passwordMd5,
                            loginTime = loginTime,
                            loginIP = loginIP,
                            privateKey = privateKey,
                            token0825 = token0825,
                            token00BA = null,
                            randomDeviceName = bot.configuration.randomDeviceName
                        )
                    )
                }

                is TouchPacket.TouchResponse.Redirection -> {
                    socket.close()
                    bot.logger.info("Redirecting to ${packet.serverIP}")
                    socket = BotSocketAdapter(packet.serverIP)
                    loginResult.complete(socket.resendTouch())
                }

                is SubmitPasswordPacket.LoginResponse.Failed -> {
                    loginResult.complete(packet.result)
                    return
                }

                is CaptchaPacket.CaptchaResponse.Correct -> {
                    this.privateKey = PrivateKey(getRandomByteArray(16))//似乎是必须的
                    this.token00BA = packet.token00BA

                    socket.sendPacket(
                        SubmitPasswordPacket(
                            bot = bot.qqAccount,
                            passwordMd5 = bot.account.passwordMd5,
                            loginTime = loginTime,
                            loginIP = loginIP,
                            privateKey = privateKey,
                            token0825 = token0825,
                            token00BA = packet.token00BA,
                            randomDeviceName = bot.configuration.randomDeviceName
                        )
                    )
                }

                is SubmitPasswordPacket.LoginResponse.CaptchaInit -> {
                    //[token00BA]来源之一: 验证码
                    this.token00BA = packet.token00BA
                    this.captchaCache = packet.captchaPart1

                    this.captchaSectionId = 1
                    socket.sendPacket(CaptchaPacket.RequestTransmission(bot.qqAccount, this.token0825, this.captchaSectionId++, packet.token00BA))
                }

                is CaptchaPacket.CaptchaResponse.Transmission -> {
                    //packet is ServerCaptchaWrongPacket
                    if (this.captchaSectionId == 0) {
                        bot.logger.warning("验证码错误, 请重新输入")
                        this.captchaSectionId = 1
                        this.captchaCache = null
                    }

                    this.captchaCache!!.writeFully(packet.captchaSectionN)
                    this.token00BA = packet.token00BA

                    val configuration = bot.configuration
                    if (packet.transmissionCompleted) {
                        if (configuration.failOnCaptcha) {
                            loginResult.complete(LoginResult.CAPTCHA)
                            close()
                            return
                        }
                        val code = configuration.captchaSolver(bot, captchaCache!!)

                        this.captchaCache = null
                        if (code == null || code.length != 4) {
                            this.captchaSectionId = 1//意味着正在刷新验证码
                            socket.sendPacket(CaptchaPacket.Refresh(bot.qqAccount, token0825))
                        } else {
                            this.captchaSectionId = 0//意味着已经提交验证码
                            socket.sendPacket(CaptchaPacket.Submit(bot.qqAccount, token0825, code, packet.captchaToken))
                        }
                    } else {
                        socket.sendPacket(CaptchaPacket.RequestTransmission(bot.qqAccount, token0825, captchaSectionId++, packet.token00BA))
                    }
                }

                is SubmitPasswordPacket.LoginResponse.Success -> {
                    this.sessionResponseDecryptionKey = packet.sessionResponseDecryptionKey
                    socket.sendPacket(RequestSessionPacket(bot.qqAccount, socket.serverIp, packet.token38, packet.token88, packet.encryptionKey))
                }

                //是ClientPasswordSubmissionPacket之后服务器回复的可能之一
                is SubmitPasswordPacket.LoginResponse.KeyExchange -> {
                    this.privateKey = packet.privateKeyUpdate

                    socket.sendPacket(
                        SubmitPasswordPacket(
                            bot = bot.qqAccount,
                            passwordMd5 = bot.account.passwordMd5,
                            loginTime = loginTime,
                            loginIP = loginIP,
                            privateKey = privateKey,
                            token0825 = token0825,
                            token00BA = packet.tokenUnknown ?: token00BA,
                            randomDeviceName = bot.configuration.randomDeviceName,
                            tlv0006 = packet.tlv0006
                        )
                    )
                }

                is RequestSessionPacket.SessionKeyResponse -> {
                    _sessionKey = packet.sessionKey
                    bot.logger.info("sessionKey = ${packet.sessionKey.value.toUHexString()}")

                    setOnlineStatus(OnlineStatus.ONLINE)//required
                }

                is ChangeOnlineStatusPacket.ChangeOnlineStatusResponse -> {
                    BotLoginSucceedEvent(bot).broadcast()

                    val configuration = bot.configuration
                    heartbeatJob = this@TIMPCBotNetworkHandler.launch {
                        while (socket.isOpen) {
                            delay(configuration.heartbeatPeriodMillis)
                            with(bot) {
                                class HeartbeatTimeoutException : CancellationException("heartbeat timeout")

                                if (withTimeoutOrNull(configuration.heartbeatTimeoutMillis) {
                                        // FIXME: 2019/11/26 启动被挤掉线检测

                                        HeartbeatPacket(bot.qqAccount, sessionKey).sendAndExpect<HeartbeatPacketResponse>()
                                    } == null) {

                                    // retry one time
                                    if (withTimeoutOrNull(configuration.heartbeatTimeoutMillis) {
                                            HeartbeatPacket(bot.qqAccount, sessionKey).sendAndExpect<HeartbeatPacketResponse>()
                                        } == null) {
                                        bot.logger.warning("Heartbeat timed out")

                                        delay(configuration.firstReconnectDelayMillis)
                                        bot.tryReinitializeNetworkHandler(HeartbeatTimeoutException())
                                        return@launch
                                    }
                                }
                            }
                        }
                    }

                    bot.logger.info("Successfully logged in")
                    loginResult.complete(null)
                    this.close()//The LoginHandler is useless since then
                }
            }
        }

        @Suppress("MemberVisibilityCanBePrivate")
        suspend fun setOnlineStatus(status: OnlineStatus) {
            socket.sendPacket(ChangeOnlineStatusPacket(bot.qqAccount, sessionKey, status))
        }

        fun close() {
            this.captchaCache = null
        }
    }
}
