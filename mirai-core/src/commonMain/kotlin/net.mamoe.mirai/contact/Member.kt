/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "UnusedImport")

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.recall
import net.mamoe.mirai.utils.WeakRefProperty
import kotlin.jvm.JvmSynthetic
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * 代表一位群成员.
 *
 * 群成员可能也是好友, 但他们在对象类型上不同.
 * 群成员可以通过 [asFriend] 得到相关好友对象.
 *
 * ## 与好友相关的操作
 * [Member.isFriend] 判断此成员是否为好友
 */
@Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_SUPER_CLASS")
@OptIn(JavaFriendlyAPI::class)
public abstract class Member : MemberJavaFriendlyAPI, User() {
    /**
     * 所在的群.
     */
    @WeakRefProperty
    public abstract val group: Group

    /**
     * 成员的权限, 动态更新.
     *
     * @see MemberPermissionChangeEvent 权限变更事件. 由群主或机器人的操作触发.
     */
    public abstract val permission: MemberPermission

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
    public abstract var nameCard: String

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
    public abstract var specialTitle: String

    /**
     * 被禁言剩余时长. 单位为秒.
     *
     * @see isMuted 判断改成员是否处于禁言状态
     * @see mute 设置禁言
     * @see unmute 取消禁言
     */
    public abstract val muteTimeRemaining: Int

    /**
     * 禁言.
     *
     * QQ 中最小操作和显示的时间都是一分钟.
     * 机器人可以实现精确到秒, 会被客户端显示为 1 分钟但不影响实际禁言时间.
     *
     * 管理员可禁言成员, 群主可禁言管理员和群员.
     *
     * @param durationSeconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 机器人无权限时返回 `false`
     *
     * @see Member.isMuted 判断此成员是否正处于禁言状态中
     * @see unmute 取消禁言此成员
     *
     * @see Int.minutesToSeconds
     * @see Int.hoursToSeconds
     * @see Int.daysToSeconds
     *
     * @see MemberMuteEvent 成员被禁言事件
     *
     * @throws PermissionDeniedException 无权限修改时抛出
     */
    @JvmSynthetic
    public abstract suspend fun mute(durationSeconds: Int)

    /**
     * 解除禁言.
     *
     * 管理员可解除成员的禁言, 群主可解除管理员和群员的禁言.
     *
     * @see Member.isMuted 判断此成员是否正处于禁言状态中
     *
     * @see MemberUnmuteEvent 成员被取消禁言事件
     *
     * @throws PermissionDeniedException 无权限修改时抛出
     */
    @JvmSynthetic
    public abstract suspend fun unmute()

    /**
     * 踢出该成员.
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmSynthetic
    public abstract suspend fun kick(message: String = "")

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
    @JvmSynthetic
    public abstract override suspend fun sendMessage(message: Message): MessageReceipt<Member>

    /**
     * @see sendMessage
     */
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "VIRTUAL_MEMBER_HIDDEN", "OVERRIDE_BY_INLINE")
    @kotlin.internal.InlineOnly
    @JvmSynthetic
    public suspend inline fun sendMessage(message: String): MessageReceipt<Member> {
        return sendMessage(PlainText(message))
    }

    public final override fun toString(): String = "Member($id)"
}

/**
 * 得到此成员作为好友的对象.
 *
 * @throws IllegalStateException 当此成员不是好友时抛出
 */
public fun Member.asFriend(): Friend = this.bot.getFriendOrNull(this.id) ?: error("$this is not a friend")

/**
 * 得到此成员作为好友的对象, 当此成员不是好友时返回 `null`
 */
public fun Member.asFriendOrNull(): Friend? = this.bot.getFriendOrNull(this.id)

/**
 * 判断此成员是否为好友
 */
public inline val Member.isFriend: Boolean
    get() = this.bot.friends.contains(this.id)

/**
 * 如果此成员是好友, 则执行 [block] 并返回其返回值. 否则返回 `null`
 */
public inline fun <R> Member.takeIfIsFriend(block: (Friend) -> R): R? {
    return this.asFriendOrNull()?.let(block)
}

/**
 * 获取非空群名片或昵称.
 *
 * 若 [群名片][Member.nameCard] 不为空则返回群名片, 为空则返回 [User.nick]
 */
public val Member.nameCardOrNick: String get() = this.nameCard.takeIf { it.isNotEmpty() } ?: this.nick

/**
 * 获取非空群名片或昵称.
 *
 * @return 当 [User] 为 [Member] 时返回 [Member.nameCardOrNick]
 *
 * 否则返回 [Member.nick]
 */
public val User.nameCardOrNick: String
    get() = when (this) {
        is Member -> this.nameCardOrNick
        else -> this.nick
    }

/**
 * 判断群成员是否处于禁言状态.
 */
public val Member.isMuted: Boolean
    get() = muteTimeRemaining != 0 && muteTimeRemaining != 0xFFFFFFFF.toInt()

/**
 * @see Member.mute
 */
@ExperimentalTime
public suspend inline fun Member.mute(duration: Duration) {
    require(duration.inDays <= 30) { "duration must be at most 1 month" }
    require(duration.inSeconds > 0) { "duration must be greater than 0 second" }
    this.mute(duration.inSeconds.toInt())
}

/**
 * @see Member.mute
 */
public suspend inline fun Member.mute(durationSeconds: Long): Unit = this.mute(durationSeconds.toInt())