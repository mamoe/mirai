/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaHappyAPI
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
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 联系人. 虽然叫做联系人, 但他的子类有 [QQ] 和 [群][Group].
 *
 * @author Him188moe
 */ // 不要删除多平台结构 !!! kotlin bug
@OptIn(MiraiInternalAPI::class, JavaHappyAPI::class)
@Suppress("INAPPLICABLE_JVM_NAME")
expect abstract class Contact() : CoroutineScope, ContactJavaHappyAPI {
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
    abstract val id: Long

    /**
     * 向这个对象发送消息.
     *
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException 发送群消息时若 [Bot] 被禁言抛出
     *
     * @return 消息回执. 可 [引用回复][MessageReceipt.quote]（仅群聊）或 [撤回][MessageReceipt.recall] 这条消息.
     */
    @JvmName("sendMessageSuspend")
    @JvmSynthetic
    abstract suspend fun sendMessage(message: Message): MessageReceipt<out Contact>

    @JvmName("sendMessageSuspend")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @JvmSynthetic
    abstract suspend fun sendMessage(message: MessageChain): MessageReceipt<out Contact>

    /**
     * 上传一个图片以备发送.
     *
     * @see BeforeImageUploadEvent 图片发送前事件, cancellable
     * @see ImageUploadEvent 图片发送完成事件
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时. (最大大小约为 20 MB)
     */
    @JvmName("uploadImageSuspend")
    @JvmSynthetic
    abstract suspend fun uploadImage(image: ExternalImage): OfflineImage

    /**
     * 判断 `this` 和 [other] 是否是相同的类型, 并且 [id] 相同.
     *
     * 注:
     * [id] 相同的 [Member] 和 [QQ], 他们并不 [equals].
     * 因为, [Member] 含义为群员, 必属于一个群.
     * 而 [QQ] 含义为一个独立的人, 可以是好友, 也可以是陌生人.
     */
    abstract override fun equals(other: Any?): Boolean

    /**
     * @return `bot.hashCode() * 31 + id.hashCode()`
     */
    abstract override fun hashCode(): Int

    /**
     * @return "QQ($id)" or "Group($id)" or "Member($id)"
     */
    abstract override fun toString(): String
}

/**
 * @see Bot.recall
 */
@MiraiExperimentalAPI
suspend inline fun Contact.recall(source: MessageChain) = this.bot.recall(source)

/**
 * @see Bot.recall
 */
suspend inline fun Contact.recall(source: MessageSource) = this.bot.recall(source)

/**
 * @see Bot.recallIn
 */
@MiraiExperimentalAPI
inline fun Contact.recallIn(
    message: MessageChain,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.bot.recallIn(message, millis, coroutineContext)

/**
 * @see Bot.recallIn
 */
inline fun Contact.recallIn(
    source: MessageSource,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.bot.recallIn(source, millis, coroutineContext)

/**
 * @see Contact.sendMessage
 */
@Suppress("UNCHECKED_CAST")
suspend inline fun <C : Contact> C.sendMessage(plain: String): MessageReceipt<C> =
    sendMessage(plain.toMessage()) as? MessageReceipt<C> ?: error("Internal class cast mistake")