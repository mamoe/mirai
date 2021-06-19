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
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.WtLogin15
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.millisToHumanReadableString
import net.mamoe.mirai.utils.minutesToMillis

internal interface KeyRefreshProcessor {
    suspend fun keyRefreshLoop(handler: NetworkHandler)

    @Throws(LoginFailedException::class)
    suspend fun refreshKeysNow(handler: NetworkHandler)

    companion object : ComponentKey<KeyRefreshProcessor>
}

internal class KeyRefreshProcessorImpl(
    private val logger: MiraiLogger,
) : KeyRefreshProcessor {
    override suspend fun keyRefreshLoop(handler: NetworkHandler): Unit = coroutineScope {
        val client = handler.context[SsoProcessor].client
        launch(CoroutineName("Login Session Refresh Scheduler")) {
            while (isActive) {
                client.wLoginSigInfo.vKey.run {
                    //由过期时间最短的且不会被skey更换更新的vkey计算重新登录的时间
                    val delay = (expireTime - creationTime).times(1000) - 5.minutesToMillis
                    logger.info { "Scheduled refresh login session in ${delay.millisToHumanReadableString()}." }
                    delay(delay)
                }
                runCatching {
                    handler.context[SsoProcessor].login(handler)
                }.onFailure {
                    logger.warning("Failed to refresh login session.", it)
                }
            }
        }
        launch(CoroutineName("Key Refresh Scheduler")) {
            while (isActive) {
                client.wLoginSigInfo.sKey.run {
                    val delay = (expireTime - creationTime).times(1000) - 5.minutesToMillis
                    logger.info { "Scheduled key refresh in ${delay.millisToHumanReadableString()}." }
                    delay(delay)
                }
                runCatching {
                    refreshKeysNow(handler)
                }.onFailure {
                    logger.error("Failed to refresh key.", it)
                }
            }
        }
    }

    override suspend fun refreshKeysNow(handler: NetworkHandler) {
        WtLogin15(handler.context[SsoProcessor].client).sendAndExpect(handler)
    }
}