/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.context.SsoProcessorContext
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.ExceptionCollector
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
        private val packetCodec: PacketCodec by lazy { context[PacketCodec] }
        private val ssoProcessor: SsoProcessor by lazy { context[SsoProcessor] }

        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
            ctx.fireChannelRead(msg.toReadPacket().use { packet ->
                packetCodec.decodeRaw(ssoProcessor.ssoSession, packet)
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
            PacketCodec.PacketLogger.debug { "encode: $msg" }
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

        coroutineContext.job.invokeOnCompletion {
            future.channel().close()
            eventLoopGroup.shutdownGracefully()
        }

        future.channel().closeFuture().addListener {
            if (_state.correspondingState == State.CLOSED) return@addListener
            setState { StateConnecting(ExceptionCollector(it.cause())) }
        }

        return contextResult.await()
    }

    private val decodePipeline = PacketDecodePipeline(this@NettyNetworkHandler.coroutineContext)

    private inner class PacketDecodePipeline(parentContext: CoroutineContext) :
        CoroutineScope by parentContext.childScope() {
        private val channel: Channel<RawIncomingPacket> = Channel(Channel.BUFFERED)
        private val packetCodec: PacketCodec by lazy { context[PacketCodec] }

        init {
            launch(CoroutineName("PacketDecodePipeline processor")) {
                // 'single thread' processor
                channel.consumeAsFlow().collect { raw ->
                    val result = packetCodec.processBody(context.bot, raw)
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
     *
     * For what jobs each state will do, it is not solely decided by the state itself. [StateObserver]s may also launch jobs into the scope.
     *
     * @see StateObserver
     */
    private abstract inner class NettyState(
        correspondingState: State
    ) : BaseStateImpl(correspondingState) {
        abstract suspend fun sendPacketImpl(packet: OutgoingPacket)
    }

    private inner class StateInitialized : NettyState(State.INITIALIZED) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            error("Cannot send packet when connection is not set. (resumeConnection not called.)")
        }

        override suspend fun resumeConnection0() {
            setState { StateConnecting(ExceptionCollector()) }
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
        /**
         * Collected (suppressed) exceptions that have led this state.
         *
         * Dropped when state becomes [StateOK].
         */
        private val collectiveExceptions: ExceptionCollector,
        wait: Boolean = false
    ) : NettyState(State.CONNECTING) {
        private val connection = async {
            if (wait) {
                delay(5000)
            }
            createConnection(decodePipeline)
        }

        private val connectResult = async {
            connection.join()
            context[SsoProcessor].login(this@NettyNetworkHandler)
        }.apply {
            invokeOnCompletion { error ->
                if (error != null) {
                    setState {
                        StateConnecting(
                            collectiveExceptions.apply { collect(error) },
                            wait = true
                        )
                    } // logon failure closes the network handler.
                }
                // and this error will also be thrown by `StateConnecting.resumeConnection`
            }
        }

        override fun getCause(): Throwable? = collectiveExceptions.getLast()

        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            connection.await() // split line number
                .writeAndFlush(packet)
        }

        override suspend fun resumeConnection0() {
            connectResult.await() // propagates exceptions
            val connection = connection.await()
            setState { StateLoading(connection) }
                .resumeConnection()
        }

        override fun toString(): String = "StateConnecting"
    }

    /**
     * @see BotInitProcessor
     * @see StateObserver
     */
    private inner class StateLoading(
        private val connection: NettyChannel
    ) : NettyState(State.LOADING) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            connection.writeAndFlush(packet)
        }

        override suspend fun resumeConnection0() {
            (coroutineContext.job as CompletableJob).run {
                complete()
                join()
            }
            setState { StateOK(connection) }
        } // noop

        override fun toString(): String = "StateLoading"
    }

    private inner class StateOK(
        private val connection: NettyChannel
    ) : NettyState(State.OK) {
        init {
            coroutineContext.job.invokeOnCompletion {
                connection.close()
            }
        }

        private val heartbeatProcessor = context[HeartbeatProcessor]

        private val heartbeat = async(CoroutineName("Heartbeat Scheduler")) {
            while (isActive) {
                try {
                    delay(context[SsoProcessorContext].configuration.heartbeatPeriodMillis)
                } catch (e: CancellationException) {
                    return@async // considered normally cancel
                }

                try {
                    heartbeatProcessor.doHeartbeatNow(this@NettyNetworkHandler)
                } catch (e: Throwable) {
                    setState {
                        StateConnecting(ExceptionCollector(IllegalStateException("Exception in Heartbeat job", e)))
                    }
                }
            }
        }

        private val configPush = launch(CoroutineName("ConfigPush sync")) {
            try {
                context[ConfigPushProcessor].syncConfigPush(this@NettyNetworkHandler)
            } catch (e: ConfigPushProcessor.RequireReconnectException) {
                setState { StateClosed(e) }
            }
        }

        // we can also move them as observers if needed.

        private val keyRefresh = launch(CoroutineName("Key refresh")) {
            context[KeyRefreshProcessor].keyRefreshLoop(this@NettyNetworkHandler)
        }

        override suspend fun sendPacketImpl(packet: OutgoingPacket) {
            connection.writeAndFlush(packet)
        }

        override suspend fun resumeConnection0() {
            joinCompleted(coroutineContext.job)
            joinCompleted(heartbeat)
            joinCompleted(configPush)
            joinCompleted(keyRefresh)
        } // noop

        private suspend inline fun joinCompleted(job: Job) {
            if (job.isCompleted) job.join()
        }

        override fun toString(): String = "StateOK"
    }

    private inner class StateClosed(
        val exception: Throwable?
    ) : NettyState(State.CLOSED) {
        init {
            closeSuper(exception)
        }

        override fun getCause(): Throwable? = exception
        override suspend fun sendPacketImpl(packet: OutgoingPacket) = error("NetworkHandler is already closed.")
        override suspend fun resumeConnection0() {
            exception?.let { throw it }
        } // noop

        override fun toString(): String = "StateClosed"
    }

    override fun initialState(): BaseStateImpl = StateInitialized()
}
