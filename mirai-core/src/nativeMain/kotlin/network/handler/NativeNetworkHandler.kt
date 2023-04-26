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
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.info

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
        internal val socket: PlatformSocket,
    ) : Closeable, CoroutineScope by coroutineContext.childScope("NativeConn") {
        private val decodePipeline: PacketDecodePipeline = PacketDecodePipeline(this.coroutineContext)

        private val packetCodec: PacketCodec by lazy { context[PacketCodec] }
        private val ssoProcessor: SsoProcessor by lazy { context[SsoProcessor] }

        private val sendQueue: Channel<OutgoingPacket> = Channel(Channel.BUFFERED) { undelivered ->
            launch { write(undelivered) }
        }

        private val lengthDelimitedPacketReader = LengthDelimitedPacketReader(decodePipeline::send)

        init {
            launch {
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

            launch {
                while (isActive) {
                    try {
                        val packet = socket.read()

                        lengthDelimitedPacketReader.offer(packet)
                    } catch (e: Throwable) {
                        if (e is CancellationException) return@launch
                        logger.error("Error while reading packet.", e)
                        setState { StateClosed(e) }
                    }
                }
            }
        }

        fun write(packet: OutgoingPacket) {
            sendQueue.trySend(packet).onFailure {
                throw it
                    ?: throw IllegalStateException("Internal error: Failed to send packet '${packet.commandName}' without reason.")
            }
        }

        override fun close() {
            cancel()
            sendQueue.close()
        }
    }

    override suspend fun createConnection(): NativeConn {
        logger.info { "Connecting to $address" }
        return NativeConn(PlatformSocket.connect(address)).also {
            logger.info { "Connected to server $address" }
        }
    }

    override fun NativeConn.getConnectedIP(): Long {
        return this.socket.connectedIp
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override fun NativeConn.close() {
        this.close()
    }

    override fun NativeConn.writeAndFlushOrCloseAsync(packet: OutgoingPacket) {
        write(packet)
    }
}