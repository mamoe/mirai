/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.utils.Context
import net.mamoe.mirai.utils.DeviceInfo

/**
 * 通过本机信息来获取设备信息.
 *
 * Android: 获取手机信息, 与 QQ 官方相同.
 * JVM: 部分为常量, 部分为随机
 */
open expect class SystemDeviceInfo(context: Context) : DeviceInfo