/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import net.mamoe.mirai.internal.network.highway.HighwayProtocolChannel

/**
 * TCP Socket.
 */
internal actual class PlatformSocket : Closeable, HighwayProtocolChannel {
    actual val isOpen: Boolean
        get() = TODO("Not yet implemented")

    actual override fun close() {
    }

    actual suspend fun send(packet: ByteArray, offset: Int, length: Int) {
    }

    /**
     * @throws SendPacketInternalException
     */
    actual override suspend fun send(packet: ByteReadPacket) {
    }

    /**
     * @throws ReadPacketInternalException
     */
    actual override suspend fun read(): ByteReadPacket {
        TODO("Not yet implemented")
    }

    actual suspend fun connect(serverHost: String, serverPort: Int) {
    }

    actual companion object {
        actual suspend fun connect(
            serverIp: String,
            serverPort: Int
        ): PlatformSocket {
            TODO("Not yet implemented")
        }

        actual suspend inline fun <R> withConnection(
            serverIp: String,
            serverPort: Int,
            block: PlatformSocket.() -> R
        ): R {
            TODO("Not yet implemented")
        }

    }

}

internal actual class SocketException : IOException {
    actual constructor() : super("", null)

    actual constructor(message: String) : super(message)
}

internal actual class NoRouteToHostException : IOException {
    actual constructor() : super("")
    actual constructor(message: String) : super(message)
}

internal actual class UnknownHostException : IOException {
    actual constructor() : super("")
    actual constructor(message: String) : super(message)
}