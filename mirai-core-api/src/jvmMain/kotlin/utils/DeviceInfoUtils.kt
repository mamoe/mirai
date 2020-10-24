/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.json.Json
import java.io.File

/**
 * 加载一个设备信息. 若文件不存在或为空则随机并创建一个设备信息保存.
 */
public fun File.loadAsDeviceInfo(json: Json): DeviceInfo {
    if (!this.exists() || this.length() == 0L) {
        return DeviceInfo.random().also {
            this.writeText(json.encodeToString(DeviceInfo.serializer(), it))
        }
    }
    return json.decodeFromString(DeviceInfo.serializer(), this.readText())
}