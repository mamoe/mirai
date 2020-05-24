/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:JvmName("SubscriberKt")
@file:JvmMultifileClass

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Listener.ConcurrencyKind.CONCURRENT
import net.mamoe.mirai.event.Listener.ConcurrencyKind.LOCKED
import net.mamoe.mirai.event.Listener.EventPriority.MONITOR
import net.mamoe.mirai.event.Listener.EventPriority.NORMAL
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.internal.Handler
import net.mamoe.mirai.event.internal.subscribeInternal
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.reflect.KClass


//
// 以下为带筛选 Bot 的监听 (已启用)
//


@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
inline fun <reified E : BotEvent> Bot.subscribe(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = this.subscribe(E::class, coroutineContext, concurrency, priority, handler)

@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <E : BotEvent> Bot.subscribe(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    priority: Listener.EventPriority = NORMAL,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> = eventClass.subscribeInternal(
    Handler(
        coroutineContext,
        concurrency,
        priority
    ) { if (it.bot === this) it.handler(it) else ListeningStatus.LISTENING }
)

@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot1")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
inline fun <reified E : BotEvent> Bot.subscribeAlways(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority, listener)

@PlannedRemoval("1.3.0")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <E : BotEvent> Bot.subscribeAlways(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    priority: Listener.EventPriority = NORMAL,
    listener: suspend E.(E) -> Unit
): Listener<E> = eventClass.subscribeInternal(
    Handler(coroutineContext, concurrency, priority) { if (it.bot === this) it.listener(it); ListeningStatus.LISTENING }
)

@Suppress("DeprecatedCallableAddReplaceWith")
@JvmSynthetic
@JvmName("subscribeOnceForBot2")
@PlannedRemoval("1.3.0")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
inline fun <reified E : BotEvent> Bot.subscribeOnce(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(E::class, coroutineContext, priority, listener)

@PlannedRemoval("1.3.0")
@kotlin.internal.LowPriorityInOverloadResolution
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
fun <E : BotEvent> Bot.subscribeOnce(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: Listener.EventPriority = NORMAL,
    listener: suspend E.(E) -> Unit
): Listener<E> =
    eventClass.subscribeInternal(Handler(coroutineContext, LOCKED, priority) {
        if (it.bot === this) {
            it.listener(it)
            ListeningStatus.STOPPED
        } else ListeningStatus.LISTENING
    })

// endregion


// region 为了兼容旧版本的方法

@PlannedRemoval("1.2.0")
@JvmName("subscribe")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : Event> CoroutineScope.subscribeDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = handler
)

@PlannedRemoval("1.2.0")
@JvmName("subscribe")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : Event> CoroutineScope.subscribeDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = handler
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeAlways")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : Event> CoroutineScope.subscribeAlwaysDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = listener
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeAlways")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : Event> CoroutineScope.subscribeAlwaysDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = listener
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeOnce")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : Event> CoroutineScope.subscribeOnceDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    coroutineContext = coroutineContext,
    priority = MONITOR,
    handler = listener
)

@PlannedRemoval("1.2.0")
@JvmName("subscribeOnce")
@JvmSynthetic
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : Event> CoroutineScope.subscribeOnceDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    priority = MONITOR,
    handler = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : BotEvent> Bot.subscribeDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = this.subscribe(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = handler
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribe")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : BotEvent> Bot.subscribeDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = LOCKED,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> = subscribe(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = handler
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeAlwaysForBot1")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : BotEvent> Bot.subscribeAlwaysDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeAlways")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : BotEvent> Bot.subscribeAlwaysDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: Listener.ConcurrencyKind = CONCURRENT,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeAlways(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    concurrency = concurrency,
    priority = MONITOR,
    handler = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeOnceForBot2")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
inline fun <reified E : BotEvent> Bot.subscribeOnceDeprecated(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    noinline listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    coroutineContext = coroutineContext,
    priority = MONITOR,
    handler = listener
)

@PlannedRemoval("1.2.0")
@JvmSynthetic
@JvmName("subscribeOnce")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("unused")
fun <E : BotEvent> Bot.subscribeOnceDeprecated(
    eventClass: KClass<E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    listener: suspend E.(E) -> Unit
): Listener<E> = subscribeOnce(
    eventClass = eventClass,
    coroutineContext = coroutineContext,
    priority = MONITOR,
    handler = listener
)
// endregion