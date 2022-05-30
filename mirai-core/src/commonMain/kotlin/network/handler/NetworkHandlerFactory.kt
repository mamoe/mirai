/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import net.mamoe.mirai.internal.network.handler.NetworkHandler.State

/**
 * Factory for a specific [NetworkHandler] implementation.
 */
internal expect fun interface NetworkHandlerFactory<out H : NetworkHandler> {
    open fun create(context: NetworkHandlerContext, host: String, port: Int): H

    /**
     * Create an instance of [H]. The returning [H] has [NetworkHandler.state] of [State.INITIALIZED]
     */
    fun create(context: NetworkHandlerContext, address: SocketAddress): H

    companion object {
        fun getPlatformDefault(): NetworkHandlerFactory<*>
    }
}

internal expect abstract class SocketAddress

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal expect fun SocketAddress.getHost(): String

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal expect fun SocketAddress.getPort(): Int

internal expect fun createSocketAddress(host: String, port: Int): SocketAddress
