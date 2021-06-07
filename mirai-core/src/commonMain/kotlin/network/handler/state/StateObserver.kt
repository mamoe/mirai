/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.state

import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.state.CombinedStateObserver.Companion.plus
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.systemProp

/**
 * Observer of state changes.
 *
 * @see SafeStateObserver
 * @see LoggingStateObserver
 */
internal interface StateObserver {

    fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    ) {
    }

    fun exceptionOnCreatingNewState(
        networkHandler: NetworkHandlerSupport,
        previousState: NetworkHandlerSupport.BaseStateImpl,
        exception: Throwable,
    ) {
    }

    fun beforeStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
    ) {

    }

    fun afterStateResume(
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
        ): StateObserver {
            return observers.reduceOrNull { acc, stateObserver ->
                acc + stateObserver
            } ?: NOP
        }

        val LOGGING: StateObserver? = when (systemProp(
            "mirai.debug.network.state.observer.logging",
            "off"
        ).lowercase()) {
            "full" -> {
                SafeStateObserver(
                    LoggingStateObserver(MiraiLogger.create("States"), true),
                    MiraiLogger.create("LoggingStateObserver errors")
                )
            }
            "off", "false" -> {
                null
            }
            "on", "true" -> {
                SafeStateObserver(
                    LoggingStateObserver(MiraiLogger.create("States"), false),
                    MiraiLogger.create("LoggingStateObserver errors")
                )
            }
            else -> null
        }
    }
}
