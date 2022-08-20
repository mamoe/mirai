/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageEventKt")
@file:Suppress("unused")

package net.mamoe.mirai.message

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.syncFromEvent
import net.mamoe.mirai.message.data.MessageChain
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 判断两个 [MessageEvent] 的 [MessageEvent.sender] 和 [MessageEvent.subject] 是否相同
 */
public fun MessageEvent.isContextIdenticalWith(another: MessageEvent): Boolean {
    return this.sender == another.sender && this.subject == another.subject
}


/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessageEvent]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see syncFromEvent 实现原理
 */
@JvmSynthetic
public suspend inline fun <reified P : MessageEvent> P.nextMessage(
    timeoutMillis: Long = -1,
    priority: EventPriority = EventPriority.MONITOR,
    noinline filter: suspend P.(P) -> Boolean = { true }
): MessageChain = nextMessage(timeoutMillis, priority, false, filter)

/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessageEvent] 并拦截该事件
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param intercept 是否拦截, 传入 `true` 时表示拦截此事件不让接下来的监听器处理, 返回 `false` 时表示让接下来的监听器处理
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see syncFromEvent 实现原理
 * @see MessageEvent.intercept 拦截事件
 *
 * @since 2.13
 */
@JvmSynthetic
public suspend inline fun <reified P : MessageEvent> P.nextMessage(
    timeoutMillis: Long = -1,
    priority: EventPriority = EventPriority.HIGH,
    intercept: Boolean = false,
    noinline filter: suspend P.(P) -> Boolean = { true }
): MessageChain {
    val mapper: suspend (P) -> P? = createMapper(filter)

    return (if (timeoutMillis == -1L) {
        GlobalEventChannel.syncFromEvent(priority, mapper)
    } else {
        withTimeout(timeoutMillis) {
            GlobalEventChannel.syncFromEvent(priority, mapper)
        }
    }).apply { if (intercept) intercept() }.message
}

/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessageEvent]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒.
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 * @return 消息链. 超时时返回 `null`
 *
 * @see syncFromEvent
 */
@JvmSynthetic
public suspend inline fun <reified P : MessageEvent> P.nextMessageOrNull(
    timeoutMillis: Long,
    priority: EventPriority = EventPriority.MONITOR,
    noinline filter: suspend P.(P) -> Boolean = { true }
): MessageChain? {
    require(timeoutMillis > 0) { "timeoutMillis must be > 0" }

    val mapper: suspend (P) -> P? = createMapper(filter)

    return (if (timeoutMillis == -1L) {
        GlobalEventChannel.syncFromEvent(priority, mapper)
    } else {
        withTimeoutOrNull(timeoutMillis) {
            GlobalEventChannel.syncFromEvent(priority, mapper)
        }
    })?.message
}

/**
 * @since 2.10
 */
@PublishedApi // inline, safe to remove in the future
internal inline fun <reified P : MessageEvent> P.createMapper(crossinline filter: suspend P.(P) -> Boolean): suspend (P) -> P? =
    mapper@{ event ->
        if (!event.isContextIdenticalWith(this)) return@mapper null
        if (!filter(event, event)) return@mapper null
        event
    }

/**
 * @see nextMessage
 */
@JvmSynthetic
public inline fun <reified P : MessageEvent> P.nextMessageAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: EventPriority = EventPriority.MONITOR,
    noinline filter: suspend P.(P) -> Boolean = { true }
): Deferred<MessageChain> {
    return this.bot.async(coroutineContext) {
        nextMessage(timeoutMillis, priority, filter)
    }
}

/**
 * [nextMessageOrNull] 的异步版本
 *
 * @see nextMessageOrNull
 */
@JvmSynthetic
public inline fun <reified P : MessageEvent> P.nextMessageOrNullAsync(
    timeoutMillis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: EventPriority = EventPriority.MONITOR,
    noinline filter: suspend P.(P) -> Boolean = { true }
): Deferred<MessageChain?> {
    require(timeoutMillis > 0) { "timeoutMillis must be > 0" }
    return this.bot.async(coroutineContext) {
        nextMessageOrNull(timeoutMillis, priority, filter)
    }
}