/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.utils.castOrNull
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.toHumanReadableString
import kotlin.time.measureTime

internal interface BotOfflineEventMonitor {
    companion object : ComponentKey<BotOfflineEventMonitor>

    fun attachJob(bot: QQAndroidBot, scope: CoroutineScope)
}

internal class BotOfflineEventMonitorImpl : BotOfflineEventMonitor {
    override fun attachJob(bot: QQAndroidBot, scope: CoroutineScope) {
        bot.eventChannel.parentScope(scope).subscribeAlways(
            ::onEvent,
            priority = EventPriority.MONITOR,
            concurrency = ConcurrencyKind.LOCKED,
        )
    }

    // TODO: 2021/4/25  Review BotOfflineEventMonitor
    private suspend fun onEvent(event: BotOfflineEvent) {
        val bot = event.bot.asQQAndroidBot()
        val network = bot.network
        if (
            !event.bot.isActive // bot closed
        // || _isConnecting // bot 还在登入 // TODO: 2021/4/14 处理还在登入?
        ) {
            // Close network to avoid endless reconnection while network is ok
            // https://github.com/mamoe/mirai/issues/894
            kotlin.runCatching { network.close(null) }
            return
        }
        /*
        if (network.areYouOk() && event !is BotOfflineEvent.Force && event !is BotOfflineEvent.MsfOffline) {
            // network 运行正常
            return@subscribeAlways
        }*/
        when (event) {
            is BotOfflineEvent.Active -> {
                val cause = event.cause
                val msg = if (cause == null) "" else " with exception: $cause"
                bot.logger.info("Bot is closed manually $msg", cause)
                network.close(null)
            }
            is BotOfflineEvent.Force -> {
                bot.logger.info { "Connection occupied by another android device: ${event.message}" }
                if (event.reconnect) {
                    bot.logger.info { "Reconnecting..." }
                    // delay(3000)
                } else {
                    network.close(null)
                }
            }
            is BotOfflineEvent.MsfOffline,
            is BotOfflineEvent.Dropped,
            is BotOfflineEvent.RequireReconnect,
            -> {
                // nothing to do
            }
        }

        if (event.reconnect) {
            if (!network.isOk()) {
                // normally closed
                return
            }

            val causeMessage = event.castOrNull<BotOfflineEvent.CauseAware>()?.cause?.toString() ?: event.toString()
            bot.logger.info { "Connection lost, retrying login ($causeMessage)." }

            bot.launch {
                val success: Boolean
                val time = measureTime {
                    success = kotlin.runCatching {
                        bot.login()
                    }.isSuccess // resume connection
                }

                if (success) {
                    bot.logger.info { "Reconnected successfully in ${time.toHumanReadableString()}." }
                }
            }
        }
    }

}
