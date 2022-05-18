/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "DEPRECATION", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:JvmName("SubscriberKt")
@file:JvmMultifileClass

package net.mamoe.mirai.event

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.event.EventPriority.*
import net.mamoe.mirai.utils.NotStableForInheritance
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * 订阅者的状态
 */
public enum class ListeningStatus {
    /**
     * 表示继续监听
     */
    LISTENING,

    /**
     * 表示已停止.
     *
     * - 若监听器使用 [ConcurrencyKind.LOCKED],
     * 在这之后监听器将会被从监听器列表中删除, 因此不再能接收到事件.
     * - 若使用 [ConcurrencyKind.CONCURRENT],
     * 在这之后无法保证立即停止监听.
     */
    STOPPED
}

/**
 * 事件监听器.
 * 由 [EventChannel.subscribe] 等方法返回.
 *
 * 取消监听: [complete]
 */
@NotStableForInheritance
public interface Listener<in E : Event> : CompletableJob {

    // Impl notes:
    // Inheriting CompletableJob is a bad idea. See #1224.
    // However, we cannot change it as it leads to binary changes.
    // We can do it in 3.0 or when we found incompatibility with kotlinx.coroutines.

    /**
     * 并发类型
     */
    public val concurrencyKind: ConcurrencyKind


    /**
     * 事件优先级
     * @see [EventPriority]
     */
    public val priority: EventPriority get() = NORMAL

    /**
     * 这个方法将会调用 [EventChannel.subscribe] 时提供的参数 `noinline handler: suspend E.(E) -> ListeningStatus`.
     *
     * 这个函数会传递捕获的异常到本 [Listener] 创建时提供的监听方 [CoroutineContext] (通常). 详细行为可见 [EventChannel.subscribe].
     */
    public suspend fun onEvent(event: E): ListeningStatus
}

public enum class ConcurrencyKind {
    /**
     * 并发地同时处理多个事件, 但无法保证 [Listener.onEvent] 返回 [ListeningStatus.STOPPED] 后立即停止事件监听.
     */
    CONCURRENT,

    /**
     * 使用 [Mutex] 保证同一时刻只处理一个事件.
     */
    LOCKED
}


/**
 * 事件优先级.
 *
 * 在广播时, 事件监听器的调用顺序为 (从左到右):
 * [HIGHEST] -> [HIGH] -> [NORMAL] -> [LOW] -> [LOWEST] -> [MONITOR]
 *
 * - 使用 [MONITOR] 优先级的监听器将会被**并行**调用.
 * - 使用其他优先级的监听器都将会**按顺序**调用.
 *   因此一个监听器的挂起可以阻塞事件处理过程而导致低优先级的监听器较晚处理.
 *
 * 当事件被 [拦截][Event.intercept] 后, 优先级较低 (靠右) 的监听器将不会被调用.
 */
public enum class EventPriority {

    HIGHEST, HIGH, NORMAL, LOW, LOWEST,

    /**
     * 最低的优先级.
     *
     * 使用此优先级的监听器应遵循约束:
     * - 不 [拦截事件][Event.intercept]
     */
    MONITOR;
}