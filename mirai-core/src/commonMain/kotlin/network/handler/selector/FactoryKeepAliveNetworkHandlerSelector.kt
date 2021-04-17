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
import net.mamoe.mirai.internal.network.handler.components.ServerList
import net.mamoe.mirai.internal.network.handler.context.NetworkHandlerContext

/**
 * [AbstractKeepAliveNetworkHandlerSelector] implementation delegating [createInstance] to [factory]
 */
internal class FactoryKeepAliveNetworkHandlerSelector<H : NetworkHandler>(
    private val factory: NetworkHandlerFactory<H>,
    private val serverList: ServerList,
    private val context: NetworkHandlerContext,
) : AbstractKeepAliveNetworkHandlerSelector<H>() {
    override fun createInstance(): H =
        factory.create(context, serverList.pollCurrent()?.toSocketAddress() ?: throw NoServerAvailableException())
}