/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.net.impl.netty

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.network.net.NetworkHandler
import net.mamoe.mirai.internal.network.net.NetworkHandlerContext
import net.mamoe.mirai.internal.network.net.protocol.PacketCodec
import net.mamoe.mirai.internal.network.net.protocol.RawIncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.childScope
import java.net.SocketAddress
import kotlin.coroutines.CoroutineContext

internal class NettyNetworkHandler(
    context: NetworkHandlerContext,
    private val address: SocketAddress,
) : NetworkHandlerSupport(context) {
    override suspend fun close() {
        super.close()
        setState(StateClosed())
    }

    override suspend fun sendPacketImpl(packet: OutgoingPacket) {
        val state = _state as NettyState
        state.sendPacketImpl(packet)
    }

    ///////////////////////////////////////////////////////////////////////////
    // netty conn.
    ///////////////////////////////////////////////////////////////////////////

    private inner class ByteBufToIncomingPacketDecoder : SimpleChannelInboundHandler<ByteBuf>(ByteBuf::class.java) {
        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
            ctx.fireChannelRead(msg.toReadPacket().use { packet ->
                PacketCodec.decodeRaw(context.bot.client, packet)
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

    private suspend fun createConnection(decodePipeline: PacketDecodePipeline): ChannelHandlerContext {
        val contextResult = CompletableDeferred<ChannelHandlerContext>()

        val eventLoopGroup = NioEventLoopGroup()
        Bootstrap().group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                        override fun channelActive(ctx: ChannelHandlerContext) {
                            contextResult.complete(ctx)
                        }
                    })
                        .addLast(LengthFieldBasedFrameDecoder(Int.MAX_VALUE, 0, 4, -4, 0))
                        .addLast(ByteBufToIncomingPacketDecoder())
                        .addLast(RawIncomingPacketCollector(decodePipeline))
                }
            })
            .connect(address).runBIO { await() }
        // TODO: 2021/4/14  eventLoopGroup 移动到 bot, 并在 bot.close() 时关闭

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

    private abstract inner class NettyState(
        correspondingState: NetworkHandler.State
    ) : BaseStateImpl(correspondingState) {
        abstract suspend fun sendPacketImpl(packet: OutgoingPacket)
    }

    private inner class StateInitialized : NettyState(NetworkHandler.State.INITIALIZED) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            error("Cannot send packet when connection is not set. (resumeConnection not called.)")
        }

        override suspend fun resumeConnection() {
            setState(StateConnecting(PacketDecodePipeline(this@NettyNetworkHandler.coroutineContext)))
        }
    }

    private inner class StateConnecting(
        val decodePipeline: PacketDecodePipeline,
    ) : NettyState(NetworkHandler.State.CONNECTING) {
        private val connection = async {
            createConnection(decodePipeline)
        }

        private val connectResult = async {
            val connection = connection.await()
            context.ssoController.login()
            setState(StateOK(connection))
        }.apply {
            invokeOnCompletion { error ->
                if (error != null) setState(StateClosed()) // logon failure closes the network handler.
            }
        }

        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            connection.await().writeAndFlush(packet)
        }

        override suspend fun resumeConnection() {
            connectResult.await() // propagates exceptions
        }
    }

    private inner class StateOK(
        private val connection: ChannelHandlerContext
    ) : NettyState(NetworkHandler.State.OK) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            connection.writeAndFlush(packet)
        }

        override suspend fun resumeConnection() {} // noop
    }

    private inner class StateClosed : NettyState(NetworkHandler.State.OK) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) = error("NetworkHandler is already closed.")
        override suspend fun resumeConnection() {} // noop
    }

    override fun initialState(): BaseStateImpl = StateInitialized()
}

private fun ByteBuf.toReadPacket(): ByteReadPacket {
    val buf = this
    return buildPacket {
        ByteBufInputStream(buf).withUse { copyTo(outputStream()) }
    }
}
