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
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


/**
 * Set/cancel admin permission for bot/member
 */
internal class MemberAdminChangeTest : AbstractNoticeProcessorTest() {

    @Test
    fun `bot member to admin`() = runBlockingUnit {
        suspend fun runTest() = use {
            OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 4827,
                msgUid = 144,
                msgTime = 162,
                realMsgTime = 1629,
                msgData = "00 22 07 BB 01 01 00 12 C4 B3 01".hexToBytes(),
                svrIp = 12165,
            )
        }

        // bot was MEMBER

        val group = setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<BotGroupPermissionChangeEvent>(event)
            assertEquals(MemberPermission.MEMBER, event.origin)
            assertEquals(MemberPermission.ADMINISTRATOR, event.new)
            assertEquals(MemberPermission.ADMINISTRATOR, group.botPermission)
        }


        // bot was ADMINISTRATOR

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.OWNER)
                botAsMember.permission = MemberPermission.ADMINISTRATOR
            }

        runTest().run {
            assertEquals(0, size)
        }
    }

    @Test
    fun `bot admin to member`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 483,
                msgUid = 14411512,
                msgTime = 1629863,
                realMsgTime = 1623063,
                msgData = "00 22 07 BB 01 00 00 12 C4 B3 00".hexToBytes(),
                svrIp = 2039273,
            )
        }

        val group = setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.OWNER)
                botAsMember.permission = MemberPermission.ADMINISTRATOR
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<BotGroupPermissionChangeEvent>(event)
            assertEquals(MemberPermission.ADMINISTRATOR, event.origin)
            assertEquals(MemberPermission.MEMBER, event.new)
            assertEquals(MemberPermission.MEMBER, group.botPermission)
        }


        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.OWNER)
                botAsMember.permission = MemberPermission.MEMBER // already member
            }

        runTest().run {
            assertEquals(0, size)
        }
    }

    @Test
    fun `member member to admin`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 5639,
                msgUid = 1441812,
                msgTime = 1623204,
                realMsgTime = 1623204,
                msgData = "00 22 07 BB 01 01 00 12 C4 B1 01".hexToBytes(),
                svrIp = -20900855,
            )
        }

        // member was MEMBER

        val group = setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                addMember(1230002, permission = MemberPermission.OWNER)
            }


        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberPermissionChangeEvent>(event)
            assertEquals(1230001, event.member.id)
            assertEquals(MemberPermission.MEMBER, event.origin)
            assertEquals(MemberPermission.ADMINISTRATOR, event.new)
            assertEquals(MemberPermission.ADMINISTRATOR, group.members[1230001]!!.permission)
        }


        // member was already ADMINISTRATOR

        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.ADMINISTRATOR)
                addMember(1230002, permission = MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(0, size)
        }
    }

    @Test
    fun `member admin to member`() = runBlockingUnit {
        suspend fun runTest() = use {
            net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo(
                fromUin = 2230203,
                toUin = 1230003,
                msgType = 44,
                msgSeq = 745,
                msgUid = 144115576812,
                msgTime = 162250,
                realMsgTime = 16290,
                msgData = "00 22 07 BB 01 00 00 12 C4 B1 00".hexToBytes(),
                svrIp = 277969,
            )
        }

        val group = setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                    .permission = MemberPermission.ADMINISTRATOR
                addMember(1230002, permission = MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(1, size)
            val event = single()
            assertIs<MemberPermissionChangeEvent>(event)
            assertEquals(1230001, event.member.id)
            assertEquals(MemberPermission.ADMINISTRATOR, event.origin)
            assertEquals(MemberPermission.MEMBER, event.new)
            assertEquals(MemberPermission.MEMBER, group.members[1230001]!!.permission)
        }


        setBot(1230003)
            .addGroup(2230203, 1230002, name = "testtest").apply {
                addMember(1230001, permission = MemberPermission.MEMBER)
                    .permission = MemberPermission.MEMBER // already member
                addMember(1230002, permission = MemberPermission.OWNER)
            }

        runTest().run {
            assertEquals(0, size)
        }
    }


}