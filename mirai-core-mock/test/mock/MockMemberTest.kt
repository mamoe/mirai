/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.BotGroupPermissionChangeEvent
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

internal class MockMemberTest : MockBotTestBase() {
    @Test
    internal fun testAvatar() = runTest {
        val m = bot.addGroup(111, "aaa").addMember(simpleMemberInfo(222, "bbb", permission = MemberPermission.MEMBER))
        assertNotEquals("", m.avatarUrl)
    }

    @Test
    internal fun changeOwner() = runTest {
        val group = bot.addGroup(111, "aaa")
        val member = group.addMember(simpleMemberInfo(222, "bbb", permission = MemberPermission.MEMBER))
        val events = runAndReceiveEventBroadcast {
            group.changeOwner(member)
            assertSame(member, group.owner)
            assertSame(MemberPermission.OWNER, member.permission)
        }
        assertEquals(2, events.size)
        assertIsInstance<MemberPermissionChangeEvent>(events[0]) {
            assertSame(member, this.member)
            assertSame(MemberPermission.OWNER, new)
            assertSame(MemberPermission.MEMBER, origin)
            assertSame(group, this.group)
        }
        assertIsInstance<BotGroupPermissionChangeEvent>(events[1]) {
            assertSame(MemberPermission.MEMBER, new)
            assertSame(MemberPermission.OWNER, origin)
            assertSame(group, this.group)
        }
    }

    @Test
    internal fun modifyAdmin() = runTest {
        val group = bot.addGroup(111, "aaa")
        group.changeOwner(group.botAsMember)
        val m = group.addMember(simpleMemberInfo(222, "bbb", permission = MemberPermission.MEMBER))
        val events = runAndReceiveEventBroadcast {
            m.modifyAdmin(true)
            assertEquals(MemberPermission.ADMINISTRATOR, m.permission)
            m.modifyAdmin(false)
            assertEquals(MemberPermission.MEMBER, m.permission)
        }
        assertEquals(2, events.size)
        assertIsInstance<MemberPermissionChangeEvent>(events[0]) {
            assertSame(m, member)
            assertSame(MemberPermission.MEMBER, origin)
            assertSame(MemberPermission.ADMINISTRATOR, new)
            assertSame(group, this.group)
        }
        assertIsInstance<MemberPermissionChangeEvent>(events[1]) {
            assertSame(m, member)
            assertSame(MemberPermission.ADMINISTRATOR, origin)
            assertSame(MemberPermission.MEMBER, new)
            assertSame(group, this.group)
        }
    }
}