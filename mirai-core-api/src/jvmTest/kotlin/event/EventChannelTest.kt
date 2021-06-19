/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import net.mamoe.mirai.event.events.FriendEvent
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.internal.event.GlobalEventListeners
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class EventChannelTest : AbstractEventTest() {
    suspend fun suspendCall() {

    }

    data class TE(
        val x: Int
    ) : AbstractEvent()

    val semaphore = Semaphore(1)

    @BeforeEach
    fun x() {
        runBlocking { semaphore.acquire() }
        _EventBroadcast.implementation = object : _EventBroadcast() {
            override suspend fun <E : Event> broadcastPublic(event: E): E =
                broadcastImpl(event) // do not call MiraiImpl
        }
    }

    @AfterEach
    fun s() {
        GlobalEventListeners.clear()
        runBlocking { semaphore.release() }
        _EventBroadcast.implementation = _EventBroadcast() // restore
    }

    @Test
    fun testFilter() {
        runBlocking {
            val received = suspendCoroutine<Int> { cont ->
                GlobalEventChannel
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAsChannel() {
        runBlocking {
            val channel = GlobalEventChannel
                .filterIsInstance<TE>()
                .filter { true }
                .filter { it.x == 2 }
                .filter { true }
                .asChannel(Channel.BUFFERED)

            println("Broadcast 1")
            TE(1).broadcast()
            println("Broadcast 2")
            TE(2).broadcast()
            println("Broadcast done")
            channel.close()

            val list = channel.receiveAsFlow().toList()

            assertEquals(1, list.size)
            assertEquals(TE(2), list.single())
        }
    }

    @Test
    fun testExceptionInFilter() {
        runBlocking {
            assertFailsWith<ExceptionInEventChannelFilterException> {
                suspendCoroutine<Int> { cont ->
                    GlobalEventChannel
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
            }.run {
                assertEquals("test error", cause.message)
            }
        }
    }

    @Test
    fun testExceptionInSubscribe() {
        runBlocking {
            assertFailsWith<IllegalStateException> {
                suspendCoroutine<Int> { cont ->
                    GlobalEventChannel
                        .exceptionHandler {
                            cont.resumeWithException(it)
                        }
                        .subscribeOnce<TE> {
                            error("test error")
                        }

                    launch {
                        println("Broadcast 1")
                        TE(1).broadcast()
                        println("Broadcast done")
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
        var a: EventChannel<MessageEvent> = global.filterIsInstance<MessageEvent>()

        val filterLambda: (ev: MessageEvent) -> Boolean = { true }

        // Kotlin can't resolve to the non-suspend one
        a.filter {
            // it: Event
            suspendCall() // would be allowed in Kotlin
            it.isIntercepted
        }

        val messageEventChannel = a.filterIsInstance<MessageEvent>()
        // group.asChannel<GroupMessageEvent>()

        val listener: Listener<GroupMessageEvent> = messageEventChannel.subscribeAlways<GroupEvent>() {

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