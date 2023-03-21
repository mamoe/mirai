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
import kotlin.jvm.JvmOverloads


/**
 * [BotConfiguration] 的平台特别配置
 * @since 2.15
 */
public expect abstract class AbstractBotConfiguration protected constructor() {
    protected abstract var deviceInfo: ((Bot) -> DeviceInfo)?
    protected abstract var networkLoggerSupplier: ((Bot) -> MiraiLogger)
    protected abstract var botLoggerSupplier: ((Bot) -> MiraiLogger)

    /**
     * 使用文件存储设备信息.
     *
     * 此函数只在 JVM 和 Android 有效. 在其他平台将会抛出异常.
     * @param filepath 文件路径. 默认是相对于 `workingDir` 的文件 "device.json".
     * @see BotConfiguration.deviceInfo
     */
    @JvmOverloads
    @BotConfiguration.ConfigurationDsl
    public fun fileBasedDeviceInfo(filepath: String = "device.json")

    internal fun applyMppCopy(new: BotConfiguration)
}