/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads


/**
 * 用于创建默认的日志记录器. 在一些需要使用日志的 Mirai 的组件, 如 [Bot], 都会通过这个函数构造日志记录器.
 *
 * 可直接修改这个变量的值来重定向日志输出.
 *
 * **注意:** 请务必将所有的输出定向到日志记录系统, 否则在某些情况下 (如 web 控制台中) 将无法接收到输出
 *
 * **注意:** 请为日志做好分类, 即不同的模块使用不同的 [MiraiLogger].
 * 如, [Bot] 中使用 `identity` 为 "Bot(qqId)" 的 [MiraiLogger]
 * 而 [Bot] 的网络处理中使用 `identity` 为 "BotNetworkHandler".
 *
 * Java 调用: `Utils.getDefaultLogger().invoke(identity)`
 */
var DefaultLogger: (identity: String?) -> MiraiLogger = { PlatformLogger(it) }

/**
 * 给这个 logger 添加一个开关, 用于控制是否记录 log
 */
@JvmOverloads
fun MiraiLogger.withSwitch(default: Boolean = true): MiraiLoggerWithSwitch = MiraiLoggerWithSwitch(this, default)

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
interface MiraiLogger {
    /**
     * 顶层日志记录器.
     *
     * 顶层日志会导致混乱并难以定位问题. 请自行构造 logger 实例并使用.
     * 请参考使用 [DefaultLogger]
     */
    @Deprecated(message = "顶层日志会导致混乱并难以定位问题. 请自行构造 logger 实例并使用.", level = DeprecationLevel.WARNING)
    companion object : MiraiLogger by DefaultLogger("Mirai")

    /**
     * 日志的标记. 在 Mirai 中, identity 可为
     * - "Bot"
     * - "BotNetworkHandler"
     * 等.
     *
     * 它只用于帮助调试或统计. 十分建议清晰定义 identity
     */
    val identity: String?

    /**
     * 获取 [MiraiLogger] 是否已开启
     *
     * 除 [MiraiLoggerWithSwitch] 可控制开关外, 其他的所有 [MiraiLogger] 均一直开启.
     */
    val isEnabled: Boolean

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
    var follower: MiraiLogger?

    /**
     * 记录一个 `verbose` 级别的日志.
     * 无关紧要的, 经常大量输出的日志应使用它.
     */
    fun verbose(message: String?)

    fun verbose(e: Throwable?) = verbose(null, e)
    fun verbose(message: String?, e: Throwable?)

    /**
     * 记录一个 _调试_ 级别的日志.
     */
    fun debug(message: String?)

    fun debug(e: Throwable?) = debug(null, e)
    fun debug(message: String?, e: Throwable?)


    /**
     * 记录一个 _信息_ 级别的日志.
     */
    fun info(message: String?)

    fun info(e: Throwable?) = info(null, e)
    fun info(message: String?, e: Throwable?)


    /**
     * 记录一个 _警告_ 级别的日志.
     */
    fun warning(message: String?)

    fun warning(e: Throwable?) = warning(null, e)
    fun warning(message: String?, e: Throwable?)


    /**
     * 记录一个 _错误_ 级别的日志.
     */
    fun error(message: String?)

    fun error(e: Throwable?) = error(null, e)
    fun error(message: String?, e: Throwable?)

    /** 根据优先级调用对应函数 */
    fun call(priority: SimpleLogger.LogPriority, message: String? = null, e: Throwable? = null) =
        priority.correspondingFunction(this, message, e)

    /**
     * 添加一个 [follower], 返回 [follower]
     * 它只会把 `this` 的属性 [MiraiLogger.follower] 修改为这个函数的参数 [follower], 然后返回这个参数.
     * 若 [MiraiLogger.follower] 已经有值, 则会替换掉这个值.
     *
     *   +------+      +----------+      +----------+      +----------+
     *   | base | <--  | follower | <--  | follower | <--  | follower |
     *   +------+      +----------+      +----------+      +----------+
     *
     * @return [follower]
     */
    operator fun <T : MiraiLogger> plus(follower: T): T

