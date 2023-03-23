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
import net.mamoe.mirai.event.events.BotGroupPermissionChangeEvent
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class GroupRetrieveTest : AbstractNoticeProcessorTest() {

    @Test
    fun `other member retrieves group from another member when they are in the group`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 27,
                msgUid = 14411,
                msgTime = 1629,
                realMsgTime = 164,
                msgData = "00 22 07 BB 01 FF 00 12 C4 B1 00 12 C4 B2".hexToBytes(),
                svrIp = -9623,
            )
        }

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.OWNER)
                addMember(1230002, permission = MemberPermission.MEMBER)
            }

        runTest().toList().run {
            assertEquals(2, size, toString())
            get(0).run {
                assertIs<MemberPermissionChangeEvent>(this)
                assertEquals(1230001, member.id)
                assertEquals(MemberPermission.OWNER, origin)
                assertEquals(MemberPermission.MEMBER, new)
                assertEquals(MemberPermission.MEMBER, group.members[1230001]!!.permission)
            }
            get(1).run {
                assertIs<MemberPermissionChangeEvent>(this)
                assertEquals(1230002, member.id)
                assertEquals(MemberPermission.MEMBER, origin)
                assertEquals(MemberPermission.OWNER, new)
                assertEquals(MemberPermission.OWNER, group.members[1230002]!!.permission)
            }
        }
    }

    @Test
    fun `other member retrieves group from bot when they are in the group`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 459,
                msgUid = 14411518,
                msgTime = 1629,
                realMsgTime = 1629,
                msgData = "00 22 07 BB 01 FF 00 12 C4 B3 00 12 C4 B2".hexToBytes(),
                svrIp = -164,
            )
        }

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.MEMBER)
                botAsMember.permission = MemberPermission.OWNER
            }

        runTest().toList().run {
            assertEquals(2, size, toString())
            get(0).run {
                assertIs<BotGroupPermissionChangeEvent>(this)
                assertEquals(MemberPermission.OWNER, origin)
                assertEquals(MemberPermission.MEMBER, new)
                assertEquals(MemberPermission.MEMBER, group.botPermission)
            }
            get(1).run {
                assertIs<MemberPermissionChangeEvent>(this)
                assertEquals(1230002, member.id)
                assertEquals(MemberPermission.MEMBER, origin)
                assertEquals(MemberPermission.OWNER, new)
                assertEquals(MemberPermission.OWNER, group.members[1230002]!!.permission)
            }
        }
    }
}