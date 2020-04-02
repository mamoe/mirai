/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.console.events;

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.internal._subscribeEventForJaptOnly
import java.util.function.Consumer
import java.util.function.Function

internal fun <E : Event> broadcast(e: E): E = runBlocking { e.broadcast() }

internal fun <E : Event> Class<E>.subscribeEventForJaptOnly(
    scope: CoroutineScope,
    onEvent: Function<E, ListeningStatus>
): Listener<E> = _subscribeEventForJaptOnly(scope, onEvent)

internal fun <E : Event> Class<E>.subscribeEventForJaptOnly(scope: CoroutineScope, onEvent: Consumer<E>): Listener<E> =
    _subscribeEventForJaptOnly(scope, onEvent)