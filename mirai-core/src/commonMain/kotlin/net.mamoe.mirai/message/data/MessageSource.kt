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

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * 消息源, 用于被引用. 它将由协议模块实现.
 * 消息源只用于 [QuoteReply]
 *
 * `mirai-core-qqandroid`: `net.mamoe.mirai.qqandroid.message.MessageSourceFromMsg`
 *
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
     * 若原消息发送失败, 这个方法会等待最多 3 秒随后抛出 [IllegalStateException]
     */
    suspend fun ensureSequenceIdAvailable()

    /**
     * 发送时间, 单位为秒
     */
    val time: Long

    /**
     * 发送人号码
     */
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
