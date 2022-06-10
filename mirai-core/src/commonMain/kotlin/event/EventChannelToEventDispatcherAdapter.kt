/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import net.mamoe.mirai.event.Event
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

internal class EventChannelToEventDispatcherAdapter<E : Event> private constructor(
    baseEventClass: KClass<out E>, defaultCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : EventChannelImpl<E>(baseEventClass, defaultCoroutineContext) {
    companion object {
        @InternalEventMechanism
        val instance by lazy { EventChannelToEventDispatcherAdapter(Event::class, EmptyCoroutineContext) }
    }
}
