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
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.subscribeInternal
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

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
 * 要创建一个全局都存在的监听, 即守护协程, 请在 [GlobalScope] 下调用本函数:
 * ```kotlin
 * GlobalScope.subscribe<Event> { /* 一些处理 */ }
 * ```
 *
 *
 * 要创建一个仅在某个机器人在线时的监听, 请在 [Bot] 下调用本函数 (因为 [Bot] 也实现 [CoroutineScope]):
 * ```kotlin
 * bot.subscribe<Subscribe> { /* 一些处理 */ }
 * ```
 *
 *
 * 事件处理时的 [CoroutineContext] 为调用本函数时的 [receiver][this] 的 [CoroutineScope.coroutineContext].
 * 因此:
 * - 事件处理时抛出的异常将会在 [this] 的 [CoroutineExceptionHandler] 中处理
 *   若 [this] 没有 [CoroutineExceptionHandler], 则在事件广播方的 [CoroutineExceptionHandler] 处理
 *   若均找不到, 则会触发 logger warning.
 * - 事件处理时抛出异常不会停止监听器.
 * - 建议在事件处理中, 即 [handler] 里处理异常, 或在 [this] 指定 [CoroutineExceptionHandler].
 *
 *
 * **注意:** 事件处理是 `suspend` 的, 请严格控制 JVM 阻塞方法的使用. 若致事件处理阻塞, 则会导致一些逻辑无法进行.
 *
 * // TODO: 2020/2/13 在 bot 下监听时同时筛选对应 bot 实例
 *
 * @see subscribeMessages 监听消息 DSL
 * @see subscribeGroupMessages 监听群消息 DSL
 * @see subscribeFriendMessages 监听好友消息 DSL
 */
@UseExperimental(MiraiInternalAPI::class)
inline fun <reified E : Event> CoroutineScope.subscribe(crossinline handler: suspend E.(E) -> ListeningStatus): Listener<E> =
    E::class.subscribeInternal(Handler { it.handler(it); })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [listener] 都会被执行.
 *
 * 仅当 [Listener.complete] 或 [Listener.cancel] 时结束.
 *
 * @see subscribe 获取更多说明
 */
@UseExperimental(MiraiInternalAPI::class, ExperimentalContracts::class)
inline fun <reified E : Event> CoroutineScope.subscribeAlways(crossinline listener: suspend E.(E) -> Unit): Listener<E> {
    contract {
        callsInPlace(listener, InvocationKind.UNKNOWN)
    }
    return E::class.subscribeInternal(Handler { it.listener(it); ListeningStatus.LISTENING })
}

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 仅在第一次 [事件广播][Event.broadcast] 时, [listener] 会被执行.
 *
 * 在这之前, 可通过 [Listener.complete] 来停止监听.
 *
 * @see subscribe 获取更多说明
 */
@UseExperimental(MiraiInternalAPI::class)
inline fun <reified E : Event> CoroutineScope.subscribeOnce(crossinline listener: suspend E.(E) -> Unit): Listener<E> =
    E::class.subscribeInternal(Handler { it.listener(it); ListeningStatus.STOPPED })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [listener] 都会被执行, 直到 [listener] 的返回值 [equals] 于 [valueIfStop]
 *
 * 可在任意时刻通过 [Listener.complete] 来停止监听.
 *
 * @see subscribe 获取更多说明
 */
@UseExperimental(MiraiInternalAPI::class)
inline fun <reified E : Event, T> CoroutineScope.subscribeUntil(valueIfStop: T, crossinline listener: suspend E.(E) -> T): Listener<E> =
    E::class.subscribeInternal(Handler { if (it.listener(it) == valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Event.broadcast] 时, [listener] 都会被执行,
 * 如果 [listener] 的返回值 [equals] 于 [valueIfContinue], 则继续监听, 否则停止
 *
 * 可在任意时刻通过 [Listener.complete] 来停止监听.
 *
 * @see subscribe 获取更多说明
 */
@UseExperimental(MiraiInternalAPI::class)
inline fun <reified E : Event, T> CoroutineScope.subscribeWhile(valueIfContinue: T, crossinline listener: suspend E.(E) -> T): Listener<E> =
    E::class.subscribeInternal(Handler { if (it.listener(it) != valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

// endregion

// region ListenerBuilder DSL

/*
/**
 * 监听构建器. 可同时进行多种方式的监听
 *
 * ```kotlin
 * FriendMessageEvent.subscribe {
 *   always{
 *     it.reply("永远发生")
 *   }
 *
 *   untilFalse {
 *     it.reply("你发送了 ${it.event}")
 *     it.event eq "停止"
 *   }
 * }
 * ```
 */
@ListenersBuilderDsl
@Suppress("MemberVisibilityCanBePrivate", "unused")
inline class ListenerBuilder<out E : Event>(
    @PublishedApi internal inline val handlerConsumer: CoroutineCoroutineScope.(Listener<E>) -> Unit
) {
    fun CoroutineCoroutineScope.handler(listener: suspend E.(E) -> ListeningStatus) {
        handlerConsumer(Handler { it.listener(it) })
    }

    fun CoroutineCoroutineScope.always(listener: suspend E.(E) -> Unit) = handler { listener(it); ListeningStatus.LISTENING }

    fun <T> CoroutineCoroutineScope.until(until: T, listener: suspend E.(E) -> T) =
        handler { if (listener(it) == until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }

    fun CoroutineCoroutineScope.untilFalse(listener: suspend E.(E) -> Boolean) = until(false, listener)
    fun CoroutineCoroutineScope.untilTrue(listener: suspend E.(E) -> Boolean) = until(true, listener)
    fun CoroutineCoroutineScope.untilNull(listener: suspend E.(E) -> Any?) = until(null, listener)


    fun <T> CoroutineCoroutineScope.`while`(until: T, listener: suspend E.(E) -> T) =
        handler { if (listener(it) !== until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }

    fun CoroutineCoroutineScope.whileFalse(listener: suspend E.(E) -> Boolean) = `while`(false, listener)
    fun CoroutineCoroutineScope.whileTrue(listener: suspend E.(E) -> Boolean) = `while`(true, listener)
    fun CoroutineCoroutineScope.whileNull(listener: suspend E.(E) -> Any?) = `while`(null, listener)


    fun CoroutineCoroutineScope.once(listener: suspend E.(E) -> Unit) = handler { listener(it); ListeningStatus.STOPPED }
}

@DslMarker
annotation class ListenersBuilderDsl
*/
// endregion