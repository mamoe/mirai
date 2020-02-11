/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.Event

/**
 * 有关一个 [Bot] 的事件
 */
interface BotEvent : Event {
    val bot: Bot
}

/**
 * [Bot] 登录完成, 好友列表, 群组列表初始化完成
 */
data class BotLoginSucceedEvent(override val bot: Bot) : BotEvent

/**
 * [Bot] 离线.
 */
data class BotOfflineEvent(override val bot: Bot) : BotEvent

/**
 * 被挤下线
 */
data class ForceOfflineEvent(
    override val bot: Bot,
    val title: String,
    val tips: String
) : BotEvent, Packet

/**
 * 有关群的事件
 */
interface GroupEvent : BotEvent {
    val group: Group
    override val bot: Bot
        get() = group.bot
}

data class AddGroupEvent(override val group: Group) : BotEvent, GroupEvent

data class RemoveGroupEvent(override val group: Group) : BotEvent, GroupEvent

data class BotGroupPermissionChangeEvent(
    override val group: Group,
    val origin: MemberPermission,
    val new: MemberPermission
) : BotEvent, GroupEvent


interface GroupSettingChangeEvent<T> : GroupEvent {
    val operator: Member
    val origin: T
    val new: T

    override val group: Group
        get() = operator.group
}

data class GroupNameChangeEvent(
    override val operator: Member,
    override val origin: String,
    override val new: String
) : BotEvent, GroupSettingChangeEvent<String>

/**
 * 群 "全员禁言" 功能开启
 */
data class GroupMuteAllEvent(
    override val operator: Member,
    override val origin: Boolean,
    override val new: Boolean
) : BotEvent, GroupSettingChangeEvent<Boolean>

data class GroupConfessTalkEvent(
    override val operator: Member,
    override val origin: Boolean,
    override val new: Boolean
) : BotEvent, GroupSettingChangeEvent<Boolean>


/**
 * 有关群成员的事件
 */
interface GroupMemberEvent : GroupEvent {
    val member: Member
    override val group: Group
        get() = member.group
}

/**
 * 成员加入群的事件
 */
data class MemberJoinEvent(override val member: Member) : BotEvent, GroupMemberEvent

/**
 * 成员离开群的事件
 */
sealed class MemberLeftEvent : BotEvent, GroupMemberEvent {
    /**
     * 成员被踢出群
     */
    data class Kick(override val member: Member, val operator: Member) : MemberLeftEvent()

    /**
     * 成员主动离开
     */
    data class Quit(override val member: Member) : MemberLeftEvent()
}

data class MemberPermissionChangeEvent(
    override val bot: Bot,
    override val member: Member,
    val origin: MemberPermission,
    val new: MemberPermission
) : BotEvent, GroupMemberEvent


