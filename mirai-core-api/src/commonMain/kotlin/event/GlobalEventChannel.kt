/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("EventChannelKt")

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.loadService
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.reflect.KClass

/**
 * 全局事件通道. 此通道包含来自所有 [Bot] 的所有类型的事件. 可通过 [EventChannel.filter] 过滤得到范围更小的 [EventChannel].
 *
 * @see EventChannel
 */
@OptIn(MiraiInternalApi::class)
public object GlobalEventChannel : EventChannel<Event>(Event::class, EmptyCoroutineContext) {
    private val instance by lazy {
        loadService(InternalGlobalEventChannelProvider::class).getInstance()
    }

    override fun asFlow(): Flow<Event> = instance.asFlow()
    override fun <E : Event> registerListener(eventClass: KClass<out E>, listener: Listener<E>) {
        return instance.registerListener0(eventClass, listener)
    }

    override fun <E : Event> createListener(
        coroutineContext: CoroutineContext,
        concurrencyKind: ConcurrencyKind,
        priority: EventPriority,
        listenerBlock: suspend (E) -> ListeningStatus
    ): Listener<E> = instance.createListener0(coroutineContext, concurrencyKind, priority, listenerBlock)

    override fun context(vararg coroutineContexts: CoroutineContext): EventChannel<Event> {
        return instance.context(*coroutineContexts)
    }
}

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

/**
 * @since 2.12
 */
@MiraiInternalApi
public interface InternalGlobalEventChannelProvider {
    public fun getInstance(): EventChannel<Event>
}