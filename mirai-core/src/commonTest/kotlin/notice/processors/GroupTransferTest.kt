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


internal class GroupTransferTest : AbstractNoticeProcessorTest() {

    @Test
    fun `owner transfers group to other member`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 439,
                msgUid = 14411520,
                msgTime = 162974,
                realMsgTime = 163874,
                msgData = "00 22 07 BB 01 FF 00 12 C4 B2 00 12 C4 B1".hexToBytes(),
                svrIp = 194,
            )
        }

        val group = setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.OWNER)
            }

        runTest().toList().run {
            assertEquals(2, size)
            get(0).run {
                assertIs<MemberPermissionChangeEvent>(this)
                assertEquals(1230002, member.id)
                assertEquals(MemberPermission.OWNER, origin)
                assertEquals(MemberPermission.MEMBER, new)
                assertEquals(MemberPermission.MEMBER, group.members[1230002]!!.permission)
            }
            get(1).run {
                assertIs<MemberPermissionChangeEvent>(this)
                assertEquals(1230001, member.id)
                assertEquals(MemberPermission.MEMBER, origin)
                assertEquals(MemberPermission.OWNER, new)
                assertEquals(MemberPermission.OWNER, group.members[1230001]!!.permission)
            }
        }
    }

    @Test
    fun `owner transfers group to bot`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 291,
                msgUid = 144115188,
                msgTime = 16298,
                realMsgTime = 16298,
                msgData = "00 22 07 BB 01 FF 00 12 C4 B2 00 12 C4 B3".hexToBytes(),
                svrIp = -14676,
            )
        }

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.OWNER)
            }

        runTest().toList().run {
            assertEquals(2, size)
            get(0).run {
                assertIs<MemberPermissionChangeEvent>(this)
                assertEquals(1230002, member.id)
                assertEquals(MemberPermission.OWNER, origin)
                assertEquals(MemberPermission.MEMBER, new)
                assertEquals(MemberPermission.MEMBER, group.members[1230002]!!.permission)
            }
            get(1).run {
                assertIs<BotGroupPermissionChangeEvent>(this)
                assertEquals(MemberPermission.MEMBER, origin)
                assertEquals(MemberPermission.OWNER, new)
                assertEquals(MemberPermission.OWNER, group.botPermission)
            }
        }
    }
}