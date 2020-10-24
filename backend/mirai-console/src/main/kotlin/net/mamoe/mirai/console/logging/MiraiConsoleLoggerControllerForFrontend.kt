/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

package net.mamoe.mirai.console.logging

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.internal.data.builtins.ConsoleDataScope
import net.mamoe.mirai.console.internal.data.builtins.LoggerConfig
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.utils.SimpleLogger

@ConsoleFrontEndImplementation
@ConsoleInternalApi
public abstract class MiraiConsoleLoggerControllerForFrontend : MiraiConsoleLoggerControllerPlatformBase() {

    private fun shouldLog(
        priority: LoggerConfig.LogPriority,
        settings: LoggerConfig.LogPriority
    ): Boolean = settings <= priority

    private fun shouldLog(identity: String?, priority: LoggerConfig.LogPriority): Boolean {
        return if (identity == null) {
            shouldLog(priority, LoggerConfig.defaultPriority)
        } else {
            shouldLog(priority, LoggerConfig.loggers[identity] ?: LoggerConfig.defaultPriority)
        }
    }

    override fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean =
        shouldLog(identity, LoggerConfig.LogPriority.by(priority))

}