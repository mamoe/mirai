/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.unsafeWeakRef

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
) : MessagePacket<Member, Group>(bot) {
    val group: Group by group.unsafeWeakRef()
    override val sender: Member by sender.unsafeWeakRef()

    override val subject: Group get() = group

    inline fun At.member(): Member = group[this.target]
    inline fun Long.member(): Member = group[this]
    override fun toString(): String =
        "GroupMessage(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"
}