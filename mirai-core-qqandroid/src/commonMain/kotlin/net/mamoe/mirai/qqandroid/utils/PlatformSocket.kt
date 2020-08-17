/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.errors.IOException
import kotlin.coroutines.CoroutineContext

/**
 * 多平台适配的 TCP Socket.
 */
internal expect class PlatformSocket() : Closeable {
    @kotlin.Throws(SocketException::class)
    suspend fun connect(coroutineContext: CoroutineContext, serverHost: String, serverPort: Int)

    /**
     * @throws SendPacketInternalException
     */
    suspend fun send(packet: ByteArray, offset: Int = 0, length: Int = packet.size - offset)

    /**
     * @throws SendPacketInternalException
     */
    suspend fun send(packet: ByteReadPacket)

    /**
     * @throws ReadPacketInternalException
     */
    suspend fun read(): ByteReadPacket

    val isOpen: Boolean

    override fun close()
}

internal expect open class SocketException : IOException
internal expect class NoRouteToHostException : SocketException
internal expect class UnknownHostException : IOException