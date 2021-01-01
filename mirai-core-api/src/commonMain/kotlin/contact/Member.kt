/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "UnusedImport")

package net.mamoe.mirai.contact

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.utils.WeakRefProperty

/**
 * 代表一位群成员.
 *
 * 群成员分为 [普通成员][NormalMember] 和 [匿名成员][AnonymousMember]
 *
 * 一个群成员可能也是机器人的好友, 但他们在对象类型上不同 ([Member] != [Friend]). 可以通过 [Member.asFriend] 得到相关好友对象.
 *
 * ## 相关的操作
 * [Member.isFriend] 判断此成员是否为好友
 * [Member.isAnonymous] 判断此成员是否为匿名群成员
 * [Member.isNormal] 判断此成员是否为正常群成员
 */
public interface Member : User {
    /**
     * 所在的群.
     */
    @WeakRefProperty
    public val group: Group

    /**
     * 成员的权限, 动态更新.
     *
     * @see MemberPermissionChangeEvent 权限变更事件. 由群主或机器人的操作触发.
     */
    public val permission: MemberPermission

    /**
     * 群名片. 可能为空.
     *
     * @see [NormalMember.nameCard]
     * @see [AnonymousMember.nameCard]
     */
    public val nameCard: String

    /**
     * 群头衔.
     *
     * 为 [AnonymousMember] 时一定是 `"匿名"`
     *
     * @see [NormalMember.specialTitle]
     */
    public val specialTitle: String

    /**
     * 禁言这个群成员 [durationSeconds] 秒, 在机器人无权限操作时抛出 [PermissionDeniedException].
     *
     * QQ 中最小操作和显示的时间都是一分钟. 机器人可以实现精确到秒, 会被客户端显示为 1 分钟但不影响实际禁言时间.
     *
     * 管理员可禁言成员, 群主可禁言管理员和群员.
     *
     * @param durationSeconds 持续时间. 精确到秒. 最短 0 秒, 最长 30 天. 超过范围则会抛出异常 [IllegalStateException].
     *
     * @see NormalMember.isMuted 判断此成员是否正处于禁言状态中
     * @see NormalMember.unmute 取消禁言此成员
     *
     * @see MemberMuteEvent 成员被禁言事件
     * @see BotMuteEvent Bot 被禁言事件
     *
     * @see Member.mute 支持 Kotlin [kotlin.time.Duration] 的扩展
     */
    @JvmBlockingBridge
    public suspend fun mute(durationSeconds: Int)

    /**
     * 向群成员发送消息.
     * 若群成员同时是好友, 则会发送好友消息. 否则发送临时会话消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * 注意: 只可以向 [NormalMember] 发送消息. 向 [AnonymousMember] 发送时将会得到异常 [UnsupportedOperationException].
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
     * @throws UnsupportedOperationException 当向 [AnonymousMember] 发送消息时抛出
     *
     * @return 消息回执. 可 [引用][MessageReceipt.quote] 或 [撤回][MessageReceipt.recall] 这条消息.
     */
    public override suspend fun sendMessage(message: Message): MessageReceipt<Member>

    /**
     * 发送纯文本消息
     *
     * @see sendMessage
     */
    public override suspend fun sendMessage(message: String): MessageReceipt<Member>

    /**
     * 创建一个 "戳一戳" 消息
     *
     * 注意: 只可以戳 [NormalMember]. 向 [AnonymousMember] 操作时将会得到异常.
     *
     * @see Nudge.sendTo 发送这个戳一戳消息
     */
    public override fun nudge(): MemberNudge
}

/**
 * 得到此成员作为好友的对象.
 *
 * @throws IllegalStateException 当此成员不是好友时抛出
 */
public fun Member.asFriend(): Friend = this.bot.getFriend(this.id) ?: error("$this is not a friend")

/**
 * 得到此成员作为好友的对象, 当此成员不是好友时返回 `null`
 */
public fun Member.asFriendOrNull(): Friend? = this.bot.getFriend(this.id)

/**
 * 得到此成员作为陌生人的对象, 当此成员不是陌生人时返回 `null`
 */
public fun Member.asStrangerOrNull(): Stranger? = this.bot.getStranger(this.id)


/**
 * 判断此成员是否为好友
 */
public inline val Member.isFriend: Boolean
    get() = this.bot.friends.contains(this.id)

/**
 * 获取非空群名片或昵称.
 *
 * 若 [群名片][Member.nameCard] 不为空则返回群名片, 为空则返回 [User.nick]
 */
public val Member.nameCardOrNick: String get() = this.nameCard.takeIf { it.isNotEmpty() } ?: this.nick
public val Member.isNormal: Boolean get() = this is NormalMember
public val Member.isAnonymous: Boolean get() = this is AnonymousMember
