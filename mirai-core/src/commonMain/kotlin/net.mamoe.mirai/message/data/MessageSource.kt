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
interface MessageSource : Message {
    companion object Key : Message.Key<MessageSource>

    /**
     * 实际上是个随机数, 但服务器确实是用它当做 uid
     */
    val messageUid: Long

    /**
     * 发送人号码
     */
    val senderId: Long

    /**
     * 群号码
     */
    val groupId: Long

    /**
     * 原消息内容
     */
    val sourceMessage: MessageChain

    /**
     * 固定返回空字符串 ("")
     */
    override fun toString(): String
}