/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.net

import kotlinx.atomicfu.atomic
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.net.NetworkHandler.State
import net.mamoe.mirai.internal.network.net.protocol.SsoController
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.CancellationException

/**
 * Immutable context for [NetworkHandler]
 */
internal interface NetworkHandlerContext {
    val bot: QQAndroidBot

    val logger: MiraiLogger
    val ssoController: SsoController
    val configuration: BotConfiguration

    fun getNextAddress(): SocketAddress // FIXME: 2021/4/14
}

internal class NetworkHandlerContextImpl(
    override val bot: QQAndroidBot,
    override val ssoController: SsoController,
) : NetworkHandlerContext {
    override val configuration: BotConfiguration
        get() = bot.configuration

    override fun getNextAddress(): SocketAddress {
        TODO("Not yet implemented")
    }

    override val logger: MiraiLogger by lazy { configuration.networkLoggerSupplier(bot) }
}

/**
 * Basic interface available to application. Usually wrapped with [SelectorNetworkHandler].
 *
 * A [NetworkHandler] holds no reference to [Bot]s.
 */
internal interface NetworkHandler {
    val context: NetworkHandlerContext

    /**
     * State of this handler.
     */
    val state: State

    enum class State {
        /**
         * Just created and no connection has been made.
         *
         * At this state [resumeConnection] turns state into [CONNECTING] and
         * establishes a connection to the server and do authentication, for which [sendAndExpect] suspends.
         */
        INITIALIZED,

        /**
         * Connection to server, including the process of authentication.
         *
         * At this state [resumeConnection] does nothing. [sendAndExpect] suspends for the result of connection started in [INITIALIZED].
         */
        CONNECTING,

        /**
         * Everything is working. [resumeConnection] does nothing. [sendAndExpect] does not suspend for connection reasons.
         */
        OK,

        /**
         * No Internet Connection available or for any other reasons but it is possible to establish a connection again(switching state to [CONNECTING]).
         */
        CONNECTION_LOST,

        /**
         * Cannot resume anymore. Both [resumeConnection] and [sendAndExpect] throw a [CancellationException].
         *
         * When a handler reached [CLOSED] state, it is finalized and cannot be restored to any other states.
         */
        CLOSED,
    }

    /**
     * Attempts to resume the connection. Throws no exception but changes [state]
     * @see State
     */
    suspend fun resumeConnection()


    /**
     * Sends [packet] and expects to receive a response from the server.
     * @param attempts ranges `1..INFINITY`
     */
    suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long = 5000, attempts: Int = 2): Packet?

    /**
     * Sends [packet] and does not expect any response. (Response is still processed but not passed as a return value of this function.)
     */
    suspend fun sendWithoutExpect(packet: OutgoingPacket)


    /**
     * Closes this handler gracefully and suspends the coroutine for its completion.
     */
    suspend fun close()
}

/**
 * Factory for a specific [NetworkHandler] implementation.
 */
internal interface NetworkHandlerFactory<H : NetworkHandler> {
    fun create(context: NetworkHandlerContext, host: String, port: Int): H =
        create(context, InetSocketAddress.createUnresolved(host, port))

    fun create(context: NetworkHandlerContext, host: InetAddress, port: Int): H =
        create(context, InetSocketAddress(host, port))

    /**
     * Create an instance of [H]. The returning [H] has [NetworkHandler.state] of [State.INITIALIZED]
     */
    fun create(context: NetworkHandlerContext, address: SocketAddress): H
}

/**
 * A lazy stateful selector of [NetworkHandler].
 *
 * - Calls [factory.create][NetworkHandlerFactory.create] to create [NetworkHandler]s.
 * - Re-initialize [NetworkHandler] instances if the old one is dead.
 * - Suspends requests when connection is not available.
 *
 * No connection is created until first invocation of [getResumedInstance],
 * and new connections are created only when calling [getResumedInstance] if the old connection was dead.
 */
internal abstract class NetworkHandlerSelector<H : NetworkHandler> {
    /**
     * Returns an instance immediately without suspension, or `null` if instance not ready.
     * @see awaitResumeInstance
     */
    abstract fun getResumedInstance(): H?

    /**
     * Returns an alive [NetworkHandler], or suspends the coroutine until the connection has been made again.
     */
    abstract suspend fun awaitResumeInstance(): H
}

// TODO: 2021/4/14 better naming
internal abstract class AutoReconnectNetworkHandlerSelector<H : NetworkHandler> : NetworkHandlerSelector<H>() {
    private val current = atomic<H?>(null)

    protected abstract fun createInstance(): H

    final override fun getResumedInstance(): H? = current.value

    final override tailrec suspend fun awaitResumeInstance(): H {
        val current = getResumedInstance()
        return if (current != null) {
            when (current.state) {
                State.OK -> current
                State.CLOSED -> {
                    this.current.compareAndSet(current, null) // invalidate the instance and try again.
                    awaitResumeInstance()
                }
                else -> {
                    current.resumeConnection() // try to advance state.
                    awaitResumeInstance()
                }
            }
        } else {
            this.current.compareAndSet(current, createInstance())
            awaitResumeInstance()
        }
    }
}

/**
 * Delegates [NetworkHandler] calls to instance returned by [NetworkHandlerSelector.awaitResumeInstance].
 */
internal class SelectorNetworkHandler(
    override val context: NetworkHandlerContext,
    private val selector: NetworkHandlerSelector<*>
) : NetworkHandler {
    private suspend inline fun instance(): NetworkHandler = selector.awaitResumeInstance()

    override val state: State get() = selector.getResumedInstance()?.state ?: State.INITIALIZED

    override suspend fun resumeConnection() {
        instance() // the selector will resume connection for us.
    }

    override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, attempts: Int) =
        instance().sendAndExpect(packet, timeout, attempts)

    override suspend fun sendWithoutExpect(packet: OutgoingPacket) = instance().sendWithoutExpect(packet)
    override suspend fun close() = instance().close()
}