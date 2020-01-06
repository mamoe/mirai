package net.mamoe.mirai.qqandroid.network

import kotlinx.coroutines.*
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
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.ClosedChannelException
import net.mamoe.mirai.utils.io.PlatformDatagramChannel
import net.mamoe.mirai.utils.io.ReadPacketInternalException
import net.mamoe.mirai.utils.io.debugPrint
import kotlin.coroutines.CoroutineContext

@UseExperimental(MiraiInternalAPI::class)
internal class QQAndroidBotNetworkHandler(override val bot: QQAndroidBot) : BotNetworkHandler() {
    override val supervisor: CompletableJob = SupervisorJob(bot.coroutineContext[Job])

    private val channel: PlatformDatagramChannel = PlatformDatagramChannel("wtlogin.qq.com", 8000)

    override suspend fun login() {
        launch(CoroutineName("Incoming Packet Receiver")) { processReceive() }

        LoginPacket(bot.client).sendAndExpect<LoginPacket.LoginPacketResponse>()
        println("Login sent")
    }

    private suspend inline fun processReceive() {
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

            launch(CoroutineName("Incoming Packet handler")) {
                try {
                    rawInput.debugPrint("Received")
                } catch (e: Exception) {
                    bot.logger.error(e)
                }
            }

            rawInput.use {
                KnownPacketFactories.parseIncomingPacket(bot, rawInput) { packet: Packet, packetId: PacketId, sequenceId: Int ->
                    if (PacketReceivedEvent(packet).broadcast().cancelled) {
                        return
                    }
                    packetListeners.forEach { listener ->
                        if (listener.filter(packetId, sequenceId) && packetListeners.remove(listener)) {
                            listener.complete(packet)
                        }
                    }
                }
            }
        }
    }

    suspend fun <E : Packet> OutgoingPacket.sendAndExpect(): E {
        val handler = PacketListener(packetId = packetId, sequenceId = sequenceId)
        packetListeners.addLast(handler)
        check(channel.send(delegate)) { packetListeners.remove(handler); "Cannot send packet" }
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

    override suspend fun awaitDisconnection() {
        while (true) {
            delay(100)
            // TODO: 2019/12/31
        }
    }

    override fun dispose(cause: Throwable?) {
        println("Closed")
        super.dispose(cause)
    }

    override val coroutineContext: CoroutineContext = bot.coroutineContext
}