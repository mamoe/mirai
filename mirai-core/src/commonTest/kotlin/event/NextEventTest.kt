/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION")

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.*
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.event.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@JvmBlockingBridge
internal class NextEventTest : AbstractEventTest() {
    private val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    @AfterEach
    fun stopDispatcher() {
        dispatcher.close()
    }


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
    suspend fun `nextEvent can receive`() {
        val channel = GlobalEventChannel

        withContext(dispatcher) {
            val deferred = async(start = CoroutineStart.UNDISPATCHED) {
                channel.nextEvent<TE>()
            }

            TE(1).broadcast()
            yield()
            assertTrue { deferred.isCompleted }
            assertEquals(TE(1), deferred.await())
        }
    }

    @Test
    suspend fun `nextEvent can filter type`() {
        val channel = GlobalEventChannel

        withContext(dispatcher) {
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
    }

    @Test
    suspend fun `nextEvent can filter by filter`() {
        val channel = GlobalEventChannel

        withContext(dispatcher) {
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
    }

    @Test
    suspend fun `nextEvent can timeout`() {
        val channel = GlobalEventChannel

        withContext(dispatcher) {
            assertThrows<TimeoutCancellationException> {
                withTimeout(timeMillis = 1) { channel.nextEvent<TE>(EventPriority.MONITOR) }
            }
        }
    }

    @Test
    suspend fun `nextEvent can cancel`() {
        val channel = GlobalEventChannel

        withContext(dispatcher) {
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
    }

    ///////////////////////////////////////////////////////////////////////////
    // nextEventOrNull
    ///////////////////////////////////////////////////////////////////////////

    @Test
    suspend fun `nextEventOrNull can receive`() {
        withContext(dispatcher) {
            val deferred = async(start = CoroutineStart.UNDISPATCHED) {
                nextEventOrNull<TE>(5000)
            }

            TE(1).broadcast()
            yield()
            assertTrue { deferred.isCompleted }
            assertEquals(TE(1), deferred.await())
        }
    }

    @Test
    suspend fun `nextEventOrNull can filter type`() {
        withContext(dispatcher) {
            val deferred = async(start = CoroutineStart.UNDISPATCHED) {
                nextEventOrNull<TE>(5000)
            }

            TE2(1).broadcast()
            yield()
            assertFalse { deferred.isCompleted }

            TE(1).broadcast()
            yield()
            assertTrue { deferred.isCompleted }
            assertEquals(TE(1), deferred.await())
        }
    }

    @Test
    suspend fun `nextEventOrNull can filter by filter`() {
        withContext(dispatcher) {
            val deferred = async(start = CoroutineStart.UNDISPATCHED) {
                nextEventOrNull<TE>(5000) { it.x == 2 }
            }

            TE(1).broadcast()
            yield()
            assertFalse { deferred.isCompleted }

            TE(2).broadcast()
            yield()
            assertTrue { deferred.isCompleted }
            assertEquals(TE(2), deferred.await())
        }
    }

    @Test
    suspend fun `nextEventOrNull can timeout`() {
        withContext(dispatcher) {
            assertEquals(null, nextEventOrNull<TE>(timeoutMillis = 1))
        }
    }
}