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
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.recall
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

@Suppress("unused", "NOTHING_TO_INLINE")
class GroupMessage(
    val senderName: String,
    /**
     * 发送方权限.
     */
    val permission: MemberPermission,
    sender: Member,
    override val message: MessageChain
) : MessagePacket<Member, Group>(), Event {
    override val sender: Member by sender.unsafeWeakRef()
    val group: Group get() = sender.group
    override val bot: Bot get() = sender.bot

    override val subject: Group get() = group

    inline fun Long.member(): Member = group[this]

    @MiraiExperimentalAPI
    suspend inline fun MessageChain.recall() = bot.recall(this)

    suspend inline fun MessageSource.recall() = bot.recall(this)
    inline fun MessageSource.recallIn(delay: Long): Job = bot.recallIn(this, delay)
    @MiraiExperimentalAPI
    inline fun MessageChain.recallIn(delay: Long): Job = bot.recallIn(this, delay)

    override fun toString(): String =
        "GroupMessage(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"
}