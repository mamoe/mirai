/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.nextEventOrNull
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
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
        if (nextEventOrNull<ConfigPushSvc.PushReq.PushReqResponse>(60_000) { it.bot == network.context.bot } == null) {
            val bdhSyncer = network.context[BdhSessionSyncer]
            if (!bdhSyncer.hasSession) {
                val e = IllegalStateException("Timeout waiting for ConfigPush.")
                bdhSyncer.bdhSession.completeExceptionally(e)
                logger.warning { "Missing ConfigPush. Switching server..." }
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