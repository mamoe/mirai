/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.handler.selector.NetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.impl.HeartbeatFailedException
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext

/**
 * Implements protocol-specific logic based on [NetworkHandlerSupport]. This can be shared between tests and
 */
internal abstract class CommonNetworkHandler<Conn>(
    context: NetworkHandlerContext,
    protected val address: SocketAddress,
) : NetworkHandlerSupport(context) {
    final override tailrec suspend fun sendPacketImpl(packet: OutgoingPacket) {
        val state = _state as CommonNetworkHandler<*>.CommonState
        if (state.sendPacketImpl(packet)) return

        // now the state it not yet ready for sending packet ...
        stateChannel.receive() // [SUSPENSION POINT] so we wait for next state ...
        return sendPacketImpl(packet) // and try again.
    }

    override fun toString(): String {
        return "CommonNetworkHandler(context=$context, address=$address)"
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

    ///////////////////////////////////////////////////////////////////////////
    // conn.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a connection
     */
    protected abstract suspend fun createConnection(): Conn

    /**
     * Writes and flushes the packet asynchronously.
     */
    protected abstract fun Conn.writeAndFlushOrCloseAsync(packet: OutgoingPacket)

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    protected abstract fun Conn.close()

    internal inner class PacketDecodePipeline(parentContext: CoroutineContext) :
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
        if (state == NetworkHandler.State.CLOSED) return // quick check if already closed
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
    protected abstract inner class CommonState(
        correspondingState: NetworkHandler.State,
    ) : NetworkHandlerSupport.BaseStateImpl(correspondingState) {
        /**
         * @return `true` if packet has been sent, `false` if state is not ready for send.
         * @throws IllegalStateException if is [StateClosed].
         */
        abstract suspend fun sendPacketImpl(packet: OutgoingPacket): Boolean
    }

    protected inner class StateInitialized : CommonState(NetworkHandler.State.INITIALIZED) {
        override suspend fun sendPacketImpl(packet: OutgoingPacket): Boolean {
            //            error("Cannot send packet when connection is not set. (resumeConnection not called.)")
            return false
        }

        override suspend fun resumeConnection0() {
            this.setState { StateConnecting(ExceptionCollector()) }
                ?.resumeConnection()
                ?: this@CommonNetworkHandler.resumeConnection() // concurrently closed by other thread.

            println("INITIALIZED RETURN")
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
    ) : CommonState(NetworkHandler.State.CONNECTING) {
        private lateinit var connection: Deferred<Conn>

        @Suppress("JoinDeclarationAndAssignment")
        private lateinit var connectResult: Deferred<Unit>

        override fun startState() {
            connection = async {
                createConnection()
            }

            connectResult = async {
                connection.join()
                context[SsoProcessor].login(this@CommonNetworkHandler)
            }
            connectResult.invokeOnCompletion { error ->
                if (error == null) {
                    this@CommonNetworkHandler.launch { resumeConnection() }
                } else {
                    // failed in SSO stage
                    context[SsoProcessor].firstLoginResult.compareAndSet(null, FirstLoginResult.OTHER_FAILURE)

                    if (error is StateSwitchingException && error.new is CommonNetworkHandler<*>.StateConnecting) {
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
                .also {
                    println(" this.setState { StateLoading(connection) }: " + it)
                }
                ?.resumeConnection()
                ?: this@CommonNetworkHandler.resumeConnection() // concurrently closed by other thread.
        }

        override fun toString(): String = "StateConnecting"
    }

    /**
     * @see BotInitProcessor
     * @see StateObserver
     */
    protected inner class StateLoading(
        private val connection: Conn,
    ) : CommonState(NetworkHandler.State.LOADING) {

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

        private val configPush = this@CommonNetworkHandler.launch(CoroutineName("ConfigPush sync")) {
            context[ConfigPushProcessor].syncConfigPush(this@CommonNetworkHandler)
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
        private val connection: Conn,
        private val configPush: Job,
    ) : CommonState(NetworkHandler.State.OK) {
        override fun startState() {
            coroutineContext.job.invokeOnCompletion { err ->
                if (err is StateSwitchingException) {
                    if (err.new.correspondingState == NetworkHandler.State.CLOSED) {
                        return@invokeOnCompletion
                    }
                }
                connection.close()
            }
        }

        private val heartbeatJobs =
            context[HeartbeatScheduler].launchJobsIn(this@CommonNetworkHandler, this) { name, e ->
                setState { StateClosed(HeartbeatFailedException(name, e)) }
            }

        // we can also move them as observers if needed.

        private val keyRefresh = launch(CoroutineName("Key refresh")) {
            context[KeyRefreshProcessor].keyRefreshLoop(this@CommonNetworkHandler)
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
     * 这会永久关闭这个 [NetworkHandler], 但通常 bot 会使用 [NetworkHandlerSelector], selector 会创建新的 [NetworkHandler] 来恢复连接.
     *
     * 备注: selector 会恢复连接, 当且仅当 [exception] 类型是 [NetworkException] 且 [NetworkException.recoverable] 为 `true`.
     */
    protected inner class StateClosed(
        val exception: Throwable?,
    ) : CommonState(NetworkHandler.State.CLOSED) {

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

    override fun initialState(): NetworkHandlerSupport.BaseStateImpl = StateInitialized()

}

internal suspend inline fun joinCompleted(job: Job) {
    if (job.isCompleted) job.join()
}
