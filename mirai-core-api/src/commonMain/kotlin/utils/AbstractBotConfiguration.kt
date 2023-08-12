/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DeviceInfo.Companion.loadAsDeviceInfo
import java.io.File
import java.io.InputStream


/**
 * [BotConfiguration] 的 JVM 平台特别配置
 * @since 2.15
 */
@NotStableForInheritance
public abstract class AbstractBotConfiguration { // open for Java
    protected abstract var deviceInfo: ((Bot) -> DeviceInfo)?
    protected abstract var networkLoggerSupplier: ((Bot) -> MiraiLogger)
    protected abstract var botLoggerSupplier: ((Bot) -> MiraiLogger)


    /**
     * 工作目录. 默认为 "."
     */
    public var workingDir: File = File(".")

    ///////////////////////////////////////////////////////////////////////////
    // Device
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 使用文件存储设备信息.
     *
     * 此函数只在 JVM 和 Android 有效. 在其他平台将会抛出异常.
     * @param filepath 文件路径. 默认是相对于 [workingDir] 的文件 "device.json".
     * @see deviceInfo
     */
    @JvmOverloads
    @BotConfiguration.ConfigurationDsl
    public fun fileBasedDeviceInfo(filepath: String = "device.json") {
        deviceInfo = {
            workingDir.resolve(filepath).loadAsDeviceInfo(BotConfiguration.json)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logging
    ///////////////////////////////////////////////////////////////////////////


    /**
     * 重定向 [网络日志][networkLoggerSupplier] 到指定目录. 若目录不存在将会自动创建 ([File.mkdirs])
     * 默认目录路径为 "$workingDir/logs/".
     * @see DirectoryLogger
     * @see redirectNetworkLogToDirectory
     */
    @JvmOverloads
    @BotConfiguration.ConfigurationDsl
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
    @BotConfiguration.ConfigurationDsl
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
    @BotConfiguration.ConfigurationDsl
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
    @BotConfiguration.ConfigurationDsl
    public fun redirectBotLogToDirectory(
        dir: File = File("logs"),
        retain: Long = 1.weeksToMillis,
        identity: (bot: Bot) -> String = { "Bot ${it.id}" }
    ) {
        require(!dir.isFile) { "dir must not be a file" }
        botLoggerSupplier = { DirectoryLogger(identity(it), workingDir.resolve(dir), retain) }
    }

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

    ///////////////////////////////////////////////////////////////////////////
    // Misc
    ///////////////////////////////////////////////////////////////////////////

    internal fun applyMppCopy(new: BotConfiguration) {
        new.workingDir = workingDir
        new.cacheDir = cacheDir
    }
}
