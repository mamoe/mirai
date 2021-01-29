/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * 返回一个 [Deferred], 其值为下一个广播并通过 [filter] 的事件 [E] 示例.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制.
 * @param filter 过滤器. 返回 `true` 时表示得到了需要的实例. 返回 `false` 时表示继续监听
 * @param coroutineContext 添加给启动的协程的 [CoroutineContext]
 *
 * @see nextEvent 同步版本
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see syncFromEvent 挂起当前协程, 并尝试从事件中同步一个值
 *
 * @throws TimeoutCancellationException 在超时后抛出.
 * @since 2.2
 */
@JvmSynthetic
@MiraiExperimentalApi
public inline fun <reified E : Event> CoroutineScope.nextEventAsync(
    timeoutMillis: Long = -1,
    priority: EventPriority = EventPriority.MONITOR,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline filter: (E) -> Boolean = { true }
): Deferred<E> {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return async(coroutineContext) {
        withTimeoutOrCoroutineScope(timeoutMillis, this) {
            nextEventImpl(E::class, this, priority, filter)
        }
    }
}


/**
 * 返回一个 [Deferred], 其值为下一个广播并通过 [filter] 的事件 [E] 示例.
 *
 * @param timeoutMillis 超时. 单位为毫秒.
 * @param filter 过滤器. 返回 `true` 时表示得到了需要的实例. 返回 `false` 时表示继续监听
 * @param coroutineContext 添加给启动的协程的 [CoroutineContext]
 *
 * @see nextEvent 同步版本
 * @see EventChannel.subscribe 普通地监听一个事件
 * @see syncFromEvent 挂起当前协程, 并尝试从事件中同步一个值
 *
 * @return 事件实例, 在超时后返回 `null`
 * @since 2.2
 */
@MiraiExperimentalApi
@JvmSynthetic
public inline fun <reified E : Event> CoroutineScope.nextEventOrNullAsync(
    timeoutMillis: Long,
    priority: EventPriority = EventPriority.MONITOR,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline filter: (E) -> Boolean = { true }
): Deferred<E?> {
    return async(coroutineContext) {
        withTimeoutOrNull(timeoutMillis) {
            nextEventImpl(E::class, this, priority, filter)
        }
    }
}