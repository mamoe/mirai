package net.mamoe.mirai.utils

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.packet.login.TouchPacket.TouchResponse
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmStatic

/**
 * 验证码处理器. 需挂起(阻塞)直到处理完成验证码.
 *
 * 返回长度为 4 的验证码. 为空则刷新验证码
 */
typealias CaptchaSolver = suspend Bot.(IoBuffer) -> String?

/**
 * 在各平台实现的默认的验证码处理器.
 */
expect var DefaultCaptchaSolver: CaptchaSolver

/**
 * 网络和连接配置
 */
class BotConfiguration : CoroutineContext.Element {
    /**
     * 等待 [TouchResponse] 的时间
     */
    var touchTimeout: TimeSpan = 2.seconds
    /**
     * 是否使用随机的设备名.
     * 使用随机可以降低被封禁的风险, 但可能导致每次登录都需要输入验证码
     * 当一台设备只登录少量账号时, 将此项设置为 `false` 可能更好.
     */
    var randomDeviceName: Boolean = false
    /**
     * 心跳周期. 过长会导致被服务器断开连接.
     */
    var heartbeatPeriod: TimeSpan = 60.seconds
    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 1s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    var heartbeatTimeout: TimeSpan = 2.seconds
    /**
     * 心跳失败后的第一次重连前的等待时间.
     */
    var firstReconnectDelay: TimeSpan = 5.seconds
    /**
     * 重连失败后, 继续尝试的每次等待时间
     */
    var reconnectPeriod: TimeSpan = 60.seconds
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
    var captchaSolver: CaptchaSolver = DefaultCaptchaSolver

    companion object Key : CoroutineContext.Key<BotConfiguration> {
        /**
         * 默认的配置实例
         */
        @JvmStatic
        val Default = BotConfiguration()
    }

    override val key: CoroutineContext.Key<*> get() = Key
}

suspend inline fun currentBotConfiguration(): BotConfiguration = coroutineContext[BotConfiguration] ?: error("No BotConfiguration found")