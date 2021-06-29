/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.internal.event.EVENT_LAUNCH_UNDISPATCHED
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal class EventLaunchUndispatchedTest : AbstractEventTest() {
    internal class TestEvent : AbstractEvent()

    val originalValue = EVENT_LAUNCH_UNDISPATCHED

    @Test
    suspend fun `event runs undispatched`() {
        EVENT_LAUNCH_UNDISPATCHED = true
        doTest()
        EVENT_LAUNCH_UNDISPATCHED = originalValue
    }

    @Test
    suspend fun `event runs undispatched fail`() {
        EVENT_LAUNCH_UNDISPATCHED = false
        assertFails { doTest() }
        EVENT_LAUNCH_UNDISPATCHED = originalValue
    }

    private suspend fun doTest() = coroutineScope {
        val invoked = ConcurrentLinkedQueue<Int>()

        val thread = Thread.currentThread()

        val job = SupervisorJob()
        globalEventChannel().parentJob(job).exceptionHandler { } // printing exception to stdout is very slow
            .run {
                subscribeAlways<TestEvent>(priority = EventPriority.MONITOR) {
                    assertSame(thread, Thread.currentThread())
                    invoked.add(1)
                    awaitCancellation()
                }
                repeat(10000) { i ->
                    subscribeAlways<TestEvent>(priority = EventPriority.MONITOR) {
                        assertSame(thread, Thread.currentThread())
                        invoked.add(i + 2)
                        awaitCancellation()
                    }
                }
            }

        launch(start = CoroutineStart.UNDISPATCHED) { TestEvent().broadcast() }
        // `launch` returns on first suspension point of `broadcast`
        // if EVENT_LAUNCH_UNDISPATCHED is `true`, all listeners run to `awaitCancellation` when `broadcast` is suspended
        // otherwise, they are put into tasks queue to be scheduled. 10000 tasks wont complete very quickly, so the following `invoked.size` check works.
        assertSame(thread, Thread.currentThread())
        assertEquals(invoked.toList(), invoked.sorted())
        assertEquals(10000 + 1, invoked.size)
        job.cancel()
    }
}