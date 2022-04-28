/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.utils.structureToString
import net.mamoe.mirai.internal.utils.structureToStringIfAvailable
import kotlin.test.assertNotNull
import kotlin.test.asserter

internal interface EqualityAsserter {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    fun <@kotlin.internal.OnlyInputTypes T> assertEquals(
        expected: List<T>,
        actual: List<T>,
        message: String? = null
    )

    object Ordinary : EqualityAsserter {
        override fun <T> assertEquals(expected: List<T>, actual: List<T>, message: String?) {
            if (expected.size == actual.size) {
                if (expected.size == 1 && expected.singleOrNull() == actual.singleOrNull()) {
                    return asserter.assertEquals(message, expected.single(), actual.single())
                }

                if (expected.zip(actual).all { (e, a) -> e == a }) return

                asserter.assertEquals(message, expected, actual)
            } else {
                asserter.assertEquals(message, expected, actual)
            }
        }
    }

    object Structural : EqualityAsserter {
        override fun <T> assertEquals(expected: List<T>, actual: List<T>, message: String?) {
            if (expected.size == 1 && actual.size == 1) {
                val e = expected.single()
                val a = actual.single()
                if (a == null || e == null) {
                    asserter.assertEquals(
                        "[Null] $message",
                        structureToStringOrOrdinaryString(e),
                        structureToStringOrOrdinaryString(a)
                    )
                    assertNotNull(a, message)
                    assertNotNull(e, message)
                }
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                if (!e!!::class.isInstance(a) && !a!!::class.isInstance(e)) {
                    asserter.assertEquals(
                        "[Incompatible type] $message",
                        structureToStringOrOrdinaryString(e),
                        structureToStringOrOrdinaryString(a)
                    )
                    return
                }
                asserter.assertEquals(
                    message,
                    structureToStringOrOrdinaryString(e),
                    structureToStringOrOrdinaryString(a)
                )
            } else {
                asserter.assertEquals(
                    message,
                    expected.joinToString { structureToStringOrOrdinaryString(it) },
                    actual.joinToString { structureToStringOrOrdinaryString(it) })
            }
        }

        private fun <T> structureToStringOrOrdinaryString(it: T): String =
            it.structureToString().ifBlank {
                it.structureToStringIfAvailable() ?: error("structureToString is not available")
            }
    }

    object OrdinaryThenStructural : EqualityAsserter {
        override fun <T> assertEquals(expected: List<T>, actual: List<T>, message: String?) {
            try {
                Ordinary.assertEquals(expected, actual, message)
                return
            } catch (e: AssertionError) {
                Structural.assertEquals(expected, actual, message)
                return
            }
        }

    }
}