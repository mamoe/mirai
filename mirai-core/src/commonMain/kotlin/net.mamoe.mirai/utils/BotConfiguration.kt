package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.Bot
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmStatic

/**
 * 验证码, 设备锁解决器
 */
abstract class LoginSolver {
    abstract suspend fun onSolvePicCaptcha(bot: Bot, data: IoBuffer): String?

    abstract suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String?

    abstract suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String?
}

/**
 * 在各平台实现的默认的验证码处理器.
 */
expect var defaultLoginSolver: LoginSolver

/**
 * 网络和连接配置
 */
class BotConfiguration {
    /**
     * 日志记录器
     */
    var logger: MiraiLogger? = null

    /**
     * 父 [CoroutineContext]
     */
    var parentCoroutineContext: CoroutineContext = EmptyCoroutineContext

    /**
     * 连接每个服务器的时间
     */
    var touchTimeoutMillis: Long = 1.secondsToMillis
    /**
     * 是否使用随机的设备名.
     * 使用随机可以降低被封禁的风险, 但可能导致每次登录都需要输入验证码
     * 当一台设备只登录少量账号时, 将此项设置为 `false` 可能更好.
     */
    var randomDeviceName: Boolean = false
    /**
     * 心跳周期. 过长会导致被服务器断开连接.
     */
    var heartbeatPeriodMillis: Long = 300.secondsToMillis
    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 5s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    var heartbeatTimeoutMillis: Long = 2.secondsToMillis
    /**
     * 心跳失败后的第一次重连前的等待时间.
     */
    var firstReconnectDelayMillis: Long = 5.secondsToMillis
    /**
     * 重连失败后, 继续尝试的每次等待时间
     */
    var reconnectPeriodMillis: Long = 60.secondsToMillis
    /**
     * 最多尝试多少次重连
     */
    var reconnectionRetryTimes: Int = 3
    /**
     * 有验证码要求就失败
     */
    var failOnCaptcha = false
    /**
     * 验证码处理器
     */
    var loginSolver: LoginSolver = defaultLoginSolver
    /**
     * 登录完成后几秒会收到好友消息的历史记录,
     * 这些历史记录不会触发事件.
     * 这个选项为是否把这些记录添加到日志
     */
    var logPreviousMessages: Boolean = false

    companion object {
        /**
         * 默认的配置实例
         */
        @JvmStatic
        val Default = BotConfiguration()
    }
}