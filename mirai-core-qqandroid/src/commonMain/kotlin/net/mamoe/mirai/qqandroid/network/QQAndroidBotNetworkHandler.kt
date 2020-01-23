package net.mamoe.mirai.qqandroid.network

import kotlinx.coroutines.*
import kotlinx.io.core.*
import kotlinx.io.pool.ObjectPool
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.event.PacketReceivedEvent
import net.mamoe.mirai.qqandroid.network.protocol.packet.KnownPacketFactories
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.SvcReqRegisterPacket
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.*
import kotlin.coroutines.CoroutineContext

@UseExperimental(MiraiInternalAPI::class)
internal class QQAndroidBotNetworkHandler(bot: QQAndroidBot) : BotNetworkHandler() {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()
    override val supervisor: CompletableJob = SupervisorJob(bot.coroutineContext[Job])

    private lateinit var channel: PlatformSocket

    override suspend fun login() {
        channel = PlatformSocket()
        channel.connect("113.96.13.208", 8080)
        launch(CoroutineName("Incoming Packet Receiver")) { processReceive() }

        println("Sending login")
        LoginPacket.SubCommand9(bot.client).sendAndExpect<LoginPacket.LoginPacketResponse>()
        println("SessionTicket=${bot.client.wLoginSigInfo.wtSessionTicket.data.toUHexString()}")
        println("d2key=${bot.client.wLoginSigInfo.d2Key.toUHexString()}")
        println("SessionTicketKey=${bot.client.wLoginSigInfo.wtSessionTicketKey.toUHexString()}")
        println()
        println()
        println()
        println("Sending ReqRegister")
        SvcReqRegisterPacket(bot.client).sendAndExpect<SvcReqRegisterPacket.Response>()
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
     * 缓存的包
     */
    private var cachedPacket: ByteReadPacket? = null
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
        try {
            KnownPacketFactories.parseIncomingPacket(bot, input) { packet: Packet, commandName: String, sequenceId: Int ->
                if (PacketReceivedEvent(packet).broadcast().cancelled) {
                    return@parseIncomingPacket
                }
                packetListeners.forEach { listener ->
                    if (listener.filter(commandName, sequenceId) && packetListeners.remove(listener)) {
                        listener.complete(packet)
                    }
                }
            }
        } finally {
            println()
            println() // separate for debugging
        }
    }

    /**
     * 处理从服务器接收过来的包. 这些包可能是粘在一起的, 也可能是不完整的. 将会自动处理
     */
    @UseExperimental(ExperimentalCoroutinesApi::class)
    internal suspend fun processPacket(rawInput: ByteReadPacket): Unit = rawInput.debugPrint("Received").let { input: ByteReadPacket ->
        if (input.remaining == 0L) {
            return
        }

        if (cachedPacket == null) {
            // 没有缓存
            var length: Int = input.readInt() - 4
            if (input.remaining == length.toLong()) {
                // 捷径: 当包长度正好, 直接传递剩余数据.
                parsePacketAsync(input)
                return
            }
            // 循环所有完整的包
            while (input.remaining > length) {
                parsePacketAsync(input.readIoBuffer(length))

                length = input.readInt() - 4
            }

            if (input.remaining != 0L) {
                // 剩余的包长度不够, 缓存后接收下一个包
                expectingRemainingLength = length - input.remaining
                cachedPacket = input
            } else {
                cachedPacket = null // 表示包长度正好
            }
        } else {
            // 有缓存

            if (input.remaining >= expectingRemainingLength) {
                // 剩余长度够, 连接上去, 处理这个包.
                parsePacketAsync(buildPacket {
                    writePacket(cachedPacket!!)
                    writePacket(input, expectingRemainingLength)
                })
                cachedPacket = null // 缺少的长度已经给上了.

                if (input.remaining != 0L) {
                    processPacket(input) // 继续处理剩下内容
                }
            } else {
                // 剩余不够, 连接上去
                expectingRemainingLength -= input.remaining
                cachedPacket = buildPacket {
                    writePacket(cachedPacket!!)
                    writePacket(input)
                }
            }
        }
        if (input.remaining == 0L) {
            bot.logger.error("Empty packet received. Consider if bad packet was sent.")
            return
        }
    }


    @UseExperimental(ExperimentalCoroutinesApi::class)
    private suspend fun processReceive() {
        while (channel.isOpen) {
            val rawInput = try {
                channel.read()
            } catch (e: ClosedChannelException) {
                dispose()
                return
            } catch (e: ReadPacketInternalException) {
                bot.logger.error("Socket channel read failed: ${e.message}")
                continue
            } catch (e: CancellationException) {
                return
            } catch (e: Throwable) {
                bot.logger.error("Caught unexpected exceptions", e)
                continue
            }
            launch(context = PacketReceiveDispatcher + CoroutineName("Incoming Packet handler"), start = CoroutineStart.ATOMIC) {
                processPacket(rawInput)
            }
        }
    }

    suspend fun <E : Packet> OutgoingPacket.sendAndExpect(): E {
        val handler = PacketListener(commandName = commandName, sequenceId = sequenceId)
        packetListeners.addLast(handler)
        //println(delegate.readBytes().toUHexString())
        println("Sending length=" + delegate.remaining)
        channel.send(delegate)//) { packetListeners.remove(handler); "Cannot send packet" }
        println("Packet sent")
        @Suppress("UNCHECKED_CAST")
        return handler.await() as E
    }

    @PublishedApi
    internal val packetListeners = LockFreeLinkedList<PacketListener>()

    @PublishedApi
    internal inner class PacketListener(
        val commandName: String,
        val sequenceId: Int
    ) : CompletableDeferred<Packet> by CompletableDeferred(supervisor) {
        fun filter(commandName: String, sequenceId: Int) = this.commandName == commandName && this.sequenceId == sequenceId
    }

    override suspend fun awaitDisconnection() = supervisor.join()

    override fun dispose(cause: Throwable?) {
        println("Closed")
        super.dispose(cause)
    }

    override val coroutineContext: CoroutineContext = bot.coroutineContext
}