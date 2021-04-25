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
import net.mamoe.mirai.utils.debug

internal class LoggingStateObserver(
    val logger: MiraiLogger,
    private val showStacktrace: Boolean = false
) : StateObserver {
    override fun toString(): String {
        return "LoggingStateObserver"
    }

    override fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl
    ) {
        logger.debug(
            { "State changed: ${previous.correspondingState} -> ${new.correspondingState}" },
            if (showStacktrace) Exception("Show stacktrace") else null
        )
    }

    override fun exceptionOnCreatingNewState(
        networkHandler: NetworkHandlerSupport,
        previousState: NetworkHandlerSupport.BaseStateImpl,
        exception: Throwable
    ) {
        logger.debug { "State changed: ${previousState.correspondingState} -> $exception" }
    }

    override fun beforeStateResume(networkHandler: NetworkHandler, state: NetworkHandlerSupport.BaseStateImpl) {
        logger.debug { "State resuming: ${state.correspondingState}." }
    }

    override fun afterStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
        result: Result<Unit>
    ) {
        result.fold(
            onSuccess = {
                logger.debug { "State resumed: ${state.correspondingState}." }
            },
            onFailure = {
                logger.debug { "State resumed: ${state.correspondingState} ${result.exceptionOrNull()}" }
            }
        )
    }
}