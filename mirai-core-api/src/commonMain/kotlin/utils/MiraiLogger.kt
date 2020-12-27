/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

/**
 * 给这个 logger 添加一个开关, 用于控制是否记录 log
 */
@JvmOverloads
public fun MiraiLogger.withSwitch(default: Boolean = true): MiraiLoggerWithSwitch = MiraiLoggerWithSwitch(this, default)

/**
 * 日志记录器. 所有的输出均依赖于它.
 * 不同的对象可拥有只属于自己的 logger. 通过 [identity] 来区分.
 *
 * 注意: 如果你需要重新实现日志, 请不要直接实现这个接口, 请继承 [MiraiLoggerPlatformBase]
 *
 * 在定义 logger 变量时, 请一直使用 [MiraiLogger] 或者 [MiraiLoggerWithSwitch].
 *
 * Mirai 内建三种日志实现, 分别是 [SimpleLogger], [PlatformLogger], [SilentLogger]
 *
 * @see SimpleLogger 简易 logger, 它将所有的日志记录操作都转移给 lambda `(String?, Throwable?) -> Unit`
 * @see PlatformLogger 各个平台下的默认日志记录实现.
 * @see SilentLogger 忽略任何日志记录操作的 logger 实例.
 *
 * @see MiraiLoggerPlatformBase 平台通用基础实现. 若 Mirai 自带的日志系统无法满足需求, 请继承这个类并实现其抽象函数.
 */
public interface MiraiLogger {

    public companion object {
        /**
         * 顶层日志, 仅供 Mirai 内部使用.
         */
        @MiraiInternalApi
        @MiraiExperimentalApi
        public val TopLevel: MiraiLogger by lazy { create("Mirai") }

        @Volatile
        private var defaultLogger: (identity: String?) -> MiraiLogger = { PlatformLogger(it) }

        /**
         * 可直接修改这个变量的值来重定向日志输出.
         */
        @JvmStatic
        public fun setDefaultLoggerCreator(creator: (identity: String?) -> MiraiLogger) {
            defaultLogger = creator
        }

        /**
         * 用于创建默认的日志记录器. 在一些需要使用日志的 Mirai 的组件, 如 [Bot], 都会通过这个函数构造日志记录器.
         *
         * **注意:** 请务必将所有的输出定向到日志记录系统, 否则在某些情况下 (如 web 控制台中) 将无法接收到输出
         *
         * **注意:** 请为日志做好分类, 即不同的模块使用不同的 [MiraiLogger].
         * 如, [Bot] 中使用 `identity` 为 "Bot(qqId)" 的 [MiraiLogger]
         * 而 [Bot] 的网络处理中使用 `identity` 为 "BotNetworkHandler".
         *
         * @see setDefaultLoggerCreator
         */
        @JvmStatic
        public fun create(identity: String?): MiraiLogger {
            return defaultLogger.invoke(identity)
        }
    }

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
     * 随从. 在 this 中调用所有方法后都应继续往 [follower] 传递调用.
     * [follower] 的存在可以让一次日志被多个日志记录器记录.
     *
     * 一般不建议直接修改这个属性. 请通过 [plus] 来连接两个日志记录器.
     * 如: `val logger = bot.logger + MyLogger()`
     * 当调用 `logger.info()` 时, `bot.logger` 会首先记录, `MyLogger` 会随后记录.
     *
     * 当然, 多个 logger 也可以加在一起: `val logger = bot.logger + MynLogger() + MyLogger2()`
     */
    public var follower: MiraiLogger?

    /**
     * 记录一个 `verbose` 级别的日志.
     * 无关紧要的, 经常大量输出的日志应使用它.
     */
    public fun verbose(message: String?)

    public fun verbose(e: Throwable?): Unit = verbose(null, e)
    public fun verbose(message: String?, e: Throwable?)

    /**
     * 记录一个 _调试_ 级别的日志.
     */
    public fun debug(message: String?)

    public fun debug(e: Throwable?): Unit = debug(null, e)
    public fun debug(message: String?, e: Throwable?)


    /**
     * 记录一个 _信息_ 级别的日志.
     */
    public fun info(message: String?)

    public fun info(e: Throwable?): Unit = info(null, e)
    public fun info(message: String?, e: Throwable?)


    /**
     * 记录一个 _警告_ 级别的日志.
     */
    public fun warning(message: String?)

    public fun warning(e: Throwable?): Unit = warning(null, e)
    public fun warning(message: String?, e: Throwable?)


    /**
     * 记录一个 _错误_ 级别的日志.
     */
    public fun error(message: String?)

    public fun error(e: Throwable?): Unit = error(null, e)
    public fun error(message: String?, e: Throwable?)

