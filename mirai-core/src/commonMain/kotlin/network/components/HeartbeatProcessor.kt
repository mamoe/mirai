/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.login.Heartbeat
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc

internal interface HeartbeatProcessor {

    @Throws(Exception::class)
    suspend fun doAliveHeartbeatNow(networkHandler: NetworkHandler)

    @Throws(Exception::class)
    suspend fun doStatHeartbeatNow(networkHandler: NetworkHandler)

    @Throws(Exception::class)
    suspend fun doRegisterNow(networkHandler: NetworkHandler): StatSvc.Register.Response

    companion object : ComponentKey<HeartbeatProcessor>
}

internal class HeartbeatProcessorImpl : HeartbeatProcessor {
    @Throws(Exception::class)
    override suspend fun doStatHeartbeatNow(networkHandler: NetworkHandler) {
        networkHandler.sendAndExpect(
            StatSvc.SimpleGet(networkHandler.context.bot.client),
            timeout = networkHandler.context[SsoProcessorContext].configuration.heartbeatTimeoutMillis,
            attempts = 2
        )
    }

    @Throws(Exception::class)
    override suspend fun doAliveHeartbeatNow(networkHandler: NetworkHandler) {
        networkHandler.sendAndExpect(
            Heartbeat.Alive(networkHandler.context.bot.client),
            timeout = networkHandler.context[SsoProcessorContext].configuration.heartbeatTimeoutMillis,
            attempts = 2
        )
    }

    @Throws(Exception::class)
    override suspend fun doRegisterNow(networkHandler: NetworkHandler): StatSvc.Register.Response {
        return networkHandler.context[SsoProcessor].sendRegister(networkHandler)
    }
}