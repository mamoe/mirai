/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.MemberPermissionChangeEvent
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.mock.userprofile.buildUserProfile
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class MockBotBaseTest : MockBotTestBase() {

    @Test
    internal fun testMockBotMocking() = runTest {
        repeat(50) { i ->
            bot.addFriend(20000L + i, "usr$i")
            bot.addStranger(10000L + i, "stranger$i")
            bot.addGroup(798100000L + i, "group$i")
        }
        assertEquals(50, bot.friends.size)
        assertEquals(50, bot.strangers.size)
        assertEquals(50, bot.groups.size)

        repeat(50) { i ->
            assertEquals("usr$i", bot.getFriendOrFail(20000L + i).nick)
            assertEquals("stranger$i", bot.getStrangerOrFail(10000L + i).nick)

            val group = bot.getGroupOrFail(798100000L + i)
            assertEquals("group$i", group.name)
            assertSame(group.botAsMember, group.owner)
            assertSame(MemberPermission.OWNER, group.botPermission)
            assertEquals(0, group.members.size)
        }

        val mockGroup = bot.getGroupOrFail(798100000L)
        repeat(50) { i ->
            mockGroup.appendMember(simpleMemberInfo(3700000L + i, "member$i", permission = MemberPermission.MEMBER))
        }
        repeat(50) { i ->
            val member = mockGroup.getOrFail(3700000L + i)
            assertEquals(MemberPermission.MEMBER, member.permission)
            assertEquals("member$i", member.nick)
            assertTrue(member.nameCard.isEmpty())
            assertEquals(MemberPermission.OWNER, mockGroup.botPermission)
        }

        val newOwner: MockNormalMember
        runAndReceiveEventBroadcast {
            newOwner = mockGroup.addMember(simpleMemberInfo(84485417, "root", permission = MemberPermission.OWNER))
        }.let { events ->
            assertEquals(0, events.size)
        }
        assertEquals(MemberPermission.OWNER, newOwner.permission)
        assertEquals(MemberPermission.MEMBER, mockGroup.botPermission)
        assertSame(newOwner, mockGroup.owner)

        val newNewOwner = mockGroup.getOrFail(3700000L)
        runAndReceiveEventBroadcast {
            mockGroup.changeOwner(newNewOwner)
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<MemberPermissionChangeEvent>(events[0]) {
                assertSame(newNewOwner, member)
                assertSame(MemberPermission.OWNER, new)
                assertSame(MemberPermission.MEMBER, origin)
            }
            assertIsInstance<MemberPermissionChangeEvent>(events[1]) {
                assertSame(newOwner, member)
                assertSame(MemberPermission.OWNER, origin)
                assertSame(MemberPermission.MEMBER, new)
            }
        }
        assertEquals(MemberPermission.OWNER, newNewOwner.permission)
        assertEquals(MemberPermission.MEMBER, newOwner.permission)
        assertEquals(MemberPermission.MEMBER, mockGroup.botPermission)
        assertSame(newNewOwner, mockGroup.owner)
    }

    @Test
    internal fun testQueryProfile() = runTest {
        val service = bot.userProfileService
        val profile = buildUserProfile {
            nickname("Test0")
        }
        service.putUserProfile(1, profile)
        assertSame(profile, Mirai.queryProfile(bot, 1))
    }

}