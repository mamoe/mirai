/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "DEPRECATION_ERROR", "EXPOSED_SUPER_CLASS", "MemberVisibilityCanBePrivate")

@file:JvmMultifileClass
@file:JvmName("Utils")


package net.mamoe.mirai.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.utils.DeviceInfo.Companion.loadAsDeviceInfo
import java.io.File
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * [Bot] 配置. 用于 [BotFactory.newBot]
 *
 * Kotlin 使用方法:
 * ```
 * val bot = BotFactory.newBot(...) {
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
public actual open class BotConfiguration { // open for Java
    /**
     * 工作目录. 默认为 "."
     */
    public var workingDir: File = File(".")

    ///////////////////////////////////////////////////////////////////////////
    // Coroutines
    ///////////////////////////////////////////////////////////////////////////

    /** 父 [CoroutineContext]. [Bot] 创建后会使用 [SupervisorJob] 覆盖其 [Job], 但会将这个 [Job] 作为父 [Job] */
    public actual var parentCoroutineContext: CoroutineContext = EmptyCoroutineContext

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
     *   bot.eventChannel.subscribe { ... }
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
    public actual suspend inline fun inheritCoroutineContext() {
        parentCoroutineContext = coroutineContext
    }


    ///////////////////////////////////////////////////////////////////////////
    // Connection
    ///////////////////////////////////////////////////////////////////////////

    /** 连接心跳包周期. 过长会导致被服务器断开连接. */
    public actual var heartbeatPeriodMillis: Long = 60.secondsToMillis

    /**
     * 状态心跳包周期. 过长会导致掉线.
     * 该值会在登录时根据服务器下发的配置自动进行更新.
     * @since 2.6
     * @see heartbeatStrategy
     */
    public actual var statHeartbeatPeriodMillis: Long = 300.secondsToMillis

    /**
     * 心跳策略.
     * @since 2.6.3
     */
    public actual var heartbeatStrategy: HeartbeatStrategy = HeartbeatStrategy.STAT_HB

    /**
     * 心跳策略.
     * @since 2.6.3
     */
    public actual enum class HeartbeatStrategy {
        /**
         * 使用 2.6.0 增加的*状态心跳* (Stat Heartbeat). 通常推荐这个模式.
         *
         * 该模式大多数情况下更稳定. 但有些账号使用这个模式时会遇到一段时间后发送消息成功但客户端不可见的问题.
         */
        STAT_HB,

        /**
         * 不发送状态心跳, 而是发送*切换在线状态* (可能会导致频繁的好友或客户端上线提示, 也可能产生短暂 (几秒) 发送消息不可见的问题).
         *
         * 建议在 [STAT_HB] 不可用时使用 [REGISTER].
         */
        REGISTER,

        /**
         * 不主动维护会话. 多数账号会每 16 分钟掉线然后重连. 则会有短暂的不可用时间.
         *
         * 仅当 [STAT_HB] 和 [REGISTER] 都造成无法接收等问题时使用.
         * 同时请在 [https://github.com/mamoe/mirai/issues/1209] 提交问题.
         */
        NONE;
    }

    /**
     * 每次心跳时等待结果的时间.
     * 一旦心跳超时, 整个网络服务将会重启 (将消耗约 1s). 除正在进行的任务 (如图片上传) 会被中断外, 事件和插件均不受影响.
     */
    public actual var heartbeatTimeoutMillis: Long = 5.secondsToMillis

    /** 心跳失败后的第一次重连前的等待时间. */
    @Deprecated(
        "Useless since new network. Please just remove this.",
        level = DeprecationLevel.HIDDEN
    ) // deprecated since 2.7, error since 2.8
    @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.8", hiddenSince = "2.10")
    public actual var firstReconnectDelayMillis: Long = 5.secondsToMillis

    /** 重连失败后, 继续尝试的每次等待时间 */
    @Deprecated(
        "Useless since new network. Please just remove this.",
        level = DeprecationLevel.HIDDEN
    ) // deprecated since 2.7, error since 2.8
    @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.8", hiddenSince = "2.10")
    public actual var reconnectPeriodMillis: Long = 5.secondsToMillis

    /** 最多尝试多少次重连 */
    public actual var reconnectionRetryTimes: Int = Int.MAX_VALUE

    /**
     * 在被挤下线时 ([BotOfflineEvent.Force]) 自动重连. 默认为 `false`.
     *
     * 其他情况掉线都默认会自动重连, 详见 [BotOfflineEvent.reconnect]
     *
     * @since 2.1
     */
    public actual var autoReconnectOnForceOffline: Boolean = false

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
    public actual var loginSolver: LoginSolver? = LoginSolver.Default

    /** 使用协议类型 */
    public actual var protocol: MiraiProtocol = MiraiProtocol.ANDROID_PHONE

    public actual enum class MiraiProtocol {
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

        /**
         * iPad - 来自MiraiGo
         *
         * @since 2.8
         */
        IPAD,

        /**
         * MacOS - 来自MiraiGo
         *
         * @since 2.8
         */
        MACOS,

    }

    /**
     * Highway 通道上传图片, 语音, 文件等资源时的协程数量.
     *
     * 每个协程的速度约为 200KB/s. 协程数量越多越快, 同时也更要求性能.
     * 默认 [CPU 核心数][Runtime.availableProcessors].
     *
     * @since 2.2
     */
    public actual var highwayUploadCoroutineCount: Int = Runtime.getRuntime().availableProcessors()

    /**
     * 设置 [autoReconnectOnForceOffline] 为 `true`, 即在被挤下线时自动重连.
     * @since 2.1
     */
    @ConfigurationDsl
    public actual fun autoReconnectOnForceOffline() {
        autoReconnectOnForceOffline = true
    }

    ///////////////////////////////////////////////////////////////////////////
    // Device
    ///////////////////////////////////////////////////////////////////////////

    @JvmField
    internal actual var accountSecrets: Boolean = true

    /**
     * 禁止保存 `account.secrets`.
     *
     * `account.secrets` 保存账号的会话信息。
     * 它可加速登录过程，也可能可以减少出现验证码的次数。如果遇到一段时间后无法接收消息通知等同步问题时可尝试禁用。
     *
     * @since 2.11
     */
    public actual fun disableAccountSecretes() {
        accountSecrets = false
    }

    /**
     * 设备信息覆盖. 在没有手动指定时将会通过日志警告, 并使用随机设备信息.
     * @see fileBasedDeviceInfo 使用指定文件存储设备信息
     * @see randomDeviceInfo 使用随机设备信息
     */
    public actual var deviceInfo: ((Bot) -> DeviceInfo)? = deviceInfoStub // allows user to set `null` manually.

    /**
     * 使用随机设备信息.
     *
     * @see deviceInfo
     */
    @ConfigurationDsl
    public actual fun randomDeviceInfo() {
        deviceInfo = null
    }

    /**
     * 使用特定由 [DeviceInfo] 序列化产生的 JSON 的设备信息
     *
     * @see deviceInfo
     */
    @ConfigurationDsl
    public actual fun loadDeviceInfoJson(json: String) {
        deviceInfo = {
            DeviceInfoManager.deserialize(json, Companion.json)
        }
    }

    /**
     * 使用文件存储设备信息.
     *
     * 此函数只在 JVM 和 Android 有效. 在其他平台将会抛出异常.
     * @param filepath 文件路径. 默认是相对于 [workingDir] 的文件 "device.json".
     * @see deviceInfo
     */
    @JvmOverloads
    @ConfigurationDsl
    public actual fun fileBasedDeviceInfo(filepath: String) {
        deviceInfo = getFileBasedDeviceInfoSupplier { workingDir.resolve(filepath) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logging
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 日志记录器
     *
     * - 默认打印到标准输出, 通过 [MiraiLogger.create]
     * - 忽略所有日志: [noBotLog]
     * - 重定向到一个目录: `botLoggerSupplier = { DirectoryLogger("Bot ${it.id}") }`
     * - 重定向到一个文件: `botLoggerSupplier = { SingleFileLogger("Bot ${it.id}") }`
     *
     * @see MiraiLogger
     */
    public actual var botLoggerSupplier: ((Bot) -> MiraiLogger) = {
        MiraiLogger.Factory.create(Bot::class, "Bot ${it.id}")
    }

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
    public actual var networkLoggerSupplier: ((Bot) -> MiraiLogger) = {
        MiraiLogger.Factory.create(Bot::class, "Net ${it.id}")
    }


    /**
     * 重定向 [网络日志][networkLoggerSupplier] 到指定目录. 若目录不存在将会自动创建 ([File.mkdirs])
     * 默认目录路径为 "$workingDir/logs/".
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
        networkLoggerSupplier = { DirectoryLogger(identity(it), workingDir.resolve(dir), retain) }
    }

    /**
     * 重定向 [网络日志][networkLoggerSupplier] 到指定文件. 默认文件路径为 "$workingDir/mirai.log".
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
        networkLoggerSupplier = { SingleFileLogger(identity(it), workingDir.resolve(file)) }
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
        identity: (bot: Bot) -> String = { "Bot ${it.id}" }
    ) {
        require(!file.isDirectory) { "file must not be a dir" }
        botLoggerSupplier = { SingleFileLogger(identity(it), workingDir.resolve(file)) }
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
        identity: (bot: Bot) -> String = { "Bot ${it.id}" }
    ) {
        require(!dir.isFile) { "dir must not be a file" }
        botLoggerSupplier = { DirectoryLogger(identity(it), workingDir.resolve(dir), retain) }
    }

    /**
     * 不显示网络日志. 不推荐.
     * @see networkLoggerSupplier 更多日志处理方式
     */
    @ConfigurationDsl
    public actual fun noNetworkLog() {
        networkLoggerSupplier = { _ -> SilentLogger }
    }

    /**
     * 不显示 [Bot] 日志. 不推荐.
     * @see botLoggerSupplier 更多日志处理方式
     */
    @ConfigurationDsl
    public actual fun noBotLog() {
        botLoggerSupplier = { _ -> SilentLogger }
    }

    /**
     * 是否显示过于冗长的事件日志
     *
     * 默认为 `false`
     *
     * @since 2.8
     */
    public actual var isShowingVerboseEventLog: Boolean = false

    ///////////////////////////////////////////////////////////////////////////
    // Cache
    //////////////////////////////////////////////////////////////////////////

    /**
     * 缓存数据目录, 相对于 [workingDir].
     *
     * 缓存目录保存的内容均属于不稳定的 Mirai 内部数据, 请不要手动修改它们. 清空缓存不会影响功能. 只会导致一些操作如读取全部群列表要重新进行.
     * 默认启用的缓存可以加快登录过程.
     *
     * 注意: 这个目录只存储能在 [BotConfiguration] 配置的内容, 即包含:
     * - 联系人列表
     * - 登录服务器列表
     * - 资源服务秘钥
     *
     * 其他内容如通过 [InputStream] 发送图片时的缓存使用 [FileCacheStrategy], 默认使用系统临时文件且会在关闭时删除文件.
     *
     * @since 2.4
     */
    public var cacheDir: File = File("cache")

    /**
     * 联系人信息缓存配置. 将会保存在 [cacheDir] 中 `contacts` 目录
     * @since 2.4
     */
    public actual var contactListCache: ContactListCache = ContactListCache()

    /**
     * 联系人信息缓存配置
     * @see contactListCache
     * @see enableContactCache
     * @see disableContactCache
     * @since 2.4
     */
    public actual class ContactListCache {
        /**
         * 在有修改时自动保存间隔. 默认 60 秒. 在每次登录完成后有修改时都会立即保存一次.
         */
        public actual var saveIntervalMillis: Long = 60_000

        /**
         * 在有修改时自动保存间隔. 默认 60 秒. 在每次登录完成后有修改时都会立即保存一次.
         */ // was @ExperimentalTime before 2.9
        public actual inline var saveInterval: Duration
            @JvmSynthetic inline get() = saveIntervalMillis.milliseconds
            @JvmSynthetic inline set(v) {
                saveIntervalMillis = v.inWholeMilliseconds
            }

        /**
         * 开启好友列表缓存.
         */
        public actual var friendListCacheEnabled: Boolean = false

        /**
         * 开启群成员列表缓存.
         */
        public actual var groupMemberListCacheEnabled: Boolean = false
    }

    /**
     * 配置 [ContactListCache]
     * ```
     * contactListCache {
     *     saveIntervalMillis = 30_000
     *     friendListCacheEnabled = true
     * }
     * ```
     * @since 2.4
     */
    @JvmSynthetic
    public actual inline fun contactListCache(action: ContactListCache.() -> Unit) {
        action.invoke(this.contactListCache)
    }

    /**
     * 禁用好友列表和群成员列表的缓存.
     * @since 2.4
     */
    @ConfigurationDsl
    public actual fun disableContactCache() {
        contactListCache.friendListCacheEnabled = false
        contactListCache.groupMemberListCacheEnabled = false
    }

    /**
     * 启用好友列表和群成员列表的缓存.
     * @since 2.4
     */
    @ConfigurationDsl
    public actual fun enableContactCache() {
        contactListCache.friendListCacheEnabled = true
        contactListCache.groupMemberListCacheEnabled = true
    }

    /**
     * 登录缓存.
     *
     * 开始后在密码登录成功时会保存秘钥等信息, 在下次启动时通过这些信息登录, 而不提交密码.
     * 可以减少验证码出现的频率.
     *
     * 秘钥信息会由密码加密保存. 如果秘钥过期, 则会进行普通密码登录.
     *
     * 默认 `true` (开启).
     *
     * @since 2.6
     */
    public actual var loginCacheEnabled: Boolean = true

    ///////////////////////////////////////////////////////////////////////////
    // Misc
    ///////////////////////////////////////////////////////////////////////////

    @Suppress("DuplicatedCode")
    public actual fun copy(): BotConfiguration {
        return BotConfiguration().also { new ->
            // To structural order
            new.workingDir = workingDir
            @Suppress("DEPRECATION_ERROR")
            new.parentCoroutineContext = parentCoroutineContext
            new.heartbeatPeriodMillis = heartbeatPeriodMillis
            new.heartbeatTimeoutMillis = heartbeatTimeoutMillis
            new.statHeartbeatPeriodMillis = statHeartbeatPeriodMillis
            new.heartbeatStrategy = heartbeatStrategy
            new.reconnectionRetryTimes = reconnectionRetryTimes
            new.autoReconnectOnForceOffline = autoReconnectOnForceOffline
            new.loginSolver = loginSolver
            new.protocol = protocol
            new.highwayUploadCoroutineCount = highwayUploadCoroutineCount
            new.accountSecrets = accountSecrets
            new.deviceInfo = deviceInfo
            new.botLoggerSupplier = botLoggerSupplier
            new.networkLoggerSupplier = networkLoggerSupplier
            new.cacheDir = cacheDir
            new.contactListCache = contactListCache
            new.convertLineSeparator = convertLineSeparator
            new.isShowingVerboseEventLog = isShowingVerboseEventLog
        }
    }

    /**
     * 是否处理接受到的特殊换行符, 默认为 `true`
     *
     * - 若为 `true`, 会将收到的 `CRLF(\r\n)` 和 `CR(\r)` 替换为 `LF(\n)`
     * - 若为 `false`, 则不做处理
     *
     * @since 2.4
     */
    @get:JvmName("isConvertLineSeparator")
    public actual var convertLineSeparator: Boolean = true

    /** 标注一个配置 DSL 函数 */
    @Target(AnnotationTarget.FUNCTION)
    @DslMarker
    public actual annotation class ConfigurationDsl

    public actual companion object {
        /** 默认的配置实例. 可以进行修改 */
        @JvmStatic
        public actual val Default: BotConfiguration = BotConfiguration()

        /**
         * Json 序列化器, 使用 'kotlinx.serialization'
         */
        internal val json: Json = kotlin.runCatching {
            Json {
                isLenient = true
                ignoreUnknownKeys = true
                prettyPrint = true
            }
        }.getOrElse {
            @Suppress("JSON_FORMAT_REDUNDANT_DEFAULT") // compatible for older versions
            Json {}
        }

        internal fun BotConfiguration.getFileBasedDeviceInfoSupplier(file: () -> File): (Bot) -> DeviceInfo {
            return {
                @Suppress("DEPRECATION_ERROR")
                file().loadAsDeviceInfo(json)
            }
        }
    }
}