/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

internal actual abstract class SocketAddress(
    val host: String,
    val port: Int,
    @Suppress("UNUSED_PARAMETER") constructorMarker: Unit?, // avoid ambiguity with function SocketAddress
)

internal actual fun SocketAddress.getHost(): String = host
internal actual fun SocketAddress.getPort(): Int = port


internal class SocketAddressImpl(host: String, port: Int) : SocketAddress(host, port, null) {
    override fun toString(): String {
        return "$host:$port"
    }
}

internal actual fun createSocketAddress(host: String, port: Int): SocketAddress {
    return SocketAddressImpl(host, port)
}

