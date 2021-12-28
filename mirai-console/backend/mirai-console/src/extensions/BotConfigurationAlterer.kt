/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.FunctionExtension
import net.mamoe.mirai.utils.BotConfiguration

/**
 * [MiraiConsole.addBot] 时的 [BotConfiguration] 修改扩展
 *
 * @see MiraiConsole.addBot
 */
@Suppress("SpellCheckingInspection") // alterer
public fun interface BotConfigurationAlterer : FunctionExtension {

    /**
     * 修改 [configuration], 返回修改完成的 [BotConfiguration]
     */
    public fun alterConfiguration(
        botId: Long,
        configuration: BotConfiguration,
    ): BotConfiguration

    public companion object ExtensionPoint :
        AbstractExtensionPoint<BotConfigurationAlterer>(BotConfigurationAlterer::class)
}