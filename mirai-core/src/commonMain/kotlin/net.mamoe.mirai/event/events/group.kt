/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:Suppress("unused", "FunctionName")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.event.internal.MiraiAtomicBoolean
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.internal.runBlocking
import kotlin.jvm.*


/**
 * 机器人被踢出群或在其他客户端主动退出一个群. 在事件广播前 [Bot.groups] 就已删除这个群.
 */
sealed class BotLeaveEvent : BotEvent, Packet, AbstractEvent() {
    abstract val group: Group

    /**
     * 机器人主动退出一个群.
     */
    data class Active(override val group: Group) : BotLeaveEvent()

    /**
     * 机器人被管理员或群主踢出群. 暂不支持获取操作人
     */
    data class Kick(override val group: Group) : BotLeaveEvent()

    override val bot: Bot get() = group.bot
}

/**
 * Bot 在群里的权限被改变. 操作人一定是群主
 */
data class BotGroupPermissionChangeEvent(
    override val group: Group,
    val origin: MemberPermission,
    val new: MemberPermission
) : BotPassiveEvent, GroupEvent, Packet, AbstractEvent()

/**
 * Bot 被禁言
 */
data class BotMuteEvent(
    val durationSeconds: Int,
    /**
     * 操作人.
     */
    val operator: Member
) : GroupEvent, Packet, BotPassiveEvent, AbstractEvent() {
    override val group: Group
        get() = operator.group
}

/**
 * Bot 被取消禁言
 */
data class BotUnmuteEvent(
    /**
     * 操作人.
     */
    val operator: Member
) : GroupEvent, Packet, BotPassiveEvent, AbstractEvent() {
    override val group: Group
        get() = operator.group
}

/**
 * Bot 成功加入了一个新群
 */
@MiraiExperimentalAPI
data class BotJoinGroupEvent(
    override val group: Group
) : BotPassiveEvent, GroupEvent, Packet, AbstractEvent()

// region 群设置

/**
 * 群设置改变. 此事件广播前修改就已经完成.
 */
interface GroupSettingChangeEvent<T> : GroupEvent, BotPassiveEvent, BroadcastControllable {
    val origin: T
    val new: T

    override val shouldBroadcast: Boolean
        get() = origin != new
}

/**
 * 群名改变. 此事件广播前修改就已经完成.
 */
data class GroupNameChangeEvent(
    override val origin: String,
    override val new: String,
    override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    override val operator: Member?
) : GroupSettingChangeEvent<String>, Packet, GroupOperableEvent, AbstractEvent() {
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    val isByBot: Boolean
        get() = operator == null
}

/**
 * 入群公告改变. 此事件广播前修改就已经完成.
 */
data class GroupEntranceAnnouncementChangeEvent(
    override val origin: String,
    override val new: String,
    override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    override val operator: Member?
) : GroupSettingChangeEvent<String>, Packet, GroupOperableEvent, AbstractEvent()


/**
 * 群 "全员禁言" 功能状态改变. 此事件广播前修改就已经完成.
 */
