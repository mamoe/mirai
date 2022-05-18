/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.readPacketAtMost
import io.ktor.utils.io.streams.writePacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mamoe.mirai.internal.network.highway.HighwayProtocolChannel
import net.mamoe.mirai.utils.withUse
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.Socket
import java.util.concurrent.Executors
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * TCP Socket.
 */
internal class PlatformSocket : Closeable, HighwayProtocolChannel {
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
        kotlin.runCatching { writeChannel.close() }
        kotlin.runCatching { readChannel.close() }
    }

    @PublishedApi
    internal lateinit var writeChannel: BufferedOutputStream

    @PublishedApi
    internal lateinit var readChannel: BufferedInputStream

    suspend fun send(packet: ByteArray, offset: Int, length: Int) {
        runInterruptible(Dispatchers.IO) {
            writeChannel.write(packet, offset, length)
            writeChannel.flush()
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    override suspend fun send(packet: ByteReadPacket) {
        runInterruptible(Dispatchers.IO) {
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
    override suspend fun read(): ByteReadPacket = suspendCancellableCoroutine { cont ->
        val task = thread.submit {
            kotlin.runCatching {
                readChannel.readPacketAtMost(Long.MAX_VALUE)
            }.let {
                cont.resumeWith(it)
            }
        }
        cont.invokeOnCancellation {
            kotlin.runCatching { task.cancel(true) }
        }
    }

    suspend fun connect(serverHost: String, serverPort: Int) {
        runInterruptible(Dispatchers.IO) {
            socket = Socket(serverHost, serverPort)
            readChannel = socket.getInputStream().buffered()
            writeChannel = socket.getOutputStream().buffered()
        }
    }

    companion object {
        suspend fun connect(
            serverIp: String,
            serverPort: Int,
        ): PlatformSocket {
            val socket = PlatformSocket()
            socket.connect(serverIp, serverPort)
            return socket
        }

        suspend inline fun <R> withConnection(
            serverIp: String,
            serverPort: Int,
            block: PlatformSocket.() -> R,
        ): R {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return connect(serverIp, serverPort).withUse(block)
        }
    }
}


internal typealias SocketException = java.net.SocketException
internal typealias NoRouteToHostException = NoRouteToHostException
internal typealias UnknownHostException = UnknownHostException