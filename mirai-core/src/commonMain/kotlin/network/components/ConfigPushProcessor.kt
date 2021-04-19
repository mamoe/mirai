/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.coroutines.CancellationException
import net.mamoe.mirai.event.nextEventOrNull
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.login.ConfigPushSvc
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning

internal interface ConfigPushProcessor {
    @Throws(RequireReconnectException::class)
    suspend fun syncConfigPush(network: NetworkHandler)

    class RequireReconnectException : Exception("ConfigPushProcessor: server requires reconnection")

    companion object : ComponentKey<ConfigPushProcessor>
}

internal class ConfigPushProcessorImpl(
    private val logger: MiraiLogger,
) : ConfigPushProcessor {
    @Throws(ConfigPushProcessor.RequireReconnectException::class)
    override suspend fun syncConfigPush(network: NetworkHandler) {
        network.ConfigPushSyncer()
    }

    @Suppress("FunctionName", "UNUSED_VARIABLE")
    private suspend fun NetworkHandler.ConfigPushSyncer() {
        logger.info { "Awaiting ConfigPushSvc.PushReq." }
        when (val resp: ConfigPushSvc.PushReq.PushReqResponse? = nextEventOrNull(30_000)) {
            null -> {
                val bdhSyncer = context[BdhSessionSyncer]
                val hasSession = bdhSyncer.hasSession
                kotlin.runCatching { bdhSyncer.bdhSession.completeExceptionally(CancellationException("Timeout waiting for ConfigPushSvc.PushReq")) }
                if (!hasSession) {
                    logger.warning { "Missing ConfigPushSvc.PushReq. Switching server..." }
//                    bot.launch { BotOfflineEvent.RequireReconnect(bot).broadcast() }
                    throw ConfigPushProcessor.RequireReconnectException()
                } else {
                    logger.warning { "Missing ConfigPushSvc.PushReq. Using the latest response. File uploading may be affected." }
                }
            }
            is ConfigPushSvc.PushReq.PushReqResponse.ConfigPush -> {
                logger.info { "ConfigPushSvc.PushReq: Config updated." }
            }
            is ConfigPushSvc.PushReq.PushReqResponse.ServerListPush -> {
                logger.info { "ConfigPushSvc.PushReq: Server updated." }
                // handled in ConfigPushSvc
                return
            }
        }
    }
}