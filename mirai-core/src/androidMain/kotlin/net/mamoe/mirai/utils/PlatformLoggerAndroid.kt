/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import android.util.Log

/**
 * Android 平台的默认的日志记录器, 使用 [Log]
 * 不应该直接构造这个类的实例. 需使用 [DefaultLogger]
 */
actual open class PlatformLogger actual constructor(override val identity: String?) : MiraiLoggerPlatformBase() {
    override fun verbose0(any: Any?) {
        Log.v(identity, any?.toString() ?: "")
    }

    override fun verbose0(message: String?, e: Throwable?) {
        Log.v(identity, message ?: "", e)
    }

    override fun debug0(any: Any?) {
        Log.d(identity, any?.toString() ?: "")
    }

    override fun debug0(message: String?, e: Throwable?) {
        Log.d(identity, message ?: "", e)
    }

    override fun info0(any: Any?) {
        Log.i(identity, any?.toString() ?: "")
    }

    override fun info0(message: String?, e: Throwable?) {
        Log.i(identity, message ?: "", e)
    }

    override fun warning0(any: Any?) {
        Log.w(identity, any?.toString() ?: "")
    }

    override fun warning0(message: String?, e: Throwable?) {
        Log.w(identity, message ?: "", e)
    }

    override fun error0(any: Any?) {
        Log.e(identity, any?.toString() ?: "")
    }

    override fun error0(message: String?, e: Throwable?) {
        Log.e(identity, message ?: "", e)
    }
}