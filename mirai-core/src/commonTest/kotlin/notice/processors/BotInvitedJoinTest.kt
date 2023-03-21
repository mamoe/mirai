/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.notice.processors

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipelineImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class BotInvitedJoinTest : AbstractNoticeProcessorTest() {
    @Test
    fun `invited join`() = runBlockingUnit {
        suspend fun runTest() = use {

            Structmsg.StructMsg(
                version = 1,
                msgType = 2,
                msgSeq = 1630,
                msgTime = 1630,
                reqUin = 1230,
                msg = Structmsg.SystemMsg(
                    subType = 1,
                    msgTitle = "邀请加群",
                    msgDescribe = "邀请你加入 %group_name%",
                    actions = mutableListOf(
                        Structmsg.SystemMsgAction(
                            name = "拒绝",
                            result = "已拒绝",
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = 12,
                                groupCode = 2230203,
                            ),
                            detailName = "拒绝",
                        ), Structmsg.SystemMsgAction(
                            name = "同意",
                            result = "已同意",
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = 11,
                                groupCode = 2230203,
                            ),
                            detailName = "同意",
                        ), Structmsg.SystemMsgAction(
                            name = "忽略",
                            result = "已忽略",
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = 14,
                                groupCode = 2230203,
                            ),
                            detailName = "忽略",
                        )
                    ),
                    groupCode = 2230203,
                    actionUin = 1230001,
                    groupMsgType = 2,
                    groupInviterRole = 1,
                    groupInfo = Structmsg.GroupInfo(
                        appPrivilegeFlag = 67698880,
                    ),
                    msgInviteExtinfo = Structmsg.MsgInviteExt(
                    ),
                    reqUinNick = "user3",
                    groupName = "testtest",
                    actionUinNick = "user1",
                    groupExtFlag = 1075905600,
                    actionUinQqNick = "user1",
                    reqUinGender = 255,
                    c2cInviteJoinGroupFlag = 1,
                ),
            )
        }

        setBot(1230003)

        runTest().toList().run {
            assertEquals(1, size, toString())
            get(0).run {
                assertIs<BotInvitedJoinGroupRequestEvent>(this)
                assertEquals(1230001, invitorId)
                assertEquals("user1", invitorNick)
                assertEquals(2230203, groupId)
                assertEquals("testtest", groupName)
                assertEquals(1630, eventId)
            }
        }

    }

    @Test
    fun `invited join  accepted`() = runBlockingUnit {
        // https://github.com/mamoe/mirai/issues/1213
        suspend fun runTest() = use(
            createContext = { attributes ->
                object : NoticeProcessorPipelineImpl.ContextImpl(attributes) {
                    override suspend fun QQAndroidBot.addNewGroupByCode(code: Long): GroupImpl {
                        assertEquals(2230203, code)
                        return bot.addGroup(2230203, 1230001, name = "testtest").apply {
                            addMember(1230003, permission = MemberPermission.MEMBER)
                            addMember(1230001, permission = MemberPermission.OWNER)
                        }
                    }
                }
            }
        ) {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 2230203,
                    toUin = 1230002,
                    msgType = 33,
                    msgSeq = 593,
                    msgTime = 1632,
                    msgUid = 14411518,
                    authUin = 1230002,
                    authNick = "user2",
                    extGroupKeyInfo = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ExtGroupKeyInfo(
                        curMaxSeq = 1652,
                        curTime = 16327,
                    ),
                    authSex = 2,
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    msgContent = "00 22 07 BB 01 00 12 C4 B2 03 00 12 C4 B1 06 B4 B4 BD A8 D5 DF 00 30 31 39 37 31 44 32 34 34 31 44 30 42 30 45 41 44 42 35 35 32 46 43 33 31 35 32 36 33 43 45 39 39 43 45 43 43 35 46 30 35 45 32 46 38 39 44 41 33".hexToBytes(),
                ),
            )
        }

        setBot(1230002)

        runTest().toList().run {
            assertEquals(1, size, toString())
            get(0).run {
                assertIs<BotJoinGroupEvent.Invite>(this)
                assertEquals(1230001, invitor.id)
            }
        }

    }


    @Test
    fun `invitation accepted`() = runBlockingUnit {
        suspend fun runTest() =
            use(createContext = { attributes ->
                object : NoticeProcessorPipelineImpl.ContextImpl(attributes) {
                    override suspend fun QQAndroidBot.addNewGroupByCode(code: Long): GroupImpl {
                        assertEquals(2230203, code)
                        return bot.addGroup(2230203, 1230002, name = "testtest").apply {
                            addMember(1230001, permission = MemberPermission.MEMBER)
                        }
                    }
                }
            }) {
                net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                    msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                        fromUin = 2230203,
                        toUin = 1230003,
                        msgType = 33,
                        msgSeq = 61485,
                        msgTime = 1630,
                        msgUid = 1441,
                        authUin = 1230003,
                        authNick = "user3",
                        extGroupKeyInfo = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ExtGroupKeyInfo(
                            curMaxSeq = 1631,
                            curTime = 1630,
                        ),
                    ),
                    contentHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ContentHead(
                    ),
                    msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                        richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                        ),
                        msgContent = "00 22 07 BB 01 00 12 C4 B3 03 00 12 C4 B1 00 00 30 34 32 42 39 44 46 43 34 39 45 42 34 30 46 41 42 45 45 32 33 36 34 37 45 46 39 35 31 44 44 42 31 31 32 36 31 31 38 44 43 46 44 32 37 42 30 42 45".hexToBytes(),
                    ),
                )
            }

        setBot(1230003)

        runTest().toList().run {
            assertEquals(1, size, toString())
            get(0).run {
                assertIs<BotJoinGroupEvent.Invite>(this)
                assertEquals(1230001, invitor.id)
                assertEquals(2230203, group.id)
            }
        }
    }
}