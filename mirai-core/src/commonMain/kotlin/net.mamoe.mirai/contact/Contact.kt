/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE", "EXPERIMENTAL_OVERRIDE")
@file:OptIn(MiraiInternalAPI::class, JavaFriendlyAPI::class)

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.event.events.BeforeImageUploadEvent
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.ImageUploadEvent
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.recall
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmSynthetic


/**
 * 联系人. 虽然叫做联系人, 但他的子类有 [用户][User], 和 [群][Group].
 */
abstract class Contact : CoroutineScope, ContactJavaFriendlyAPI(), ContactOrBot {
    /**
     * 这个联系人所属 [Bot].
     */
    @WeakRefProperty
    abstract val bot: Bot

    /**
     * 可以是 QQ 号码或者群号码.
     *
     * 对于 [QQ], `uin` 与 `id` 是相同的意思.
     * 对于 [Group], `groupCode` 与 `id` 是相同的意思.
     *
     * @see QQ.id
     * @see Group.id
     */
    abstract override val id: Long

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
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可 [引用回复][MessageReceipt.quote]（仅群聊）或 [撤回][MessageReceipt.recall] 这条消息.
     */
    @JvmSynthetic
    abstract suspend fun sendMessage(message: Message): MessageReceipt<Contact>

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "VIRTUAL_MEMBER_HIDDEN", "OVERRIDE_BY_INLINE")
    @kotlin.internal.InlineOnly // purely virtual
    @JvmSynthetic
    suspend inline fun sendMessage(message: String): MessageReceipt<Contact> {
        return sendMessage(message.toMessage())
    }

    /**
     * 上传一个图片以备发送.
     *
     * @see Image 查看有关图片的更多信息
     *
     * @see BeforeImageUploadEvent 图片发送前事件, 可拦截.
     * @see ImageUploadEvent 图片发送完成事件, 不可拦截.
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时抛出. (最大大小约为 20 MB, 但 mirai 限制的大小为 30 MB)
     */
    @JvmSynthetic
    abstract suspend fun uploadImage(image: ExternalImage): OfflineImage

    final override fun equals(other: Any?): Boolean = super.equals(other)
    final override fun hashCode(): Int = super.hashCode()

    /**
     * @return "Friend($id)" or "Group($id)" or "Member($id)"
     */
    abstract override fun toString(): String
}

/**
 * @see Bot.recall
 */
@MiraiExperimentalAPI
@JvmSynthetic
suspend inline fun Contact.recall(source: MessageChain) = this.bot.recall(source)

/**
 * @see Bot.recall
 */
@JvmSynthetic
suspend inline fun Contact.recall(source: MessageSource) = this.bot.recall(source)

/**
 * @see Bot.recallIn
 */
@MiraiExperimentalAPI
@JvmSynthetic
inline fun Contact.recallIn(
    message: MessageChain,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.bot.recallIn(message, millis, coroutineContext)

/**
 * @see Bot.recallIn
 */
@JvmSynthetic
inline fun Contact.recallIn(
    source: MessageSource,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.bot.recallIn(source, millis, coroutineContext)