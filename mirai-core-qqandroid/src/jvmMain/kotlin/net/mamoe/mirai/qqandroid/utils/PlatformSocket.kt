/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.ExperimentalIoApi
import kotlinx.io.errors.IOException
import kotlinx.io.streams.readPacketAtMost
import kotlinx.io.streams.writePacket
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket
import java.net.SocketException
import kotlin.coroutines.CoroutineContext

/**
 * 多平台适配的 TCP Socket.
 */
internal actual class PlatformSocket : Closeable {
    private lateinit var socket: Socket

    actual val isOpen: Boolean
        get() =
            if (::socket.isInitialized)
                socket.isConnected
            else false

    actual override fun close() {
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

    actual suspend fun connect(coroutineContext: CoroutineContext, serverHost: String, serverPort: Int) {
        withContext(Dispatchers.IO) {
            socket = Socket(serverHost, serverPort)
            readChannel = socket.getInputStream().buffered()
            writeChannel = socket.getOutputStream().buffered()
        }
    }
}

actual typealias NoRouteToHostException = java.net.NoRouteToHostException

actual typealias SocketException = SocketException

// ktor aSocket

/*
/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.ExperimentalIoApi
import kotlinx.io.core.buildPacket
import kotlinx.io.errors.IOException
import net.mamoe.mirai.qqandroid.utils.io.useBytes
import java.net.InetSocketAddress
import java.net.SocketException
import kotlin.coroutines.CoroutineContext

/**
 * 多平台适配的 TCP Socket.
 */
internal actual class PlatformSocket : Closeable {
    private lateinit var socket: io.ktor.network.sockets.Socket

    actual val isOpen: Boolean
        get() =
            if (::socket.isInitialized)
                !socket.isClosed
            else false

    actual override fun close() {
        if (::socket.isInitialized) {
            socket.close()
        }
    }

    @PublishedApi
    internal lateinit var writeChannel: ByteWriteChannel

    @PublishedApi
    internal lateinit var readChannel: ByteReadChannel

    actual suspend fun send(packet: ByteArray, offset: Int, length: Int) {
        withContext(Dispatchers.IO) {
            writeChannel.writeFully(packet, offset, length)
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    actual suspend fun send(packet: ByteReadPacket) {
        withContext(Dispatchers.IO) {
            try {
                packet.useBytes { data: ByteArray, length: Int ->
                    writeChannel.writeFully(data, offset = 0, length = length)
                }
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
                buildPacket {
                    readChannel.read {
                        writeFully(it)
                    }
                }
            } catch (e: IOException) {
                throw ReadPacketInternalException(e)
            }
        }
    }

    @OptIn(ExperimentalIoApi::class, KtorExperimentalAPI::class)
    actual suspend fun connect(coroutineContext: CoroutineContext, serverHost: String, serverPort: Int) {
        withContext(Dispatchers.IO) {
            socket = aSocket(ActorSelectorManager(coroutineContext)).tcp().tcpNoDelay()
                .connect(InetSocketAddress(serverHost, serverPort))
            readChannel = socket.openReadChannel()
            writeChannel = socket.openWriteChannel(true)
        }
    }
}

actual typealias NoRouteToHostException = java.net.NoRouteToHostException

actual typealias SocketException = SocketException
 */
actual typealias UnknownHostException = java.net.UnknownHostException