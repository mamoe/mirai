/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import java.net.InetSocketAddress

@Suppress("ACTUAL_WITHOUT_EXPECT") // visibility
internal actual typealias SocketAddress = java.net.InetSocketAddress

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal actual fun SocketAddress.getHost(): String = hostString ?: error("Failed to get host from address '$this'.")

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal actual fun SocketAddress.getPort(): Int = this.port

internal actual fun createSocketAddress(host: String, port: Int): SocketAddress {
    return InetSocketAddress.createUnresolved(host, port)
}