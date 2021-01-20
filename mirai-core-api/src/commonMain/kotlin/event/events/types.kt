/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.internal.network.Packet

/**
 * 有关一个 [Bot] 的事件
 */
public interface BotEvent : Event {
    public val bot: Bot
}

/**
 * [Bot] 被动接收的事件. 这些事件可能与机器人有关
 */
public interface BotPassiveEvent : BotEvent

/**
 * 由 [Bot] 主动发起的动作的事件
 */
public interface BotActiveEvent : BotEvent


/**
 * 有关群的事件
 */
public interface GroupEvent : BotEvent {
    public val group: Group
    override val bot: Bot
        get() = group.bot
}


/**
 * 可由 [Member] 或 [Bot] 操作的事件
 * @see isByBot
 * @see operatorOrBot
 */
public interface GroupOperableEvent : GroupEvent {
    /**
     * 操作人, 为 `null` 时为 [Bot] 操作
     */
    public val operator: Member?
}

/**
 * 是否由 [Bot] 操作
 */
@get:JvmSynthetic
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@kotlin.internal.HidesMembers
public inline val GroupOperableEvent.isByBot: Boolean
    get() = operator == null

/**
 * 当操作人为 [Member] 时获取这个 [Member],
 * 当操作人为 [Bot] 时获取 [Group.botAsMember]
 */
@get:JvmSynthetic
public inline val GroupOperableEvent.operatorOrBot: Member
    get() = this.operator ?: this.group.botAsMember

/**
 * 有关 [User] 的事件
 */
public interface UserEvent : BotEvent {
    public val user: User
}

/**
 * 有关好友的事件
 */
public interface FriendEvent : BotEvent, UserEvent {
    public val friend: Friend
    override val bot: Bot get() = friend.bot
    override val user: Friend get() = friend
}

/**
 * 有关陌生人的事件
 */
public interface StrangerEvent : BotEvent, UserEvent {
    public val stranger: Stranger
    override val bot: Bot get() = stranger.bot
    override val user: Stranger get() = stranger
}

/**
 * 有关群成员的事件
 */
public interface GroupMemberEvent : GroupEvent, UserEvent {
    public val member: Member
    override val group: Group get() = member.group
    override val user: Member get() = member
}

public interface OtherClientEvent : BotEvent, Packet {
    public val client: OtherClient
    override val bot: Bot get() = client.bot
}
