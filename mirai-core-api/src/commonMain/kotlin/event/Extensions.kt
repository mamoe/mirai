/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlin.coroutines.resume
import kotlin.reflect.KClass

/**
 * 挂起当前协程, 直到监听到事件 [E] 的广播并通过 [filter], 返回这个事件实例.
 *
 * 本函数是 [EventChannel.subscribe] 的衍生工具函数, 内部会调用 [EventChannel.subscribe].
 *
 * ## 挂起可取消
 *
 * 本函数的挂起过程可以被[取消][CancellableContinuation.cancel]. 这意味着若在 [CoroutineScope.launch] 中使用本函数, 则 [launch] 启动的 [Job] 可以通过 [Job.cancel] 取消 (停止), 届时本函数会抛出 [CancellationException].
 *
 * ## 异常处理
 *
 * [filter] 抛出的异常属于监听方异常, 将会由 [nextEvent] 原样重新抛出.
 *
 * ## 使用 [Flow] 的替代方法
 *
 * 在 Kotlin 可使用 [EventChannel.asFlow] 配合 [Flow.filter] 和 [Flow.first] 实现与 [nextEvent] 相似的功能 (注意, Flow 方法将会使用 [EventPriority.MONITOR] 优先级).
 *
 * 示例:
 *
 * ```
 * val event: GroupMessageEvent = GlobalEventChannel.asFlow().filterIsInstance<GroupMessageEvent>().filter { it.sender.id == 123456 }.first()
 * // 上下两行代码等价
 * val event: GroupMessageEvent = GlobalEventChannel.filterIsInstance<GroupMessageEvent>().nextEvent(EventPriority.MONITOR) { it.sender.id == 123456 }
 * ```
 *
 * 由于 [Flow] 拥有更多操作 (如 [Flow.firstOrNull]), 在不需要指定[事件优先级][EventPriority]时使用 [Flow] 拥有更高自由度.
 *
 * @param filter 过滤器. 返回 `true` 时表示得到了需要的实例. 返回 `false` 时表示继续监听
 *
 * @since 2.10
 */
public suspend inline fun <reified E : Event> EventChannel<*>.nextEvent(
    priority: EventPriority = EventPriority.NORMAL,
    noinline filter: suspend (E) -> Boolean = { true }
): E = nextEvent(priority, false, filter)

/**
 * 挂起当前协程, 直到监听到事件 [E] 的广播并通过 [filter], 返回这个事件实例.
 *
 * 本函数是 [EventChannel.subscribe] 的衍生工具函数, 内部会调用 [EventChannel.subscribe].
 *
 * ## 挂起可取消
 *
 * 本函数的挂起过程可以被[取消][CancellableContinuation.cancel]. 这意味着若在 [CoroutineScope.launch] 中使用本函数, 则 [launch] 启动的 [Job] 可以通过 [Job.cancel] 取消 (停止), 届时本函数会抛出 [CancellationException].
 *
 * ## 异常处理
 *
 * [filter] 抛出的异常属于监听方异常, 将会由 [nextEvent] 原样重新抛出.
 *
 * ## 使用 [Flow] 的替代方法
 *
 * 在 Kotlin 可使用 [EventChannel.asFlow] 配合 [Flow.filter] 和 [Flow.first] 实现与 [nextEvent] 相似的功能 (注意, Flow 方法将会使用 [EventPriority.MONITOR] 优先级).
 *
 * 示例:
 *
 * ```
 * val event: GroupMessageEvent = GlobalEventChannel.asFlow().filterIsInstance<GroupMessageEvent>().filter { it.sender.id == 123456 }.first()
 * // 上下两行代码等价
 * val event: GroupMessageEvent = GlobalEventChannel.filterIsInstance<GroupMessageEvent>().nextEvent(EventPriority.MONITOR) { it.sender.id == 123456 }
 * ```
 *
 * 由于 [Flow] 拥有更多操作 (如 [Flow.firstOrNull]), 在不需要指定[事件优先级][EventPriority]时使用 [Flow] 拥有更高自由度.
 *
 * @param intercept 是否拦截, 传入 `true` 时表示拦截此事件不让接下来的监听器处理, 传入 `false` 时表示让接下来的监听器处理
 * @param filter 过滤器. 返回 `true` 时表示得到了需要的实例. 返回 `false` 时表示继续监听
 *
 * @since 2.13
 */
