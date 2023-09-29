/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.MessageToByteEncoder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.job
import net.mamoe.mirai.internal.network.handler.CommonNetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.debug
import java.net.SocketAddress
import io.netty.channel.Channel as NettyChannel

internal open class NettyNetworkHandler(
    context: NetworkHandlerContext,
    address: SocketAddress,
) : CommonNetworkHandler<NettyChannel>(context, address.cast()) {
    override fun toString(): String {
        return "NettyNetworkHandler(context=$context, address=$address)"
    }


    ///////////////////////////////////////////////////////////////////////////
    // exception handling
    ///////////////////////////////////////////////////////////////////////////

    protected open fun handlePipelineException(ctx: ChannelHandlerContext, error: Throwable) {
        setState { StateClosed(NettyChannelException(cause = error, message = "An unexpected exception was received from netty pipeline. (context=$context, address=$address)")) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // netty conn.
    ///////////////////////////////////////////////////////////////////////////

    private inner class IncomingPacketDecoder(
        private val decodePipeline: PacketDecodePipeline,
    ) : SimpleChannelInboundHandler<ByteBuf>(ByteBuf::class.java) {
        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
            decodePipeline.send(msg.toReadPacket())
        }
    }

    private inner class OutgoingPacketEncoder : MessageToByteEncoder<OutgoingPacket>(OutgoingPacket::class.java) {
        override fun encode(ctx: ChannelHandlerContext, msg: OutgoingPacket, out: ByteBuf) {
            out.writeBytes(msg.delegate)
        }
    }

    protected open fun setupChannelPipeline(pipeline: ChannelPipeline, decodePipeline: PacketDecodePipeline) {
        pipeline
            .addLast(object : ChannelInboundHandlerAdapter() {
                @Suppress("OVERRIDE_DEPRECATION")
                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                    handlePipelineException(ctx, cause)
                }
            })
            .addLast("outgoing-packet-encoder", OutgoingPacketEncoder())
            .addLast(LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 4, -4, 4))
            .addLast(IncomingPacketDecoder(decodePipeline))
    }

    protected open fun createDummyDecodePipeline() = PacketDecodePipeline(this@NettyNetworkHandler.coroutineContext)

    // can be overridden for tests
    override suspend fun createConnection(): NettyChannel {
        packetLogger.debug { "Connecting to $address" }

        val contextResult = CompletableDeferred<NettyChannel>()
        val eventLoopGroup = NioEventLoopGroup()
        val decodePipeline = PacketDecodePipeline(
            this@NettyNetworkHandler.coroutineContext
                .plus(eventLoopGroup.asCoroutineDispatcher())
        )

        val future = Bootstrap().group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    setupChannelPipeline(ch.pipeline(), decodePipeline)
                    ch.pipeline()
                        .addLast(object : ChannelInboundHandlerAdapter() {
                            override fun channelInactive(ctx: ChannelHandlerContext?) {
                                eventLoopGroup.shutdownGracefully()
                                contextResult.cancel()
                            }
                        })

                }
            })
            .connect(address)
            .runCatching {
                awaitKt()
            }.onFailure {
                eventLoopGroup.shutdownGracefully()
                contextResult.cancel()
            }.getOrElse { error ->
                throw NettyChannelException(cause = error, message = "Failed to connect $address")
            }

        contextResult.complete(future.channel())

        coroutineContext.job.invokeOnCompletion {
            future.channel().close()
            eventLoopGroup.shutdownGracefully()
        }

        future.channel().closeFuture().addListener {
            if (_state.correspondingState == State.CLOSED) return@addListener
            close(NettyChannelException(cause = it.cause()))
        }

        return contextResult.await()
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override fun io.netty.channel.Channel.close() {
        this.close()
    }

    override fun NettyChannel.writeAndFlushOrCloseAsync(packet: OutgoingPacket) {
        writeAndFlush(packet)
            .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
            .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
    }
}
