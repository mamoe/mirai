/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:Suppress("unused", "FunctionName", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "DEPRECATION_ERROR")

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
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.jvm.*

/**
 * 机器人被踢出群或在其他客户端主动退出一个群. 在事件广播前 [Bot.groups] 就已删除这个群.
 */
public sealed class BotLeaveEvent : BotEvent, Packet, AbstractEvent() {
    public abstract val group: Group

    /**
     * 机器人主动退出一个群.
     */
    @MiraiExperimentalAPI("目前此事件类型不一定正确. 部分被踢出情况也会广播此事件.")
    public data class Active internal constructor(
        public override val group: Group
    ) : BotLeaveEvent() {
        public override fun toString(): String = "BotLeaveEvent.Active(group=${group.id})"
    }

    /**
     * 机器人被管理员或群主踢出群.
     */
    @MiraiExperimentalAPI("BotLeaveEvent 的子类可能在将来改动. 使用 BotLeaveEvent 以保证兼容性.")
    public data class Kick internal constructor(
        public override val operator: Member
    ) : BotLeaveEvent(),
        GroupOperableEvent {
        public override val group: Group get() = operator.group
        public override val bot: Bot get() = super<BotLeaveEvent>.bot
        public override fun toString(): String = "BotLeaveEvent.Kick(group=${group.id},operator=${operator.id})"
    }

    public override val bot: Bot get() = group.bot
}

/**
 * Bot 在群里的权限被改变. 操作人一定是群主
 */
public data class BotGroupPermissionChangeEvent internal constructor(
    public override val group: Group,
    public val origin: MemberPermission,
    public val new: MemberPermission
) : BotPassiveEvent, GroupEvent, Packet, AbstractEvent()

/**
 * Bot 被禁言
 */
public data class BotMuteEvent internal constructor(
    public val durationSeconds: Int,
    /**
     * 操作人.
     */
    public val operator: Member
) : GroupEvent, Packet, BotPassiveEvent, AbstractEvent() {
    public override val group: Group
        get() = operator.group
}

/**
 * Bot 被取消禁言
 */
public data class BotUnmuteEvent internal constructor(
    /**
     * 操作人.
     */
    public val operator: Member
) : GroupEvent, Packet, BotPassiveEvent, AbstractEvent() {
    public override val group: Group
        get() = operator.group
}

/**
 * Bot 成功加入了一个新群
 */
public sealed class BotJoinGroupEvent : GroupEvent, Packet, AbstractEvent() {
    public abstract override val group: Group

    /**
     * 不确定. 可能是主动加入
     */
    @MiraiExperimentalAPI
    public data class Active internal constructor(
        public override val group: Group
    ) : BotPassiveEvent, GroupEvent, Packet, AbstractEvent() {
        public override fun toString(): String = "BotJoinGroupEvent.Active(group=$group)"
    }

    /**
     * Bot 被一个群内的成员直接邀请加入了群.
     *
     * 此时服务器基于 Bot 的 QQ 设置自动同意了请求.
     */
    @MiraiExperimentalAPI
    public data class Invite internal constructor(
        /**
         * 邀请人
         */
        public val invitor: Member
    ) : BotPassiveEvent, GroupEvent, Packet, AbstractEvent() {
        public override val group: Group get() = invitor.group

        public override fun toString(): String {
            return "BotJoinGroupEvent.Invite(invitor=$invitor)"
        }
    }
}

// region 群设置

/**
 * 群设置改变. 此事件广播前修改就已经完成.
 */
public interface GroupSettingChangeEvent<T> : GroupEvent, BotPassiveEvent, BroadcastControllable {
    public val origin: T
    public val new: T

    public override val shouldBroadcast: Boolean
        get() = origin != new
}

/**
 * 群名改变. 此事件广播前修改就已经完成.
 */
public data class GroupNameChangeEvent internal constructor(
    public override val origin: String,
    public override val new: String,
    public override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    public override val operator: Member?
) : GroupSettingChangeEvent<String>, Packet, GroupOperableEvent, AbstractEvent() {
    @LowPriorityInOverloadResolution
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    internal val isByBot: Boolean
        get() = operator == null
}

/**
 * 入群公告改变. 此事件广播前修改就已经完成.
 */