    /**
     * 添加一个 [follower]
     * 若 [MiraiLogger.follower] 已经有值, 则会对这个值调用 [plusAssign]. 即会在日志记录器链的末尾添加这个参数 [follower]
     *
     * @see follower
     */
    operator fun plusAssign(follower: MiraiLogger)
}


inline fun MiraiLogger.verbose(lazyMessage: () -> String) {
    if (isEnabled) verbose(lazyMessage())
}

inline fun MiraiLogger.verbose(lazyMessage: () -> String, e: Throwable?) {
    if (isEnabled) verbose(lazyMessage(), e)
}

inline fun MiraiLogger.debug(lazyMessage: () -> String?) {
    if (isEnabled) debug(lazyMessage())
}

inline fun MiraiLogger.debug(lazyMessage: () -> String?, e: Throwable?) {
    if (isEnabled) debug(lazyMessage(), e)
}

inline fun MiraiLogger.info(lazyMessage: () -> String?) {
    if (isEnabled) info(lazyMessage())
}

inline fun MiraiLogger.info(lazyMessage: () -> String?, e: Throwable?) {
    if (isEnabled) info(lazyMessage(), e)
}

inline fun MiraiLogger.warning(lazyMessage: () -> String?) {
    if (isEnabled) warning(lazyMessage())
}

inline fun MiraiLogger.warning(lazyMessage: () -> String?, e: Throwable?) {
    if (isEnabled) warning(lazyMessage(), e)
}

inline fun MiraiLogger.error(lazyMessage: () -> String?) {
    if (isEnabled) error(lazyMessage())
}

