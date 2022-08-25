/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.*

@OptIn(TestOnly::class)
internal class ExceptionCollectorTest {

    @Test
    fun `can collect`() {
        val collector = ExceptionCollector()

        collector.collect(IllegalArgumentException())

        assertTrue { collector.getLast() is IllegalArgumentException }
        assertEquals(1, collector.asSequence().count())
    }

    @Test
    fun `can collect suppressed`() {
        val collector = ExceptionCollector()

        collector.collect(IllegalArgumentException())
        collector.collect(IllegalStateException())

        assertIs<IllegalStateException>(collector.getLast())
        assertTrue { collector.getLast()!!.suppressedExceptions[0] is IllegalArgumentException }
        assertEquals(2, collector.asSequence().count())
    }

    @Test
    fun `can collect suppressed nested`() {
        val collector = ExceptionCollector()

        collector.collect(IndexOutOfBoundsException())
        collector.collect(IllegalArgumentException())
        collector.collect(IllegalStateException())

        assertIs<IllegalStateException>(collector.getLast())
        assertTrue { collector.getLast()!!.suppressedExceptions[0] is IllegalArgumentException }
        assertTrue { collector.getLast()!!.suppressedExceptions[1] is IndexOutOfBoundsException }
        assertEquals(3, collector.asSequence().count())
    }

    @Test
    fun `ignore same exception`() {
        val collector = ExceptionCollector()

        val exception = Exception()

        collector.collect(exception)
        collector.collect(exception)
        collector.collect(exception)

        assertSame(exception, collector.asSequence().last())
        assertEquals(0, collector.getLast()!!.suppressedExceptions.size)
        assertEquals(1, collector.asSequence().count())
    }

    @Test
    fun `ignore exception with same stacktrace and preserve first occurrence`() {
        val exceptions = mutableListOf<Exception>()

        repeat(5) { id ->
            exceptions.add(Exception("#$id"))
        }


        val collector = ExceptionCollector()
        exceptions.forEach { exception ->
            collector.collect(exception)
        }

        assertEquals(0, collector.getLast()!!.suppressedExceptions.size)
        assertEquals(1, collector.asSequence().count())
        assertSame(exceptions.first(), collector.getLast())
        assertEquals("#0", collector.getLast()!!.message)
    }
}