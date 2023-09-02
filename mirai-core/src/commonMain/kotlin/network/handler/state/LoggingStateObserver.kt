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
import net.mamoe.mirai.utils.*
import kotlin.coroutines.coroutineContext

internal class LoggingStateObserver(
    val logger: MiraiLogger,
    private val showStacktrace: Boolean = false,
) : StateObserver {
    override fun toString(): String = "LoggingStateObserver(logger=${logger.identity})"

    override fun beforeStateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl,
    ) {
        logger.debug(
            { "Before change: ${previous.correspondingState} -> ${new.correspondingState}" },
            if (showStacktrace) Exception("Show stacktrace") else null
        )
    }

    override fun stateChanged(
        networkHandler: NetworkHandlerSupport,
        previous: NetworkHandlerSupport.BaseStateImpl,
        new: NetworkHandlerSupport.BaseStateImpl,
    ) {
        logger.debug(
            { "State changed: ${previous.correspondingState} -> ${new.correspondingState}" },
            if (showStacktrace) Exception("Show stacktrace") else null
        )
    }

    override fun exceptionOnCreatingNewState(
        networkHandler: NetworkHandlerSupport,
        previousState: NetworkHandlerSupport.BaseStateImpl,
        exception: Throwable,
    ) {
        logger.debug { "State exception: ${previousState.correspondingState} -> $exception" }
    }

    override suspend fun beforeStateResume(networkHandler: NetworkHandler, state: NetworkHandlerSupport.BaseStateImpl) {
        logger.debug { "State resuming: [${coroutineContext.coroutineName}] ${state.correspondingState}" }
    }

    override suspend fun afterStateResume(
        networkHandler: NetworkHandler,
        state: NetworkHandlerSupport.BaseStateImpl,
        result: Result<Unit>,
    ) {
        result.fold(
            onSuccess = {
                logger.debug { "State resumed: [${coroutineContext.coroutineName}] ${state.correspondingState}." }
            },
            onFailure = {
                logger.debug {
                    "State resumed: [${coroutineContext.coroutineName}] ${state.correspondingState} " +
                            "${result.exceptionOrNull()?.unwrapCancellationException(false)}"
                }
            }
        )
    }

    companion object {
        /**
         * - `on`/`true` for simple logging
         * - `full` for logging with stacktrace
         */
        var ENABLED = systemProp(
            "mirai.network.state.observer.logging",
            "off"
        ).lowercase()

        fun createLoggingIfEnabled(): StateObserver? {
            return when (ENABLED) {
                "full" -> {
                    SafeStateObserver(
                        LoggingStateObserver(MiraiLogger.Factory.create(LoggingStateObserver::class, "States"), true),
                        MiraiLogger.Factory.create(LoggingStateObserver::class, "LoggingStateObserver errors")
                    )
                }
                "on", "true" -> {
                    SafeStateObserver(
                        LoggingStateObserver(MiraiLogger.Factory.create(LoggingStateObserver::class, "States"), false),
                        MiraiLogger.Factory.create(LoggingStateObserver::class, "LoggingStateObserver errors")
                    )
                }
                else -> null
            }
        }
    }
}