package net.mamoe.mirai.utils.io

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.errors.IOException
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 多平台适配的 TCP Socket.
 */
@MiraiInternalAPI
expect class PlatformSocket() : Closeable {
    suspend fun connect(serverHost: String, serverPort: Int)

    /**
     * @throws SendPacketInternalException
     */
    suspend inline fun send(packet: ByteReadPacket)

    /**
     * @throws ReadPacketInternalException
     */
    suspend inline fun read(): ByteReadPacket

    val isOpen: Boolean
}