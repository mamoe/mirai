/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:Suppress(
    "FunctionName", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "DEPRECATION_ERROR",
    "MemberVisibilityCanBePrivate"
)

package net.mamoe.mirai.event.events

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * 机器人被踢出群或在其他客户端主动退出一个群. 在事件广播前 [Bot.groups] 就已删除这个群.
 */
public sealed class BotLeaveEvent : BotEvent, Packet, AbstractEvent(), GroupMemberInfoChangeEvent {
    public abstract override val group: Group

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
        public override val operator: NormalMember
    ) : BotLeaveEvent(),
        GroupOperableEvent {
        public override val group: Group get() = operator.group
        public override val bot: Bot get() = super<BotLeaveEvent>.bot
        public override fun toString(): String = "BotLeaveEvent.Kick(group=${group.id},operator=${operator.id})"
    }

    /**
     * 机器人因群主解散群而退出群. 操作人一定是群主
     * @since 2.8
     */
    @MiraiExperimentalApi("BotLeaveEvent 的子类可能在将来改动. 使用 BotLeaveEvent 以保证兼容性.")
    public data class Disband @MiraiInternalApi constructor(
        public override val group: Group
    ) : BotLeaveEvent(), GroupOperableEvent {
        public override val operator: NormalMember = group.owner
        public override fun toString(): String = "BotLeaveEvent.Disband(group=${group.id})"
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
) : BotPassiveEvent, GroupEvent, Packet, AbstractEvent(), GroupMemberInfoChangeEvent

/**
 * Bot 被禁言
 */
public data class BotMuteEvent @MiraiInternalApi constructor(
    public val durationSeconds: Int,
    /**
     * 操作人.
     */
    public val operator: NormalMember
) : GroupEvent, Packet, BotPassiveEvent, AbstractEvent(), GroupMemberInfoChangeEvent {
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
    public val operator: NormalMember
) : GroupEvent, Packet, BotPassiveEvent, AbstractEvent(), GroupMemberInfoChangeEvent {
    public override val group: Group
        get() = operator.group
}

/**
 * Bot 成功加入了一个新群
 */
public sealed class BotJoinGroupEvent : GroupEvent, BotPassiveEvent, Packet, AbstractEvent(),
    GroupMemberInfoChangeEvent {
    public abstract override val group: Group

    /**
     * 不确定, 已知的来源:
     * - Bot 在其他客户端创建群聊而同步到 Bot 客户端.
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
        public val invitor: NormalMember
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
    public override val operator: NormalMember?
) : GroupSettingChangeEvent<String>, Packet, GroupOperableEvent, AbstractEvent(), GroupMemberInfoChangeEvent

/**
 * 入群公告改变. 此事件广播前修改就已经完成.
 */
@DeprecatedSinceMirai(warningSince = "2.12")
@Deprecated("This event is not being triggered anymore.", level = DeprecationLevel.WARNING)
public data class GroupEntranceAnnouncementChangeEvent @MiraiInternalApi constructor(
    public override val origin: String,
    public override val new: String,
    public override val group: Group,
    /**
     * 操作人. 为 null 时则是机器人操作
     */
    public override val operator: NormalMember?
) : GroupSettingChangeEvent<String>, Packet, GroupOperableEvent, AbstractEvent(), GroupMemberInfoChangeEvent


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
    public override val operator: NormalMember?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent(), GroupMemberInfoChangeEvent


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
    public override val operator: NormalMember?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent(), GroupMemberInfoChangeEvent


/**
 * 群 "坦白说" 功能状态改变. 此事件广播前修改就已经完成.
 */
public data class GroupAllowConfessTalkEvent @MiraiInternalApi constructor(
    public override val origin: Boolean,
    public override val new: Boolean,
    public override val group: Group,
    public val isByBot: Boolean // 无法获取操作人
) : GroupSettingChangeEvent<Boolean>, Packet, AbstractEvent(), GroupMemberInfoChangeEvent

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
    public override val operator: NormalMember?
) : GroupSettingChangeEvent<Boolean>, Packet, GroupOperableEvent, AbstractEvent(), GroupMemberInfoChangeEvent


// endregion


// region 群成员

// region 成员变更

/**
 * 成员已经加入群的事件
 */
