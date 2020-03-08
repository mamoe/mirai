/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.event.internal.broadcastInternal
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 可被监听的类, 可以是任何 class 或 object.
 *
 * 若监听这个类, 监听器将会接收所有事件的广播.
 *
 * @see subscribeAlways
 * @see subscribeOnce
 *
 * @see subscribeMessages
 *
 * @see [broadcast] 广播事件
 * @see [subscribe] 监听事件
 */
interface Event

/**
 * 可被取消的事件
 */
interface CancellableEvent {
    /**
     * 事件是否已取消.
     */
    val isCancelled: Boolean

    /**
     * 取消这个事件.
     */
    fun cancel()
}

/**
 * 可被取消的事件的实现
 */
abstract class AbstractCancellableEvent : Event, CancellableEvent {
    /**
     * 事件是否已取消.
     */
    override val isCancelled: Boolean get() = _cancelled

    private var _cancelled: Boolean = false

    /**
     * 取消事件.
     */
    override fun cancel() {
        _cancelled = true
    }
}

/**
 * 广播一个事件的唯一途径.
 */
@OptIn(MiraiInternalAPI::class)
suspend fun <E : Event> E.broadcast(): E = apply {
    if (this is BroadcastControllable && !this.shouldBroadcast) {
        return@apply
    }
    this@broadcast.broadcastInternal() // inline, no extra cost
}

/**
 * 设置为 `true` 以关闭事件.
 * 所有的 `subscribe` 都能正常添加到监听器列表, 但所有的广播都会直接返回.
 */
var EventDisabled = false

/**
 * 可控制是否需要广播这个事件包
 */
interface BroadcastControllable : Event {
    val shouldBroadcast: Boolean
        get() = true
}