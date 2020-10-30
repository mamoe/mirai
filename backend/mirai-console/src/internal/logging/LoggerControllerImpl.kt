/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.logging

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.internal.data.builtins.LoggerConfig
import net.mamoe.mirai.console.logging.AbstractLoggerController
import net.mamoe.mirai.console.logging.LogPriority
import net.mamoe.mirai.console.util.ConsoleInternalApi

@ConsoleFrontEndImplementation
@ConsoleInternalApi
internal object LoggerControllerImpl : AbstractLoggerController() {

    override fun getPriority(identity: String?): LogPriority = if (identity == null) {
        LoggerConfig.defaultPriority
    } else {
        LoggerConfig.loggers[identity] ?: LoggerConfig.defaultPriority
    }

}