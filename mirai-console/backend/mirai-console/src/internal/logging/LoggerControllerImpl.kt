/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.logging

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.internal.data.builtins.LoggerConfig
import net.mamoe.mirai.console.logging.AbstractLoggerController
import net.mamoe.mirai.console.util.ConsoleInternalApi

@ConsoleFrontEndImplementation
@ConsoleInternalApi
internal object LoggerControllerImpl : AbstractLoggerController.PathBased() {
    internal var initialized = false

    override fun findPriority(identity: String?): LogPriority? {
        if (!initialized) return LogPriority.NONE
        return if (identity == null) {
            LoggerConfig.defaultPriority
        } else {
            LoggerConfig.loggers[identity]
        }
    }

    override val defaultPriority: LogPriority
        get() = if (initialized) LoggerConfig.defaultPriority else LogPriority.NONE
}