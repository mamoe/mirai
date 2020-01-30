package net.mamoe.mirai.utils.io

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.readAvailable
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 多平台适配的 TCP Socket.
 */
@MiraiInternalAPI
actual class PlatformSocket : Closeable {
    @UseExperimental(KtorExperimentalAPI::class)
    lateinit var socket: Socket

    @UseExperimental(KtorExperimentalAPI::class)
    actual val isOpen: Boolean
        get() = socket.socketContext.isActive

    override fun close() = socket.dispose()

    @PublishedApi
    internal lateinit var writeChannel: ByteWriteChannel
    @PublishedApi
    internal lateinit var readChannel: ByteReadChannel

    actual suspend inline fun send(packet: ByteArray, offset: Int, length: Int) {
        try {
            writeChannel.writeFully(packet, offset, length)
        } catch (e: Exception) {
            throw SendPacketInternalException(e)
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    actual suspend inline fun send(packet: ByteReadPacket) {
        try {
            writeChannel.writePacket(packet)
        } catch (e: Exception) {
            throw SendPacketInternalException(e)
        }
    }

    /**
     * @throws ReadPacketInternalException
     */
    actual suspend inline fun read(): ByteReadPacket {
        // do not use readChannel.readRemaining() !!! this function never returns
        ByteArrayPool.useInstance { buffer ->
            val count = try {
                readChannel.readAvailable(buffer)
            } catch (e: Exception) {
                throw ReadPacketInternalException(e)
            }
            return buffer.toReadPacket(0, count)
        }
    }

    @UseExperimental(KtorExperimentalAPI::class)
    actual suspend fun connect(serverHost: String, serverPort: Int) {
        socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().connect(serverHost, serverPort)
        writeChannel = socket.openWriteChannel(true)
        readChannel = socket.openReadChannel()
    }
}