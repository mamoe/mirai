/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageEventKt")
@file:Suppress("unused")

package net.mamoe.mirai.message

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.event.syncFromEvent
import net.mamoe.mirai.event.syncFromEventOrNull
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.anyIsInstance
import net.mamoe.mirai.message.data.firstIsInstance
import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 判断两个 [MessageEvent] 的 [MessageEvent.sender] 和 [MessageEvent.subject] 是否相同
 */
fun MessageEvent.isContextIdenticalWith(another: MessageEvent): Boolean {
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
 * @see syncFromEvent
 */
@JvmSynthetic
suspend inline fun <reified P : MessageEvent> P.nextMessage(
    timeoutMillis: Long = -1,
    crossinline filter: suspend P.(P) -> Boolean
): MessageChain {
    return syncFromEvent<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }?.takeIf { filter(it, it) }
    }.message
}

/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessageEvent]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 * @return 消息链. 超时时返回 `null`
 *
 * @see syncFromEventOrNull
 */
@JvmSynthetic
suspend inline fun <reified P : MessageEvent> P.nextMessageOrNull(
    timeoutMillis: Long = -1,
    crossinline filter: suspend P.(P) -> Boolean
): MessageChain? {
    return syncFromEventOrNull<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageOrNull) }?.takeIf { filter(it, it) }
    }?.message
}

/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同的 [MessageEvent]
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @throws TimeoutCancellationException
 *
 * @see syncFromEvent
 */
@JvmSynthetic
suspend inline fun <reified P : MessageEvent> P.nextMessage(
    timeoutMillis: Long = -1
): MessageChain {
    return syncFromEvent<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }
    }.message
}

/**
 * @see nextMessage
 * @throws TimeoutCancellationException
 */
@JvmSynthetic
inline fun <reified P : MessageEvent> P.nextMessageAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<MessageChain> {
    return this.bot.async(coroutineContext) {
        syncFromEvent<P, P>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageAsync) }
        }.message
    }
}

/**
 * @see nextMessage
 */
@JvmSynthetic
inline fun <reified P : MessageEvent> P.nextMessageAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline filter: suspend P.(P) -> Boolean
): Deferred<MessageChain> {
    return this.bot.async(coroutineContext) {
        syncFromEvent<P, P>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageAsync) }
                .takeIf { filter(this, this) }
        }.message
    }
}

/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同的 [MessageEvent]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @return 消息链. 超时时返回 `null`
 *
 * @see syncFromEventOrNull
 */
@JvmSynthetic
suspend inline fun <reified P : MessageEvent> P.nextMessageOrNull(
    timeoutMillis: Long = -1
): MessageChain? {
    return syncFromEventOrNull<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageOrNull) }
    }?.message
}

/**
 * @see nextMessageOrNull
 */
@JvmSynthetic
inline fun <reified P : MessageEvent> P.nextMessageOrNullAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<MessageChain?> {
    return this.bot.async(coroutineContext) {
        syncFromEventOrNull<P, P>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageOrNullAsync) }
        }?.message
    }
}

/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同的 [MessageEvent]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @see syncFromEvent
 * @see whileSelectMessages
 * @see selectMessages
 */
@JvmSynthetic
suspend inline fun <reified M : Message> MessageEvent.nextMessageContaining(
    timeoutMillis: Long = -1
): M {
    return syncFromEvent<MessageEvent, MessageEvent>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageContaining) }
            .takeIf { this.message.anyIsInstance<M>() }
    }.message.firstIsInstance()
}

@JvmSynthetic
inline fun <reified M : Message> MessageEvent.nextMessageContainingAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<M> {
    return this.bot.async(coroutineContext) {
        @Suppress("RemoveExplicitTypeArguments")
        syncFromEvent<MessageEvent, MessageEvent>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageContainingAsync) }
                .takeIf { this.message.anyIsInstance<M>() }
        }.message.firstIsInstance<M>()
    }
}

/**
 * 挂起当前协程, 等待下一条 [MessageEvent.sender] 和 [MessageEvent.subject] 与 [this] 相同并含有 [M] 类型的消息的 [MessageEvent]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @return 指定类型的消息. 超时时返回 `null`
 *
 * @see syncFromEventOrNull
 */
@JvmSynthetic
suspend inline fun <reified M : Message> MessageEvent.nextMessageContainingOrNull(
    timeoutMillis: Long = -1
): M? {
    return syncFromEventOrNull<MessageEvent, MessageEvent>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageContainingOrNull) }
            .takeIf { this.message.anyIsInstance<M>() }
    }?.message?.firstIsInstance()
}

@JvmSynthetic
inline fun <reified M : Message> MessageEvent.nextMessageContainingOrNullAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<M?> {
    return this.bot.async(coroutineContext) {
        syncFromEventOrNull<MessageEvent, MessageEvent>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageContainingOrNullAsync) }
                .takeIf { this.message.anyIsInstance<M>() }
        }?.message?.firstIsInstance<M>()
    }
}


@PlannedRemoval("1.2.0")
@Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
@Suppress("DEPRECATION_ERROR")
@JvmSynthetic
fun ContactMessage.isContextIdenticalWith(another: ContactMessage): Boolean {
    return this.sender == another.sender && this.subject == another.subject && this.bot == another.bot
}