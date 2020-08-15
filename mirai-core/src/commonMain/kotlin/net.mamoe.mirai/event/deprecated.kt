/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("SubscribeMessagesKt")
@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "MemberVisibilityCanBePrivate",
    "unused",
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE"
)

package net.mamoe.mirai.event

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


//
//
////
//// 此文件存放所有 `net.mamoe.mirai.event.subscribeMessages` 已弃用的函数.
////
//
//
//
//
//
//
//


@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
@kotlin.internal.LowPriorityInOverloadResolution
fun <R> Bot.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: MessagePacketSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return MessagePacketSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind, priority) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

@PlannedRemoval("1.3.0")
@kotlin.internal.LowPriorityInOverloadResolution
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <R> Bot.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return GroupMessageSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind, priority) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

@kotlin.internal.LowPriorityInOverloadResolution
@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <R> Bot.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return FriendMessageSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind, priority) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}


@kotlin.internal.LowPriorityInOverloadResolution
@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <R> Bot.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    priority: Listener.EventPriority = EventPriority.MONITOR,
    listeners: TempMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return TempMessageSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind, priority) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

@kotlin.internal.LowPriorityInOverloadResolution
@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
inline fun <reified E : BotEvent> Bot.incoming(
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

@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@PlannedRemoval("1.2.0")
fun <R> CoroutineScope.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: MessagePacketSubscribersBuilder.() -> R
): R = this.subscribeMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)

@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@kotlin.internal.LowPriorityInOverloadResolution
@PlannedRemoval("1.2.0")
fun <R> CoroutineScope.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R = this.subscribeGroupMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)

@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@PlannedRemoval("1.2.0")
fun <R> CoroutineScope.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R = this.subscribeFriendMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)

@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@PlannedRemoval("1.2.0")
fun <R> CoroutineScope.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: TempMessageSubscribersBuilder.() -> R
): R = this.subscribeTempMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)

@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@PlannedRemoval("1.2.0")
fun <R> Bot.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: MessagePacketSubscribersBuilder.() -> R
): R = this.subscribeMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)

@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@PlannedRemoval("1.2.0")
fun <R> Bot.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R = this.subscribeGroupMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)

@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@PlannedRemoval("1.2.0")
fun <R> Bot.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R = this.subscribeFriendMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)

@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@PlannedRemoval("1.2.0")
fun <R> Bot.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: TempMessageSubscribersBuilder.() -> R
): R = this.subscribeTempMessages(coroutineContext, concurrencyKind, EventPriority.MONITOR, listeners)