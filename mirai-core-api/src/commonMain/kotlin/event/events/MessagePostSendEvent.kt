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
import net.mamoe.mirai.internal.event.VerboseEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiInternalApi
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 在发送消息后广播的事件, 总是在 [MessagePreSendEvent] 之后广播.
 *
 * 只要 [MessagePreSendEvent] 未被 [取消][CancellableEvent.cancel], [MessagePostSendEvent] 就一定会被广播, 并携带 [发送时产生的异常][MessagePostSendEvent.exception] (如果有).
 *
 * 在此事件广播前, 消息一定已经发送成功, 或产生一个异常.
 *
 * @see Contact.sendMessage 发送消息. 为广播这个事件的唯一途径
 * @see MessagePreSendEvent
 */
@OptIn(MiraiInternalApi::class)
public sealed class MessagePostSendEvent<C : Contact> : BotEvent, BotActiveEvent, AbstractEvent(), VerboseEvent {
    /** 发信目标. */
    public abstract val target: C
    public final override val bot: Bot get() = target.bot

    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public abstract val message: MessageChain

    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public abstract val exception: Throwable?

    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public abstract val receipt: MessageReceipt<C>?
}

/**
 * 获取指代这条已经发送的消息的 [MessageSource]. 若消息发送失败, 返回 `null`
 * @see MessagePostSendEvent.sourceResult
 */
@get:JvmSynthetic
public inline val MessagePostSendEvent<*>.source: MessageSource?
    get() = receipt?.source

/**
 * 获取指代这条已经发送的消息的 [MessageSource], 并包装为 [kotlin.Result]
 * @see MessagePostSendEvent.result
 */
@get:JvmSynthetic
@Suppress("RESULT_CLASS_IN_RETURN_TYPE")
public inline val MessagePostSendEvent<*>.sourceResult: Result<MessageSource>
    get() = result.map { it.source }

/**
 * 在此消息发送成功时返回 `true`.
 * @see MessagePostSendEvent.exception
 * @see MessagePostSendEvent.result
 */
@get:JvmSynthetic
public inline val MessagePostSendEvent<*>.isSuccess: Boolean
    get() = exception == null

/**
 * 在此消息发送失败时返回 `true`.
 * @see MessagePostSendEvent.exception
 * @see MessagePostSendEvent.result
 */
@get:JvmSynthetic
public inline val MessagePostSendEvent<*>.isFailure: Boolean
    get() = exception != null

/**
 * 将 [MessagePostSendEvent.exception] 与 [MessagePostSendEvent.receipt] 表示为 [Result]
 */
@Suppress("RESULT_CLASS_IN_RETURN_TYPE")
public inline val <C : Contact> MessagePostSendEvent<C>.result: Result<MessageReceipt<C>>
    get() = exception.let { exception -> if (exception != null) Result.failure(exception) else Result.success(receipt!!) }

/**
 * 在群消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public data class GroupMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Group,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Group>?
) : MessagePostSendEvent<Group>()

/**
 * 在好友或群临时会话消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public sealed class UserMessagePostSendEvent<C : User> : MessagePostSendEvent<C>()

/**
 * 在好友消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public data class FriendMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Friend,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Friend>?
) : UserMessagePostSendEvent<Friend>()

/**
 * 在群临时会话消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
@Deprecated(
    "mirai 正计划支持其他渠道发起的临时会话, 届时此事件会变动. 原 TempMessagePostSendEvent 已更改为 GroupTempMessagePostSendEvent",
    replaceWith = ReplaceWith(
        "GroupTempMessagePostSendEvent",
        "net.mamoe.mirai.event.events.GroupTempMessagePostSendEvent"
    ),
    DeprecationLevel.HIDDEN
)
@DeprecatedSinceMirai(hiddenSince = "2.0") // maybe 2.0
public sealed class TempMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Member,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Member>?
) : UserMessagePostSendEvent<Member>() {
    public open val group: Group get() = target.group
}

/**
 * 在群临时会话消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
@OptIn(MiraiInternalApi::class)
public data class GroupTempMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: NormalMember,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<NormalMember>?
) : @kotlin.Suppress("DEPRECATION_ERROR") TempMessagePostSendEvent(target, message, exception, receipt) {
    public override val group: Group get() = target.group
}

/**
 * 在陌生人消息发送后广播的事件.
 * @see MessagePostSendEvent
 */
public data class StrangerMessagePostSendEvent @MiraiInternalApi constructor(
    /** 发信目标. */
    public override val target: Stranger,
    /** 待发送的消息. 此为 [MessagePreSendEvent.message] 的最终值. */
    public override val message: MessageChain,
    /**
     * 发送消息时抛出的异常. `null` 表示消息成功发送.
     * @see result
     */
    public override val exception: Throwable?,
    /**
     * 发送消息成功时的回执. `null` 表示消息发送失败.
     * @see result
     */
    public override val receipt: MessageReceipt<Stranger>?
) : UserMessagePostSendEvent<Stranger>()
