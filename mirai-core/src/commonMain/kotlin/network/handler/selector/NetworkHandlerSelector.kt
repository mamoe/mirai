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

/**
 * A lazy stateful selector of [NetworkHandler]. This is used as a director([selector][SelectorNetworkHandler.selector]) to [SelectorNetworkHandler].
 */
internal interface NetworkHandlerSelector<H : NetworkHandler> {
    /**
     * Returns an instance immediately without suspension, or `null` if instance not ready.
     *
     * This function should not throw any exception.
     * @see awaitResumeInstance
     */
    fun getResumedInstance(): H?

    /**
     * Returns an alive [NetworkHandler], or suspends the coroutine until the connection has been made again.
     *
     * This function may throw exceptions, which would be propagated to the original caller of [SelectorNetworkHandler.resumeConnection].
     */
    suspend fun awaitResumeInstance(): H
}