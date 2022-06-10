/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.utils.castOrNull
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.millisToHumanReadableString
import net.mamoe.mirai.utils.warning
import kotlin.system.measureTimeMillis

/**
 * Handles [BotOfflineEvent]
 */
internal interface BotOfflineEventMonitor {
    companion object : ComponentKey<BotOfflineEventMonitor>

    /**
     * Attach a listener to the [scope]. [scope] is usually the scope of [NetworkHandler.State.OK].
     */
    fun attachJob(bot: AbstractBot, scope: CoroutineScope)
}

internal data class BotClosedByEvent(
    val event: BotOfflineEvent,
    override val message: String? = "Bot is closed by event '$event'.",
) : NetworkException(false)

internal class BotOfflineEventMonitorImpl : BotOfflineEventMonitor {
    override fun attachJob(bot: AbstractBot, scope: CoroutineScope) {
        bot.eventChannel.parentScope(scope).subscribeAlways(
            ::onEvent,
            priority = EventPriority.MONITOR,
            concurrency = ConcurrencyKind.LOCKED,
        )
    }

    private fun onEvent(event: BotOfflineEvent) {
        val bot = event.bot.asQQAndroidBot()
        val network = bot.network

        fun closeNetwork() {
            if (network.state == State.CLOSED) return // avoid recursive calls.
            network.close(if (event is BotOfflineEvent.CauseAware) event.cause else BotClosedByEvent(event))
        }

        when (event) {
            is BotOfflineEvent.Active -> {
                // This event might also be broadcast by the network handler by a state observer.
                // In that case, `network.state` will be `CLOSED` then `closeNetwork` returns immediately.
                // So there won't be recursive calls.

                val cause = event.cause
                val msg = if (cause == null) "" else " with exception: $cause"
                bot.logger.info("Bot is closed manually$msg", cause)

                closeNetwork()
            }
            is BotOfflineEvent.Force -> {
                bot.logger.warning { "Connection occupied by another android device: ${event.message}" }
                closeNetwork()
            }
            is BotOfflineEvent.MsfOffline,
            is BotOfflineEvent.Dropped,
            is BotOfflineEvent.RequireReconnect,
            -> {
                val causeMessage = event.castOrNull<BotOfflineEvent.CauseAware>()?.cause?.toString() ?: event.toString()
                bot.logger.warning { "Connection lost, reconnecting... ($causeMessage)" }
                closeNetwork()
            }
        }

        if (event.reconnect) {
            launchRecovery(bot)
        }
    }

    private fun launchRecovery(bot: AbstractBot) {
        // Run this coroutine in EventDispatcher, so joinBroadcast will work.
        // EventDispatcher is in Bot's components level so won't be closed by network.
        bot.components[EventDispatcher].broadcastAsync {
            val success: Boolean
            val time = measureTimeMillis {
                success = kotlin.runCatching {
                    bot.network.resumeConnection()
                }.isSuccess
            }

            if (success) {
                bot.logger.info { "Reconnected successfully in ${time.millisToHumanReadableString()}." }
            }

            null
        }
    }

}
