/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.*
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.handler.selector.PacketTimeoutException
import net.mamoe.mirai.utils.BotConfiguration.HeartbeatStrategy.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info

/**
 * Accepts any kinds of exceptions. A [NetworkException] can control whether this error is recoverable, while any other ones are regarded as unexpected failure.
 */
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
        when (val hb = context[SsoProcessorContext].configuration.heartbeatStrategy) {
            STAT_HB -> {
                list += launchHeartbeatJobAsync(
                    scope = scope,
                    name = "${network.context.bot.id}.StatHeartbeat",
                    delay = { configuration.statHeartbeatPeriodMillis },
                    timeout = { timeout },
                    action = { heartbeatProcessor.doStatHeartbeatNow(network) },
                    onHeartFailure = onHeartFailure
                )
            }
            REGISTER -> {
                list += launchHeartbeatJobAsync(
                    scope = scope,
                    name = "${network.context.bot.id}.RegisterHeartbeat",
                    delay = { configuration.statHeartbeatPeriodMillis },
                    timeout = { timeout },
                    action = { heartbeatProcessor.doRegisterNow(network) },
                    onHeartFailure = onHeartFailure
                )
            }
            NONE -> {
            }
            else -> throw IllegalStateException("Unexpected HeartbeatStrategy: $hb")
        }

        list += launchHeartbeatJobAsync(
            scope = scope,
            name = "${network.context.bot.id}.AliveHeartbeat",
            delay = { configuration.heartbeatPeriodMillis },
            timeout = { timeout },
            action = { heartbeatProcessor.doAliveHeartbeatNow(network) },
            onHeartFailure = onHeartFailure
        )
        return list
    }

    /**
     * If any of the functions throw an exception, HB will fail unexpectedly can [onHeartFailure] will be called.
     */
    private fun launchHeartbeatJobAsync(
        scope: CoroutineScope,
        name: String,
        delay: () -> Long,
        timeout: () -> Long,
        action: suspend () -> Unit,
        onHeartFailure: HeartbeatFailureHandler,
    ): Deferred<Unit> {
        val coroutineName = "$name Scheduler"
        return scope.async(CoroutineName(coroutineName)) {
            while (isActive) {
                try {
                    delay(delay())
                } catch (e: CancellationException) {
                    return@async // considered normally cancel
                } catch (e: Throwable) {
                    onHeartFailure(
                        name,
                        IllegalStateException(
                            "$coroutineName: Internal error: exception in heartbeat delay function",
                            e
                        ) // throwing a ISE will stop the handler.
                    )
                    return@async
                }

                try {
                    var cause: Throwable? = null
                    val result = try {
                        withTimeoutOrNull(timeout()) { action() }
                    } catch (e: TimeoutCancellationException) {
                        // from `action`
                        cause = e
                        null
                    }
                    if (result == null) {
                        onHeartFailure(
                            name,
                            PacketTimeoutException(
                                "$coroutineName: Timeout receiving action response",
                                cause // cause is TimeoutCancellationException from `action`
                            ) // This is a NetworkException that is recoverable
                        )
                        return@async
                    }
                } catch (e: Throwable) {
                    // catch other errors in `action`, should not happen
                    onHeartFailure(
                        name,
                        IllegalStateException("$coroutineName: Internal error: caught unexpected exception", e)
                    ) // Terminal ISE
                    return@async
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