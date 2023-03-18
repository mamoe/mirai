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
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.*

internal class MemberJoinTest : AbstractNoticeProcessorTest() {

    @Test
    fun `member actively request join`() = runBlockingUnit {
        suspend fun runTest() = use {
            Structmsg.StructMsg(
                version = 1,
                msgType = 2,
                msgSeq = 16300,
                msgTime = 1630,
                reqUin = 1230001,
                msg = Structmsg.SystemMsg(
                    subType = 1,
                    msgTitle = "加群申请",
                    msgDescribe = "申请加入 %group_name%",
                    msgAdditional = "verification message",
                    srcId = 1,
                    subSrcId = 5,
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
                    groupMsgType = 1,
                    groupInfo = Structmsg.GroupInfo(
                        appPrivilegeFlag = 67698880,
                    ),
                    groupFlagext3 = 128,
                    reqUinFaceid = 7425,
                    reqUinNick = "user1",
                    groupName = "testtest",
                    groupExtFlag = 1075905600,
                    actionUinQqNick = "user1",
                    reqUinGender = 1,
                    reqUinAge = 19,
                ),
            )
        }

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest", botPermission = MemberPermission.ADMINISTRATOR).apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberJoinRequestEvent>(event)
            assertEquals(1230001, event.fromId)
            assertEquals(2230203, event.groupId)
            assertEquals("verification message", event.message)
            assertEquals("testtest", event.groupName)
        }
    }

    @Test
    fun `member request accepted by other admin`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 2230203,
                    toUin = 1230003,
                    msgType = 33,
                    msgSeq = 45,
                    msgTime = 16,
                    msgUid = 1441,
                    authUin = 1230001,
                    authNick = "user1",
                    extGroupKeyInfo = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ExtGroupKeyInfo(
                        curMaxSeq = 1628,
                        curTime = 1630,
                    ),
                    authSex = 2,
                ),
                contentHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ContentHead(
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                    ),
                    msgContent = "00 22 07 BB 01 00 12 C4 B1 02 00 12 C4 B3 06 B9 DC C0 ED D4 B1 00 30 44 38 32 41 43 32 46 33 30 36 46 44 34 35 30 30 36 38 32 46 36 41 38 32 30 31 38 34 41 42 30 43 43 30 32 43 41 33 33 37 41 31 30 38 43 32 36 36".hexToBytes(),
                ),
            )

        }

        val group = setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest", botPermission = MemberPermission.ADMINISTRATOR).apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
            }

        assertNull(group.members[1230001])

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberJoinEvent.Active>(event)
            assertEquals(2230203, event.groupId)
            assertEquals(1230001, event.member.id)
            assertNotNull(group.members[1230001])
        }
    }

    @Test
    fun `member request accepted by bot as admin`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 2230203,
                    toUin = 1230002,
                    msgType = 33,
                    msgSeq = 45576,
                    msgTime = 1640123193,
                    msgUid = 144115188080508961,
                    authUin = 1230003,
                    authNick = "user3",
                    extGroupKeyInfo = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ExtGroupKeyInfo(
                        curMaxSeq = 1773,
                        curTime = 1640123193,
                    ),
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    msgContent = "00 22 07 BB 01 00 12 C4 B3 82 00 12 C4 B2 06 B9 DC C0 ED D4 B1 00 30 30 38 38 32 32 30 42 30 31 35 42 34 42 42 30 32 44 43 38 30 41 38 37 45 45 45 46 38 42 41 37 45 31 43 32 44 37 32 30 43 37 32 41 34 42 31 39 32".hexToBytes(),
                ),
            )
        }

        val group = setBot(1230002)
            .addGroup(2230203, 1230001, name = "testtest", botPermission = MemberPermission.ADMINISTRATOR).apply {
                addMember(1230001, "user2", MemberPermission.OWNER)
                addMember(1230002, "bot", MemberPermission.ADMINISTRATOR)
            }

        assertNull(group.members[1230003])

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberJoinEvent.Active>(event)
            assertEquals(2230203, event.groupId)
            assertEquals(1230003, event.member.id)
            assertNotNull(group.members[1230003])
        }
    }

    @Test
    fun `member request rejected by other admin`() {
        // There is no corresponding event
    }


    @Test
    fun `member joins directly when group allows anyone`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
                msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                    fromUin = 2230203,
                    toUin = 1230003,
                    msgType = 33,
                    msgSeq = 45,
                    msgTime = 16,
                    msgUid = 1441,
                    authUin = 1230001,
                    authNick = "user1",
                    extGroupKeyInfo = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ExtGroupKeyInfo(
                        curMaxSeq = 1628,
                        curTime = 1630,
                    ),
                    authSex = 2,
                ),
                contentHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.ContentHead(
                ),
                msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                    richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                    ),
                    msgContent = "00 22 07 BB 01 00 12 C4 B1 02 00 12 C4 B3 06 B9 DC C0 ED D4 B1 00 30 44 38 32 41 43 32 46 33 30 36 46 44 34 35 30 30 36 38 32 46 36 41 38 32 30 31 38 34 41 42 30 43 43 30 32 43 41 33 33 37 41 31 30 38 43 32 36 36".hexToBytes(),
                ),
            )

        }

        val group = setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest", botPermission = MemberPermission.ADMINISTRATOR).apply {
                addMember(1230002, "user2", MemberPermission.OWNER)
            }

        assertNull(group.members[1230001])

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberJoinEvent.Active>(event)
            assertEquals(2230203, event.groupId)
            assertEquals(1230001, event.member.id)
            assertNotNull(group.members[1230001])
        }
    }


}