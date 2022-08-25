/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
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
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.utils.fromMiraiLogger
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.Either.Companion.fold
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.Volatile
import kotlin.reflect.KClass

/**
 * Implements a state-based [NetworkHandler].
 */
internal abstract class NetworkHandlerSupport(
    final override val context: NetworkHandlerContext,
    additionalCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : NetworkHandler, CoroutineScope {
    final override val coroutineContext: CoroutineContext =
        additionalCoroutineContext.childScopeContext(SupervisorJob(context.bot.coroutineContext.job))
            .plus(CoroutineExceptionHandler.fromMiraiLogger(logger))

    /**
     * Creates an instance of the initial state. This is guaranteed to be called at most once.
     */
    protected abstract fun initialState(): BaseStateImpl

    /**
     * Performs network IO or launches a coroutine for that, to send the [packet].
     *
     * **Node**: It's not guaranteed whether this function sends the packet in-place or launches a coroutine for it.
     * Caller should not rely on this characteristic.
     */
    protected abstract suspend fun sendPacketImpl(packet: OutgoingPacket)

    /**
     * Handles *unknown* [RawIncomingPacket]s. Packets with unrecognized [RawIncomingPacket.commandName] are considered *unknown*.
     *
     * Can be called by implementation
     */
    protected fun collectUnknownPacket(raw: RawIncomingPacket) {
        packetLogger.debug { "Unknown packet: commandName=${raw.commandName}, body=${raw.body.toUHexString()}" }
        // may add hooks here (to context)
    }

    override fun close(cause: Throwable?) {
        if (coroutineContext.job.isActive) {
            coroutineContext.job.cancel(
                if (cause is CancellationException) {
                    cause
                } else {
                    CancellationException("NetworkHandler closed", cause)
                }
            )
        }
    }

    protected val packetLogger: MiraiLogger by lazy {
        context.logger.subLogger("NetworkDebug").withSwitch(PacketCodec.PACKET_DEBUG)
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
                packet.result.fold(
                    onLeft = { listener.result.completeExceptionally(it) },
                    onRight = { listener.result.complete(it) }
                )
            }
        }
        launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                packetHandler.handlePacket(packet)
            } catch (e: Throwable) { // do not pass it to CoroutineExceptionHandler for a more controllable behavior.
                logger.error(e)
            }
        }
    }

    final override suspend fun <P : Packet?> sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int): P {
        require(attempts >= 1) { "attempts must be at least 1." }
        val listener = PacketListener(packet.commandName, packet.sequenceId)
        packetListeners.add(listener)
        withExceptionCollector {
            try {
                repeat(attempts) {
                    context[PacketLoggingStrategy].logSent(logger, packet)
                    sendPacketImpl(packet)
                    try {
                        @Suppress("UNCHECKED_CAST")
                        return withTimeout(timeout) {
                            listener.result.await()
                        } as P
                    } catch (e: TimeoutCancellationException) {
                        collectException(e)
                    }
                }
                throwLast()
            } finally {
                packetListeners.remove(listener)
                if (listener.result.isActive) {
                    listener.result.completeExceptionally(
                        getLast() ?: IllegalStateException("Internal error: sendAndExpect failed without an exception.")
                    )
                }
            }
        }
    }

    final override suspend fun <P : Packet?> sendAndExpect(
        packet: OutgoingPacketWithRespType<P>,
        timeout: Long,
        attempts: Int
    ): P = sendAndExpect(packet as OutgoingPacket, timeout, attempts)

    final override suspend fun sendWithoutExpect(packet: OutgoingPacket) {
        context[PacketLoggingStrategy].logSent(logger, packet)
        sendPacketImpl(packet)
    }

    /**
     * Listens for the resultant packet from server.
     */
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

    private val packetListeners = ConcurrentLinkedDeque<PacketListener>()

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
     * **IMPORTANT implementation notes:**
     *
     * You must create subclasses of [BaseStateImpl] for EVERY SINGLE [NetworkHandler.State].
     * **DO NOT** use same type for more than one [NetworkHandler.State],
     * otherwise [setState] will refuse updating state in some concurrent situations and will be very difficult to debug.
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

        // Important: read the above doc before implementing BaseStateImpl.

        // Do not use init blocks to launch anything. Do use [startState]


        /**
         * Starts things that should be done in this state.
         *
         * Called after this instance is initialized, and it is at suitable time for initialization.
         *
         * Note: must be fast.
         */
        open fun startState() {

        }

        /**
         * Called after this instance is set to [_state]. (Visible publicly)
         */
        open fun afterUpdated() {

        }

        open fun getCause(): Throwable? = null

        /**
         * May throw any exception that caused the state to fail.
         */
        @Throws(Exception::class)
        suspend fun resumeConnection() {
            val observer = context.getOrNull(StateObserver)
            observer?.beforeStateResume(this@NetworkHandlerSupport, _state)
            val result = kotlin.runCatching { resumeConnection0() }
            observer?.afterStateResume(this@NetworkHandlerSupport, _state, result)
            result.getOrThrow()
        }

        protected abstract suspend fun resumeConnection0()
    }

    /**
     * State is *lazy*, initialized only if requested.
     *
     * You must not set this property directly, but use [setState].
     */
    @Suppress("PropertyName")
    protected var _state: BaseStateImpl by lateinitMutableProperty { initialState() }
        private set

    final override val state: NetworkHandler.State get() = _state.correspondingState

    override fun getLastFailure(): Throwable? = _state.getCause()

    private val _stateChannel = Channel<NetworkHandler.State>(0)
    final override val stateChannel: ReceiveChannel<NetworkHandler.State> get() = _stateChannel

    protected data class StateSwitchingException(
        val old: BaseStateImpl,
        val new: BaseStateImpl,
        override val cause: Throwable? = new.getCause(), // so it can be unwrapped
    ) : CancellationException("State is switched from $old to $new")

    /**
     * Calculate [new state][new] and set it as the current, returning the new state,
     * or `null` if state has concurrently been set to CLOSED, or has same [class][KClass] as current.
     *
     * You may need to call [BaseStateImpl.resumeConnection] to activate the new state, as states are lazy.
     */
    protected inline fun <reified S : BaseStateImpl> setState(noinline new: () -> S): S? =
        _state.setState(new)

    /**
     * Attempts to change state if current state is [this].
     *
     * Returns null if new state has same [class][KClass] as current or when current state is already set to another state concurrently by another thread.
     *
     * This is designed to be used inside [BaseStateImpl].
     */
    protected inline fun <reified S : BaseStateImpl> BaseStateImpl.setState(
        noinline new: () -> S,
    ): S? = lock.withLock {
        if (_state === this) {
            @OptIn(TestOnly::class)
            this@NetworkHandlerSupport.setStateImpl(S::class, new)
        } else {
            null
        }
    }

    private val lock = reentrantLock()
    internal val lockForSetStateWithOldInstance = SynchronizedObject()

    @Volatile
    private var changingState: KClass<out BaseStateImpl>? = null

    /**
     * This can only be called by [setState] or in tests. Note:
     */
    //
    @TestOnly
    internal fun <S : BaseStateImpl> setStateImpl(newType: KClass<S>, new: () -> S): S? =
        lock.withLock {
            val old = _state
            if (old::class == newType) return@withLock null // already set to expected state by another thread. Avoid replications.
            if (old.correspondingState == NetworkHandler.State.CLOSED) return@withLock null // CLOSED is final.

            val changingState = changingState
            if (changingState != null) {
                if (changingState == newType) {
                    // no duplicates
                    return null
                } else {
                    error("New state ${newType.simpleName} clashes with current switching process, changingState = ${changingState.simpleName}.")
                }
            }

            this.changingState = newType

            try {

                val stateObserver = context.getOrNull(StateObserver)

                val impl = try {
                    new()
                } catch (e: Throwable) {
                    stateObserver?.exceptionOnCreatingNewState(this, old, e)
                    throw e
                }

                try {
                    check(old !== impl) { "Old and new states cannot be the same." }

                    stateObserver?.beforeStateChanged(this, old, impl)

                    // We should startState before expose it publicly because State.resumeConnection may wait for some jobs that are launched in startState.
                    // We cannot close old state before changing the 'public' _state to be the new one, otherwise every client will get some kind of exceptions (unspecified, maybe CancellationException).
                    impl.startState() // launch jobs
                } catch (e: Throwable) {
                    throw e
                }

                // No further change

                _state = impl // update current state
                // After _state is updated, we are safe.

                old.cancel(StateSwitchingException(old, impl)) // close old
                impl.afterUpdated() // now do post-update things.

                stateObserver?.stateChanged(this, old, impl) // notify observer
                _stateChannel.trySend(impl.correspondingState) // notify selector

                return@withLock impl
            } finally {
                this.changingState = null
            }
        }

    final override suspend fun resumeConnection() {
        _state.resumeConnection()
    }
}