/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "UnusedImport")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.FriendNudge
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText

/**
 * 代表一位好友.
 *
 * 一个 [Friend] 实例并不是独立的, 它属于一个 [Bot].
 * 对于同一个 [Bot], 任何一个人的 [Friend] 实例都是单一的.
 * [Friend] 无法通过任何方式直接构造. 任何时候都应从 [Bot.getFriend] 或事件中获取.
 *
 * @see FriendMessageEvent
 */
public interface Friend : User, CoroutineScope {
    /**
     * QQ 号码
     */
    public override val id: Long

    /**
     * 昵称
     */
    public override val nick: String

    /**
     * 好友备注
     */
    public override val remark: String

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see FriendMessagePreSendEvent 发送消息前事件
     * @see FriendMessagePostSendEvent 发送消息后事件
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可 [引用][MessageReceipt.quote] 或 [撤回][MessageReceipt.recall] 这条消息.
     */
    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Friend>

    /**
     * 删除并屏蔽该好友, 屏蔽后对方将无法发送临时会话消息
     *
     * @see FriendDeleteEvent 好友删除事件
     */
    @JvmBlockingBridge
    public suspend fun delete()

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Friend> =
        this.sendMessage(message.toPlainText())

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see Nudge.sendTo 发送这个戳一戳消息
     */
    public override fun nudge(): FriendNudge = FriendNudge(this)
}