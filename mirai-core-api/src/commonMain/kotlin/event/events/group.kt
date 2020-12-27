/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:Suppress("unused", "FunctionName", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "DEPRECATION_ERROR")

package net.mamoe.mirai.event.events

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 机器人被踢出群或在其他客户端主动退出一个群. 在事件广播前 [Bot.groups] 就已删除这个群.
 */
public sealed class BotLeaveEvent : BotEvent, Packet, AbstractEvent() {
    public abstract val group: Group

    /**
     * 机器人主动退出一个群.
     */
    @MiraiExperimentalApi("目前此事件类型不一定正确. 部分被踢出情况也会广播此事件.")
    public data class Active @MiraiInternalApi constructor(
        public override val group: Group
    ) : BotLeaveEvent() {
        public override fun toString(): String = "BotLeaveEvent.Active(group=${group.id})"
    }

    /**
     * 机器人被管理员或群主踢出群.
     */
    @MiraiExperimentalApi("BotLeaveEvent 的子类可能在将来改动. 使用 BotLeaveEvent 以保证兼容性.")
    public data class Kick @MiraiInternalApi constructor(
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
public data class BotGroupPermissionChangeEvent @MiraiInternalApi constructor(
    public override val group: Group,
    public val origin: MemberPermission,
    public val new: MemberPermission
) : BotPassiveEvent, GroupEvent, Packet, AbstractEvent()

/**
 * Bot 被禁言
 */
public data class BotMuteEvent @MiraiInternalApi constructor(
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
public data class BotUnmuteEvent @MiraiInternalApi constructor(
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
public sealed class BotJoinGroupEvent : GroupEvent, BotPassiveEvent, Packet, AbstractEvent() {
    public abstract override val group: Group

    /**
     * 不确定. 可能是主动加入
     */
    @MiraiExperimentalApi
    public data class Active @MiraiInternalApi constructor(
        public override val group: Group
    ) : BotJoinGroupEvent() {
        public override fun toString(): String = "BotJoinGroupEvent.Active(group=$group)"
    }

    /**
     * Bot 被一个群内的成员直接邀请加入了群.
     *
     * 此时服务器基于 Bot 的 QQ 设置自动同意了请求.
     */
    @MiraiExperimentalApi
    public data class Invite @MiraiInternalApi constructor(
        /**
         * 邀请人
         */
        public val invitor: Member
    ) : BotJoinGroupEvent() {
        public override val group: Group get() = invitor.group

        public override fun toString(): String {
            return "BotJoinGroupEvent.Invite(invitor=$invitor)"
        }
    }

    /**
     * 原群主通过 https://huifu.qq.com/ 恢复原来群主身份并入群,
     * [Bot] 是原群主
     */
    @MiraiExperimentalApi
    public data class Retrieve @MiraiInternalApi constructor(
        public override val group: Group
    ) : BotJoinGroupEvent() {
        override fun toString(): String = "BotJoinGroupEvent.Retrieve(group=${group.id})"
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
public data class GroupNameChangeEvent @MiraiInternalApi constructor(
    public override val origin: String,
    public override val new: String,
    public override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    public override val operator: Member?
) : GroupSettingChangeEvent<String>, Packet, GroupOperableEvent, AbstractEvent()

/**
 * 入群公告改变. 此事件广播前修改就已经完成.
 */
public data class GroupEntranceAnnouncementChangeEvent @MiraiInternalApi constructor(
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
public data class GroupMuteAllEvent @MiraiInternalApi constructor(
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
public data class GroupAllowAnonymousChatEvent @MiraiInternalApi constructor(
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
public data class GroupAllowConfessTalkEvent @MiraiInternalApi constructor(
    public override val origin: Boolean,
    public override val new: Boolean,
    public override val group: Group,
    public val isByBot: Boolean // 无法获取操作人
) : GroupSettingChangeEvent<Boolean>, Packet, AbstractEvent()

/**
 * 群 "允许群员邀请好友加群" 功能状态改变. 此事件广播前修改就已经完成.
 */
public data class GroupAllowMemberInviteEvent @MiraiInternalApi constructor(
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
    public override val member: NormalMember
) : GroupMemberEvent, BotPassiveEvent, Packet,
    AbstractEvent() {
    /**
     * 被邀请加入群
     */
    public data class Invite @MiraiInternalApi constructor(
        public override val member: NormalMember
    ) : MemberJoinEvent(member) {
        public override fun toString(): String = "MemberJoinEvent.Invite(member=${member.id})"
    }

    /**
     * 成员主动加入群
     */
    public data class Active @MiraiInternalApi constructor(
        public override val member: NormalMember
    ) : MemberJoinEvent(member) {
        public override fun toString(): String = "MemberJoinEvent.Active(member=${member.id})"
    }

    /**
     * 原群主通过 https://huifu.qq.com/ 恢复原来群主身份并入群,
     * 此时 [member] 的 [Member.permission] 肯定是 [MemberPermission.OWNER]
     */
    public data class Retrieve @MiraiInternalApi constructor(
        public override val member: NormalMember
    ) : MemberJoinEvent(member) {
        override fun toString(): String = "MemberJoinEvent.Retrieve(member=${member.id})"
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
        public override val member: NormalMember,
        /**
         * 操作人. 为 null 则是机器人操作.
         */
        public override val operator: NormalMember?
    ) : MemberLeaveEvent(), Packet, GroupOperableEvent {
        public override fun toString(): String = "MemberLeaveEvent.Kick(member=${member.id}, operator=${operator?.id})"
    }

    /**
     * 成员主动离开
     */
    public data class Quit(
        public override val member: NormalMember
    ) : MemberLeaveEvent(), Packet {
        public override fun toString(): String = "MemberLeaveEvent.Quit(member=${member.id})"
    }
}

/**
 * [Bot] 被邀请加入一个群.
 */
@Suppress("DEPRECATION")
public data class BotInvitedJoinGroupRequestEvent @MiraiInternalApi constructor(
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
    /**
     * 邀请人. 若在事件发生后邀请人已经被删除好友, [invitor] 为 `null`.
     */
    public val invitor: Friend? get() = this.bot.getFriend(invitorId)

    @JvmField
    internal val responded: AtomicBoolean = AtomicBoolean(false)

    @JvmBlockingBridge
    public suspend fun accept(): Unit = Mirai.acceptInvitedJoinGroupRequest(this)

    @JvmBlockingBridge
    public suspend fun ignore(): Unit = Mirai.ignoreInvitedJoinGroupRequest(this)
}

/**
 * 一个账号请求加入群事件, [Bot] 在此群中是管理员或群主.
 */
@Suppress("DEPRECATION")
public data class MemberJoinRequestEvent @MiraiInternalApi constructor(
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
    /**
     * 相关群. 若在事件发生后机器人退出这个群, [group] 为 `null`.
     */
    public val group: Group? get() = this.bot.getGroup(groupId)

    @JvmField
    @PublishedApi
    internal val responded: AtomicBoolean = AtomicBoolean(false)

    /**
     * 同意这个请求
     */
    @JvmBlockingBridge
    public suspend fun accept(): Unit = Mirai.acceptMemberJoinRequest(this)

    /**
     * 拒绝这个请求
     */
    @JvmBlockingBridge
    @JvmOverloads
    public suspend fun reject(blackList: Boolean = false, message: String = ""): Unit =
        Mirai.rejectMemberJoinRequest(this, blackList, message)

    /**
     * 忽略这个请求.
     */
    @JvmBlockingBridge
    public suspend fun ignore(blackList: Boolean = false): Unit = Mirai.ignoreMemberJoinRequest(this, blackList)
}

// endregion

// region 名片和头衔

/**
 * 成员群名片改动. 此事件广播前修改就已经完成.
 *
 * 由于服务器并不会告知名片变动, 此事件只能由 mirai 在发现变动时才广播. 不要依赖于这个事件.
 */
public data class MemberCardChangeEvent @MiraiInternalApi constructor(
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
public data class MemberSpecialTitleChangeEvent @MiraiInternalApi constructor(
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
    public override val operator: NormalMember?
) : GroupMemberEvent, GroupOperableEvent, AbstractEvent()

// endregion


// region 成员权限

/**
 * 成员权限改变的事件. 成员不可能是机器人自己.
 */
public data class MemberPermissionChangeEvent @MiraiInternalApi constructor(
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
public data class MemberMuteEvent @MiraiInternalApi constructor(
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
public data class MemberUnmuteEvent @MiraiInternalApi constructor(
    public override val member: Member,
    /**
     * 操作人. 为 null 则为机器人操作
     */
    public override val operator: Member?
) : GroupMemberEvent, Packet, GroupOperableEvent, AbstractEvent()

// endregion

// region 戳一戳


/**
 * [Member] 被 [戳][Nudge] 的事件.
 */
@MiraiExperimentalApi
public data class MemberNudgedEvent @MiraiInternalApi constructor(
    /**
     * 戳一戳的发起人, 不可能是 bot
     */
    public val from: Member,
    /**
     * 戳一戳的目标 (被戳的群员), 不可能是 bot
     */
    public override val member: Member,
    /**
     * 戳一戳的动作名称
     */
    public val action: String,
    /**
     * 戳一戳中设置的自定义后缀
     */
    public val suffix: String,
) : GroupMemberEvent, BotPassiveEvent, Packet, AbstractEvent()

// endregion

// region 群荣誉
/**
 * [Member] 荣誉改变时的事件, 目前只支持龙王
 */
@MiraiExperimentalApi
public sealed class MemberHonorChangeEvent : GroupMemberEvent, BotPassiveEvent, Packet, AbstractEvent() {
    /**
     * 改变荣誉的群成员
     */
    public abstract override val member: NormalMember

    /**
     * 改变的荣誉类型
     */
    public abstract val honorType: GroupHonorType

    /**
     * 获得荣誉时的事件
     */
    public class Achieve(override val member: NormalMember, override val honorType: GroupHonorType) :
        MemberHonorChangeEvent()

    /**
     * 失去荣誉时的事件
     */
    public class Lose(override val member: NormalMember, override val honorType: GroupHonorType) :
        MemberHonorChangeEvent()
}

/**
 * [Group] 龙王改变时的事件
 */
public class GroupTalkativeChangeEvent(
    /**
     * 改变的群
     */
    override val group: Group,
    /**
     * 当前龙王
     */
    public val now: NormalMember,
    /**
     * 先前龙王
     */
    public val previous: NormalMember
) : Packet, GroupEvent, BotPassiveEvent, AbstractEvent()

// endregion

// endregion
