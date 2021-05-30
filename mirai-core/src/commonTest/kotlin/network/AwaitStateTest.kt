/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.selects.whileSelect
import net.mamoe.mirai.internal.network.framework.AbstractMockNetworkHandlerTest
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.network.handler.awaitState
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AwaitStateTest : AbstractMockNetworkHandlerTest() {

    @Test
    fun `test select onStateChanged`() = runBlockingUnit {
        createNetworkHandler().run {
            assertState(INITIALIZED)
            val queue = ConcurrentLinkedQueue<NetworkHandler.State>()
            launch(start = CoroutineStart.UNDISPATCHED) {
                select<Unit> {
                    onStateChanged {
                        queue.add(it)
                    }
                }
                assertEquals(1, queue.size)
                assertEquals(OK, queue.first())
            }
            launch {
                setState(OK)
            }

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test whileSelect onStateChanged on demand`() = runBlockingUnit(singleThreadDispatcher) {
        createNetworkHandler().run {
            assertState(INITIALIZED)
            val queue = ConcurrentLinkedQueue<NetworkHandler.State>()
            val selector = launch(
                CoroutineExceptionHandler { _, throwable ->
                    if (throwable !is CancellationException) throwable.printStackTrace()
                },
                start = CoroutineStart.UNDISPATCHED
            ) {
                while (isActive) {
                    whileSelect {
                        onStateChanged {
                            queue.add(it)
                            isActive
                        }
                    }
                }
                throw AssertionError("Terminated too early")
            }
            launch {
                setState(OK)
                yield()
                setState(CLOSED)
                yield()
                selector.cancel()
            }

            runCatching { selector.join() }

            assertEquals(2, queue.size)
            assertEquals(OK, queue.first())
            assertEquals(CLOSED, queue.drop(1).first())
        }
    }

    // single thread so we can use [yield] to transfer dispatch
    private val singleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test whileSelect onStateChanged drop if not listening`() = runBlockingUnit(singleThreadDispatcher) {
        createNetworkHandler().run {
            assertState(INITIALIZED)
            val queue = ConcurrentLinkedQueue<NetworkHandler.State>()

            setState(CONNECTING)
            setState(LOADING)

            val selector = launch(
                CoroutineExceptionHandler { _, throwable ->
                    if (throwable !is CancellationException) throwable.printStackTrace()
                },
                start = CoroutineStart.UNDISPATCHED
            ) {
                while (isActive) {
                    whileSelect {
                        onStateChanged {
                            queue.add(it)
                            true
                        }
                    }
                }
            }

            launch {
                setState(OK)
                yield() // yields the thread to run coroutine of select
                setState(CLOSED)
                yield()
                selector.cancel()
            }

            runCatching { selector.join() }

            assertEquals(OK, queue.first(), queue.toString())
            assertEquals(2, queue.size)
            assertEquals(CLOSED, queue.drop(1).first())
        }
    }

    @Test
    fun `can await`() = runBlockingUnit(singleThreadDispatcher) {
        createNetworkHandler().run {
            assertState(INITIALIZED)
            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                awaitState(CLOSED)
                assertState(CLOSED)
            }
            yield()
            assertTrue { job.isActive }

            setState(OK)
            yield()
            assertTrue { job.isActive }

            setState(CLOSED)
            yield()
            assertFalse { job.isActive }
        }
    }
}