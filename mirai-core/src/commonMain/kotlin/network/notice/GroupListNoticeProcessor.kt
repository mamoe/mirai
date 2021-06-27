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
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.MemberPermission.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.addNewNormalMember
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.getGroupByUin
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.components.ContactUpdater
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.notice.decoders.MsgType0x2DC
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.parseToMessageDataList
import net.mamoe.mirai.internal.utils.toMemberInfo
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.context
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.read


/**
 * Member/Bot invited/active join // force/active leave
 * Member/Bot permission change
 *
 * @see BotJoinGroupEvent
 * @see MemberJoinEvent
 *
 * @see BotLeaveEvent
 * @see MemberLeaveEvent
 *
 * @see MemberPermissionChangeEvent
 * @see BotGroupPermissionChangeEvent
 *
 * @see BotInvitedJoinGroupRequestEvent
 * @see MemberJoinRequestEvent
 */
internal class GroupListNoticeProcessor(
    private val logger: MiraiLogger,
) : MixedNoticeProcessor(), NewContactSupport {

    override suspend fun PipelineContext.processImpl(data: MsgType0x210) {
        if (data.uSubMsgType != 0x44L) return
        val msg = data.vProtobuf.loadAs(Submsgtype0x44.Submsgtype0x44.MsgBody.serializer())
        if (msg.msgGroupMsgSync == null) return

        when (msg.msgGroupMsgSync.msgType) {
            1, 2 -> {
                bot.components[ContactUpdater].groupListModifyLock.withLock {
                    bot.addNewGroupByCode(msg.msgGroupMsgSync.grpCode)?.let {
                        collect(BotJoinGroupEvent.Active(it))
                    }
                }
            }
        }
    }

    /**
     * @see MemberJoinEvent.Invite
     * @see MemberLeaveEvent.Quit
     */
    override suspend fun PipelineContext.processImpl(data: MsgType0x2DC) = data.context {
        if (data.kind != 0x10) return
        val proto = data.buf.loadAs(TroopTips0x857.NotifyMsgBody.serializer(), offset = 1)
        if (proto.optEnumType != 1) return
        val tipsInfo = proto.optMsgGraytips ?: return

        val message = tipsInfo.optBytesContent.decodeToString()
        // 机器人信息
        when (tipsInfo.robotGroupOpt) {
            // 添加
            1 -> {
                val dataList = message.parseToMessageDataList()
                val invitor = dataList.first().let { messageData ->
                    group[messageData.data.toLong()] ?: return
                }
                val member = dataList.last().let { messageData ->
                    group.addNewNormalMember(messageData.toMemberInfo()) ?: return
                }
                collect(MemberJoinEvent.Invite(member, invitor))
            }
            // 移除
            2 -> {
                message.parseToMessageDataList().first().let {
                    val member = group.getOrFail(it.data.toLong())
                    group.members.delegate.remove(member)
                    collect(MemberLeaveEvent.Quit(member))
                }
            }

            else -> {
                logger.debug { "Unknown robotGroupOpt ${tipsInfo.robotGroupOpt}, message=$message" }
            }
        }

        return markAsConsumed()
    }

    /**
     * @see MemberJoinEvent.Invite
     * @see BotJoinGroupEvent.Invite
     * @see MemberJoinEvent.Active
     * @see BotJoinGroupEvent.Active
     */
    override suspend fun PipelineContext.processImpl(data: MsgComm.Msg) = data.context {
        bot.components[ContactUpdater].groupListModifyLock.withLock {
            when (data.msgHead.msgType) {
                33 -> processGroupJoin33(data)
                34 -> Unit // 34 与 33 重复, 忽略 34
                38 -> processGroupJoin38(data)
                85 -> processGroupJoin85(data)
                else -> return
            }
            markAsConsumed()
        }
    }

    // 33
    private suspend fun PipelineContext.processGroupJoin33(data: MsgComm.Msg) = data.context {
        msgBody.msgContent.read {
            val groupUin = Mirai.calculateGroupUinByGroupCode(readUInt().toLong())
            val group = bot.getGroupByUin(groupUin) ?: bot.addNewGroupByUin(groupUin) ?: return
            discardExact(1)
            val joinedMemberUin = readUInt().toLong()
            val joinType = readByte().toInt()
            val invitorUin = readUInt().toLong()
            when (joinType) {
                // 邀请加入
                -125, 3 -> {
                    val invitor = group[invitorUin] ?: return
                    collected += if (joinedMemberUin == bot.id) {
                        BotJoinGroupEvent.Invite(invitor)
                    } else {
                        MemberJoinEvent.Invite(group.addNewNormalMember(getNewMemberInfo()) ?: return, invitor)
                    }
                }
                // 通过群员分享的二维码/直接加入
                -126, 2 -> {
                    collected += if (joinedMemberUin == bot.id) {
                        BotJoinGroupEvent.Active(group)
                    } else {
                        MemberJoinEvent.Active(group.addNewNormalMember(getNewMemberInfo()) ?: return)
                    }
                }
                // 忽略
                else -> {
                }
            }
        }
        // 邀请入群
        // package: 27 0B 60 E7 01 CA CC 69 8B 83 44 71 47 90 06 B9 DC C0 ED D4 B1 00 30 33 44 30 42 38 46 30 39 37 32 38 35 43 34 31 38 30 33 36 41 34 36 31 36 31 35 32 37 38 46 46 43 30 41 38 30 36 30 36 45 38 31 43 39 41 34 38 37
        // package: groupUin + 01 CA CC 69 8B 83 + invitorUin + length(06) + string + magicKey


        // 主动入群, 直接加入: msgContent=27 0B 60 E7 01 76 E4 B8 DD 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 42 39 41 30 33 45 38 34 30 39 34 42 46 30 45 32 45 38 42 31 43 43 41 34 32 42 38 42 44 42 35 34 44 42 31 44 32 32 30 46 30 38 39 46 46 35 41 38
        // 主动直接加入                  27 0B 60 E7 01 76 E4 B8 DD 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 33 30 45 38 42 31 33 46 41 41 31 33 46 38 31 35 34 41 38 33 32 37 31 43 34 34 38 35 33 35 46 45 31 38 32 43 39 42 43 46 46 32 44 39 39 46 41 37

        // 有人被邀请(经过同意后)加入      27 0B 60 E7 01 76 E4 B8 DD 83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 34 30 34 38 32 33 38 35 37 41 37 38 46 33 45 37 35 38 42 39 38 46 43 45 44 43 32 41 30 31 36 36 30 34 31 36 39 35 39 30 38 39 30 39 45 31 34 34
        // 搜索到群, 直接加入             27 0B 60 E7 01 07 6E 47 BA 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 32 30 39 39 42 39 41 46 32 39 41 35 42 33 46 34 32 30 44 36 44 36 39 35 44 38 45 34 35 30 46 30 45 30 38 45 31 41 39 42 46 46 45 32 30 32 34 35
    }

    // 38
    private suspend fun PipelineContext.processGroupJoin38(data: MsgComm.Msg) = data.context {
        if (bot.getGroupByUin(msgHead.fromUin) != null) return
        bot.addNewGroupByUin(msgHead.fromUin)?.let { collect(BotJoinGroupEvent.Active(it)) }
    }

    // 85
    private suspend fun PipelineContext.processGroupJoin85(data: MsgComm.Msg) = data.context {
        // msgHead.authUin: 处理人
        if (msgHead.toUin != bot.id) return
        processGroupJoin38(data)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Structmsg.StructMsg
    ///////////////////////////////////////////////////////////////////////////

    override suspend fun PipelineContext.processImpl(data: Structmsg.StructMsg) = data.msg.context {
        markAsConsumed()
        if (this == null) return
        when (subType) {
            // 处理被邀请入群 或 处理成员入群申请
            1 -> when (groupMsgType) {
                1 -> {
                    // 成员申请入群
                    MemberJoinRequestEvent(
                        bot, data.msgSeq, msgAdditional,
                        data.reqUin, groupCode, groupName, reqUinNick
                    )
                }
                2 -> {
                    // Bot 被邀请入群
                    BotInvitedJoinGroupRequestEvent(
                        bot, data.msgSeq, actionUin,
                        groupCode, groupName, actionUinNick
                    )
                }
                22 -> {
                    // 成员邀请入群
                    MemberJoinRequestEvent(
                        bot, data.msgSeq, msgAdditional,
                        data.reqUin, groupCode, groupName, reqUinNick, actionUin
                    )
                }
                else -> throw contextualBugReportException(
                    "parse SystemMsgNewGroup, subType=1",
                    this._miraiContentToString(),
                    additional = "并尽量描述此时机器人是否正被邀请加入群, 或者是有有新群员加入此群"
                )
            }
            2 -> { // 被邀请入群, 自动同意, 不需处理

                //                            val group = bot.getNewGroup(groupCode) ?: return null
                //                            val invitor = group[actionUin]
                //
                //                            BotJoinGroupEvent.Invite(invitor)
            }
            3 -> { // 已被请他管理员处理
            }
            5 -> {
                val group = bot.getGroup(groupCode) ?: return
                when (groupMsgType) {
                    3 -> {
                        // https://github.com/mamoe/mirai/issues/651
                        // msgDescribe=将你设置为管理员
                        // msgTitle=管理员设置
                    }
                    13 -> {
                        // 成员主动退出, 机器人是管理员, 接到通知
                        // 但无法获取是哪个成员.
                    }
                    7 -> { // 机器人被踢
                        val operator = group[actionUin] ?: return
                        BotLeaveEvent.Kick(operator)
                    }
                    else -> {
                        throw contextualBugReportException(
                            "解析 NewContact.SystemMsgNewGroup, subType=5, groupMsgType=$groupMsgType",
                            this._miraiContentToString(),
                            null,
                            "并描述此时机器人是否被踢出群等"
                        )
                    }
                }
            }
            else -> throw contextualBugReportException(
                "解析 NewContact.SystemMsgNewGroup, subType=$subType, groupMsgType=$groupMsgType",
                forDebug = this._miraiContentToString(),
                additional = "并尽量描述此时机器人是否正被邀请加入群, 或者是有有新群员加入此群"
            )
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // OnlinePushTrans.PbMsgInfo
    ///////////////////////////////////////////////////////////////////////////

    override suspend fun PipelineContext.processImpl(data: OnlinePushTrans.PbMsgInfo) {
        markAsConsumed()
        when (data.msgType) {
            44 -> data.msgData.read {
                //                  3D C4 33 DD 01 FF CD 76 F4 03 C3 7E 2E 34
                //      群转让
                //      start with  3D C4 33 DD 01 FF
                //                  3D C4 33 DD 01 FF C3 7E 2E 34 CD 76 F4 03
                // 权限变更
                //                  3D C4 33 DD 01 00/01 .....
                //                  3D C4 33 DD 01 01 C3 7E 2E 34 01
                discardExact(5)
                val kind = readUByte().toInt()
                if (kind == 0xFF) {
                    val from = readUInt().toLong()
                    val to = readUInt().toLong()

                    handleGroupOwnershipTransfer(data, from, to)
                } else {
                    val var5 = if (kind == 0 || kind == 1) 0 else readUInt().toInt()
                    val target = readUInt().toLong()

                    if (var5 == 0) {
                        val newPermission = if (remaining == 1L) readByte() else return
                        handlePermissionChange(data, target, newPermission.toInt())
                    }
                }
            }
            34 -> {
                /* quit
                27 0B 60 E7
                01
                2F 55 7C B8
                82
                00 30 42 33 32 46 30 38 33 32 39 32 35 30 31 39 33 45 46 32 45 30 36 35 41 35 41 33 42 37 35 43 41 34 46 37 42 38 42 38 42 44 43 35 35 34 35 44 38 30
                 */
                /* kick
                27 0B 60 E7
                01
                A8 32 51 A1
                83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 39 32 46 45 30 36 31 41 33 37 36 43 44 35 37 35 37 39 45 37 32 34 44 37 37 30 36 46 39 39 43 35 35 33 33 31 34 44 32 44 46 35 45 42 43 31 31 36
                 */

                data.msgData.read {
                    readUInt().toLong() // groupCode
                    readByte().toInt() // follow type
                    val target = readUInt().toLong()
                    val kind = readUByte().toInt()
                    val operator = readUInt().toLong()
                    val groupUin = data.fromUin
                    handleLeave(target, kind, operator, groupUin)
                }
            }
            else -> {
                when {
                    data.msgType == 529 && data.msgSubtype == 9 -> {
                        /*
                        PbMsgInfo#1773430973 {
fromUin=0x0000000026BA1173(649728371)
generalFlag=0x00000001(1)
msgData=0A 07 70 72 69 6E 74 65 72 10 02 1A CD 02 0A 1F 53 61 6D 73 75 6E 67 20 4D 4C 2D 31 38 36 30 20 53 65 72 69 65 73 20 28 55 53 42 30 30 31 29 0A 16 4F 6E 65 4E 6F 74 65 20 66 6F 72 20 57 69 6E 64 6F 77 73 20 31 30 0A 19 50 68 61 6E 74 6F 6D 20 50 72 69 6E 74 20 74 6F 20 45 76 65 72 6E 6F 74 65 0A 11 4F 6E 65 4E 6F 74 65 20 28 44 65 73 6B 74 6F 70 29 0A 1D 4D 69 63 72 6F 73 6F 66 74 20 58 50 53 20 44 6F 63 75 6D 65 6E 74 20 57 72 69 74 65 72 0A 16 4D 69 63 72 6F 73 6F 66 74 20 50 72 69 6E 74 20 74 6F 20 50 44 46 0A 15 46 6F 78 69 74 20 50 68 61 6E 74 6F 6D 20 50 72 69 6E 74 65 72 0A 03 46 61 78 32 09 0A 03 6A 70 67 10 01 18 00 32 0A 0A 04 6A 70 65 67 10 01 18 00 32 09 0A 03 70 6E 67 10 01 18 00 32 09 0A 03 67 69 66 10 01 18 00 32 09 0A 03 62 6D 70 10 01 18 00 32 09 0A 03 64 6F 63 10 01 18 01 32 0A 0A 04 64 6F 63 78 10 01 18 01 32 09 0A 03 74 78 74 10 00 18 00 32 09 0A 03 70 64 66 10 01 18 01 32 09 0A 03 70 70 74 10 01 18 01 32 0A 0A 04 70 70 74 78 10 01 18 01 32 09 0A 03 78 6C 73 10 01 18 01 32 0A 0A 04 78 6C 73 78 10 01 18 01
msgSeq=0x00001AFF(6911)
msgSubtype=0x00000009(9)
msgTime=0x5FDF21A3(1608458659)
msgType=0x00000211(529)
msgUid=0x010000005FDEE04C(72057595646369868)
realMsgTime=0x5FDF21A3(1608458659)
svrIp=0x3E689409(1047041033)
toUin=0x0000000026BA1173(649728371)
}
                         */
                        return
                    }
                }
                throw contextualBugReportException(
                    "解析 OnlinePush.PbPushTransMsg, msgType=${data.msgType}",
                    data._miraiContentToString(),
                    null,
                    "并描述此时机器人是否被踢出, 或是否有成员列表变更等动作."
                )
            }
        }
    }

    private fun PipelineContext.handleLeave(
        target: Long,
        kind: Int,
        operator: Long,
        groupUin: Long,
    ) {
        when (kind) {
            2, 0x82 -> bot.getGroupByUin(groupUin)?.let { group ->
                if (target == bot.id) {
                    collect(BotLeaveEvent.Active(group))
                    bot.groups.delegate.remove(group)
                    group.cancel(CancellationException("Left actively"))
                } else {
                    val member = group[target] ?: return
                    collect(MemberLeaveEvent.Quit(member))
                    group.members.delegate.remove(member)
                    member.cancel(CancellationException("Left actively"))
                }
            }
            3, 0x83 -> bot.getGroupByUin(groupUin)?.let { group ->
                if (target == bot.id) {
                    val member = group.members[operator] ?: return
                    collect(BotLeaveEvent.Kick(member))
                    bot.groups.delegate.remove(group)
                    group.cancel(CancellationException("Being kicked"))
                } else {
                    val member = group[target] ?: return
                    collect(MemberLeaveEvent.Kick(member, group.members[operator]))
                    group.members.delegate.remove(member)
                    member.cancel(CancellationException("Being kicked"))
                }
            }
        }
    }

    /**
     * Group owner changes permission of a member, when bot is a member.
     *
     * @see BotGroupPermissionChangeEvent
     * @see MemberPermissionChangeEvent
     */
    private fun PipelineContext.handlePermissionChange(
        data: OnlinePushTrans.PbMsgInfo,
        target: Long,
        newPermissionByte: Int,
    ) {
        val group = bot.getGroupByUin(data.fromUin) ?: return

        val newPermission = if (newPermissionByte == 1) ADMINISTRATOR else MEMBER

        if (target == bot.id) {
            if (group.botPermission == newPermission) return

            collect(BotGroupPermissionChangeEvent(group, group.botPermission, newPermission))
            group.botAsMember.permission = newPermission
        } else {
            val member = group[target] ?: return
            if (member.permission == newPermission) return

            collect(MemberPermissionChangeEvent(member, member.permission, newPermission))
            member.permission = newPermission
        }
    }

    /**
     * Owner of the group [from] transfers ownership to another member [to], or retrieve ownership.
     */
    // TODO: 2021/6/26 tests
    private suspend fun PipelineContext.handleGroupOwnershipTransfer(
        data: OnlinePushTrans.PbMsgInfo,
        from: Long,
        to: Long,
    ) {
        val group = bot.getGroupByUin(data.fromUin)
        if (from == bot.id) {
            // bot -> member
            group ?: return markAsConsumed()

            // Bot permission changed to MEMBER
            if (group.botPermission != MEMBER) {
                collect(BotGroupPermissionChangeEvent(group, group.botPermission, MEMBER))
                group.botAsMember.permission = MEMBER
            }

            // member Retrieve or permission changed to OWNER
            var newOwner = group[to]
            if (newOwner == null) {
                newOwner = group.addNewNormalMember(MemberInfoImpl(uin = to, nick = "", permission = OWNER)) ?: return
                collect(MemberJoinEvent.Retrieve(newOwner))
            } else if (newOwner.permission != OWNER) {
                collect(MemberPermissionChangeEvent(newOwner, newOwner.permission, OWNER))
                newOwner.permission = OWNER
            }
        } else {
            // member -> bot

            // bot Retrieve or permission changed to OWNER
            if (group == null) {
                collect(BotJoinGroupEvent.Retrieve(bot.addNewGroupByUin(data.fromUin) ?: return))
                return
            }

            // member permission changed to MEMBER
            val member = group[from]
            if (member != null && member.permission != MEMBER) {
                collect(MemberPermissionChangeEvent(member, member.permission, MEMBER))
                member.permission = MEMBER
            } else {
                // if member is null, he has already quit the group in another event.
            }

            if (group.botPermission != OWNER) {
                collect(BotGroupPermissionChangeEvent(group, group.botPermission, OWNER))
                group.botAsMember.permission = OWNER
            }
        }
    }
}