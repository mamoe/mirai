/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import platform.posix._SC_NPROCESSORS_ONLN
import platform.posix.sysconf


public actual fun localIpAddress(): String = "192.168.1.123"

public actual fun availableProcessors(): Int = sysconf(_SC_NPROCESSORS_ONLN).toInt()

internal actual fun isSameClassPlatform(object1: Any, object2: Any): Boolean {
    return object1::class == object2::class
}