/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.BotInitProcessor
import net.mamoe.mirai.internal.network.components.HeartbeatScheduler
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.network.handler.selector.MaxAttemptsReachedException
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.PacketFactory
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.systemProp
import net.mamoe.mirai.utils.unwrapCancellationException

/**
 * Coroutine-based network framework. Usually wrapped with [SelectorNetworkHandler] to enable retrying.
 *
 * Implementation is typically subclass of [NetworkHandlerSupport].
 *
 * Instances are often created by [NetworkHandlerFactory].
 *
 * For more information, see [State].
 *
 * @see NetworkHandlerSupport
 * @see NetworkHandlerFactory
 */
internal interface NetworkHandler : CoroutineScope {
    val context: NetworkHandlerContext

    /**
     * Current state of this handler.
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
     * Initial state is [State.INITIALIZED], at which no packets can be sent before [resumeConnection], which transmits state into [State.CONNECTING].
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
     * ## 深入了解状态维护机制
     *
     * ### 登录时的状态转移
     *
     * [NetworkHandler] 实例会在 [登录][Bot.login] 时创建 (定义在 [AbstractBot.network]), 此时初始状态为 [INITIALIZED].
     *
     * [Bot.login] 会在创建该实例后就执行 [NetworkHandler.resumeConnection], 意图是将 [NetworkHandler] **调整**为可以收发功能数据包的正常工作状态, 即 [OK].
     * 为了达成这个状态, [NetworkHandler] 将需要通过 [INITIALIZED] -> [CONNECTING] -> [LOADING] -> [OK], 即**初始化** -> **连接服务器** -> **载入** -> **完成**流程.
     *
     * 在 [CONNECTING] 状态中, [NetworkHandler] 将会与服务器建立网络连接, 并执行登录.
     * 若成功, 状态进入 [LOADING], 此时 [NetworkHandler] 将会从服务器拉取好友列表等必要信息.
     * 拉取顺利完成后状态进入 [OK], 用于维护登录会话的心跳任务等协程将会由启动 [HeartbeatScheduler] 启动, [NetworkHandler] 现在可以收发功能数据包. [NetworkHandler.resumeConnection] 返回, [Bot.login] 返回.
     *
     * ### 单向状态转移与"重置"
     *
     * [NetworkHandler] 的实现除了 [SelectorNetworkHandler] 外, 都只设计实现单向的状态转移,
     * 这意味着它们最终的状态都是 [CLOSED], 并且一旦到达 [CLOSED] 就无法回到之前的状态.
     *
     * [Selector][SelectorNetworkHandler] 是对某一个 [NetworkHandler] 的包装, 它为 [NetworkHandler] 增加异常处理和"可重置"的状态转移能力.
     * 若在单向状态转移过程 (比如登录) 中出现异常, 异常会被传递到 [NetworkHandler.resumeConnection] 抛出.
     *
     * ### 异常的区分
     *
     * 网络系统中的异常是有语义的 — 它如果是 [NetworkException], [Selector][SelectorNetworkHandler] 会使用异常的 [NetworkException.recoverable] 信息判断是否应该"重置"状态.
     * [NetworkException] 是已知的异常. 而任何其他的异常, 例如 [IllegalStateException], [NoSuchElementException], 都是未知的异常.
     * 未知的异常会被认为是严重的内部错误, [Selector][SelectorNetworkHandler] 会[关闭 bot][Bot.close]. (提醒: 关闭 bot 是终止操作, 标志 bot 实例的结束)
     *
     * 你在 [NetworkHandler.resumeConnection] 和 [Selector][SelectorNetworkHandler] 中需要注意异常类型, 为函数清晰注释其可能抛出的异常类型.
     *
     * [Selector][SelectorNetworkHandler] 不会立即将异常向上传递, 而是会根据异常类型尝试"重置"状态, 以处理网络状态不佳或服务器要求重连到其他地址等情况.
     *
     * ### Selector 重置状态
     *
     * [Selector][SelectorNetworkHandler] 通过实例化新的 [NetworkHandler] 来获得状态为 [INITIALIZED] 的实例, 对外来说就是"重置"了状态.
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
     * Suspends the coroutine until [sendAndExpect] can be executed without suspension.
     *
     * If this functions returns normally, it indicates that [state] is [State.LOADING] or [State.OK]
     *
     * @throws NetworkException 已知的异常
     * @throws Throwable 其他内部错误
     * @throws MaxAttemptsReachedException 重试次数达到上限
     * @throws CancellationException 协程被取消
     *
     * @see SelectorNetworkHandler.instance
     */
    @Throws(NetworkException::class, Throwable::class)
    suspend fun resumeConnection()


