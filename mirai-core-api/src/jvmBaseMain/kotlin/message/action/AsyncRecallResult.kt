/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
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
 * [MessageSource.recallIn] 的结果.
 *
 * @see MessageSource.recallIn
 */
public actual class AsyncRecallResult internal actual constructor(
    /**
     * 撤回时产生的异常. Kotlin [Deferred] API.
     */
    public actual val exception: Deferred<Throwable?>,
) {
    /**
     * 撤回时产生的异常. Java [CompletableFuture] API.
     */
    public val exceptionFuture: CompletableFuture<Throwable?> by lazy { exception.asCompletableFuture() }

    /**
     * 撤回是否成功. Kotlin [Deferred] API.
     */
    public actual val isSuccess: Deferred<Boolean> by lazy {
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
    public actual suspend fun awaitException(): Throwable? {
        return exception.await()
    }

    /**
     * 等待撤回完成, 返回撤回的结果.
     */
    public actual suspend fun awaitIsSuccess(): Boolean {
        return isSuccess.await()
    }
}
