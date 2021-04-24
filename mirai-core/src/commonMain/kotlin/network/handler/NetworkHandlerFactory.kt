/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Factory for a specific [NetworkHandler] implementation.
 */
internal interface NetworkHandlerFactory<H : NetworkHandler> {
    fun create(context: NetworkHandlerContext, host: String, port: Int): H =
        create(context, InetSocketAddress.createUnresolved(host, port))

    fun create(context: NetworkHandlerContext, host: InetAddress, port: Int): H =
        create(context, InetSocketAddress(host, port))

    /**
     * Create an instance of [H]. The returning [H] has [NetworkHandler.state] of [State.INITIALIZED]
     */
    fun create(context: NetworkHandlerContext, address: SocketAddress): H
}
