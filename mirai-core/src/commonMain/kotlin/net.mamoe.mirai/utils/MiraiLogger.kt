/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import kotlin.jvm.JvmOverloads


/**
 * 用于创建默认的日志记录器. 在一些需要使用日志的 Mirai 的组件, 如 [Bot], 都会通过这个函数构造日志记录器.
 * 可直接修改这个变量的值来重定向日志输出.
 *
 * **注意:** 请务必将所有的输出定向到日志记录系统, 否则在某些情况下 (如 web 控制台中) 将无法接收到输出
 *
 * **注意:** 请为日志做好分类, 即不同的模块使用不同的 [MiraiLogger].
 * 如, [Bot] 中使用 identity 为 "Bot(qqId)" 的 [MiraiLogger]
 * 而 [Bot] 的网络处理中使用 identity 为 "BotNetworkHandler" 的.
 */
var DefaultLogger: (identity: String?) -> MiraiLogger = { PlatformLogger(it) }

/**
 * 给这个 logger 添加一个开关, 用于控制是否记录 log
 *
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
 * @see MiraiLoggerPlatformBase 平台通用基础实现. 若
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
     * 随从. 在 this 中调用所有方法后都应继续往 [follower] 传递调用.
     * [follower] 的存在可以让一次日志被多个日志记录器记录.
     *
     * 一般不建议直接修改这个属性. 请通过 [plus] 来连接两个日志记录器.
     * 如: `val logger = bot.logger + MyOwnLogger()`
     * 这样, 当调用 `logger.info()` 时, bot.logger 会首先记录, MyOwnLogger 会随后记录.
     *
     * 当然, 多个 logger 也可以加在一起: `val logger = bot.logger + MyOwnLogger() + MyOwnLogger2()`
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
    if (this !is MiraiLoggerWithSwitch || switch) verbose(lazyMessage())
}

inline fun MiraiLogger.verbose(lazyMessage: () -> String, e: Throwable?) {
    if (this !is MiraiLoggerWithSwitch || switch) verbose(lazyMessage(), e)
}

inline fun MiraiLogger.debug(lazyMessage: () -> String?) {
    if (this !is MiraiLoggerWithSwitch || switch) debug(lazyMessage())
}

inline fun MiraiLogger.debug(lazyMessage: () -> String?, e: Throwable?) {
    if (this !is MiraiLoggerWithSwitch || switch) debug(lazyMessage(), e)
}

inline fun MiraiLogger.info(lazyMessage: () -> String?) {
    if (this !is MiraiLoggerWithSwitch || switch) info(lazyMessage())
}

inline fun MiraiLogger.info(lazyMessage: () -> String?, e: Throwable?) {
    if (this !is MiraiLoggerWithSwitch || switch) info(lazyMessage(), e)
}

inline fun MiraiLogger.warning(lazyMessage: () -> String?) {
    if (this !is MiraiLoggerWithSwitch || switch) warning(lazyMessage())
}

inline fun MiraiLogger.warning(lazyMessage: () -> String?, e: Throwable?) {
    if (this !is MiraiLoggerWithSwitch || switch) warning(lazyMessage(), e)
}

inline fun MiraiLogger.error(lazyMessage: () -> String?) {
    if (this !is MiraiLoggerWithSwitch || switch) error(lazyMessage())
}

inline fun MiraiLogger.error(lazyMessage: () -> String?, e: Throwable?) {
    if (this !is MiraiLoggerWithSwitch || switch) error(lazyMessage(), e)
}

/**
 * 当前平台的默认的日志记录器.
 * 在 _JVM 控制台_ 端的实现为 [println]
 * 在 _Android_ 端的实现为 `android.util.Log`
 *
 * 不应该直接构造这个类的实例. 请使用 [DefaultLogger], 或使用默认的顶层日志记录 [MiraiLogger.Companion]
 */
expect open class PlatformLogger @JvmOverloads internal constructor(identity: String? = "Mirai") : MiraiLoggerPlatformBase

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
}

/**
 * 简易日志记录, 所有类型日志都会被重定向 [logger]
 */
