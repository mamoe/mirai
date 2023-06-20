/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

internal fun MiraiLogger.asUtilsLogger(): UtilsLogger = MiraiUtilsLogger(this)

internal class MiraiUtilsLogger(
    private val miraiLogger: MiraiLogger,
) : UtilsLogger {
    override val isVerboseEnabled: Boolean
        get() = miraiLogger.isVerboseEnabled
    override val isDebugEnabled: Boolean
        get() = miraiLogger.isDebugEnabled
    override val isInfoEnabled: Boolean
        get() = miraiLogger.isInfoEnabled
    override val isWarningEnabled: Boolean
        get() = miraiLogger.isWarningEnabled
    override val isErrorEnabled: Boolean
        get() = miraiLogger.isErrorEnabled

    override fun verbose(message: String?, e: Throwable?) {
        miraiLogger.verbose(message, e)
    }

    override fun debug(message: String?, e: Throwable?) {
        miraiLogger.debug(message, e)
    }

    override fun info(message: String?, e: Throwable?) {
        miraiLogger.info(message, e)
    }

    override fun warning(message: String?, e: Throwable?) {
        miraiLogger.warning(message, e)
    }

    override fun error(message: String?, e: Throwable?) {
        miraiLogger.error(message, e)
    }
}