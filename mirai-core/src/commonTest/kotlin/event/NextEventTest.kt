/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.*

internal class NextEventTest : AbstractEventTest() {
    data class TE(
        val x: Int
    ) : AbstractEvent()

    data class TE2(
        val x: Int
    ) : AbstractEvent()

    ///////////////////////////////////////////////////////////////////////////
    // nextEvent
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `nextEvent can receive`() = runBlockingUnit {
        val channel = GlobalEventChannel

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            channel.nextEvent<TE>()
        }

        TE(1).broadcast()
        yield()
        assertTrue { deferred.isCompleted }
        assertEquals(TE(1), deferred.await())
    }

    @Test
    fun `nextEvent can filter type`() = runBlockingUnit {
        val channel = GlobalEventChannel

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            channel.nextEvent<TE>()
        }

        TE2(1).broadcast()
        yield()
        assertFalse { deferred.isCompleted }

        TE(1).broadcast()
        yield()
        assertTrue { deferred.isCompleted }
        assertEquals(TE(1), deferred.await())
    }

    @Test
    fun `nextEvent can filter by filter`() = runBlockingUnit {
        val channel = GlobalEventChannel

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            channel.nextEvent<TE> { it.x == 2 }
        }

        TE(1).broadcast()
        yield()
        assertFalse { deferred.isCompleted }

        TE(2).broadcast()
        yield()
        assertTrue { deferred.isCompleted }
        assertEquals(TE(2), deferred.await())
    }

    @Test
    fun `nextEvent can timeout`() = runBlockingUnit {
        val channel = GlobalEventChannel

        assertFailsWith<TimeoutCancellationException> {
            withTimeout(timeMillis = 1) { channel.nextEvent<TE>(EventPriority.MONITOR) }
        }
    }

    @Test
    fun `nextEvent can cancel`() = runBlockingUnit {
        val channel = GlobalEventChannel

        coroutineScope {
            val job = launch {
                val result = kotlin.runCatching { channel.nextEvent<TE>(EventPriority.MONITOR) }
                assertTrue { result.isFailure }
                assertIs<CancellationException>(result.exceptionOrNull())
                throw result.exceptionOrNull()!!
            }
            assertTrue { job.isActive }
            job.cancelAndJoin()
            assertTrue { job.isCancelled }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // nextEventOrNull
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `nextEventOrNull can receive`() = runBlockingUnit {
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeoutOrNull<TE>(5000) { globalEventChannel().nextEvent(EventPriority.MONITOR) }
        }

        TE(1).broadcast()
        yield()
        assertTrue { deferred.isCompleted }
        assertEquals(TE(1), deferred.await())
    }

    @Test
    fun `nextEventOrNull can filter type`() = runBlockingUnit {
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeoutOrNull<TE>(5000) { globalEventChannel().nextEvent(EventPriority.MONITOR) }
        }

        TE2(1).broadcast()
        yield()
        assertFalse { deferred.isCompleted }

        TE(1).broadcast()
        yield()
        assertTrue { deferred.isCompleted }
        assertEquals(TE(1), deferred.await())
    }

    @Test
    fun `nextEventOrNull can filter by filter`() = runBlockingUnit {
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeoutOrNull<TE>(5000) { globalEventChannel().nextEvent(EventPriority.MONITOR) { it.x == 2 } }
        }

        TE(1).broadcast()
        yield()
        assertFalse { deferred.isCompleted }

        TE(2).broadcast()
        yield()
        assertTrue { deferred.isCompleted }
        assertEquals(TE(2), deferred.await())
    }

    @Test
    fun `nextEventOrNull can timeout`() = runBlockingUnit {
        assertEquals(null,
            withTimeoutOrNull<TE>(timeMillis = 1) { globalEventChannel().nextEvent(EventPriority.MONITOR) })
    }
}