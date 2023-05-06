/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleFrontEndImplementation::class, MiraiExperimentalApi::class, ConsoleExperimentalApi::class)

package net.mamoe.mirai.console.internal.logging

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.internal.data.builtins.LoggerConfig
import net.mamoe.mirai.console.logging.AbstractLoggerController
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiExperimentalApi

internal class LoggerControllerImpl : AbstractLoggerController.PathBased() {
    // 防止 stack overflow (使用 logger 要加载 LoggerController, LoggerConfig 可能会使用 logger)
    // 在 console init 阶段 register
    internal val loggerConfig: LoggerConfig by lazy {
        LoggerConfig()
    }
    override val isLoggerControlStateSupported: Boolean get() = true

    internal fun onReload() {
        this.loggerConfigUpdateTime++
    }

    override fun findPriority(identity: String?): LogPriority? {
        return if (identity == null) {
            loggerConfig.defaultPriority
        } else {
            loggerConfig.loggers[identity]
        }
    }

    override val defaultPriority: LogPriority
        get() = loggerConfig.defaultPriority
}