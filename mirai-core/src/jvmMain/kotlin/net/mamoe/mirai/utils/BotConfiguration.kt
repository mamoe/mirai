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

import net.mamoe.mirai.Bot
import java.io.File

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
public actual open class BotConfiguration : BotConfigurationBase() { // open for Java
    /**
     * 设备信息覆盖. 在没有手动指定时将会通过日志警告, 并使用随机设备信息.
     * @see fileBasedDeviceInfo 使用指定文件存储设备信息
     * @see randomDeviceInfo 使用随机设备信息
     */
    public actual var deviceInfo: ((Context) -> DeviceInfo)? = deviceInfoStub

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
     * 使用特定由 [DeviceInfoData] 序列化产生的 JSON 的设备信息
     *
     * @see deviceInfo
     */
    @SinceMirai("1.2.0")
    public actual fun loadDeviceInfoJson(json: String) {
        deviceInfo = { context ->
            this.json.decodeFromString(DeviceInfoData.serializer(), json).also { it.context = context }
        }
    }

    /**
     * 重定向 [网络日志][networkLoggerSupplier] 到指定目录. 若目录不存在将会自动创建 ([File.mkdirs])
     * @see DirectoryLogger
     * @see redirectNetworkLogToDirectory
     */
    @JvmOverloads
    @ConfigurationDsl
    @SinceMirai("1.1.0")
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
    @SinceMirai("1.1.0")
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
    @SinceMirai("1.1.0")
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
    @SinceMirai("1.1.0")
    public fun redirectBotLogToFile(
        file: File = File("mirai.log"),
        identity: (bot: Bot) -> String = { "Net ${it.id}" }
    ) {
        require(!file.isDirectory) { "file must not be a dir" }
        file.createNewFile()
        botLoggerSupplier = { SingleFileLogger(identity(it), file) }
    }

    @Suppress("ACTUAL_WITHOUT_EXPECT")
    public actual enum class MiraiProtocol actual constructor(
        /** 协议模块使用的 ID */
        @JvmField internal actual val id: Long
    ) {
        /**
         * Android 手机.
         */
        ANDROID_PHONE(537062845),

        /**
         * Android 平板.
         */
        ANDROID_PAD(537062409),

        /**
         * Android 手表.
         * */
        @SinceMirai("1.1.0")
        ANDROID_WATCH(537061176)
    }

    public actual companion object {
        /** 默认的配置实例. 可以进行修改 */
        @JvmStatic
        public actual val Default: BotConfiguration = BotConfiguration()
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

    public actual fun copy(): BotConfiguration {
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
            new.fileCacheStrategy = fileCacheStrategy
        }
    }
}
