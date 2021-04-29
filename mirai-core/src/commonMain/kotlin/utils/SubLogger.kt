/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.*

internal fun MiraiLogger.subLogger(name: String): MiraiLogger {
    return SubLogger(name, this)
}

private class SubLogger(
    private val name: String,
    private val main: MiraiLogger,
) : MiraiLoggerPlatformBase() {
    override val identity: String? get() = main.identity
    override val isEnabled: Boolean get() = main.isEnabled

    override fun verbose0(message: String?, e: Throwable?) {
        if (message != null) {
            main.verbose({ "[$name] $message" }, e)
        } else {
            main.verbose(null, e)
        }
    }

    override fun debug0(message: String?, e: Throwable?) {
        if (message != null) {
            main.debug({ "[$name] $message" }, e)
        } else {
            main.debug(null, e)
        }
    }

    override fun info0(message: String?, e: Throwable?) {
        if (message != null) {
            main.info({ "[$name] $message" }, e)
        } else {
            main.info(null, e)
        }
    }

    override fun warning0(message: String?, e: Throwable?) {
        if (message != null) {
            main.warning({ "[$name] $message" }, e)
        } else {
            main.warning(null, e)
        }
    }

    override fun error0(message: String?, e: Throwable?) {
        if (message != null) {
            main.error({ "[$name] $message" }, e)
        } else {
            main.error(null, e)
        }
    }
}