/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.jvm.JvmName

@Suppress("unused", "NOTHING_TO_INLINE")
class GroupMessage(
    bot: Bot,
    group: Group,
    val senderName: String,
    /**
     * 发送方权限.
     */
    val permission: MemberPermission,
    sender: Member,
    override val message: MessageChain
) : MessagePacket<Member, Group>(bot), Event {
    val group: Group by group.unsafeWeakRef()
    override val sender: Member by sender.unsafeWeakRef()

    override val subject: Group get() = group

    inline fun Long.member(): Member = group[this]


    /**
     * 给这个消息事件的主体发送引用回复消息
     * 对于好友消息事件, 这个方法将会给好友 ([subject]) 发送消息
     * 对于群消息事件, 这个方法将会给群 ([subject]) 发送消息
     */
    suspend inline fun quoteReply(message: MessageChain): MessageReceipt<Group> = reply(this.message.quote() + message)

    suspend inline fun quoteReply(message: Message): MessageReceipt<Group> = reply(this.message.quote() + message)
    suspend inline fun quoteReply(plain: String): MessageReceipt<Group> = reply(this.message.quote() + plain)


    @JvmName("reply2")
    suspend inline fun String.quoteReply(): MessageReceipt<Group> = quoteReply(this)

    @JvmName("reply2")
    suspend inline fun Message.quoteReply(): MessageReceipt<Group> = quoteReply(this)

    @JvmName("reply2")
    suspend inline fun MessageChain.quoteReply(): MessageReceipt<Group> = quoteReply(this)

    suspend inline fun MessageChain.recall() = group.recall(this)
    suspend inline fun MessageSource.recall() = group.recall(this)
    inline fun MessageSource.recallIn(delay: Long): Job = group.recallIn(this, delay)
    inline fun MessageChain.recallIn(delay: Long): Job = group.recallIn(this, delay)

    override fun toString(): String =
        "GroupMessage(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"
}