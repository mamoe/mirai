/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.logging

import net.mamoe.mirai.console.logging.LoggerController
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase
import net.mamoe.mirai.utils.SimpleLogger

internal class MiraiConsoleLogger(
    private val controller: LoggerController,
    val logger: MiraiLogger
) : MiraiLoggerPlatformBase() {
    override val identity: String? get() = logger.identity
    override val isEnabled: Boolean get() = logger.isEnabled
    override fun info0(message: String?, e: Throwable?) {
        if (controller.shouldLog(identity, SimpleLogger.LogPriority.INFO))
            logger.info(message, e)
    }

    override fun warning0(message: String?, e: Throwable?) {
        if (controller.shouldLog(identity, SimpleLogger.LogPriority.WARNING))
            logger.warning(message, e)
    }

    override fun debug0(message: String?, e: Throwable?) {
        if (controller.shouldLog(identity, SimpleLogger.LogPriority.DEBUG))
            logger.debug(message, e)
    }

    override fun error0(message: String?, e: Throwable?) {
        if (controller.shouldLog(identity, SimpleLogger.LogPriority.ERROR))
            logger.error(message, e)
    }

    override fun verbose0(message: String?, e: Throwable?) {
        if (controller.shouldLog(identity, SimpleLogger.LogPriority.VERBOSE))
            logger.verbose(message, e)
    }
}
