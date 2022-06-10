/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework.components

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.protocol.data.jce.SvcRespRegister
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.lateinitMutableProperty

internal open class TestSsoProcessor(private val bot: QQAndroidBot) : SsoProcessor {
    val deviceInfo = bot.configuration.createDeviceInfo(bot)
    override var client: QQAndroidClient by lateinitMutableProperty {
        QQAndroidClient(bot.account, device = deviceInfo, accountSecrets = AccountSecretsImpl(deviceInfo, bot.account))
    }
    override val ssoSession: SsoSession get() = bot.client
    override val firstLoginResult: AtomicRef<FirstLoginResult?> = atomic(null)
    override var registerResp: StatSvc.Register.Response? = null
    override suspend fun login(handler: NetworkHandler) {
        bot.network.logger.debug { "SsoProcessor.login" }
    }

    override suspend fun logout(handler: NetworkHandler) {
        bot.network.logger.debug { "SsoProcessor.logout" }
    }

    override suspend fun sendRegister(handler: NetworkHandler): StatSvc.Register.Response {
        bot.network.logger.debug { "SsoProcessor.sendRegister" }
        return StatSvc.Register.Response(SvcRespRegister())
    }
}