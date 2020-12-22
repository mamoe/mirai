/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:JvmName("SubscriberKt")
@file:JvmMultifileClass

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.event.Listener.ConcurrencyKind
import net.mamoe.mirai.event.Listener.EventPriority
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass


// region subscribe / subscribeAlways / subscribeOnce

private const val COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE = """
    CoroutineScope.subscribe 已被弃用. 
    CoroutineScope.subscribe 设计为在指定协程作用域下创建事件监听器, 监听所有事件 E. 
    但由于 Bot 也实现接口 CoroutineScope, 就可以调用 Bot.subscribe<MessageEvent>, 
    直观语义上应该是监听来自 Bot 的事件, 但实际是监听来自所有 Bot 的事件.
    
    请以 Bot.eventChannel 或 GlobalEventChannel 替代. 可在 EventChannel 获取更详细的帮助.
"""

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribe(coroutineContext, concurrency, priority, handler)",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
public inline fun <reified E : Event> CoroutineScope.subscribe(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: ConcurrencyKind = ConcurrencyKind.LOCKED,
    priority: EventPriority = EventPriority.NORMAL,
    noinline handler: suspend E.(E) -> ListeningStatus
): Listener<E> = this.globalEventChannel().subscribe(coroutineContext, concurrency, priority, handler)

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribe(eventClass, coroutineContext, concurrency, priority, handler)",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
public fun <E : Event> CoroutineScope.subscribe(
    eventClass: KClass<out E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: ConcurrencyKind = ConcurrencyKind.LOCKED,
    priority: EventPriority = EventPriority.NORMAL,
    handler: suspend E.(E) -> ListeningStatus
): Listener<E> =
    this.globalEventChannel().subscribe(eventClass, coroutineContext, concurrency, priority, handler)

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribeAlways(E::class, coroutineContext, concurrency, priority, handler)",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    priority: EventPriority = EventPriority.NORMAL,
    noinline handler: suspend E.(E) -> Unit
): Listener<E> =
    this.globalEventChannel().subscribeAlways(E::class, coroutineContext, concurrency, priority, handler)


@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribeAlways(eventClass, coroutineContext, concurrency, priority, handler)",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
public fun <E : Event> CoroutineScope.subscribeAlways(
    eventClass: KClass<out E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    priority: EventPriority = EventPriority.NORMAL,
    handler: suspend E.(E) -> Unit
): Listener<E> =
    this.globalEventChannel().subscribeAlways(eventClass, coroutineContext, concurrency, priority, handler)

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribeOnce(coroutineContext, priority, handler)",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
public inline fun <reified E : Event> CoroutineScope.subscribeOnce(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: EventPriority = EventPriority.NORMAL,
    noinline handler: suspend E.(E) -> Unit
): Listener<E> = this.globalEventChannel().subscribeOnce(coroutineContext, priority, handler)

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribeOnce(eventClass, coroutineContext, priority, handler)",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
public fun <E : Event> CoroutineScope.subscribeOnce(
    eventClass: KClass<out E>,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    priority: EventPriority = EventPriority.NORMAL,
    handler: suspend E.(E) -> Unit
): Listener<E> = this.globalEventChannel().subscribeOnce(eventClass, coroutineContext, priority, handler)

// endregion


// region subscribe for Kotlin functional reference


@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe1")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: (E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> =
    this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe2")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: E.(E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> =
    this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe1")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: suspend (E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> =
    this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith(
        "this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe3")
public inline fun <reified E : Event> CoroutineScope.subscribe(
    crossinline handler: suspend E.(E) -> ListeningStatus,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> =
    this.globalEventChannel().subscribe(E::class, coroutineContext, concurrency, priority) { handler(this) }


// endregion


// region subscribeAlways for Kotlin functional references


@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR, replaceWith =
    ReplaceWith(
        "this.globalEventChannel().subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribeAlways1")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: (E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = this.globalEventChannel()
    .subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR, replaceWith =
    ReplaceWith(
        "this.globalEventChannel().subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribeAlways1")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: E.(E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = this.globalEventChannel()
    .subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR, replaceWith =
    ReplaceWith(
        "this.globalEventChannel().subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe4")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: suspend (E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = this.globalEventChannel()
    .subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

@Deprecated(
    COROUTINE_SCOPE_SUBSCRIBE_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR, replaceWith =
    ReplaceWith(
        "this.globalEventChannel().subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }",
        "net.mamoe.mirai.event.Listener.ConcurrencyKind",
        "net.mamoe.mirai.event.Listener.EventPriority",
        "net.mamoe.mirai.event.globalEventChannel",
    )
)
@JvmSynthetic
@LowPriorityInOverloadResolution
@JvmName("subscribe1")
public inline fun <reified E : Event> CoroutineScope.subscribeAlways(
    crossinline handler: suspend E.(E) -> Unit,
    priority: EventPriority = EventPriority.NORMAL,
    concurrency: ConcurrencyKind = ConcurrencyKind.CONCURRENT,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Listener<E> = this.globalEventChannel()
    .subscribeAlways(E::class, coroutineContext, concurrency, priority) { handler(this) }

// endregion