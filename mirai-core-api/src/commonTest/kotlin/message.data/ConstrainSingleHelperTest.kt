/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.safeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ConstrainSingleHelperTest {

    @Test
    fun linearPlains() {
        val list = listOf(PlainText("1"), PlainText("2"))
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(list, value)
            assertFalse { hasConstrainSingle }
        }
    }

    @Test
    fun linearNonConstrains() {
        val list = listOf(PlainText("1"), At(2))
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(list, value)
            assertFalse { hasConstrainSingle }
        }
    }

    @Test
    fun hierarchicalNonConstrains() {
        val list = listOf(PlainText("1"), (PlainText("1") + At(2)))
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(listOf(PlainText("1"), PlainText("1"), At(2)), value)
            assertFalse { hasConstrainSingle }
        }
    }

    @Test
    fun singleConstrain() {
        val list = listOf(Dice(2))
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(list, value)
            assertTrue { hasConstrainSingle }
        }
    }

    @Test
    fun linearDuplicatedConstrains() {
        val list = listOf(Dice(2), Dice(3))
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(1, value.size)
            assertEquals(Dice(3), value.first())
            assertTrue { hasConstrainSingle }
        }
    }

    @Test
    fun linearMultipleDuplicatedConstrains() {
        val list = listOf(PlainText("a"), LightApp("aa"), Dice(2), Dice(3), LightApp("bb"), PlainText("c"))
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(1, value.size)
            assertEquals(listOf(LightApp("bb")), value)
            assertTrue { hasConstrainSingle }
        }
    }

    @Test
    fun hierarchicalDuplicatedConstrains() {
        val list = listOf(
            MySingle(1), // before first MessageContent that can be replaced by the last ConstrainSingle
            PlainText("a"),
            LightApp("aa"),
            LinearMessageChainImpl.create( // without processing ConstrainSingle
                listOf(
                    PlainText("ab"),
                    LightApp("aab"),
                    Dice(5),
                    Dice(3),
                    LightApp("bbb"),
                    PlainText("cb")
                ), true
            ),
            Dice(3),
            LightApp("bb"), // last ConstrainSingle
            PlainText("c")
        )
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(2, value.size)
            assertEquals(listOf(MySingle(1), LightApp("bb")), value)
            assertTrue { hasConstrainSingle }
        }
    }

    @Test
    fun hierarchicalDuplicatedConstrains3() {
        val list = listOf(
            PlainText("a"),
            LightApp("aa"),
            LinearMessageChainImpl.create( // without processing ConstrainSingle
                listOf(
                    PlainText("ab"),
                    LightApp("aab"),
                    Dice(5),
                    Dice(3),
                    LightApp("bbb"),
                    PlainText("cb")
                ), true
            ),
            Dice(3),
            MySingle(1), // after first MessageContent that can be replaced by the last ConstrainSingle
            LightApp("bb"), // last ConstrainSingle
            PlainText("c")
        )
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(2, value.size)
            assertEquals(listOf(LightApp("bb"), MySingle(1)), value)
            assertTrue { hasConstrainSingle }
        }
    }

    @Test
    fun hierarchicalDuplicatedConstrains2() {
        val list = listOf(
            PlainText("a"),
            LightApp("aa"),
            Dice(3),
            LightApp("bb"),
            PlainText("c"),
            LinearMessageChainImpl.create( // without processing ConstrainSingle
                listOf(
                    PlainText("a"),
                    LightApp("aa"),
                    Dice(2),
                    MySingle(1), // after first MessageContent that can be replaced by the last ConstrainSingle
                    Dice(3),
                    LightApp("foo"), // last ConstrainSingle
                    PlainText("c")
                ), true
            ),
        )
        ConstrainSingleHelper.constrainSingleMessages(list.asSequence()).run {
            assertEquals(2, value.size)
            assertEquals(listOf(LightApp("foo"), MySingle(1)), value)
            assertTrue { hasConstrainSingle }
        }
    }
}

private class MySingle(
    val value: Int
) : MessageMetadata, ConstrainSingle {
    override val key: MessageKey<MySingle>
        get() = Key

    override fun toString(): String {
        return "MySingle@${this.hashCode()}"
    }

    override fun equals(other: Any?): Boolean {
        return other is MySingle && other.value == this.value
    }

    override fun hashCode(): Int {
        return value
    }

    private object Key : AbstractMessageKey<MySingle>({ it.safeCast() })
}