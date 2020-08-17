/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmSynthetic
import kotlin.reflect.KClass

/**
 * 挂起当前协程, 监听事件 [E], 并尝试从这个事件中**同步**一个值, 在超时时抛出 [TimeoutCancellationException]
 *
 * @param timeoutMillis 超时. 单位为毫秒.
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see asyncFromEvent 本函数的异步版本
 * @see subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 *
 * @see syncFromEventOrNull 本函数的在超时后返回 `null` 的版本
 *
 * @throws TimeoutCancellationException 在超时后抛出.
 * @throws Throwable 当 [mapper] 抛出任何异常时, 本函数会抛出该异常
 */
@JvmSynthetic
public suspend inline fun <reified E : Event, R : Any> syncFromEvent(
    timeoutMillis: Long = -1,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): R {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }

    return if (timeoutMillis == -1L) {
        coroutineScope {
            syncFromEventImpl<E, R>(E::class, this, priority, mapper)
        }
    } else {
        withTimeout(timeoutMillis) {
            syncFromEventImpl<E, R>(E::class, this, priority, mapper)
        }
    }
}

/**
 * 挂起当前协程, 监听这个事件, 并尝试从这个事件中获取一个值, 在超时时返回 `null`
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值.
 *
 * @return 超时返回 `null`, 否则返回 [mapper] 返回的第一个非 `null` 值.
 *
 * @see asyncFromEvent 本函数的异步版本
 * @see subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 *
 * @throws Throwable 当 [mapper] 抛出任何异常时, 本函数会抛出该异常
 */
@JvmSynthetic
public suspend inline fun <reified E : Event, R : Any> syncFromEventOrNull(
    timeoutMillis: Long,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): R? {
    require(timeoutMillis > 0) { "timeoutMillis must be > 0" }

    return withTimeoutOrNull(timeoutMillis) {
        syncFromEventImpl<E, R>(E::class, this, priority, mapper)
    }
}

/**
 * 异步监听这个事件, 并尝试从这个事件中获取一个值.
 *
 * 若 [mapper] 抛出的异常将会被传递给 [Deferred.await] 抛出.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param coroutineContext 额外的 [CoroutineContext]
 * @param mapper 过滤转换器. 返回非 `null` 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see syncFromEvent
 * @see asyncFromEvent
 * @see subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 */
@JvmSynthetic
@Suppress("DeferredIsResult")
public inline fun <reified E : Event, R : Any> CoroutineScope.asyncFromEventOrNull(
    timeoutMillis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): Deferred<R?> {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return this.async(coroutineContext) {
        syncFromEventOrNull(timeoutMillis, priority, mapper)
    }
}


/**
 * 异步监听这个事件, 并尝试从这个事件中获取一个值.
 *
 * 若 [mapper] 抛出的异常将会被传递给 [Deferred.await] 抛出.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param coroutineContext 额外的 [CoroutineContext]
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see syncFromEvent
 * @see asyncFromEventOrNull
 * @see subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 */
@JvmSynthetic
@Suppress("DeferredIsResult")
public inline fun <reified E : Event, R : Any> CoroutineScope.asyncFromEvent(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): Deferred<R> {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return this.async(coroutineContext) {
        syncFromEvent(timeoutMillis, priority, mapper)
    }
}


//////////////
//// internal
//////////////

@JvmSynthetic
@PublishedApi
internal suspend inline fun <E : Event, R> syncFromEventImpl(
    eventClass: KClass<E>,
    coroutineScope: CoroutineScope,
    priority: Listener.EventPriority,
    crossinline mapper: suspend E.(E) -> R?
): R = suspendCancellableCoroutine { cont ->
    coroutineScope.subscribe(eventClass, priority = priority) {
        try {
            cont.resumeWith(kotlin.runCatching {
                mapper.invoke(this, it) ?: return@subscribe ListeningStatus.LISTENING
            })
        } catch (e: Exception) {
        }
        return@subscribe ListeningStatus.STOPPED
    }
}