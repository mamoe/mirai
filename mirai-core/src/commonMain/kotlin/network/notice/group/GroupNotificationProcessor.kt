/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.group

import kotlinx.io.core.readUInt
import kotlinx.io.core.readUShort
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.checkIsMemberImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.notice.NewContactSupport
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.proto.TroopTips0x857
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.utils.context
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.read

internal class GroupNotificationProcessor : MixedNoticeProcessor(), NewContactSupport {
    override suspend fun PipelineContext.processImpl(data: MsgType0x2DC) {
        when (data.kind) {
            0x0C -> processMute(data)
            0x0E -> processAllowAnonymousChat(data)
            0x10 -> processAllowConfessTask(data)
            0x14 -> processGrayTip(data)
        }
    }

    /**
     * @see MemberMuteEvent
     * @see MemberUnmuteEvent
     * @see GroupMuteAllEvent
     * @see BotMuteEvent
     * @see BotUnmuteEvent
     */
    private fun PipelineContext.processMute(
        data: MsgType0x2DC,
    ) = data.context {
        fun handleMuteMemberPacket(
            bot: QQAndroidBot,
            group: GroupImpl,
            operator: NormalMember,
            target: Long,
            timeSeconds: Int,
        ): Packet? {
            if (target == 0L) {
                val new = timeSeconds != 0
                if (group.settings.isMuteAllField == new) {
                    return null
                }
                group.settings.isMuteAllField = new
                return GroupMuteAllEvent(!new, new, group, operator)
            }

            if (target == bot.id) {
                return when {
                    group.botMuteRemaining == timeSeconds -> null
                    timeSeconds == 0 || timeSeconds == 0xFFFF_FFFF.toInt() -> {
                        group.botAsMember.checkIsMemberImpl()._muteTimestamp = 0
                        BotUnmuteEvent(operator)
                    }
                    else -> {
                        group.botAsMember.checkIsMemberImpl()._muteTimestamp =
                            currentTimeSeconds().toInt() + timeSeconds
                        BotMuteEvent(timeSeconds, operator)
                    }
                }
            }

            val member = group[target] ?: return null
            member.checkIsMemberImpl()

            if (member.muteTimeRemaining == timeSeconds) return null

            member._muteTimestamp = currentTimeSeconds().toInt() + timeSeconds
            return if (timeSeconds == 0) MemberUnmuteEvent(member, operator)
            else MemberMuteEvent(member, timeSeconds, operator)
        }

        markAsConsumed()

        buf.read {
            val operatorUin = readUInt().toLong()
            if (operatorUin == bot.id) return
            val operator = group[operatorUin] ?: return
            readUInt().toLong() // time
            val length = readUShort().toInt()
            repeat(length) {
                val target = readUInt().toLong()
                val timeSeconds = readUInt()
                collected += handleMuteMemberPacket(bot, group, operator, target, timeSeconds.toInt())
            }
        }
    }

    /**
     * @see GroupAllowAnonymousChatEvent
     */
    private fun PipelineContext.processAllowAnonymousChat(
        data: MsgType0x2DC,
    ) = data.context {
        markAsConsumed()
        buf.read {
            val operator = group[readUInt().toLong()] ?: return
            val new = readInt() == 0
            if (group.settings.isAnonymousChatEnabledField == new) return

            group.settings.isAnonymousChatEnabledField = new
            collect(GroupAllowAnonymousChatEvent(!new, new, group, operator))
        }
    }

    /**
     * @see GroupAllowConfessTalkEvent
     */
    private fun PipelineContext.processAllowConfessTask(
        data: MsgType0x2DC,
    ) = data.context {
        val proto = data.buf.loadAs(TroopTips0x857.NotifyMsgBody.serializer(), offset = 1)
        markAsConsumed()
        when (proto.optEnumType) {
            1 -> {
                val tipsInfo = proto.optMsgGraytips ?: return

                val message = tipsInfo.optBytesContent.decodeToString()
                // 机器人信息
                when (tipsInfo.robotGroupOpt) {
                    // others
                    0 -> {
                        if (message.endsWith("群聊坦白说")) {
                            val new = when (message) {
                                "管理员已关闭群聊坦白说" -> false
                                "管理员已开启群聊坦白说" -> true
                                else -> {
                                    bot.network.logger.debug { "Unknown server confess talk messages $message" }
                                    return
                                }
                            }
                            collect(GroupAllowConfessTalkEvent(new, !new, group, false))
                        }
                    }
                }
            }
            else -> markNotConsumed()
        }
    }

    /**
     * @see NudgeEvent
     * @see MemberHonorChangeEvent
     * @see GroupTalkativeChangeEvent
     */
    private fun PipelineContext.processGrayTip(
        data: MsgType0x2DC,
    ) = data.context {
        val grayTip = buf.loadAs(TroopTips0x857.NotifyMsgBody.serializer(), 1).optGeneralGrayTip
        markAsConsumed()
        when (grayTip?.templId) {
            // 戳一戳
            10043L, 1133L, 1132L, 1134L, 1135L, 1136L -> {
                //预置数据，服务器将不会提供己方已知消息
                val action = grayTip.msgTemplParam["action_str"].orEmpty()
                val from = grayTip.msgTemplParam["uin_str1"]?.findMember() ?: group.botAsMember
                val target = grayTip.msgTemplParam["uin_str2"]?.findMember() ?: group.botAsMember
                val suffix = grayTip.msgTemplParam["suffix_str"].orEmpty()

                collected += NudgeEvent(
                    from = if (from.id == bot.id) bot else from,
                    target = if (target.id == bot.id) bot else target,
                    action = action,
                    suffix = suffix,
                    subject = group,
                )
            }
            // 龙王
            10093L, 1053L, 1054L -> {
                val now: NormalMember = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember
                val previous: NormalMember? = grayTip.msgTemplParam["uin_last"]?.findMember()

                if (previous == null) {
                    collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.TALKATIVE))
                } else {
                    collect(GroupTalkativeChangeEvent(group, now, previous))
                    collect(MemberHonorChangeEvent.Lose(previous, GroupHonorType.TALKATIVE))
                    collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.TALKATIVE))
                }
            }
            else -> {
                markNotConsumed()
                bot.network.logger.debug {
                    "Unknown Transformers528 0x14 template\ntemplId=${grayTip?.templId}\nPermList=${grayTip?.msgTemplParam?._miraiContentToString()}"
                }
            }
        }
    }
}

internal operator fun List<TroopTips0x857.TemplParam>.get(name: String) = this.findLast { it.name == name }?.value