public suspend inline fun <reified E : Event> EventChannel<*>.nextEvent(
    priority: EventPriority = EventPriority.NORMAL,
    intercept: Boolean = false,
    noinline filter: suspend (E) -> Boolean = { true }
): E = coroutineScope {
    suspendCancellableCoroutine { cont ->
        var listener: Listener<E>? = null
        listener = this@nextEvent.parentScope(this@coroutineScope).subscribe(E::class, priority = priority) { event ->
            val result = kotlin.runCatching {
                if (!filter(event)) return@subscribe ListeningStatus.LISTENING
                event
            }

            try {
                cont.resumeWith(result.apply { onSuccess { if (intercept) intercept() } })
            } finally {
                listener?.complete() // ensure completed on exceptions
            }
            return@subscribe ListeningStatus.STOPPED
        }

        cont.invokeOnCancellation {
            kotlin.runCatching { listener.cancel("nextEvent outer scope cancelled", it) }
        }
    }
}

/**
 * 挂起当前协程, 监听事件 [E], 并尝试从这个事件中**获取**一个值, 在超时时抛出 [TimeoutCancellationException]
 *
 * 本函数是 [EventChannel.subscribe] 的衍生工具函数, 内部会调用 [EventChannel.subscribe].
 *
 * ## 挂起可取消
 *
 * 本函数的挂起过程可以被[取消][CancellableContinuation.cancel]. 这意味着若在 [CoroutineScope.launch] 中使用本函数, 则 [launch] 启动的 [Job] 可以通过 [Job.cancel] 取消 (停止), 届时本函数会抛出 [CancellationException].
 *
 * ## 异常处理
 *
 * [filter] 抛出的异常属于监听方异常, 将会由 [nextEvent] 原样重新抛出.
 *
 * ## 使用 [Flow] 的替代方法
 *
 * 在 Kotlin 可使用 [EventChannel.asFlow] 配合 [Flow.filter] 和 [Flow.first] 实现与 [nextEvent] 相似的功能 (注意, Flow 方法将会使用 [EventPriority.MONITOR] 优先级).
 *
 * 示例:
 *
 * ```
 * val senderId: Long = GlobalEventChannel.asFlow()
 *     .filterIsInstance<GroupMessageEvent>()
 *     .filter { it.sender.id == 123456L }
 *     .map { it.sender.id }.first()
 *
 * // 上下代码等价
 *
 * val senderId: Long = GlobalEventChannel
 *     .filterIsInstance<GroupMessageEvent>()
 *     .syncFromEvent(EventPriority.MONITOR) { if (it.sender.id = 123456) it.sender.name else null }
 * ```
 *
 * 由于 [Flow] 拥有更多操作且逻辑更清晰, 在不需要指定[事件优先级][EventPriority]时更推荐使用 [Flow].
 *
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see nextEvent 挂起当前协程, 并获取下一个事件实例
 *
 * @throws Throwable 当 [mapper] 抛出任何异常时, 本函数会抛出该异常
 *
 * @since 2.10
 */
public suspend inline fun <reified E : Event, R : Any> EventChannel<*>.syncFromEvent(
    priority: EventPriority = EventPriority.NORMAL,
    noinline mapper: suspend (E) -> R?
): R = coroutineScope {
    suspendCancellableCoroutine { cont ->
        var listener: Listener<E>? = null
        listener = this@syncFromEvent.parentScope(this).subscribe(E::class, priority = priority) { event ->
            val result = kotlin.runCatching {
                mapper(event) ?: return@subscribe ListeningStatus.LISTENING
            }

            try {
                cont.resumeWith(result)
            } finally {
                listener?.complete() // ensure completed on exceptions
            }
            return@subscribe ListeningStatus.STOPPED
        }

        cont.invokeOnCancellation {
            kotlin.runCatching { listener.cancel("syncFromEvent outer scope cancelled", it) }
        }
    }
}

// Can't move to JVM, filename clashes

/**
 * @since 2.10
 */
@PublishedApi
@Deprecated("For binary compatibility")
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
@Deprecated("For binary compatibility")
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
