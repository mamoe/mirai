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

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 在发送消息前广播的事件. 可被 [取消][CancellableEvent.cancel].
 *
 * 此事件总是在 [MessagePostSendEvent] 之前广播.
 *
 * 当 [MessagePreSendEvent] 被 [取消][CancellableEvent.cancel] 后:
 * - [MessagePostSendEvent] 不会广播
 * - 消息不会发送.
 * - [Contact.sendMessage] 会抛出异常 [EventCancelledException]
 *
 * @see Contact.sendMessage 发送消息. 为广播这个事件的唯一途径
 */
public sealed class MessagePreSendEvent : BotEvent, BotActiveEvent, AbstractEvent(), CancellableEvent {
    /** 发信目标. */
    public abstract val target: Contact
    public final override val bot: Bot get() = target.bot

    /** 待发送的消息. 修改后将会同时应用于发送. */
    public abstract var message: Message
}

/**
 * 在发送群消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class GroupMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Group,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : MessagePreSendEvent()

/**
 * 在发送好友或群临时会话消息前广播的事件.
 * @see MessagePreSendEvent
 */
public sealed class UserMessagePreSendEvent : MessagePreSendEvent() {
    /** 发信目标. */
    public abstract override val target: User
}

/**
 * 在发送好友消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class FriendMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Friend,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : UserMessagePreSendEvent()

/**
 * 在发送临时会话消息前广播的事件.
 * @see MessagePreSendEvent
 */
@Deprecated(
    "mirai 正计划支持其他渠道发起的临时会话, 届时此事件会变动. 原 TempMessagePreSendEvent 已更改为 GroupTempMessagePreSendEvent",
    replaceWith = ReplaceWith(
        "GroupTempMessagePreSendEvent",
        "net.mamoe.mirai.event.events.GroupTempMessagePreSendEvent"
    ),
    DeprecationLevel.ERROR
)
public sealed class TempMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Member,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : UserMessagePreSendEvent() {
    public open val group: Group get() = target.group
}

/**
 * 在发送群临时会话消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class GroupTempMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: NormalMember,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : @kotlin.Suppress("DEPRECATION_ERROR") TempMessagePreSendEvent(target, message) {
    public override val group: Group get() = target.group
}

/**
 * 在发送陌生人消息前广播的事件.
 * @see MessagePreSendEvent
 */
public data class StrangerMessagePreSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Stranger,
    /** 待发送的消息. 修改后将会同时应用于发送. */
    public override var message: Message
) : UserMessagePreSendEvent()
