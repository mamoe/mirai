/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import kotlinx.coroutines.CancellationException
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory

/**
 * A **lazy** director([selector][SelectorNetworkHandler.selector]) of [NetworkHandler].
 *
 * *lazy* means that no action is taken at any time until member functions are invoked.
 *
 * It can produce [H] instances (maybe by calling [NetworkHandlerFactory]), to be used by [SelectorNetworkHandler]
 *
 * @see SelectorNetworkHandler
 */
internal interface NetworkHandlerSelector<out H : NetworkHandler> {
    /**
     * Returns an instance immediately without suspension, or `null` if instance not ready. Returned [H] can be in any states.
     *
     * This function should not throw any exception.
     * @see awaitResumeInstance
     */
    fun getCurrentInstanceOrNull(): H?

    /**
     * Returns the current [NetworkHandler] or creates a new one if it is `null`. Returned [H] can be in any states.
     */
    fun getCurrentInstanceOrCreate(): H

    /**
     * Returns an alive [NetworkHandler], or suspends the coroutine until the connection has been made again.
     * Returned [H] can be in [NetworkHandler.State.LOADING] and [NetworkHandler.State.OK] only (but it may happen that the state changed just after returning from this function).
     *
     * @throws NetworkException [NetworkHandler.resumeConnection] 抛出了 [NetworkException] 并且 [NetworkException.recoverable] 为 `false`.
     * @throws Throwable [NetworkHandler.resumeConnection] 抛出了其他异常. 任何其他异常都属于内部错误并会原样抛出.
     * @throws MaxAttemptsReachedException 重试次数达到上限 (由 selector 构造)
     * @throws CancellationException 协程被取消
     */
    @Throws(NetworkException::class, MaxAttemptsReachedException::class, CancellationException::class, Throwable::class)
    suspend fun awaitResumeInstance(): H
}