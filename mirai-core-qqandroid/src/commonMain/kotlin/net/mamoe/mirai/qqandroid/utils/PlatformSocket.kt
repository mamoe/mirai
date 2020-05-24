/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.errors.IOException
import net.mamoe.mirai.utils.Throws
import kotlin.coroutines.CoroutineContext

/**
 * 多平台适配的 TCP Socket.
 */
internal expect class PlatformSocket() : Closeable {
    @Throws(SocketException::class)
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

expect open class SocketException : IOException
expect class NoRouteToHostException : SocketException
expect class UnknownHostException : IOException