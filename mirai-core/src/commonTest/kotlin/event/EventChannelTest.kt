/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.FriendEvent
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.test.*

internal class EventChannelTest : AbstractEventTest() {
    suspend fun suspendCall() {
        coroutineContext
    }

    data class TE(
        val x: Int,
        val y: Int = 1,
    ) : AbstractEvent()

    @Test
    fun singleFilter() {
        runBlocking {
            val received = suspendCancellableCoroutine { cont ->
                globalEventChannel()
                    .filterIsInstance<TE>()
                    .filter {
                        true
                    }
                    .filter {
                        it.x == 2
                    }
                    .filter {
                        true
                    }
                    .subscribeOnce<TE> {
                        cont.resume(it.x)
                    }

                launch {
                    println("Broadcast 1")
                    TE(1).broadcast()
                    println("Broadcast 2")
                    TE(2).broadcast()
                    println("Broadcast done")
                }
            }

            assertEquals(2, received)
        }
    }

    @Test
    fun multipleFilters() {
        runBlocking {
            val received = suspendCancellableCoroutine { cont ->
                globalEventChannel()
                    .filterIsInstance<TE>()
                    .filter {
                        true
                    }
                    .filter {
                        it.x == 2
                    }
                    .filter {
                        it.y == 2
                    }
                    .filter {
                        true
                    }
                    .subscribeOnce<TE> {
                        cont.resume(it.x)
                    }

                launch {
                    println("Broadcast 1")
                    TE(1, 1).broadcast()
                    println("Broadcast 2")
                    TE(2, 1).broadcast()
                    println("Broadcast 2")
                    TE(2, 3).broadcast()
                    println("Broadcast 2")
                    TE(2, 2).broadcast()
                    println("Broadcast done")
                }
            }

            assertEquals(2, received)
        }
    }

    @Test
    fun multipleContexts1() {
        runBlocking {
            withContext(CoroutineName("1")) {
                val received = suspendCancellableCoroutine { cont ->
                    globalEventChannel()
                        .context(CoroutineName("2"))
                        .context(CoroutineName("3"))
                        .subscribeOnce<TE>(CoroutineName("4")) {
                            assertEquals("4", currentCoroutineContext()[CoroutineName]!!.name)
                            cont.resume(it.x)
                        }

                    launch {
                        TE(2, 2).broadcast()
                    }
                }

                assertEquals(2, received)
            }
        }
    }

    @Test
    fun multipleContexts2() {
        runBlocking {
            withContext(CoroutineName("1")) {
                val received = suspendCancellableCoroutine { cont ->
                    globalEventChannel()
                        .context(CoroutineName("2"))
                        .context(CoroutineName("3"))
                        .subscribeOnce<TE> {
                            assertEquals("3", currentCoroutineContext()[CoroutineName]!!.name)
                            cont.resume(it.x)
                        }

                    launch {
                        TE(2, 2).broadcast()
                    }
                }

                assertEquals(2, received)
            }
        }
    }


    @Test
    fun multipleContexts3() {
        runBlocking {
            withContext(CoroutineName("1")) {
                val received = suspendCancellableCoroutine { cont ->
                    globalEventChannel()
                        .context(CoroutineName("2"))
                        .subscribeOnce<TE> {
                            assertEquals("2", currentCoroutineContext()[CoroutineName]!!.name)
                            cont.resume(it.x)
                        }

                    launch {
                        TE(2, 2).broadcast()
                    }
                }

                assertEquals(2, received)
            }
        }
    }

    @Test
    fun multipleContexts4() {
        runBlocking {
            withContext(CoroutineName("1")) {
                val received = suspendCancellableCoroutine { cont ->
                    globalEventChannel()
                        .subscribeOnce<TE> {
                            assertEquals("1", currentCoroutineContext()[CoroutineName]!!.name)
                            cont.resume(it.x)
                        }

                    launch {
                        TE(2, 2).broadcast()
                    }
                }

                assertEquals(2, received)
            }
        }
    }

    @Test
    fun `test forwardToChannel`() {
        runBlocking {
            val channel = Channel<TE>(Channel.BUFFERED)
            val listener = globalEventChannel()
                .filterIsInstance<TE>()
                .filter { true }
                .filter { it.x == 2 }
                .filter { true }
                .forwardToChannel(channel)

            TE(1).broadcast()
            TE(2).broadcast()
            listener.complete()
            TE(2).broadcast()

            channel.close()

            val list = channel.receiveAsFlow().toList()

            assertEquals(1, list.size)
            assertEquals(TE(2), list.single())
        }
    }

    @Test
    fun `test forwardToChannel listener completes if channel closed`() {
        runBlocking {
            val channel = Channel<TE>(Channel.BUFFERED)
            val listener = globalEventChannel()
                .filterIsInstance<TE>()
                .filter { true }
                .filter { it.x == 2 }
                .filter { true }
                .forwardToChannel(channel)

            TE(1).broadcast()
            TE(2).broadcast()
            channel.close()
            assertTrue { listener.isActive }
            TE(2).broadcast()
            assertTrue { listener.isCompleted }
            assertFalse { listener.isActive }

            val list = channel.receiveAsFlow().toList()

            assertEquals(1, list.size)
            assertEquals(TE(2), list.single())
        }
    }

    @Test
    fun testExceptionInFilter() {
        assertFailsWith<ExceptionInEventChannelFilterException> {
            runBlocking {
                @Suppress("RemoveExplicitTypeArguments")
                suspendCancellableCoroutine<Int> { cont ->
                    globalEventChannel()
                        .exceptionHandler {
                            cont.resumeWithException(it)
                        }
                        .filter {
                            error("test error")
                        }
                        .subscribeOnce<TE> {
                            cont.resume(it.x)
                        }

                    launch {
                        println("Broadcast 1")
                        TE(1).broadcast()
                        println("Broadcast done")
                    }
                }
            }
        }.run {
            assertEquals("test error", cause.message)
        }
    }

    @Test
    fun testExceptionInSubscribe() {
        runBlocking {
            assertFailsWith<IllegalStateException> {
                suspendCancellableCoroutine<Int> { cont ->
                    val handler = CoroutineExceptionHandler { _, throwable ->
                        cont.resumeWithException(throwable)
                    }

                    val listener = globalEventChannel()
                        .context(handler)
                        .subscribeOnce<TE> {
                            assertSame(handler, currentCoroutineContext()[CoroutineExceptionHandler])
                            error("test error")
                        }

                    launch {
                        println("Broadcast 1")
                        TE(1).broadcast()
                        println("Broadcast done")
                        listener.complete()
                    }
                }
            }.run {
                assertEquals("test error", message)
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testVariance() {
        var global: EventChannel<Event> = GlobalEventChannel
        val a: EventChannel<MessageEvent> = global.filterIsInstance()

        val filterLambda: (ev: MessageEvent) -> Boolean = { true }

        // Kotlin can't resolve to the non-suspend one
        a.filter {
            // it: Event
            suspendCall() // would be allowed in Kotlin
            it.isIntercepted
        }

        val messageEventChannel = a.filterIsInstance<MessageEvent>()
        // group.asChannel<GroupMessageEvent>()

        val listener: Listener<GroupMessageEvent> = messageEventChannel.subscribeAlways<GroupEvent> {

        }

        global = a

        global.subscribeMessages {

        }

        messageEventChannel.subscribeMessages {

        }

        global.subscribeAlways<FriendEvent> {

        }

        // inappliable: out cannot passed as in
        // val b: EventChannel<in FriendMessageEvent> = global.filterIsInstance<FriendMessageEvent>()
    }
}