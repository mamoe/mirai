/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.event.internal

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.ListeningStatus
import net.mamoe.mirai.utils.MiraiInternalAPI
import java.util.function.Consumer
import java.util.function.Function
import kotlin.coroutines.EmptyCoroutineContext

@MiraiInternalAPI
@Suppress("FunctionName")
fun <E : Event> Class<E>._subscribeEventForJaptOnly(scope: CoroutineScope, onEvent: Function<E, ListeningStatus>): Listener<E> {
    return this.kotlin.subscribeInternal(
        scope.Handler(
            EmptyCoroutineContext,
            Listener.ConcurrencyKind.CONCURRENT
        ) { onEvent.apply(it) })
}

@MiraiInternalAPI
@Suppress("FunctionName")
fun <E : Event> Class<E>._subscribeEventForJaptOnly(scope: CoroutineScope, onEvent: Consumer<E>): Listener<E> {
    return this.kotlin.subscribeInternal(
        scope.Handler(
            EmptyCoroutineContext,
            Listener.ConcurrencyKind.CONCURRENT
        ) { onEvent.accept(it); ListeningStatus.LISTENING; })
}