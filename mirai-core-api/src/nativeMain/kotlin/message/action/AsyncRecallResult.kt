/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.action

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn

/**
 * 异步撤回结果.
 *
 * 可由 [MessageSource.recallIn] 返回得到.
 *
 * ## 用法
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
 * @see MessageSource.recallIn
 */
public actual class AsyncRecallResult internal actual constructor(
    /**
     * 撤回时产生的异常, 当撤回成功时为 `null`.
     */
    public actual val exception: Deferred<Throwable?>,
) {
    /**
     * 撤回是否成功.
     */
    public actual val isSuccess: Deferred<Boolean> by lazy {
        CompletableDeferred<Boolean>().apply {
            exception.invokeOnCompletion {
                complete(it == null)
            }
        }
    }

    /**
     * 挂起协程直到撤回完成, 返回撤回时产生的异常. 当撤回成功时返回 `null`.
     */
    public actual suspend fun awaitException(): Throwable? {
        return exception.await()
    }

    /**
     * 挂起协程直到撤回完成, 返回撤回的结果.
     */
    public actual suspend fun awaitIsSuccess(): Boolean {
        return isSuccess.await()
    }
}
