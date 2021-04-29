/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.context

import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.components.AccountSecretsManager
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.components.createAccountsSecretsManager
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo

/**
 * Provides the information needed by the [SsoProcessor].
 */
internal interface SsoProcessorContext {
    /**
     * Use other properties instead. Use [bot] only when you cannot find other properties.
     */
    val bot: QQAndroidBot

    val account: BotAccount
    val device: DeviceInfo

    val protocol: BotConfiguration.MiraiProtocol

    val accountSecretsManager: AccountSecretsManager

    val configuration: BotConfiguration

    companion object : ComponentKey<SsoProcessorContext>
}

internal class SsoProcessorContextImpl(
    override val bot: QQAndroidBot
) : SsoProcessorContext {
    override val account: BotAccount get() = bot.account
    override val device: DeviceInfo = configuration.deviceInfo?.invoke(bot) ?: DeviceInfo.random()
    override val protocol: BotConfiguration.MiraiProtocol get() = configuration.protocol
    override val accountSecretsManager: AccountSecretsManager
        get() = configuration.createAccountsSecretsManager(
            bot.logger.subLogger("AccountSecretsManager")
        )
    override val configuration: BotConfiguration get() = bot.configuration
}
