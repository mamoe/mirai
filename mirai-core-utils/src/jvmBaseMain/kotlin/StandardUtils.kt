/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import java.net.Inet4Address

internal actual fun isSameClassPlatform(object1: Any, object2: Any): Boolean {
    return object1.javaClass == object2.javaClass
}

public actual fun localIpAddress(): String = runCatching {
    Inet4Address.getLocalHost().hostAddress
}.getOrElse { "192.168.1.123" }

public actual fun availableProcessors(): Int = Runtime.getRuntime().availableProcessors()