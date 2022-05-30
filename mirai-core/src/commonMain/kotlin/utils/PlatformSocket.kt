/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("PlatformSocketKt_common")

package net.mamoe.mirai.internal.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import net.mamoe.mirai.internal.network.handler.SocketAddress
import net.mamoe.mirai.internal.network.handler.getHost
import net.mamoe.mirai.internal.network.handler.getPort
import net.mamoe.mirai.internal.network.highway.HighwayProtocolChannel
import kotlin.jvm.JvmName

/**
 * TCP Socket.
 */
internal expect class PlatformSocket : Closeable, HighwayProtocolChannel {
    val isOpen: Boolean

    override fun close()

    suspend fun send(packet: ByteArray, offset: Int, length: Int)

    /**
     * @throws SendPacketInternalException
     */
    override suspend fun send(packet: ByteReadPacket)

    /**
     * @throws ReadPacketInternalException
     */
    override suspend fun read(): ByteReadPacket

    companion object {
        suspend fun connect(
            serverIp: String,
            serverPort: Int,
        ): PlatformSocket

        suspend inline fun <R> withConnection(
            serverIp: String,
            serverPort: Int,
            block: PlatformSocket.() -> R,
        ): R
    }
}

internal suspend inline fun PlatformSocket.Companion.connect(address: SocketAddress): PlatformSocket {
    return connect(address.getHost(), address.getPort())
}


internal expect class SocketException : IOException {
    constructor()
    constructor(message: String)
}

internal expect class NoRouteToHostException : IOException {
    constructor()
    constructor(message: String)
}

internal expect class UnknownHostException : IOException {
    constructor()
    constructor(message: String)
}