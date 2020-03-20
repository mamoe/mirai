/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.subscribeInternal
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName

/*
 * 该文件为所有的订阅事件的方法.
 */

/**
 * 订阅者的状态
 */
enum class ListeningStatus {
    /**
     * 表示继续监听
     */
    LISTENING,

    /**
     * 表示已停止
     */
    STOPPED
}

/**
 * 事件监听器.
 * 由 [subscribe] 等方法返回.
 *
 * 取消监听: [complete]
 */
interface Listener<in E : Event> : CompletableJob {

    enum class ConcurrencyKind {
        /**
         * 并发地同时处理多个事件, 但无法保证 [onEvent] 返回 [ListeningStatus.STOPPED] 后立即停止事件监听.
         */
        CONCURRENT,

        /**
         * 使用 [Mutex] 保证同一时刻只处理一个事件.
         */
        LOCKED
    }

    /**
     * 并发类型
     */
    val concurrencyKind: ConcurrencyKind

    suspend fun onEvent(event: E): ListeningStatus
}

// region 顶层方法 创建当前 coroutineContext 下的子 Job

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行.
 *
 * 当 [handler] 返回 [ListeningStatus.STOPPED] 时停止监听.
 * 或 [Listener.complete] 后结束.
 *
 * 这个函数返回 [Listener], 它是一个 [CompletableJob]. 请注意它除非被 [Listener.complete] 或 [Listener.cancel], 则不会完成.
 * 例:
 * ```kotlin
 * runBlocking { // this: CoroutineScope
 *   subscribe<Event> { /* 一些处理 */ } // 返回 Listener, 即 CompletableJob
 * }
 * foo()
 * ```
 * `runBlocking` 不会结束, 也就是下一行 `foo()` 不会被执行. 直到监听时创建的 `Listener` 被停止.
 *
 *
 * 要创建一个仅在某个机器人在线时的监听, 请在 [Bot] 下调用本函数 (因为 [Bot] 也实现 [CoroutineScope]).
 * 这种方式创建的监听会自动筛选 [Bot].
 * ```kotlin
 * bot1.subscribe<BotEvent> { /* 只会处理来自 bot1 的事件 */ }
 * ```
 *
 *
 * 要创建一个全局都存在的监听, 即守护协程, 请在 [GlobalScope] 下调用本函数:
 * ```kotlin
 * GlobalScope.subscribe<Event> { /* 会收到来自全部 Bot 的事件和与 Bot 不相关的事件 */ }
 * ```
 *
 *
 * 事件处理时的 [CoroutineContext] 为调用本函数时的 [receiver][this] 的 [CoroutineScope.coroutineContext].
 * 因此:
 * - 事件处理时抛出的异常将会在 [this] 的 [CoroutineExceptionHandler] 中处理
 *   若 [this] 没有 [CoroutineExceptionHandler], 则在事件广播方的 [CoroutineExceptionHandler] 处理
 *   若均找不到, 则会触发 logger warning.
 * - 事件处理时抛出异常不会停止监听器.
 * - 建议在事件处理中 (即 [handler] 里) 处理异常,
 *   或在 [this] 的 [CoroutineScope.coroutineContext] 中添加 [CoroutineExceptionHandler].
 *
 *
 * **注意:** 事件处理是 `suspend` 的, 请规范处理 JVM 阻塞方法.
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 *
 * @see subscribingGet 监听一个事件, 并尝试从这个事件中获取一个值.
 * @see subscribingGetAsync 异步监听一个事件, 并尝试从这个事件中获取一个值.
 *
 * @see subscribeAlways 一直监听
 * @see subscribeOnce   只监听一次
 *
 * @see subscribeMessages       监听消息 DSL
 * @see subscribeGroupMessages  监听群消息 DSL
 * @see subscribeFriendMessages 监听好友消息 DSL
 */
@OptIn(MiraiInternalAPI::class)
inline fun <reified E : Event> CoroutineScope.subscribe(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = Listener.ConcurrencyKind.LOCKED,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> =
    E::class.subscribeInternal(Handler(coroutineContext, concurrency) { it.handler(it); })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [listener] 都会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [Bot] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 *
 * @see subscribe 获取更多说明
 */
@OptIn(MiraiInternalAPI::class, ExperimentalContracts::class)
inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = Listener.ConcurrencyKind.LOCKED,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> {
    contract {
        callsInPlace(listener, InvocationKind.UNKNOWN)
    }
    return E::class.subscribeInternal(
        Handler(
            coroutineContext,
            concurrency
        ) { it.listener(it); ListeningStatus.LISTENING })
}

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 仅在第一次 [事件广播][Event.broadcast] 时, [listener] 会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [Bot] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 *
 * @see subscribe 获取更多说明
 */
@OptIn(MiraiInternalAPI::class)
inline fun <reified E : Event> CoroutineScope.subscribeOnce(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> =
    E::class.subscribeInternal(
        Handler(
            coroutineContext,
            Listener.ConcurrencyKind.LOCKED
        ) { it.listener(it); ListeningStatus.STOPPED })


//
// 以下为带筛选 Bot 的监听
//


/**
 * 在 [Bot] 的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [handler] 都会被执行,
 * 当 [handler] 返回 [ListeningStatus.STOPPED] 时停止监听
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [Bot] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 *
 * @see subscribe 获取更多说明
 */
@JvmName("subscribeAlwaysForBot")
@OptIn(MiraiInternalAPI::class)
inline fun <reified E : BotEvent> Bot.subscribe(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = Listener.ConcurrencyKind.LOCKED,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> =
    E::class.subscribeInternal(
        Handler(
            coroutineContext,
            concurrency
        ) { if (it.bot === this) it.handler(it) else ListeningStatus.LISTENING })


/**
 * 在 [Bot] 的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [listener] 都会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [Bot] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 *
 * @see subscribe 获取更多说明
 */
@JvmName("subscribeAlwaysForBot1")
@OptIn(MiraiInternalAPI::class)
inline fun <reified E : BotEvent> Bot.subscribeAlways(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> {
    return E::class.subscribeInternal(
        Handler(
            coroutineContext,
            concurrency
        ) { if (it.bot === this) it.listener(it); ListeningStatus.LISTENING })
}

/**
 * 在 [Bot] 的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 仅在第一次 [事件广播][Event.broadcast] 时, [listener] 会被执行.
 *
 * 可在任意时候通过 [Listener.complete] 来主动停止监听.
 * [Bot] 被关闭后事件监听会被 [取消][Listener.cancel].
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 *
 * @see subscribe 获取更多说明
 */
@JvmName("subscribeOnceForBot2")
@OptIn(MiraiInternalAPI::class)
inline fun <reified E : BotEvent> Bot.subscribeOnce(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> =
    E::class.subscribeInternal(Handler(coroutineContext, Listener.ConcurrencyKind.LOCKED) {
        if (it.bot === this) {
            it.listener(it)
            ListeningStatus.STOPPED
        } else ListeningStatus.LISTENING
    })

// endregion