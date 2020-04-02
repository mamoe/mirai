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
import net.mamoe.mirai.utils.SinceMirai
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 挂起当前协程, 监听这个事件, 并尝试从这个事件中获取一个值.
 *
 * 若 [mapper] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 *
 * @see subscribingGetAsync 本函数的异步版本
 */
@SinceMirai("0.29.0")
@MiraiExperimentalAPI
suspend inline fun <reified E : Event, R : Any> subscribingGet(
    timeoutMillis: Long = -1,
    noinline mapper: suspend E.(E) -> R? // 不要 crossinline: crossinline 后 stacktrace 会不正常
): R {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return subscribingGetOrNull(timeoutMillis, mapper) ?: error("timeout subscribingGet")
}

/**
 * 挂起当前协程, 监听这个事件, 并尝试从这个事件中获取一个值.
 *
 * 若 [mapper] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 *
 * @see subscribingGetAsync 本函数的异步版本
 */
@SinceMirai("0.29.0")
@MiraiExperimentalAPI
suspend inline fun <reified E : Event, R : Any> subscribingGetOrNull(
    timeoutMillis: Long = -1,
    noinline mapper: suspend E.(E) -> R? // 不要 crossinline: crossinline 后 stacktrace 会不正常
): R? {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }

    return if (timeoutMillis == -1L) {
        coroutineScope {
            subscribingGetOrNullImpl<E, R>(this, mapper)
        }
    } else {
        withTimeoutOrNull(timeoutMillis) {
            subscribingGetOrNullImpl<E, R>(this, mapper)
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
 * @param mapper 过滤转换器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 */
@SinceMirai("0.29.0")
@MiraiExperimentalAPI
inline fun <reified E : Event, R : Any> CoroutineScope.subscribingGetAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    noinline mapper: suspend E.(E) -> R? // 不要 crossinline: crossinline 后 stacktrace 会不正常
): Deferred<R> = this.async(coroutineContext) {
    subscribingGet(timeoutMillis, mapper)
}


//////////////
//// internal
//////////////


@PublishedApi
internal suspend inline fun <reified E : Event, R> subscribingGetOrNullImpl(
    coroutineScope: CoroutineScope,
    noinline mapper: suspend E.(E) -> R?
): R {
    var result: Result<R?> = Result.success(null) // stub
    var listener: Listener<E>? = null
    @Suppress("DuplicatedCode") // for better performance
    listener = coroutineScope.subscribe {
        val value = try {
            mapper.invoke(this, it)
        } catch (e: Exception) {
            result = Result.failure(e)
            listener!!.complete()
            return@subscribe ListeningStatus.STOPPED
        }

        if (value != null) {
            result = Result.success(value)
            listener!!.complete()
            return@subscribe ListeningStatus.STOPPED
        } else return@subscribe ListeningStatus.LISTENING
    }
    listener.join()

    return result.getOrThrow()!!
}