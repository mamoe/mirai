/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE", "EXPERIMENTAL_OVERRIDE")
@file:OptIn(JavaFriendlyAPI::class)

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.MessageReceipt.Companion.quote
import net.mamoe.mirai.message.MessageReceipt.Companion.recall
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.OverFileSizeMaxException
import net.mamoe.mirai.utils.WeakRefProperty

/**
 * 联系对象, 即可以与 [Bot] 互动的对象. 包含 [用户][User], 和 [群][Group].
 */
public interface Contact : ContactOrBot, CoroutineScope {
    /**
     * 这个联系对象所属 [Bot].
     */
    @WeakRefProperty
    public override val bot: Bot

    /**
     * 可以是 QQ 号码或者群号码.
     *
     * @see User.id
     * @see Group.id
     */
    public override val id: Long

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see MessagePreSendEvent 发送消息前事件
     * @see MessagePostSendEvent 发送消息后事件
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可 [引用回复][MessageReceipt.quote]（仅群聊）或 [撤回][MessageReceipt.recall] 这条消息.
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: Message): MessageReceipt<Contact>

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: String): MessageReceipt<Contact> = this.sendMessage(message.toPlainText())

    /**
     * 上传一个图片以备发送.
     *
     * @see Image 查看有关图片的更多信息, 如上传图片
     *
     * @see BeforeImageUploadEvent 图片发送前事件, 可拦截.
     * @see ImageUploadEvent 图片发送完成事件, 不可拦截.
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时抛出. (最大大小约为 20 MB, 但 mirai 限制的大小为 30 MB)
     */
    @JvmBlockingBridge
    public suspend fun uploadImage(image: ExternalImage): Image

    /**
     * @return "Friend($id)" or "Group($id)" or "Member($id)"
     */
    public override fun toString(): String
}

/**
 * @see IMirai.recallMessage
 */
@JvmSynthetic
public suspend inline fun Contact.recallMessage(source: MessageChain): Unit = Mirai.recallMessage(bot, source)

/**
 * @see IMirai.recallMessage
 */
@JvmSynthetic
public suspend inline fun Contact.recallMessage(source: MessageSource): Unit = Mirai.recallMessage(bot, source)