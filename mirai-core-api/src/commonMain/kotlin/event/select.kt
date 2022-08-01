/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DuplicatedCode")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.isContextIdenticalWith
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 挂起当前协程, 等待任意一个事件监听器返回 `false` 后返回.
 *
 * 创建的所有事件监听器都会判断发送人信息 ([isContextIdenticalWith]), 监听之后的所有消息.
 *
 * [selectBuilder] DSL 类似于 [EventChannel.subscribeMessages] 的 DSL, 屏蔽了一些 `reply` DSL 以确保类型安全
 *
 * ```kotlin
 * reply("开启复读模式")
 *
 * whileSelectMessages {
 *     "stop" {
 *         reply("已关闭复读")
 *         false // 停止循环
 *     }
 *     // 也可以使用 startsWith("") { true } 等 DSL
 *     default {
 *         reply(message)
 *         true // 继续循环
 *     }
 *     timeout(3000) {
 *         // on
 *         true
 *     }
 * } // 等待直到 `false`
 *
 * reply("复读模式结束")
 * ```
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @see EventChannel.subscribe
 * @see EventChannel.subscribeMessages
 * @see nextMessage 挂起协程并等待下一条消息
 */
@Suppress("unused")
@BuilderInference
public suspend inline fun <reified T : MessageEvent> T.whileSelectMessages(
    timeoutMillis: Long = -1,
    filterContext: Boolean = true,
    priority: EventPriority = EventPriority.MONITOR,
    @BuilderInference crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, Boolean>.() -> Unit
): Unit = whileSelectMessagesImpl(timeoutMillis, filterContext, priority, selectBuilder)

/**
 * [selectMessages] 的 [Unit] 返回值捷径 (由于 Kotlin 无法推断 [Unit] 类型)
 */
@MiraiExperimentalApi
@JvmName("selectMessages1")
@BuilderInference
public suspend inline fun <reified T : MessageEvent> T.selectMessagesUnit(
    timeoutMillis: Long = -1,
    filterContext: Boolean = true,
    priority: EventPriority = EventPriority.MONITOR,
    @BuilderInference crossinline selectBuilder: @MessageDsl MessageSelectBuilderUnit<T, Unit>.() -> Unit
): Unit = selectMessagesImpl(timeoutMillis, true, filterContext, priority, selectBuilder)


/**
 * 挂起当前协程, 等待任意一个事件监听器触发后返回其返回值.
 *
 * 创建的所有事件监听器都会判断发送人信息 ([isContextIdenticalWith]), 监听之后的所有消息.
 *
 * [selectBuilder] DSL 类似于 [EventChannel.subscribeMessages] 的 DSL, 屏蔽了一些 `reply` DSL 以确保类型安全
 *
 * ```kotlin
 * val value: String = selectMessages {
 *   "hello" { "111" }
 *   "hi" { "222" }
 *   startsWith("/") { it }
 *   default { "default" }
 * }
 * ```
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @see nextMessage 挂起协程并等待下一条消息
 */
@Suppress("unused") // false positive
@BuilderInference
public suspend inline fun <reified T : MessageEvent, R> T.selectMessages(
    timeoutMillis: Long = -1,
    filterContext: Boolean = true,
    priority: EventPriority = EventPriority.MONITOR,
    @BuilderInference
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, R>.() -> Unit
): R =
    selectMessagesImpl(
        timeoutMillis,
        false,
        filterContext,
        priority
    ) { selectBuilder.invoke(this as MessageSelectBuilder<T, R>) }

