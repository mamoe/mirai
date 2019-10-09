@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.listeners
import net.mamoe.mirai.event.internal.subscribeInternal
import kotlin.reflect.KClass

enum class ListeningStatus {
    LISTENING,
    STOPPED
}

/* KClass 的扩展方法 */

@Synchronized
fun <E : Event> KClass<E>.subscribe(handler: suspend (E) -> ListeningStatus) = this.listeners.add(Handler(handler))

fun <E : Event> KClass<E>.subscribeAlways(listener: suspend (E) -> Unit) = this.subscribeInternal(Handler { listener(it); ListeningStatus.LISTENING })

fun <E : Event> KClass<E>.subscribeOnce(listener: suspend (E) -> Unit) = this.subscribeInternal(Handler { listener(it); ListeningStatus.STOPPED })

fun <E : Event, T> KClass<E>.subscribeUntil(valueIfStop: T, listener: suspend (E) -> T) = subscribeInternal(Handler { if (listener(it) === valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })
fun <E : Event> KClass<E>.subscribeUntilFalse(listener: suspend (E) -> Boolean) = subscribeUntil(false, listener)
fun <E : Event> KClass<E>.subscribeUntilTrue(listener: suspend (E) -> Boolean) = subscribeUntil(true, listener)
fun <E : Event> KClass<E>.subscribeUntilNull(listener: suspend (E) -> Any?) = subscribeUntil(null, listener)


fun <E : Event, T> KClass<E>.subscribeWhile(valueIfContinue: T, listener: suspend (E) -> T) = subscribeInternal(Handler { if (listener(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })
fun <E : Event> KClass<E>.subscribeWhileFalse(listener: suspend (E) -> Boolean) = subscribeWhile(false, listener)
fun <E : Event> KClass<E>.subscribeWhileTrue(listener: suspend (E) -> Boolean) = subscribeWhile(true, listener)
fun <E : Event> KClass<E>.subscribeWhileNull(listener: suspend (E) -> Any?) = subscribeWhile(null, listener)

/* 顶层方法 */

inline fun <reified E : Event> subscribe(noinline handler: suspend (E) -> ListeningStatus) = E::class.subscribe(handler)

inline fun <reified E : Event> subscribeAlways(noinline listener: suspend (E) -> Unit) = E::class.subscribeAlways(listener)

inline fun <reified E : Event> subscribeOnce(noinline listener: suspend (E) -> Unit) = E::class.subscribeOnce(listener)

inline fun <reified E : Event, T> subscribeUntil(valueIfStop: T, noinline listener: suspend (E) -> T) = E::class.subscribeUntil(valueIfStop, listener)
inline fun <reified E : Event> subscribeUntilFalse(noinline listener: suspend (E) -> Boolean) = E::class.subscribeUntilFalse(listener)
inline fun <reified E : Event> subscribeUntilTrue(noinline listener: suspend (E) -> Boolean) = E::class.subscribeUntilTrue(listener)
inline fun <reified E : Event> subscribeUntilNull(noinline listener: suspend (E) -> Any?) = E::class.subscribeUntilNull(listener)


inline fun <reified E : Event, T> subscribeWhile(valueIfContinue: T, noinline listener: suspend (E) -> T) = E::class.subscribeWhile(valueIfContinue, listener)
inline fun <reified E : Event> subscribeWhileFalse(noinline listener: suspend (E) -> Boolean) = E::class.subscribeWhileFalse(listener)
inline fun <reified E : Event> subscribeWhileTrue(noinline listener: suspend (E) -> Boolean) = E::class.subscribeWhileTrue(listener)
inline fun <reified E : Event> subscribeWhileNull(noinline listener: suspend (E) -> Any?) = E::class.subscribeWhileNull(listener)


/**
 * 监听一个事件. 可同时进行多种方式的监听
 * @see ListenerBuilder
 */
fun <E : Event> KClass<E>.subscribeAll(listeners: ListenerBuilder<E>.() -> Unit) {
    ListenerBuilder<E> { this.subscribeInternal(it) }.apply(listeners)
}

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
 *     it.reply("你发送了 ${it.message}")
 *     it.message eq "停止"
 *   }
 * }
 * ```
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
inline class ListenerBuilder<out E : Event>(
        private val handlerConsumer: (Handler<in E>) -> Unit
) {
    fun handler(listener: suspend (E) -> ListeningStatus) {
        handlerConsumer(Handler(listener))
    }

    fun always(listener: suspend (E) -> Unit) = handler { listener(it); ListeningStatus.LISTENING }

    fun <T> until(until: T, listener: suspend (E) -> T) = handler { if (listener(it) === until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }
    fun untilFalse(listener: suspend (E) -> Boolean) = until(false, listener)
    fun untilTrue(listener: suspend (E) -> Boolean) = until(true, listener)
    fun untilNull(listener: suspend (E) -> Any?) = until(null, listener)


    fun <T> `while`(until: T, listener: suspend (E) -> T) = handler { if (listener(it) !== until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }
    fun whileFalse(listener: suspend (E) -> Boolean) = `while`(false, listener)
    fun whileTrue(listener: suspend (E) -> Boolean) = `while`(true, listener)
    fun whileNull(listener: suspend (E) -> Any?) = `while`(null, listener)


    fun once(block: suspend (E) -> Unit) = handler { block(it); ListeningStatus.STOPPED }
}