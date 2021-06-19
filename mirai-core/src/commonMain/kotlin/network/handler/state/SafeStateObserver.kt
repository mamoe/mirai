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
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.error

internal fun StateObserver.safe(logger: MiraiLogger): StateObserver {
    if (this is SafeStateObserver) return this
    return SafeStateObserver(this, logger)
}

/**
 * Catches exception then log by [logger]
 */
internal class SafeStateObserver(
    val delegate: StateObserver,
    val logger: MiraiLogger,
) : StateObserver {

    override fun toString(): String {
        return "SafeStateObserver(delegate=$delegate)"
    }

    override fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    ) {
        try {
            delegate.stateChanged(networkHandler, previous, new)
        } catch (e: Throwable) {
            logger.error(
                { "Internal error: exception in StateObserver $delegate" },
                ExceptionInStateObserverException(e)
            )
        }
    }

    override fun exceptionOnCreatingNewState(
        networkHandler: NetworkHandlerSupport,
        previousState: NetworkHandlerSupport.BaseStateImpl,
        exception: Throwable
    ) {
        try {
            delegate.exceptionOnCreatingNewState(networkHandler, previousState, exception)
        } catch (e: Throwable) {
            logger.error(
                { "Internal error: exception in StateObserver $delegate" },
                ExceptionInStateObserverException(e)
            )
        }
    }

    override fun beforeStateResume(networkHandler: NetworkHandler, state: NetworkHandlerSupport.BaseStateImpl) {
        try {
            delegate.beforeStateResume(networkHandler, state)
        } catch (e: Throwable) {
            logger.error(
                { "Internal error: exception in StateObserver $delegate" },
                ExceptionInStateObserverException(e)
            )
        }
    }

    override fun afterStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
        result: Result<Unit>
    ) {
        try {
            delegate.afterStateResume(networkHandler, state, result)
        } catch (e: Throwable) {
            logger.error(
                { "Internal error: exception in StateObserver $delegate" },
                ExceptionInStateObserverException(e)
            )
        }
    }
}