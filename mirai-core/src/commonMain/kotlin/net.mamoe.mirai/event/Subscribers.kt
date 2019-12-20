@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.Listener
import net.mamoe.mirai.event.internal.subscribeInternal
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/*
 * 该文件为所有的订阅事件的方法.
 */

/**
 * 订阅者的状态
 */ // Not using enum for Android
inline class ListeningStatus(inline val listening: Boolean) {
    companion object {
        /**
         * 表示继续监听
         */
        @JvmStatic
        val LISTENING = ListeningStatus(true)

        /**
         * 表示已停止
         */
        @JvmStatic
        val STOPPED = ListeningStatus(false)
    }
}


// region 顶层方法

/**
 * 订阅所有 [E] 及其子类事件.
 *
 * 将以当前协程的 job 为父 job 启动监听, 因此, 当当前协程运行结束后, 监听也会结束.
 * [handler] 将会有当前协程上下文执行, 即会被调用 [subscribe] 时的协程调度器执行
 */
suspend inline fun <reified E : Subscribable> subscribe(noinline handler: suspend E.(E) -> ListeningStatus): Listener<E> = E::class.subscribe(handler)

suspend inline fun <reified E : Subscribable> subscribeAlways(noinline listener: suspend E.(E) -> Unit): Listener<E> = E::class.subscribeAlways(listener)

suspend inline fun <reified E : Subscribable> subscribeOnce(noinline listener: suspend E.(E) -> Unit): Listener<E> = E::class.subscribeOnce(listener)

suspend inline fun <reified E : Subscribable, T> subscribeUntil(valueIfStop: T, noinline listener: suspend E.(E) -> T): Listener<E> =
    E::class.subscribeUntil(valueIfStop, listener)

suspend inline fun <reified E : Subscribable> subscribeUntilFalse(noinline listener: suspend E.(E) -> Boolean): Listener<E> =
    E::class.subscribeUntilFalse(listener)

suspend inline fun <reified E : Subscribable> subscribeUntilTrue(noinline listener: suspend E.(E) -> Boolean): Listener<E> =
    E::class.subscribeUntilTrue(listener)

suspend inline fun <reified E : Subscribable> subscribeUntilNull(noinline listener: suspend E.(E) -> Any?): Listener<E> = E::class.subscribeUntilNull(listener)


suspend inline fun <reified E : Subscribable, T> subscribeWhile(valueIfContinue: T, noinline listener: suspend E.(E) -> T): Listener<E> =
    E::class.subscribeWhile(valueIfContinue, listener)

suspend inline fun <reified E : Subscribable> subscribeWhileFalse(noinline listener: suspend E.(E) -> Boolean): Listener<E> =
    E::class.subscribeWhileFalse(listener)

suspend inline fun <reified E : Subscribable> subscribeWhileTrue(noinline listener: suspend E.(E) -> Boolean): Listener<E> =
    E::class.subscribeWhileTrue(listener)

suspend inline fun <reified E : Subscribable> subscribeWhileNull(noinline listener: suspend E.(E) -> Any?): Listener<E> = E::class.subscribeWhileNull(listener)

// endregion


// region KClass 的扩展方法 (不推荐)

@PublishedApi
internal suspend fun <E : Subscribable> KClass<E>.subscribe(handler: suspend E.(E) -> ListeningStatus) = this.subscribeInternal(Handler { it.handler(it) })

@PublishedApi
internal suspend fun <E : Subscribable> KClass<E>.subscribeAlways(listener: suspend E.(E) -> Unit) =
    this.subscribeInternal(Handler { it.listener(it); ListeningStatus.LISTENING })

@PublishedApi
internal suspend fun <E : Subscribable> KClass<E>.subscribeOnce(listener: suspend E.(E) -> Unit) =
    this.subscribeInternal(Handler { it.listener(it); ListeningStatus.STOPPED })

