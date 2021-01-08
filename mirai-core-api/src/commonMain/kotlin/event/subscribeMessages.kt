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
import net.mamoe.mirai.event.Listener.ConcurrencyKind.CONCURRENT
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.content
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public typealias MessageEventSubscribersBuilder = MessageSubscribersBuilder<MessageEvent, Listener<MessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 *
 * @see subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: MessageEventSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::MessageEventSubscribersBuilder, coroutineContext, concurrencyKind, priority).run(listeners)
}

public typealias GroupMessageSubscribersBuilder = MessageSubscribersBuilder<GroupMessageEvent, Listener<GroupMessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有群消息事件
 *
 * @see subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::GroupMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority).run(listeners)
}

public typealias FriendMessageSubscribersBuilder = MessageSubscribersBuilder<FriendMessageEvent, Listener<FriendMessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有好友消息事件
 *
 * @see subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::FriendMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority).run(listeners)
}

public typealias TempMessageSubscribersBuilder = MessageSubscribersBuilder<TempMessageEvent, Listener<TempMessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有临时会话消息事件
 *
 * @see subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: TempMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::TempMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority).run(listeners)
}


public typealias StrangerMessageSubscribersBuilder = MessageSubscribersBuilder<StrangerMessageEvent, Listener<StrangerMessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有陌生人消息事件
 *
 * @see subscribe 事件监听基础
 * @see EventChannel 事件通道
 */
public fun <R> EventChannel<*>.subscribeStrangerMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: StrangerMessageSubscribersBuilder.() -> R
): R {
    contract { callsInPlace(listeners, InvocationKind.EXACTLY_ONCE) }
    return createBuilder(::StrangerMessageSubscribersBuilder, coroutineContext, concurrencyKind, priority)
        .run(listeners)
}

private typealias MessageSubscriberBuilderConstructor<E> = (
    Unit,
    (E.(String) -> Boolean, MessageListener<E, Unit>) -> Listener<E>
) -> MessageSubscribersBuilder<E, Listener<E>, Unit, Unit>

private inline fun <reified E : MessageEvent> EventChannel<*>.createBuilder(
    constructor: MessageSubscriberBuilderConstructor<E>,
    coroutineContext: CoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind,
    priority: Listener.EventPriority
): MessageSubscribersBuilder<E, Listener<E>, Unit, Unit> = constructor(Unit) { filter, listener ->
    subscribeAlways(coroutineContext, concurrencyKind, priority) {
        val toString = this.message.content
        if (filter(this, toString))
            listener(this, toString)
    }
}