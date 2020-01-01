package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import kotlin.jvm.JvmOverloads


/**
 * 用于创建默认的日志记录器. 在一些需要使用日志的 Mirai 的组件, 如 [Bot], 都会通过这个函数构造日志记录器
 * 可直接修改这个变量的值来重定向日志输出.
 */
var DefaultLogger: (identity: String?) -> MiraiLogger = { PlatformLogger(it) }

/**
 * 当前平台的默认的日志记录器.
 * 在 _JVM 控制台_ 端的实现为 [println]
 * 在 _Android_ 端的实现为 [android.util.Log]
 *
 * 不应该直接构造这个类的实例. 请使用 [DefaultLogger]
 */
expect open class PlatformLogger @JvmOverloads internal constructor(identity: String? = "Mirai") : MiraiLoggerPlatformBase

/**
 * 给这个 logger 添加一个开关, 用于控制是否记录 log
 */
@JvmOverloads
fun MiraiLogger.withSwitch(default: Boolean = true): MiraiLoggerWithSwitch = MiraiLoggerWithSwitch(this, default)

/**
 * 日志记录器. 所有的输出均依赖于它.
 * 不同的对象可能拥有只属于自己的 logger. 通过 [identity] 来区分.
 *
 * 注意: 请不要直接实现这个接口, 请继承 [MiraiLoggerPlatformBase]
 *
 * @see MiraiLoggerPlatformBase 平台通用基础实现
 */
interface MiraiLogger {
    /**
     * 顶层日志记录器
     */
    companion object : MiraiLogger by DefaultLogger("Mirai")

    val identity: String?

    /**
     * 随从. 在 this 中调用所有方法后都应继续往 [follower] 传递调用.
     * [follower] 的存在可以让一次日志被多个日志记录器记录.
     *
     * 例:
     * ```kotlin
     * val bot = Bot( ... )
     * bot.follower = MyOwnLogger()
     *
     * bot.info("Hi")
     * ```
     * 在这个例子中的 `MyOwnLogger` 将可以记录到 "Hi".
     */
    var follower: MiraiLogger?

    /**
     * 记录一个 `verbose` 级别的日志.
     */
    fun verbose(any: Any?)

    fun verbose(e: Throwable?) = verbose(null, e)
    fun verbose(message: String?, e: Throwable?)

    /**
     * 记录一个 `debug` 级别的日志.
     */
    fun debug(any: Any?)

    fun debug(e: Throwable?) = debug(null, e)
    fun debug(message: String?, e: Throwable?)


    /**
     * 记录一个 `info` 级别的日志.
     */
    fun info(any: Any?)

    fun info(e: Throwable?) = info(null, e)
    fun info(message: String?, e: Throwable?)


    /**
     * 记录一个 `warning` 级别的日志.
     */
    fun warning(any: Any?)

    fun warning(e: Throwable?) = warning(null, e)
    fun warning(message: String?, e: Throwable?)


    /**
     * 记录一个 `error` 级别的日志.
     */
    fun error(e: Any?)

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
     * @see follower
     * @return [follower]
     */
    operator fun plus(follower: MiraiLogger): MiraiLogger

    /**
     * 添加一个 [follower]
     * 若 [MiraiLogger.follower] 已经有值, 则会对这个值调用 [plusAssign]. 即会在日志记录器链的末尾添加这个参数 [follower]
     *
     * @see follower
     */
    operator fun plusAssign(follower: MiraiLogger)
}

/**
 * 不做任何事情的 logger, keep silent.
 */
@Suppress("unused")
object SilentLogger : PlatformLogger() {
    override val identity: String? = null

    override fun error0(any: Any?) {
    }

    override fun debug0(any: Any?) {
    }

    override fun warning0(any: Any?) {
    }

    override fun verbose0(any: Any?) {
    }

    override fun info0(any: Any?) {
    }
}

@Suppress("FunctionName")
fun SimpleLogger(logger: (String?, Throwable?) -> Unit): SimpleLogger = SimpleLogger(null, logger)

/**
 * 简易日志记录, 所有类型日志都会被重定向 [logger]
 */
