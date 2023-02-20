/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")
@file:JvmBlockingBridge

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.UserMessagePostSendEvent
import net.mamoe.mirai.event.events.UserMessagePreSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.action.UserNudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 代表一个 **用户**.
 *
 * 其子类有 [群成员][Member] 和 [好友][Friend].
 * 虽然群成员也可能是好友, 但他们仍是不同的两个类型.
 *
 * 注意: 一个 [User] 实例并不是独立的, 它属于一个 [Bot].
 *
 * 对于同一个 [Bot] 任何一个人的 [User] 实例都是单一的.
 */
@NotStableForInheritance
public interface User : Contact, UserOrBot, CoroutineScope {
    /**
     * QQ 号码
     */
    public override val id: Long

    /**
     * 备注信息
     *
     * 仅 [Bot] 与 [User] 存在好友关系的时候才可能存在备注
     *
     * [Bot] 与 [User] 没有好友关系时永远为空[字符串][String] ("")
     *
     * @see [User.remarkOrNick]
     */
    public val remark: String

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see UserMessagePreSendEvent 发送消息前事件
     * @see UserMessagePostSendEvent 发送消息后事件
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可 [引用][MessageReceipt.quote] 或 [撤回][MessageReceipt.recall] 这条消息.
     */
    public override suspend fun sendMessage(message: Message): MessageReceipt<User>

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    public override suspend fun sendMessage(message: String): MessageReceipt<User> =
        this.sendMessage(message.toPlainText())

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see Nudge.sendTo 发送这个戳一戳消息
     */
    public override fun nudge(): UserNudge

    /**
     * 查询用户信息.
     *
     * 此函数不会缓存信息. 每次调用此函数都会向服务器查询新信息.
     *
     * @since 2.1
     */
    public suspend fun queryProfile(): UserProfile = Mirai.queryProfile(bot, this.id)
}

/**
 * 获取非空备注或昵称.
 *
 * 若 [备注][User.remark] 不为空则返回备注, 为空则返回 [User.nick]
 */
public val User.remarkOrNick: String get() = this.remark.takeIf { it.isNotEmpty() } ?: this.nick

/**
 * 获取非空备注或群名片.
 *
 * 若 [备注][User.remark] 不为空则返回备注, 为空则返回 [Member.nameCard]
 */
public val Member.remarkOrNameCard: String get() = this.remark.takeIf { it.isNotEmpty() } ?: this.nameCard

/**
 * 获取非空备注或群名片或昵称.
 *
 * 若 [备注][User.remark] 不为空则返回备注, 为空则返回 [Member.nameCardOrNick]
 */
public val Member.remarkOrNameCardOrNick: String get() = this.remark.takeIf { it.isNotEmpty() } ?: this.nameCardOrNick

