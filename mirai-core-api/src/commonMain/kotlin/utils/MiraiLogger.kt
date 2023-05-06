/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

/**
 * 给这个 logger 添加一个开关, 用于控制是否记录 log
 */
@JvmOverloads
public fun MiraiLogger.withSwitch(default: Boolean = true): MiraiLoggerWithSwitch = MiraiLoggerWithSwitch(this, default)

/**
 * 日志记录器.
 *
 * ## Mirai 日志系统
 *
 * Mirai 内建简单的日志系统, 即 [MiraiLogger]. [MiraiLogger] 的实现有 [SimpleLogger], [PlatformLogger], [SilentLogger].
 *
 * ## 实现或使用 [MiraiLogger]
 *
 * 不建议实现或使用 [MiraiLogger]. 请优先考虑使用上述第三方框架. [MiraiLogger] 仅应用于兼容旧版本代码.
 *
 * @see SimpleLogger 简易 logger, 它将所有的日志记录操作都转移给 lambda `(String?, Throwable?) -> Unit`
 * @see PlatformLogger 各个平台下的默认日志记录实现.
 * @see SilentLogger 忽略任何日志记录操作的 logger 实例.
 *
 * @see MiraiLoggerPlatformBase 平台通用基础实现. 若 Mirai 自带的日志系统无法满足需求, 请继承这个类并实现其抽象函数.
 */
public expect interface MiraiLogger {

    /**
     * 可以 service 实现的方式覆盖.
     *
     * @since 2.7
     */
    public interface Factory {
        /**
         * 创建 [MiraiLogger] 实例.
         *
         * @param requester 请求创建 [MiraiLogger] 的对象的 class
         * @param identity 对象标记 (备注)
         */
        public open fun create(requester: KClass<*>, identity: String? = null): MiraiLogger

        /**
         * 创建 [MiraiLogger] 实例.
         *
         * @param requester 请求创建 [MiraiLogger] 的对象
         */
        public open fun create(requester: KClass<*>): MiraiLogger

        public companion object INSTANCE : Factory
    }

    public companion object;

    /**
     * 日志的标记. 在 Mirai 中, identity 可为
     * - "Bot"
     * - "BotNetworkHandler"
     * 等.
     *
     * 它只用于帮助调试或统计. 十分建议清晰定义 identity
     */
    public val identity: String?

    /**
     * 获取 [MiraiLogger] 是否已开启
     *
     * 除 [MiraiLoggerWithSwitch] 可控制开关外, 其他的所有 [MiraiLogger] 均一直开启.
     */
    public val isEnabled: Boolean

    /**
     * 当 VERBOSE 级别的日志启用时返回 `true`.
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public open val isVerboseEnabled: Boolean

    /**
     * 当 DEBUG 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public open val isDebugEnabled: Boolean

    /**
     * 当 INFO 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public open val isInfoEnabled: Boolean

    /**
     * 当 WARNING 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public open val isWarningEnabled: Boolean

    /**
     * 当 ERROR 级别的日志启用时返回 `true`
     *
     * 若 [isEnabled] 为 `false`, 返回 `false`.
     * 其他情况下返回 [isEnabled] 的值.
     *
     * @since 2.7
     */
    public open val isErrorEnabled: Boolean

    /**
     * 记录一个 `verbose` 级别的日志.
     * 无关紧要的, 经常大量输出的日志应使用它.
     */
    public fun verbose(message: String?)

    public open fun verbose(e: Throwable?)
    public fun verbose(message: String?, e: Throwable?)

    /**
     * 记录一个 _调试_ 级别的日志.
     */
    public fun debug(message: String?)

    public open fun debug(e: Throwable?)
    public fun debug(message: String?, e: Throwable?)


    /**
     * 记录一个 _信息_ 级别的日志.
     */
    public fun info(message: String?)

    public open fun info(e: Throwable?)
    public fun info(message: String?, e: Throwable?)


    /**
     * 记录一个 _警告_ 级别的日志.
     */
    public fun warning(message: String?)

    public open fun warning(e: Throwable?)
    public fun warning(message: String?, e: Throwable?)


    /**
     * 记录一个 _错误_ 级别的日志.
     */
    public fun error(message: String?)

    public open fun error(e: Throwable?)
    public fun error(message: String?, e: Throwable?)

    /** 根据优先级调用对应函数 */
    public open fun call(priority: SimpleLogger.LogPriority, message: String? = null, e: Throwable? = null)
}


public inline fun MiraiLogger.verbose(message: () -> String) {
    if (isVerboseEnabled) verbose(message())
}

public inline fun MiraiLogger.verbose(message: () -> String, e: Throwable?) {
    if (isVerboseEnabled) verbose(message(), e)
}

