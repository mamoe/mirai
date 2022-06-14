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
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.mamoe.mirai.internal.network.highway.HighwayProtocolChannel
import net.mamoe.mirai.utils.DEFAULT_BUFFER_SIZE
import net.mamoe.mirai.utils.toReadPacket
import net.mamoe.mirai.utils.wrapIO
import platform.posix.close
import platform.posix.read
import platform.posix.write
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * TCP Socket.
 */
internal actual class PlatformSocket(
    private val socket: Int
) : Closeable, HighwayProtocolChannel {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = newSingleThreadContext("PlatformSocket#$socket.dispatcher")

    private val readLock = Mutex()
    private val readBuffer = ByteArray(DEFAULT_BUFFER_SIZE).pin()
    private val writeLock = Mutex()
    private val writeBuffer = ByteArray(DEFAULT_BUFFER_SIZE).pin()

    actual val isOpen: Boolean
        get() = write(socket, null, 0) != 0

    actual override fun close() {
        if (close(socket) != 0) {
            throw PosixException.forErrno(posixFunctionName = "close()").wrapIO()
        }
    }

    actual suspend fun send(packet: ByteArray, offset: Int, length: Int): Unit = readLock.withLock {
        withContext(dispatcher) {
            require(offset >= 0) { "offset must >= 0" }
            require(length >= 0) { "length must >= 0" }
            require(offset + length <= packet.size) { "It must follows offset + length <= packet.size" }
            packet.usePinned { pin ->
                if (write(socket, pin.addressOf(offset), length.convert()) != 0) {
                    throw PosixException.forErrno(posixFunctionName = "close()").wrapIO()
                }
            }
        }
    }


    actual override suspend fun send(packet: ByteReadPacket): Unit = readLock.withLock {
        withContext(dispatcher) {
            val writeBuffer = writeBuffer
            val length = packet.readAvailable(writeBuffer.get())
            if (write(socket, writeBuffer.addressOf(0), length.convert()) != 0) {
                throw PosixException.forErrno(posixFunctionName = "close()").wrapIO()
            }
        }
    }

    /**
     * @throws ReadPacketInternalException
     */
    actual override suspend fun read(): ByteReadPacket = writeLock.withLock {
        withContext(dispatcher) {
            val readBuffer = readBuffer
            val length = read(socket, readBuffer.addressOf(0), readBuffer.get().size.convert())
            readBuffer.get().toReadPacket(length = length)
        }
    }

    actual companion object {

        actual suspend fun connect(
            serverIp: String,
            serverPort: Int
        ): PlatformSocket {
            val r = sockets.socket_create_connect(serverIp.cstr, serverPort.toUShort())
            if (r < 0) error("Failed socket_create_connect: $r")
            return PlatformSocket(r)

//            val addr = memScoped {
//                alloc<sockaddr_in>() {
//                    sin_family = AF_INET.convert()
//                    sin_port = htons(serverPort.toUShort())
//                    sin_addr.S_un
//                    sin_addr = resolveIpFromHost(serverIp).reinterpret<in_addr>().rawValue
//                }
//            }.reinterpret<sockaddr>()
//
//            val id = socket(AF_INET, SOCK_STREAM, 0)
//            if (id.toInt() == -1) throw PosixException.forErrno(posixFunctionName = "socket()")
//
//            val conn = connect(id, addr.ptr, sizeOf<sockaddr_in>().convert())
//            if (conn != 0) throw PosixException.forErrno(posixFunctionName = "connect()")
//
//            return PlatformSocket(conn)
        }

//        private fun resolveIpFromHost(serverIp: String): CPointer<hostent> {
//            return gethostbyname(serverIp)
//                ?: throw IllegalStateException("Failed to resolve IP from host. host=$serverIp")
//        }

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
