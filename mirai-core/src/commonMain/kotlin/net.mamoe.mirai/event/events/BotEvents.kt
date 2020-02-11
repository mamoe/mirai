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


// region Bot 状态

/**
 * [Bot] 登录完成, 好友列表, 群组列表初始化完成
 */
data class BotOnlineEvent(override val bot: Bot) : BotActiveEvent

/**
 * [Bot] 离线.
 */
sealed class BotOfflineEvent : BotActiveEvent {

    /**
     * 主动离线
     */
    data class Active(override val bot: Bot, val cause: Throwable?) : BotOfflineEvent()

    /**
     * 被挤下线
     */
    data class Force(override val bot: Bot) : BotOfflineEvent()
}

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
 * 群设置改变. 此事件广播前修改就已经完成.
 */
interface GroupSettingChangeEvent<T> : GroupEvent, BotPassiveEvent {
    val origin: T
    val new: T
}

/**
 * 群名改变. 此事件广播前修改就已经完成.
 */
sealed class GroupNameChangeEvent(
) : GroupSettingChangeEvent<String>, BotPassiveEvent {

    /**
     * 由管理员操作
     */
    data class ByOperator(
        override val origin: String,
        override val new: String,
        val operator: Member
    ) : GroupNameChangeEvent() {
        override val group: Group
            get() = operator.group
    }

    /**
     * 由机器人操作
     */
    data class ByBot(
        override val origin: String,
        override val new: String,
        override val group: Group
    ) : GroupNameChangeEvent()
}

/**
 * 群 "全员禁言" 功能状态改变. 此事件广播前修改就已经完成.
 */
sealed class GroupMuteAllEvent : GroupSettingChangeEvent<Boolean>, BotPassiveEvent {
    /**
     * 由管理员操作
     */
    data class ByOperator(
        override val origin: Boolean,
        override val new: Boolean,
        val operator: Member
    ) : GroupMuteAllEvent() {
        override val group: Group
            get() = operator.group
    }

    /**
     * 由机器人操作
     */
    data class ByBot(
        override val origin: Boolean,
        override val new: Boolean,
        override val group: Group
    ) : GroupMuteAllEvent()
}

/**
 * 群 "坦白说" 功能状态改变. 此事件广播前修改就已经完成.
 */
sealed class GroupConfessTalkEvent : GroupSettingChangeEvent<Boolean>, BotPassiveEvent {

    /**
     * 由管理员操作
     */
    data class ByOperator(
        override val origin: Boolean,
        override val new: Boolean,
        val operator: Member
    ) : GroupConfessTalkEvent() {
        override val group: Group
            get() = operator.group
    }

    /**
     * 由机器人操作
     */
    data class ByBot(
        override val origin: Boolean,
        override val new: Boolean,
        override val group: Group
    ) : GroupConfessTalkEvent()
}

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
sealed class MemberLeftEvent : GroupMemberEvent {
    /**
     * 成员被踢出群. 成员不可能是机器人自己.
     */
    sealed class Kick : MemberLeftEvent() {
        /**
         * 被管理员踢出
         */
        data class ByOperator(
            override val member: Member,
            val operator: Member
        ) : Kick(), BotPassiveEvent

        /**
         * 被机器人踢出
         */
        data class ByBot(
            override val member: Member
        ) : Kick(), BotActiveEvent
    }

    /**
     * 成员主动离开
     */
    data class Quit(override val member: Member) : MemberLeftEvent()
}

// endregion

// region 名片和头衔

/**
 * 群名片改动. 此事件广播前修改就已经完成.
 */
sealed class MemberCardChangeEvent : GroupMemberEvent {
    /**
     * 修改前
     */
    abstract val origin: String

    /**
     * 修改后
     */
    abstract val new: String

    abstract override val member: Member

    /**
     * 由管理员修改
     */
    data class ByOperator(
        override val origin: String,
        override val new: String,
        override val member: Member,
        val operator: Member
    ) : MemberCardChangeEvent(), BotPassiveEvent

    /**
     * 由 [Bot] 修改. 由 [Member.nameCard]
     */
    data class ByBot(
        override val origin: String,
        override val new: String,
        override val member: Member
    ) : MemberCardChangeEvent(), BotActiveEvent

    /**
     * 该成员自己修改
     */
    data class BySelf(
        override val origin: String,
        override val new: String,
        override val member: Member
    ) : MemberCardChangeEvent(), BotPassiveEvent
}

/**
 * 群头衔改动. 一定为群主操作
 */
data class MemberSpecialTitleChangeEvent(
    /**
     * 修改前
     */
    val origin: String,

    /**
     * 修改后
     */
    val new: String,

    override val member: Member
) : GroupMemberEvent

// endregion


// region 成员权限

/**
 * 成员权限改变的事件. 成员不可能是机器人自己.
 */
data class MemberPermissionChangeEvent(
    override val member: Member,
    val origin: MemberPermission,
    val new: MemberPermission
) : GroupMemberEvent, BotPassiveEvent

// endregion


// region 禁言

/**
 * 群成员被禁言事件. 操作人和被禁言的成员都不可能是机器人本人
 */
sealed class MemberMuteEvent : GroupMemberEvent {
    abstract override val member: Member

    abstract val durationSeconds: Int

    /**
     * 管理员禁言成员
     */
    data class ByOperator(
        override val member: Member,
        override val durationSeconds: Int,
        val operator: Member
    ) : MemberMuteEvent(), BotPassiveEvent

    /**
     * 机器人禁言成员. 通过 [Member.mute] 触发
     */
    data class ByBot(
        override val member: Member,
        override var durationSeconds: Int
    ) : MemberMuteEvent(), BotActiveEvent
}

/**
 * 群成员被取消禁言事件. 操作人和被禁言的成员都不可能是机器人本人
 */
sealed class MemberUnmuteEvent : GroupMemberEvent, BotPassiveEvent {
    abstract override val member: Member

    /**
     * 管理员禁言成员
     */
    data class ByOperator(
        override val member: Member,
        val operator: Member
    ) : MemberUnmuteEvent(), BotPassiveEvent

    /**
     * 机器人禁言成员. 通过 [Member.unmute] 触发
     */
    data class ByBot(
        override val member: Member
    ) : MemberUnmuteEvent(), BotActiveEvent
}

// endregion

// endregion

// endregion