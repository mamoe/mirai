/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.coroutines.resume
import kotlin.jvm.JvmSynthetic
import kotlin.reflect.KClass


/**
 * 挂起当前协程, 直到监听到事件 [E] 的广播, 返回这个事件实例.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制.
 *
 * @see subscribe 普通地监听一个事件
 * @see syncFromEvent 挂起当前协程, 并尝试从事件中同步一个值
 *
 * @throws TimeoutCancellationException 在超时后抛出.
 */
@JvmSynthetic
suspend inline fun <reified E : Event> nextEvent(
    timeoutMillis: Long = -1,
    priority: Listener.EventPriority = EventPriority.MONITOR
): E {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return withTimeoutOrCoroutineScope(timeoutMillis) {
        nextEventImpl(E::class, this, priority)
    }
}


/**
 * 挂起当前协程, 直到监听到事件 [E] 的广播, 返回这个事件实例.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制.
 *
 * @see subscribe 普通地监听一个事件
 * @see syncFromEvent 挂起当前协程, 并尝试从事件中同步一个值
 *
 * @return 事件实例, 在超时后返回 `null`
 */
@JvmSynthetic
suspend inline fun <reified E : Event> nextEventOrNull(
    timeoutMillis: Long,
    priority: Listener.EventPriority = EventPriority.MONITOR
): E? {
    return withTimeoutOrNull(timeoutMillis) {
        nextEventImpl(E::class, this, priority)
    }
}

//
//
// 以下为已弃用的函数
//
//
//


@PlannedRemoval("1.3.0")
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Deprecated for better Coroutine life cycle management. Please filter bot instance on your own.",
    level = DeprecationLevel.HIDDEN
)
@JvmSynthetic
suspend inline fun <reified E : BotEvent> Bot.nextEvent(
    timeoutMillis: Long = -1,
    priority: Listener.EventPriority = EventPriority.MONITOR
): E {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0" }
    return withTimeoutOrCoroutineScope(timeoutMillis) {
        nextBotEventImpl(this@nextEvent, E::class, this, priority)
    }
}

@JvmSynthetic
@PublishedApi
internal suspend inline fun <E : Event> nextEventImpl(
    eventClass: KClass<E>,
    coroutineScope: CoroutineScope,
    priority: Listener.EventPriority
): E = suspendCancellableCoroutine { cont ->
    coroutineScope.subscribe(eventClass, priority = priority) {
        try {
            cont.resume(this)
        } catch (e: Exception) {
        }
        return@subscribe ListeningStatus.STOPPED
    }
}

@JvmSynthetic
@PublishedApi
internal suspend inline fun <E : BotEvent> nextBotEventImpl(
    bot: Bot,
    eventClass: KClass<E>,
    coroutineScope: CoroutineScope,
    priority: Listener.EventPriority
): E = suspendCancellableCoroutine { cont ->
    coroutineScope.subscribe(eventClass, priority = priority) {
        try {
            if (this.bot == bot) cont.resume(this)
        } catch (e: Exception) {
        }
        return@subscribe ListeningStatus.STOPPED
    }
}

@JvmSynthetic
@PublishedApi
internal suspend inline fun <R> withTimeoutOrCoroutineScope(
    timeoutMillis: Long,
    noinline block: suspend CoroutineScope.() -> R
): R {
    require(timeoutMillis == -1L || timeoutMillis > 0) { "timeoutMillis must be -1 or > 0 " }

    return if (timeoutMillis == -1L) {
        coroutineScope(block)
    } else {
        withTimeout(timeoutMillis, block)
    }
}