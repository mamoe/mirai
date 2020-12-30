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
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.action.StrangerNudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 代表一位陌生人.
 *
 * 一个 [Stranger] 实例并不是独立的, 它属于一个 [Bot].
 * 对于同一个 [Bot], 任何一个人的 [Stranger] 实例都是单一的.
 * [Stranger] 无法通过任何方式直接构造. 任何时候都应从 [Bot.getStranger] 或事件中获取.
 *
 * 陌生人的来源：当将添加好友设置为
 * ‘任何人可添加为好友’或‘需要回答对验证问题时’
 * 且被他人成功添加时此人会成为陌生人
 *
 * 陌生人需要主动添加好友才能构成好友关系
 * 但 Mirai 将不会提供此功能
 * 请手动在其他客户端添加好友
 *
 * @see StrangerMessageEvent
 */
public interface Stranger : User, CoroutineScope {
    /**
     * QQ 号码
     */
    public override val id: Long

    /**
     * 昵称
     */
    public override val nick: String


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
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    @JvmBlockingBridge
    public override suspend fun sendMessage(message: Message): MessageReceipt<Stranger>

    /**
     * 删除并屏蔽该陌生人, 屏蔽后对方将无法发送临时会话消息
     *
     * @see StrangerRelationChangeEvent.Deleted 陌生人删除事件
     */
    @JvmBlockingBridge
    public suspend fun delete()

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    @JvmBlockingBridge
    public override suspend fun sendMessage(message: String): MessageReceipt<Stranger> =
        this.sendMessage(message.toPlainText())

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see Nudge.sendTo 发送这个戳一戳消息
     */
    @MiraiExperimentalApi
    public override fun nudge(): StrangerNudge = StrangerNudge(this)
}