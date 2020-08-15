/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DuplicatedCode")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.isContextIdenticalWith
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 挂起当前协程, 等待任意一个事件监听器返回 `false` 后返回.
 *
 * 创建的所有事件监听器都会判断发送人信息 ([isContextIdenticalWith]), 监听之后的所有消息.
 *
 * [selectBuilder] DSL 类似于 [CoroutineScope.subscribeMessages] 的 DSL, 屏蔽了一些 `reply` DSL 以确保类型安全
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
 * @see subscribe
 * @see subscribeMessages
 * @see nextMessage 挂起协程并等待下一条消息
 */
@Suppress("unused")
suspend inline fun <reified T : MessageEvent> T.whileSelectMessages(
    timeoutMillis: Long = -1,
    filterContext: Boolean = true,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, Boolean>.() -> Unit
) = whileSelectMessagesImpl(timeoutMillis, filterContext, priority, selectBuilder)

/**
 * [selectMessages] 的 [Unit] 返回值捷径 (由于 Kotlin 无法推断 [Unit] 类型)
 */
@MiraiExperimentalAPI
@JvmName("selectMessages1")
suspend inline fun <reified T : MessageEvent> T.selectMessagesUnit(
    timeoutMillis: Long = -1,
    filterContext: Boolean = true,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    crossinline selectBuilder: @MessageDsl MessageSelectBuilderUnit<T, Unit>.() -> Unit
) = selectMessagesImpl(timeoutMillis, true, filterContext, priority, selectBuilder)


/**
 * 挂起当前协程, 等待任意一个事件监听器触发后返回其返回值.
 *
 * 创建的所有事件监听器都会判断发送人信息 ([isContextIdenticalWith]), 监听之后的所有消息.
 *
 * [selectBuilder] DSL 类似于 [CoroutineScope.subscribeMessages] 的 DSL, 屏蔽了一些 `reply` DSL 以确保类型安全
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
// @BuilderInference // https://youtrack.jetbrains.com/issue/KT-37716
suspend inline fun <reified T : MessageEvent, R> T.selectMessages(
    timeoutMillis: Long = -1,
    filterContext: Boolean = true,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    // @BuilderInference
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, R>.() -> Unit
): R =
    selectMessagesImpl(timeoutMillis,
        false,
        filterContext,
        priority) { selectBuilder.invoke(this as MessageSelectBuilder<T, R>) }

/**
 * [selectMessages] 时的 DSL 构建器.
 *
 * 它是特殊化的消息监听 ([CoroutineScope.subscribeMessages]) DSL, 屏蔽了一些 `reply` DSL 以确保作用域安全性
 *
 * @see MessageSelectBuilderUnit 查看上层 API
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
abstract class MessageSelectBuilder<M : MessageEvent, R> @PublishedApi internal constructor(
    ownerMessagePacket: M,
    stub: Any?,
    subscriber: (M.(String) -> Boolean, MessageListener<M, Any?>) -> Unit
) : MessageSelectBuilderUnit<M, R>(ownerMessagePacket, stub, subscriber) {

    // 这些函数无法获取返回值. 必须屏蔽.

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun <N : Any> mapping(
        mapper: M.(String) -> N?,
        onEvent: @MessageDsl suspend M.(N) -> R
    ) = error("prohibited")

    @Deprecated("Use `default` instead", level = DeprecationLevel.HIDDEN)
    override fun always(onEvent: MessageListener<M, Any?>) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.reply(block: suspend () -> Any?): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.reply(message: String): Nothing = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.reply(message: Message): Nothing = error("prohibited")

    @JvmName("reply3")
    @Suppress(
        "INAPPLICABLE_JVM_NAME", "unused", "UNCHECKED_CAST",
        "INVALID_CHARACTERS", "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName"
    )
    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.`->`(message: String): Nothing = error("prohibited")

    @JvmName("reply3")
    @Suppress(
        "INAPPLICABLE_JVM_NAME", "unused", "UNCHECKED_CAST",
        "INVALID_CHARACTERS", "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName"
    )
    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override infix fun MessageSelectionTimeoutChecker.`->`(message: Message): Nothing = error("prohibited")

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
    override fun String.containsReply(replier: suspend M.(String) -> Any?) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun Regex.matchingReply(replier: suspend M.(MatchResult) -> Any?) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun Regex.findingReply(replier: suspend M.(MatchResult) -> Any?) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.endsWithReply(replier: suspend M.(String) -> Any?) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.reply(reply: String) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.reply(reply: Message) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun String.reply(replier: suspend M.(String) -> Any?) = error("prohibited")


    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.reply(toReply: String) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.reply(message: Message) = error("prohibited")

    @JvmName("reply3")
    @Suppress("INAPPLICABLE_JVM_NAME", "INVALID_CHARACTERS", "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName")
    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.`->`(toReply: String) = error("prohibited")

    @JvmName("reply3")
    @Suppress("INAPPLICABLE_JVM_NAME", "INVALID_CHARACTERS", "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName")
    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.`->`(message: Message) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.reply(replier: suspend M.(String) -> Any?) =
        error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.quoteReply(toReply: String) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.quoteReply(toReply: Message) = error("prohibited")

    @Deprecated("Using `reply` DSL in message selection is prohibited", level = DeprecationLevel.HIDDEN)
    override fun ListeningFilter.quoteReply(replier: suspend M.(String) -> Any?) = error("prohibited")
}

/**
 * [selectMessagesUnit] 或 [selectMessages] 时的 DSL 构建器.
 *
 * 它是特殊化的消息监听 ([CoroutineScope.subscribeMessages]) DSL
 *
 * @see MessageSubscribersBuilder 查看上层 API
 */
