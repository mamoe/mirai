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

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * 从服务器接收的或客户端构造用来发送的群内的或好友的引用回复.
 *
 * 可以引用一条群消息并发送给一个好友, 或是引用好友消息发送给群.
 * 可以引用自己发出的消息. 详见 [MessageReceipt.quote]
 *
 * 总是使用 [quote] 来构造这个实例.
 */
open class QuoteReply
@MiraiInternalAPI constructor(val source: MessageSource) : Message, MessageContent {
    companion object Key : Message.Key<QuoteReply>

    override fun toString(): String = ""
}

/**
 * 用于发送的引用回复.
 * 总是使用 [quote] 来构造实例.
 */
@OptIn(MiraiInternalAPI::class)
sealed class QuoteReplyToSend
@MiraiInternalAPI constructor(source: MessageSource) : QuoteReply(source) {
    class ToGroup(source: MessageSource, val sender: QQ) : QuoteReplyToSend(source) {
        fun createAt(): At = At(sender as Member)
    }

    class ToFriend(source: MessageSource) : QuoteReplyToSend(source)
}

/**
 * 引用这条消息.
 * @see sender 消息发送人.
 */
@OptIn(MiraiInternalAPI::class)
fun MessageChain.quote(sender: QQ?): QuoteReplyToSend {
    this.firstOrNull<MessageSource>()?.let {
        return it.quote(sender)
    }
    error("cannot find MessageSource")
}

/**
 * 引用这条消息.
 * @see from 消息来源. 若是好友发送
 */
@OptIn(MiraiInternalAPI::class)
fun MessageSource.quote(from: QQ?): QuoteReplyToSend {
    return if (this.groupId != 0L) {
        check(from is Member) { "sender must be Member to quote a GroupMessage" }
        QuoteReplyToSend.ToGroup(this, from)
    } else QuoteReplyToSend.ToFriend(this)
}