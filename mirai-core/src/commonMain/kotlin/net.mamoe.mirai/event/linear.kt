/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 挂起当前协程, 监听这个事件, 并尝试从这个事件中获取一个值.
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 *
 * @see subscribingGetAsync 本函数的异步版本
 */
@MiraiExperimentalAPI
suspend inline fun <reified E : Event, R : Any> subscribingGet(
    timeoutMillis: Long = -1,
    noinline filter: E.(E) -> R? // 不要 crossinline: crossinline 后 stacktrace 会不正常
): R {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return subscribingGetOrNull(timeoutMillis, filter) ?: error("timeout subscribingGet")
}

/**
 * 挂起当前协程, 监听这个事件, 并尝试从这个事件中获取一个值.
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 *
 * @see subscribingGetAsync 本函数的异步版本
 */
@MiraiExperimentalAPI
suspend inline fun <reified E : Event, R : Any> subscribingGetOrNull(
    timeoutMillis: Long = -1,
    noinline filter: E.(E) -> R? // 不要 crossinline: crossinline 后 stacktrace 会不正常
): R? {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    var result: R? = null
    var resultThrowable: Throwable? = null

    if (timeoutMillis == -1L) {
        @Suppress("DuplicatedCode") // for better performance
        coroutineScope {
            var listener: Listener<E>? = null
            listener = this.subscribe {
                val value = try {
                    filter.invoke(this, it)
                } catch (e: Exception) {
                    resultThrowable = e
                    return@subscribe ListeningStatus.STOPPED.also { listener!!.complete() }
                }

                if (value != null) {
                    result = value
                    return@subscribe ListeningStatus.STOPPED.also { listener!!.complete() }
                } else return@subscribe ListeningStatus.LISTENING
            }
        }
    } else {
        withTimeoutOrNull(timeoutMillis) {
            var listener: Listener<E>? = null
            @Suppress("DuplicatedCode") // for better performance
            listener = this.subscribe {
                val value = try {
                    filter.invoke(this, it)
                } catch (e: Exception) {
                    resultThrowable = e
                    return@subscribe ListeningStatus.STOPPED.also { listener!!.complete() }
                }

                if (value != null) {
                    result = value
                    return@subscribe ListeningStatus.STOPPED.also { listener!!.complete() }
                } else return@subscribe ListeningStatus.LISTENING
            }
        }
    }
    resultThrowable?.let { throw it }
    return result
}

/**
 * 异步监听这个事件, 并尝试从这个事件中获取一个值.
 *
 * 若 [filter] 抛出的异常将会被传递给 [Deferred.await] 抛出.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param coroutineContext 额外的 [CoroutineContext]
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 */
@MiraiExperimentalAPI
inline fun <reified E : Event, R : Any> CoroutineScope.subscribingGetAsync(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    timeoutMillis: Long = -1,
    noinline filter: E.(E) -> R? // 不要 crossinline: crossinline 后 stacktrace 会不正常
): Deferred<R> = this.async(coroutineContext) {
    subscribingGet(timeoutMillis, filter)
}