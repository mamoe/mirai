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
import java.io.IOException
import java.net.Socket

/**
 * 多平台适配的 TCP Socket.
 */
@MiraiInternalAPI
actual class PlatformSocket : Closeable {
    private lateinit var socket: Socket

    actual val isOpen: Boolean
        get() = socket.isConnected

    override fun close() {
        if (::socket.isInitialized) {
            socket.close()
        }
    }

    @PublishedApi
    internal lateinit var writeChannel: BufferedOutputStream
    @PublishedApi
    internal lateinit var readChannel: BufferedInputStream

    actual suspend fun send(packet: ByteArray, offset: Int, length: Int) {
        withContext(Dispatchers.IO) {
            writeChannel.write(packet, offset, length)
            writeChannel.flush()
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    actual suspend fun send(packet: ByteReadPacket) {
        withContext(Dispatchers.IO) {
            try {
                writeChannel.writePacket(packet)
                writeChannel.flush()
            } catch (e: IOException) {
                throw SendPacketInternalException(e)
            }
        }
    }

    /**
     * @throws ReadPacketInternalException
     */
    actual suspend fun read(): ByteReadPacket {
        return withContext(Dispatchers.IO) {
            try {
                readChannel.readPacketAtMost(Long.MAX_VALUE)
            } catch (e: IOException) {
                throw ReadPacketInternalException(e)
            }
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