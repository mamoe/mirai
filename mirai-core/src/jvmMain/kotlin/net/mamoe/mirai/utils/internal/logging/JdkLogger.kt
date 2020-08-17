/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal.logging

import net.mamoe.mirai.utils.MiraiLoggerPlatformBase
import java.util.logging.Level
import java.util.logging.Logger

internal class JdkLogger(private val logger: Logger) : MiraiLoggerPlatformBase() {
    override fun verbose0(message: String?, e: Throwable?) {
        logger.log(Level.FINER, message, e)
    }

    override fun debug0(message: String?, e: Throwable?) {
        logger.log(Level.FINEST, message, e)

    }

    override fun info0(message: String?, e: Throwable?) {
        logger.log(Level.INFO, message, e)
    }

    override fun warning0(message: String?, e: Throwable?) {
        logger.log(Level.WARNING, message, e)
    }

    override fun error0(message: String?, e: Throwable?) {
        logger.log(Level.SEVERE, message, e)
    }

    override val identity: String?
        get() = logger.name
}