/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

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
internal interface NetworkHandlerSelector<H : NetworkHandler> {
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
     * Returned [H] can be in [NetworkHandler.State.OK] only (but it may happen that the state changed just after returning from this function).
     *
     * This function may throw exceptions, which would be propagated to the original caller of [SelectorNetworkHandler.resumeConnection].
     */
    suspend fun awaitResumeInstance(): H
}