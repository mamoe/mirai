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

import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase

internal class MiraiConsoleLoggerUnused(
    val controller: MiraiConsoleLoggerController,
    override val identity: String?
) : MiraiLoggerPlatformBase() {
    internal object InitializeLock

    internal lateinit var delegateLogger: MiraiDelegateLogger
    private val logger: MiraiLogger by lazy {
        val logger = if (controller.cacheLoggers) {
            val reference = controller.getLoggerRegistration(identity)
            if (reference.compareAndSet(null, InitializeLock)) {
                val logger = MiraiConsoleLogger(controller, controller.newLogger(identity))
                reference.set(logger)
                logger
            } else {
                // initialized/initializing
                val logger: MiraiLogger
                while (true) {
                    val status = reference.get()
                    if (status is MiraiLogger) {
                        logger = status
                        break
                    }
                }
                logger
            }
        } else {
            MiraiConsoleLogger(controller, controller.newLogger(identity))
        }
        delegateLogger.logger = logger
        logger
    }
    override val isEnabled: Boolean get() = logger.isEnabled

    override fun debug0(message: String?, e: Throwable?) = logger.debug(message, e)
    override fun error0(message: String?, e: Throwable?) = logger.error(message, e)
    override fun info0(message: String?, e: Throwable?) = logger.info(message, e)
    override fun verbose0(message: String?, e: Throwable?) = logger.verbose(message, e)
    override fun warning0(message: String?, e: Throwable?) = logger.warning(message, e)

}