data class GroupMuteAllEvent(
    override val origin: Boolean,
    override val new: Boolean,
    override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    override val operator: Member?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent()


/**
 * 群 "匿名聊天" 功能状态改变. 此事件广播前修改就已经完成.
 */
data class GroupAllowAnonymousChatEvent(
    override val origin: Boolean,
    override val new: Boolean,
    override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    override val operator: Member?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent()


/**
 * 群 "坦白说" 功能状态改变. 此事件广播前修改就已经完成.
 */
data class GroupAllowConfessTalkEvent(
    override val origin: Boolean,
    override val new: Boolean,
    override val group: Group,
    val isByBot: Boolean // 无法获取操作人
) : GroupSettingChangeEvent<Boolean>, Packet, AbstractEvent()

/**
 * 群 "允许群员邀请好友加群" 功能状态改变. 此事件广播前修改就已经完成.
 */
data class GroupAllowMemberInviteEvent(
    override val origin: Boolean,
    override val new: Boolean,
    override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    override val operator: Member?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent()


// endregion


// region 群成员

// region 成员变更

/**
 * 成员已经加入群的事件
 */
sealed class MemberJoinEvent(override val member: Member) : GroupMemberEvent, BotPassiveEvent, Packet, AbstractEvent() {
    /**
     * 被邀请加入群
     */
    data class Invite(override val member: Member) : MemberJoinEvent(member)

    /**
     * 成员主动加入群
     */
    data class Active(override val member: Member) : MemberJoinEvent(member)
}

/**
 * 成员已经离开群的事件. 在事件广播前成员就已经从 [Group.members] 中删除
 */
sealed class MemberLeaveEvent : GroupMemberEvent, AbstractEvent() {
    /**
     * 成员被踢出群. 成员不可能是机器人自己.
     */
    data class Kick(
        override val member: Member,
        /**
         * 操作人. 为 null 则是机器人操作.
         */
        override val operator: Member?
    ) : MemberLeaveEvent(), Packet, GroupOperableEvent {
        override fun toString(): String {
            return "MemberLeaveEvent.Kick(member=$member, operator=$operator)"
        }
    }

    /**
     * 成员主动离开
     */
    data class Quit(override val member: Member) : MemberLeaveEvent(), Packet {
        override fun toString(): String {
            return "MemberLeaveEvent.Quit(member=$member)"
        }
    }
}

/**
 * [Bot] 被邀请加入一个群.
 */
data class BotInvitedJoinGroupRequestEvent internal constructor(
    override val bot: Bot,
    /**
     * 事件唯一识别号
     */
    val eventId: Long,
    /**
     * 邀请入群的账号的 id
     */
    val invitorId: Long,
    val groupId: Long,
    val groupName: String,
    /**
     * 邀请人昵称
     */
    val invitorNick: String
) : BotEvent, Packet, AbstractEvent() {
    val invitor: Friend = this.bot.getFriend(invitorId)

    @JvmField
    internal val responded: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

    @JvmSynthetic
    suspend fun accept() = bot.acceptInvitedJoinGroupRequest(this)

    @JvmSynthetic
    suspend fun ignore() = bot.ignoreInvitedJoinGroupRequest(this)

    @JavaFriendlyAPI
    @JvmName("accept")
    fun __acceptBlockingForJava__() =
        runBlocking { bot.acceptInvitedJoinGroupRequest(this@BotInvitedJoinGroupRequestEvent) }

    @JavaFriendlyAPI
    @JvmName("ignore")
    fun __ignoreBlockingForJava__() =
        runBlocking { bot.ignoreInvitedJoinGroupRequest(this@BotInvitedJoinGroupRequestEvent) }
}

/**
 * 一个账号请求加入群事件, [Bot] 在此群中是管理员或群主.
 */
data class MemberJoinRequestEvent internal constructor(
    override val bot: Bot,
    /**
     * 事件唯一识别号
     */
    val eventId: Long,
    /**
     * 入群申请消息
     */
    val message: String,
    /**
     * 申请入群的账号的 id
     */
    val fromId: Long,
    val groupId: Long,
    val groupName: String,
    /**
     * 申请人昵称
     */
    val fromNick: String
) : BotEvent, Packet, AbstractEvent() {
    val group: Group = this.bot.getGroup(groupId)

    @JvmField
    internal val responded: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

    @JvmSynthetic
    suspend fun accept() = bot.acceptMemberJoinRequest(this)

    @JvmSynthetic
    suspend fun reject(blackList: Boolean = false) = bot.rejectMemberJoinRequest(this, blackList)

    @JvmSynthetic
    suspend fun ignore(blackList: Boolean = false) = bot.ignoreMemberJoinRequest(this, blackList)


    @JavaFriendlyAPI
    @JvmName("accept")
    fun __acceptBlockingForJava__() = runBlocking { bot.acceptMemberJoinRequest(this@MemberJoinRequestEvent) }

    @JavaFriendlyAPI
    @JvmOverloads
    @JvmName("reject")
    fun __rejectBlockingForJava__(blackList: Boolean = false) =
        runBlocking { bot.rejectMemberJoinRequest(this@MemberJoinRequestEvent, blackList) }

    @JavaFriendlyAPI
    @JvmOverloads
    @JvmName("ignore")
    fun __ignoreBlockingForJava__(blackList: Boolean = false) =
        runBlocking { bot.ignoreMemberJoinRequest(this@MemberJoinRequestEvent, blackList) }
}

// endregion

// region 名片和头衔

/**
 * 成员群名片改动. 此事件广播前修改就已经完成.
 */
data class MemberCardChangeEvent(
    /**
     * 修改前
     */
    val origin: String,

    /**
     * 修改后
     */
    val new: String,

    override val member: Member
) : GroupMemberEvent, Packet, AbstractEvent()

/**
 * 成员群头衔改动. 一定为群主操作
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

    override val member: Member,

    /**
     * 操作人.
     * 不为 null 时一定为群主. 可能与 [member] 引用相同, 此时为群员自己修改.
     * 为 null 时则是机器人操作.
     */
    override val operator: Member?
) : GroupMemberEvent, GroupOperableEvent, AbstractEvent()

// endregion


// region 成员权限

/**
 * 成员权限改变的事件. 成员不可能是机器人自己.
 */
data class MemberPermissionChangeEvent(
    override val member: Member,
    val origin: MemberPermission,
    val new: MemberPermission
) : GroupMemberEvent, BotPassiveEvent, Packet, AbstractEvent()

// endregion


// region 禁言

/**
 * 群成员被禁言事件. 被禁言的成员都不可能是机器人本人
 *
 * @see BotMuteEvent 机器人被禁言的事件
 */
data class MemberMuteEvent(
    override val member: Member,
    val durationSeconds: Int,
    /**
     * 操作人. 为 null 则为机器人操作
     */
    override val operator: Member?
) : GroupMemberEvent, Packet, GroupOperableEvent, AbstractEvent()

/**
 * 群成员被取消禁言事件. 被禁言的成员都不可能是机器人本人
 *
 * @see BotUnmuteEvent 机器人被取消禁言的事件
 */
data class MemberUnmuteEvent(
    override val member: Member,
    /**
     * 操作人. 为 null 则为机器人操作
     */
    override val operator: Member?
) : GroupMemberEvent, Packet, GroupOperableEvent, AbstractEvent()

// endregion

// endregion
