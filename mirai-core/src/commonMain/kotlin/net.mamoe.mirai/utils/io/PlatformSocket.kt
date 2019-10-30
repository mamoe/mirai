package net.mamoe.mirai.utils.io

import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.errors.IOException

/**
 * 多平台适配的 DatagramChannel.
 */
expect class PlatformDatagramChannel(serverHost: String, serverPort: Short) : Closeable {

    suspend fun read(buffer: IoBuffer): Int
    suspend fun send(buffer: IoBuffer): Int

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