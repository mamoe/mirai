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
import net.mamoe.mirai.utils.*
import platform.posix.close
import platform.posix.errno
import platform.posix.recv
import platform.posix.write
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * TCP Socket.
 */
internal actual class PlatformSocket(
    private val socket: Int,
    bufferSize: Int = DEFAULT_BUFFER_SIZE * 2 // improve performance for some big packets
) : Closeable, HighwayProtocolChannel {
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val readDispatcher: CoroutineDispatcher = newSingleThreadContext("PlatformSocket#$socket.dispatcher")

    // Native send and read are blocking. Using a dedicated thread(dispatcher) to do the job.
    @Suppress("UnnecessaryOptInAnnotation")
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val sendDispatcher: CoroutineDispatcher = newSingleThreadContext("PlatformSocket#$socket.dispatcher")

    private val readLock = Mutex()
    private val writeLock = Mutex()
    private val writeBuffer = ByteArray(bufferSize).pin()

    actual val isOpen: Boolean
        get() = write(socket, null, 0).convert<Long>() != 0L

    actual override fun close() {
        close(socket)
        (readDispatcher as CloseableCoroutineDispatcher).close()
        (sendDispatcher as CloseableCoroutineDispatcher).close()
        writeBuffer.unpin()
    }

    actual suspend fun send(packet: ByteArray, offset: Int, length: Int): Unit = writeLock.withLock {
        withContext(sendDispatcher) {
            require(offset >= 0) { "offset must >= 0" }
            require(length >= 0) { "length must >= 0" }
            require(offset + length <= packet.size) { "It must follows offset + length <= packet.size" }
            packet.usePinned { pin ->
                if (write(socket, pin.addressOf(offset), length.convert()).convert<Long>() < 0L) {
                    throw PosixException.forErrno(posixFunctionName = "write()").wrapIO()
                }
            }
        }
    }

    /**
     * @throws SendPacketInternalException
     */
    actual override suspend fun send(packet: ByteReadPacket): Unit = writeLock.withLock {
        withContext(sendDispatcher) {
            logger.info { "Native socket sending: len=${packet.remaining}" }
            val writeBuffer = writeBuffer
            while (packet.remaining != 0L) {
                val length = packet.readAvailable(writeBuffer.get())
                if (write(socket, writeBuffer.addressOf(0), length.convert()).convert<Long>() < 0L) {
                    throw PosixException.forErrno(posixFunctionName = "write()").wrapIO()
                }
                logger.info { "Native socket sent $length bytes." }
            }
        }
    }

    /**
     * @throws ReadPacketInternalException
     */
    actual override suspend fun read(): ByteReadPacket = readLock.withLock {
        withContext(readDispatcher) {
            logger.info { "Native socket reading." }

            val readBuffer = ByteArrayPool.borrow()

            try {
                val length = readBuffer.usePinned { pinned ->
                    recv(socket, pinned.addressOf(0), pinned.get().size.convert(), 0).convert<Long>()
                }

                if (length <= 0L) throw EOFException("recv: $length, errno=$errno")
                logger.info {
                    "Native socket read $length bytes: ${
                        readBuffer.copyOf(length.toInt()).toUHexString()
                    }"
                }
                readBuffer.toReadPacket(length = length.toInt()) { ByteArrayPool.recycle(it) }
            } catch (e: Throwable) {
                ByteArrayPool.recycle(readBuffer)
                throw e
            }
        }
    }

    actual companion object {
        private val logger: MiraiLogger = MiraiLogger.Factory.create(PlatformSocket::class)

        actual suspend fun connect(
            serverIp: String,
            serverPort: Int
        ): PlatformSocket {
            val r = sockets.socket_create_connect(serverIp.cstr, serverPort.toUShort())
            if (r < 0) error("Failed socket_create_connect: $r")
            return PlatformSocket(r)
//            val addr = nativeHeap.alloc<sockaddr_in>() {
//                sin_family = AF_INET.toUByte()
//                sin_addr.s_addr = resolveIpFromHost(serverIp).pointed.s_addr
//                sin_port = serverPort.toUInt().toUShort()
//            }
//
//            val id = socket(AF_INET, SOCK_STREAM, 0)
//            if (id == -1) throw PosixException.forErrno(posixFunctionName = "socket()")
//
//            println("connect")
//            val conn = connect(id, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().toUInt())
//            println("connect: $conn, $errno")
//            if (conn < 0) throw PosixException.forErrno(posixFunctionName = "connect()")
//
//            return PlatformSocket(conn)
        }

//        private fun resolveIpFromHost(serverIp: String): CPointer<in_addr> {
//            val host = gethostbyname(serverIp) // points to static data, don't free
//                ?: throw IllegalStateException("Failed to resolve IP from host. host=$serverIp")
//            println(host.pointed.h_addr_list?.get(1)?.reinterpret<in_addr>()?.pointed?.s_addr)
//            return host.pointed.h_addr_list?.get(1)?.reinterpret<in_addr>() ?: error("Failed to get ip")
////            val hAddrList = host.pointed.h_addr_list
////                ?: throw IllegalStateException("Empty IP list resolved from host. host=$serverIp")
////
////
////            val str = hAddrList[0]!!.reinterpret<UIntVar>()
////
////            try {
////                return str.pointed.value
////            } finally {
//////                free()
////            }
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