    /**
     * Sends [packet], suspends and expects to receive a response from the server.
     *
     * Coroutine suspension may happen if connection is not yet available however,
     * [IllegalStateException] is thrown if [NetworkHandler] is already in [State.CLOSED] since closure is final.
     *
     * @throws TimeoutCancellationException if timeout has been reached.
     * @throws CancellationException if the [NetworkHandler] is closed, with the last cause for closure.
     * @throws IllegalArgumentException if [timeout] or [attempts] are invalid.
     *
     * @param attempts ranges `1..INFINITY`
     */
    suspend fun <P : Packet?> sendAndExpect(
        packet: OutgoingPacketWithRespType<P>,
        timeout: Long = 5000,
        attempts: Int = 2
    ): P

    /**
     * Sends [packet], suspends and expects to receive a response from the server.
     *
     * Note that it's corresponding [PacketFactory]'s responsibility to decode the returned date from server to give resultant [Packet],
     * and that packet is cast to [P], hence unsafe (vulnerable for [ClassCastException]).
     * Always use [sendAndExpect] with [OutgoingPacketWithRespType], use this unsafe overload only for legacy code.
     */
    suspend fun <P : Packet?> sendAndExpect(packet: OutgoingPacket, timeout: Long = 5000, attempts: Int = 2): P

    /**
     * Sends [packet] and does not expect any response.
     *
     * Response is still being processed but not passed as a return value of this function, so it does not suspend this function (due to awaiting for the response).
     * However, coroutine is still suspended if connection is not yet available.
     * [IllegalStateException] will be thrown if [NetworkHandler] is already in [State.CLOSED] since closure is final, since closure is final.
     *
     * @throws CancellationException if the [NetworkHandler] is closed, with the last cause for closure.
     */
    suspend fun sendWithoutExpect(packet: OutgoingPacket)

    /**
     * Closes this handler gracefully (i.e. asynchronously). You should provide [cause] as possible as you can to give debugging information.
     *
     * After invocation of [close], [state] will always be [State.CLOSED].
     */
    fun close(cause: Throwable?)

    companion object {
        /**
         * [unwrapCancellationException] will give a rather long and complicated trace showing all internal traces.
         * The traces may help locate where the [Job.cancel] is called, but this is not usually important.
         * @since 2.13
         */
        var CANCELLATION_TRACE by atomic(systemProp("mirai.network.handler.cancellation.trace", false))

        inline fun <R> runUnwrapCancellationException(block: () -> R): R {
            try {
                return block()
            } catch (e: CancellationException) {
                // e is like `Exception in thread "main" kotlinx.coroutines.JobCancellationException: Parent job is Cancelling; job=JobImpl{Cancelled}@f252f300`
                // and this is useless.

                throw e.unwrapCancellationException(CANCELLATION_TRACE)

                // if (e.suppressed.isNotEmpty()) throw e // preserve details.
                // throw e.findCause { it !is CancellationException } ?: e
            }
        }
    }
}

internal val NetworkHandler.logger: MiraiLogger get() = context.logger

/**
 * Suspends coroutine to wait for the state [suspendUntil].
 */
internal suspend fun NetworkHandler.awaitState(suspendUntil: NetworkHandler.State) {
    if (this.state == suspendUntil) return
    stateChannel.consumeAsFlow().takeWhile { it != suspendUntil }.collect()
}

/**
 * Suspends coroutine to wait for a state change. Returns immediately after [NetworkHandler.state] has changed.
 */
internal suspend fun NetworkHandler.awaitStateChange() {
    stateChannel.consumeAsFlow().first()
}