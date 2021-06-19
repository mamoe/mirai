/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.component

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.TestOnly
import network.framework.components.TestEventDispatcherImpl
import org.junit.jupiter.api.Test

internal class EventDispatcherTest : AbstractTest() {
    private class Ev : AbstractEvent()

    private val dispatcher = TestEventDispatcherImpl(SupervisorJob(), MiraiLogger.TopLevel)

    @Test
    fun `can broadcast`() = runBlockingUnit {
        assertEventBroadcasts<Ev>(1) {
            dispatcher.broadcast(Ev())
        }
    }

    @Test
    fun `can async`() = runBlockingUnit {
        assertEventBroadcasts<Ev>(1) {
            dispatcher.broadcastAsync(Ev())
            (dispatcher.coroutineContext.job as CompletableJob).run {
                complete()
                join()
            }
        }
    }

    @Test
    fun `can join`() = runBlockingUnit {
        assertEventBroadcasts<Ev>(20) {
            repeat(20) {
                dispatcher.broadcastAsync(Ev())
            }
            dispatcher.joinBroadcast()
        }
    }

    @Test
    fun `broadcastAsync starts job immediately`() = runBlockingUnit {
        assertEventBroadcasts<Ev>(1) {
            dispatcher.broadcastAsync(Ev())
            dispatcher.joinBroadcast()
        }
    }

    @Test
    fun `broadcastAsync starts job immediately parallel`() = runBlockingUnit {
        assertEventBroadcasts<Ev>(20) {
            repeat(20) {
                dispatcher.broadcastAsync(Ev())
            }
            dispatcher.joinBroadcast()
        }
    }
}