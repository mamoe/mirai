package net.mamoe.mirai.qqandroid.network

import kotlinx.coroutines.*
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.use
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.event.PacketReceivedEvent
import net.mamoe.mirai.qqandroid.network.protocol.packet.KnownPacketFactories
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.PacketId
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.RegPushReason
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.SvcReqRegisterPacket
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.unsafeWeakRef
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
        delay(2000)
        println()
        println()
        println()
        println("Sending ReqRegister")
        SvcReqRegisterPacket(bot.client, RegPushReason.setOnlineStatus).sendAndExpect<SvcReqRegisterPacket.Response>()
    }

    internal fun launchPacketProcessor(rawInput: ByteReadPacket): Job = launch(CoroutineName("Incoming Packet handler")) {
        rawInput.debugPrint("Received").use { input ->
            if (input.remaining == 0L) {
                bot.logger.error("Empty packet received. Consider if bad packet was sent.")
                return@launch
            }
            KnownPacketFactories.parseIncomingPacket(bot, input) { packet: Packet, packetId: PacketId, sequenceId: Int ->
                if (PacketReceivedEvent(packet).broadcast().cancelled) {
                    return@parseIncomingPacket
                }
                packetListeners.forEach { listener ->
                    if (listener.filter(packetId, sequenceId) && packetListeners.remove(listener)) {
                        listener.complete(packet)
                    }
                }
            }
        }
    }

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
            launchPacketProcessor(rawInput)
        }
    }

    suspend fun <E : Packet> OutgoingPacket.sendAndExpect(): E {
        val handler = PacketListener(packetId = packetId, sequenceId = sequenceId)
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
        val packetId: PacketId,
        val sequenceId: Int
    ) : CompletableDeferred<Packet> by CompletableDeferred(supervisor) {
        fun filter(packetId: PacketId, sequenceId: Int) = this.packetId == packetId && this.sequenceId == sequenceId
    }

    override suspend fun awaitDisconnection() = supervisor.join()

    override fun dispose(cause: Throwable?) {
        println("Closed")
        super.dispose(cause)
    }

    override val coroutineContext: CoroutineContext = bot.coroutineContext
}