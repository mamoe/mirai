package net.mamoe.mirai.utils.io

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.errors.IOException
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 多平台适配的 DatagramChannel.
 */
@MiraiInternalAPI
expect class PlatformDatagramChannel(serverHost: String, serverPort: Short) : Closeable {
    /**
     * @throws SendPacketInternalException
     */
    suspend inline fun send(packet: ByteReadPacket): Boolean

    /**
     * @throws ReadPacketInternalException
     */
    suspend inline fun read(): ByteReadPacket

    val isOpen: Boolean
}

/**
 * Channel 被关闭
 */
expect class ClosedChannelException : IOException

/**
 * 在 [PlatformDatagramChannel.send] 或 [PlatformDatagramChannel.read] 时出现的错误.
 */
class SendPacketInternalException(cause: Throwable?) : Exception(cause)

/**
 * 在 [PlatformDatagramChannel.send] 或 [PlatformDatagramChannel.read] 时出现的错误.
 */
class ReadPacketInternalException(cause: Throwable?) : Exception(cause)