    /** 根据优先级调用对应函数 */
    public fun call(priority: SimpleLogger.LogPriority, message: String? = null, e: Throwable? = null): Unit =
        priority.correspondingFunction(this, message, e)

    /**
     * 添加一个 [follower], 返回 [follower]
     * 它只会把 `this` 的属性 [MiraiLogger.follower] 修改为这个函数的参数 [follower], 然后返回这个参数.
     * 若 [MiraiLogger.follower] 已经有值, 则会替换掉这个值.
     * ```
     *   +------+      +----------+      +----------+      +----------+
     *   | base | <--  | follower | <--  | follower | <--  | follower |
     *   +------+      +----------+      +----------+      +----------+
     * ```
     *
     * @return [follower]
     */
    public operator fun <T : MiraiLogger> plus(follower: T): T
}


public inline fun MiraiLogger.verbose(message: () -> String) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) verbose(message())
}

public inline fun MiraiLogger.verbose(message: () -> String, e: Throwable?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) verbose(message(), e)
}

public inline fun MiraiLogger.debug(message: () -> String?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) debug(message())
}

public inline fun MiraiLogger.debug(message: () -> String?, e: Throwable?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) debug(message(), e)
}

public inline fun MiraiLogger.info(message: () -> String?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) info(message())
}

public inline fun MiraiLogger.info(message: () -> String?, e: Throwable?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) info(message(), e)
}

public inline fun MiraiLogger.warning(message: () -> String?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) warning(message())
}

public inline fun MiraiLogger.warning(message: () -> String?, e: Throwable?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) warning(message(), e)
}

public inline fun MiraiLogger.error(message: () -> String?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) error(message())
}

public inline fun MiraiLogger.error(message: () -> String?, e: Throwable?) {
    contract { callsInPlace(message, AT_MOST_ONCE) }
    if (isEnabled) error(message(), e)
}

/**
 * 当前平台的默认的日志记录器.
 * 在 _JVM 控制台_ 端的实现为 [println]
 * 在 _Android_ 端的实现为 `android.util.Log`
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
 * 2020-05-21 19:51:09 V/Bot 1994701021: Send: OidbSvc.0x88d_7
 * ```
 *
 * 日期时间格式为 `yyyy-MM-dd HH:mm:ss`,
 *
 * 严重程度为 V, I, W, E. 分别对应 verbose, info, warning, error
 *
 * @see MiraiLogger.create
 */
@MiraiInternalApi
public expect open class PlatformLogger constructor(
    identity: String? = "Mirai",
    output: (String) -> Unit, // TODO: 2020/11/30 review logs, currently it's just for compile
) : MiraiLoggerPlatformBase {
    @JvmOverloads
    public constructor(identity: String? = "Mirai")
}


/**
 * 不做任何事情的 logger, keep silent.
 */
@Suppress("unused")
public object SilentLogger : PlatformLogger() {
    public override val identity: String? = null

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
 * 日志基类. 实现了 [follower] 的调用传递.
 * 若 Mirai 自带的日志系统无法满足需求, 请继承这个类或 [PlatformLogger] 并实现其抽象函数.
 *
 * 这个类不应该被用作变量的类型定义. 只应被作为继承对象.
 * 在定义 logger 变量时, 请一直使用 [MiraiLogger] 或者 [MiraiLoggerWithSwitch].
 *
 * @see PlatformLogger
 * @see SimpleLogger
 */
public abstract class MiraiLoggerPlatformBase : MiraiLogger {
    public override val isEnabled: Boolean get() = true
    public final override var follower: MiraiLogger? = null

    public final override fun verbose(message: String?) {
        if (!isEnabled) return
        follower?.verbose(message)
        verbose0(message)
    }

    public final override fun verbose(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.verbose(message, e)
        verbose0(message, e)
    }

    public final override fun debug(message: String?) {
        if (!isEnabled) return
        follower?.debug(message)
        debug0(message)
    }

    public final override fun debug(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.debug(message, e)
        debug0(message, e)
    }

    public final override fun info(message: String?) {
        if (!isEnabled) return
        follower?.info(message)
        info0(message)
    }

    public final override fun info(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.info(message, e)
        info0(message, e)
    }

    public final override fun warning(message: String?) {
        if (!isEnabled) return
        follower?.warning(message)
        warning0(message)
    }

    public final override fun warning(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.warning(message, e)
        warning0(message, e)
    }

    public final override fun error(message: String?) {
        if (!isEnabled) return
        follower?.error(message)
        error0(message)
    }

    public final override fun error(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.error(message, e)
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

    public override operator fun <T : MiraiLogger> plus(follower: T): T {
        this.follower = follower
        return follower
    }
}
