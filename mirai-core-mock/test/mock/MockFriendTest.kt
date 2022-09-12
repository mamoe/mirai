/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.mock.internal.contact.MockImage
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.utils.cast
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class MockFriendTest : MockBotTestBase() {

    @Test
    internal fun testNewFriendRequest() = runTest {
        runAndReceiveEventBroadcast {
            bot.broadcastNewFriendRequestEvent(
                1, "Hi", 0, "Hello!"
            ).reject()
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<NewFriendRequestEvent>(events[0]) {
                assertEquals(1, fromId)
                assertEquals("Hi", fromNick)
                assertEquals(0, fromGroupId)
                assertEquals("Hello!", message)
            }
            assertEquals(bot.friends.size, 0)
        }

        runAndReceiveEventBroadcast {
            bot.broadcastNewFriendRequestEvent(
                1, "Hi", 0, "Hello!"
            ).accept()
        }.let { events ->
            assertEquals(2, events.size, events.toString())
            assertIsInstance<NewFriendRequestEvent>(events[0]) {
                assertEquals(1, fromId)
                assertEquals("Hi", fromNick)
                assertEquals(0, fromGroupId)
                assertEquals("Hello!", message)
            }

            assertIsInstance<FriendAddEvent>(events[1]) {
                assertEquals(1, friend.id)
                assertEquals("Hi", friend.nick)
                assertSame(friend, bot.getFriend(friend.id))
            }

            assertEquals(1, bot.friends.size)
        }

    }

    @Test
    fun testFriendAvatarChangedEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addFriend(111, "a").changeAvatarUrl(MockImage.random(bot).getUrl(bot))
            bot.addFriend(222, "b")
        }.let { events ->
            assertIsInstance<FriendAvatarChangedEvent>(events[0])
            assertEquals(111, events[0].cast<FriendAvatarChangedEvent>().friend.id)
            assertNotEquals("", bot.getFriend(111)!!.avatarUrl)
            assertNotEquals("", bot.getFriend(222)!!.avatarUrl)
            assertNotEquals("", bot.getFriend(222)!!.avatarUrl.toUrl().readText())
        }
    }

    @Test
    fun testFriendRemarkChangeEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addFriend(1, "").remark = "Test"
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<FriendRemarkChangeEvent>(events[0]) {
                assertEquals(1, this.friend.id)
                assertEquals("", oldRemark)
                assertEquals("Test", newRemark)
            }
        }
    }

    @Test
    fun testFriendRequestAndAddEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.broadcastNewFriendRequestEvent(
                1, "Test", 0, "Hi"
            ).accept()
            bot.broadcastNewFriendRequestEvent(
                2, "Hi", 1, "0"
            ).reject()
        }.let { events ->
            assertEquals(3, events.size)
            assertIsInstance<NewFriendRequestEvent>(events[0]) {
                assertEquals(1, fromId)
                assertEquals("Test", fromNick)
                assertEquals(0, fromGroupId)
                assertEquals("Hi", message)
            }
            assertIsInstance<FriendAddEvent>(events[1]) {
                assertEquals(1, friend.id)
                assertEquals("Test", friend.nick)
            }
            assertIsInstance<NewFriendRequestEvent>(events[2]) {
                assertEquals(2, fromId)
                assertEquals("Hi", fromNick)
                assertEquals(1, fromGroupId)
                assertEquals("0", message)
            }
        }
    }

    @Test
    fun testFriendNickChangedEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addFriend(0, "Old").nick = "Test"
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<FriendNickChangedEvent>(events[0]) {
                assertEquals("Old", from)
                assertEquals("Test", to)
            }
        }
    }

    @Test
    fun testFriendInputStatusChangedEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addFriend(1, "a").broadcastFriendInputStateChange(true)
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<FriendInputStatusChangedEvent>(events[0]) {
                assertTrue(inputting)
                assertSame(bot.getFriend(1), friend)
            }
        }
    }

}