public sealed class MemberJoinEvent(
    public override val member: NormalMember
) : GroupMemberEvent, BotPassiveEvent, Packet,
    AbstractEvent(), GroupMemberInfoChangeEvent {
    /**
     * 被邀请加入群
     */
    public data class Invite @MiraiInternalApi constructor(
        public override val member: NormalMember,
        /**
         * 邀请者
         */
        public val invitor: NormalMember
    ) : MemberJoinEvent(member) {
        public override fun toString(): String = "MemberJoinEvent.Invite(member=${member.id}, invitor=${invitor.id})"
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
public sealed class MemberLeaveEvent : GroupMemberEvent, AbstractEvent(), GroupMemberInfoChangeEvent {
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
    public override val groupId: Long,
    public val groupName: String,
    /**
     * 邀请人昵称
     */
    public val invitorNick: String
) : BotEvent, Packet, AbstractEvent(), BaseGroupMemberInfoChangeEvent {
    /**
     * 邀请人. 若在事件发生后邀请人已经被删除好友, [invitor] 为 `null`.
     */
    public val invitor: Friend? get() = this.bot.getFriend(invitorId)

    internal val responded: AtomicBoolean = atomic(false)

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
    override val groupId: Long,
    val groupName: String,
    /**
     * 申请人昵称
     */
    val fromNick: String,
    /**
     * 邀请人 id（如果是邀请入群）
     */
    val invitorId: Long? = null
) : BotEvent, Packet, AbstractEvent(), BaseGroupMemberInfoChangeEvent {
    /**
     * 相关群. 若在事件发生后机器人退出这个群, [group] 为 `null`.
     */
    public val group: Group? get() = this.bot.getGroup(groupId)

    /**
     * 邀请入群的成员. 若在事件发生时机器人或该成员退群, [invitor] 为 `null`.
     */
    public val invitor: NormalMember? by lazy { invitorId?.let { group?.get(it) } }

    internal val responded: AtomicBoolean = atomic(false)

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

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public constructor(
        bot: Bot, eventId: Long, message: String,
        fromId: Long, groupId: Long, groupName: String, fromNick: String
    ) : this(bot, eventId, message, fromId, groupId, groupName, fromNick, null)

    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public fun copy(
        bot: Bot, eventId: Long, message: String,
        fromId: Long, groupId: Long, groupName: String, fromNick: String
    ): MemberJoinRequestEvent = copy(
        bot = bot, eventId = eventId, message = message, fromId = fromId,
        groupId = groupId, groupName = groupName, fromNick = fromNick, invitorId = null
    )

    internal companion object {
        @Suppress("unused")
        @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
        @JvmStatic
        @JvmName("copy\$default") // avoid being mangled
        fun copy_default(
            var0: MemberJoinRequestEvent, var1: Bot, var2: Long, var4: String, var5: Long, var7: Long,
            var9: String, var10: String, var11: Int, @Suppress("UNUSED_PARAMETER") var12: Any
        ): MemberJoinRequestEvent {
            var bot = var1
            var eventId = var2
            var message = var4
            var fromId = var5
            var groupId = var7
            var groupName = var9
            var fromNick = var10
            if (var11 and 1 != 0) bot = var0.bot
            if (var11 and 2 != 0) eventId = var0.eventId
            if (var11 and 4 != 0) message = var0.message
            if (var11 and 8 != 0) fromId = var0.fromId
            if (var11 and 16 != 0) groupId = var0.groupId
            if (var11 and 32 != 0) groupName = var0.groupName
            if (var11 and 64 != 0) fromNick = var0.fromNick
            return var0.copy(
                bot = bot, eventId = eventId, message = message,
                fromId = fromId, groupId = groupId, groupName = groupName, fromNick = fromNick
            )
        }
    }
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

    public override val member: NormalMember
) : GroupMemberEvent, Packet, AbstractEvent(), GroupMemberInfoChangeEvent

/**
 * 成员群特殊头衔改动. 一定为群主操作
 *
 * 由于服务器并不会告知特殊头衔的重置, 因此此事件在特殊头衔重置后只能由 mirai 在发现变动时才广播
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

    public override val member: NormalMember,

    /**
     * 操作人.
     * 不为 null 时一定为群主. 可能与 [member] 引用相同, 此时为群员自己修改.
     * 为 null 时则是机器人操作.
     */
    public override val operator: NormalMember?
) : GroupMemberEvent, GroupOperableEvent, AbstractEvent(), Packet, GroupMemberInfoChangeEvent

// endregion


// region 成员权限

/**
 * 成员权限改变的事件. 成员不可能是机器人自己.
 */
public data class MemberPermissionChangeEvent @MiraiInternalApi constructor(
    public override val member: NormalMember,
    public val origin: MemberPermission,
    public val new: MemberPermission
) : GroupMemberEvent, BotPassiveEvent, Packet, AbstractEvent(), GroupMemberInfoChangeEvent

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
) : GroupMemberEvent, Packet, GroupOperableEvent, AbstractEvent(), GroupMemberInfoChangeEvent

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
) : GroupMemberEvent, Packet, GroupOperableEvent, AbstractEvent(), GroupMemberInfoChangeEvent

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
    public data class Achieve(override val member: NormalMember, override val honorType: GroupHonorType) :
        MemberHonorChangeEvent() {

        override fun toString(): String {
            return "MemberHonorChangeEvent.Achieve(member=$member, honorType=$honorType)"
        }
    }

    /**
     * 失去荣誉时的事件
     */
    public data class Lose(override val member: NormalMember, override val honorType: GroupHonorType) :
        MemberHonorChangeEvent() {

        override fun toString(): String {
            return "MemberHonorChangeEvent.Lose(member=$member, honorType=$honorType)"
        }
    }
}

/**
 * [Group] 龙王改变时的事件
 */
public data class GroupTalkativeChangeEvent(
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
