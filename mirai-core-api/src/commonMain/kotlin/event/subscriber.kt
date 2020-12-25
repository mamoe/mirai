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

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import net.mamoe.mirai.event.Listener.EventPriority.*

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
     * - 若监听器使用 [Listener.ConcurrencyKind.LOCKED],
     * 在这之后监听器将会被从监听器列表中删除, 因此不再能接收到事件.
     * - 若使用 [Listener.ConcurrencyKind.CONCURRENT],
     * 在这之后无法保证立即停止监听.
     */
    STOPPED
}

/**
 * 事件监听器.
 * 由 [CoroutineScope.subscribe] 等方法返回.
 *
 * 取消监听: [complete]
 */
public interface Listener<in E : Event> : CompletableJob {

    public enum class ConcurrencyKind {
        /**
         * 并发地同时处理多个事件, 但无法保证 [onEvent] 返回 [ListeningStatus.STOPPED] 后立即停止事件监听.
         */
        CONCURRENT,

        /**
         * 使用 [Mutex] 保证同一时刻只处理一个事件.
         */
        LOCKED
    }

    /**
     * 并发类型
     */
    public val concurrencyKind: ConcurrencyKind

    /**
     * 事件优先级.
     *
     * 在广播时, 事件监听器的调用顺序为 (从左到右):
     * `[HIGHEST]` -> `[HIGH]` -> `[NORMAL]` -> `[LOW]` -> `[LOWEST]` -> `[MONITOR]`
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

        internal companion object {
            @JvmStatic
            internal val prioritiesExcludedMonitor: Array<EventPriority> = run {
                values().filter { it != MONITOR }.toTypedArray()
            }
        }
    }

    /**
     * 事件优先级
     * @see [EventPriority]
     */
    public val priority: EventPriority get() = NORMAL

    /**
     * 这个方法将会调用 [CoroutineScope.subscribe] 时提供的参数 `noinline handler: suspend E.(E) -> ListeningStatus`.
     *
     * 这个函数不会抛出任何异常, 详见 [CoroutineScope.subscribe]
     */
    public suspend fun onEvent(event: E): ListeningStatus
}

public typealias EventPriority = Listener.EventPriority