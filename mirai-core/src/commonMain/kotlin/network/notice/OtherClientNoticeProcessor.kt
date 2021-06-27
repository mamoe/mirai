/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.contact.Platform
import net.mamoe.mirai.event.events.OtherClientMessageEvent
import net.mamoe.mirai.event.events.OtherClientOfflineEvent
import net.mamoe.mirai.event.events.OtherClientOnlineEvent
import net.mamoe.mirai.internal.contact.appId
import net.mamoe.mirai.internal.contact.createOtherClient
import net.mamoe.mirai.internal.message.OnlineMessageSourceFromFriendImpl
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.components.ContactUpdater
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushStatus
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.SubMsgType0x7
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.context

/**
 * @see OtherClientOnlineEvent
 * @see OtherClientOfflineEvent
 *
 * @see OtherClientMessageEvent
 */
internal class OtherClientNoticeProcessor : MixedNoticeProcessor() {
    /**
     * @see OtherClientOnlineEvent
     * @see OtherClientOfflineEvent
     */
    override suspend fun PipelineContext.processImpl(data: RequestPushStatus) {
        markAsConsumed()
        bot.components[ContactUpdater].otherClientsLock.withLock {
            val instanceInfo = data.vecInstanceList?.firstOrNull()
            val appId = instanceInfo?.iAppId ?: 1
            when (data.status.toInt()) {
                1 -> { // online
                    if (bot.otherClients.any { appId == it.appId }) return

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
                                    "packet: \n" + data._miraiContentToString() +
                                            "\n\nquery: \n" +
                                            Mirai.getOnlineOtherClientsList(bot)._miraiContentToString(),
                                    additional = "Failed to find corresponding instanceInfo.",
                                ),
                            )
                            OtherClientInfo(appId, Platform.WINDOWS, "", "电脑")
                        }

                    val client = bot.createOtherClient(info)
                    bot.otherClients.delegate.add(client)
                    collected += OtherClientOnlineEvent(
                        client,
                        ClientKind[data.nClientType?.toInt() ?: 0],
                    )
                }

                2 -> { // off
                    val client = bot.otherClients.find { it.appId == appId } ?: return
                    client.cancel(CancellationException("Offline"))
                    bot.otherClients.delegate.remove(client)
                    collected += OtherClientOfflineEvent(client)
                }

                else -> throw contextualBugReportException(
                    "decode SvcRequestPushStatus (PC Client status change)",
                    data._miraiContentToString(),
                    additional = "unknown status=${data.status}",
                )
            }
        }
    }


    /**
     * @see OtherClientMessageEvent
     */
    override suspend fun PipelineContext.processImpl(data: MsgComm.Msg) = data.context {
        if (msgHead.msgType != 529) return

        // top_package/awbk.java:3765
        markAsConsumed() // todo check
        if (msgHead.c2cCmd != 7) {
            // 各种垃圾
            // 08 04 12 1E 08 E9 07 10 B7 F7 8B 80 02 18 E9 07 20 00 28 DD F1 92 B7 07 30 DD F1 92 B7 07 48 02 50 03 32 1E 08 88 80 F8 92 CD 84 80 80 10 10 01 18 00 20 01 2A 0C 0A 0A 08 01 12 06 E5 95 8A E5 95 8A
            return
        }
        val body = msgBody.msgContent.loadAs(SubMsgType0x7.MsgBody.serializer())

        val textMsg =
            body.msgSubcmd0x4Generic?.buf?.loadAs(SubMsgType0x7.MsgBody.QQDataTextMsg.serializer())
                ?: return

        with(body.msgHeader ?: return) {
            if (dstUin != bot.id) return
            val client = bot.otherClients.find { it.appId == srcInstId }
                ?: return // don't compare with dstAppId. diff.

            val chain = buildMessageChain {
                +OnlineMessageSourceFromFriendImpl(bot, listOf(data))
                for (msgItem in textMsg.msgItems) {
                    when (msgItem.type) {
                        1 -> +PlainText(msgItem.text)
                        else -> {
                        }
                    }
                }
            }

            collect(OtherClientMessageEvent(client, chain, msgHead.msgTime))
        }
    }
}