public inline fun MiraiLogger.debug(message: () -> String?) {
    if (isDebugEnabled) debug(message())
}

public inline fun MiraiLogger.debug(message: () -> String?, e: Throwable?) {
    if (isDebugEnabled) debug(message(), e)
}

public inline fun MiraiLogger.info(message: () -> String?) {
    if (isInfoEnabled) info(message())
}

public inline fun MiraiLogger.info(message: () -> String?, e: Throwable?) {
    if (isInfoEnabled) info(message(), e)
}

public inline fun MiraiLogger.warning(message: () -> String?) {
    if (isWarningEnabled) warning(message())
}

public inline fun MiraiLogger.warning(message: () -> String?, e: Throwable?) {
    if (isWarningEnabled) warning(message(), e)
}

public inline fun MiraiLogger.error(message: () -> String?) {
    if (isErrorEnabled) error(message())
}

public inline fun MiraiLogger.error(message: () -> String?, e: Throwable?) {
    if (isErrorEnabled) error(message(), e)
}

/**
 * 当前平台的默认的日志记录器.
 * - 在 _JVM 控制台_ 端的实现为 [println]
 * - 在 _Android_ 端的实现为 `android.util.Log`
 *
 *
 * 单条日志格式 (正则) 为:
 * ```regex
 * ^([\w-]*\s[\w:]*)\s(\w)\/(.*?):\s(.+)$
 * ```
 * 其中 group 分别为: 日期与时间, 严重程度, [identity], 消息内容.
 *
 * 示例:
 * ```log
 * 2020-05-21 19:51:09 V/Bot 123456789: Send: OidbSvc.0x88d_7
 * ```
 *
 * 日期时间格式为 `yyyy-MM-dd HH:mm:ss`,
 *
 * 严重程度为 V, I, W, E. 分别对应 verbose, info, warning, error
 *
 * @see MiraiLogger.Factory.create
 */
@MiraiInternalApi
public expect open class PlatformLogger @JvmOverloads constructor(
    identity: String? = "Mirai",
) : MiraiLoggerPlatformBase

/**
 * 不做任何事情的 logger, keep silent.
 */
@OptIn(MiraiInternalApi::class)
@Suppress("unused")
public object SilentLogger : PlatformLogger() {
    public override val identity: String? = null

    override val isEnabled: Boolean
        get() = false
    override val isVerboseEnabled: Boolean
        get() = false
    override val isDebugEnabled: Boolean
        get() = false
    override val isInfoEnabled: Boolean
        get() = false
    override val isWarningEnabled: Boolean
        get() = false
    override val isErrorEnabled: Boolean
        get() = false

    public override fun error0(message: String?): Unit = Unit
    public override fun debug0(message: String?): Unit = Unit
    public override fun warning0(message: String?): Unit = Unit
    public override fun verbose0(message: String?): Unit = Unit
    public override fun info0(message: String?): Unit = Unit

    public override fun verbose0(message: String?, e: Throwable?): Unit = Unit
    public override fun debug0(message: String?, e: Throwable?): Unit = Unit
    public override fun info0(message: String?, e: Throwable?): Unit = Unit
    public override fun warning0(message: String?, e: Throwable?): Unit = Unit
    public override fun error0(message: String?, e: Throwable?): Unit = Unit
}

/**
 * 简易日志记录, 所有类型日志都会被重定向 [logger]
 */
public open class SimpleLogger(
    public final override val identity: String?,
    protected open val logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit
) : MiraiLoggerPlatformBase() {

    public enum class LogPriority(
        @MiraiExperimentalApi public val nameAligned: String,
        public val simpleName: String,
        @MiraiExperimentalApi public val correspondingFunction: MiraiLogger.(message: String?, e: Throwable?) -> Unit
    ) {
        VERBOSE("VERBOSE", "V", MiraiLogger::verbose),
        DEBUG(" DEBUG ", "D", MiraiLogger::debug),
        INFO("  INFO ", "I", MiraiLogger::info),
        WARNING("WARNING", "W", MiraiLogger::warning),
        ERROR(" ERROR ", "E", MiraiLogger::error)
    }

    public companion object {
        public inline operator fun invoke(crossinline logger: (message: String?, e: Throwable?) -> Unit): SimpleLogger =
            SimpleLogger(null, logger)

        public inline operator fun invoke(
            identity: String?,
            crossinline logger: (message: String?, e: Throwable?) -> Unit
        ): SimpleLogger =
            SimpleLogger(identity) { _, message, e ->
                logger(message, e)
            }

        public operator fun invoke(logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit): SimpleLogger =
            SimpleLogger(null, logger)
    }

    public override fun verbose0(message: String?): Unit = logger(LogPriority.VERBOSE, message, null)
    public override fun verbose0(message: String?, e: Throwable?): Unit = logger(LogPriority.VERBOSE, message, e)
    public override fun debug0(message: String?): Unit = logger(LogPriority.DEBUG, message, null)
    public override fun debug0(message: String?, e: Throwable?): Unit = logger(LogPriority.DEBUG, message, e)
    public override fun info0(message: String?): Unit = logger(LogPriority.INFO, message, null)
    public override fun info0(message: String?, e: Throwable?): Unit = logger(LogPriority.INFO, message, e)
    public override fun warning0(message: String?): Unit = logger(LogPriority.WARNING, message, null)
    public override fun warning0(message: String?, e: Throwable?): Unit = logger(LogPriority.WARNING, message, e)
    public override fun error0(message: String?): Unit = logger(LogPriority.ERROR, message, null)
    public override fun error0(message: String?, e: Throwable?): Unit = logger(LogPriority.ERROR, message, e)
}

