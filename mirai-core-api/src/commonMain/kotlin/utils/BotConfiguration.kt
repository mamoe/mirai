/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "DEPRECATION_ERROR", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

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
 *
 * Java 使用方法:
 * ```java
 * Bot bot = BotFactory.newBot(..., new BotConfiguration() {{
 *     setBogLoggerSupplier((Bot bot) -> { ... })
 *     fileBasedDeviceInfo()
 *     ...
 * }})
 * ```
 */
@Suppress("PropertyName")
public open class BotConfiguration { // open for Java
    /**
     * 日志记录器
     *
     * - 默认打印到标准输出, 通过 [MiraiLogger.create]
     * - 忽略所有日志: [noBotLog]
     * - 重定向到一个目录: `networkLoggerSupplier = { DirectoryLogger("Net ${it.id}") }`
     * - 重定向到一个文件: `networkLoggerSupplier = { SingleFileLogger("Net ${it.id}") }`
     *
     * @see MiraiLogger
     */
    public var botLoggerSupplier: ((Bot) -> MiraiLogger) = { MiraiLogger.create("Bot ${it.id}") }

    /**
     * 网络层日志构造器
     *
     * - 默认打印到标准输出, 通过 [MiraiLogger.create]
     * - 忽略所有日志: [noNetworkLog]
     * - 重定向到一个目录: `networkLoggerSupplier = { DirectoryLogger("Net ${it.id}") }`
     * - 重定向到一个文件: `networkLoggerSupplier = { SingleFileLogger("Net ${it.id}") }`
     *
     * @see MiraiLogger
     */
    public var networkLoggerSupplier: ((Bot) -> MiraiLogger) = { MiraiLogger.create("Net ${it.id}") }

    /** 父 [CoroutineContext]. [Bot] 创建后会使用 [SupervisorJob] 覆盖其 [Job], 但会将这个 [Job] 作为父 [Job] */
    public var parentCoroutineContext: CoroutineContext = EmptyCoroutineContext

    /** 心跳周期. 过长会导致被服务器断开连接. */
    public var heartbeatPeriodMillis: Long = 60.secondsToMillis

    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 1s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    public var heartbeatTimeoutMillis: Long = 5.secondsToMillis

    /** 心跳失败后的第一次重连前的等待时间. */
    public var firstReconnectDelayMillis: Long = 5.secondsToMillis

    /** 重连失败后, 继续尝试的每次等待时间 */
    public var reconnectPeriodMillis: Long = 5.secondsToMillis

    /** 最多尝试多少次重连 */
    public var reconnectionRetryTimes: Int = Int.MAX_VALUE

    /**
     * 验证码处理器
     *
     * - 在 Android 需要手动提供 [LoginSolver]
     * - 在 JVM, Mirai 会根据环境支持情况选择 Swing/CLI 实现
     *
     * 详见 [LoginSolver.Default]
     *
     * @see LoginSolver
     */
    public var loginSolver: LoginSolver? = LoginSolver.Default

    /** 使用协议类型 */
    public var protocol: MiraiProtocol = MiraiProtocol.ANDROID_PHONE

    /**
     * 设备信息覆盖. 在没有手动指定时将会通过日志警告, 并使用随机设备信息.
     * @see fileBasedDeviceInfo 使用指定文件存储设备信息
     * @see randomDeviceInfo 使用随机设备信息
     */
    public var deviceInfo: ((Bot) -> DeviceInfo)? = deviceInfoStub

    /**
     * Json 序列化器, 使用 'kotlinx.serialization'
     */
    @MiraiExperimentalApi
    public var json: Json = kotlin.runCatching {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }.getOrElse { Json {} }

    /**
     * 使用随机设备信息.
     *
     * @see deviceInfo
     */
    @ConfigurationDsl
    public fun randomDeviceInfo() {
        deviceInfo = null
    }

    /**
     * 使用特定由 [DeviceInfo] 序列化产生的 JSON 的设备信息
     *
     * @see deviceInfo
     */
    @ConfigurationDsl
    public fun loadDeviceInfoJson(json: String) {
        deviceInfo = {
            this.json.decodeFromString(DeviceInfo.serializer(), json)
        }
    }

    /**
     * 重定向 [网络日志][networkLoggerSupplier] 到指定目录. 若目录不存在将会自动创建 ([File.mkdirs])
     * @see DirectoryLogger
     * @see redirectNetworkLogToDirectory
     */
    @JvmOverloads
    @ConfigurationDsl
    public fun redirectNetworkLogToDirectory(
        dir: File = File("logs"),
        retain: Long = 1.weeksToMillis,
        identity: (bot: Bot) -> String = { "Net ${it.id}" }
    ) {
        require(!dir.isFile) { "dir must not be a file" }
        dir.mkdirs()
        networkLoggerSupplier = { DirectoryLogger(identity(it), dir, retain) }
    }