/**
 * [selectMessages] 时的 DSL 构建器.
 *
 * 它是特殊化的消息监听 ([EventChannel.subscribeMessages]) DSL, 屏蔽了一些 `reply` DSL 以确保作用域安全性
 *
 * @see MessageSelectBuilderUnit 查看上层 API
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
public abstract class MessageSelectBuilder<M : MessageEvent, R> @PublishedApi internal constructor(
    ownerMessagePacket: M,
    stub: Any?,
    subscriber: (M.(String) -> Boolean, MessageListener<M, Any?>) -> Unit
) : MessageSelectBuilderUnit<M, R>(ownerMessagePacket, stub, subscriber) {

    // 这些函数无法获取返回值. 必须屏蔽.

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun <N : Any> mapping(
        mapper: M.(String) -> N?,
        onEvent: @MessageDsl suspend M.(N) -> R
    ): Nothing = error("prohibited")

    @Deprecated("Use `default` instead", level = DeprecationLevel.HIDDEN)
    override fun always(onEvent: MessageListener<M, Any?>): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.reply(block: suspend () -> Any?): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.reply(message: String): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.reply(message: Message): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.quoteReply(block: suspend () -> Any?): Nothing =
        error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.quoteReply(message: String): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.quoteReply(message: Message): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.containsReply(reply: String): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.containsReply(replier: suspend M.(String) -> Any?): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun Regex.matchingReply(replier: suspend M.(MatchResult) -> Any?): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun Regex.findingReply(replier: suspend M.(MatchResult) -> Any?): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.endsWithReply(replier: suspend M.(String) -> Any?): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.reply(reply: String): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.reply(reply: Message): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.reply(replier: suspend M.(String) -> Any?): Nothing = error("prohibited")


    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.reply(toReply: String): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.reply(message: Message): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.reply(replier: suspend M.(String) -> Any?): Nothing =
        error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.quoteReply(toReply: String): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.quoteReply(toReply: Message): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.quoteReply(replier: suspend M.(String) -> Any?): Nothing = error("prohibited")
}

@JvmInline
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
public value class MessageSelectionTimeoutChecker internal constructor(public val timeoutMillis: Long)

public class MessageSelectionTimeoutException : RuntimeException()


/////////////////////////
//// implementations ////
/////////////////////////


@JvmSynthetic
@PublishedApi
internal suspend inline fun <R> withSilentTimeoutOrCoroutineScope(
    timeoutMillis: Long,
    noinline block: suspend CoroutineScope.() -> R
): R {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0 " }

    return withContext(ExceptionHandlerIgnoringCancellationException) {
        if (timeoutMillis == -1L) {
            coroutineScope(block)
        } else {
            withTimeout(timeoutMillis, block)
        }
    }
}

@PublishedApi
internal val SELECT_MESSAGE_STUB: Any = Any()

@PublishedApi
internal val ExceptionHandlerIgnoringCancellationException: CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        if (throwable !is CancellationException) {
            throw throwable
        }
    }

@PublishedApi
@BuilderInference
internal suspend inline fun <reified T : MessageEvent, R> T.selectMessagesImpl(
    timeoutMillis: Long = -1,
    isUnit: Boolean,
    filterContext: Boolean = true,
    priority: EventPriority,
    @BuilderInference
    crossinline selectBuilder: @MessageDsl MessageSelectBuilderUnit<T, R>.() -> Unit
): R = withSilentTimeoutOrCoroutineScope(timeoutMillis) {
    var deferred: CompletableDeferred<R>? = CompletableDeferred()
    coroutineContext[Job]!!.invokeOnCompletion {
        deferred?.cancel()
    }

    // ensure sequential invoking
    val listeners: MutableList<Pair<T.(String) -> Boolean, MessageListener<T, Any?>>> = mutableListOf()
    val defaultListeners: MutableList<MessageListener<T, Any?>> = mutableListOf()

    if (isUnit) {
        // https://youtrack.jetbrains.com/issue/KT-37716
        val outside = { filter: T.(String) -> Boolean, listener: MessageListener<T, Any?> ->
            listeners += filter to listener
        }
        object : MessageSelectBuilderUnit<T, R>(
            this@selectMessagesImpl,
            SELECT_MESSAGE_STUB,
            outside
        ) {
            override fun obtainCurrentCoroutineScope(): CoroutineScope = this@withSilentTimeoutOrCoroutineScope
            override fun obtainCurrentDeferred(): CompletableDeferred<R>? = deferred
            override fun default(onEvent: MessageListener<T, R>) {
                defaultListeners += onEvent
            }
        }
    } else {
        // https://youtrack.jetbrains.com/issue/KT-37716
        val outside = { filter: T.(String) -> Boolean, listener: MessageListener<T, Any?> ->
            listeners += filter to listener
        }
        object : MessageSelectBuilder<T, R>(
            this@selectMessagesImpl,
            SELECT_MESSAGE_STUB,
            outside
        ) {
            override fun obtainCurrentCoroutineScope(): CoroutineScope = this@withSilentTimeoutOrCoroutineScope
            override fun obtainCurrentDeferred(): CompletableDeferred<R>? = deferred
            override fun default(onEvent: MessageListener<T, R>) {
                defaultListeners += onEvent
            }
        }
    }.apply(selectBuilder)

    // we don't have any way to reduce duplication yet,
    // until local functions are supported in inline functions
    @Suppress("DuplicatedCode") val subscribeAlways = globalEventChannel().subscribeAlways<T>(
        concurrency = ConcurrencyKind.LOCKED,
        priority = priority
    ) { event ->
        if (filterContext && !this.isContextIdenticalWith(this@selectMessagesImpl))
            return@subscribeAlways

        val toString = event.message.contentToString()
        listeners.forEach { (filter, listener) ->
            if (deferred?.isCompleted == true || !isActive)
                return@subscribeAlways

            if (filter.invoke(event, toString)) {
                // same to the one below
                val value = listener.invoke(event, toString)
                if (value !== SELECT_MESSAGE_STUB) {
                    @Suppress("UNCHECKED_CAST")
                    deferred?.complete(value as R)
                    return@subscribeAlways
                } else if (isUnit) { // value === stub
                    // unit mode: we can directly complete this selection
                    @Suppress("UNCHECKED_CAST")
                    deferred?.complete(Unit as R)
                }
            }
        }
        defaultListeners.forEach { listener ->
            // same to the one above
            val value = listener.invoke(event, toString)
            if (value !== SELECT_MESSAGE_STUB) {
                @Suppress("UNCHECKED_CAST")
                deferred?.complete(value as R)
                return@subscribeAlways
            } else if (isUnit) { // value === stub
                // unit mode: we can directly complete this selection
                @Suppress("UNCHECKED_CAST")
                deferred?.complete(Unit as R)
            }
        }
    }

    deferred!!.await().also {
        subscribeAlways.complete()
        deferred = null
        coroutineContext.cancelChildren()
    }
}

@Suppress("unused")
@PublishedApi
internal suspend inline fun <reified T : MessageEvent> T.whileSelectMessagesImpl(
    timeoutMillis: Long,
    filterContext: Boolean,
    priority: EventPriority,
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, Boolean>.() -> Unit
): Unit = withSilentTimeoutOrCoroutineScope(timeoutMillis) {
    var deferred: CompletableDeferred<Boolean>? = CompletableDeferred()
    coroutineContext[Job]!!.invokeOnCompletion {
        deferred?.cancel()
    }

    // ensure sequential invoking
    val listeners: MutableList<Pair<T.(String) -> Boolean, MessageListener<T, Any?>>> = mutableListOf()
    val defaultListeners: MutableList<MessageListener<T, Any?>> = mutableListOf()

    // https://youtrack.jetbrains.com/issue/KT-37716
    val outside = { filter: T.(String) -> Boolean, listener: MessageListener<T, Any?> ->
        listeners += filter to listener
    }
    object : MessageSelectBuilder<T, Boolean>(
        this@whileSelectMessagesImpl,
        SELECT_MESSAGE_STUB,
        outside
    ) {
        override fun obtainCurrentCoroutineScope(): CoroutineScope = this@withSilentTimeoutOrCoroutineScope
        override fun obtainCurrentDeferred(): CompletableDeferred<Boolean>? = deferred
        override fun default(onEvent: MessageListener<T, Boolean>) {
            defaultListeners += onEvent
        }
    }.apply(selectBuilder)

    // ensure atomic completing
    val subscribeAlways = globalEventChannel().subscribeAlways<T>(
        concurrency = ConcurrencyKind.LOCKED,
        priority = priority
    ) { event ->
        if (filterContext && !this.isContextIdenticalWith(this@whileSelectMessagesImpl))
            return@subscribeAlways

        val toString = event.message.contentToString()
        listeners.forEach { (filter, listener) ->
            if (deferred?.isCompleted != false || !isActive)
                return@subscribeAlways

            if (filter.invoke(event, toString)) {
                listener.invoke(event, toString).let { value ->
                    if (value !== SELECT_MESSAGE_STUB) {
                        deferred?.complete(value as Boolean)
                        return@subscribeAlways // accept the first value only
                    }
                }
            }
        }
        defaultListeners.forEach { listener ->
            listener.invoke(event, toString).let { value ->
                if (value !== SELECT_MESSAGE_STUB) {
                    deferred?.complete(value as Boolean)
                    return@subscribeAlways // accept the first value only
                }
            }
        }
    }

    while (deferred?.await() == true) {
        deferred = CompletableDeferred()
    }
    subscribeAlways.complete()
    deferred = null
    coroutineContext.cancelChildren()
}
