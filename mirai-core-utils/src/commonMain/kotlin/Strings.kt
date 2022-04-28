/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

public fun CharSequence.chineseLength(upTo: Int = Int.MAX_VALUE): Int {
    return this.sumUpTo(upTo) { it.chineseLength }
}

public val Char.chineseLength: Int
    get() {
        return when (this) {
            in '\u0000'..'\u007F' -> 1
            in '\u0080'..'\u07FF' -> 2
            in '\u0800'..'\uFFFF' -> 3
            else -> 4
        }
    }

public inline fun CharSequence.sumUpTo(upTo: Int, selector: (Char) -> Int): Int {
    var sum = 0
    for (element in this) {
        sum += selector(element)
        if (sum >= upTo) {
            return sum
        }
    }
    return sum
}
