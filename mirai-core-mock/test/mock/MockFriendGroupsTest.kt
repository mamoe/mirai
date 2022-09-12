/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import net.mamoe.mirai.mock.test.MockBotTestBase
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MockFriendGroupsTest : MockBotTestBase() {
    @Test
    internal fun testFriendGroupsDefaultEmpty() = runTest {
        assertEquals(1, bot.friendGroups.asCollection().size)
        assertEquals(bot.friendGroups.default, bot.friendGroups[0])
        assertEquals(bot.friendGroups.default, bot.friendGroups.asCollection().iterator().next())
    }

    @Test
    internal fun testFriendGroupCreating() = runTest {
        val group = bot.friendGroups.create("Test")
        println(group.id)
        assertEquals(2, bot.friendGroups.asCollection().size)
        assertEquals(group, bot.friendGroups[group.id])
    }

    @Test
    internal fun testFriendGroupReferences() = runTest {
        val group = bot.friendGroups.create("Test")

        val friend = bot.addFriend(5, "Test")
        assertEquals(bot.friendGroups.default, friend.friendGroup)
        assertEquals(0, friend.mockApi.friendGroupId)

        group.moveIn(friend)
        assertEquals(group, friend.friendGroup)
        assertEquals(group.id, friend.mockApi.friendGroupId)

        group.delete()
        assertEquals(bot.friendGroups.default, friend.friendGroup)

        assertEquals(0, friend.mockApi.friendGroupId)
    }
}