inline fun MiraiLogger.error(lazyMessage: () -> String?, e: Throwable?) {
    if (isEnabled) error(lazyMessage(), e)
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
 * @see DefaultLogger
 */
expect open class PlatformLogger @JvmOverloads constructor(identity: String? = "Mirai") : MiraiLoggerPlatformBase


/**
 * 不做任何事情的 logger, keep silent.
 */
@Suppress("unused")
object SilentLogger : PlatformLogger() {
    override val identity: String? = null

    override fun error0(message: String?) = Unit
    override fun debug0(message: String?) = Unit
    override fun warning0(message: String?) = Unit
    override fun verbose0(message: String?) = Unit
    override fun info0(message: String?) = Unit

    override fun verbose0(message: String?, e: Throwable?) = Unit
    override fun debug0(message: String?, e: Throwable?) = Unit
    override fun info0(message: String?, e: Throwable?) = Unit
    override fun warning0(message: String?, e: Throwable?) = Unit
    override fun error0(message: String?, e: Throwable?) = Unit
}

/**
 * 简易日志记录, 所有类型日志都会被重定向 [logger]
 */
open class SimpleLogger(
    final override val identity: String?,
    protected open val logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit
) : MiraiLoggerPlatformBase() {

    enum class LogPriority(
        @MiraiExperimentalAPI val nameAligned: String,
        val simpleName: String,
        @MiraiExperimentalAPI val correspondingFunction: MiraiLogger.(message: String?, e: Throwable?) -> Unit
    ) {
        VERBOSE("VERBOSE", "V", MiraiLogger::verbose),
        DEBUG(" DEBUG ", "D", MiraiLogger::debug),
        INFO("  INFO ", "I", MiraiLogger::info),
        WARNING("WARNING", "W", MiraiLogger::warning),
        ERROR(" ERROR ", "E", MiraiLogger::error)
    }

    companion object {
        inline operator fun invoke(crossinline logger: (message: String?, e: Throwable?) -> Unit): SimpleLogger =
            SimpleLogger(null, logger)

        inline operator fun invoke(
            identity: String?,
            crossinline logger: (message: String?, e: Throwable?) -> Unit
        ): SimpleLogger =
            SimpleLogger(identity) { _, message, e ->
                logger(message, e)
            }

        operator fun invoke(logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit): SimpleLogger =
            SimpleLogger(null, logger)
    }

    override fun verbose0(message: String?) = logger(LogPriority.VERBOSE, message, null)
    override fun verbose0(message: String?, e: Throwable?) = logger(LogPriority.VERBOSE, message, e)
    override fun debug0(message: String?) = logger(LogPriority.DEBUG, message, null)
    override fun debug0(message: String?, e: Throwable?) = logger(LogPriority.DEBUG, message, e)
    override fun info0(message: String?) = logger(LogPriority.INFO, message, null)
    override fun info0(message: String?, e: Throwable?) = logger(LogPriority.INFO, message, e)
    override fun warning0(message: String?) = logger(LogPriority.WARNING, message, null)
    override fun warning0(message: String?, e: Throwable?) = logger(LogPriority.WARNING, message, e)
    override fun error0(message: String?) = logger(LogPriority.ERROR, message, null)
    override fun error0(message: String?, e: Throwable?) = logger(LogPriority.ERROR, message, e)
}

/**
 * 带有开关的 Logger. 仅能通过 [MiraiLogger.withSwitch] 构造
 *
 * @see enable 开启
 * @see disable 关闭
 */
@Suppress("MemberVisibilityCanBePrivate")
class MiraiLoggerWithSwitch internal constructor(private val delegate: MiraiLogger, default: Boolean) :
    MiraiLoggerPlatformBase() {
    override val identity: String? get() = delegate.identity

    /**
     * true 为开启.
     */
    @PublishedApi
    internal var switch: Boolean = default

    override val isEnabled: Boolean get() = switch

    fun enable() {
        switch = true
    }

    fun disable() {
        switch = false
    }

    override fun verbose0(message: String?) = delegate.verbose(message)
    override fun verbose0(message: String?, e: Throwable?) = delegate.verbose(message, e)
    override fun debug0(message: String?) = delegate.debug(message)
    override fun debug0(message: String?, e: Throwable?) = delegate.debug(message, e)
    override fun info0(message: String?) = delegate.info(message)
    override fun info0(message: String?, e: Throwable?) = delegate.info(message, e)
    override fun warning0(message: String?) = delegate.warning(message)
    override fun warning0(message: String?, e: Throwable?) = delegate.warning(message, e)
    override fun error0(message: String?) = delegate.error(message)
    override fun error0(message: String?, e: Throwable?) = delegate.error(message, e)
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
abstract class MiraiLoggerPlatformBase : MiraiLogger {
    override val isEnabled: Boolean get() = true
    final override var follower: MiraiLogger? = null

    final override fun verbose(message: String?) {
        if (!isEnabled) return
        follower?.verbose(message)
        verbose0(message)
    }

    final override fun verbose(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.verbose(message, e)
        verbose0(message, e)
    }

    final override fun debug(message: String?) {
        if (!isEnabled) return
        follower?.debug(message)
        debug0(message)
    }

    final override fun debug(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.debug(message, e)
        debug0(message, e)
    }

    final override fun info(message: String?) {
        if (!isEnabled) return
        follower?.info(message)
        info0(message)
    }

    final override fun info(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.info(message, e)
        info0(message, e)
    }

    final override fun warning(message: String?) {
        if (!isEnabled) return
        follower?.warning(message)
        warning0(message)
    }

    final override fun warning(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.warning(message, e)
        warning0(message, e)
    }

    final override fun error(message: String?) {
        if (!isEnabled) return
        follower?.error(message)
        error0(message)
    }

    final override fun error(message: String?, e: Throwable?) {
        if (!isEnabled) return
        follower?.error(message, e)
        error0(message, e)
    }

    protected open fun verbose0(message: String?) = verbose0(message, null)
    protected abstract fun verbose0(message: String?, e: Throwable?)
    protected open fun debug0(message: String?) = debug0(message, null)
    protected abstract fun debug0(message: String?, e: Throwable?)
    protected open fun info0(message: String?) = info0(message, null)
    protected abstract fun info0(message: String?, e: Throwable?)
    protected open fun warning0(message: String?) = warning0(message, null)
    protected abstract fun warning0(message: String?, e: Throwable?)
    protected open fun error0(message: String?) = error0(message, null)
    protected abstract fun error0(message: String?, e: Throwable?)

    override operator fun <T : MiraiLogger> plus(follower: T): T {
        this.follower = follower
        return follower
    }

    override fun plusAssign(follower: MiraiLogger) =
        if (this.follower == null) this.follower = follower
        else this.follower!! += follower
}
