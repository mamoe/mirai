package net.mamoe.mirai.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.nio.read
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.ReadableByteChannel

actual class MiraiDatagramChannel actual constructor(serverHost: String, serverPort: Short) : Closeable {
    private val serverAddress: InetSocketAddress = InetSocketAddress(serverHost, serverPort.toInt())
    private val channel: DatagramChannel = DatagramChannel.open().connect(serverAddress)

    actual suspend fun read(buffer: IoBuffer) = withContext(Dispatchers.IO) { (channel as ReadableByteChannel).read(buffer) }
    actual suspend fun send(buffer: IoBuffer) = withContext(Dispatchers.IO) { buffer.readDirect { channel.send(it, serverAddress) } }

    override fun close() {
        channel.close()
    }

    actual val isOpen: Boolean get() = channel.isOpen
}

actual typealias ClosedChannelException = java.nio.channels.ClosedChannelException