/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.state

import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport

internal class CombinedStateObserver(
    private val list: List<StateObserver>,
) : StateObserver {
    override fun beforeStateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl,
    ) {
        list.forEach { it.beforeStateChanged(networkHandler, previous, new) }
    }

    override fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl,
    ) {
        list.forEach { it.stateChanged(networkHandler, previous, new) }
    }

    override fun exceptionOnCreatingNewState(
        networkHandler: NetworkHandlerSupport,
        previousState: NetworkHandlerSupport.BaseStateImpl,
        exception: Throwable,
    ) {
        list.forEach { it.exceptionOnCreatingNewState(networkHandler, previousState, exception) }
    }

    override suspend fun beforeStateResume(networkHandler: NetworkHandler, state: NetworkHandlerSupport.BaseStateImpl) {
        list.forEach { it.beforeStateResume(networkHandler, state) }
    }

    override suspend fun afterStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
        result: Result<Unit>,
    ) {
        list.forEach { it.afterStateResume(networkHandler, state, result) }
    }

    override fun toString(): String {
        return list.joinToString(
            prefix = "CombinedStateObserver[",
            postfix = "]",
            separator = " -> "
        ) { it.toString() }
    }

    companion object {
        operator fun StateObserver?.plus(last: StateObserver?): StateObserver {
            return when {
                last == null -> this ?: StateObserver.NOP
                this == null -> last
                else -> {
                    val result = mutableListOf<StateObserver>()
                    if (this is CombinedStateObserver) result.addAll(this.list) else result.add(this)
                    if (last is CombinedStateObserver) result.addAll(last.list) else result.add(last)
                    CombinedStateObserver(result)
                }
            }
        }
    }
}