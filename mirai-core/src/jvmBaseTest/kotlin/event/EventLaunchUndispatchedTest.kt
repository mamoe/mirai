/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.*
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.internal.network.components.EVENT_LAUNCH_UNDISPATCHED
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.AfterEach
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal class EventLaunchUndispatchedTest : AbstractEventTest() {
    internal class TestEvent : AbstractEvent()

    private var originalValue = EVENT_LAUNCH_UNDISPATCHED

    @Test
    fun `event runs undispatched`() = runBlockingUnit {
        originalValue = EVENT_LAUNCH_UNDISPATCHED
        EVENT_LAUNCH_UNDISPATCHED = true
        doTest()
        EVENT_LAUNCH_UNDISPATCHED = originalValue
    }

    @Test
    fun `event runs undispatched fail`() = runBlockingUnit {
        originalValue = EVENT_LAUNCH_UNDISPATCHED
        EVENT_LAUNCH_UNDISPATCHED = false
        assertFails { doTest() }
        EVENT_LAUNCH_UNDISPATCHED = originalValue
    }

    private val dispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    @AfterEach
    fun cleanup() {
        dispatcher.close()
    }

    private suspend fun doTest() = coroutineScope {
        val invoked = ConcurrentLinkedQueue<Int>()

        val thread = Thread.currentThread()

        val job = SupervisorJob()
        globalEventChannel(dispatcher).parentJob(job).exceptionHandler {} // printing exception to stdout is very slow
            .run {
                subscribeAlways<TestEvent>(dispatcher, priority = EventPriority.MONITOR) {
                    assertSame(thread, Thread.currentThread())
                    invoked.add(1)
                    awaitCancellation()
                }
                repeat(1000) { i ->
                    subscribeAlways<TestEvent>(dispatcher, priority = EventPriority.MONITOR) {
                        assertSame(thread, Thread.currentThread())
                        invoked.add(i + 2)
                        awaitCancellation()
                    }
                }
            }

        launch(dispatcher, start = CoroutineStart.UNDISPATCHED) { TestEvent().broadcast() }
        // `launch` returns on first suspension point of `broadcast`
        // if EVENT_LAUNCH_UNDISPATCHED is `true`, all listeners run to `awaitCancellation` when `broadcast` is suspended
        // otherwise, they are put into tasks queue to be scheduled. 10000 tasks wont complete very quickly, so the following `invoked.size` check works.
        assertSame(thread, Thread.currentThread())
        assertEquals(invoked.toList(), invoked.sorted())
        assertEquals(1000 + 1, invoked.size)
        job.cancel()
    }
}