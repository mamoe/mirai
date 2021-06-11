/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.PacketCodec
import net.mamoe.mirai.internal.network.components.PacketHandler
import net.mamoe.mirai.internal.network.components.PacketLoggingStrategy
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.handler.selector.NetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

/**
 * Implements basic logics of [NetworkHandler]
 */
internal abstract class NetworkHandlerSupport(
    final override val context: NetworkHandlerContext,
    additionalCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : NetworkHandler, CoroutineScope {
    final override val coroutineContext: CoroutineContext =
        additionalCoroutineContext.childScopeContext(SupervisorJob(context.bot.coroutineContext.job))

    protected abstract fun initialState(): BaseStateImpl
    protected abstract suspend fun sendPacketImpl(packet: OutgoingPacket)

    protected fun collectUnknownPacket(raw: RawIncomingPacket) {
        packetLogger.debug { "Unknown packet: commandName=${raw.commandName}, body=${raw.body.toUHexString()}" }
        // may add hooks here (to context)
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
    // packets synchronization impl
    ///////////////////////////////////////////////////////////////////////////

    private val packetHandler: PacketHandler by lazy { context[PacketHandler] }

    /**
     * Called when a packet is received.
     */
    internal open fun collectReceived(packet: IncomingPacket) {
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

    final override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int): Packet? {
        require(attempts >= 1) { "attempts must be at least 1." }
        val listener = PacketListener(packet.commandName, packet.sequenceId)
        packetListeners.add(listener)
        withExceptionCollector {
            try {
                repeat(attempts) {
                    context[PacketLoggingStrategy].logSent(logger, packet)
                    sendPacketImpl(packet)
                    try {
                        return withTimeout(timeout) {
                            listener.result.await()
                        }
                    } catch (e: TimeoutCancellationException) {
                        collectException(e)
                    }
                }
                throwLast()
            } finally {
                packetListeners.remove(listener)
                listener.result.completeExceptionally(getLast() ?: IllegalStateException("No response"))
            }
        }
    }

    final override suspend fun sendWithoutExpect(packet: OutgoingPacket) {
        context[PacketLoggingStrategy].logSent(logger, packet)
        sendPacketImpl(packet)
    }

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
     *
     * **IMPORTANT notes to lifecycle:**
     *
     * Normally if the state is set to [NetworkHandler.State.CLOSED] by [setState], [selector][NetworkHandlerSelector] may reinitialize an instance.
     *
     * Any exception caught by the scope (supervisor job) is considered as _fatal failure_ that will set state to CLOSE and **propagate the exception to user of [selector][NetworkHandlerSelector]**.
     *
     *
     * You must catch all the exceptions and change states by [setState] manually.
     */
    abstract inner class BaseStateImpl(
        val correspondingState: NetworkHandler.State,
    ) : CoroutineScope {
        final override val coroutineContext: CoroutineContext =
            this@NetworkHandlerSupport.coroutineContext + Job(this@NetworkHandlerSupport.coroutineContext.job)

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
     *
     * You need to call setter inside `synchronized(this) { }`.
     */
    @Suppress("PropertyName")
    protected var _state: BaseStateImpl by lateinitMutableProperty { initialState() }
        private set

    final override val state: NetworkHandler.State get() = _state.correspondingState

    override fun getLastFailure(): Throwable? = _state.getCause()

    private val _stateChannel = Channel<NetworkHandler.State>(0)
    final override val stateChannel: ReceiveChannel<NetworkHandler.State> get() = _stateChannel

    private val setStateLock = SynchronizedObject()

    protected data class StateSwitchingException(
        val old: BaseStateImpl,
        val new: BaseStateImpl,
    ) : CancellationException("State is switched from $old to $new")

    /**
     * Attempts to change state.
     *
     * Returns null if new state has same [class][KClass] as current (meaning already set by another thread).
     */
    protected inline fun <reified S : BaseStateImpl> setState(noinline new: () -> S): S? = setState(S::class, new)

    /**
     * Attempts to change state if current state is [this].
     *
     * Returns null if new state has same [class][KClass] as current or when current state is already set to another state concurrently by another thread.
     *
     * This is designed to be used inside [BaseStateImpl].
     */
    protected inline fun <reified S : BaseStateImpl> BaseStateImpl.setState(
        noinline new: () -> S
    ): S? = synchronized(setStateLock) {
        if (_state === this) {
            this@NetworkHandlerSupport.setState(new)
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
    @JvmName("setState1")
    protected fun <S : BaseStateImpl> setState(newType: KClass<S>, new: () -> S): S? =
        @OptIn(TestOnly::class)
        setStateImpl(newType as KClass<S>?, new)

    /**
     * This can only be called by [setState] or in tests.
     *
     * [newType] can be `null` **iff in tests**, to ignore checks.
     */
    //
    @TestOnly
    internal fun <S : BaseStateImpl> setStateImpl(newType: KClass<S>?, new: () -> S): S? = synchronized(setStateLock) {
        val old = _state
        if (newType != null && old::class == newType) return null // already set to expected state by another thread. Avoid replications.
        if (old.correspondingState == NetworkHandler.State.CLOSED) return null // CLOSED is final.

        val stateObserver = context.getOrNull(StateObserver)

        val impl = try {
            new() // inline only once
        } catch (e: Throwable) {
            stateObserver?.exceptionOnCreatingNewState(this, old, e)
            throw e
        }

        check(old !== impl) { "Old and new states cannot be the same." }

        _state = impl // update current state
        old.cancel(StateSwitchingException(old, impl)) // close old
        stateObserver?.stateChanged(this, old, impl) // notify observer
        _stateChannel.trySend(impl.correspondingState) // notify selector

        return impl
    }

    final override suspend fun resumeConnection() {
        _state.resumeConnection()
    }
}