class SimpleLogger(override val identity: String?, private val logger: (String?, Throwable?) -> Unit) : MiraiLoggerPlatformBase() {
    override fun verbose0(any: Any?) = logger(any?.toString(), null)
    override fun verbose0(message: String?, e: Throwable?) = logger(message, e)
    override fun debug0(any: Any?) = logger(any?.toString(), null)
    override fun debug0(message: String?, e: Throwable?) = logger(message, e)
    override fun info0(any: Any?) = logger(any?.toString(), null)
    override fun info0(message: String?, e: Throwable?) = logger(message, e)
    override fun warning0(any: Any?) = logger(any?.toString(), null)
    override fun warning0(message: String?, e: Throwable?) = logger(message, e)
    override fun error0(any: Any?) = logger(any?.toString(), null)
    override fun error0(message: String?, e: Throwable?) = logger(message, e)
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

    private var switch: Boolean = default

    fun enable() {
        switch = true
    }

    fun disable() {
        switch = false
    }

    override fun verbose0(any: Any?) = if (switch) delegate.verbose(any) else Unit
    override fun verbose0(message: String?, e: Throwable?) = if (switch) delegate.verbose(message, e) else Unit
    override fun debug0(any: Any?) = if (switch) delegate.debug(any) else Unit
    override fun debug0(message: String?, e: Throwable?) = if (switch) delegate.debug(message, e) else Unit
    override fun info0(any: Any?) = if (switch) delegate.info(any) else Unit
    override fun info0(message: String?, e: Throwable?) = if (switch) delegate.info(message, e) else Unit
    override fun warning0(any: Any?) = if (switch) delegate.warning(any) else Unit
    override fun warning0(message: String?, e: Throwable?) = if (switch) delegate.warning(message, e) else Unit
    override fun error0(any: Any?) = if (switch) delegate.error(any) else Unit
    override fun error0(message: String?, e: Throwable?) = if (switch) delegate.error(message, e) else Unit

}

/**
 * 平台日志基类.
 * 实现了 [follower] 的调用传递.
 *
 * 若要自行实现日志记录, 请优先考虑继承 [PlatformLogger].
 *
 * 它不应该被用作变量的类型定义. 只应被继承
 */
abstract class MiraiLoggerPlatformBase : MiraiLogger {
    final override var follower: MiraiLogger? = null

    final override fun verbose(any: Any?) {
        follower?.verbose(any)
        verbose0(any)
    }

    final override fun verbose(message: String?, e: Throwable?) {
        follower?.verbose(message, e)
        verbose0(message, e)
    }

    final override fun debug(any: Any?) {
        follower?.debug(any)
        debug0(any)
    }

    final override fun debug(message: String?, e: Throwable?) {
        follower?.debug(message, e)
        debug0(message, e)
    }

    final override fun info(any: Any?) {
        follower?.info(any)
        info0(any)
    }

    final override fun info(message: String?, e: Throwable?) {
        follower?.info(message, e)
        info0(message, e)
    }

    final override fun warning(any: Any?) {
        follower?.warning(any)
        warning0(any)
    }

    final override fun warning(message: String?, e: Throwable?) {
        follower?.warning(message, e)
        warning0(message, e)
    }

    final override fun error(e: Any?) {
        follower?.error(e)
        error0(e)
    }

    final override fun error(message: String?, e: Throwable?) {
        follower?.error(message, e)
        error0(message, e)
    }

    protected abstract fun verbose0(any: Any?)
    protected abstract fun verbose0(message: String?, e: Throwable?)
    protected abstract fun debug0(any: Any?)
    protected abstract fun debug0(message: String?, e: Throwable?)
    protected abstract fun info0(any: Any?)
    protected abstract fun info0(message: String?, e: Throwable?)
    protected abstract fun warning0(any: Any?)
    protected abstract fun warning0(message: String?, e: Throwable?)
    protected abstract fun error0(any: Any?)
    protected abstract fun error0(message: String?, e: Throwable?)

    override fun plus(follower: MiraiLogger): MiraiLogger {
        this.follower = follower
        return follower
    }

    override fun plusAssign(follower: MiraiLogger) =
        if (this.follower == null) this.follower = follower
        else this.follower!! += follower
}
