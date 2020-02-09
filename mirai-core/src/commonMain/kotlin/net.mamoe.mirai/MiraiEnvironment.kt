/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

/**
 * 平台相关环境属性
 */
expect object MiraiEnvironment {
    val platform: Platform
}

/**
 * 可用平台列表
 */
enum class Platform {
    ANDROID,
    JVM
}