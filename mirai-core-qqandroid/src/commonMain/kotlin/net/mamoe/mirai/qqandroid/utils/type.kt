/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils


fun Int.toIpV4AddressString(): String {
    @Suppress("NAME_SHADOWING")
    var var0 = this.toLong() and 0xFFFFFFFF
    return buildString {
        for (var2 in 3 downTo 0) {
            append(255L and var0 % 256L)
            var0 /= 256L
            if (var2 != 0) {
                append('.')
            }
        }
    }
}