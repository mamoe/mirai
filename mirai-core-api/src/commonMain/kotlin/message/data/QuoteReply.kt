/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.safeCast


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
 * 客户端在跳转原消息时, 会通过 [MessageSource.ids] 等 metadata
 *
 * @see MessageSource 获取有关消息源的更多信息
 */
@Serializable
public data class QuoteReply(public val source: MessageSource) : Message, MessageMetadata, ConstrainSingle {
    public companion object Key : AbstractMessageKey<QuoteReply>({ it.safeCast() })

    public override val key: MessageKey<QuoteReply> get() = Key

    public override fun toString(): String =
        "[mirai:quote:${source.ids.contentToString()},${source.internalIds.contentToString()}]"

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