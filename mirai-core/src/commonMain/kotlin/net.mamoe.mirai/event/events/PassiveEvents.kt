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

// region Bot 在线状态

/**
 * 被挤下线
 */
data class BotForceOfflineEvent(
    override val bot: Bot,
    val title: String,
    val tips: String
) : BotPassiveEvent, Packet
// endregion

// region 群

/**
 * Bot 在群里的权限被改变. 操作人一定是群主
 */
data class BotGroupPermissionChangeEvent(
    override val group: Group,
    val origin: MemberPermission,
    val new: MemberPermission
) : BotPassiveEvent, GroupEvent

// region 群设置

/**
 * 群设置改变
 */
interface GroupSettingChangeEvent<T> : GroupEvent, BotPassiveEvent {
    val operator: Member
    val origin: T
    val new: T

    override val group: Group
        get() = operator.group
}

/**
 * 群名改变
 */
data class GroupNameChangeEvent(
    override val operator: Member,
    override val origin: String,
    override val new: String
) : GroupSettingChangeEvent<String>, BotPassiveEvent

/**
 * 群 "全员禁言" 功能状态改变
 */
data class GroupMuteAllEvent(
    override val operator: Member,
    override val origin: Boolean,
    override val new: Boolean
) : GroupSettingChangeEvent<Boolean>, BotPassiveEvent

/**
 * 群 "坦白说" 功能状态改变
 */
data class GroupConfessTalkEvent(
    override val operator: Member,
    override val origin: Boolean,
    override val new: Boolean
) : GroupSettingChangeEvent<Boolean>, BotPassiveEvent

// endregion


// region 群成员

// region 成员变更

/**
 * 成员加入群的事件
 */
data class MemberJoinEvent(override val member: Member) : GroupMemberEvent, BotPassiveEvent

/**
 * 成员离开群的事件
 */
sealed class MemberLeftEvent : GroupMemberEvent, BotPassiveEvent {
    /**
     * 成员被踢出群. 成员不可能是机器人自己.
     *
     * @see BotKickMemberEvent 机器人踢出一个人
     */
    data class Kick(override val member: Member, val operator: Member) : MemberLeftEvent()

    /**
     * 成员主动离开
     */
    data class Quit(override val member: Member) : MemberLeftEvent()
}

// endregion

// region

/**
 * 群名片改动
 */
sealed class MemberCardChangeEvent : GroupMemberEvent, BotPassiveEvent {
    /**
     * 群名片
     */
    abstract val card: String

    /**
     * 由管理员修改
     */
    data class ByOperator(
        override val card: String,
        override val member: Member,
        val operator: Member
    ) : MemberCardChangeEvent()

    /**
     * 该成员自己修改
     */
    data class BySelf(
        override val card: String,
        override val member: Member
    ) : MemberCardChangeEvent()
}

// endregion


// region 成员权限

/**
 * 成员权限改变的事件. 成员不可能是机器人自己.
 */
data class MemberPermissionChangeEvent(
    override val bot: Bot,
    override val member: Member,
    val origin: MemberPermission,
    val new: MemberPermission
) : GroupMemberEvent, BotPassiveEvent

// endregion


// region 禁言

/**
 * 群成员被禁言事件. 操作人和被禁言的成员都不可能是机器人本人
 *
 * @see BotMuteMemberEvent 机器人禁言一个人
 */
data class MemberMuteEvent(
    override val member: Member,
    val operator: Member,
    val durationSeconds: Int
) : GroupMemberEvent, BotPassiveEvent {
    override fun toString(): String = "MemberMuteEvent(member=${member.id}, group=${group.id}, operator=${operator.id}, duration=${durationSeconds}s"
}

/**
 * 群成员被取消禁言事件. 操作人和被禁言的成员都不可能是机器人本人
 *
 * @see BotUnmuteMemberEvent 机器人取消禁言某个人
 */
data class MemberUnmuteEvent(override val member: Member, val operator: Member) : GroupMemberEvent, BotPassiveEvent

// endregion

// endregion

// endregion