/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package  net.mamoe.mirai.message.data

import kotlin.test.Test
import kotlin.test.assertEquals


internal class CombinedMessageTest {


    @Test
    fun testAsSequence() {
        var message: Message = PlainText("Hello ")
        message += "World"

        assertEquals(
            "Hello World",
            message.flatten().joinToString(separator = "")
        )
    }

    @Test
    fun testAsSequence2() {
        var message: Message = PlainText("Hello ")
        message += listOf(
            PlainText("W"),
            PlainText("o"),
            PlainText("r") + PlainText("ld")
        ).asMessageChain()

        assertEquals(
            "Hello World",
            message.flatten().joinToString(separator = "")
        )
    }
}

fun <T> Iterator<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated, transform).toString()
}

fun <T, A : Appendable> Iterator<T>.joinTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)
        if (limit < 0 || count <= limit) {
            buffer.appendElement(element, transform)
        } else break
    }
    if (limit in 0 until count) buffer.append(truncated)
    buffer.append(postfix)
    return buffer
}

internal fun <T> Appendable.appendElement(element: T, transform: ((T) -> CharSequence)?) {
    when {
        transform != null -> append(transform(element))
        element is CharSequence? -> append(element)
        element is Char -> append(element)
        else -> append(element.toString())
    }
}