/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package android.util

import net.mamoe.mirai.internal.utils.StdoutLogger

// Dummy implementation for tests, since we don't have an Android SDK

@Suppress("UNUSED_PARAMETER", "unused")
object Log {
    const val VERBOSE = 2
    const val DEBUG = 3
    const val INFO = 4
    const val WARN = 5
    const val ERROR = 6
    const val ASSERT = 7

    private val stdout = StdoutLogger("AndroidLog")

    @JvmStatic
    fun v(tag: String?, msg: String?): Int {
        stdout.verbose(msg)
        return 0
    }

    @JvmStatic
    fun v(tag: String?, msg: String?, tr: Throwable?): Int {
        stdout.verbose(msg, tr)
        return 0
    }

    @JvmStatic
    fun d(tag: String?, msg: String?): Int {
        stdout.debug(msg, tr)
        return 0
    }

    @JvmStatic
    fun d(tag: String?, msg: String?, tr: Throwable?): Int {
        stdout.debug(msg, tr)
        return 0
    }

    @JvmStatic
    fun i(tag: String?, msg: String?): Int {
        stdout.info(msg, tr)
        return 0
    }

    @JvmStatic
    fun i(tag: String?, msg: String?, tr: Throwable?): Int {
        stdout.info(msg, tr)
        return 0
    }

    @JvmStatic
    fun w(tag: String?, msg: String?): Int {
        stdout.warning(msg, tr)
        return 0
    }

    @JvmStatic
    fun w(tag: String?, msg: String?, tr: Throwable?): Int {
        stdout.warning(msg, tr)
        return 0
    }

    @JvmStatic
    fun w(tag: String?, tr: Throwable?): Int {
        stdout.warning(msg, tr)
        return 0
    }

    @JvmStatic
    fun e(tag: String?, msg: String?): Int {
        stdout.error(msg, tr)
        return 0
    }

    @JvmStatic
    fun e(tag: String?, msg: String?, tr: Throwable?): Int {
        stdout.error(msg, tr)
        return 0
    }

    @JvmStatic
    fun wtf(tag: String?, msg: String?): Int {
        stdout.error(msg, tr)
        return 0
    }

    @JvmStatic
    fun wtf(tag: String?, tr: Throwable?): Int {
        stdout.error(msg, tr)
        return 0
    }

    @JvmStatic
    fun wtf(tag: String?, msg: String?, tr: Throwable?): Int {
        stdout.error(msg, tr)
        return 0
    }

    @JvmStatic
    fun getStackTraceString(tr: Throwable): String {
        return tr.stackTraceToString()
    }

    @JvmStatic
    fun println(priority: Int, tag: String?, msg: String?): Int {
        stdout.info(msg, tr)
        return 0
    }


    private inline val tr get() = null
    private inline val msg get() = null
}