class SimpleLogger(
    override val identity: String?,
    private val logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit
) : MiraiLoggerPlatformBase() {

    enum class LogPriority {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR
    }

    companion object {
        inline operator fun invoke(crossinline logger: (message: String?, e: Throwable?) -> Unit): SimpleLogger = SimpleLogger(null, logger)

        inline operator fun invoke(identity: String?, crossinline logger: (message: String?, e: Throwable?) -> Unit): SimpleLogger =
            SimpleLogger(identity) { _, message, e ->
                logger(message, e)
            }

        operator fun invoke(logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit): SimpleLogger = SimpleLogger(null, logger)
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
class MiraiLoggerWithSwitch internal constructor(private val delegate: MiraiLogger, default: Boolean) : MiraiLoggerPlatformBase() {
    override val identity: String? get() = delegate.identity

    /**
     * true 为开启.
     */
    @PublishedApi
    internal var switch: Boolean = default

    val isEnabled: Boolean get() = switch

    fun enable() {
        switch = true
    }

    fun disable() {
        switch = false
    }

    override fun verbose0(message: String?) = if (switch) delegate.verbose(message) else Unit
    override fun verbose0(message: String?, e: Throwable?) = if (switch) delegate.verbose(message, e) else Unit
    override fun debug0(message: String?) = if (switch) delegate.debug(message) else Unit
    override fun debug0(message: String?, e: Throwable?) = if (switch) delegate.debug(message, e) else Unit
    override fun info0(message: String?) = if (switch) delegate.info(message) else Unit
    override fun info0(message: String?, e: Throwable?) = if (switch) delegate.info(message, e) else Unit
    override fun warning0(message: String?) = if (switch) delegate.warning(message) else Unit
    override fun warning0(message: String?, e: Throwable?) = if (switch) delegate.warning(message, e) else Unit
    override fun error0(message: String?) = if (switch) delegate.error(message) else Unit
    override fun error0(message: String?, e: Throwable?) = if (switch) delegate.error(message, e) else Unit
}

/**
 * 日志基类. 实现了 [follower] 的调用传递.
 * 若 Mirai 自带的日志系统无法满足需求, 请继承这个类并实现其抽象函数.
 *
 * 这个类不应该被用作变量的类型定义. 只应被作为继承对象.
 * 在定义 logger 变量时, 请一直使用 [MiraiLogger] 或者 [MiraiLoggerWithSwitch].
 */
abstract class MiraiLoggerPlatformBase : MiraiLogger {
    final override var follower: MiraiLogger? = null

    final override fun verbose(message: String?) {
        follower?.verbose(message)
        verbose0(message)
    }

    final override fun verbose(message: String?, e: Throwable?) {
        follower?.verbose(message, e)
        verbose0(message, e)
    }

    final override fun debug(message: String?) {
        follower?.debug(message)
        debug0(message)
    }

    final override fun debug(message: String?, e: Throwable?) {
        follower?.debug(message, e)
        debug0(message, e)
    }

    final override fun info(message: String?) {
        follower?.info(message)
        info0(message)
    }

    final override fun info(message: String?, e: Throwable?) {
        follower?.info(message, e)
        info0(message, e)
    }

    final override fun warning(message: String?) {
        follower?.warning(message)
        warning0(message)
    }

    final override fun warning(message: String?, e: Throwable?) {
        follower?.warning(message, e)
        warning0(message, e)
    }

    final override fun error(message: String?) {
        follower?.error(message)
        error0(message)
    }

    final override fun error(message: String?, e: Throwable?) {
        follower?.error(message, e)
        error0(message, e)
    }

    protected abstract fun verbose0(message: String?)
    protected abstract fun verbose0(message: String?, e: Throwable?)
    protected abstract fun debug0(message: String?)
    protected abstract fun debug0(message: String?, e: Throwable?)
    protected abstract fun info0(message: String?)
    protected abstract fun info0(message: String?, e: Throwable?)
    protected abstract fun warning0(message: String?)
    protected abstract fun warning0(message: String?, e: Throwable?)
    protected abstract fun error0(message: String?)
    protected abstract fun error0(message: String?, e: Throwable?)

    override operator fun <T : MiraiLogger> plus(follower: T): T {
        this.follower = follower
        return follower
    }

    override fun plusAssign(follower: MiraiLogger) =
        if (this.follower == null) this.follower = follower
        else this.follower!! += follower
}
