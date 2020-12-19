/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("Utils")
@file:JvmMultifileClass

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.AtAll.display
import net.mamoe.mirai.utils.safeCast


internal fun Int.toIpV4AddressString(): String {
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

internal fun String.chineseLength(upTo: Int): Int {
    return this.sumUpTo(upTo) {
        when (it) {
            in '\u0000'..'\u007F' -> 1
            in '\u0080'..'\u07FF' -> 2
            in '\u0800'..'\uFFFF' -> 3
            else -> 4
        }
    }
}

internal fun MessageChain.estimateLength(target: ContactOrBot, upTo: Int): Int =
    sumUpTo(upTo) { it, up ->
        it.estimateLength(target, up)
    }

internal fun SingleMessage.estimateLength(target: ContactOrBot, upTo: Int): Int {
    return when (this) {
        is QuoteReply -> 444 + this.source.originalMessage.estimateLength(target, upTo) // Magic number
        is Image -> 260 // Magic number
        is PlainText -> content.chineseLength(upTo)
        is At -> this.getDisplay(target.safeCast()).chineseLength(upTo)
        is AtAll -> display.chineseLength(upTo)
        else -> this.toString().chineseLength(upTo)
    }
}

internal inline fun <T> Iterable<T>.sumUpTo(upTo: Int, selector: (T, remaining: Int) -> Int): Int {
    var sum = 0
    for (element in this) {
        if (sum >= upTo) {
            return sum
        }
        sum += selector(element, (upTo - sum).coerceAtLeast(0))
    }
    return sum
}

internal inline fun CharSequence.sumUpTo(upTo: Int, selector: (Char) -> Int): Int {
    var sum = 0
    for (element in this) {
        sum += selector(element)
        if (sum >= upTo) {
            return sum
        }
    }
    return sum
}
