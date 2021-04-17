/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.state

import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport

internal class CombinedStateObserver(
    private val first: StateObserver,
    private val last: StateObserver,
) : StateObserver {
    override fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    ) {
        first.stateChanged(networkHandler, previous, new)
        last.stateChanged(networkHandler, previous, new)
    }

    override fun exceptionOnCreatingNewState(
        networkHandler: NetworkHandlerSupport,
        previousState: NetworkHandlerSupport.BaseStateImpl,
        exception: Throwable
    ) {
        first.exceptionOnCreatingNewState(networkHandler, previousState, exception)
        last.exceptionOnCreatingNewState(networkHandler, previousState, exception)
    }

    override fun beforeStateResume(networkHandler: NetworkHandler, state: NetworkHandlerSupport.BaseStateImpl) {
        first.beforeStateResume(networkHandler, state)
        last.beforeStateResume(networkHandler, state)
    }

    override fun afterStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
        result: Result<Unit>
    ) {
        first.afterStateResume(networkHandler, state, result)
        last.afterStateResume(networkHandler, state, result)
    }

    override fun toString(): String {
        return "CombinedStateObserver(first=$first, last=$last)"
    }

    companion object {
        operator fun StateObserver?.plus(last: StateObserver?): StateObserver {
            return when {
                this == null -> last
                last == null -> this
                else -> CombinedStateObserver(this, last)
            } ?: StateObserver.NOP
        }
    }
}