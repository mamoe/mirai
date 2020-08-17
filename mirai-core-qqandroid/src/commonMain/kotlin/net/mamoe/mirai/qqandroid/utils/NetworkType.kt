/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

/**
 * 连接类型
 */
internal inline class NetworkType(val value: Int) {
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