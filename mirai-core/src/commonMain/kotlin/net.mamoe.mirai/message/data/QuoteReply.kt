/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.message.data

import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic


/**
 * 引用回复.
 *
 * 支持引用任何一条消息发送给任何人.
 *
 * #### 元数据
 * [QuoteReply] 被作为 [MessageMetadata], 因为它不包含实际的消息内容, 且只能在消息中单独存在.
 *
 * #### [source] 的类型:
 * - 在发送引用回复时, [source] 类型为 [OnlineMessageSource] 或 [OfflineMessageSource]
 * - 在接收引用回复时, [source] 类型一定为 [OfflineMessageSource]
 *
 * #### 原消息内容
 * 引用回复的原消息内容完全由 [source] 中 [MessageSource.originalMessage] 控制, 客户端不会自行寻找原消息.
 *
 * #### 客户端内跳转
 * 客户端在跳转原消息时, 会通过 [MessageSource.id] 等 metadata
 *
 * @see MessageSource 获取有关消息源的更多信息
 */
public class QuoteReply(public val source: MessageSource) : Message, MessageMetadata, ConstrainSingle<QuoteReply> {
    public companion object Key : Message.Key<QuoteReply> {
        public override val typeName: String
            get() = "QuoteReply"
    }

    public override val key: Message.Key<QuoteReply> get() = Key

    public override fun toString(): String = "[mirai:quote:${source.id},${source.internalId}]"
    public override fun equals(other: Any?): Boolean = other is QuoteReply && other.source == this.source
    public override fun hashCode(): Int = source.hashCode()
}

/**
 * @see MessageSource.bot
 */
@get:JvmSynthetic
public inline val QuoteReply.bot: Bot
    get() = source.bot

/**
 * 撤回引用的源消息
 */
@JvmSynthetic
public suspend inline fun QuoteReply.recallSource(): Unit = this.source.recall()

/**
 * 在一段时间后撤回引用的源消息
 */
@JvmOverloads
public inline fun QuoteReply.recallSourceIn(
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.source.recallIn(millis, coroutineContext)


//// 因语义不明而弃用的 API, 兼容到 1.3.0

@PlannedRemoval("1.3.0")
@get:JvmSynthetic
@Deprecated("use source.id for clearer semantics", ReplaceWith("source.id"))
public inline val QuoteReply.id: Int
    get() = source.id

@PlannedRemoval("1.3.0")
@get:JvmSynthetic
@Deprecated("use source.internalId for clearer semantics", ReplaceWith("source.internalId"))
public inline val QuoteReply.internalId: Int
    get() = source.internalId

@PlannedRemoval("1.3.0")
@get:JvmSynthetic
@Deprecated("use source.fromId for clearer semantics", ReplaceWith("source.fromId"))
public inline val QuoteReply.fromId: Long
    get() = source.fromId

@PlannedRemoval("1.3.0")
@get:JvmSynthetic
@Deprecated("use source.targetId for clearer semantics", ReplaceWith("source.targetId"))
public inline val QuoteReply.targetId: Long
    get() = source.targetId

@PlannedRemoval("1.3.0")
@get:JvmSynthetic
@Deprecated("use source.originalMessage for clearer semantics", ReplaceWith("source.originalMessage"))
public inline val QuoteReply.originalMessage: MessageChain
    get() = source.originalMessage

@PlannedRemoval("1.3.0")
@get:JvmSynthetic
@Deprecated("use source.time for clearer semantics", ReplaceWith("source.time"))
public inline val QuoteReply.time: Int
    get() = source.time

@PlannedRemoval("1.3.0")
@Deprecated("use recallSourceIn for clearer semantics", ReplaceWith("recallSourceIn(millis, coroutineContext)"))
@JvmOverloads
public inline fun QuoteReply.recallIn(
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = recallSourceIn(millis, coroutineContext)

@PlannedRemoval("1.3.0")
@Deprecated("use recallSource for clearer semantics", ReplaceWith("this.recallSource()"))
@JvmSynthetic
public suspend inline fun QuoteReply.recall(): Unit = recallSource()
