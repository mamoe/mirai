/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.priv

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.DirectMessageEvent
import net.mamoe.mirai.event.events.DirectMessageSyncEvent
import net.mamoe.mirai.internal.contact.GuildImpl
import net.mamoe.mirai.internal.contact.appId
import net.mamoe.mirai.internal.message.toGuildMessageChainOnline
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.GuildMsg
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.utils.MiraiLogger

internal class DirectMessageProcessor(
    private val logger: MiraiLogger,
) : SimpleNoticeProcessor<GuildMsg.MsgOnlinePush>(type()) {

    internal data class SendDirectMessageReceipt(
        val bot: Bot?,
        val messageRandom: Int,
        val sequenceId: Int,
        val fromAppId: Int,
    ) : Packet, Event, Packet.NoLog, AbstractEvent() {
        override fun toString(): String {
            return "MsgPush.PushGroupProMsg.SendDirectMessageReceipt(messageRandom=$messageRandom, sequenceId=$sequenceId)"
        }

        companion object {
            val EMPTY = SendDirectMessageReceipt(null, 0, 0, 0)
        }
    }

    override suspend fun NoticePipelineContext.processImpl(data: GuildMsg.MsgOnlinePush) {

        for (item in data.msgs) {
            bot.client.directSeq = item.head?.contentHead?.seq ?: (bot.client.directSeq + 1)

            val tinyId = item.head?.routingHead?.fromTinyId
            val fromUin = item.head?.routingHead?.fromUin

            val directMessageMember = item.extInfo?.directMessageMember?.find { it.tinyId != bot.selfTinyId } ?: return
            val isFromSelfAccount = (tinyId == bot.selfTinyId) || (fromUin == bot.id)
            val guild = bot.getGuild(directMessageMember.sourceGuildId) as GuildImpl? ?: return
            val sender = guild.members.find { it.id == tinyId } ?: return


            if (item.head!!.contentHead?.type?.toInt() == 3840) {
                val list = mutableListOf(item)
                if (item.head?.routingHead?.directMessageFlag?.toInt() == 1) {
                    if (!isFromSelfAccount) {
                        collect(
                            DirectMessageEvent(
                                guild = guild,
                                time = item.head!!.contentHead?.time!!.toInt(),
                                sender = sender,
                                message = list.toGuildMessageChainOnline(bot, guild.id, MessageSourceKind.DIRECT),
                            )
                        )
                    } else {
                        collect(
                            DirectMessageSyncEvent(
                                client = bot.otherClients.find { it.appId == item.head!!.routingHead!!.fromAppid?.toInt() }
                                    ?: return, // don't compare with dstAppId. diff.
                                guild = guild,
                                time = item.head!!.contentHead?.time!!.toInt(),
                                sender = sender,
                                senderName = sender.nameCard,
                                message = list.toGuildMessageChainOnline(bot, guild.id, MessageSourceKind.DIRECT),
                            )
                        )
                    }
                }
            }
        }
    }

}