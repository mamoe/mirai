/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.guild


import io.ktor.utils.io.core.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GuildMessageEvent
import net.mamoe.mirai.event.events.GuildMessageSyncEvent
import net.mamoe.mirai.internal.contact.ChannelImpl
import net.mamoe.mirai.internal.contact.GuildImpl
import net.mamoe.mirai.internal.contact.GuildMemberImpl
import net.mamoe.mirai.internal.contact.appId
import net.mamoe.mirai.internal.message.toGuildMessageChainNoSource
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.Guild
import net.mamoe.mirai.internal.network.protocol.data.proto.GuildMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.utils.MiraiLogger

internal class GuildMessageProcessor(
    private val logger: MiraiLogger,
) : SimpleNoticeProcessor<GuildMsg.PressMsg>(type()) {

    internal data class SendGuildMessageReceipt(
        val bot: Bot?,
        val messageRandom: Int,
        val sequenceId: Int,
        val fromAppId: Int,
    ) : Packet, Event, Packet.NoLog, AbstractEvent() {
        override fun toString(): String {
            return "MsgPush.PushGroupProMsg.SendGuildMessageReceipt(messageRandom=$messageRandom, sequenceId=$sequenceId)"
        }

        companion object {
            val EMPTY = SendGuildMessageReceipt(null, 0, 0, 0)
        }
    }

    override suspend fun NoticePipelineContext.processImpl(data: GuildMsg.PressMsg) {

        for (item in data.msgs) {
            val isFromSelfAccount =
                (item.head?.routingHead?.fromTinyId == bot.account.tinyId) || (item.head?.routingHead?.fromUin == bot.id)
            val guild = bot.getGuild(item.head?.routingHead?.guildId!!) as GuildImpl? ?: return
            val channel =
                guild.channelNodes.find { it.id == item.head?.routingHead!!.channelId } as ChannelImpl? ?: return
            val sender =
                guild.members.find { it.id == item.head?.routingHead!!.fromTinyId } as GuildMemberImpl? ?: return

            val list = mutableListOf<Guild.ChannelMsgContent>()
            list.add(item)

            if (item.head?.contentHead?.type?.toInt() == 3841) {

                var common: ImMsgBody.CommonElem? = null
                if (item.body?.richText != null) {
                    for (elem in item.body!!.richText!!.elems) {
                        if (elem.commonElem != null) {
                            common = elem.commonElem
                            break
                        }
                    }
                }
                //TODO: tips / maybe not todo XD
                if (item.head!!.contentHead!!.subType?.toInt() == 2) {

                }

                if (common == null || common.serviceType != 500) {
                    return
                }


                val eventBody = ByteReadPacket(common.pbElem).readProtoBuf(Guild.EventBody.serializer())
                if (eventBody.updateMsg?.eventType != null) {
                    //TODO 撤回
                    if (eventBody.updateMsg.eventType.toInt() == 1 || eventBody.updateMsg.eventType.toInt() == 2) {
                        return
                    }

                    //TODO 消息贴表情更新 (包含添加或删除)
                    if (eventBody.updateMsg.eventType.toInt() == 4) {
                        return
                    }
                }
                //TODO 创建子频道
                if (null != eventBody.createChan) {
                    return
                }

                //TODO 删除子频道
                if (null != eventBody.destroyChan) {
                    return
                }
                //TODO 修改子频道
                if (null != eventBody.changeChanInfo) {
                    return
                }
                //TODO 加入频道
                if (null != eventBody.joinGuild) {

                    return
                }
            }

            if (item.head!!.contentHead?.type?.toInt() == 3840) {
                if (item.head!!.routingHead!!.directMessageFlag?.toInt() == 1) {
                    //TODO: 私聊信息解码
                    return
                }
            }

            if (!isFromSelfAccount) {
                //TODO 记得在 [CommandSender] 添加相关方法
                collect(
                    GuildMessageEvent(
                        guild = guild,
                        channel = channel,
                        time = item.head!!.contentHead?.time!!.toInt(),
                        sender = sender,
                        message = list.toGuildMessageChainNoSource(bot, guild.id, MessageSourceKind.FRIEND),
                    ),
                )
            } else {
                collect(
                    GuildMessageSyncEvent(
                        client = bot.otherClients.find { it.appId == item.head!!.routingHead!!.fromAppid?.toInt() }
                            ?: return, // don't compare with dstAppId. diff.
                        guild = guild,
                        channel = channel,
                        time = item.head!!.contentHead?.time!!.toInt(),
                        sender = sender,
                        senderName = sender.nameCard,
                        message = list.toGuildMessageChainNoSource(bot, guild.id, MessageSourceKind.FRIEND),
                    ),
                )
            }
        }
    }
}