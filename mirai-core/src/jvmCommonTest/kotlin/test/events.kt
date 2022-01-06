/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.utils.cast
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal inline fun <reified T : Event, R> assertEventBroadcasts(times: Int = 1, block: () -> R): R {
    assertEventBroadcasts<T>(times) {
        return block()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal inline fun <reified T : Event> assertEventBroadcasts(times: Int = 1, block: () -> Unit): List<T> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    val receivedEvents = ConcurrentLinkedQueue<Event>()
    val listener = GlobalEventChannel.subscribeAlways<Event> { event ->
        receivedEvents.add(event)
    }

    try {
        block()
    } finally {
        listener.complete()
    }

    if (times < 0) return receivedEvents.filterIsInstance<T>().cast()

    val actual = receivedEvents.filterIsInstance<T>().count()
    assertEquals(
        times,
        actual,
        "Expected event ${T::class.simpleName} broadcast $times time(s). " +
                "But actual count is ${actual}. " +
                "\nAll received events: ${receivedEvents.joinToString(", ", "[", "]")}"
    )

    return receivedEvents.filterIsInstance<T>().cast()
}

@OptIn(ExperimentalCoroutinesApi::class)
internal inline fun <R> assertEventNotBroadcast(block: () -> R): R {
    return assertEventBroadcasts<Event, R>(0, block)
}