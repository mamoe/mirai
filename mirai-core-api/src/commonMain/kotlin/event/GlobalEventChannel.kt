/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("EventChannelKt")

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 全局事件通道. 此通道包含来自所有 [Bot] 的所有类型的事件. 可通过 [EventChannel.filter] 过滤得到范围更小的 [EventChannel].
 *
 * @see EventChannel
 */
public object GlobalEventChannel : EventChannel<Event>(Event::class, EmptyCoroutineContext)

/**
 * 在此 [CoroutineScope] 下创建一个监听所有事件的 [EventChannel]. 相当于 `GlobalEventChannel.parentScope(this).context(coroutineContext)`.
 *
 * 在返回的 [EventChannel] 中的事件监听器都会以 [this] 作为父协程作用域. 即会 使用 [this]
 *
 * @param coroutineContext 额外的 [CoroutineContext]
 *
 * @throws IllegalStateException 当 [this] 和 [coroutineContext] 均不包含 [CoroutineContext]
 */
@JvmSynthetic
public fun CoroutineScope.globalEventChannel(coroutineContext: CoroutineContext = EmptyCoroutineContext): EventChannel<Event> {
    return if (coroutineContext === EmptyCoroutineContext) GlobalEventChannel.parentScope(this)
    else GlobalEventChannel.parentScope(this).context(coroutineContext)
}
