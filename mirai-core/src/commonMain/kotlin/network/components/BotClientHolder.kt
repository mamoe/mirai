/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.lateinitMutableProperty

internal interface BotClientHolder {
    var client: QQAndroidClient

    fun refreshClient()

    companion object : ComponentKey<BotClientHolder>
}

internal class BotClientHolderImpl(
    private val bot: QQAndroidBot,
    private val logger: MiraiLogger,
) : BotClientHolder {
    override var client: QQAndroidClient by lateinitMutableProperty { createClient(bot) }

    override fun refreshClient() {
        client = createClient(bot)
    }

    private fun createClient(bot: QQAndroidBot): QQAndroidClient {
        val ssoContext = bot.components[SsoProcessorContext]
        val device = ssoContext.device
        return QQAndroidClient(
            ssoContext.account,
            device = device,
            accountSecrets = bot.components[AccountSecretsManager].getSecretsOrCreate(ssoContext.account, device)
        ).apply {
            _bot = bot
        }
    }
}