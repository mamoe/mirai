/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * [selectMessagesUnit] 或 [selectMessages] 时的 DSL 构建器.
 *
 * 它是特殊化的消息监听 ([EventChannel.subscribeMessages]) DSL
 *
 * @see MessageSubscribersBuilder 查看上层 API
 */
@OptIn(MiraiInternalApi::class)
public abstract class MessageSelectBuilderUnit<M : MessageEvent, R> @PublishedApi internal constructor(
    ownerMessagePacket: M,
    stub: Any?,
    subscriber: (M.(String) -> Boolean, MessageListener<M, Any?>) -> Unit
) : CommonMessageSelectBuilderUnit<M, R>(ownerMessagePacket, stub, subscriber) {
    @JvmName("timeout-ncvN2qU")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public fun timeout00(timeoutMillis: Long): MessageSelectionTimeoutChecker {
        return timeout(timeoutMillis)
    }

    @Suppress("unused")
    @JvmName("invoke-RNyhSv4")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public fun MessageSelectionTimeoutChecker.invoke00(block: suspend () -> R) {
        return invoke(block)
    }

    @Suppress("unused")
    @JvmName("invoke-RNyhSv4")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public fun MessageSelectionTimeoutChecker.invoke000(block: suspend () -> R): Nothing? {
        invoke(block)
        return null
    }

    @JvmName("reply-RNyhSv4")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.reply00(block: suspend () -> Any?) {
        return reply(block)
    }

    @JvmName("reply-RNyhSv4")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.reply000(block: suspend () -> Any?): Nothing? {
        reply(block)
        return null
    }

    @JvmName("reply-sCZ5gAI")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.reply00(message: String) {
        return reply(message)
    }

    @JvmName("reply-sCZ5gAI")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.reply000(message: String): Nothing? {
        reply(message)
        return null
    }

    @JvmName("reply-AVDwu3U")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.reply00(message: Message) {
        return reply(message)
    }

    @JvmName("reply-AVDwu3U")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.reply000(message: Message): Nothing? {
        reply(message)
        return null
    }


    @JvmName("quoteReply-RNyhSv4")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.quoteReply00(block: suspend () -> Any?) {
        return reply(block)
    }

    @JvmName("quoteReply-RNyhSv4")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.quoteReply000(block: suspend () -> Any?): Nothing? {
        reply(block)
        return null
    }

    @JvmName("quoteReply-sCZ5gAI")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.quoteReply00(message: String) {
        return reply(message)
    }

    @JvmName("quoteReply-sCZ5gAI")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.quoteReply000(message: String): Nothing? {
        reply(message)
        return null
    }

    @JvmName("quoteReply-AVDwu3U")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.quoteReply00(message: Message) {
        return reply(message)
    }

    @JvmName("quoteReply-AVDwu3U")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public infix fun MessageSelectionTimeoutChecker.quoteReply000(message: Message): Nothing? {
        reply(message)
        return null
    }
}

/**
 * [MessageSelectBuilderUnit] 的跨平台实现
 */
@MiraiInternalApi
public abstract class CommonMessageSelectBuilderUnit<M : MessageEvent, R> protected constructor(
    private val ownerMessagePacket: M,
    stub: Any?,
    subscriber: (M.(String) -> Boolean, MessageListener<M, Any?>) -> Unit
) : MessageSubscribersBuilder<M, Unit, R, Any?>(stub, subscriber) {
    /**
     * 当其他条件都不满足时的默认处理.
     */
    @MessageDsl
    public abstract fun default(onEvent: MessageListener<M, R>) // 需要后置默认监听器

    @Deprecated("Use `default` instead", level = DeprecationLevel.HIDDEN)
    override fun always(onEvent: MessageListener<M, Any?>): Nothing = error("prohibited")

    /**
     * 限制本次 select 的最长等待时间, 当超时后抛出 [TimeoutCancellationException]
     */
    @MessageDsl
    public fun timeoutException(
        timeoutMillis: Long,
        exception: () -> Throwable
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
    public fun timeout(timeoutMillis: Long, block: suspend () -> R) {
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
    public fun timeout(timeoutMillis: Long): MessageSelectionTimeoutChecker {
        require(timeoutMillis > 0) { "timeoutMillis must be positive" }
        return MessageSelectionTimeoutChecker(timeoutMillis)
    }

    /**
     * 返回一个限制本次 select 的最长等待时间的 [Deferred]
     *
     * @see Deferred<Unit>.invoke
     */
    @Suppress("unused")
    public fun MessageSelectionTimeoutChecker.invoke(block: suspend () -> R) {
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
    public open infix fun MessageSelectionTimeoutChecker.reply(block: suspend () -> Any?) {
        return timeout(this.timeoutMillis) {
            executeAndReply(block)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    public open infix fun MessageSelectionTimeoutChecker.reply(message: Message) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.subject.sendMessage(message)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    public open infix fun MessageSelectionTimeoutChecker.reply(message: String) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.subject.sendMessage(message)
            Unit as R
        }
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
    public open infix fun MessageSelectionTimeoutChecker.quoteReply(block: suspend () -> Any?) {
        return timeout(this.timeoutMillis) {
            executeAndQuoteReply(block)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    public open infix fun MessageSelectionTimeoutChecker.quoteReply(message: Message) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.subject.sendMessage(ownerMessagePacket.message.quote() + message)
            Unit as R
        }
    }

    @Suppress("unused", "UNCHECKED_CAST")
    public open infix fun MessageSelectionTimeoutChecker.quoteReply(message: String) {
        return timeout(this.timeoutMillis) {
            ownerMessagePacket.subject.sendMessage(ownerMessagePacket.message.quote() + message)
            Unit as R
        }
    }

    /**
     * 当其他条件都不满足时回复原消息.
     *
     * 当 [block] 返回值为 [Unit] 时不回复, 为 [Message] 时回复 [Message], 其他将 [toString] 后回复为 [PlainText]
     */
    @MessageDsl
    public fun defaultReply(block: suspend () -> Any?): Unit = subscriber({ true }, {
        this@CommonMessageSelectBuilderUnit.executeAndReply(block)
    })


    /**
     * 当其他条件都不满足时引用回复原消息.
     *
     * 当 [block] 返回值为 [Unit] 时不回复, 为 [Message] 时回复 [Message], 其他将 [toString] 后回复为 [PlainText]
     */
    @MessageDsl
    public fun defaultQuoteReply(block: suspend () -> Any?): Unit = subscriber({ true }, {
        this@CommonMessageSelectBuilderUnit.executeAndQuoteReply(block)
    })

    private suspend inline fun executeAndReply(noinline block: suspend () -> Any?) {
        when (val result = block()) {
            Unit -> {

            }

            is Message -> ownerMessagePacket.subject.sendMessage(result)
            else -> ownerMessagePacket.subject.sendMessage(result.toString())
        }
    }

    private suspend inline fun executeAndQuoteReply(noinline block: suspend () -> Any?) {
        when (val result = block()) {
            Unit -> {

            }

            is Message -> ownerMessagePacket.subject.sendMessage(ownerMessagePacket.message.quote() + result)
            else -> ownerMessagePacket.subject.sendMessage(ownerMessagePacket.message.quote() + result.toString())
        }
    }

    protected abstract fun obtainCurrentCoroutineScope(): CoroutineScope
    protected abstract fun obtainCurrentDeferred(): CompletableDeferred<R>?
}