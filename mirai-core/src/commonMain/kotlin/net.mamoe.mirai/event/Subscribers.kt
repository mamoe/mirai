package net.mamoe.mirai.event

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.subscribeInternal

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
 */
interface Listener<in E : Subscribable> : CompletableJob {
    suspend fun onEvent(event: E): ListeningStatus
}

// region 顶层方法 创建当前 coroutineContext 下的子 Job

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Subscribable.broadcast] 时, [handler] 都会被执行.
 *
 * 当 [handler] 返回 [ListeningStatus.STOPPED] 时停止监听.
 * 或 [Listener] complete 时结束.
 *
 *
 * **注意**: 这个函数返回 [Listener], 它是一个 [CompletableJob]. 如果不手动 [CompletableJob.complete], 它将会阻止当前 [CoroutineScope] 结束.
 * 例如:
 * ```kotlin
 * runBlocking { // this: CoroutineScope
 *   subscribe<Subscribable> { /* 一些处理 */ } // 返回 Listener, 即 CompletableJob
 * }
 * foo()
 * ```
 * `runBlocking` 不会结束, 也就是下一行 `foo()` 不会被执行. 直到监听时创建的 `Listener` 被停止.
 *
 *
 * 要创建一个全局都存在的监听, 即守护协程, 请在 [GlobalScope] 下调用本函数:
 * ```kotlin
 * GlobalScope.subscribe<Subscribable> { /* 一些处理 */ }
 * ```
 *
 *
 * 要创建一个仅在机器人在线时的监听, 请在 [Bot] 下调用本函数 (因为 [Bot] 也实现 [CoroutineScope]):
 * ```kotlin
 * bot.subscribe<Subscribe> { /* 一些处理 */ }
 * ```
 */
inline fun <reified E : Subscribable> CoroutineScope.subscribe(crossinline handler: suspend E.(E) -> ListeningStatus): Listener<E> =
    E::class.subscribeInternal(Handler { it.handler(it) })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Subscribable.broadcast] 时, [listener] 都会被执行.
 *
 * 仅当 [Listener] complete 时结束.
 *
 * @see subscribe 获取更多说明
 */
inline fun <reified E : Subscribable> CoroutineScope.subscribeAlways(crossinline listener: suspend E.(E) -> Unit): Listener<E> =
    E::class.subscribeInternal(Handler { it.listener(it); ListeningStatus.LISTENING })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 仅在第一次 [事件广播][Subscribable.broadcast] 时, [listener] 会被执行.
 *
 * 在这之前, 可通过 [Listener.complete] 来停止监听.
 *
 * @see subscribe 获取更多说明
 */
inline fun <reified E : Subscribable> CoroutineScope.subscribeOnce(crossinline listener: suspend E.(E) -> Unit): Listener<E> =
    E::class.subscribeInternal(Handler { it.listener(it); ListeningStatus.STOPPED })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Subscribable.broadcast] 时, [listener] 都会被执行, 直到 [listener] 的返回值 [equals] 于 [valueIfStop]
 *
 * 可在任意时刻通过 [Listener.complete] 来停止监听.
 *
 * @see subscribe 获取更多说明
 */
inline fun <reified E : Subscribable, T> CoroutineScope.subscribeUntil(valueIfStop: T, crossinline listener: suspend E.(E) -> T): Listener<E> =
    E::class.subscribeInternal(Handler { if (it.listener(it) == valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

/**
 * 在指定的 [CoroutineScope] 下订阅所有 [E] 及其子类事件.
 * 每当 [事件广播][Subscribable.broadcast] 时, [listener] 都会被执行,
 * 如果 [listener] 的返回值 [equals] 于 [valueIfContinue], 则继续监听, 否则停止
 *
 * 可在任意时刻通过 [Listener.complete] 来停止监听.
 *
 * @see subscribe 获取更多说明
 */
inline fun <reified E : Subscribable, T> CoroutineScope.subscribeWhile(valueIfContinue: T, crossinline listener: suspend E.(E) -> T): Listener<E> =
    E::class.subscribeInternal(Handler { if (it.listener(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

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
inline class ListenerBuilder<out E : Subscribable>(
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