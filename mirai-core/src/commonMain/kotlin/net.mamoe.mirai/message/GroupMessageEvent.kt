/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("DEPRECATION_ERROR", "unused", "NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.PlannedRemoval

/**
 * 机器人收到的群消息的事件
 *
 * @see MessageEvent
 */
public class GroupMessageEvent(
    public override val senderName: String,
    /**
     * 发送方权限.
     */
    public val permission: MemberPermission,
    public override val sender: Member,
    public override val message: MessageChain,
    public override val time: Int
) : @PlannedRemoval("1.2.0") GroupMessage(), Event, GroupEvent {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromGroup) { "source provided to a GroupMessage must be an instance of OnlineMessageSource.Incoming.FromGroup" }
    }

    public override val group: Group get() = sender.group
    public override val bot: Bot get() = sender.bot

    public override val subject: Group get() = group

    public override val source: OnlineMessageSource.Incoming.FromGroup get() = message.source as OnlineMessageSource.Incoming.FromGroup

    public inline fun At.asMember(): Member = group[this.target]

    public override fun toString(): String =
        "GroupMessageEvent(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"
}