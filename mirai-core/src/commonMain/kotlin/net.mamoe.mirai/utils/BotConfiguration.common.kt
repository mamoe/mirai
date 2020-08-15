/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("unused", "DEPRECATION_ERROR", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * [Bot] 配置.
 *
 * Kotlin 使用方法:
 * ```
 * val bot = Bot(...) {
 *    // 在这里配置 Bot
 *
 *    bogLoggerSupplier = { bot -> ... }
 *    fileBasedDeviceInfo()
 *    inheritCoroutineContext() // 使用 `coroutineScope` 的 Job 作为父 Job
 * }
 * ```
 */
expect open class BotConfiguration() : BotConfigurationBase {
    /**
     * 设备信息覆盖. 在没有手动指定时将会通过日志警告, 并使用随机设备信息.
     * @see randomDeviceInfo 使用随机设备信息
     */
    var deviceInfo: ((Context) -> DeviceInfo)?

    /**
     * 使用随机设备信息.
     *
     * @see deviceInfo
     */
    @ConfigurationDsl
    fun randomDeviceInfo()

    /**
     * 协议类型, 服务器仅允许使用不同协议同时登录.
     */
    enum class MiraiProtocol {
        /**
         * Android 手机.
         */
        ANDROID_PHONE,

        /**
         * Android 平板.
         */
        ANDROID_PAD,

        /**
         * Android 手表.
         * */
        @SinceMirai("1.1.0")
        ANDROID_WATCH;


        internal val id: Long
    }

    companion object {
        /** 默认的配置实例. 可以进行修改 */
        @JvmStatic
        val Default: BotConfiguration
    }

    fun copy(): BotConfiguration
}

@SinceMirai("1.1.0")
open class BotConfigurationBase internal constructor() {
    /**
     * 日志记录器
     *
     * - 默认打印到标准输出, 通过 [DefaultLogger]
     * - 忽略所有日志: [noBotLog]
     * - 重定向到一个目录: `networkLoggerSupplier = { DirectoryLogger("Net ${it.id}") }`
     * - 重定向到一个文件: `networkLoggerSupplier = { SingleFileLogger("Net ${it.id}") }`
     *
     * @see MiraiLogger
     */
    var botLoggerSupplier: ((Bot) -> MiraiLogger) = { DefaultLogger("Bot ${it.id}") }

    /**
     * 网络层日志构造器
     *
     * - 默认打印到标准输出, 通过 [DefaultLogger]
     * - 忽略所有日志: [noNetworkLog]
     * - 重定向到一个目录: `networkLoggerSupplier = { DirectoryLogger("Net ${it.id}") }`
     * - 重定向到一个文件: `networkLoggerSupplier = { SingleFileLogger("Net ${it.id}") }`
     *
     * @see MiraiLogger
     */
    var networkLoggerSupplier: ((Bot) -> MiraiLogger) = { DefaultLogger("Net ${it.id}") }

    /** 父 [CoroutineContext]. [Bot] 创建后会使用 [SupervisorJob] 覆盖其 [Job], 但会将这个 [Job] 作为父 [Job] */
    var parentCoroutineContext: CoroutineContext = EmptyCoroutineContext

    /** 心跳周期. 过长会导致被服务器断开连接. */
    var heartbeatPeriodMillis: Long = 60.secondsToMillis

    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 1s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    var heartbeatTimeoutMillis: Long = 5.secondsToMillis

    /** 心跳失败后的第一次重连前的等待时间. */
    var firstReconnectDelayMillis: Long = 5.secondsToMillis

    /** 重连失败后, 继续尝试的每次等待时间 */
    var reconnectPeriodMillis: Long = 5.secondsToMillis

    /** 最多尝试多少次重连 */
    var reconnectionRetryTimes: Int = Int.MAX_VALUE

    /** 验证码处理器 */
    var loginSolver: LoginSolver = LoginSolver.Default

    /** 使用协议类型 */
    var protocol: MiraiProtocol = MiraiProtocol.ANDROID_PAD

    /** 缓存策略  */
    @SinceMirai("1.0.0")
    @MiraiExperimentalAPI
    var fileCacheStrategy: FileCacheStrategy = FileCacheStrategy.PlatformDefault

    /**
     * Json 序列化器, 使用 'kotlinx.serialization'
     */
    @SinceMirai("1.1.0")
    @MiraiExperimentalAPI
    var json: Json = kotlin.runCatching {
        @OptIn(UnstableDefault::class)
        Json(JsonConfiguration(isLenient = true, ignoreUnknownKeys = true))
    }.getOrElse { Json(JsonConfiguration.Stable) }

    /**
     * 不显示网络日志. 不推荐.
     * @see networkLoggerSupplier 更多日志处理方式
     */
    @ConfigurationDsl
    fun noNetworkLog() {
        networkLoggerSupplier = { _ -> SilentLogger }
    }

    /**
     * 不显示 [Bot] 日志. 不推荐.
     * @see botLoggerSupplier 更多日志处理方式
     */
    @ConfigurationDsl
    fun noBotLog() {
        botLoggerSupplier = { _ -> SilentLogger }
    }

    /**
     * 使用当前协程的 [coroutineContext] 作为 [parentCoroutineContext].
     *
     * Bot 将会使用一个 [SupervisorJob] 覆盖 [coroutineContext] 当前协程的 [Job], 并使用当前协程的 [Job] 作为父 [Job]
     *
     * 用例:
     * ```
     * coroutineScope {
     *   val bot = Bot(...) {
     *     inheritCoroutineContext()
     *   }
     *   bot.login()
     * } // coroutineScope 会等待 Bot 退出
     * ```
     *
     *
     * **注意**: `bot.cancel` 时将会让父 [Job] 也被 cancel.
     * ```
     * coroutineScope { // this: CoroutineScope
     *   launch {
     *     while(isActive) {
     *       delay(500)
     *       println("I'm alive")
     *     }
     *   }
     *
     *   val bot = Bot(...) {
     *      inheritCoroutineContext() // 使用 `coroutineScope` 的 Job 作为父 Job
     *   }
     *   bot.login()
     *   bot.cancel() // 取消了整个 `coroutineScope`, 因此上文不断打印 `"I'm alive"` 的协程也会被取消.
     * }
     * ```
     *
     * 因此, 此函数尤为适合在 `suspend fun main()` 中使用, 它能阻止主线程退出:
     * ```
     * suspend fun main() {
     *   val bot = Bot() {
     *     inheritCoroutineContext()
     *   }
     *   bot.subscribe { ... }
     *
     *   // 主线程不会退出, 直到 Bot 离线.
     * }
     * ```
     *
     * 简言之,
     * - 若想让 [Bot] 作为 '守护进程' 运行, 则无需调用 [inheritCoroutineContext].
     * - 若想让 [Bot] 依赖于当前协程, 让当前协程等待 [Bot] 运行, 则使用 [inheritCoroutineContext]
     *
     * @see parentCoroutineContext
     */
    @JvmSynthetic
    @ConfigurationDsl
    suspend inline fun inheritCoroutineContext() {
        parentCoroutineContext = coroutineContext
    }

    /** 标注一个配置 DSL 函数 */
    @Target(AnnotationTarget.FUNCTION)
    @DslMarker
    annotation class ConfigurationDsl
}

internal val deviceInfoStub: (Context) -> DeviceInfo = {
    @Suppress("DEPRECATION")
    MiraiLogger.warning("未指定设备信息, 已使用随机设备信息. 请查看 BotConfiguration.deviceInfo 以获取更多信息.")
    @Suppress("DEPRECATION")
    MiraiLogger.warning("Device info isn't specified. Please refer to BotConfiguration.deviceInfo for more information")
    SystemDeviceInfo()
}