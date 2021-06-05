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

        val list = mutableListOf<Job>()
        list += launchHeartbeatJobAsync(
            scope = scope,
            name = "StatHeartbeat",
            timeout = { context[SsoProcessorContext].configuration.statHeartbeatPeriodMillis },
            action = { heartbeatProcessor.doStatHeartbeatNow(network) },
            onHeartFailure = onHeartFailure
        )
        list += launchHeartbeatJobAsync(
            scope = scope,
            name = "AliveHeartbeat",
            timeout = { context[SsoProcessorContext].configuration.heartbeatPeriodMillis },
            action = { heartbeatProcessor.doAliveHeartbeatNow(network) },
            onHeartFailure = onHeartFailure
        )
        return list
    }

    private fun launchHeartbeatJobAsync(
        scope: CoroutineScope,
        name: String,
        timeout: () -> Long,
        action: suspend () -> Unit,
        onHeartFailure: HeartbeatFailureHandler,
    ): Deferred<Unit> {
        return scope.async(CoroutineName("$name Scheduler")) {
            while (isActive) {
                try {
                    delay(timeout())
                } catch (e: CancellationException) {
                    return@async // considered normally cancel
                }

                try {
                    action()
                } catch (e: Throwable) {
                    onHeartFailure(name, e)
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