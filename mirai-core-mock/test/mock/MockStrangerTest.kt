/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import net.mamoe.mirai.event.events.StrangerRelationChangeEvent
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.utils.cast
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class MockStrangerTest : MockBotTestBase() {
    @Test
    internal fun testStrangerRelationChangeEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addStranger(111, "aa").addAsFriend()
            bot.addStranger(222, "bb").delete()
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<StrangerRelationChangeEvent.Friended>(events[0])
            assertEquals(111, events[0].cast<StrangerRelationChangeEvent.Friended>().friend.id)
            assertIsInstance<StrangerRelationChangeEvent.Deleted>(events[1])
            assertEquals(222, events[1].cast<StrangerRelationChangeEvent.Deleted>().stranger.id)
            assertNotEquals("", bot.getFriend(111)!!.avatarUrl)
        }
    }
}