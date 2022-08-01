/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:JvmName("EventChannelKotlinExtensions")

package net.mamoe.mirai.event

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic


/**
 * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
 *
 * ```
 * suspend fun GroupMessageEvent.onMessage(event: GroupMessageEvent): ListeningStatus {
 *     return ListeningStatus.LISTENING
 * }
 *
 * eventChannel.subscribe(GroupMessageEvent::onMessage)
 * ```
 * @see EventChannel.subscribe
 */
@JvmSynthetic
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribe(
    crossinline handler: suspend E.(E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

/**
 * 支持 Kotlin 挂起函数的函数引用的监听方式.
 *
 * ```
 * suspend fun onMessage(event: GroupMessageEvent): ListeningStatus {
 *     return ListeningStatus.LISTENING
 * }
 *
 * eventChannel.subscribe(::onMessage)
 * ```
 * @see EventChannel.subscribe
 */
@JvmSynthetic
@JvmName("subscribe1")
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribe(
    crossinline handler: suspend (E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

/**
 * 支持 Kotlin 带接收者的函数的函数引用的监听方式.
 *
 * ```
 * fun GroupMessageEvent.onMessage(event: GroupMessageEvent): ListeningStatus {
 *     return ListeningStatus.LISTENING
 * }
 *
 * eventChannel.subscribe(GroupMessageEvent::onMessage)
 * ```
 * @see EventChannel.subscribe
 */
@JvmSynthetic
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribe(
    crossinline handler: E.(E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

/**
 * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
 *
 * ```
 * fun onMessage(event: GroupMessageEvent): ListeningStatus {
 *     return ListeningStatus.LISTENING
 * }
 *
 * eventChannel.subscribe(::onMessage)
 * ```
 * @see EventChannel.subscribe
 */
@JvmSynthetic
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribe(
    crossinline handler: (E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

/**
 * 支持 Kotlin 挂起函数的函数引用的监听方式.
 * ```
 * suspend fun onMessage(event: GroupMessageEvent) {
 *
 * }
 * eventChannel.subscribeAlways(::onMessage)
 * ```
 * @see EventChannel.subscribeAlways
 */
@JvmName("subscribeAlways1")
@JvmSynthetic
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribeAlways(
    crossinline handler: suspend (E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }


/**
 * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
 * ```
 * suspend fun GroupMessageEvent.onMessage(event: GroupMessageEvent) {
 *
 * }
 * eventChannel.subscribeAlways(GroupMessageEvent::onMessage)
 * ```
 * @see EventChannel.subscribeAlways
 */
@JvmSynthetic
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribeAlways(
    crossinline handler: suspend E.(E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

/**
 * 支持 Kotlin 带接收者的函数的函数引用的监听方式.
 * ```
 * fun GroupMessageEvent.onMessage(event: GroupMessageEvent) {
 *
 * }
 * eventChannel.subscribeAlways(GroupMessageEvent::onMessage)
 * ```
 * @see EventChannel.subscribeAlways
 */
@JvmSynthetic
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribeAlways(
    crossinline handler: E.(E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

/**
 * 支持 Kotlin 带接收者的挂起函数的函数引用的监听方式.
 * ```
 * fun onMessage(event: GroupMessageEvent) {
 *
 * }
 * eventChannel.subscribeAlways(::onMessage)
 * ```
 * @see EventChannel.subscribeAlways
 */
@JvmSynthetic
public inline fun <BaseEvent : Event, reified E : Event> EventChannel<BaseEvent>.subscribeAlways(
    crossinline handler: (E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }