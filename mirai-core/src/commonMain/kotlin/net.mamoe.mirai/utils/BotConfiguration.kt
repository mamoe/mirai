/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler
import kotlin.coroutines.CoroutineContext
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
 * [Bot] 配置
 */
@Suppress("PropertyName")
expect open class BotConfiguration() {
    /**
     * 日志记录器
     */
    var botLoggerSupplier: ((Bot) -> MiraiLogger)
    /**
     * 网络层日志构造器
     */
    var networkLoggerSupplier: ((BotNetworkHandler) -> MiraiLogger)
    /**
     * 设备信息覆盖. 默认使用随机的设备信息.
     */
    var deviceInfo: ((Context) -> DeviceInfo)?

    /**
     * 父 [CoroutineContext]
     */
    var parentCoroutineContext: CoroutineContext

    /**
     * 心跳周期. 过长会导致被服务器断开连接.
     */
    var heartbeatPeriodMillis: Long
    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 5s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    var heartbeatTimeoutMillis: Long
    /**
     * 心跳失败后的第一次重连前的等待时间.
     */
    var firstReconnectDelayMillis: Long
    /**
     * 重连失败后, 继续尝试的每次等待时间
     */
    var reconnectPeriodMillis: Long
    /**
     * 最多尝试多少次重连
     */
    var reconnectionRetryTimes: Int
    /**
     * 验证码处理器
     */
    var loginSolver: LoginSolver

    companion object {
        /**
         * 默认的配置实例
         */
        @JvmStatic
        val Default: BotConfiguration
    }

    operator fun _NoNetworkLog.unaryPlus()

    /**
     * 不记录网络层的 log.
     * 网络层 log 包含包接收, 包发送, 和一些调试用的记录.
     */
    @BotConfigurationDsl
    val NoNetworkLog: _NoNetworkLog

    @Suppress("ClassName")
    object _NoNetworkLog
}

@DslMarker
annotation class BotConfigurationDsl