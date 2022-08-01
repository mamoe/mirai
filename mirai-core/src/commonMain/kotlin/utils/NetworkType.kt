/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlin.jvm.JvmInline

/**
 * 连接类型
 */
@JvmInline
internal value class NetworkType(val value: Int) {
    companion object {
        /**
         * 移动网络
         */
        val MOBILE = NetworkType(1)

        /**
         * Wifi
         */
        val WIFI = NetworkType(2)

        /**
         * 其他任何类型
         */
        val OTHER = NetworkType(0)
    }
}