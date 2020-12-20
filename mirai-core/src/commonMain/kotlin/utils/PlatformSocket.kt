/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.streams.readPacketAtMost
import kotlinx.io.streams.writePacket
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.NoRouteToHostException
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.Executors

/**
 * TCP Socket.
 */
internal class PlatformSocket : Closeable {
    private lateinit var socket: Socket

    val isOpen: Boolean
        get() =
            if (::socket.isInitialized)
                socket.isConnected
            else false

    override fun close() {
        if (::socket.isInitialized) {
            socket.close()
        }
        thread.shutdownNow()
    }

    @PublishedApi
    internal lateinit var writeChannel: BufferedOutputStream

    @PublishedApi
    internal lateinit var readChannel: BufferedInputStream

    suspend fun send(packet: ByteArray, offset: Int, length: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(Dispatchers.IO) {
            writeChannel.write(packet, offset, length)
            writeChannel.flush()
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    suspend fun send(packet: ByteReadPacket) {
        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(Dispatchers.IO) {
            try {
                writeChannel.writePacket(packet)
                writeChannel.flush()
            } catch (e: IOException) {
                throw SendPacketInternalException(e)
            }
        }
    }

    private val thread = Executors.newSingleThreadExecutor()

    /**
     * @throws ReadPacketInternalException
     */
    suspend fun read(): ByteReadPacket = suspendCancellableCoroutine { cont ->
        thread.submit {
            kotlin.runCatching {
                readChannel.readPacketAtMost(Long.MAX_VALUE)
            }.let {
                cont.resumeWith(it)
            }
        }
    }

    suspend fun connect(serverHost: String, serverPort: Int) {
        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(Dispatchers.IO) {
            socket = Socket(serverHost, serverPort)
            readChannel = socket.getInputStream().buffered()
            writeChannel = socket.getOutputStream().buffered()
        }
    }
}


internal typealias SocketException = java.net.SocketException
internal typealias NoRouteToHostException = NoRouteToHostException
internal typealias UnknownHostException = UnknownHostException