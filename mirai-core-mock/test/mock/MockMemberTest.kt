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
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class MockMemberTest : MockBotTestBase() {
    @Test
    internal fun testAvatar() = runTest {
        val m = bot.addGroup(111, "aaa").addMember(simpleMemberInfo(222, "bbb", permission = MemberPermission.MEMBER))
        assertNotEquals("", m.avatarUrl)
    }

    @Test
    internal fun modifyAdmin() = runTest {
        val m = bot.addGroup(111, "aaa").also { group ->
            group.changeOwner(group.botAsMember)
        }.addMember(simpleMemberInfo(222, "bbb", permission = MemberPermission.MEMBER))
        runAndReceiveEventBroadcast {
            m.modifyAdmin(true)
        }.let { events ->
            assertTrue {
                events.size == 1 && events[0] is MemberPermissionChangeEvent
            }
            val event = events[0] as MemberPermissionChangeEvent
            assertTrue {
                event.new == MemberPermission.ADMINISTRATOR
                        && event.member.id == 222L
            }
        }
        assertEquals(MemberPermission.ADMINISTRATOR, m.permission)

        runAndReceiveEventBroadcast {
            m.modifyAdmin(false)
        }.let { events ->
            assertTrue {
                events.size == 1 && events[0] is MemberPermissionChangeEvent
            }
            val event = events[0] as MemberPermissionChangeEvent
            assertTrue {
                event.new == MemberPermission.MEMBER
                        && event.member.id == 222L
            }
        }
        assertEquals(MemberPermission.MEMBER, m.permission)
    }
}