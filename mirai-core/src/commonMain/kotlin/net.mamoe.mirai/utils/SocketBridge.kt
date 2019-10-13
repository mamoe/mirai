package net.mamoe.mirai.utils

import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.errors.IOException

expect class MiraiDatagramChannel(serverHost: String, serverPort: Short) : Closeable {
    suspend fun read(buffer: IoBuffer): Int
    suspend fun send(buffer: IoBuffer): Int

    val isOpen: Boolean
}

expect class ClosedChannelException : IOException