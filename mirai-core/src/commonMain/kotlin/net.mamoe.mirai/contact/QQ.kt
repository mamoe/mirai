/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.jvm.JvmSynthetic

/**
 * QQ 对象.
 *
 * 自 0.39.0 起 mirai 引入 [User] 作为 [Friend] 和 [Member] 的父类,
 * 以备将来支持仅 [Friend] 可用的 API, 如设置备注.
 *
 * 所有 API 均有二进制兼容.
 *
 * 请根据实际情况, 使用 [Friend] 或 [User] 替代.
 */
@PlannedRemoval("1.0.0")
@Deprecated(
    "use Friend or Person instead",
    replaceWith = ReplaceWith("Friend", "net.mamoe.mirai.contact.Friend"),
    level = DeprecationLevel.ERROR
)
@Suppress("DEPRECATION_ERROR")
abstract class QQ : User(), CoroutineScope {
    /**
     * QQ 号码
     */
    abstract override val id: Long

    /**
     * 昵称
     */
    abstract override val nick: String

    /**
     * 头像下载链接
     */
    override val avatarUrl: String
        get() = "http://q1.qlogo.cn/g?b=qq&nk=$id&s=640"

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     *
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    @JvmSynthetic
    abstract override suspend fun sendMessage(message: Message): MessageReceipt<QQ>
}