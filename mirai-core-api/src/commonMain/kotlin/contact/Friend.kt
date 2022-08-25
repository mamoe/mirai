/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "UnusedImport")
@file:JvmBlockingBridge

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.roaming.RoamingSupported
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.FriendNudge
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 代表一位好友.
 *
 * 一个 [Friend] 实例并不是独立的, 它属于一个 [Bot].
 * 对于同一个 [Bot], 任何一个人的 [Friend] 实例都是单一的.
 * [Friend] 无法通过任何方式直接构造. 任何时候都应从 [Bot.getFriend] 或事件中获取.
 *
 * @see FriendMessageEvent
 */
@Suppress("RedundantSetter")
@NotStableForInheritance
public interface Friend : User, CoroutineScope, AudioSupported, RoamingSupported {

    /**
     * 该好友所在的好友分组
     *
     * @since 2.13
     */
    public val friendGroup: FriendGroup


    /**
     * 备注信息
     *
     * 更改备注后会广播 [FriendRemarkChangeEvent]
     *
     * @see User.remarkOrNick
     * @see FriendRemarkChangeEvent
     */
    override var remark: String
        /**
         * @since 2.13
         */
        set

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
    public override suspend fun sendMessage(message: Message): MessageReceipt<Friend>

    /**
     * 删除并屏蔽该好友, 屏蔽后对方将无法发送临时会话消息
     *
     * @see FriendDeleteEvent 好友删除事件
     */
    public suspend fun delete()

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    public override suspend fun sendMessage(message: String): MessageReceipt<Friend> =
        this.sendMessage(message.toPlainText())

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see Nudge.sendTo 发送这个戳一戳消息
     */
    public override fun nudge(): FriendNudge = FriendNudge(this)
}