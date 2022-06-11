/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

internal actual fun hash(e: Throwable): Long {
    var hashCode = 1L
    val trace = e.getStackTraceAddresses()
    for (stackTraceAddress in trace) {
        hashCode = (hashCode xor stackTraceAddress).shl(1)
    }

    // Somehow stacktrace analysis is on my own Windows machine but not on GitHub Actions.
    // Hashing with a class to tentatively not filter out different types.
    return hashCode xor e::class.hashCode().toLongUnsigned()
}