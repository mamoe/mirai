/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.net.protocol.SsoContext
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
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
    val ssoContext: SsoContext
    val configuration: BotConfiguration
}

internal class NetworkHandlerContextImpl(
    override val bot: QQAndroidBot,
    override val ssoContext: SsoContext,
) : NetworkHandlerContext {
    override val configuration: BotConfiguration
        get() = bot.configuration

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
    fun close()

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