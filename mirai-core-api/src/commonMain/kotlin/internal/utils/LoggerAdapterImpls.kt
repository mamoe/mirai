/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.MiraiLoggerPlatformBase
import org.slf4j.Logger
import java.util.logging.Level

internal class Log4jLogger(private val logger: org.apache.logging.log4j.Logger) : MiraiLoggerPlatformBase() {

    override fun verbose0(message: String?, e: Throwable?) {
        logger.trace(message, e)
    }

    override fun debug0(message: String?, e: Throwable?) {
        logger.debug(message, e)
    }

    override fun info0(message: String?, e: Throwable?) {
        logger.info(message, e)
    }

    override fun warning0(message: String?, e: Throwable?) {
        logger.warn(message, e)
    }

    override fun error0(message: String?, e: Throwable?) {
        logger.error(message, e)
    }

    override val identity: String?
        get() = logger.name

}

internal class Slf4jLogger(private val logger: Logger) : MiraiLoggerPlatformBase() {
    override fun verbose0(message: String?, e: Throwable?) {
        logger.trace(message, e)
    }

    override fun debug0(message: String?, e: Throwable?) {
        logger.debug(message, e)
    }

    override fun info0(message: String?, e: Throwable?) {
        logger.info(message, e)
    }

    override fun warning0(message: String?, e: Throwable?) {
        logger.warn(message, e)
    }

    override fun error0(message: String?, e: Throwable?) {
        logger.error(message, e)
    }

    override val identity: String?
        get() = logger.name
}

internal class JdkLogger(private val logger: java.util.logging.Logger) : MiraiLoggerPlatformBase() {
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