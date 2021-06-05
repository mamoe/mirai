/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import net.mamoe.mirai.internal.network.components.ServerList
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory

/**
 * [AbstractKeepAliveNetworkHandlerSelector] implementation delegating [createInstance] to [factory]
 */
internal class FactoryKeepAliveNetworkHandlerSelector<H : NetworkHandler> : AbstractKeepAliveNetworkHandlerSelector<H> {
    private val factory: NetworkHandlerFactory<H>
    private val context: NetworkHandlerContext

    constructor(factory: NetworkHandlerFactory<H>, context: NetworkHandlerContext) : super() {
        this.factory = factory
        this.context = context
    }

    constructor(
        maxAttempts: Int,
        factory: NetworkHandlerFactory<H>,
        context: NetworkHandlerContext
    ) : super(maxAttempts) {
        this.factory = factory
        this.context = context
    }

    override fun createInstance(): H =
        factory.create(
            context,
//            context[ServerList].pollCurrent()?.toSocketAddress() ?: throw NoServerAvailableException()
            context[ServerList].pollAny().toSocketAddress()
        )
}