package net.mamoe.mirai.utils

import android.util.Log

actual typealias PlatformLogger = AndroidLogger

/**
 * Android 平台的默认的日志记录器, 使用 [Log]
 * 不应该直接构造这个类的实例. 需使用 [DefaultLogger]
 */
open class AndroidLogger internal constructor(override val identity: String?) : MiraiLoggerPlatformBase() {
    override fun verbose0(any: Any?) {
        Log.v(identity, any.toString())
    }

    override fun verbose0(message: String?, e: Throwable?) {
        Log.v(identity, message.toString(), e)
    }

    override fun debug0(any: Any?) {
        Log.d(identity, any.toString())
    }

    override fun debug0(message: String?, e: Throwable?) {
        Log.d(identity, message.toString(), e)
    }

    override fun info0(any: Any?) {
        Log.i(identity, any.toString())
    }

    override fun info0(message: String?, e: Throwable?) {
        Log.i(identity, message.toString(), e)
    }

    override fun warning0(any: Any?) {
        Log.w(identity, any.toString())
    }

    override fun warning0(message: String?, e: Throwable?) {
        Log.w(identity, message.toString(), e)
    }

    override fun error0(any: Any?) {
        Log.e(identity, any.toString())
    }

    override fun error0(message: String?, e: Throwable?) {
        Log.e(identity, message.toString(), e)
    }
}