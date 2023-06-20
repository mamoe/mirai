/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

/**
 * Mirror of `MiraiLogger`, to be used in utils module.
 */
public interface UtilsLogger {
    /**
     * 当 VERBOSE 级别的日志启用时返回 `true`.
     */
    public val isVerboseEnabled: Boolean

    /**
     * 当 DEBUG 级别的日志启用时返回 `true`
     */
    public val isDebugEnabled: Boolean

    /**
     * 当 INFO 级别的日志启用时返回 `true`
     */
    public val isInfoEnabled: Boolean

    /**
     * 当 WARNING 级别的日志启用时返回 `true`
     */
    public val isWarningEnabled: Boolean

    /**
     * 当 ERROR 级别的日志启用时返回 `true`
     */
    public val isErrorEnabled: Boolean


    /**
     * 记录一个 `verbose` 级别的日志.
     * 无关紧要的, 经常大量输出的日志应使用它.
     */
    public fun verbose(message: String?, e: Throwable? = null)

    /**
     * 记录一个 _调试_ 级别的日志.
     */
    public fun debug(message: String?, e: Throwable? = null)

    /**
     * 记录一个 _信息_ 级别的日志.
     */
    public fun info(message: String?, e: Throwable? = null)

    /**
     * 记录一个 _警告_ 级别的日志.
     */
    public fun warning(message: String?, e: Throwable? = null)

    /**
     * 记录一个 _错误_ 级别的日志.
     */
    public fun error(message: String?, e: Throwable? = null)

    public companion object {
        @OptIn(TestOnly::class)
        private val noop: UtilsLogger by lazy {
            SimpleUtilsLogger().apply {
                isDebugEnabled = false
                isErrorEnabled = false
                isInfoEnabled = false
                isWarningEnabled = false
                isVerboseEnabled = false
            }
        }

        public fun noop(): UtilsLogger = noop
    }
}

public fun UtilsLogger.info(e: Throwable?) {
    info(null, e)
}

public fun UtilsLogger.error(e: Throwable?) {
    error(null, e)
}

public fun UtilsLogger.warning(e: Throwable?) {
    warning(null, e)
}

public fun UtilsLogger.debug(e: Throwable?) {
    debug(null, e)
}

public fun UtilsLogger.verbose(e: Throwable?) {
    verbose(null, e)
}


public inline fun UtilsLogger.verbose(message: () -> String) {
    if (isVerboseEnabled) verbose(message())
}

public inline fun UtilsLogger.verbose(message: () -> String, e: Throwable?) {
    if (isVerboseEnabled) verbose(message(), e)
}

public inline fun UtilsLogger.debug(message: () -> String?) {
    if (isDebugEnabled) debug(message())
}

public inline fun UtilsLogger.debug(message: () -> String?, e: Throwable?) {
    if (isDebugEnabled) debug(message(), e)
}

public inline fun UtilsLogger.info(message: () -> String?) {
    if (isInfoEnabled) info(message())
}

public inline fun UtilsLogger.info(message: () -> String?, e: Throwable?) {
    if (isInfoEnabled) info(message(), e)
}

public inline fun UtilsLogger.warning(message: () -> String?) {
    if (isWarningEnabled) warning(message())
}

public inline fun UtilsLogger.warning(message: () -> String?, e: Throwable?) {
    if (isWarningEnabled) warning(message(), e)
}

public inline fun UtilsLogger.error(message: () -> String?) {
    if (isErrorEnabled) error(message())
}

public inline fun UtilsLogger.error(message: () -> String?, e: Throwable?) {
    if (isErrorEnabled) error(message(), e)
}

@TestOnly
public open class SimpleUtilsLogger
@TestOnly
public constructor() : UtilsLogger {
    override var isVerboseEnabled: Boolean = true
    override var isDebugEnabled: Boolean = true
    override var isInfoEnabled: Boolean = true
    override var isWarningEnabled: Boolean = true
    override var isErrorEnabled: Boolean = true

    override fun verbose(message: String?, e: Throwable?) {
        if (!isVerboseEnabled) return
        println("[V] $message")
        e?.printStackTrace()
    }

    override fun debug(message: String?, e: Throwable?) {
        if (!isDebugEnabled) return
        println("[D] $message")
        e?.printStackTrace()
    }

    override fun info(message: String?, e: Throwable?) {
        if (!isInfoEnabled) return
        println("[I] $message")
        e?.printStackTrace()
    }

    override fun warning(message: String?, e: Throwable?) {
        if (!isWarningEnabled) return
        println("[W] $message")
        e?.printStackTrace()
    }

    override fun error(message: String?, e: Throwable?) {
        if (!isErrorEnabled) return
        println("[E] $message")
        e?.printStackTrace()
    }
}
