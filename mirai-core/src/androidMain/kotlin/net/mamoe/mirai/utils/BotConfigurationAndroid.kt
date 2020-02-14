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
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 在各平台实现的默认的验证码处理器.
 */
actual var defaultLoginSolver: LoginSolver = object : LoginSolver() {
    override suspend fun onSolvePicCaptcha(bot: Bot, data: IoBuffer): String? {
        error("should be implemented manually by you")
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        error("should be implemented manually by you")
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        error("should be implemented manually by you")
    }
}

@Suppress("ClassName", "PropertyName")
actual open class BotConfiguration actual constructor() {
    /**
     * 日志记录器
     */
    actual var botLoggerSupplier: ((Bot) -> MiraiLogger) = { DefaultLogger("Bot(${it.uin})") }
    /**
     * 网络层日志构造器
     */
    actual var networkLoggerSupplier: ((BotNetworkHandler) -> MiraiLogger) = { DefaultLogger("Network(${it.bot.uin})") }
    /**
     * 设备信息覆盖. 默认使用随机的设备信息.
     */
    actual var deviceInfo: ((Context) -> DeviceInfo)? = null

    /**
     * 父 [CoroutineContext]
     */
    actual var parentCoroutineContext: CoroutineContext = EmptyCoroutineContext

    /**
     * 心跳周期. 过长会导致被服务器断开连接.
     */
    actual var heartbeatPeriodMillis: Long = 60.secondsToMillis
    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 5s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    actual var heartbeatTimeoutMillis: Long = 2.secondsToMillis
    /**
     * 心跳失败后的第一次重连前的等待时间.
     */
    actual var firstReconnectDelayMillis: Long = 5.secondsToMillis
    /**
     * 重连失败后, 继续尝试的每次等待时间
     */
    actual var reconnectPeriodMillis: Long = 60.secondsToMillis
    /**
     * 最多尝试多少次重连
     */
    actual var reconnectionRetryTimes: Int = 3
    /**
     * 验证码处理器
     */
    actual var loginSolver: LoginSolver = defaultLoginSolver

    actual companion object {
        /**
         * 默认的配置实例
         */
        @JvmStatic
        actual val Default = BotConfiguration()
    }

    @Suppress("NOTHING_TO_INLINE")
    @BotConfigurationDsl
    inline operator fun FileBasedDeviceInfo.unaryPlus() {
        deviceInfo = { File(filepath).loadAsDeviceInfo() }
    }

    @Suppress("NOTHING_TO_INLINE")
    @BotConfigurationDsl
    inline operator fun FileBasedDeviceInfo.ByDeviceDotJson.unaryPlus() {
        deviceInfo = { File("device.json").loadAsDeviceInfo() }
    }

    actual operator fun _NoNetworkLog.unaryPlus() {
        networkLoggerSupplier = supplier
    }

    /**
     * 不记录网络层的 log.
     * 网络层 log 包含包接收, 包发送, 和一些调试用的记录.
     */
    @BotConfigurationDsl
    actual val NoNetworkLog: _NoNetworkLog
        get() = _NoNetworkLog


    @BotConfigurationDsl
    actual object _NoNetworkLog {
        internal val supplier = { _: BotNetworkHandler -> SilentLogger }
    }
}

/**
 * 使用文件系统存储设备信息.
 */
@BotConfigurationDsl
inline class FileBasedDeviceInfo @BotConfigurationDsl constructor(val filepath: String) {
    /**
     * 使用 "device.json" 存储设备信息
     */
    @BotConfigurationDsl
    companion object ByDeviceDotJson
}