/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.*
import kotlinx.coroutines.selects.SelectClause1
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.PacketCodec
import net.mamoe.mirai.internal.network.components.PacketHandler
import net.mamoe.mirai.internal.network.components.PacketLoggingStrategy
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * Implements basic logics of [NetworkHandler]
 */
internal abstract class NetworkHandlerSupport(
    override val context: NetworkHandlerContext,
    final override val coroutineContext: CoroutineContext = SupervisorJob(),
) : NetworkHandler, CoroutineScope by coroutineContext.childScope(SupervisorJob()) {

    protected abstract fun initialState(): BaseStateImpl
    protected abstract suspend fun sendPacketImpl(packet: OutgoingPacket)

    private val packetHandler: PacketHandler by lazy { context[PacketHandler] }

    /**
     * Called when a packet is received.
     */
    protected open fun collectReceived(packet: IncomingPacket) {
        for (listener in packetListeners) {
            if (!listener.isExpected(packet)) continue
            if (packetListeners.remove(listener)) {
                val e = packet.exception
                if (e != null) {
                    listener.result.completeExceptionally(e)
                } else {
                    listener.result.complete(packet.data)
                }
            }
        }
        launch {
            try {
                packetHandler.handlePacket(packet)
            } catch (e: Throwable) { // do not pass it to CoroutineExceptionHandler for a more controllable behavior.
                logger.error(e)
            }
        }
    }

    protected fun collectUnknownPacket(raw: RawIncomingPacket) {
        packetLogger.debug { "Unknown packet: commandName=${raw.commandName}, body=${raw.body.toUHexString()}" }
        // may add hooks here (to context)
    }

    final override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int): Packet? {
        val listener = PacketListener(packet.commandName, packet.sequenceId)
        withExceptionCollector {
            context[PacketLoggingStrategy].logSent(logger, packet)
            try {
                packetListeners.add(listener)
                sendPacketImpl(packet)
                try {
                    return withTimeout(timeout) {
                        listener.result.await()
                    }
                } catch (e: TimeoutCancellationException) {
                    collectException(e)
                }
            } finally {
                listener.result.completeExceptionally(getLast() ?: IllegalStateException("No response"))
                packetListeners.remove(listener)
            }
            throwLast()
        }
    }

    final override suspend fun sendWithoutExpect(packet: OutgoingPacket) {
        context[PacketLoggingStrategy].logSent(logger, packet)
        sendPacketImpl(packet)
    }

    override fun close(cause: Throwable?) {
//        if (cause == null) {
//            logger.info { "NetworkHandler '$this' closed" }
//        } else {
//            logger.info { "NetworkHandler '$this' closed: $cause" }
//        }
        coroutineContext.job.cancel("NetworkHandler closed", cause)
    }

    protected val packetLogger: MiraiLogger by lazy {
        MiraiLogger.create(context.logger.identity + ".debug").withSwitch(PacketCodec.PACKET_DEBUG)
    }

    ///////////////////////////////////////////////////////////////////////////
    // await impl
    ///////////////////////////////////////////////////////////////////////////

    protected class PacketListener(
        val commandName: String,
        val sequenceId: Int,
    ) {
        /**
         * Response from server. May complete with [CompletableDeferred.completeExceptionally] for a meaningful stacktrace.
         */
        val result = CompletableDeferred<Packet?>()

        fun isExpected(packet: IncomingPacket): Boolean =
            this.commandName == packet.commandName && this.sequenceId == packet.sequenceId
    }

    private val packetListeners = ConcurrentLinkedQueue<PacketListener>()

    ///////////////////////////////////////////////////////////////////////////
    // state impl
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A **scoped** state corresponding to [NetworkHandler.State].
     *
     * CoroutineScope is cancelled when switched to another state.
     *
     * State can only be changed inside [setState].
     */
    abstract inner class BaseStateImpl(
        val correspondingState: NetworkHandler.State,
    ) : CoroutineScope by CoroutineScope(coroutineContext + Job(coroutineContext.job)) {

        open fun getCause(): Throwable? = null

        /**
         * May throw any exception that caused the state to fail.
         */
        @Throws(Exception::class)
        suspend fun resumeConnection() {
            val observer = context.getOrNull(StateObserver)
            if (observer != null) {
                observer.beforeStateResume(this@NetworkHandlerSupport, _state)
                val result = kotlin.runCatching { resumeConnection0() }
                observer.afterStateResume(this@NetworkHandlerSupport, _state, result)
                result.getOrThrow()
            } else {
                resumeConnection0()
            }
        }

        protected abstract suspend fun resumeConnection0()
    }

    /**
     * State is *lazy*, initialized only if requested.
     */
    @Suppress("PropertyName")
    protected var _state: BaseStateImpl by lateinitMutableProperty { initialState() }
        private set

    final override val state: NetworkHandler.State get() = _state.correspondingState

    protected var _stateChangedDeferred = CompletableDeferred<NetworkHandler.State>()
        private set

    /**
     * For suspension until a state. e.g login.
     */
    override val onStateChanged: SelectClause1<NetworkHandler.State> get() = _stateChangedDeferred.onAwait

    protected data class StateSwitchingException(
        val old: BaseStateImpl,
        val new: BaseStateImpl,
    ) : CancellationException("State is switched from $old to $new")


    /**
     * Attempts to change state. Returns null if new state has same [class][KClass] as current.
     */
    protected inline fun <reified S : BaseStateImpl> setState(noinline new: () -> S): S? = setState(S::class, new)
    protected inline fun <reified S : BaseStateImpl> setState(
        old: BaseStateImpl, noinline new: () -> S
    ): S? = synchronized(this) {
        if (_state === old) {
            setState(new)
        } else {
            null
        }
    }

    /**
     * Calculate [new state][new] and set it as the current, returning the new state,
     * or `null` if state has concurrently been set to CLOSED, or has same [class][KClass] as current.
     *
     * You may need to call [BaseStateImpl.resumeConnection] to activate the new state, as states are lazy.
     */
    protected fun <S : BaseStateImpl> setState(newType: KClass<S>?, new: () -> S): S? = synchronized(this) {
        if (newType != null && _state::class == newType) return@synchronized null // already set to expected state by another thread. Avoid replications.
        if (_state.correspondingState == NetworkHandler.State.CLOSED) return null // CLOSED is final.

        val stateObserver = context.getOrNull(StateObserver)

        val impl = try {
            new() // inline only once
        } catch (e: Throwable) {
            stateObserver?.exceptionOnCreatingNewState(this, _state, e)
            throw e
        }

        val old = _state
        check(old !== impl) { "Old and new states cannot be the same." }


        // Order notes:
        // 1. Notify observers to attach jobs to [impl] (if so)
        _stateChangedDeferred.complete(impl.correspondingState)
        stateObserver?.stateChanged(this, old, impl)
        _stateChangedDeferred = CompletableDeferred()
        // 2. Update state to [state]. This affects selectors.
        _state = impl // switch state first. selector may be busy selecting.
        // 3. Cleanup, cancel old states.
        old.cancel(StateSwitchingException(old, impl))

        return impl
    }

    final override suspend fun resumeConnection() {
        _state.resumeConnection()
    }
}