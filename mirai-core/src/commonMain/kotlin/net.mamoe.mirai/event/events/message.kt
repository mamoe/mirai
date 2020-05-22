/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")
@file:Suppress("unused")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import net.mamoe.mirai.event.events.ImageUploadEvent.Failed
import net.mamoe.mirai.event.events.ImageUploadEvent.Succeed
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.utils.ExternalImage
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


/**
 * 主动发送消息
 */
sealed class MessageSendEvent : BotEvent, BotActiveEvent, AbstractEvent() {
    abstract val target: Contact
    final override val bot: Bot
        get() = target.bot

    data class GroupMessageSendEvent(
        override val target: Group,
        var message: MessageChain
    ) : MessageSendEvent(), CancellableEvent

    data class FriendMessageSendEvent(
        override val target: Friend,
        var message: MessageChain
    ) : MessageSendEvent(), CancellableEvent

    // TODO: 2020/4/30 添加临时会话消息发送事件
}

/**
 * 消息撤回事件. 可是任意消息被任意人撤回.
 */
sealed class MessageRecallEvent : BotEvent, AbstractEvent() {
    /**
     * 消息原发送人
     */
    abstract val authorId: Long

    /**
     * 消息 id.
     * @see MessageSource.id
     */
    abstract val messageId: Int

    /**
     * 消息内部 id.
     * @see MessageSource.id
     */
    abstract val messageInternalId: Int

    /**
     * 原发送时间
     */
    abstract val messageTime: Int // seconds

    /**
     * 好友消息撤回事件, 暂不支持.
     */ // TODO: 2020/4/22 支持好友消息撤回事件的解析和主动广播
    data class FriendRecall(
        override val bot: Bot,
        override val messageId: Int,
        override val messageInternalId: Int,
        override val messageTime: Int,
        /**
         * 撤回操作人, 可能为 [Bot.id] 或好友的 [User.id]
         */
        val operator: Long
    ) : MessageRecallEvent(), Packet {
        override val authorId: Long
            get() = bot.id
    }

    /**
     * 群消息撤回事件.
     */
    data class GroupRecall(
        override val bot: Bot,
        override val authorId: Long,
        override val messageId: Int,
        override val messageInternalId: Int,
        override val messageTime: Int,
        /**
         * 操作人. 为 null 时则为 [Bot] 操作.
         */
        override val operator: Member?,
        override val group: Group
    ) : MessageRecallEvent(), GroupOperableEvent, Packet
}

val MessageRecallEvent.GroupRecall.author: Member
    get() = if (authorId == bot.id) group.botAsMember else group[authorId]

val MessageRecallEvent.FriendRecall.isByBot: Boolean get() = this.operator == bot.id
// val MessageRecallEvent.GroupRecall.isByBot: Boolean get() = (this as GroupOperableEvent).isByBot
// no need

val MessageRecallEvent.isByBot: Boolean
    get() = when (this) {
        is MessageRecallEvent.FriendRecall -> this.isByBot
        is MessageRecallEvent.GroupRecall -> (this as GroupOperableEvent).isByBot
    }


/**
 * 图片上传前. 可以阻止上传
 */
data class BeforeImageUploadEvent(
    val target: Contact,
    val source: ExternalImage
) : BotEvent, BotActiveEvent, AbstractEvent(), CancellableEvent {
    override val bot: Bot
        get() = target.bot
}

/**
 * 图片上传完成
 *
 * @see Succeed
 * @see Failed
 */
sealed class ImageUploadEvent : BotEvent, BotActiveEvent, AbstractEvent() {
    abstract val target: Contact
    abstract val source: ExternalImage
    override val bot: Bot
        get() = target.bot

    data class Succeed(
        override val target: Contact,
        override val source: ExternalImage,
        val image: Image
    ) : ImageUploadEvent()

    data class Failed(
        override val target: Contact,
        override val source: ExternalImage,
        val errno: Int,
        val message: String
    ) : ImageUploadEvent()
}

