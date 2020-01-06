package net.mamoe.mirai.utils.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.nio.readPacketAtMost
import kotlinx.io.nio.writePacket
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

actual typealias ClosedChannelException = java.nio.channels.ClosedChannelException

/**
 * 多平台适配的 DatagramChannel.
 */
actual class PlatformDatagramChannel actual constructor(
    serverHost: String,
    serverPort: Short
) : Closeable {
    @PublishedApi
    internal val channel: DatagramChannel = DatagramChannel.open().connect(InetSocketAddress(serverHost, serverPort.toInt()))
    actual val isOpen: Boolean get() = channel.isOpen
    override fun close() = channel.close()

    actual suspend inline fun send(packet: ByteReadPacket): Boolean = withContext(Dispatchers.IO) {
        try {
            (channel as WritableByteChannel).writePacket(packet)
        } catch (e: Throwable) {
            throw SendPacketInternalException(e)
        }
    }

    actual suspend inline fun read(): ByteReadPacket = withContext(Dispatchers.IO) {
        try {
            (channel as ReadableByteChannel).readPacketAtMost(Long.MAX_VALUE)
        } catch (e: Throwable) {
            throw ReadPacketInternalException(e)
        }
    }
}