/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.NotStableForInheritance
import kotlin.jvm.JvmName
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

/**
 * 代表一位普通的群成员.
 *
 * 要查询更多用户信息, 使用 [NormalMember.queryProfile].
 *
 * @see AnonymousMember
 */
@NotStableForInheritance
public interface NormalMember : Member {
    /**
     * 群名片. 可能为空.
     *
     * 管理员和群主都可修改任何人（包括群主）的群名片.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see [nameCardOrNick] 获取非空群名片或昵称
     *
     * @see MemberCardChangeEvent 群名片被管理员, 自己或 [Bot] 改动事件. 修改时也会触发此事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    public override var nameCard: String

    /**
     * 群特殊头衔.
     *
     * 仅群主可以修改群特殊头衔.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see MemberSpecialTitleChangeEvent  成员群特殊头衔改动事件.
     * @throws PermissionDeniedException 无权限修改时
     * @suppress 请勿试图修改其为空字符串来产生空头衔效果，这将使成员实际佩戴头衔退回为[活跃度相关头衔][Member.active]或管理员头衔.
     */
    public override var specialTitle: String

    /**
     * 被禁言剩余时长. 单位为秒.
     *
     * @see isMuted 判断改成员是否处于禁言状态
     * @see mute 设置禁言
     * @see unmute 取消禁言
     */
    public val muteTimeRemaining: Int

    /**
     * 当该群员处于禁言状态时返回 `true`.
     * @since 2.6
     */
    public val isMuted: Boolean get() = muteTimeRemaining != 0

    /**
     * 入群时间. 单位为秒.
     *
     * @since 2.1
     */
    public val joinTimestamp: Int

    /**
     * 最后发言时间. 单位为秒.
     *
     * @since 2.1
     */
    public val lastSpeakTimestamp: Int

    /**
     * 解除禁言.
     *
     * 管理员可解除成员的禁言, 群主可解除管理员和群员的禁言.
     *
     * @see NormalMember.isMuted 判断此成员是否正处于禁言状态中
     *
     * @see MemberUnmuteEvent 成员被取消禁言事件
     *
     * @throws PermissionDeniedException 无权限修改时抛出
     */
    public suspend fun unmute()

    /**
     * 踢出该成员.
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @param block 为 `true` 时拉黑成员
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     *
     */
    public suspend fun kick(message: String, block: Boolean)

    /**
     * 踢出该成员, 默认不拉黑
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     *
     */
    public suspend fun kick(message: String): Unit = kick(message, false)


    /**
     * 给予或移除群成员的管理员权限。
     *
     * 此操作需要 Bot 为群主 [MemberPermission.OWNER]
     *
     * @param operation true 为给予
     *
     * @see MemberPermissionChangeEvent 群成员权限变更事件
     * @throws PermissionDeniedException 无权限修改时抛出
     *
     * @since 2.7
     */
    public suspend fun modifyAdmin(operation: Boolean)

    /**
     * 向群成员发送消息.
     * 若群成员同时是好友, 则会发送好友消息. 否则发送临时会话消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see FriendMessagePreSendEvent 当此成员是好友时发送消息前事件
     * @see FriendMessagePostSendEvent 当此成员是好友时发送消息后事件
     *
     * @see GroupTempMessagePreSendEvent 当此成员不是好友时发送消息前事件
     * @see GroupTempMessagePostSendEvent 当此成员不是好友时发送消息后事件
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    public override suspend fun sendMessage(message: Message): MessageReceipt<NormalMember>

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    public override suspend fun sendMessage(message: String): MessageReceipt<NormalMember> =
        this.sendMessage(message.toPlainText())

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see Nudge.sendTo 发送这个戳一戳消息
     */
    public override fun nudge(): MemberNudge = MemberNudge(this)
}


/**
 * 获取非空群名片或昵称.
 * @return 当 [User] 为 [NormalMember] 时返回 [Member.nameCardOrNick], 否则返回 [Member.nick]
 */ // Java: NormalMemberKt.getNameCardOrNick(user)
public val User.nameCardOrNick: String
    get() = when (this) {
        is NormalMember -> this.nameCardOrNick
        else -> this.nick
    }

/**
 * 获取非空群名片或昵称.
 * @return 当 [UserOrBot] 为 [NormalMember] 时返回 [Member.nameCardOrNick], 否则返回 [Member.nick]
 * @since 2.6
 */
public val UserOrBot.nameCardOrNick: String
    get() = when (this) {
        is NormalMember -> this.nameCardOrNick
        else -> this.nick
    }

/**
 * @see Member.mute
 */
@ExperimentalTime
public suspend inline fun NormalMember.mute(duration: Duration) {
    require(duration.toDouble(DurationUnit.DAYS) <= 30) { "duration must be at most 1 month" }
    require(duration.toDouble(DurationUnit.SECONDS) > 0) { "duration must be greater than 0 second" }
    this.mute(duration.toDouble(DurationUnit.SECONDS).toInt())
}

@OptIn(ExperimentalTime::class)
@Suppress("unused")
@JvmName("mute-fcu0wV4")
@Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
public suspend inline fun NormalMember.mute00(duration: Duration) {
    return mute(duration)
}

/**
 * 判断群成员是否处于禁言状态.
 * @suppress 在 2.6 移入了 [NormalMember] 成员函数. 保留二进制兼容.
 */
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXTENSION_SHADOWED_BY_MEMBER")
@kotlin.internal.LowPriorityInOverloadResolution
public val NormalMember.isMuted: Boolean
    get() = muteTimeRemaining != 0 && muteTimeRemaining != 0xFFFFFFFF.toInt()
