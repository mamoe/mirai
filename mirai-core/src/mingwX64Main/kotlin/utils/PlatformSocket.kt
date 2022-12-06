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
import io.ktor.utils.io.core.EOFException
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.internal.network.highway.HighwayProtocolChannel
import net.mamoe.mirai.utils.ByteArrayPool
import net.mamoe.mirai.utils.DEFAULT_BUFFER_SIZE
import net.mamoe.mirai.utils.toReadPacket
import platform.posix.*
import sockets.socket_get_connected_ip
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * TCP Socket.
 */
internal actual class PlatformSocket(
    private val socket: SOCKET
) : Closeable, HighwayProtocolChannel {
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val readDispatcher: CoroutineDispatcher = newSingleThreadContext("PlatformSocket#$socket.readDispatcher")

    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val sendDispatcher: CoroutineDispatcher = newSingleThreadContext("PlatformSocket#$socket.sendDispatcher")

    private val writeLock = Mutex()
    private val writeBuffer = ByteArray(DEFAULT_BUFFER_SIZE).pin()

    actual val isOpen: Boolean
        get() = send(socket, null, 0, 0).convert<Long>() != 0L
    actual val connectedIp: Long
        get() = if (isOpen) {
            sockets.socket_get_connected_ip(socket).toLong()
        } else {
            0L
        }

    actual override fun close() {
        closesocket(socket)
        (readDispatcher as? CloseableCoroutineDispatcher)?.close()
        (sendDispatcher as? CloseableCoroutineDispatcher)?.close()
        writeBuffer.unpin()
    }

    actual suspend fun send(packet: ByteArray, offset: Int, length: Int): Unit = writeLock.withLock {
        withContext(sendDispatcher) {
            require(offset >= 0) { "offset must >= 0" }
            require(length >= 0) { "length must >= 0" }
            require(offset + length <= packet.size) { "It must follows offset + length <= packet.size" }
            packet.usePinned { pin ->
                if (send(socket, pin.addressOf(offset), length.convert(), 0).convert<Long>() < 0L) {
                    @Suppress("INVISIBLE_MEMBER")
                    throw PosixException.forErrno(posixFunctionName = "send()").wrapIO()
                }
            }
        }
    }


    actual override suspend fun send(packet: ByteReadPacket): Unit = writeLock.withLock {
        withContext(sendDispatcher) {
            val writeBuffer = writeBuffer
            while (packet.remaining != 0L) {
                val length = packet.readAvailable(writeBuffer.get())
                if (send(socket, writeBuffer.addressOf(0), length.convert(), 0).convert<Long>() < 0L) {
                    @Suppress("INVISIBLE_MEMBER")
                    throw PosixException.forErrno(posixFunctionName = "send()").wrapIO()
                }
            }
        }
    }

    /**
     * @throws EOFException
     */
    actual override suspend fun read(): ByteReadPacket = withContext(readDispatcher) {
        val readBuffer = ByteArrayPool.borrow()
        try {
            val length = readBuffer.usePinned { pinned ->
                recv(socket, pinned.addressOf(0), pinned.get().size.convert(), 0).convert<Long>()
            }
            if (length <= 0L) throw EOFException("recv: $length, errno=$errno")
            // [toReadPacket] 后的 readBuffer 会被其内部直接使用，而不是 copy 一份
            // 在 release 前不能被复用
            readBuffer.toReadPacket(length = length.toInt()) { ByteArrayPool.recycle(it) }
        } catch (e: Throwable) {
            ByteArrayPool.recycle(readBuffer)
            throw e
        }
    }

    actual companion object {

        actual suspend fun connect(
            serverIp: String,
            serverPort: Int
        ): PlatformSocket {
            val r = sockets.socket_create_connect(serverIp.cstr, serverPort.toString().cstr)
            if (r == INVALID_SOCKET) error("Failed socket_create_connect: $r")
            return PlatformSocket(r)
        }

        actual suspend inline fun <R> withConnection(
            serverIp: String,
            serverPort: Int,
            block: PlatformSocket.() -> R
        ): R {
            contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
            return connect(serverIp, serverPort).use(block)
        }
    }

}
