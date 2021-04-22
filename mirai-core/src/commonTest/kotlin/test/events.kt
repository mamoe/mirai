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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal inline fun <reified T : Event, R> assertEventBroadcasts(times: Int = 1, block: () -> R): R {
    val receivedEvents = AtomicInteger(0)
    val listener = GlobalEventChannel.subscribeAlways<T> {
        receivedEvents.incrementAndGet()
    }
    try {
        return block()
    } finally {
        listener.complete()
        assertEquals(
            times,
            receivedEvents.get(),
            "Expected event ${T::class.simpleName} broadcast $times time(s). But actual is ${receivedEvents.get()}."
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal inline fun <reified T : Event> assertEventBroadcasts(times: Int = 1, block: () -> Unit) {
    val receivedEvents = AtomicInteger(0)
    val listener = GlobalEventChannel.subscribeAlways<T> {
        receivedEvents.incrementAndGet()
    }
    try {
        return block()
    } finally {
        listener.complete()
        assertEquals(
            times,
            receivedEvents.get(),
            "Expected event ${T::class.simpleName} broadcast $times time(s). But actual is ${receivedEvents.get()}."
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal inline fun <reified T : Event, R> assertEventNotBroadcast(block: () -> R): R {
    return assertEventBroadcasts<T, R>(0, block)
}