    /**
     * 重定向 [网络日志][networkLoggerSupplier] 到指定文件.
     * 日志将会逐行追加到此文件. 若文件不存在将会自动创建 ([File.createNewFile])
     * @see SingleFileLogger
     * @see redirectNetworkLogToDirectory
     */
    @JvmOverloads
    @ConfigurationDsl
    public fun redirectNetworkLogToFile(
        file: File = File("mirai.log"),
        identity: (bot: Bot) -> String = { "Net ${it.id}" }
    ) {
        require(!file.isDirectory) { "file must not be a dir" }
        file.createNewFile()
        networkLoggerSupplier = { SingleFileLogger(identity(it), file) }
    }

    /**
     * 重定向 [Bot 日志][botLoggerSupplier] 到指定目录. 若目录不存在将会自动创建 ([File.mkdirs])
     * @see DirectoryLogger
     * @see redirectBotLogToFile
     */
    @JvmOverloads
    @ConfigurationDsl
    public fun redirectBotLogToDirectory(
        dir: File = File("logs"),
        retain: Long = 1.weeksToMillis,
        identity: (bot: Bot) -> String = { "Net ${it.id}" }
    ) {
        require(!dir.isFile) { "dir must not be a file" }
        dir.mkdirs()
        botLoggerSupplier = { DirectoryLogger(identity(it), dir, retain) }
    }

    /**
     * 重定向 [Bot 日志][botLoggerSupplier] 到指定文件.
     * 日志将会逐行追加到此文件. 若文件不存在将会自动创建 ([File.createNewFile])
     * @see SingleFileLogger
     * @see redirectBotLogToDirectory
     */
    @JvmOverloads
    @ConfigurationDsl
    public fun redirectBotLogToFile(
        file: File = File("mirai.log"),
        identity: (bot: Bot) -> String = { "Net ${it.id}" }
    ) {
        require(!file.isDirectory) { "file must not be a dir" }
        file.createNewFile()
        botLoggerSupplier = { SingleFileLogger(identity(it), file) }
    }

    public enum class MiraiProtocol {
        /**
         * Android 手机. 所有功能都支持.
         */
        ANDROID_PHONE,

        /**
         * Android 平板.
         *
         * 注意: 不支持戳一戳事件解析
         */
        ANDROID_PAD,

        /**
         * Android 手表.
         */
        ANDROID_WATCH,

    }

    public companion object {
        /** 默认的配置实例. 可以进行修改 */
        @JvmStatic
        public val Default: BotConfiguration = BotConfiguration()
    }

    /**
     * 使用文件存储设备信息.
     *
     * 此函数只在 JVM 和 Android 有效. 在其他平台将会抛出异常.
     * @param filepath 文件路径. 可相对于程序运行路径 (`user.dir`), 也可以是绝对路径.
     * @see deviceInfo
     */
    @JvmOverloads
    @ConfigurationDsl
    public fun fileBasedDeviceInfo(filepath: String = "device.json") {
        deviceInfo = getFileBasedDeviceInfoSupplier(filepath)
    }

    public fun copy(): BotConfiguration {
        return BotConfiguration().also { new ->
            new.botLoggerSupplier = botLoggerSupplier
            new.networkLoggerSupplier = networkLoggerSupplier
            new.deviceInfo = deviceInfo
            new.parentCoroutineContext = parentCoroutineContext
            new.heartbeatPeriodMillis = heartbeatPeriodMillis
            new.heartbeatTimeoutMillis = heartbeatTimeoutMillis
            new.firstReconnectDelayMillis = firstReconnectDelayMillis
            new.reconnectPeriodMillis = reconnectPeriodMillis
            new.reconnectionRetryTimes = reconnectionRetryTimes
            new.loginSolver = loginSolver
            new.protocol = protocol
        }
    }


    /**
     * 不显示网络日志. 不推荐.
     * @see networkLoggerSupplier 更多日志处理方式
     */
    @ConfigurationDsl
    public fun noNetworkLog() {
        networkLoggerSupplier = { _ -> SilentLogger }
    }

    /**
     * 不显示 [Bot] 日志. 不推荐.
     * @see botLoggerSupplier 更多日志处理方式
     */
    @ConfigurationDsl
    public fun noBotLog() {
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
    public suspend inline fun inheritCoroutineContext() {
        parentCoroutineContext = coroutineContext
    }

    /** 标注一个配置 DSL 函数 */
    @Target(AnnotationTarget.FUNCTION)
    @DslMarker
    public annotation class ConfigurationDsl
}

internal val deviceInfoStub: (Bot) -> DeviceInfo = {
    @Suppress("DEPRECATION")
    MiraiLogger.TopLevel.warning("未指定设备信息, 已使用随机设备信息. 请查看 BotConfiguration.deviceInfo 以获取更多信息.")
    @Suppress("DEPRECATION")
    MiraiLogger.TopLevel.warning("Device info isn't specified. Please refer to BotConfiguration.deviceInfo for more information")
    DeviceInfo.random()
}