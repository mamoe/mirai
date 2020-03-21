package net.mamoe.mirai.message.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

internal class CombinedMessageTest {

    @Test
    fun testAsSequence() {
        var message: Message = "Hello ".toMessage()
        message += "World"

        assertEquals(
            "Hello World",
            (message as CombinedMessage).asSequence().joinToString(separator = "")
        )
    }

    @Test
    fun testAsSequence2() {
        var message: Message = "Hello ".toMessage()
        message += listOf(
            PlainText("W"),
            PlainText("o"),
            PlainText("r") + PlainText("ld")
        ).asMessageChain()

        assertEquals(
            "Hello World",
            (message as CombinedMessage).asSequence().joinToString(separator = "")
        )
    }

    private val toAdd = "1".toMessage()

    @OptIn(ExperimentalTime::class)
    @Test
    fun speedTest() = repeat(100) {
        var count = 1L

        repeat(Int.MAX_VALUE) {
            count++
        }

        var combineMessage: Message = toAdd

        println(
            "init combine ok " + measureTime {
                repeat(1000) {
                    combineMessage += toAdd
                }
            }.inMilliseconds
        )

        val list = mutableListOf<Message>()
        println(
            "init messageChain ok " + measureTime {
                repeat(1000) {
                    list += toAdd
                }
            }.inMilliseconds
        )

        measureTime {
            list.joinToString(separator = "")
        }.let { time ->
            println("list foreach: ${time.inMilliseconds} ms")
        }

        measureTime {
            (combineMessage as CombinedMessage).iterator().joinToString(separator = "")
        }.let { time ->
            println("combined iterate: ${time.inMilliseconds} ms")
        }

        measureTime {
            (combineMessage as CombinedMessage).asSequence().joinToString(separator = "")
        }.let { time ->
            println("combined sequence: ${time.inMilliseconds} ms")
        }

        repeat(5) {
            println()
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testFastIteration() {
        println("start!")
        println("start!")
        println("start!")
        println("start!")

        var combineMessage: Message = toAdd

        println(
            "init combine ok " + measureTime {
                repeat(1000) {
                    combineMessage += toAdd
                }
            }.inMilliseconds
        )

        measureTime {
            (combineMessage as CombinedMessage).iterator().joinToString(separator = "")
        }.let { time ->
            println("combine: ${time.inMilliseconds} ms")
        }
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
    if (limit >= 0 && count > limit) buffer.append(truncated)
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