/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.framework.test

import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertFails

internal class FrameworkEventTest : AbstractTest() {

    class TestEvent : AbstractEvent()
    class TestEvent2 : AbstractEvent()

    @Test
    fun `can observe event`() = runBlockingUnit {
        assertEventBroadcasts<TestEvent> {
            TestEvent().broadcast()
        }
    }

    @Test
    fun `observes expected event`() = runBlockingUnit {
        assertEventBroadcasts<TestEvent>(1) {
            TestEvent().broadcast()
            TestEvent2().broadcast()
        }
    }

    @Test
    fun `can observe event multiple times`() = runBlockingUnit {
        assertEventBroadcasts<TestEvent>(2) {
            TestEvent().broadcast()
            TestEvent().broadcast()
        }
    }

    @Test
    fun `can observe event only in block`() = runBlockingUnit {
        TestEvent().broadcast()
        assertEventBroadcasts<TestEvent>(1) {
            TestEvent().broadcast()
        }
    }

    @Test
    fun `fails if times not match`() = runBlockingUnit {
        assertFails {
            assertEventBroadcasts<TestEvent>(2) {
                TestEvent().broadcast()
            }
        }
        assertFails {
            assertEventBroadcasts<TestEvent>(0) {
                TestEvent().broadcast()
            }
        }
    }
}