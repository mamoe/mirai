/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.utils.addNameHierarchically
import net.mamoe.mirai.utils.childScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.Volatile

/**
 * A proxy to [NetworkHandler] that delegates calls to instance returned by [NetworkHandlerSelector.awaitResumeInstance].
 * Selection logic is implemented in [NetworkHandlerSelector].
 *
 * This is useful to implement a delegation of [NetworkHandler]. The functionality of *selection* is provided by the strategy [selector][NetworkHandlerSelector].
 *
 * ### Important notes
 *
 * [NetworkHandlerSelector.awaitResumeInstance] is called everytime when an operation in [NetworkHandler] is called.
 *
 * Before every [sendAndExpect] call, [resumeConnection] is invoked.
 *
 * @see NetworkHandlerSelector
 */
internal open class SelectorNetworkHandler<out H : NetworkHandler>(
    val selector: NetworkHandlerSelector<H>,
) : NetworkHandler {
    @Volatile
    private var lastCancellationCause: Throwable? = null

    override val context: NetworkHandlerContext get() = selector.getCurrentInstanceOrCreate().context

    protected val scope: CoroutineScope by lazy {
        context.bot.coroutineContext
            .addNameHierarchically("SelectorNetworkHandler")
            .childScope()
    }
    private val lock = SynchronizedObject()

    protected suspend inline fun instance(): H {
        if (!scope.isActive) {
            throw lastCancellationCause?.let(::CancellationException)
                ?: CancellationException("SelectorNetworkHandler is already closed")
        }
        return selector.awaitResumeInstance()
    }

    override val state: State
        get() = selector.getCurrentInstanceOrCreate().state

    override fun getLastFailure(): Throwable? = selector.getCurrentInstanceOrCreate().getLastFailure()

    override val stateChannel: ReceiveChannel<State>
        get() = selector.getCurrentInstanceOrCreate().stateChannel

    override suspend fun resumeConnection() {
        instance() // the selector will resume connection for us.
    }

    override suspend fun <P : Packet?> sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int): P =
        instance().sendAndExpect(packet, timeout, attempts)

    override suspend fun <P : Packet?> sendAndExpect(
        packet: OutgoingPacketWithRespType<P>,
        timeout: Long,
        attempts: Int
    ): P = instance().sendAndExpect(packet, timeout, attempts)

    override suspend fun sendWithoutExpect(packet: OutgoingPacket) = instance().sendWithoutExpect(packet)
    override fun close(cause: Throwable?) {
        if (cause is NetworkException && cause.recoverable) {
            selector.getCurrentInstanceOrNull()?.close(cause)
            return
        }
        synchronized(lock) {
            if (scope.isActive) {
                lastCancellationCause = cause
                scope.cancel()
            } else {
                return
            }
        }
        selector.getCurrentInstanceOrNull()?.close(cause)
    }

    override val coroutineContext: CoroutineContext
        get() = selector.getCurrentInstanceOrNull()?.coroutineContext ?: scope.coroutineContext // merely use fallback

    override fun toString(): String = "SelectorNetworkHandler(currentInstance=${selector.getCurrentInstanceOrNull()})"
}

