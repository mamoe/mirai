/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("SubscribeMessagesKt")
@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.OtherClient
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.ConcurrencyKind.CONCURRENT
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.content
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public typealias MessageEventSubscribersBuilder = MessageSubscribersBuilder<MessageEvent, Listener<MessageEvent>, Unit, Unit>

/**
 * 通过 DSL 订阅来自所有 [Bot] 的所有联系人的消息事件.
 *
 * ```
 * eventChannel.subscribeMessages {
 *     "test" {
 *         // 当消息内容为 "test" 时执行
 *         // this: MessageEvent
 *         reply("test!")
 *     }
 *
 *     "Hello" reply "Hi" // 当消息内容为 "Hello" 时回复 "Hi"
 *     "quote me" quoteReply "ok" // 当消息内容为 "quote me" 时引用该消息并回复 "ok"
 *     "quote me2" quoteReply {
 *         // lambda 也是允许的：
 *         // 返回值接受 Any?
 *         // 为 Unit 时不发送任何内容；
 *         // 为 Message 时直接发送；
 *         // 为 String 时发送为 PlainText；
 *         // 否则 toString 并发送为 PlainText
 *
 *         "ok"
 *     }
 *
 *     case("iGNorECase", ignoreCase=true) reply "OK" // 忽略大小写
 *     startsWith("-") reply { cmd ->
 *         // 当消息内容以 "-" 开头时执行
 *         // cmd 为消息去除开头 "-" 的内容
 *     }
 *
 *
 *     val listener: Listener<MessageEvent> = "1" reply "2"
 *     // 每个语句都会被注册为事件监听器，可以这样获取监听器
 *
 *     listener.complete() // 停止 "1" reply "2" 这个事件监听
 * }
 * ```
 *
 * @see EventChannel.subscribe 事件监听基础
 * @see EventChannel 事件通道
 *
 * @see subscribeFriendMessages
 * @see subscribeGroupMessages
 * @see subscribeGroupTempMessages
 * @see subscribeOtherClientMessages
 * @see subscribeStrangerMessages
 */
public fun <R> EventChannel<*>.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: ConcurrencyKind = CONCURRENT,
    priority: EventPriority = EventPriority.MONITOR,
    listeners: MessageEventSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::MessageEventSubscribersBuilder, coroutineContext, concurrencyKind, priority).run(listeners)
}

public typealias GroupMessageSubscribersBuilder = MessageSubscribersBuilder<GroupMessageEvent, Listener<GroupMessageEvent>, Unit, Unit>

/**
 * 通过 DSL 订阅来自所有 [Bot] 的所有群会话消息事件. DSL 语法查看 [subscribeMessages].
 *
 * @see EventChannel.subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: ConcurrencyKind = CONCURRENT,
    priority: EventPriority = EventPriority.MONITOR,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::GroupMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority).run(listeners)
}

public typealias FriendMessageSubscribersBuilder = MessageSubscribersBuilder<FriendMessageEvent, Listener<FriendMessageEvent>, Unit, Unit>

/**
 * 通过 DSL 订阅来自所有 [Bot] 的所有好友消息事件. DSL 语法查看 [subscribeMessages].
 *
 * @see EventChannel.subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: ConcurrencyKind = CONCURRENT,
    priority: EventPriority = EventPriority.MONITOR,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::FriendMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority).run(listeners)
}

@Deprecated(
    "mirai 正计划支持其他渠道发起的临时会话, 届时此定义会变动. 请使用 GroupTempMessageSubscribersBuilder",
    ReplaceWith(
        "GroupTempMessageSubscribersBuilder",
        "net.mamoe.mirai.event.GroupTempMessageSubscribersBuilder"
    ),
    DeprecationLevel.ERROR
)
public typealias TempMessageSubscribersBuilder = MessageSubscribersBuilder<GroupTempMessageEvent, Listener<GroupTempMessageEvent>, Unit, Unit>

/**
 * 通过 DSL 订阅来自所有 [Bot] 的所有临时会话消息事件. DSL 语法查看 [subscribeMessages].
 *
 * @see EventChannel.subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
@Deprecated(
    "mirai 正计划支持其他渠道发起的临时会话, 届时此方法会变动. 请使用 subscribeGroupTempMessages",
    ReplaceWith(
        "subscribeGroupTempMessages(coroutineContext, concurrencyKind, priority, listeners)",
        "net.mamoe.mirai.event.subscribeGroupTempMessages"
    ),
    DeprecationLevel.ERROR
)
public fun <R> EventChannel<*>.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: ConcurrencyKind = CONCURRENT,
    priority: EventPriority = EventPriority.MONITOR,
    listeners: GroupTempMessageSubscribersBuilder.() -> R
): R = subscribeGroupTempMessages(coroutineContext, concurrencyKind, priority, listeners)

public typealias GroupTempMessageSubscribersBuilder = MessageSubscribersBuilder<GroupTempMessageEvent, Listener<GroupTempMessageEvent>, Unit, Unit>

/**
 * 通过 DSL 订阅来自所有 [Bot] 的所有 [GroupTempMessageEvent]. DSL 语法查看 [subscribeMessages].
 *
 * @see EventChannel.subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeGroupTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: ConcurrencyKind = CONCURRENT,
    priority: EventPriority = EventPriority.MONITOR,
    listeners: GroupTempMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::GroupTempMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority)
        .run(listeners)
}


public typealias StrangerMessageSubscribersBuilder = MessageSubscribersBuilder<StrangerMessageEvent, Listener<StrangerMessageEvent>, Unit, Unit>

/**
 * 通过 DSL 订阅来自所有 [Bot] 的所有 [Stranger] 消息事件. DSL 语法查看 [subscribeMessages].
 *
 * @see EventChannel.subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeStrangerMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: ConcurrencyKind = CONCURRENT,
    priority: EventPriority = EventPriority.MONITOR,
    listeners: StrangerMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::StrangerMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority)
        .run(listeners)
}


public typealias OtherClientMessageSubscribersBuilder = MessageSubscribersBuilder<OtherClientMessageEvent, Listener<OtherClientMessageEvent>, Unit, Unit>

/**
 * 通过 DSL 订阅来自所有 [Bot] 的所有 [OtherClient] 消息事件. DSL 语法查看 [subscribeMessages].
 *
 * @see EventChannel.subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeOtherClientMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: ConcurrencyKind = CONCURRENT,
    priority: EventPriority = EventPriority.MONITOR,
    listeners: OtherClientMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::OtherClientMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority)
        .run(listeners)
}

private typealias MessageSubscriberBuilderConstructor<E> = (
    Unit,
    (E.(String) -> Boolean, MessageListener<E, Unit>) -> Listener<E>
) -> MessageSubscribersBuilder<E, Listener<E>, Unit, Unit>

private inline fun <reified E : MessageEvent> EventChannel<*>.createBuilder(
    constructor: MessageSubscriberBuilderConstructor<E>,
    coroutineContext: CoroutineContext,
    concurrencyKind: ConcurrencyKind,
    priority: EventPriority
): MessageSubscribersBuilder<E, Listener<E>, Unit, Unit> = constructor(Unit) { filter, listener ->
    subscribeAlways(coroutineContext, concurrencyKind, priority) {
        val toString = this.message.content
        if (filter(this, toString))
            listener(this, toString)
    }
}