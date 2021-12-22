/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.reflect.KClass


/**
 * 挂起当前协程, 直到监听到事件 [E] 的广播并通过 [filter], 返回这个事件实例.
 *
 * @param filter 过滤器. 返回 `true` 时表示得到了需要的实例. 返回 `false` 时表示继续监听
 *
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see EventChannel.syncFromEvent 挂起当前协程, 并尝试从事件中同步一个值
 *
 * @since 2.10
 */
public suspend inline fun <reified E : Event> EventChannel<*>.nextEvent(
    priority: EventPriority = EventPriority.NORMAL,
    noinline filter: suspend (E) -> Boolean = { true }
): E = coroutineScope { this@nextEvent.nextEventImpl(E::class, this@coroutineScope, priority, filter) }

/**
 * 挂起当前协程, 监听事件 [E], 并尝试从这个事件中**获取**一个值, 在超时时抛出 [TimeoutCancellationException]
 *
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see asyncFromEvent 本函数的异步版本
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 *
 * @see syncFromEventOrNull 本函数的在超时后返回 `null` 的版本
 *
 * @throws Throwable 当 [mapper] 抛出任何异常时, 本函数会抛出该异常
 *
 * @since 2.10
 */
public suspend inline fun <reified E : Event, R : Any> EventChannel<*>.syncFromEvent(
    priority: EventPriority = EventPriority.NORMAL,
    noinline mapper: suspend (E) -> R?
): R = coroutineScope { this@syncFromEvent.syncFromEventImpl(E::class, this, priority, mapper) }


/**
 * @since 2.10
 */
@PublishedApi
internal suspend fun <E : Event> EventChannel<Event>.nextEventImpl(
    eventClass: KClass<E>,
    coroutineScope: CoroutineScope,
    priority: EventPriority,
    filter: suspend (E) -> Boolean
): E = suspendCancellableCoroutine { cont ->
    var listener: Listener<E>? = null
    listener = parentScope(coroutineScope)
        .subscribe(eventClass, priority = priority) { event ->
            if (!filter(event)) return@subscribe ListeningStatus.LISTENING

            try {
                cont.resume(event)
            } finally {
                listener?.complete() // ensure completed on exceptions
            }
            return@subscribe ListeningStatus.STOPPED
        }

    cont.invokeOnCancellation {
        runCatching { listener.cancel("nextEvent outer scope cancelled", it) }
    }
}

/**
 * @since 2.10
 */
@PublishedApi
internal suspend fun <E : Event, R> EventChannel<*>.syncFromEventImpl(
    eventClass: KClass<E>,
    coroutineScope: CoroutineScope,
    priority: EventPriority,
    mapper: suspend (E) -> R?
): R = suspendCancellableCoroutine { cont ->
    parentScope(coroutineScope).subscribe(eventClass, priority = priority) { event ->
        try {
            cont.resumeWith(kotlin.runCatching {
                mapper.invoke(event) ?: return@subscribe ListeningStatus.LISTENING
            })
        } catch (_: Exception) {
        }
        return@subscribe ListeningStatus.STOPPED
    }
}
