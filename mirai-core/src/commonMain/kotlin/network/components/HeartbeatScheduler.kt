/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.*
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.context.SsoProcessorContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.selector.PacketTimeoutException
import net.mamoe.mirai.utils.BotConfiguration.HeartbeatStrategy.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info

internal typealias HeartbeatFailureHandler = (name: String, e: Throwable) -> Unit

/**
 * Schedules [HeartbeatProcessor]
 */
internal interface HeartbeatScheduler {
    fun launchJobsIn(
        network: NetworkHandlerSupport,
        scope: CoroutineScope,
        onHeartFailure: HeartbeatFailureHandler
    ): List<Job>

    companion object : ComponentKey<HeartbeatScheduler>
}

internal class TimeBasedHeartbeatSchedulerImpl(
    private val logger: MiraiLogger,
) : HeartbeatScheduler {
    override fun launchJobsIn(
        network: NetworkHandlerSupport,
        scope: CoroutineScope,
        onHeartFailure: HeartbeatFailureHandler
    ): List<Job> {
        val context: ComponentStorage = network.context
        val heartbeatProcessor = context[HeartbeatProcessor]

        val configuration = context[SsoProcessorContext].configuration
        val timeout = configuration.heartbeatTimeoutMillis

        val list = mutableListOf<Job>()
        when (context[SsoProcessorContext].configuration.heartbeatStrategy) {
            STAT_HB -> {
                list += launchHeartbeatJobAsync(
                    scope = scope,
                    name = "StatHeartbeat",
                    delay = { configuration.statHeartbeatPeriodMillis },
                    timeout = { timeout },
                    action = { heartbeatProcessor.doStatHeartbeatNow(network) },
                    onHeartFailure = onHeartFailure
                )
            }
            REGISTER -> {
                list += launchHeartbeatJobAsync(
                    scope = scope,
                    name = "RegisterHeartbeat",
                    delay = { configuration.statHeartbeatPeriodMillis },
                    timeout = { timeout },
                    action = { heartbeatProcessor.doRegisterNow(network) },
                    onHeartFailure = onHeartFailure
                )
            }
            NONE -> {
            }
        }

        list += launchHeartbeatJobAsync(
            scope = scope,
            name = "AliveHeartbeat",
            delay = { configuration.heartbeatPeriodMillis },
            timeout = { timeout },
            action = { heartbeatProcessor.doAliveHeartbeatNow(network) },
            onHeartFailure = onHeartFailure
        )
        return list
    }

    private fun launchHeartbeatJobAsync(
        scope: CoroutineScope,
        name: String,
        delay: () -> Long,
        timeout: () -> Long,
        action: suspend () -> Unit,
        onHeartFailure: HeartbeatFailureHandler,
    ): Deferred<Unit> {
        return scope.async(CoroutineName("$name Scheduler")) {
            while (isActive) {
                try {
                    delay(delay())
                } catch (e: CancellationException) {
                    return@async // considered normally cancel
                }

                try {
                    withTimeout(timeout()) {
                        action()
                    }
                } catch (e: Throwable) {
                    onHeartFailure(name, PacketTimeoutException("Timeout receiving Heartbeat response", e))
                }
            }
        }.apply {
            invokeOnCompletion { e ->
                if (e is CancellationException) return@invokeOnCompletion // normally closed
                if (e != null) logger.info { "$name failed: $e." }
            }
        }
    }


}