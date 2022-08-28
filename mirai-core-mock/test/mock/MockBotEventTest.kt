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
import net.mamoe.mirai.mock.test.MockBotTestBase
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MockBotEventTest : MockBotTestBase() {
    @Test
    fun testBotOnlineEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.login()
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<BotOnlineEvent>(events[0])
        }
    }

    @Test
    fun testBotOfflineEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.broadcastOfflineEvent()
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<BotOfflineEvent>(events[0])
        }
    }

    @Test
    fun testBotRelogin() = runTest {
        bot.login()
        runAndReceiveEventBroadcast {
            bot.login()
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<BotOnlineEvent>(events[0])
            assertIsInstance<BotReloginEvent>(events[1])
        }
    }

    @Test
    fun testMockAvatarChange() = runTest {
        assertEquals("http://q.qlogo.cn/g?b=qq&nk=${bot.id}&s=640", bot.avatarUrl)
        runAndReceiveEventBroadcast {
            bot.avatarUrl = "http://localhost/test.png"
            assertEquals("http://localhost/test.png", bot.avatarUrl)
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<BotAvatarChangedEvent>(events[0])
        }
    }

    @Test
    fun testBotNickChangedEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.nickNoEvent = "HiHi"
            bot.nick = "AAAA"
            bot nickChangesTo "BBBB"
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<BotNickChangedEvent>(events[0]) {
                assertEquals("HiHi", from)
                assertEquals("AAAA", to)
            }
            assertIsInstance<BotNickChangedEvent>(events[1]) {
                assertEquals("AAAA", from)
                assertEquals("BBBB", to)
            }
        }
    }
}