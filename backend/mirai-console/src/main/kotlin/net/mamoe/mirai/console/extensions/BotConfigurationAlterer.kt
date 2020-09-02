@file:Suppress("unused")

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.FunctionExtension
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.BotConfiguration

/**
 * [MiraiConsole.addBot] 时的 [BotConfiguration] 修改扩展
 *
 * @see MiraiConsole.addBot
 */
@ConsoleExperimentalAPI
public interface BotConfigurationAlterer : FunctionExtension {

    /**
     * 修改 [configuration], 返回修改完成的 [BotConfiguration]
     */
    @JvmDefault
    public fun alterConfiguration(
        botId: Long,
        configuration: BotConfiguration
    ): BotConfiguration = configuration

    public companion object ExtensionPoint :
        AbstractExtensionPoint<BotConfigurationAlterer>(BotConfigurationAlterer::class)
}