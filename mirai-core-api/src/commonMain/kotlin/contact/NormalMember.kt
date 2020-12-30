/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * 代表一位正常的群成员.
 *
 * 群成员可能也是好友, 但他们在对象类型上不同.
 * 群成员可以通过 [asFriend] 得到相关好友对象.
 *
 * ## 相关的操作
 * [Member.isFriend] 判断此成员是否为好友
 */
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
     * 群头衔.
     *
     * 仅群主可以修改群头衔.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see MemberSpecialTitleChangeEvent 群名片被管理员, 自己或 [Bot] 改动事件. 修改时也会触发此事件.
     * @throws PermissionDeniedException 无权限修改时
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
    @JvmBlockingBridge
    public suspend fun unmute()

    /**
     * 踢出该成员.
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     *
     */
    @JvmBlockingBridge
    public suspend fun kick(message: String)

    /**
     * 向群成员发送消息.
     * 若群成员同时是好友, 则会发送好友消息. 否则发送临时会话消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see FriendMessagePreSendEvent 当此成员是好友时发送消息前事件
     * @see FriendMessagePostSendEvent 当此成员是好友时发送消息后事件
     *
     * @see TempMessagePreSendEvent 当此成员不是好友时发送消息前事件
     * @see TempMessagePostSendEvent 当此成员不是好友时发送消息后事件
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<NormalMember>

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    @JvmBlockingBridge
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
 *
 * @return 当 [User] 为 [Member] 时返回 [Member.nameCardOrNick]
 *
 * 否则返回 [Member.nick]
 */
public val User.nameCardOrNick: String
    get() = when (this) {
        is NormalMember -> this.nameCardOrNick
        else -> this.nick
    }

/**
 * 判断群成员是否处于禁言状态.
 */
public val NormalMember.isMuted: Boolean
    get() = muteTimeRemaining != 0 && muteTimeRemaining != 0xFFFFFFFF.toInt()

/**
 * @see Member.mute
 */
@ExperimentalTime
public suspend inline fun NormalMember.mute(duration: Duration) {
    require(duration.inDays <= 30) { "duration must be at most 1 month" }
    require(duration.inSeconds > 0) { "duration must be greater than 0 second" }
    this.mute(duration.inSeconds.toInt())
}
