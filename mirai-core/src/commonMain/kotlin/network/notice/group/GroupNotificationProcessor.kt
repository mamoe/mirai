/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.group

import io.ktor.utils.io.core.*
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.checkIsGroupImpl
import net.mamoe.mirai.internal.contact.checkIsMemberImpl
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.notice.NewContactSupport
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x122
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x27
import net.mamoe.mirai.internal.network.protocol.data.proto.TroopTips0x857
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.parseToMessageDataList
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmName

internal class GroupNotificationProcessor(
    private val logger: MiraiLogger,
) : MixedNoticeProcessor(), NewContactSupport {

    override suspend fun NoticePipelineContext.processImpl(data: MsgType0x210) = data.context {
        when (data.uSubMsgType) {
            0x27L -> {
                val body = vProtobuf.loadAs(Submsgtype0x27.SubMsgType0x27.SubMsgType0x27MsgBody.serializer())
                for (msgModInfo in body.msgModInfos) {
                    markAsConsumed(msgModInfo)
                    when {
                        msgModInfo.msgModGroupProfile != null -> handleGroupProfileChanged(msgModInfo.msgModGroupProfile)
                        msgModInfo.msgModGroupMemberProfile != null -> handleGroupMemberProfileChanged(msgModInfo.msgModGroupMemberProfile)
                        else -> markNotConsumed(msgModInfo)
                    }
                }
            }
        }
    }

    /**
     * @see GroupNameChangeEvent
     */
    private fun NoticePipelineContext.handleGroupProfileChanged(
        modGroupProfile: Submsgtype0x27.SubMsgType0x27.ModGroupProfile
    ) {
        for (info in modGroupProfile.msgGroupProfileInfos) {
            when (info.field) {
                1 -> {
                    // 群名
                    val new = info.value.decodeToString()

                    val group = bot.getGroup(modGroupProfile.groupCode) ?: continue
                    group.checkIsGroupImpl()
                    val old = group.name

                    if (new == old) continue

                    if (modGroupProfile.cmdUin == bot.id) continue
                    val operator = group[modGroupProfile.cmdUin] ?: continue

                    group.settings.nameField = new

                    collect(GroupNameChangeEvent(old, new, group, operator))
                }
                2 -> {
                    // 头像
                    // top_package/akkz.java:3446
                    /*
                        var4 = var82.byteAt(0);
                           short var3 = (short) (var82.byteAt(1) | var4 << 8);
                           var85 = var18.method_77927(var7 + "");
                           var85.troopface = var3;
                           var85.hasSetNewTroopHead = true;
                         */
                    //                        bot.logger.debug(
                    //                            contextualBugReportException(
                    //                                "解析 Transformers528 0x27L ModGroupProfile 群头像修改",
                    //                                forDebug = "this=${this._miraiContentToString()}"
                    //                            )
                    //                        )
                }
                3 -> { // troop.credit.data
                    // top_package/akkz.java:3475
                    // top_package/akkz.java:3498
                    //                        bot.logger.debug(
                    //                            contextualBugReportException(
                    //                                "解析 Transformers528 0x27L ModGroupProfile 群 troop.credit.data",
                    //                                forDebug = "this=${this._miraiContentToString()}"
                    //                            )
                    //                        )
                }
                else -> {
                }
            }
        }
    }

    /**
     * @see MemberCardChangeEvent
     */
    private fun NoticePipelineContext.handleGroupMemberProfileChanged(
        modGroupMemberProfile: Submsgtype0x27.SubMsgType0x27.ModGroupMemberProfile
    ) {
        for (info in modGroupMemberProfile.msgGroupMemberProfileInfos) {
            when (info.field) {
                1 -> { // name card
                    val new = info.value
                    val group = bot.getGroup(modGroupMemberProfile.groupCode) ?: continue
                    group.checkIsGroupImpl()
                    val member = group[modGroupMemberProfile.uin] ?: continue
                    member.checkIsMemberImpl()

                    val old = member.nameCard

                    if (new == old) continue
                    member._nameCard = new

                    collect(MemberCardChangeEvent(old, new, member))
                }
                2 -> {
                    if (info.value.singleOrNull()?.code != 0) {
                        logger.debug {
                            "Unknown Transformers528 0x27L ModGroupMemberProfile, field=${info.field}, value=${info.value}"
                        }
                    }
                    continue
                }
                else -> {
                    logger.debug {
                        "Unknown Transformers528 0x27L ModGroupMemberProfile, field=${info.field}, value=${info.value}"
                    }
                    continue
                }
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // MsgType0x2DC
    ///////////////////////////////////////////////////////////////////////////

    override suspend fun NoticePipelineContext.processImpl(data: MsgType0x2DC) {
        when (data.kind) {
            0x0C -> processMute(data)
            0x0E -> processAllowAnonymousChat(data)
            0x10 -> processNormalGrayTip(data)
            0x14 -> processGeneralGrayTip(data)
        }
    }

    /**
     * @see MemberMuteEvent
     * @see MemberUnmuteEvent
     * @see GroupMuteAllEvent
     * @see BotMuteEvent
     * @see BotUnmuteEvent
     */
    private fun NoticePipelineContext.processMute(
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
            val operatorUin = readInt().toUInt().toLong()
            if (operatorUin == bot.id) return
            val operator = group[operatorUin] ?: return
            readInt().toUInt().toLong() // time
            val length = readShort().toUShort().toInt()
            repeat(length) {
                val target = readInt().toUInt().toLong()
                val timeSeconds = readInt().toUInt()
                collected += handleMuteMemberPacket(bot, group, operator, target, timeSeconds.toInt())
            }
        }
    }

    /**
     * @see GroupAllowAnonymousChatEvent
     */
    private fun NoticePipelineContext.processAllowAnonymousChat(
        data: MsgType0x2DC,
    ) = data.context {
        markAsConsumed()
        buf.read {
            val operator = group[readInt().toUInt().toLong()] ?: return
            val new = readInt() == 0
            if (group.settings.isAnonymousChatEnabledField == new) return

            group.settings.isAnonymousChatEnabledField = new
            collect(GroupAllowAnonymousChatEvent(!new, new, group, operator))
        }
    }

    /**
     * @see GroupAllowConfessTalkEvent
     * @see MemberSpecialTitleChangeEvent
     */
    //gray tip: 聊天中的灰色小框系统提示信息（无通用模板，为混合xml代码的文本）
    private fun NoticePipelineContext.processNormalGrayTip(
        data: MsgType0x2DC,
    ) = data.context {
        val proto = data.buf.loadAs(TroopTips0x857.NotifyMsgBody.serializer(), offset = 1)
        markAsConsumed()
        when (proto.optEnumType) {
            1 -> {
                val tipsInfo = proto.optMsgGraytips ?: return

                val message = tipsInfo.optBytesContent.decodeToString()
                when (tipsInfo.robotGroupOpt) {
                    // 非机器人信息
                    0 -> {
                        //坦白说开关
                        if (message.endsWith("群聊坦白说")) {
                            val new = when (message) {
                                "管理员已关闭群聊坦白说" -> false
                                "管理员已开启群聊坦白说" -> true
                                else -> {
                                    logger.debug { "Unknown server confess talk messages $message" }
                                    return
                                }
                            }
                            collect(
                                GroupAllowConfessTalkEvent(
                                    origin = !new,
                                    new = new,
                                    group = group,
                                    isByBot = false
                                )
                            )
                            //群特殊头衔授予
                        } else if (message.endsWith(">头衔")) {
                            message.parseToMessageDataList().let { seq ->
                                if (seq.count() == 2) {
                                    val uin = seq.first().data.toLong()
                                    val newTitle = seq.last().text
                                    val member = group.getMember(uin) ?: return@let
                                    member.checkIsMemberImpl()
                                    collect(
                                        MemberSpecialTitleChangeEvent(
                                            member.specialTitle,
                                            newTitle,
                                            member,
                                            group.owner
                                        )
                                    )
                                    member._specialTitle = newTitle
                                } else {
                                    logger.debug { "Unknown server special title messages $message" }
                                    return
                                }

                            }
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
    // general gray tip: 聊天中的灰色小框系统提示信息（有通用模板）
    private fun NoticePipelineContext.processGeneralGrayTip(
        data: MsgType0x2DC,
    ) = data.context {
        val grayTip = buf.loadAs(TroopTips0x857.NotifyMsgBody.serializer(), 1).optGeneralGrayTip
            ?: return@context markNotConsumed()
        markAsConsumed()
        when (grayTip.templId) {
            // 群幸运词
            10047L, 10048L -> {
                val user = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember
                val images = listOfNotNull(
                    grayTip.msgTemplParam["img_url"],
                    grayTip.msgTemplParam["img_url_1"],
                    grayTip.msgTemplParam["img_url_2"]
                )

                collected += MemberLuckyWordEvent(
                    member = user,
                    images = images
                )
            }
            // 群戳一戳
            10043L, 1133L, 1132L, 1134L, 1135L, 1136L -> {
                // group nudge
                // 预置数据，服务器将不会提供己方已知消息
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
            // 群签到/打卡
            10036L, 10038L -> {
                val user = grayTip.msgTemplParam["mqq_uin"]?.findMember() ?: group.botAsMember
                val sign = grayTip.msgTemplParam["user_sign"].orEmpty()
                val img = grayTip.msgTemplParam["rank_img"]
                val rank = """今日第(\d+)个打卡""".toRegex().matchEntire(sign)?.groupValues?.get(1)?.toInt()

                collected += SignEvent(
                    user = user,
                    sign = sign,
                    hasRank = img != null,
                    rank = rank
                )
            }
            // 群待办
            10134L, 10135L -> {
                val user = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember
                when (grayTip.templId) {
                    10134L -> collected += MemberSetTodoEvent(member = user)
                    10135L -> collected += MemberCompleteTodoEvent(member = user)
                }
            }
            // 龙王
            10093L, 10094L, 1053L, 1054L, 1103L -> {
                val now = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember
                val previous = grayTip.msgTemplParam["uin_last"]?.findMember()

                val lastTalkative = group.lastTalkative
                if (lastTalkative == now) return // duplicate
                if (!group.casLastTalkative(lastTalkative, now)) return

                if (previous == null) {
                    collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.TALKATIVE))
                } else {
                    collect(GroupTalkativeChangeEvent(group, now, previous))
                    collect(MemberHonorChangeEvent.Lose(previous, GroupHonorType.TALKATIVE))
                    collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.TALKATIVE))
                }
            }
            // 群聊之火
            1052L, 1129L, 10095L -> {
                val now = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember

                now.info.honors += GroupHonorType.PERFORMER
                collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.PERFORMER))
            }
            // 群聊炽焰
            1055L, 10096L -> {
                val now = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember

                now.info.honors -= GroupHonorType.PERFORMER
                now.info.honors += GroupHonorType.LEGEND
                collect(MemberHonorChangeEvent.Lose(now, GroupHonorType.PERFORMER))
                collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.LEGEND))
            }
            // 快乐源泉
            1067L -> {
                val now = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember

                now.info.honors += GroupHonorType.EMOTION
                collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.EMOTION))
            }
            // 善财福禄寿
            10111L -> {
                val now = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember
                val previous = grayTip.msgTemplParam["uin_last"]?.findMember()

                if (previous == null) {
                    collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.RED_PACKET))
                } else {
                    // 善财福禄寿 也是唯一的, 也许要加 新事件
                    collect(MemberHonorChangeEvent.Lose(previous, GroupHonorType.RED_PACKET))
                    collect(MemberHonorChangeEvent.Achieve(now, GroupHonorType.RED_PACKET))
                }
            }
            // 等级头衔
            10097L -> {
                // XXX: 目前只有机器人本身会触发
                val user = grayTip.msgTemplParam["uin"]?.findMember() ?: group.botAsMember
                val level = grayTip.msgTemplParam["level"]?.removePrefix("LV")?.toIntOrNull() ?: 1
                val title = grayTip.msgTemplParam["title"] ?: grayTip.msgTemplParam["level"] ?: ""

                collected += MemberTemperatureTitleChangeEvent(
                    member = user,
                    temperature = level,
                    title = title
                )
            }
            else -> {
                markNotConsumed()
                logger.debug {
                    "Unknown Transformers528 0x14 template\ntemplId=${grayTip.templId}\nPermList=${grayTip.msgTemplParam.structureToString()}"
                }
            }
        }
    }
}

internal operator fun List<TroopTips0x857.TemplParam>.get(name: String) = this.findLast { it.name == name }?.value

@JvmName("get2")
internal operator fun List<Submsgtype0x122.Submsgtype0x122.TemplParam>.get(name: String) =
    this.findLast { it.name == name }?.value
