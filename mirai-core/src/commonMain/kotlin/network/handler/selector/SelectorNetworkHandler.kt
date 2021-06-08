/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.awaitState
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.addNameHierarchically
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.findCauseOrSelf
import net.mamoe.mirai.utils.hierarchicalName
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

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
internal class SelectorNetworkHandler(
    override val context: NetworkHandlerContext, // impl notes: may consider to move into function member.
    private val selector: NetworkHandlerSelector<*>,
    /**
     * If `true`, a watcher job will be started to call [resumeConnection] when network is closed by [NetworkException] and [NetworkException.recoverable] is `true`.
     *
     * This is required for automatic reconnecting after network failure or system hibernation, since [NetworkHandler] is lazy and will reconnect iff [resumeConnection] is called.
     */
    allowActiveMaintenance: Boolean = true,
) : NetworkHandler {
    @Volatile
    private var lastCancellationCause: Throwable? = null

    private val scope: CoroutineScope by lazy {
        context.bot.coroutineContext
            .addNameHierarchically("SelectorNetworkHandler")
            .childScope()
    }

    private suspend inline fun instance(): NetworkHandler {
        if (!scope.isActive) {
            throw lastCancellationCause?.let(::CancellationException)
                ?: CancellationException("SelectorNetworkHandler is already closed")
        }
        return selector.awaitResumeInstance()
    }

    init {
        if (allowActiveMaintenance) {
            val bot = context.bot
            scope.launch(scope.hierarchicalName("BotOnlineWatchdog ${bot.id}")) {
                fun isActive(): Boolean {
                    return isActive && bot.isActive
                }
                while (isActive()) {
                    val instance = selector.getCurrentInstanceOrCreate()

                    awaitState(State.CLOSED) // suspend until next CLOSED

                    if (!isActive()) return@launch
                    if (selector.getCurrentInstanceOrNull() != instance) continue // instance already changed by other threads.

                    delay(3000) // make it slower to avoid massive reconnection on network failure.
                    if (!isActive()) return@launch

                    val failure = getLastFailure()
                    if (failure?.findCauseOrSelf { it is NetworkException && it.recoverable } != null) {
                        try {
                            resumeConnection() // notify selector to actively resume now.
                        } catch (ignored: Exception) {
                        }
                    }
                }
            }
        }
    }

    override val state: State
        get() = selector.getCurrentInstanceOrCreate().state

    override fun getLastFailure(): Throwable? = selector.getCurrentInstanceOrCreate().getLastFailure()

    override val stateChannel: ReceiveChannel<State>
        get() = selector.getCurrentInstanceOrCreate().stateChannel

    override suspend fun resumeConnection() {
        instance() // the selector will resume connection for us.
    }

    override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int) =
        instance().sendAndExpect(packet, timeout, attempts)

    override suspend fun sendWithoutExpect(packet: OutgoingPacket) = instance().sendWithoutExpect(packet)
    override fun close(cause: Throwable?) {
        synchronized(scope) {
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

