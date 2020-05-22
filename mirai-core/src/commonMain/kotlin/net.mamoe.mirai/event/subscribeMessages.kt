/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("SubscribeMessagesKt")
@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.TempMessageEvent
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

typealias MessagePacketSubscribersBuilder = MessageSubscribersBuilder<MessageEvent, Listener<MessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 *
 * @see subscribe 事件监听基础
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */

fun <R> CoroutineScope.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: MessagePacketSubscribersBuilder.() -> R
): R {
    // contract 可帮助 IDE 进行类型推断. 无实际代码作用.
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }

    return MessagePacketSubscribersBuilder(Unit)
    { filter, messageListener: MessageListener<MessageEvent, Unit> ->
        // subscribeAlways 即注册一个监听器. 这个监听器收到消息后就传递给 [messageListener]
        // messageListener 即为 DSL 里 `contains(...) { }`, `startsWith(...) { }` 的代码块.
        subscribeAlways(coroutineContext, concurrencyKind, priority) {
            // this.message.contentToString() 即为 messageListener 中 it 接收到的值
            val toString = this.message.contentToString()
            if (filter.invoke(this, toString))
                messageListener.invoke(this, toString)
        }
    }.run(listeners)
}

typealias GroupMessageSubscribersBuilder = MessageSubscribersBuilder<GroupMessageEvent, Listener<GroupMessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有群消息事件
 *
 * @see subscribe 事件监听基础
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
fun <R> CoroutineScope.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return GroupMessageSubscribersBuilder(Unit) { filter, listener ->
        subscribeAlways(coroutineContext, concurrencyKind, priority) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

typealias FriendMessageSubscribersBuilder = MessageSubscribersBuilder<FriendMessageEvent, Listener<FriendMessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有好友消息事件
 *
 * @see subscribe 事件监听基础
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
fun <R> CoroutineScope.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return FriendMessageSubscribersBuilder(Unit) { filter, listener ->
        subscribeAlways(coroutineContext, concurrencyKind, priority) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

typealias TempMessageSubscribersBuilder = MessageSubscribersBuilder<TempMessageEvent, Listener<TempMessageEvent>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有临时会话消息事件
 *
 * @see subscribe 事件监听基础
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
fun <R> CoroutineScope.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: TempMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return TempMessageSubscribersBuilder(Unit) { filter, listener ->
        subscribeAlways(coroutineContext, concurrencyKind, priority) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}


/**
 * 打开一个指定事件的接收通道
 *
 * @param capacity 同 [Channel] 的参数, 参见 [Channel.Factory] 中的常量.
 *
 * @see capacity 默认无限大小. 详见 [Channel.Factory] 中的常量 [Channel.UNLIMITED], [Channel.CONFLATED], [Channel.RENDEZVOUS].
 * 请谨慎使用 [Channel.RENDEZVOUS]: 在 [Channel] 未被 [接收][Channel.receive] 时他将会阻塞事件处理
 *
 * @see subscribe 事件监听基础
 *
 * @see subscribeFriendMessages
 * @see subscribeMessages
 * @see subscribeGroupMessages
 */
inline fun <reified E : Event> CoroutineScope.incoming(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    capacity: Int = Channel.UNLIMITED
): ReceiveChannel<E> {
    return Channel<E>(capacity).apply {
        val listener = this@incoming.subscribeAlways<E>(coroutineContext, concurrencyKind, priority) {
            send(this)
        }
        this.invokeOnClose {
            listener.cancel(CancellationException("ReceiveChannel closed", it))
        }
    }
}

