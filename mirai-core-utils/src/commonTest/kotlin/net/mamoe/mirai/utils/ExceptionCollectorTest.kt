/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

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
        assertTrue { collector.getLast()!!.suppressed.single() is IllegalArgumentException }
        assertEquals(2, collector.asSequence().count())
    }

    @Test
    fun `can collect suppressed nested`() {
        val collector = ExceptionCollector()

        collector.collect(StackOverflowError())
        collector.collect(IllegalArgumentException())
        collector.collect(IllegalStateException())

        assertIs<IllegalStateException>(collector.getLast())
        assertTrue { collector.getLast()!!.suppressed.single() is IllegalArgumentException }
        assertTrue { collector.getLast()!!.suppressed.single()!!.suppressed.single() is StackOverflowError }
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
        assertEquals(0, collector.getLast()!!.suppressed.size)
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

        assertEquals(0, collector.getLast()!!.suppressed.size)
        assertEquals(1, collector.asSequence().count())
        assertSame(exceptions.first(), collector.getLast())
        assertEquals("#0", collector.getLast()!!.message)
    }
}