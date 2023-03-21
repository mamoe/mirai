/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import net.mamoe.mirai.Bot

/**
 * [BotConfiguration] 的 Native 平台特别配置
 * @since 2.15
 */
@NotStableForInheritance
public actual abstract class AbstractBotConfiguration { // open for Java
    protected actual abstract var deviceInfo: ((Bot) -> DeviceInfo)?
    protected actual abstract var networkLoggerSupplier: ((Bot) -> MiraiLogger)
    protected actual abstract var botLoggerSupplier: ((Bot) -> MiraiLogger)

    /**
     * 工作目录. 默认为当前目录
     */
    public var workingDir: String = "."

    /**
     * 使用文件存储设备信息.
     *
     * 此函数只在 JVM 和 Android 有效. 在其他平台将会抛出异常.
     * @param filepath 文件路径. 默认是相对于 [workingDir] 的文件 "device.json".
     * @see deviceInfo
     */
    @BotConfiguration.ConfigurationDsl
    public actual fun fileBasedDeviceInfo(filepath: String) {
        deviceInfo = {
            val file = MiraiFile.create(workingDir).resolve(filepath)
            if (!file.exists()) {
                file.writeText(DeviceInfoManager.serialize(DeviceInfo.random(), BotConfiguration.json))
            }
            DeviceInfoManager.deserialize(file.readText(), BotConfiguration.json)
        }
    }

    /**
     * 缓存数据目录路径. 若 [cacheDir] 为绝对路径, 将解析该绝对路径, 否则作为相对于 [workingDir] 的路径解析.
     * 例如, `cache` 将会解析为 `$workingDir/cache`, 而 `/Users/Chisato/Desktop/bot/cache` 指代绝对路径, 将解析为绝对路径.
     *
     * 缓存目录保存的内容均属于不稳定的 Mirai 内部数据, 请不要手动修改它们. 清空缓存不会影响功能. 只会导致一些操作如读取全部群列表要重新进行.
     * 默认启用的缓存可以加快登录过程.
     *
     * 注意: 这个目录只存储能在 [BotConfiguration] 配置的内容, 即包含:
     * - 联系人列表
     * - 登录服务器列表
     * - 资源服务秘钥
     *
     * 其他内容如通过 [Input] 发送图片时的缓存使用 [FileCacheStrategy], 默认使用系统临时文件且会在关闭时删除文件.
     *
     * @since 2.4
     */
    public var cacheDir: String = "cache"

    internal actual fun applyMppCopy(new: BotConfiguration) {
        new.workingDir = workingDir
        new.cacheDir = cacheDir
    }
}