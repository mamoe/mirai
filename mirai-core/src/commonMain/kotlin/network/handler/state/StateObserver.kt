/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.state

import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport

/**
 * Observer of state changes.
 *
 * @see SafeStateObserver
 * @see LoggingStateObserver
 */
internal interface StateObserver {

    /**
     * Called when _state is being changed_, where [NetworkHandlerSupport._state] is still [previous], and new state is not yet started.
     */
    fun beforeStateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl,
    ) {

    }

    /**
     * Called when _state is fished changing_, where [NetworkHandlerSupport._state] had become [new].
     */
    fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl,
    ) {
    }

    fun exceptionOnCreatingNewState(
        networkHandler: NetworkHandlerSupport,
        previousState: NetworkHandlerSupport.BaseStateImpl,
        exception: Throwable,
    ) {
    }

    suspend fun beforeStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
    ) {

    }

    suspend fun afterStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
        result: Result<Unit>,
    ) {

    }

    companion object : ComponentKey<StateObserver> {
        internal val NOP = object : StateObserver {
            override fun toString(): String {
                return "StateObserver.NOP"
            }
        }

        fun chainOfNotNull(
            vararg observers: StateObserver?,
        ): StateObserver = CombinedStateObserver(observers.filterNotNull())
    }
}
