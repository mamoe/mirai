package net.mamoe.mirai.event

import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.listeners
import net.mamoe.mirai.event.internal.subscribeInternal
import kotlin.reflect.KClass

enum class ListeningStatus {
    LISTENING,
    STOPPED
}

@Synchronized
fun <E : Event> KClass<E>.subscribe(handler: suspend (E) -> ListeningStatus) = this.listeners.add(Handler(handler))

fun <E : Event> KClass<E>.subscribeAlways(listener: suspend (E) -> Unit) = this.subscribeInternal(Handler { listener(it); ListeningStatus.LISTENING })

fun <E : Event> KClass<E>.subscribeOnce(listener: suspend (E) -> Unit) = this.subscribeInternal(Handler { listener(it); ListeningStatus.STOPPED })

fun <E : Event, T> KClass<E>.subscribeUntil(valueIfStop: T, block: suspend (E) -> T) = subscribeInternal(Handler { if (block(it) === valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })
fun <E : Event> KClass<E>.subscribeUntilFalse(block: suspend (E) -> Boolean) = subscribeUntil(false, block)
fun <E : Event> KClass<E>.subscribeUntilTrue(block: suspend (E) -> Boolean) = subscribeUntil(true, block)
fun <E : Event> KClass<E>.subscribeUntilNull(block: suspend (E) -> Any?) = subscribeUntil(null, block)


fun <E : Event, T> KClass<E>.subscribeWhile(valueIfContinue: T, block: suspend (E) -> T) = subscribeInternal(Handler { if (block(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })
fun <E : Event> KClass<E>.subscribeWhileFalse(block: suspend (E) -> Boolean) = subscribeWhile(false, block)
fun <E : Event> KClass<E>.subscribeWhileTrue(block: suspend (E) -> Boolean) = subscribeWhile(true, block)
fun <E : Event> KClass<E>.subscribeWhileNull(block: suspend (E) -> Any?) = subscribeWhile(null, block)


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
    fun handler(block: suspend (E) -> ListeningStatus) {
        handlerConsumer(Handler(block))
    }

    fun always(block: suspend (E) -> Unit) = handler { block(it); ListeningStatus.LISTENING }

    fun <T> until(until: T, block: suspend (E) -> T) = handler { if (block(it) === until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }
    fun untilFalse(block: suspend (E) -> Boolean) = until(false, block)
    fun untilTrue(block: suspend (E) -> Boolean) = until(true, block)
    fun untilNull(block: suspend (E) -> Any?) = until(null, block)


    fun <T> `while`(until: T, block: suspend (E) -> T) = handler { if (block(it) !== until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }
    fun whileFalse(block: suspend (E) -> Boolean) = `while`(false, block)
    fun whileTrue(block: suspend (E) -> Boolean) = `while`(true, block)
    fun whileNull(block: suspend (E) -> Any?) = `while`(null, block)


    fun once(block: suspend (E) -> Unit) = handler { block(it); ListeningStatus.STOPPED }
}