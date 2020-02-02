package net.mamoe.mirai.utils.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.ExperimentalIoApi
import kotlinx.io.streams.readPacketAtMost
import kotlinx.io.streams.writePacket
import net.mamoe.mirai.utils.MiraiInternalAPI
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket

/**
 * 多平台适配的 TCP Socket.
 */
@MiraiInternalAPI
actual class PlatformSocket : Closeable {
    private lateinit var socket: Socket

    actual val isOpen: Boolean
        get() = socket.isConnected

    override fun close() = socket.close()

    @PublishedApi
    internal lateinit var writeChannel: BufferedOutputStream
    @PublishedApi
    internal lateinit var readChannel: BufferedInputStream

    actual suspend inline fun send(packet: ByteArray, offset: Int, length: Int) {
        withContext(Dispatchers.IO) {
            writeChannel.write(packet, offset, length)
            writeChannel.flush()
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    actual suspend inline fun send(packet: ByteReadPacket) {
        withContext(Dispatchers.IO) {
            writeChannel.writePacket(packet)
            writeChannel.flush()
        }
    }

    /**
     * @throws ReadPacketInternalException
     */
    actual suspend inline fun read(): ByteReadPacket {
        return withContext(Dispatchers.IO) {
            readChannel.readPacketAtMost(Long.MAX_VALUE)
        }
    }

    @UseExperimental(ExperimentalIoApi::class)
    actual suspend fun connect(serverHost: String, serverPort: Int) {
        withContext(Dispatchers.IO) {
            socket = Socket(serverHost, serverPort)
            readChannel = socket.getInputStream().buffered()
            writeChannel = socket.getOutputStream().buffered()
        }
    }
}