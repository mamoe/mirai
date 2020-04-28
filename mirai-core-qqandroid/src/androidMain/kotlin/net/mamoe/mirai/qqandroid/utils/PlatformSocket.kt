/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.ExperimentalIoApi
import kotlinx.io.streams.readPacketAtMost
import kotlinx.io.streams.writePacket
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.Socket

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

    @OptIn(ExperimentalIoApi::class)
    actual suspend fun connect(serverHost: String, serverPort: Int) {
        withContext(Dispatchers.IO) {
            socket = Socket(serverHost, serverPort)
            readChannel = socket.getInputStream().buffered()
            writeChannel = socket.getOutputStream().buffered()
        }
    }
}