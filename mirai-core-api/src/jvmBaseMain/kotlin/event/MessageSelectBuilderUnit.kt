/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.event

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * [selectMessagesUnit] 或 [selectMessages] 时的 DSL 构建器.
 *
 * 它是特殊化的消息监听 ([EventChannel.subscribeMessages]) DSL
 *
 * @see MessageSubscribersBuilder 查看上层 API
 */
@OptIn(MiraiInternalApi::class)
public actual abstract class MessageSelectBuilderUnit<M : MessageEvent, R> @PublishedApi internal actual constructor(
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