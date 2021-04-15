/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.impl

import kotlinx.coroutines.*
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.net.protocol.RawIncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext


private val PACKET_DEBUG = systemProp("mirai.debug.packet.logger", true)

internal abstract class NetworkHandlerSupport(
    override val context: NetworkHandlerContext,
    final override val coroutineContext: CoroutineContext = SupervisorJob(),
) : NetworkHandler, CoroutineScope by coroutineContext.childScope(SupervisorJob()) {

    protected abstract fun initialState(): BaseStateImpl
    protected abstract suspend fun sendPacketImpl(packet: OutgoingPacket)

    /**
     * Called when a packet is received.
     */
    protected fun collectReceived(packet: IncomingPacket) {
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
    }

    protected fun collectUnknownPacket(raw: RawIncomingPacket) {
        packetLogger.debug { "Unknown packet: commandName=${raw.commandName}, body=${raw.body.toUHexString()}" }
        // may add hooks here (to context)
    }

    final override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int): Packet? {
        val listener = PacketListener(packet.commandName, packet.sequenceId)
        packetListeners.add(listener)
        var exception: Throwable? = null
        repeat(attempts.coerceAtLeast(1)) {
            try {
                sendPacketImpl(packet)
                try {
                    return withTimeout(timeout) {
                        listener.result.await()
                    }
                } catch (e: TimeoutCancellationException) {
                    if (exception != null) {
                        e.addSuppressed(exception!!)
                    }
                    exception = e // show last exception
                }
            } finally {
                packetListeners.remove()
            }
        }
        throw exception!!
    }

    final override suspend fun sendWithoutExpect(packet: OutgoingPacket) {
        sendPacketImpl(packet)
    }

    override fun close() {
        coroutineContext.job.cancel("NetworkHandler closed.")
    }

    protected val packetLogger: MiraiLogger by lazy {
        MiraiLogger.create(context.logger.identity + ".debug").withSwitch(PACKET_DEBUG)
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
    protected abstract inner class BaseStateImpl(
        val correspondingState: NetworkHandler.State,
    ) : CoroutineScope by CoroutineScope(coroutineContext + SupervisorJob(coroutineContext.job)) {
        @Throws(Exception::class)
        abstract suspend fun resumeConnection()
    }

    /**
     * State is *lazy*, initialized only if requested.
     */
    @Suppress("PropertyName")
    protected var _state: BaseStateImpl by lateinitMutableProperty { initialState() }
        private set

    final override val state: NetworkHandler.State get() = _state.correspondingState
    protected inline fun setState(crossinline new: () -> BaseStateImpl) = synchronized(this) {
        // we can add hooks here for debug.

        val impl = new()

        val old = _state
        check(old !== impl) { "Old and new states cannot be the same." }
        old.cancel()
        _state = impl
    }

    final override suspend fun resumeConnection() {
        _state.resumeConnection()
    }
}


