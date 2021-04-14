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
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import net.mamoe.mirai.internal.network.net.NetworkHandler
import net.mamoe.mirai.internal.network.net.NetworkHandlerContext
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import java.net.SocketAddress

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
            setState(StateConnecting())
        }
    }

    private inner class StateConnecting : NettyState(NetworkHandler.State.CONNECTING) {
        private val connection = async {
            createConnection()
        }

        private val connectResult = async {
            val connection = connection.await()
            context.ssoProtocol.login(this@NettyNetworkHandler)
            setState(StateOK(connection))
        }.apply {
            invokeOnCompletion { error ->
                if (error != null) setState(StateClosed())
            }
        }

        override suspend fun sendPacketImpl(packet: OutgoingPacket) =
            error("Cannot send packet when connection is not set.")

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

    ///////////////////////////////////////////////////////////////////////////
    // netty conn.
    ///////////////////////////////////////////////////////////////////////////

    private fun setupChannelContext(ctx: ChannelHandlerContext) {
        // TODO: 2021/4/14 decoders

        // incoming collector, last.
        ctx.channel().pipeline().addLast(object : ChannelInboundHandlerAdapter() {
            override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
                if (msg is IncomingPacket) {
                    collectReceived(msg)
                }
            }
        })
    }

    private suspend fun createConnection(): ChannelHandlerContext {
        val contextResult = CompletableDeferred<ChannelHandlerContext>()

        val eventLoopGroup = NioEventLoopGroup()
        val bootstrap = Bootstrap()
        bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                        override fun channelActive(ctx: ChannelHandlerContext) {
                            contextResult.complete(ctx)
                            setupChannelContext(ctx)
                        }
                    })
                }
            })
        bootstrap.connect(address)
        return contextResult.await()
    }
}