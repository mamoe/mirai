package net.mamoe.mirai.utils.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.nio.read
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.ReadableByteChannel

/**
 * 多平台适配的 DatagramChannel.
 */
actual class PlatformDatagramChannel actual constructor(serverHost: String, serverPort: Short) : Closeable {
    private val serverAddress: InetSocketAddress = InetSocketAddress(serverHost, serverPort.toInt())
    private val channel: DatagramChannel = DatagramChannel.open().connect(serverAddress)

    @Throws(ReadPacketInternalException::class)
    actual suspend fun read(buffer: IoBuffer) = withContext(Dispatchers.IO) {
        try {

            (channel as ReadableByteChannel).read(buffer)
        } catch (e: Throwable) {
            throw ReadPacketInternalException(e)
        }
    }

    @Throws(SendPacketInternalException::class)
    actual suspend fun send(buffer: IoBuffer) = withContext(Dispatchers.IO) {
        buffer.readDirect {
            try {
                channel.send(it, serverAddress)
            } catch (e: Throwable) {
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