/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.impl.common

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitor
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.TestOnly
import kotlin.test.*

internal class SetStateTest : AbstractCommonNHTest() {
    @Test
    fun `setState should ignore duplications INITIALIZED to CLOSED to CLOSED`() {
        assertState(INITIALIZED)
        assertNotNull(network.setStateClosed(IllegalStateException("1")))
        assertState(CLOSED)
        assertNull(network.setStateClosed(IllegalStateException("2")))
        assertState(CLOSED)
    }

    @Test
    fun `setState should ignore duplications OK to CLOSED to CLOSED`() {
        assertNotNull(network.setStateOK(conn))
        assertState(OK)
        assertNotNull(network.setStateClosed(IllegalStateException("1")))
        assertState(CLOSED)
        assertNull(network.setStateClosed(IllegalStateException("2")))
        assertState(CLOSED)
    }

    @Test
    fun `setState should ignore duplications 2 OK to CLOSED to CLOSED`() = runBlockingUnit {
        overrideComponents[BotOfflineEventMonitor] = object : BotOfflineEventMonitor {
            override fun attachJob(bot: AbstractBot, scope: CoroutineScope) {
            }
        }
        assertNotNull(network.setStateOK(conn))
        assertState(OK)
        assertEventBroadcasts<Event> {
            assertNotNull(network.setStateClosed(IllegalStateException("1")))
            assertState(CLOSED)
            assertNull(network.setStateClosed(IllegalStateException("2")))
            assertState(CLOSED)
            eventDispatcher.joinBroadcast()
        }.let { list ->
            assertEquals(1, list.size)
            assertIs<BotOfflineEvent.Active>(list[0])
        }
    }

    @Test
    fun `Precondition - setState should ignore duplications 2 OK to CLOSED to CLOSED`() = runBlockingUnit {
        assertNotNull(network.setStateOK(conn))
        assertState(OK)
        assertEventBroadcasts<Event> {
            assertNotNull(network.setStateClosed(IllegalStateException("1")))
            assertState(CLOSED)
            eventDispatcher.joinBroadcast()
        }.let { list ->
            assertEquals(1, list.size)
            assertIs<BotOfflineEvent.Active>(list[0])
        }
    }
}