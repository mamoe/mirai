/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.selector

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.selects.SelectClause1
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket

/**
 * A proxy to [NetworkHandler] that delegates calls to instance returned by [NetworkHandlerSelector.awaitResumeInstance].
 *
 * [NetworkHandlerSelector.awaitResumeInstance] is called everytime when an operation in [NetworkHandler] is called.
 *
 * This is useful to implement a delegation of [NetworkHandler]. The functionality of *selection* is provided by the strategy [selector][NetworkHandlerSelector].
 * @see NetworkHandlerSelector
 */
internal class SelectorNetworkHandler(
    override val context: NetworkHandlerContext, // impl notes: may consider to move into function member.
    private val selector: NetworkHandlerSelector<*>,
) : NetworkHandler {
    private val scope = CoroutineScope(SupervisorJob(context.bot.coroutineContext[Job]))
    private suspend inline fun instance(): NetworkHandler = selector.awaitResumeInstance()

    override val state: State
        get() = selector.getResumedInstance()?.state ?: State.INITIALIZED
    override val onStateChanged: SelectClause1<State>
        get() = selector.getResumedInstance()?.onStateChanged
            ?: scope.async { instance().state }.onAwait

    override suspend fun resumeConnection() {
        instance() // the selector will resume connection for us.
    }

    override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int) =
        instance().sendAndExpect(packet, timeout, attempts)

    override suspend fun sendWithoutExpect(packet: OutgoingPacket) = instance().sendWithoutExpect(packet)
    override fun close(cause: Throwable?) {
        selector.getResumedInstance()?.close(cause)
    }

    override fun toString(): String = "SelectorNetworkHandler(currentInstance=${selector.getResumedInstance()})"
}

