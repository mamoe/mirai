package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import kotlin.jvm.JvmOverloads

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
    companion object : MiraiLogger by DefaultLogger("TOP Level")

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
     * bot.logInfo("Hi")
     * ```
     * 在这个例子中的 `MyOwnLogger` 将可以记录到 "Hi".
     */
    var follower: MiraiLogger?

    fun logInfo(any: Any?) = log(any)

    fun log(e: Throwable)

    fun log(any: Any?)

    fun logError(any: Any?)

    fun logError(e: Throwable) = log(e)

    fun logDebug(any: Any?)

    fun logCyan(any: Any?)

    fun logPurple(any: Any?)

    fun logGreen(any: Any?)

    fun logBlue(any: Any?)

    /**
     * 添加一个 [follower], 返回 [follower]
     * 它只会把 `this` 的属性 [MiraiLogger.follower] 修改为这个函数的参数 [follower], 然后返回这个参数.
     * 若 [MiraiLogger.follower] 已经有值, 则会替换掉这个值.
     *
     * @see follower
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
 * 平台基类.
 * 实现了 [follower] 的调用传递.
 * 若要自行实现日志记录, 请优先考虑继承 [PlatformLogger]
 */
abstract class MiraiLoggerPlatformBase : MiraiLogger {
    final override var follower: MiraiLogger? = null

    final override fun logInfo(any: Any?) = log(any)

    final override fun log(e: Throwable) {
        log0(e)
        follower?.log(e)
    }

    final override fun log(any: Any?) {
        log0(any)
        follower?.log(any)
    }

    final override fun logError(any: Any?) {
        logError0(any)
        follower?.logError(any)
    }

    override fun logError(e: Throwable) = log(e)

    final override fun logDebug(any: Any?) {
        logDebug0(any)
        follower?.logDebug(any)
    }

    final override fun logCyan(any: Any?) {
        logCyan0(any)
        follower?.logCyan(any)
    }

    final override fun logPurple(any: Any?) {
        logPurple0(any)
        follower?.logPurple(any)
    }

    final override fun logGreen(any: Any?) {
        logGreen0(any)
        follower?.logGreen(any)
    }

    final override fun logBlue(any: Any?) {
        logBlue0(any)
        follower?.logBlue(any)
    }

    protected abstract fun log0(e: Throwable)
    protected abstract fun log0(any: Any?)
    protected abstract fun logError0(any: Any?)
    protected abstract fun logDebug0(any: Any?)
    protected abstract fun logCyan0(any: Any?)
    protected abstract fun logPurple0(any: Any?)
    protected abstract fun logGreen0(any: Any?)
    protected abstract fun logBlue0(any: Any?)

    override fun plus(follower: MiraiLogger): MiraiLogger {
        this.follower = follower
        return follower
    }

    override fun plusAssign(follower: MiraiLogger) =
        if (this.follower == null) this.follower = follower
        else this.follower!! += follower
}

/**
 * 用于创建默认的日志记录器. 在一些需要使用日志的 Mirai 的组件, 如 [Bot], 都会通过这个函数构造日志记录器
 */
var DefaultLogger: (identity: String?) -> MiraiLogger = { PlatformLogger() }

/**
 * 当前平台的默认的日志记录器.
 * 在 _JVM 控制台_ 端的实现为 [println]
 *
 * 不应该直接构造这个类的实例. 需使用 [DefaultLogger]
 */
expect open class PlatformLogger @JvmOverloads internal constructor(identity: String? = null) : MiraiLoggerPlatformBase

/**
 * 不作任何事情的 logger
 */
@Suppress("unused")
object NoLogger : PlatformLogger() {
    override val identity: String? = null

    override fun log0(e: Throwable) {
    }

    override fun log0(any: Any?) {
    }

    override fun logError0(any: Any?) {
    }

    override fun logDebug0(any: Any?) {
    }

    override fun logCyan0(any: Any?) {
    }

    override fun logPurple0(any: Any?) {
    }

    override fun logGreen0(any: Any?) {
    }

    override fun logBlue0(any: Any?) {
    }
}

/**
 * 在顶层日志记录这个异常
 */
fun Throwable.log() = MiraiLogger.log(this)