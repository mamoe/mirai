/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.contact.Platform
import net.mamoe.mirai.event.events.OtherClientOfflineEvent
import net.mamoe.mirai.event.events.OtherClientOnlineEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.appId
import net.mamoe.mirai.internal.contact.createOtherClient
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.ContactUpdater
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushStatus
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket

internal object MessageSvcRequestPushStatus : IncomingPacketFactory<Packet?>(
    "MessageSvc.RequestPushStatus", ""
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        val packet = readUniPacket(RequestPushStatus.serializer())
        bot.components[ContactUpdater].otherClientsLock.withLock {
            val instanceInfo = packet.vecInstanceList?.firstOrNull()
            val appId = instanceInfo?.iAppId ?: 1
            return when (packet.status.toInt()) {
                1 -> { // online
                    if (bot.otherClients.any { appId == it.appId }) return null

                    suspend fun tryFindInQuery(): OtherClientInfo? {
                        return Mirai.getOnlineOtherClientsList(bot).find { it.appId == appId }
                            ?: kotlin.run {
                                delay(2000) // sometimes server sync slow
                                Mirai.getOnlineOtherClientsList(bot).find { it.appId == appId }
                            }
                    }

                    val info =
                        tryFindInQuery() ?: kotlin.run {
                            bot.network.logger.warning(
                                contextualBugReportException(
                                    "SvcRequestPushStatus (OtherClient online)",
                                    "packet: \n" + packet._miraiContentToString() +
                                            "\n\nquery: \n" +
                                            Mirai.getOnlineOtherClientsList(bot)._miraiContentToString(),
                                    additional = "Failed to find corresponding instanceInfo."
                                )
                            )
                            OtherClientInfo(appId, Platform.WINDOWS, "", "电脑")
                        }

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