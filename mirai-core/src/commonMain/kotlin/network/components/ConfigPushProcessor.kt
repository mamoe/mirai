/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.protocol.packet.login.ConfigPushSvc
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.warning

/**
 * Job: Switch server if ConfigPush not received.
 */
internal interface ConfigPushProcessor {
    suspend fun syncConfigPush(network: NetworkHandler)

    companion object : ComponentKey<ConfigPushProcessor>
}

internal class ConfigPushProcessorImpl(
    private val logger: MiraiLogger,
) : ConfigPushProcessor {
    override suspend fun syncConfigPush(network: NetworkHandler) {
        val resp = withTimeoutOrNull(60_000) {
            globalEventChannel().nextEvent<ConfigPushSvc.PushReq.PushReqResponse>(
                EventPriority.MONITOR
            ) { it.bot == network.context.bot }
        }

        if (resp == null) {
            val bdhSyncer = network.context[BdhSessionSyncer]
            if (!bdhSyncer.hasSession) {
                val e = NetworkException("Timeout waiting for ConfigPush.",true)
                bdhSyncer.bdhSession.completeExceptionally(e)
                logger.warning { "Missing ConfigPush. Switching server..." }
                network.context[SsoProcessor].casFirstLoginResult(null, FirstLoginResult.CHANGE_SERVER)
                network.context.bot.components[EventDispatcher].broadcastAsync(
                    BotOfflineEvent.RequireReconnect(
                        network.context.bot,
                        e
                    )
                )
            }
        }
    }

}