public data class GroupEntranceAnnouncementChangeEvent internal constructor(
    public override val origin: String,
    public override val new: String,
    public override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    public override val operator: Member?
) : GroupSettingChangeEvent<String>, Packet, GroupOperableEvent, AbstractEvent()


/**
 * 群 "全员禁言" 功能状态改变. 此事件广播前修改就已经完成.
 */
public data class GroupMuteAllEvent internal constructor(
    public override val origin: Boolean,
    public override val new: Boolean,
    public override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    public override val operator: Member?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent()


/**
 * 群 "匿名聊天" 功能状态改变. 此事件广播前修改就已经完成.
 */
public data class GroupAllowAnonymousChatEvent internal constructor(
    public override val origin: Boolean,
    public override val new: Boolean,
    public override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    public override val operator: Member?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent()


/**
 * 群 "坦白说" 功能状态改变. 此事件广播前修改就已经完成.
 */
public data class GroupAllowConfessTalkEvent internal constructor(
    public override val origin: Boolean,
    public override val new: Boolean,
    public override val group: Group,
    public val isByBot: Boolean // 无法获取操作人
) : GroupSettingChangeEvent<Boolean>, Packet, AbstractEvent()

/**
 * 群 "允许群员邀请好友加群" 功能状态改变. 此事件广播前修改就已经完成.
 */
public data class GroupAllowMemberInviteEvent internal constructor(
    public override val origin: Boolean,
    public override val new: Boolean,
    public override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    public override val operator: Member?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent()


// endregion


// region 群成员

// region 成员变更

/**
 * 成员已经加入群的事件
 */
public sealed class MemberJoinEvent(
    public override val member: Member
) : GroupMemberEvent, BotPassiveEvent, Packet,
    AbstractEvent() {
    /**
     * 被邀请加入群
     */
    public data class Invite internal constructor(
        public override val member: Member
    ) : MemberJoinEvent(member) {
        public override fun toString(): String = "MemberJoinEvent.Invite(member=${member.id})"
    }

    /**
     * 成员主动加入群
     */
    public data class Active internal constructor(
        public override val member: Member
    ) : MemberJoinEvent(member) {
        public override fun toString(): String = "MemberJoinEvent.Active(member=${member.id})"
    }
}

/**
 * 成员已经离开群的事件. 在事件广播前成员就已经从 [Group.members] 中删除
 */
public sealed class MemberLeaveEvent : GroupMemberEvent, AbstractEvent() {
    /**
     * 成员被踢出群. 成员不可能是机器人自己.
     */
    public data class Kick(
        public override val member: Member,
        /**
         * 操作人. 为 null 则是机器人操作.
         */
        public override val operator: Member?
    ) : MemberLeaveEvent(), Packet, GroupOperableEvent {
        public override fun toString(): String = "MemberLeaveEvent.Kick(member=${member.id}, operator=${operator?.id})"
    }

    /**
     * 成员主动离开
     */
    public data class Quit(
        public override val member: Member
    ) : MemberLeaveEvent(), Packet {
        public override fun toString(): String = "MemberLeaveEvent.Quit(member=${member.id})"
    }
}

/**
 * [Bot] 被邀请加入一个群.
 */
@Suppress("DEPRECATION")
public data class BotInvitedJoinGroupRequestEvent internal constructor(
    public override val bot: Bot,
    /**
     * 事件唯一识别号
     */
    public val eventId: Long,
    /**
     * 邀请入群的账号的 id
     */
    public val invitorId: Long,
    public val groupId: Long,
    public val groupName: String,
    /**
     * 邀请人昵称
     */
    public val invitorNick: String
) : BotEvent, Packet, AbstractEvent() {
    public val invitor: Friend get() = this.bot.getFriend(invitorId)

    @JvmField
    internal val responded: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

    @JvmSynthetic
    public suspend fun accept(): Unit = bot.acceptInvitedJoinGroupRequest(this)

    @JvmSynthetic
    public suspend fun ignore(): Unit = bot.ignoreInvitedJoinGroupRequest(this)

    @JavaFriendlyAPI
    @JvmName("accept")
    public fun __acceptBlockingForJava__(): Unit =
        runBlocking { bot.acceptInvitedJoinGroupRequest(this@BotInvitedJoinGroupRequestEvent) }

    @JavaFriendlyAPI
    @JvmName("ignore")
    public fun __ignoreBlockingForJava__(): Unit =
        runBlocking { bot.ignoreInvitedJoinGroupRequest(this@BotInvitedJoinGroupRequestEvent) }
}

/**
 * 一个账号请求加入群事件, [Bot] 在此群中是管理员或群主.
 */
@Suppress("DEPRECATION")
public data class MemberJoinRequestEvent internal constructor(
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
    public val group: Group get() = this.bot.getGroup(groupId)

    @JvmField
    internal val responded: MiraiAtomicBoolean = MiraiAtomicBoolean(false)

    @JvmSynthetic
    public suspend fun accept(): Unit = bot.acceptMemberJoinRequest(this)

    @JvmSynthetic
    @JvmOverloads
    public suspend fun reject(blackList: Boolean = false, message: String = ""): Unit =
        bot.rejectMemberJoinRequest(this, blackList, message)

    @JvmSynthetic
    public suspend fun ignore(blackList: Boolean = false): Unit = bot.ignoreMemberJoinRequest(this, blackList)


    @JavaFriendlyAPI
    @JvmName("accept")
    public fun __acceptBlockingForJava__(): Unit =
        runBlocking { bot.acceptMemberJoinRequest(this@MemberJoinRequestEvent) }

    @JavaFriendlyAPI
    @JvmOverloads
    @JvmName("reject")
    public fun __rejectBlockingForJava__(blackList: Boolean = false, message: String = ""): Unit =
        runBlocking { bot.rejectMemberJoinRequest(this@MemberJoinRequestEvent, blackList, message) }

    @JavaFriendlyAPI
    @JvmOverloads
    @JvmName("ignore")
    public fun __ignoreBlockingForJava__(blackList: Boolean = false): Unit =
        runBlocking { bot.ignoreMemberJoinRequest(this@MemberJoinRequestEvent, blackList) }
}

// endregion

// region 名片和头衔

/**
 * 成员群名片改动. 此事件广播前修改就已经完成.
 *
 * 由于服务器并不会告知名片变动, 此事件只能由 mirai 在发现变动时才广播. 不要依赖于这个事件.
 */
public data class MemberCardChangeEvent internal constructor(
    /**
     * 修改前
     */
    public val origin: String,

    /**
     * 修改后
     */
    public val new: String,

    public override val member: Member
) : GroupMemberEvent, Packet, AbstractEvent()

/**
 * 成员群头衔改动. 一定为群主操作
 */
public data class MemberSpecialTitleChangeEvent internal constructor(
    /**
     * 修改前
     */
    public val origin: String,

    /**
     * 修改后
     */
    public val new: String,

    public override val member: Member,

    /**
     * 操作人.
     * 不为 null 时一定为群主. 可能与 [member] 引用相同, 此时为群员自己修改.
     * 为 null 时则是机器人操作.
     */
    public override val operator: Member?
) : GroupMemberEvent, GroupOperableEvent, AbstractEvent()

// endregion


// region 成员权限

/**
 * 成员权限改变的事件. 成员不可能是机器人自己.
 */
public data class MemberPermissionChangeEvent internal constructor(
    public override val member: Member,
    public val origin: MemberPermission,
    public val new: MemberPermission
) : GroupMemberEvent, BotPassiveEvent, Packet, AbstractEvent()

// endregion


// region 禁言

/**
 * 群成员被禁言事件. 被禁言的成员都不可能是机器人本人
 *
 * @see BotMuteEvent 机器人被禁言的事件
 */
public data class MemberMuteEvent internal constructor(
    public override val member: Member,
    public val durationSeconds: Int,
    /**
     * 操作人. 为 null 则为机器人操作
     */
    public override val operator: Member?
) : GroupMemberEvent, Packet, GroupOperableEvent, AbstractEvent()

/**
 * 群成员被取消禁言事件. 被禁言的成员都不可能是机器人本人
 *
 * @see BotUnmuteEvent 机器人被取消禁言的事件
 */
public data class MemberUnmuteEvent internal constructor(
    public override val member: Member,
    /**
     * 操作人. 为 null 则为机器人操作
     */
    public override val operator: Member?
) : GroupMemberEvent, Packet, GroupOperableEvent, AbstractEvent()

// endregion

// endregion
