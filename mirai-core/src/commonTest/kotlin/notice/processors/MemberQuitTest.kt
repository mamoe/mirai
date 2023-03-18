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
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class MemberQuitTest : AbstractNoticeProcessorTest() {

    @Test
    fun `member active quit`() = runBlockingUnit {
        suspend fun runTest() = use {
            OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 34,
                msgSeq = 266,
                msgUid = 1441151,
                msgTime = 16298,
                realMsgTime = 1629,
                msgData = "00 22 07 BB 01 00 12 C4 B1 02 00 30 39 41 36 36 41 32 31 32 33 35 37 32 43 39 35 38 42 42 36 38 45 32 36 44 34 34 32 38 45 32 32 37 32 36 44 39 44 45 41 31 34 41 44 37 30 31 46 31".hexToBytes(),
                svrIp = 618,
                extGroupKeyInfo = OnlinePushTrans.ExtGroupKeyInfo(
                    curMaxSeq = 1626,
                    curTime = 16298,
                ),
                generalFlag = 1,
            )
        }

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, "user1", MemberPermission.MEMBER)
                addMember(1230002, "user2", MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(1, size)
            single().run {
                assertIs<MemberLeaveEvent.Quit>(this)
                assertEquals(1230001, member.id)
                assertEquals(null, group.members[1230001])
            }
        }
    }

    @Test
    fun `member kick`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230002,
                msgType = 34,
                msgSeq = 430,
                msgUid = 1441,
                msgTime = 16298,
                realMsgTime = 1629,
                msgData = "00 22 07 BB 01 00 12 C4 B1 03 00 12 C4 B3 06 B4 B4 BD A8 D5 DF 00 30 45 31 39 41 35 43 41 37 34 36 44 37 38 31 36 45 34 46 36 37 41 39 35 36 46 32 34 46 46 38 33 41 32 30 34 44 41 33 38 30 35 41 38 34 39 45 44 32".hexToBytes(),
                svrIp = 54562,
                extGroupKeyInfo = net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.ExtGroupKeyInfo(
                    curMaxSeq = 1627,
                    curTime = 1629,
                ),
                generalFlag = 1,
            )
        }

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, "user1", MemberPermission.MEMBER)
                addMember(1230002, "user2", MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(1, size)
            single().run {
                assertIs<MemberLeaveEvent.Kick>(this)
                assertEquals(1230001, member.id)
                assertEquals(null, group.members[1230001])
            }
        }
    }

}