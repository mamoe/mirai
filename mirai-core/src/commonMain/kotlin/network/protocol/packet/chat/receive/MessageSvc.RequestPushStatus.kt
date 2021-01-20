package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.event.events.OtherClientOfflineEvent
import net.mamoe.mirai.event.events.OtherClientOnlineEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.appId
import net.mamoe.mirai.internal.createOtherClient
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushStatus
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket

internal object MessageSvcRequestPushStatus : IncomingPacketFactory<Packet?>(
    "MessageSvc.RequestPushStatus", ""
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        val packet = readUniPacket(RequestPushStatus.serializer())
        bot.otherClientsLock.withLock {
            val appId = packet.vecInstanceList?.firstOrNull()?.iAppId ?: 1
            return when (packet.status.toInt()) {
                1 -> { // online
                    if (bot.otherClients.any { appId == it.appId }) return null
                    val info = Mirai.getOnlineOtherClientsList(bot).firstOrNull { appId == it.appId }
                        ?: throw  contextualBugReportException(
                            "SvcRequestPushStatus (OtherClient online)",
                            packet._miraiContentToString(),
                            additional = "Failed to find corresponding instanceInfo."
                        )
                    val client = bot.createOtherClient(info)
                    bot.otherClients.delegate.add(client)
                    OtherClientOnlineEvent(
                        client,
                        ClientKind[packet.nClientType?.toInt() ?: 0]
                    )
                }

                2 -> { // off
                    val client = bot.otherClients.find { it.appId == appId } ?: return null
                    client.cancel(CancellationException("Offline"))
                    bot.otherClients.delegate.remove(client)
                    OtherClientOfflineEvent(client)
                }

                else -> throw contextualBugReportException(
                    "decode SvcRequestPushStatus (PC Client status change)",
                    packet._miraiContentToString(),
                    additional = "unknown status=${packet.status}"
                )
            }
        }
    }
}