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
 * 群内的或好友的引用回复.
 *
 * 可以引用一条群消息并发送给一个好友, 或是引用好友消息发送给群.
 * 可以引用自己发出的消息. 详见 [MessageReceipt.quote]
 *
 * 总是使用 [quote] 来构造这个实例.
 */
open class QuoteReply @MiraiInternalAPI constructor(val source: MessageSource) : Message {
    companion object Key : Message.Key<QuoteReply>

    override fun toString(): String = ""
}

/**
 * 群内的引用回复.
 * 总是使用 [quote] 来构造实例.
 */
@UseExperimental(MiraiInternalAPI::class)
class QuoteReplyToSend @MiraiInternalAPI constructor(source: MessageSource, val sender: QQ) : QuoteReply(source) {
    fun createAt(): At = At(sender as Member)
}

/**
 * 引用这条消息.
 * 好友消息: 返回 `[QuoteReply]`
 * 群消息: 返回 `[QuoteReply] + [At] + [PlainText]`(必要的结构)
 */
@UseExperimental(MiraiInternalAPI::class)
fun MessageChain.quote(sender: QQ): QuoteReplyToSend {
    this.firstOrNull<MessageSource>()?.let {
        return it.quote(sender)
    }
    error("cannot find MessageSource")
}

/**
 * 引用这条消息.
 * 好友消息: 返回 `[QuoteReply]`
 * 群消息: 返回 `[QuoteReply] + [At] + [PlainText]`(必要的结构)
 */
@UseExperimental(MiraiInternalAPI::class)
fun MessageSource.quote(sender: QQ): QuoteReplyToSend {
    if (this.groupId != 0L) {
        check(sender is Member) { "sender must be Member to quote a GroupMessage" }
    }
    return QuoteReplyToSend(this, sender)
}