/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.net

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.CancellationException

internal typealias HState = NetworkHandler.State

/**
 * Basic interface available to application. Usually wrapped with [NetworkHandlerWithSelector].
 *
 * A [NetworkHandler] holds no reference to [Bot]s.
 */
internal interface NetworkHandler { // TODO: 2021/4/13 how to hold data?
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
         * Connection to server, including logging in.
         *
         * At this state [resumeConnection] does nothing. [sendAndExpect] suspends for the result of connection started in [INITIALIZED].
         */
        CONNECTING,

        /**
         * Everything is working. [resumeConnection] does nothing. [sendAndExpect] does not suspend for connection reasons.
         */
        OK,

        /**
         * No Internet Connection is available but it is possible to establish a connection again(switching state to [CONNECTING]).
         */
        SNEAK_OFF,

        /**
         * Cannot resume anymore. Both [resumeConnection] and [sendAndExpect] throw a [CancellationException]
         */
        CLOSED,
    }

    /**
     * Attempts to resume the connection.
     * @see HState
     */
    suspend fun resumeConnection(): Boolean


    /**
     * Sends [packet] and expects to receive a response from the server.
     * @param retry ranges `1..INFINITY`
     */
    suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long = 5000, retry: Int = 2): Packet

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
 * Factory for a specific [NetworkHandler].
 */
internal interface NetworkHandlerFactory<H : NetworkHandler> {
    fun create(host: String, port: Int): H = create(InetSocketAddress.createUnresolved(host, port))
    fun create(host: InetAddress, port: Int): H = create(InetSocketAddress(host, port))

    /**
     * Create an instance of [H]. The returning [H] has [NetworkHandler.state] of [NetworkHandler.State1.INITIALIZED]
     */
    fun create(host: SocketAddress): H
}

/**
 * Strategy of managing [NetworkHandler].
 *
 * - Calls [factory.create][NetworkHandlerFactory.create] to create [NetworkHandler]s.
 * - Re-initialize [NetworkHandler] instances if the old one is dead.
 * - Suspends requests when connection is not available.
 */
internal abstract class NetworkHandlerSelector<H : NetworkHandler>(
    protected val factory: NetworkHandlerFactory<H>
) {
    /**
     * Returns an alive [NetworkHandler], or suspends the coroutine until the connection has been made again.
     */
    abstract suspend fun awaitInstance(): H

    /**
     * Returns an instance immediately without suspension, or `null` if instance not ready.
     * @see awaitInstance
     */
    abstract fun getInstance(): H?
}

internal class NetworkHandlerSelectorImpl<H : NetworkHandler>(factory: NetworkHandlerFactory<H>) :
    NetworkHandlerSelector<H>(factory) {

    private fun createInstance(): H {
        // TODO: 2021/4/13 how to pass information about conn. here?
//        factory.create()
        TODO()
    }

    override suspend fun awaitInstance(): H {
        getInstance()?.let { return it }
        TODO()
    }

    override fun getInstance(): H? = TODO()
}

/**
 * Delegates [NetworkHandler] calls to instance returned by [NetworkHandlerSelector.awaitInstance].
 */
internal class NetworkHandlerWithSelector(
    private val selector: NetworkHandlerSelector<*>
) : NetworkHandler {
    private suspend inline fun instance(): NetworkHandler = selector.awaitInstance()

    override val state: HState get() = selector.getInstance()?.state ?: HState.INITIALIZED

    override suspend fun resumeConnection(): Boolean {
        return instance().resumeConnection()
    }

    override suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long, retry: Int): Packet {
        return instance().sendAndExpect(packet, timeout, retry)
    }

    override suspend fun sendWithoutExpect(packet: OutgoingPacket) {
        return instance().sendWithoutExpect(packet)
    }

    override suspend fun close() {
        return instance().close()
    }
}