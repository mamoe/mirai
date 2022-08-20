/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.util

import kotlin.math.floor


private const val MEM_B = 1024L
private const val MEM_KB = 1024L shl 10
private const val MEM_MB = 1024L shl 20
private const val MEM_GB = 1024L shl 30

@Suppress("NOTHING_TO_INLINE")
private inline fun StringBuilder.appendDouble(number: Double): StringBuilder =
    append(floor(number * 100) / 100)

internal fun renderMemoryUsageNumber(num: Long) = buildString {
    renderMemoryUsageNumber(this, num)
}

internal fun renderMemoryUsageNumber(builder: StringBuilder, num: Long) {
    when {
        num == -1L -> {
            builder.append(num)
        }
        num < MEM_B -> {
            builder.append(num).append("B")
        }
        num < MEM_KB -> {
            builder.appendDouble(num / 1024.0).append("KB")
        }
        num < MEM_MB -> {
            builder.appendDouble((num ushr 10) / 1024.0).append("MB")
        }
        else -> {
            builder.appendDouble((num ushr 20) / 1024.0).append("GB")
        }
    }
}

private var emptyLine = "    ".repeat(10)
internal fun Appendable.emptyLine(size: Int) {
    if (emptyLine.length <= size) {
        emptyLine = String(CharArray(size) { ' ' })
    }
    append(emptyLine, 0, size)
}

internal inline fun AnsiMessageBuilder.renderMUNum(size: Int, contentLength: Int, code: () -> Unit) {
    val s = size - contentLength
    val left = s / 2
    val right = s - left
    emptyLine(left)
    code()
    emptyLine(right)
}
