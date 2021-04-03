/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.safeCast


/**
 * 引用回复. [QuoteReply] 被作为 [MessageMetadata], 因为它不包含实际的消息内容.
 *
 * 支持引用任何一条消息发送给任何人.
 *
 * 引用回复的原消息内容完全由 [source] 中 [MessageSource.originalMessage] 控制, 客户端不会自行寻找原消息.
 * 可通过 [MessageSource.copyAmend] 修改引用的消息内容.
 *
 * 客户端通过 [MessageSource.ids] 等数据定位源消息, 在修改时使用 [MessageSourceBuilder.metadata] 可以修改定位结果.
 *
 * ## 创建引用回复
 * - 直接构造 [QuoteReply]: `new QuoteReply(source)`
 * - 在 Kotlin 使用扩展 [MessageSource.quote]
 *
 * @see MessageSource 获取有关消息源的更多信息
 */
@Serializable
@SerialName(QuoteReply.SERIAL_NAME)
public data class QuoteReply(
    /**
     * 指代被引用的消息. 其中 [MessageSource.originalMessage] 可以控制客户端显示的消息内容.
     */
    public val source: MessageSource
) : Message, MessageMetadata, ConstrainSingle {
    /**
     * 从消息链中获取 [MessageSource] 并构造.
     */
    public constructor(sourceMessage: MessageChain) : this(sourceMessage.getOrFail(MessageSource))

    public override val key: MessageKey<QuoteReply> get() = Key

    public override fun toString(): String =
        "[mirai:quote:${source.ids.contentToString()},${source.internalIds.contentToString()}]"

    public override fun equals(other: Any?): Boolean = other is QuoteReply && other.source == this.source
    public override fun hashCode(): Int = source.hashCode()

    public companion object Key : AbstractMessageKey<QuoteReply>({ it.safeCast() }) {
        public const val SERIAL_NAME: String = "QuoteReply"
    }
}

/**
 * 撤回引用的源消息
 */
@JvmSynthetic
public suspend inline fun QuoteReply.recallSource(): Unit = this.source.recall()