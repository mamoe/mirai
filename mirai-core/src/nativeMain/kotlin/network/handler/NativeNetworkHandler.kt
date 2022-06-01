/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.internal.network.components.PacketCodec
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.utils.PlatformSocket
import net.mamoe.mirai.internal.utils.connect
import net.mamoe.mirai.utils.*

internal class NativeNetworkHandler(
    context: NetworkHandlerContext,
    address: SocketAddress
) : CommonNetworkHandler<NativeNetworkHandler.NativeConn>(context, address) {
    internal object Factory : NetworkHandlerFactory<NativeNetworkHandler> {
        override fun create(context: NetworkHandlerContext, address: SocketAddress): NativeNetworkHandler {
            return NativeNetworkHandler(context, address)
        }
    }

    internal inner class NativeConn(
        private val socket: PlatformSocket,
    ) : Closeable, CoroutineScope by coroutineContext.childScope("NativeConn") {
        private val decodePipeline: PacketDecodePipeline = PacketDecodePipeline(this.coroutineContext)

        private val packetCodec: PacketCodec by lazy { context[PacketCodec] }
        private val ssoProcessor: SsoProcessor by lazy { context[SsoProcessor] }

        private val sendQueue: Channel<OutgoingPacket> = Channel(Channel.BUFFERED) { undelivered ->
            launch { write(undelivered) }
        }

        private val lengthDelimitedPacketReader = LengthDelimitedPacketReader()

        /**
         * Not thread-safe
         */
        private inner class LengthDelimitedPacketReader : Closeable {
            private var missingLength: Long = 0
            private val bufferedPackets: MutableList<ByteReadPacket> = ArrayList(10)

            fun offer(packet: ByteReadPacket) {
                if (missingLength == 0L) {
                    // initial
                    missingLength = packet.readInt().toLongUnsigned() - 4
                }
                missingLength -= packet.remaining
                bufferedPackets.add(packet)
                if (missingLength <= 0) {
                    emit()
                }
            }

            fun emit() {
                when (bufferedPackets.size) {
                    0 -> {}
                    1 -> {
                        val packet = bufferedPackets.first()
                        if (missingLength == 0L) {
                            sendDecode(packet)
                            bufferedPackets.clear()
                        } else {
                            check(missingLength < 0L) { "Failed check: remainingLength < 0L" }

                            val previousPacketLength = missingLength + packet.remaining
                            sendDecode(packet.readPacketExact(previousPacketLength.toInt()))

                            // now packet contain new packet.
                            missingLength = packet.readInt().toLongUnsigned() - 4
                            bufferedPackets[0] = packet
                        }
                    }
                    else -> {
                        val combined: ByteReadPacket
                        if (missingLength == 0L) {
                            combined = buildPacket(bufferedPackets.sumOf { it.remaining }.toInt()) {
                                bufferedPackets.forEach { writePacket(it) }
                            }

                            bufferedPackets.clear()
                        } else {
                            val lastPacket = bufferedPackets.last()
                            val previousPacketPartLength = missingLength + lastPacket.remaining
                            val combinedLength =
                                (bufferedPackets.sumOf { it.remaining } - lastPacket.remaining + previousPacketPartLength).toInt()

                            combined = buildPacket(combinedLength) {
                                repeat(bufferedPackets.size - 1) { i ->
                                    writePacket(bufferedPackets[i])
                                }
                                writePacket(lastPacket, previousPacketPartLength)
                            }

                            bufferedPackets.clear()

                            // now packet contain new packet.
                            missingLength = lastPacket.readInt().toLongUnsigned() - 4
                            bufferedPackets.add(lastPacket)
                        }

                        sendDecode(combined)
                    }
                }
            }

            private fun sendDecode(combined: ByteReadPacket) {
                packetLogger.verbose { "Decoding: len=${combined.remaining}" }
                val raw = packetCodec.decodeRaw(
                    ssoProcessor.ssoSession,
                    combined
                )
                packetLogger.verbose { "Decoded: ${raw.commandName}" }
                decodePipeline.send(
                    raw
                )
            }

            override fun close() {
                bufferedPackets.forEach { it.close() }
            }
        }

        private val sender = launch {
            while (isActive) {
                val result = sendQueue.receiveCatching()
                logger.info { "Native sender: $result" }
                result.onFailure { if (it is CancellationException) return@launch }

                result.getOrNull()?.let { packet ->
                    try {
                        socket.send(packet.delegate, 0, packet.delegate.size)
                    } catch (e: Throwable) {
                        if (e is CancellationException) return@launch
                        logger.error("Error while sending packet '${packet.commandName}'", e)
                    }
                }
            }
        }

        private val receiver = launch {
            while (isActive) {
                try {
                    val packet = socket.read()

                    lengthDelimitedPacketReader.offer(packet)
                } catch (e: Throwable) {
                    if (e is CancellationException) return@launch
                    logger.error("Error while reading packet.", e)
                }
            }
        }

        fun write(packet: OutgoingPacket) {
            sendQueue.trySend(packet).onFailure {
                throw it
                    ?: throw IllegalStateException("Failed to send packet '${packet.commandName}' without reason.")
            }
        }

        override fun close() {
            cancel()
        }
    }

    override suspend fun createConnection(): NativeConn {
        logger.info { "Connecting to $address" }
        return NativeConn(PlatformSocket.connect(address)).also {
            logger.info { "Connected to server $address" }
        }
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override fun NativeConn.close() {
        this.close()
    }

    override fun NativeConn.writeAndFlushOrCloseAsync(packet: OutgoingPacket) {
        write(packet)
    }
}