abstract class MessageSelectBuilderUnit<M : MessageEvent, R> @PublishedApi internal constructor(
    private val ownerMessagePacket: M,
    stub: Any?,
    subscriber: (M.(String) -> Boolean, MessageListener<M, Any?>) -> Unit
) : MessageSubscribersBuilder<M, Unit, R, Any?>(stub, subscriber) {
    /**
     * 当其他条件都不满足时的默认处理.
     */
    @MessageDsl
    abstract fun default(onEvent: MessageListener<M, R>) // 需要后置默认监听器

    @Deprecated("Use `default` instead", level = DeprecationLevel.HIDDEN)
    override fun always(onEvent: MessageListener<M, Any?>) = error("prohibited")

    /**
     * 限制本次 select 的最长等待时间, 当超时后抛出 [TimeoutCancellationException]
     */
    @Suppress("NOTHING_TO_INLINE")
    @MessageDsl
    fun timeoutException(
        timeoutMillis: Long,
        exception: () -> Throwable = { throw MessageSelectionTimeoutException() }
    ) {
        require(timeoutMillis > 0) { "timeoutMillis must be positive" }
        obtainCurrentCoroutineScope().launch {
            delay(timeoutMillis)
            val deferred = obtainCurrentDeferred() ?: return@launch
            if (deferred.isActive && !deferred.isCompleted) {
                deferred.completeExceptionally(exception())
            }
        }
    }

    /**
     * 限制本次 select 的最长等待时间, 当超时后执行 [block] 以完成 select
     */
    @MessageDsl
    fun timeout(timeoutMillis: Long, block: suspend () -> R) {
        require(timeoutMillis > 0) { "timeoutMillis must be positive" }
        obtainCurrentCoroutineScope().launch {
            delay(timeoutMillis)
            val deferred = obtainCurrentDeferred() ?: return@launch
            if (deferred.isActive && !deferred.isCompleted) {
                deferred.complete(block())
            }
        }
    }


    /**
     * 返回一个限制本次 select 的最长等待时间的 [Deferred]
     *
     * @see invoke
     * @see reply
     */
    @MessageDsl
    fun timeout(timeoutMillis: Long): MessageSelectionTimeoutChecker {
        require(timeoutMillis > 0) { "timeoutMillis must be positive" }
        return MessageSelectionTimeoutChecker(timeoutMillis)
    }

    /**
     * 返回一个限制本次 select 的最长等待时间的 [Deferred]
     *
     * @see Deferred<Unit>.invoke
     */
    @Suppress("unused")
    fun MessageSelectionTimeoutChecker.invoke(block: suspend () -> R) {
        return timeout(this.timeoutMillis, block)
    }

    /**
     * 在超时后回复原消息
     *
     * 当 [block] 返回值为 [Unit] 时不回复, 为 [Message] 时回复 [Message], 其他将 [toString] 后回复为 [PlainText]
     *
     * @see timeout
     * @see quoteReply
     */
    @Suppress("unused", "UNCHECKED_CAST")
    open infix fun MessageSelectionTimeoutChecker.reply(block: suspend () -> Any?) {
        return timeout(this.timeoutMillis) {
            executeAndReply(block)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    open infix fun MessageSelectionTimeoutChecker.reply(message: Message) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.reply(message)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    open infix fun MessageSelectionTimeoutChecker.reply(message: String) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.reply(message)
            Unit as R
        }
    }

    @JvmName("reply3")
    @Suppress(
        "INAPPLICABLE_JVM_NAME", "unused", "UNCHECKED_CAST",
        "INVALID_CHARACTERS", "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName"
    )
    open infix fun MessageSelectionTimeoutChecker.`->`(message: Message) {
        return this.reply(message)
    }

    @JvmName("reply3")
    @Suppress(
        "INAPPLICABLE_JVM_NAME", "unused", "UNCHECKED_CAST",
        "INVALID_CHARACTERS", "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName"
    )
    open infix fun MessageSelectionTimeoutChecker.`->`(message: String) {
        return this.reply(message)
    }

    /**
     * 在超时后引用回复原消息
     *
     * 当 [block] 返回值为 [Unit] 时不回复, 为 [Message] 时回复 [Message], 其他将 [toString] 后回复为 [PlainText]
     *
     * @see timeout
     * @see reply
     */
    @Suppress("unused", "UNCHECKED_CAST")
    open infix fun MessageSelectionTimeoutChecker.quoteReply(block: suspend () -> Any?) {
        return timeout(this.timeoutMillis) {
            executeAndQuoteReply(block)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    open infix fun MessageSelectionTimeoutChecker.quoteReply(message: Message) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.quoteReply(message)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    open infix fun MessageSelectionTimeoutChecker.quoteReply(message: String) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.quoteReply(message)
            Unit as R
        }
    }

    /**
     * 当其他条件都不满足时回复原消息.
     *
     * 当 [block] 返回值为 [Unit] 时不回复, 为 [Message] 时回复 [Message], 其他将 [toString] 后回复为 [PlainText]
     */
    @MessageDsl
    fun defaultReply(block: suspend () -> Any?): Unit = subscriber({ true }, {
        this@MessageSelectBuilderUnit.executeAndReply(block)
    })


    /**
     * 当其他条件都不满足时引用回复原消息.
     *
     * 当 [block] 返回值为 [Unit] 时不回复, 为 [Message] 时回复 [Message], 其他将 [toString] 后回复为 [PlainText]
     */
    @MessageDsl
    fun defaultQuoteReply(block: suspend () -> Any?): Unit = subscriber({ true }, {
        this@MessageSelectBuilderUnit.executeAndQuoteReply(block)
    })

    private suspend inline fun executeAndReply(noinline block: suspend () -> Any?) {
        when (val result = block()) {
            Unit -> {

            }
            is Message -> ownerMessagePacket.reply(result)
            else -> ownerMessagePacket.reply(result.toString())
        }
    }

    private suspend inline fun executeAndQuoteReply(noinline block: suspend () -> Any?) {
        when (val result = block()) {
            Unit -> {

            }
            is Message -> ownerMessagePacket.quoteReply(result)
            else -> ownerMessagePacket.quoteReply(result.toString())
        }
    }

    protected abstract fun obtainCurrentCoroutineScope(): CoroutineScope
    protected abstract fun obtainCurrentDeferred(): CompletableDeferred<R>?
}

@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class MessageSelectionTimeoutChecker internal constructor(val timeoutMillis: Long)

class MessageSelectionTimeoutException : RuntimeException()


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
internal val SELECT_MESSAGE_STUB = Any()

@PublishedApi
internal val ExceptionHandlerIgnoringCancellationException = CoroutineExceptionHandler { _, throwable ->
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
    priority: Listener.EventPriority,
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
    @Suppress("DuplicatedCode") val subscribeAlways = subscribeAlways<T>(
        concurrency = Listener.ConcurrencyKind.LOCKED,
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
    priority: Listener.EventPriority,
    crossinline selectBuilder: @MessageDsl MessageSelectBuilder<T, Boolean>.() -> Unit
) = withSilentTimeoutOrCoroutineScope(timeoutMillis) {
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
    val subscribeAlways = subscribeAlways<T>(
        concurrency = Listener.ConcurrencyKind.LOCKED,
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
