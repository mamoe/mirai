/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")
@file:JvmName("SyncFromEventKt")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

/**
 * 挂起当前协程, 监听事件 [E], 并尝试从这个事件中**获取**一个值, 在超时时抛出 [TimeoutCancellationException]
 *
 * @param timeoutMillis 超时. 单位为毫秒.
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see asyncFromEvent 本函数的异步版本
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 *
 * @see syncFromEventOrNull 本函数的在超时后返回 `null` 的版本
 *
 * @throws TimeoutCancellationException 在超时后抛出.
 * @throws Throwable 当 [mapper] 抛出任何异常时, 本函数会抛出该异常
 */
@DeprecatedSinceMirai(warningSince = "2.10", errorSince = "2.12", hiddenSince = "2.13")
@Deprecated(
    "Use GlobalEventChannel.syncFromEvent",
    ReplaceWith(
        "if (timeoutMillis == -1L) { GlobalEventChannel.syncFromEvent<E, R>(priority) { mapper.invoke(it, it) } } else { withTimeout(timeoutMillis) { GlobalEventChannel.syncFromEvent<E, R>(priority) { mapper.invoke(it, it) } } }",
        "kotlinx.coroutines.withTimeout",
        "net.mamoe.mirai.event.GlobalEventChannel",
        "net.mamoe.mirai.event.syncFromEvent"
    ),
    level = DeprecationLevel.HIDDEN
)
@JvmSynthetic
public suspend inline fun <reified E : Event, R : Any> syncFromEvent(
    timeoutMillis: Long = -1,
    priority: EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): R {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }

    @Suppress("DEPRECATION")
    return if (timeoutMillis == -1L) {
        coroutineScope {
            GlobalEventChannel.syncFromEventImpl(E::class, this, priority) { mapper.invoke(it, it) }
        }
    } else {
        withTimeout(timeoutMillis) {
            GlobalEventChannel.syncFromEventImpl(E::class, this, priority) { mapper.invoke(it, it) }
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
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 *
 * @throws Throwable 当 [mapper] 抛出任何异常时, 本函数会抛出该异常
 */
@JvmSynthetic
@DeprecatedSinceMirai(warningSince = "2.10", errorSince = "2.12", hiddenSince = "2.13")
@Deprecated(
    "Use GlobalEventChannel.syncFromEvent",
    ReplaceWith("withTimeoutOrNull(timeoutMillis) { GlobalEventChannel.syncFromEvent<E, R>(priority) { event -> with(event) { mapper(event) } }"),
    level = DeprecationLevel.HIDDEN
)
public suspend inline fun <reified E : Event, R : Any> syncFromEventOrNull(
    timeoutMillis: Long,
    priority: EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): R? {
    require(timeoutMillis > 0) { "timeoutMillis must be > 0" }

    return withTimeoutOrNull(timeoutMillis) {
        @Suppress("DEPRECATION")
        GlobalEventChannel.syncFromEventImpl(E::class, this, priority) { mapper.invoke(it, it) }
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
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 */
@Deprecated(
    "Please use `async` and `syncFromEvent` manually.",
    replaceWith = ReplaceWith(
        """async(coroutineContext) {
        withTimeoutOrNull(timeoutMillis) {
            GlobalEventChannel.syncFromEvent<E, R>(priority, filter)
        }
    }""",
        "kotlinx.coroutines.async",
        "kotlinx.coroutines.withTimeoutOrNull",
        "net.mamoe.mirai.event.globalEventChannel",
        "net.mamoe.mirai.event.syncFromEvent"
    ),
    level = DeprecationLevel.HIDDEN
)
@JvmSynthetic
@DeprecatedSinceMirai(warningSince = "2.10", errorSince = "2.12", hiddenSince = "2.13")
@Suppress("DeferredIsResult")
public inline fun <reified E : Event, R : Any> CoroutineScope.asyncFromEventOrNull(
    timeoutMillis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): Deferred<R?> {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return this.async(coroutineContext) {
        withTimeoutOrNull(timeoutMillis) {
            GlobalEventChannel.syncFromEvent<E, R>(priority) { event ->
                mapper(event, event)
            }
        }
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
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 */
@Deprecated(
    "Please use `async` and `syncFromEvent` manually.",
    replaceWith = ReplaceWith(
        """async(coroutineContext) {
        if (timeoutMillis == -1L) {
            this.globalEventChannel(coroutineContext).syncFromEvent<E, R>(priority, filter)
        } else {
            withTimeout(timeoutMillis) {
                GlobalEventChannel.syncFromEvent<E, R>(priority, filter)
            }
        }
    }""",
        "kotlinx.coroutines.async",
        "kotlinx.coroutines.withTimeout",
        "net.mamoe.mirai.event.globalEventChannel",
        "net.mamoe.mirai.event.syncFromEvent"
    ),
    level = DeprecationLevel.HIDDEN
)
@DeprecatedSinceMirai(warningSince = "2.10", errorSince = "2.12", hiddenSince = "2.13")
@JvmSynthetic
@Suppress("DeferredIsResult")
public inline fun <reified E : Event, R : Any> CoroutineScope.asyncFromEvent(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: EventPriority = EventPriority.MONITOR,
    crossinline mapper: suspend E.(E) -> R?
): Deferred<R> {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return this.async(coroutineContext) {
        if (timeoutMillis == -1L) {
            GlobalEventChannel.syncFromEvent(priority) { it: E -> mapper.invoke(it, it) }
        } else {
            withTimeout(timeoutMillis) { GlobalEventChannel.syncFromEvent(priority) { it: E -> mapper.invoke(it, it) } }
        }
    }
}


//////////////
//// internal
//////////////

@Deprecated("Deprecated since its usages are deprecated", level = DeprecationLevel.HIDDEN)
@DeprecatedSinceMirai(warningSince = "2.10", hiddenSince = "2.14")
@JvmSynthetic
@PublishedApi
internal suspend inline fun <E : Event, R> syncFromEventImpl(
    eventClass: KClass<E>,
    coroutineScope: CoroutineScope,
    priority: EventPriority,
    crossinline mapper: suspend E.(E) -> R?
): R = suspendCancellableCoroutine { cont ->
    coroutineScope.globalEventChannel().subscribe(eventClass, priority = priority) {
        try {
            cont.resumeWith(kotlin.runCatching {
                mapper.invoke(this, it) ?: return@subscribe ListeningStatus.LISTENING
            })
        } catch (_: Exception) {
        }
        return@subscribe ListeningStatus.STOPPED
    }
}