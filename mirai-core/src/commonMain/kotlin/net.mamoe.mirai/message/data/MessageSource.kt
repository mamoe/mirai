/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.QQ
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * 消息源, 它存在于 [MessageChain] 中, 用于表示这个消息的来源.
 *
 * 消息源只用于 [引用回复][QuoteReply] 或 [撤回][Bot.recall].
 *
 * `mirai-core-qqandroid`: `net.mamoe.mirai.qqandroid.message.MessageSourceFromMsg`
 *
 * @see Bot.recall 撤回一条消息
 * @see MessageSource.quote 引用这条消息, 创建 [MessageChain]
 */
interface MessageSource : Message, MessageMetadata {
    companion object Key : Message.Key<MessageSource>

    /**
     * 在 Mirai 中使用的 id.
     * 高 32 位为 [sequenceId],
     * 低 32 位为 [messageRandom]
     */
    val id: Long

    /**
     * 等待 [sequenceId] 获取, 确保其可用.
     *
     * 这个方法 3 秒超时, 抛出 [IllegalStateException], 则表明原消息发送失败.
     */
    suspend fun ensureSequenceIdAvailable()

    /**
     * 发送时间, 单位为秒
     */
    val time: Long

    /**
     * 与这个消息相关的 [QQ] 的 [QQ.id]
     *
     * 群消息时为发送人的 id. 好友消息时为消息发送目标好友的 id
     */
    val qqId: Long

    @Suppress("unused") // TODO: 2020/2/29 0.25: 删除 `MessageSource.senderId`
    @Deprecated("使用 qqId. 此 API 将在 0.25 删除", level = DeprecationLevel.HIDDEN, replaceWith = ReplaceWith("this.qqId"))
    val senderId: Long

    /**
     * 群号码, 为 0 时则来自好友消息
     */
    val groupId: Long

    /**
     * 固定返回空字符串 ("")
     */
    override fun toString(): String
}

/**
 * 序列号. 若是机器人发出去的消息, 请先 [确保 sequenceId 可用][MessageSource.ensureSequenceIdAvailable]
 * @see MessageSource.id
 */
val MessageSource.sequenceId: Int get() = (this.id shr 32).toInt()

/**
 * 消息随机数. 由服务器或客户端指定后不能更改. 它是消息 id 的一部分.
 * @see MessageSource.id
 */
val MessageSource.messageRandom: Int get() = this.id.toInt()

// For MessageChain

/**
 * 消息 id.
 * @see MessageSource.id
 */
val MessageChain.id: Long get() = this[MessageSource].id

/**
 * 消息序列号, 可能来自服务器也可以发送时赋值, 不唯一.
 * @see MessageSource.id
 */
val MessageChain.sequenceId: Int get() = this[MessageSource].sequenceId

/**
 * 消息随机数. 由服务器或客户端指定后不能更改. 它是消息 id 的一部分.
 * @see MessageSource.id
 */
val MessageChain.messageRandom: Int get() = this[MessageSource].messageRandom