/**
 * 带有开关的 Logger. 仅能通过 [MiraiLogger.withSwitch] 构造
 *
 * @see enable 开启
 * @see disable 关闭
 */
@Suppress("MemberVisibilityCanBePrivate")
public class MiraiLoggerWithSwitch internal constructor(private val delegate: MiraiLogger, default: Boolean) :
    MiraiLoggerPlatformBase() {
    public override val identity: String? get() = delegate.identity

    /**
     * true 为开启.
     */
    @PublishedApi
    internal var switch: Boolean = default

    public override val isEnabled: Boolean get() = switch

    public fun enable() {
        switch = true
    }

    public fun disable() {
        switch = false
    }

    public override fun verbose0(message: String?): Unit = delegate.verbose(message)
    public override fun verbose0(message: String?, e: Throwable?): Unit = delegate.verbose(message, e)
    public override fun debug0(message: String?): Unit = delegate.debug(message)
    public override fun debug0(message: String?, e: Throwable?): Unit = delegate.debug(message, e)
    public override fun info0(message: String?): Unit = delegate.info(message)
    public override fun info0(message: String?, e: Throwable?): Unit = delegate.info(message, e)
    public override fun warning0(message: String?): Unit = delegate.warning(message)
    public override fun warning0(message: String?, e: Throwable?): Unit = delegate.warning(message, e)
    public override fun error0(message: String?): Unit = delegate.error(message)
    public override fun error0(message: String?, e: Throwable?): Unit = delegate.error(message, e)
}

/**
 * 日志基类.
 * 若 Mirai 自带的日志系统无法满足需求, 请继承这个类或 [PlatformLogger] 并实现其抽象函数.
 *
 * 这个类不应该被用作变量的类型定义. 只应被作为继承对象.
 * 在定义 logger 变量时, 请一直使用 [MiraiLogger] 或者 [MiraiLoggerWithSwitch].
 *
 * @see PlatformLogger
 * @see SimpleLogger
 */
@Suppress("DEPRECATION_ERROR")
public abstract class MiraiLoggerPlatformBase : MiraiLogger {
    public override val isEnabled: Boolean get() = true

    public final override fun verbose(message: String?) {
        if (!isEnabled) return
        verbose0(message)
    }

    public final override fun verbose(message: String?, e: Throwable?) {
        if (!isEnabled) return
        verbose0(message, e)
    }

    public final override fun debug(message: String?) {
        if (!isEnabled) return
        debug0(message)
    }

    public final override fun debug(message: String?, e: Throwable?) {
        if (!isEnabled) return
        debug0(message, e)
    }

    public final override fun info(message: String?) {
        if (!isEnabled) return
        info0(message)
    }

    public final override fun info(message: String?, e: Throwable?) {
        if (!isEnabled) return
        info0(message, e)
    }

    public final override fun warning(message: String?) {
        if (!isEnabled) return
        warning0(message)
    }

    public final override fun warning(message: String?, e: Throwable?) {
        if (!isEnabled) return
        warning0(message, e)
    }

    public final override fun error(message: String?) {
        if (!isEnabled) return
        error0(message)
    }

    public final override fun error(message: String?, e: Throwable?) {
        if (!isEnabled) return
        error0(message, e)
    }

    protected open fun verbose0(message: String?): Unit = verbose0(message, null)
    protected abstract fun verbose0(message: String?, e: Throwable?)
    protected open fun debug0(message: String?): Unit = debug0(message, null)
    protected abstract fun debug0(message: String?, e: Throwable?)
    protected open fun info0(message: String?): Unit = info0(message, null)
    protected abstract fun info0(message: String?, e: Throwable?)
    protected open fun warning0(message: String?): Unit = warning0(message, null)
    protected abstract fun warning0(message: String?, e: Throwable?)
    protected open fun error0(message: String?): Unit = error0(message, null)
    protected abstract fun error0(message: String?, e: Throwable?)
}