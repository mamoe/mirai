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
import net.mamoe.mirai.internal.network.protocol.packet.login.toIpV4Long
import net.mamoe.mirai.utils.DEFAULT_BUFFER_SIZE
import net.mamoe.mirai.utils.toReadPacket
import net.mamoe.mirai.utils.wrapIO
import platform.posix.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * TCP Socket.
 */
@OptIn(UnsafeNumber::class)
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
        get() = write(socket, null, 0).convert<Long>() != 0L

    @OptIn(ExperimentalIoApi::class)
    actual override fun close() {
        if (close(socket) != 0) {
            throw PosixException.forErrno(posixFunctionName = "close()").wrapIO()
        }
    }

    @OptIn(ExperimentalIoApi::class)
    actual suspend fun send(packet: ByteArray, offset: Int, length: Int): Unit = readLock.withLock {
        withContext(dispatcher) {
            require(offset >= 0) { "offset must >= 0" }
            require(length >= 0) { "length must >= 0" }
            require(offset + length <= packet.size) { "It must follows offset + length <= packet.size" }
            packet.usePinned { pin ->
                if (write(socket, pin.addressOf(offset), length.convert()).convert<Long>() != 0L) {
                    throw PosixException.forErrno(posixFunctionName = "close()").wrapIO()
                }
            }
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    @OptIn(ExperimentalIoApi::class)
    actual override suspend fun send(packet: ByteReadPacket): Unit = readLock.withLock {
        withContext(dispatcher) {
            val writeBuffer = writeBuffer
            val length = packet.readAvailable(writeBuffer.get())
            if (write(socket, writeBuffer.addressOf(0), length.convert()).convert<Long>() != 0L) {
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
            val length = read(socket, readBuffer.addressOf(0), readBuffer.get().size.convert()).convert<Long>()
            readBuffer.get().toReadPacket(length = length.toInt())
        }
    }

    actual companion object {

        @OptIn(UnsafeNumber::class, ExperimentalIoApi::class)
        actual suspend fun connect(
            serverIp: String,
            serverPort: Int
        ): PlatformSocket {
            val addr = memScoped {
                alloc<sockaddr_in>() {
                    sin_family = AF_INET.convert()
                    resolveIpFromHost(serverIp)
                    sin_addr.s_addr = resolveIpFromHost(serverIp)
                }
            }.reinterpret<sockaddr>()

            val id = socket(AF_INET, 1 /* SOCKET_STREAM */, IPPROTO_TCP)
            if (id != 0) throw PosixException.forErrno(posixFunctionName = "socket()")

            val conn = connect(id, addr.ptr, sizeOf<sockaddr_in>().convert())
            if (conn != 0) throw PosixException.forErrno(posixFunctionName = "connect()")

            return PlatformSocket(conn)
        }

        private fun resolveIpFromHost(serverIp: String): UInt {
            val host = gethostbyname(serverIp)
                ?: throw IllegalStateException("Failed to resolve IP from host. host=$serverIp")

            val str = try {
                val hAddrList = host.pointed.h_addr_list
                    ?: throw IllegalStateException("Empty IP list resolved from host. host=$serverIp")

                hAddrList[0]!!.toKString()
            } finally {
                free(host)
            }

            // TODO: 2022/5/30 check memory

            return str.toIpV4Long().toUInt()
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
