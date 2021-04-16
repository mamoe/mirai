/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.atomicfu.atomic
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import org.jetbrains.annotations.TestOnly

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
    private suspend inline fun instance(): NetworkHandler = selector.awaitResumeInstance()

    override val state: State
        get() = selector.getResumedInstance()?.state ?: State.INITIALIZED

    override suspend fun resumeConnection() {
        instance() // the selector will resume connection for us.
    }

    override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int) =
        instance().sendAndExpect(packet, timeout, attempts)

    override suspend fun sendWithoutExpect(packet: OutgoingPacket) = instance().sendWithoutExpect(packet)
    override fun close(cause: Throwable?) {
        selector.getResumedInstance()?.close(cause)
    }
}

internal class ExceptionInSelectorResumeException(
    cause: Throwable
) : RuntimeException(cause)

/**
 * A lazy stateful selector of [NetworkHandler]. This is used as a director([selector][SelectorNetworkHandler.selector]) to [SelectorNetworkHandler].
 */
internal interface NetworkHandlerSelector<H : NetworkHandler> {
    /**
     * Returns an instance immediately without suspension, or `null` if instance not ready.
     *
     * This function should not throw any exception.
     * @see awaitResumeInstance
     */
    fun getResumedInstance(): H?

    /**
     * Returns an alive [NetworkHandler], or suspends the coroutine until the connection has been made again.
     *
     * This function may throw exceptions, which would be propagated to the original caller of [SelectorNetworkHandler.resumeConnection].
     */
    suspend fun awaitResumeInstance(): H
}

/**
 * A lazy stateful implementation of [NetworkHandlerSelector].
 *
 * - Calls [factory.create][NetworkHandlerFactory.create] to create [NetworkHandler]s.
 * - Re-initialize [NetworkHandler] instances if the old one is dead.
 * - Suspends requests when connection is not available.
 *
 * No connection is created until first invocation of [getResumedInstance],
 * and new connections are created only when calling [getResumedInstance] if the old connection was dead.
 */
// may be replaced with a better name.
internal abstract class AbstractKeepAliveNetworkHandlerSelector<H : NetworkHandler> : NetworkHandlerSelector<H> {
    private val current = atomic<H?>(null)

    @TestOnly
    internal fun setCurrent(h: H) {
        current.value = h
    }

    protected abstract fun createInstance(): H

    final override fun getResumedInstance(): H? = current.value

    final override tailrec suspend fun awaitResumeInstance(): H {
        val current = getResumedInstance()
        return if (current != null) {
            when (current.state) {
                State.CLOSED -> {
                    this.current.compareAndSet(current, null) // invalidate the instance and try again.
                    awaitResumeInstance() // will create new instance.
                }
                State.CONNECTING,
                State.CONNECTION_LOST,
                State.INITIALIZED,
                State.OK -> {
                    current.resumeConnection()
                    return current
                }
            }
        } else {
            this.current.compareAndSet(current, createInstance())
            awaitResumeInstance()
        }
    }
}

/**
 * [AbstractKeepAliveNetworkHandlerSelector] implementation delegating [createInstance] to [factory]
 */
internal class FactoryKeepAliveNetworkHandlerSelector<H : NetworkHandler>(
    private val factory: NetworkHandlerFactory<H>,
    private val serverList: ServerList,
    private val context: NetworkHandlerContext,
) : AbstractKeepAliveNetworkHandlerSelector<H>() {
    override fun createInstance(): H =
        factory.create(context, serverList.pollCurrent()?.toSocketAddress() ?: throw NoServerAvailableException())
}

internal class NoServerAvailableException :
    NoSuchElementException("No server available. (Failed to connect to any of the servers)")