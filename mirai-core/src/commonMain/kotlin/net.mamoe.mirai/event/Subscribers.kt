@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.subscribeInternal
import kotlin.reflect.KClass

/*
 * 该文件为所有的订阅事件的方法.
 */

/**
 * 订阅者的状态
 */
enum class ListeningStatus {
    LISTENING,
    STOPPED
}


// region 顶层方法

suspend inline fun <reified E : Event> subscribe(noinline handler: suspend (E) -> ListeningStatus) = E::class.subscribe(handler)

suspend inline fun <reified E : Event> subscribeAlways(noinline listener: suspend (E) -> Unit) = E::class.subscribeAlways(listener)

suspend inline fun <reified E : Event> subscribeOnce(noinline listener: suspend (E) -> Unit) = E::class.subscribeOnce(listener)

suspend inline fun <reified E : Event, T> subscribeUntil(valueIfStop: T, noinline listener: suspend (E) -> T) = E::class.subscribeUntil(valueIfStop, listener)
suspend inline fun <reified E : Event> subscribeUntilFalse(noinline listener: suspend (E) -> Boolean) = E::class.subscribeUntilFalse(listener)
suspend inline fun <reified E : Event> subscribeUntilTrue(noinline listener: suspend (E) -> Boolean) = E::class.subscribeUntilTrue(listener)
suspend inline fun <reified E : Event> subscribeUntilNull(noinline listener: suspend (E) -> Any?) = E::class.subscribeUntilNull(listener)


suspend inline fun <reified E : Event, T> subscribeWhile(valueIfContinue: T, noinline listener: suspend (E) -> T) = E::class.subscribeWhile(valueIfContinue, listener)
suspend inline fun <reified E : Event> subscribeWhileFalse(noinline listener: suspend (E) -> Boolean) = E::class.subscribeWhileFalse(listener)
suspend inline fun <reified E : Event> subscribeWhileTrue(noinline listener: suspend (E) -> Boolean) = E::class.subscribeWhileTrue(listener)
suspend inline fun <reified E : Event> subscribeWhileNull(noinline listener: suspend (E) -> Any?) = E::class.subscribeWhileNull(listener)

// endregion


// region KClass 的扩展方法 (不推荐)

suspend fun <E : Event> KClass<E>.subscribe(handler: suspend (E) -> ListeningStatus) = this.subscribeInternal(Handler(handler))

suspend fun <E : Event> KClass<E>.subscribeAlways(listener: suspend (E) -> Unit) = this.subscribeInternal(Handler { listener(it); ListeningStatus.LISTENING })

suspend fun <E : Event> KClass<E>.subscribeOnce(listener: suspend (E) -> Unit) = this.subscribeInternal(Handler { listener(it); ListeningStatus.STOPPED })

suspend fun <E : Event, T> KClass<E>.subscribeUntil(valueIfStop: T, listener: suspend (E) -> T) =
    subscribeInternal(Handler { if (listener(it) === valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

suspend fun <E : Event> KClass<E>.subscribeUntilFalse(listener: suspend (E) -> Boolean) = subscribeUntil(false, listener)
suspend fun <E : Event> KClass<E>.subscribeUntilTrue(listener: suspend (E) -> Boolean) = subscribeUntil(true, listener)
suspend fun <E : Event> KClass<E>.subscribeUntilNull(listener: suspend (E) -> Any?) = subscribeUntil(null, listener)


suspend fun <E : Event, T> KClass<E>.subscribeWhile(valueIfContinue: T, listener: suspend (E) -> T) =
    subscribeInternal(Handler { if (listener(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

suspend fun <E : Event> KClass<E>.subscribeWhileFalse(listener: suspend (E) -> Boolean) = subscribeWhile(false, listener)
suspend fun <E : Event> KClass<E>.subscribeWhileTrue(listener: suspend (E) -> Boolean) = subscribeWhile(true, listener)
suspend fun <E : Event> KClass<E>.subscribeWhileNull(listener: suspend (E) -> Any?) = subscribeWhile(null, listener)

// endregion

// region ListenerBuilder DSL

/**
 * 监听一个事件. 可同时进行多种方式的监听
 * @see ListenerBuilder
 */
@ListenersBuilderDsl
suspend fun <E : Event> KClass<E>.subscribeAll(listeners: suspend ListenerBuilder<E>.() -> Unit) {
    with(ListenerBuilder<E> { this.subscribeInternal(it) }) {
        listeners()
    }
}

/**
 * 监听一个事件. 可同时进行多种方式的监听
 * @see ListenerBuilder
 */
@ListenersBuilderDsl
suspend inline fun <reified E : Event> subscribeAll(noinline listeners: suspend ListenerBuilder<E>.() -> Unit) = E::class.subscribeAll(listeners)

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
    private val handlerConsumer: suspend (Handler<in E>) -> Unit
) {
    suspend fun handler(listener: suspend (E) -> ListeningStatus) {
        handlerConsumer(Handler(listener))
    }

    suspend fun always(listener: suspend (E) -> Unit) = handler { listener(it); ListeningStatus.LISTENING }

    suspend fun <T> until(until: T, listener: suspend (E) -> T) = handler { if (listener(it) === until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }
    suspend fun untilFalse(listener: suspend (E) -> Boolean) = until(false, listener)
    suspend fun untilTrue(listener: suspend (E) -> Boolean) = until(true, listener)
    suspend fun untilNull(listener: suspend (E) -> Any?) = until(null, listener)


    suspend fun <T> `while`(until: T, listener: suspend (E) -> T) = handler { if (listener(it) !== until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }
    suspend fun whileFalse(listener: suspend (E) -> Boolean) = `while`(false, listener)
    suspend fun whileTrue(listener: suspend (E) -> Boolean) = `while`(true, listener)
    suspend fun whileNull(listener: suspend (E) -> Any?) = `while`(null, listener)


    suspend fun once(block: suspend (E) -> Unit) = handler { block(it); ListeningStatus.STOPPED }
}

@DslMarker
annotation class ListenersBuilderDsl

// endregion