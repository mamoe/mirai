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
import kotlinx.coroutines.*
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.handler.selector.NetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.*
import java.io.EOFException
import java.net.SocketAddress
import kotlin.coroutines.CoroutineContext
import io.netty.channel.Channel as NettyChannel

internal open class NettyNetworkHandler(
    context: NetworkHandlerContext,
    private val address: SocketAddress,
) : NetworkHandlerSupport(context) {
    final override tailrec suspend fun sendPacketImpl(packet: OutgoingPacket) {
        val state = _state as NettyState
        if (state.sendPacketImpl(packet)) return

        // now the state it not yet ready for sending packet ...
        stateChannel.receive() // [SUSPENSION POINT] so we wait for next state ...
        return sendPacketImpl(packet) // and try again.
    }

    override fun toString(): String {
        return "NettyNetworkHandler(context=$context, address=$address)"
    }


    ///////////////////////////////////////////////////////////////////////////
    // exception handling
    ///////////////////////////////////////////////////////////////////////////
    protected open fun handleExceptionInDecoding(error: Throwable) {
        fun passToExceptionHandler() {
            // Typically, just log the exception
            coroutineContext[CoroutineExceptionHandler]!!.handleException(
                coroutineContext,
                ExceptionInPacketCodecException(error.unwrap<PacketCodecException>())
            )
        }

        if (error is PacketCodecException) {
            if (error.targetException is EOFException) return
            when (error.kind) {
                PacketCodecException.Kind.SESSION_EXPIRED -> {
                    setState { StateClosed(error) }
                    return
                }
                PacketCodecException.Kind.PROTOCOL_UPDATED -> passToExceptionHandler()
                PacketCodecException.Kind.OTHER -> passToExceptionHandler()
            }
        }

        passToExceptionHandler()
    }

    protected open fun handlePipelineException(ctx: ChannelHandlerContext, error: Throwable) {
        setState { StateClosed(NettyChannelException(cause = error)) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // netty conn.
    ///////////////////////////////////////////////////////////////////////////

    private inner class ByteBufToIncomingPacketDecoder : SimpleChannelInboundHandler<ByteBuf>(ByteBuf::class.java) {
        private val packetCodec: PacketCodec by lazy { context[PacketCodec] }
        private val ssoProcessor: SsoProcessor by lazy { context[SsoProcessor] }

        override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
            kotlin.runCatching {
                ctx.fireChannelRead(msg.toReadPacket().use { packet ->
                    packetCodec.decodeRaw(ssoProcessor.ssoSession, packet)
                })
            }.onFailure { error ->
                handleExceptionInDecoding(error)
            }
        }
    }

    private inner class RawIncomingPacketCollector(
        private val decodePipeline: PacketDecodePipeline,
    ) : SimpleChannelInboundHandler<RawIncomingPacket>(RawIncomingPacket::class.java) {
        override fun channelRead0(ctx: ChannelHandlerContext, msg: RawIncomingPacket) {
            decodePipeline.send(msg)
        }
    }

    private inner class OutgoingPacketEncoder : MessageToByteEncoder<OutgoingPacket>(OutgoingPacket::class.java) {
        override fun encode(ctx: ChannelHandlerContext, msg: OutgoingPacket, out: ByteBuf) {
            packetLogger.debug { "encode: ${msg.displayName}" }
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
            .addLast(ByteBufToIncomingPacketDecoder())
            .addLast("raw-packet-collector", RawIncomingPacketCollector(decodePipeline))
    }

    protected open fun createDummyDecodePipeline() = PacketDecodePipeline(this@NettyNetworkHandler.coroutineContext)

    // can be overridden for tests
    protected open suspend fun createConnection(): NettyChannel {
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
            }.getOrThrow()

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

    protected inner class PacketDecodePipeline(parentContext: CoroutineContext) :
        CoroutineScope by parentContext.childScope() {
        private val packetCodec: PacketCodec by lazy { context[PacketCodec] }

        fun send(raw: RawIncomingPacket) {
            launch {
                packetLogger.debug { "Packet Handling Processor: receive packet ${raw.commandName}" }
                val result = packetCodec.processBody(context.bot, raw)
                if (result == null) {
                    collectUnknownPacket(raw)
                } else collectReceived(result)
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // states
    ///////////////////////////////////////////////////////////////////////////

    override fun close(cause: Throwable?) {
        if (state == State.CLOSED) return // quick check if already closed
        if (setState { StateClosed(cause) } == null) return // atomic check
        super.close(cause) // cancel coroutine scope
    }

    init {
        coroutineContext.job.invokeOnCompletion { e ->
            close(e?.unwrapCancellationException())
        }
    }

    /**
     * When state is initialized, it must be set to [_state]. (inside [setState])
     *
     * For what jobs each state will do, it is not solely decided by the state itself. [StateObserver]s may also launch jobs into the scope.
     *
     * @see StateObserver
     */
    protected abstract inner class NettyState(
        correspondingState: State,
    ) : BaseStateImpl(correspondingState) {
        /**
         * @return `true` if packet has been sent, `false` if state is not ready for send.
         * @throws IllegalStateException if is [StateClosed].
         */
        abstract suspend fun sendPacketImpl(packet: OutgoingPacket): Boolean
    }

    protected inner class StateInitialized : NettyState(State.INITIALIZED) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket): Boolean {
            //            error("Cannot send packet when connection is not set. (resumeConnection not called.)")
            return false
        }

        override suspend fun resumeConnection0() {
            this.setState { StateConnecting(ExceptionCollector()) }
                ?.resumeConnection()
                ?: this@NettyNetworkHandler.resumeConnection() // concurrently closed by other thread.
        }

        override fun toString(): String = "StateInitialized"
    }

    /**
     * 1. Connect to server.
     * 2. Perform SSO login with [SsoProcessor]
     *
     * If failure, set state to [StateClosed]
     * If success, set state to [StateOK]
     */
    protected inner class StateConnecting(
        /**
         * Collected (suppressed) exceptions that have led this state.
         *
         * Dropped when state becomes [StateOK].
         */
        private val collectiveExceptions: ExceptionCollector,
    ) : NettyState(State.CONNECTING) {
        private lateinit var connection: Deferred<io.netty.channel.Channel>

        @Suppress("JoinDeclarationAndAssignment")
        private lateinit var connectResult: Deferred<Unit>

        override fun startState() {
            connection = async {
                createConnection()
            }

            connectResult = async {
                connection.join()
                context[SsoProcessor].login(this@NettyNetworkHandler)
            }
            connectResult.invokeOnCompletion { error ->
                if (error == null) {
                    this@NettyNetworkHandler.launch { resumeConnection() }
                } else {
                    // failed in SSO stage
                    context[SsoProcessor].firstLoginResult.compareAndSet(null, FirstLoginResult.OTHER_FAILURE)

                    if (error is StateSwitchingException && error.new is StateConnecting) {
                        return@invokeOnCompletion // state already switched, so do not do it again.
                    }
                    setState {
                        // logon failure closes the network handler.
                        StateClosed(collectiveExceptions.collectGet(error))
                        // The exception will be ignored unless all further attempts recovering connection have failed.
                        // This is to reduce useless logs for the user----there is nothing to worry about if we can recover the connection.
                    }
                }
            }

        }

        override fun getCause(): Throwable? = collectiveExceptions.getLast()

        override suspend fun sendPacketImpl(packet: OutgoingPacket): Boolean = runUnwrapCancellationException {
            connection.await() // split line number
                .writeAndFlushOrCloseAsync(packet)
            return true
        }

        override suspend fun resumeConnection0() = runUnwrapCancellationException {
            connectResult.await() // propagates exceptions
            val connection = connection.await()
            this.setState { StateLoading(connection) }
                ?.resumeConnection()
                ?: this@NettyNetworkHandler.resumeConnection() // concurrently closed by other thread.
        }

        override fun toString(): String = "StateConnecting"
    }

    /**
     * @see BotInitProcessor
     * @see StateObserver
     */
    protected inner class StateLoading(
        private val connection: NettyChannel,
    ) : NettyState(State.LOADING) {

        override fun startState() {
            coroutineContext.job.invokeOnCompletion {
                if (it != null) {
                    connection.close()
                }
            }
        }

        override suspend fun sendPacketImpl(packet: OutgoingPacket): Boolean {
            connection.writeAndFlushOrCloseAsync(packet)
            return true
        }

        private val configPush = this@NettyNetworkHandler.launch(CoroutineName("ConfigPush sync")) {
            context[ConfigPushProcessor].syncConfigPush(this@NettyNetworkHandler)
        }

        override suspend fun resumeConnection0(): Unit = runUnwrapCancellationException {
            (coroutineContext.job as CompletableJob).run {
                complete()
                join()
            }
            joinCompleted(configPush) // throw exception
            setState { StateOK(connection, configPush) }
        } // noop

        override fun toString(): String = "StateLoading"
    }

    protected inner class StateOK(
        private val connection: NettyChannel,
        private val configPush: Job,
    ) : NettyState(State.OK) {
        override fun startState() {
            coroutineContext.job.invokeOnCompletion { err ->
                if (err is StateSwitchingException) {
                    if (err.new.correspondingState == State.CLOSED) {
                        return@invokeOnCompletion
                    }
                }
                connection.close()
            }
        }

        private val heartbeatJobs =
            context[HeartbeatScheduler].launchJobsIn(this@NettyNetworkHandler, this) { name, e ->
                setState { StateClosed(HeartbeatFailedException(name, e)) }
            }

        // we can also move them as observers if needed.

        private val keyRefresh = launch(CoroutineName("Key refresh")) {
            context[KeyRefreshProcessor].keyRefreshLoop(this@NettyNetworkHandler)
        }

        override suspend fun sendPacketImpl(packet: OutgoingPacket): Boolean {
            connection.writeAndFlushOrCloseAsync(packet)
            return true
        }

        override suspend fun resumeConnection0(): Unit = runUnwrapCancellationException {
            joinCompleted(coroutineContext.job)
            for (job in heartbeatJobs) joinCompleted(job)
            joinCompleted(configPush)
            joinCompleted(keyRefresh)
        } // noop

        override fun toString(): String = "StateOK"
    }

    /**
     * 这会永久关闭这个 [NettyNetworkHandler], 但通常 bot 会使用 [NetworkHandlerSelector], selector 会创建新的 [NettyNetworkHandler] 来恢复连接.
     *
     * 备注: selector 会恢复连接, 当且仅当 [exception] 类型是 [NetworkException] 且 [NetworkException.recoverable] 为 `true`.
     */
    protected inner class StateClosed(
        val exception: Throwable?,
    ) : NettyState(State.CLOSED) {

        override fun afterUpdated() {
            close(exception)
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
