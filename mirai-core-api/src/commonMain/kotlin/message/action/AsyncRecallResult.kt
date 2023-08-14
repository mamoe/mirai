/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.message.action

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asCompletableFuture
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import java.util.concurrent.CompletableFuture

/**
 * 异步撤回结果.
 *
 * 可由 [MessageSource.recallIn] 返回得到.
 *
 * ## Kotlin 用法示例
 *
 * ### 获取撤回失败时的异常
 *
 * ```
 * val exception = result.exception.await() // 挂起协程并等待撤回的结果.
 * if (exception == null) {
 *   // 撤回成功
 * } else {
 *   // 撤回失败
 * }
 * ```
 *
 * 若仅需要了解撤回是否成功而不需要获取详细异常实例, 可使用 [isSuccess]
 *
 * ## Java 用法示例
 *
 * ```java
 * Throwable exception = result.exceptionFuture.get(); // 阻塞线程并等待撤回的结果.
 * if (exception == null) {
 *   // 撤回成功
 * } else {
 *   // 撤回失败
 * }
 * ```
 *
 * @see MessageSource.recallIn
 */
public class AsyncRecallResult internal constructor(
    /**
     * 撤回时产生的异常, 当撤回成功时为 `null`. Kotlin [Deferred] API.
     */
    public val exception: Deferred<Throwable?>,
) {
    /**
     * 撤回时产生的异常, 当撤回成功时为 `null`. Java [CompletableFuture] API.
     */
    public val exceptionFuture: CompletableFuture<Throwable?> by lazy { exception.asCompletableFuture() }

    /**
     * 撤回是否成功. Kotlin [Deferred] API.
     */
    public val isSuccess: Deferred<Boolean> by lazy {
        CompletableDeferred<Boolean>().apply {
            exception.invokeOnCompletion {
                complete(it == null)
            }
        }
    }

    /**
     * 撤回是否成功. Java [CompletableFuture] API.
     */
    public val isSuccessFuture: CompletableFuture<Boolean> by lazy { isSuccess.asCompletableFuture() }

    /**
     * 挂起协程 (在 Java 为阻塞线程) 直到撤回完成, 返回撤回时产生的异常. 当撤回成功时返回 `null`.
     */
    public suspend fun awaitException(): Throwable? {
        return exception.await()
    }

    /**
     * 挂起协程 (在 Java 为阻塞线程) 直到撤回完成, 返回撤回的结果.
     */
    public suspend fun awaitIsSuccess(): Boolean {
        return isSuccess.await()
    }
}

