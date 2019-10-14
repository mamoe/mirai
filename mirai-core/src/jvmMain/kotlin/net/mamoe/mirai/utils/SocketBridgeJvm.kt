package net.mamoe.mirai.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.errors.IOException
import kotlinx.io.nio.read
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.ReadableByteChannel

actual class PlatformDatagramChannel actual constructor(serverHost: String, serverPort: Short) : Closeable {
    private val serverAddress: InetSocketAddress = InetSocketAddress(serverHost, serverPort.toInt())
    private val channel: DatagramChannel = DatagramChannel.open().connect(serverAddress)

    actual suspend fun read(buffer: IoBuffer) = withContext(Dispatchers.IO) {
        try {
            (channel as ReadableByteChannel).read(buffer)
        } catch (e: Exception) {
            throw ReadPacketInternalException(e)
        }
    }

    actual suspend fun send(buffer: IoBuffer) = withContext(Dispatchers.IO) {
        buffer.readDirect {
            try {
                channel.send(it, serverAddress)
            } catch (e: Exception) {
                throw SendPacketInternalException(e)
            }
        }
    }

    override fun close() {
        channel.close()
    }

    actual val isOpen: Boolean get() = channel.isOpen
}

actual typealias ClosedChannelException = java.nio.channels.ClosedChannelException

actual class SendPacketInternalException actual constructor(cause: Throwable?) : IOException(cause)

actual class ReadPacketInternalException actual constructor(cause: Throwable?) : IOException(cause)