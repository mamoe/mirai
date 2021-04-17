/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.selects.SelectClause1
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.utils.MiraiLogger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.CancellationException

/**
 * Basic interface available to application. Usually wrapped with [SelectorNetworkHandler].
 *
 * Implementation is usually subclass of [NetworkHandlerSupport].
 *
 * @see NetworkHandlerSupport
 */
internal interface NetworkHandler {
    val context: NetworkHandlerContext

    fun isOk() = state == State.OK

    /**
     * State of this handler.
     */
    val state: State

    /**
     * For suspension until a state. e.g login.
     */
    val onStateChanged: SelectClause1<State>

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
         * Loading essential data from server and local cache. Data include contact list.
         *
         * At this state [resumeConnection] waits for the jobs. [sendAndExpect] works normally.
         */
        LOADING,

        /**
         * Everything is working.
         *
         * At this state [resumeConnection] does nothing. [sendAndExpect] works normally.
         */
        OK,

        /**
         * No Internet Connection available or for any other reasons
         * but it is possible to establish a connection again(switching state to [CONNECTING]).
         *
         * At this state [resumeConnection] turns the handle to [CONNECTING].
         * [sendAndExpect] throws [IllegalStateException]
         */
        CONNECTION_LOST,

        /**
         * Cannot resume anymore. Both [resumeConnection] and [sendAndExpect] throw a [CancellationException].
         *
         * When a handler reached [CLOSED] state, it is finalized and cannot be restored to any other states.
         *
         * At this state [resumeConnection] throws the exception caught from underlying socket implementation (i.e netty).
         * [sendAndExpect] throws [IllegalStateException]
         */
        CLOSED,
    }

    /**
     * Attempts to resume the connection.
     *
     * May throw exception that had caused current state to fail.
     * @see State
     */
    @Throws(Exception::class)
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
     * Closes this handler gracefully.
     */
    fun close(cause: Throwable?)

    ///////////////////////////////////////////////////////////////////////////
    // compatibility
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @suppress This is for compatibility with old code. Use [sendWithoutExpect] without extension receiver instead.
     */
    suspend fun OutgoingPacket.sendWithoutExpect(
        antiCollisionParam: Any? = null
    ) = this@NetworkHandler.sendWithoutExpect(this)

    /**
     * @suppress This is for compatibility with old code. Use [sendAndExpect] without extension receiver instead.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <R> OutgoingPacket.sendAndExpect(
        timeoutMillis: Long = 5000,
        retry: Int = 2,
        antiCollisionParam: Any? = null // signature collision
    ): R = sendAndExpect(this, timeoutMillis, retry) as R

    /**
     * @suppress This is for compatibility with old code. Use [sendAndExpect] without extension receiver instead.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <R : Packet?> OutgoingPacketWithRespType<R>.sendAndExpect(
        timeoutMillis: Long = 5000,
        retry: Int = 2
    ): R = sendAndExpect(this, timeoutMillis, retry) as R
}

internal val NetworkHandler.logger: MiraiLogger get() = context.logger

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