@PublishedApi
internal suspend fun <E : Subscribable, T> KClass<E>.subscribeUntil(valueIfStop: T, listener: suspend E.(E) -> T) =
    subscribeInternal(Handler { if (it.listener(it) == valueIfStop) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

@PublishedApi
internal suspend inline fun <E : Subscribable> KClass<E>.subscribeUntilFalse(noinline listener: suspend E.(E) -> Boolean) = subscribeUntil(false, listener)

@PublishedApi
internal suspend inline fun <E : Subscribable> KClass<E>.subscribeUntilTrue(noinline listener: suspend E.(E) -> Boolean) = subscribeUntil(true, listener)

@PublishedApi
internal suspend inline fun <E : Subscribable> KClass<E>.subscribeUntilNull(noinline listener: suspend E.(E) -> Any?) = subscribeUntil(null, listener)


@PublishedApi
internal suspend fun <E : Subscribable, T> KClass<E>.subscribeWhile(valueIfContinue: T, listener: suspend E.(E) -> T) =
    subscribeInternal(Handler { if (it.listener(it) !== valueIfContinue) ListeningStatus.STOPPED else ListeningStatus.LISTENING })

@PublishedApi
internal suspend inline fun <E : Subscribable> KClass<E>.subscribeWhileFalse(noinline listener: suspend E.(E) -> Boolean) = subscribeWhile(false, listener)

@PublishedApi
internal suspend inline fun <E : Subscribable> KClass<E>.subscribeWhileTrue(noinline listener: suspend E.(E) -> Boolean) = subscribeWhile(true, listener)

@PublishedApi
internal suspend inline fun <E : Subscribable> KClass<E>.subscribeWhileNull(noinline listener: suspend E.(E) -> Any?) = subscribeWhile(null, listener)

// endregion

// region ListenerBuilder DSL

/**
 * 监听一个事件. 可同时进行多种方式的监听
 * @see ListenerBuilder
 */
@ListenersBuilderDsl
@PublishedApi
internal suspend fun <E : Subscribable> KClass<E>.subscribeAll(listeners: suspend ListenerBuilder<E>.() -> Unit) {
    listeners(ListenerBuilder { this.subscribeInternal(it) })
}

/**
 * 监听一个事件. 可同时进行多种方式的监听
 * @see ListenerBuilder
 */
@ListenersBuilderDsl
suspend inline fun <reified E : Subscribable> subscribeAll(noinline listeners: suspend ListenerBuilder<E>.() -> Unit) = E::class.subscribeAll(listeners)

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
    @PublishedApi internal inline val handlerConsumer: suspend (Listener<E>) -> Unit
) {
    suspend inline fun handler(noinline listener: suspend E.(E) -> ListeningStatus) {
        handlerConsumer(Handler { it.listener(it) })
    }

    suspend inline fun always(noinline listener: suspend E.(E) -> Unit) = handler { listener(it); ListeningStatus.LISTENING }

    suspend inline fun <T> until(until: T, noinline listener: suspend E.(E) -> T) =
        handler { if (listener(it) == until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }

    suspend inline fun untilFalse(noinline listener: suspend E.(E) -> Boolean) = until(false, listener)
    suspend inline fun untilTrue(noinline listener: suspend E.(E) -> Boolean) = until(true, listener)
    suspend inline fun untilNull(noinline listener: suspend E.(E) -> Any?) = until(null, listener)


    suspend inline fun <T> `while`(until: T, noinline listener: suspend E.(E) -> T) =
        handler { if (listener(it) !== until) ListeningStatus.STOPPED else ListeningStatus.LISTENING }

    suspend inline fun whileFalse(noinline listener: suspend E.(E) -> Boolean) = `while`(false, listener)
    suspend inline fun whileTrue(noinline listener: suspend E.(E) -> Boolean) = `while`(true, listener)
    suspend inline fun whileNull(noinline listener: suspend E.(E) -> Any?) = `while`(null, listener)


    suspend inline fun once(noinline listener: suspend E.(E) -> Unit) = handler { listener(it); ListeningStatus.STOPPED }
}

@DslMarker
annotation class ListenersBuilderDsl

// endregion