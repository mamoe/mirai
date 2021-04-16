/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.impl.netty

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.MessageToByteEncoder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.impl.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.net.protocol.PacketCodec
import net.mamoe.mirai.internal.network.net.protocol.RawIncomingPacket
import net.mamoe.mirai.internal.network.net.protocol.SsoProcessor
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.debug
import java.net.SocketAddress
import kotlin.coroutines.CoroutineContext
import io.netty.channel.Channel as NettyChannel

internal class NettyNetworkHandler(
    context: NetworkHandlerContext,
    private val address: SocketAddress,
) : NetworkHandlerSupport(context) {
    override fun close(cause: Throwable?) {
        setState { StateClosed(CancellationException("Closed manually.", cause)) }
        // wrap an exception, more stacktrace information
    }

    private fun closeSuper(cause: Throwable?) = super.close(cause)

    override suspend fun sendPacketImpl(packet: OutgoingPacket) {
        val state = _state as NettyState
        state.sendPacketImpl(packet)
    }

    override fun toString(): String {
        return "NettyNetworkHandler(context=$context, address=$address)"
    }

    ///////////////////////////////////////////////////////////////////////////
    // netty conn.
    ///////////////////////////////////////////////////////////////////////////

    private inner class ByteBufToIncomingPacketDecoder : SimpleChannelInboundHandler<ByteBuf>(ByteBuf::class.java) {
        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
            ctx.fireChannelRead(msg.toReadPacket().use { packet ->
                PacketCodec.decodeRaw(context.ssoProcessor.ssoSession, packet)
            })
        }
    }

    private inner class RawIncomingPacketCollector(
        private val decodePipeline: PacketDecodePipeline
    ) : SimpleChannelInboundHandler<RawIncomingPacket>(RawIncomingPacket::class.java) {
        override fun channelRead0(ctx: ChannelHandlerContext, msg: RawIncomingPacket) {
            decodePipeline.send(msg)
        }
    }

    private inner class OutgoingPacketEncoder : MessageToByteEncoder<OutgoingPacket>(OutgoingPacket::class.java) {
        override fun encode(ctx: ChannelHandlerContext, msg: OutgoingPacket, out: ByteBuf) {
            logger.debug { "encode: $msg" }
            out.writeBytes(msg.delegate)
        }
    }

    private suspend fun createConnection(decodePipeline: PacketDecodePipeline): NettyChannel {
        val contextResult = CompletableDeferred<NettyChannel>()
        val eventLoopGroup = NioEventLoopGroup()

        val future = Bootstrap().group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(object : ChannelInboundHandlerAdapter() {
                            override fun channelInactive(ctx: ChannelHandlerContext?) {
                                eventLoopGroup.shutdownGracefully()
                            }
                        })
                        .addLast(OutgoingPacketEncoder())
                        .addLast(LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 4, -4, 4))
                        .addLast(ByteBufToIncomingPacketDecoder())
                        .addLast(RawIncomingPacketCollector(decodePipeline))
                }
            })
            .connect(address)
            .awaitKt()

        contextResult.complete(future.channel())

        future.channel().closeFuture().addListener {
            setState { StateConnectionLost(it.cause()) }
        }

        return contextResult.await()
    }

    private inner class PacketDecodePipeline(parentContext: CoroutineContext) :
        CoroutineScope by parentContext.childScope() {
        private val channel: Channel<RawIncomingPacket> = Channel(Channel.BUFFERED)

        init {
            launch(CoroutineName("PacketDecodePipeline processor")) {
                // 'single thread' processor
                channel.consumeAsFlow().collect { raw ->
                    val result = PacketCodec.processBody(context.bot, raw)
                    if (result == null) {
                        collectUnknownPacket(raw)
                    } else collectReceived(result)
                }
            }
        }

        fun send(raw: RawIncomingPacket) = channel.sendBlocking(raw)
    }


    ///////////////////////////////////////////////////////////////////////////
    // states
    ///////////////////////////////////////////////////////////////////////////

    /**
     * When state is initialized, it must be set to [_state]. (inside [setState])
     */
    private abstract inner class NettyState(
        correspondingState: NetworkHandler.State
    ) : BaseStateImpl(correspondingState) {
        abstract suspend fun sendPacketImpl(packet: OutgoingPacket)
    }

    private inner class StateInitialized : NettyState(NetworkHandler.State.INITIALIZED) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            error("Cannot send packet when connection is not set. (resumeConnection not called.)")
        }

        override suspend fun resumeConnection0() {
            setState { StateConnecting(PacketDecodePipeline(this@NettyNetworkHandler.coroutineContext)) }
                .resumeConnection()
        }

        override fun toString(): String = "StateInitialized"
    }

    /**
     * 1. Connect to server.
     * 2. Perform SSO login with [SsoProcessor]
     * 3. If failure, set state to [StateClosed]
     * 4. If success, set state to [StateOK]
     */
    private inner class StateConnecting(
        val decodePipeline: PacketDecodePipeline,
    ) : NettyState(NetworkHandler.State.CONNECTING) {
        private val connection = async { createConnection(decodePipeline) }

        private val connectResult = async {
            val connection = connection.await()
            context.ssoProcessor.login(this@NettyNetworkHandler)
            setStateForJobCompletion { StateOK(connection) }
        }.apply {
            invokeOnCompletion { error ->
                if (error != null) setState {
                    StateClosed(
                        CancellationException("Connection failure.", error)
                    )
                } // logon failure closes the network handler.
                // and this error will also be thrown by `StateConnecting.resumeConnection`
            }
        }

        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            connection.await() // split line number
                .writeAndFlush(packet)
        }

        override suspend fun resumeConnection0() {
            connectResult.await() // propagates exceptions
        }

        override fun toString(): String = "StateConnecting"
    }

    private inner class StateOK(
        private val connection: NettyChannel
    ) : NettyState(NetworkHandler.State.OK) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            connection.writeAndFlush(packet)
        }

        override suspend fun resumeConnection0() {} // noop
        override fun toString(): String = "StateOK"
    }

    private inner class StateConnectionLost(
        private val cause: Throwable
    ) : NettyState(NetworkHandler.State.CONNECTION_LOST) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            throw IllegalStateException("Connection is lost so cannot send packet. Call resumeConnection first.", cause)
        }

        override suspend fun resumeConnection0() {
            setState { StateConnecting(PacketDecodePipeline(this@NettyNetworkHandler.coroutineContext)) }
                .resumeConnection() // the user wil
        } // noop
    }

    private inner class StateClosed(
        val exception: Throwable?
    ) : NettyState(NetworkHandler.State.CLOSED) {
        init {
            closeSuper(exception)
        }

        override suspend fun sendPacketImpl(packet: OutgoingPacket) = error("NetworkHandler is already closed.")
        override suspend fun resumeConnection0() {
            exception?.let { throw it }
        } // noop

        override fun toString(): String = "StateClosed"
    }

    override fun initialState(): BaseStateImpl = StateInitialized()
}
