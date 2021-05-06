/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.message.action

import kotlinx.coroutines.*
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import java.util.concurrent.CompletableFuture

/**
 * [MessageSource.recallIn] 的结果.
 *
 * @see MessageSource.recallIn
 */
public class AsyncRecallResult internal constructor(
    /**
     * 撤回时产生的异常. Kotlin [Deferred] API.
     */
    public val exception: Deferred<Throwable?>,
) {
    /**
     * 撤回时产生的异常. Java [CompletableFuture] API.
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
     * 等待撤回完成, 返回撤回时产生的异常.
     */
    @JvmBlockingBridge
    public suspend fun awaitException(): Throwable? {
        return exception.await()
    }

    /**
     * 等待撤回完成, 返回撤回的结果.
     */
    @JvmBlockingBridge
    public suspend fun awaitIsSuccess(): Boolean {
        return isSuccess.await()
    }
}


// copied from kotlinx-coroutines-jdk8
private fun <T> Deferred<T>.asCompletableFuture(): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    setupCancellation(future)
    invokeOnCompletion {
        @OptIn(ExperimentalCoroutinesApi::class)
        try {
            future.complete(getCompleted())
        } catch (t: Throwable) {
            future.completeExceptionally(t)
        }
    }
    return future
}

// copied from kotlinx-coroutines-jdk8
private fun Job.setupCancellation(future: CompletableFuture<*>) {
    future.whenComplete { _, exception ->
        cancel(exception?.let {
            it as? CancellationException ?: CancellationException("CompletableFuture was completed exceptionally", it)
        })
    }
}
