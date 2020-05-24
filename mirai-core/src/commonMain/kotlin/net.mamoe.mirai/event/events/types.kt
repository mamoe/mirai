/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.Event
import kotlin.internal.HidesMembers
import kotlin.jvm.JvmSynthetic

/**
 * 有关一个 [Bot] 的事件
 */
interface BotEvent : Event {
    val bot: Bot
}

/**
 * [Bot] 被动接收的事件. 这些事件可能与机器人有关
 */
interface BotPassiveEvent : BotEvent

/**
 * 由 [Bot] 主动发起的动作的事件
 */
interface BotActiveEvent : BotEvent


/**
 * 有关群的事件
 */
interface GroupEvent : BotEvent {
    val group: Group
    override val bot: Bot
        get() = group.bot
}

/**
 * 有关群成员的事件
 */
interface GroupMemberEvent : GroupEvent {
    val member: Member
    override val group: Group
        get() = member.group
}

/**
 * 可由 [Member] 或 [Bot] 操作的事件
 * @see isByBot
 * @see operatorOrBot
 */
interface GroupOperableEvent : GroupEvent {
    /**
     * 操作人, 为 `null` 时为 [Bot] 操作
     */
    val operator: Member?
}

/**
 * 是否由 [Bot] 操作
 */
@HidesMembers
@get:JvmSynthetic // inline: planning to change to another file (1.2.0)
inline val GroupOperableEvent.isByBot: Boolean
    get() = operator == null

/**
 * 当操作人为 [Member] 时获取这个 [Member],
 * 当操作人为 [Bot] 时获取 [Group.botAsMember]
 */
@get:JvmSynthetic // inline: planning to change to another file (1.2.0)
inline val GroupOperableEvent.operatorOrBot: Member
    get() = this.operator ?: this.group.botAsMember


/**
 * 有关好友的事件
 */
interface FriendEvent : BotEvent {
    val friend: Friend
    final override val bot: Bot get() = friend.bot
}