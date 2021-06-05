/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.BotInitProcessor
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.utils.MiraiLogger

/**
 * Basic interface available to application. Usually wrapped with [SelectorNetworkHandler].
 *
 * Implementation is usually subclass of [NetworkHandlerSupport].
 *
 * Instances are often created by [NetworkHandlerFactory].
 *
 * @see NetworkHandlerSupport
 * @see NetworkHandlerFactory
 */
internal interface NetworkHandler : CoroutineScope {
    val context: NetworkHandlerContext

    fun isOk() = state == State.OK

    /**
     * Current state of this handler. This is volatile.
     */
    val state: State

    fun getLastFailure(): Throwable?

    /**
     * The channel that is sent with a [State] when changed.
     */
    val stateChannel: ReceiveChannel<State>

    /**
     * State of this handler.
     *
     * ## States transition overview
     *
     * There are 5 [State]s, each of which encapsulates the state of the network connection.
     *
     * Initial state is [State.INITIALIZED], at which no packets can be send before [resumeConnection], which transmits state into [State.CONNECTING].
     * On [State.CONNECTING], [NetworkHandler] establishes a connection with the server while [SsoProcessor] takes responsibility in the single-sign-on process.
     *
     * Successful logon turns state to [State.LOADING], an **open state**, which does nothing by default. Jobs can be *attached* by [StateObserver]s.
     * For example, attaching a [BotInitProcessor] to handle account-relevant jobs to the [Bot].
     *
     * Failure during [State.CONNECTING] and [State.LOADING] switches state to [State.CLOSED], on which [NetworkHandler] is considered **permanently dead**.
     * The state after finishing of [State.LOADING] is [State.OK]. This state lasts for the majority of time.
     *
     * When connection is lost (e.g. due to Internet unavailability), it does NOT return to [State.CONNECTING] but to [State.CLOSED]. No reconnection is allowed.
     * Retrial may only be performed in [SelectorNetworkHandler].
     *
     * @see state
     */
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
         * The terminal state. Both [resumeConnection] and [sendAndExpect] throw a [IllegalStateException].
         *
         * **Important nodes**: iff [NetworkHandler] is [SelectorNetworkHandler], it might return to a normal state e.g. [INITIALIZED] if new instance of [NetworkHandler] is created.
         * However callers usually do not need to pay extra attention on this behavior. Everything will just work fine if you consider [CLOSED] as a final, non-recoverable state.
         *
         * At this state [resumeConnection] throws the exception caught from underlying socket implementation (i.e netty).
         * [sendAndExpect] throws [IllegalStateException].
         */
        CLOSED,
    }

    /**
     * Suspends the coroutine until [sendAndExpect] can be executed without suspension or state is [State.CLOSED].
     *
     * In other words, if this functions returns, it indicates that [state] is [State.LOADING] or [State.OK]
     *
     * May throw exception that had caused current state to fail.
     * @see State
     */
    @Throws(Exception::class)
    suspend fun resumeConnection()


    /**
     * Sends [packet], suspends and expects to receive a response from the server.
     *
     * Coroutine suspension may happen if connection is not yet available however,
     * [IllegalStateException] is thrown if [NetworkHandler] is already in [State.CLOSED] since closure is final.
     *
     * @param attempts ranges `1..INFINITY`
     */
    suspend fun sendAndExpect(packet: OutgoingPacket, timeout: Long = 5000, attempts: Int = 2): Packet?

    /**
     * Sends [packet] and does not expect any response.
     *
     * Response is still being processed but not passed as a return value of this function, so it does not suspends this function (due to awaiting for the response).
     * However, coroutine is still suspended if connection is not yet available,
     * and [IllegalStateException] is thrown if [NetworkHandler] is already in [State.CLOSED] since closure is final.
     */
    suspend fun sendWithoutExpect(packet: OutgoingPacket)

    /**
     * Closes this handler gracefully (i.e. asynchronously).
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
 * Suspend coroutine to wait for the state [suspendUntil].
 */
internal suspend fun NetworkHandler.awaitState(suspendUntil: NetworkHandler.State) {
    if (this.state == suspendUntil) return
    stateChannel.consumeAsFlow().takeWhile { it != suspendUntil }.collect()
}

internal suspend fun NetworkHandler.awaitStateChange() {
    stateChannel.consumeAsFlow().first()
}