@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.test

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DslMarker
internal annotation class TestDSL

@TestDSL
fun Boolean.shouldBeTrue() = assertTrue { this }

@TestDSL
fun Boolean.shouldBeFalse() = assertFalse { this }

@TestDSL
infix fun <E> E.shouldBeEqualTo(another: E) = assertEquals(another, this)

@TestDSL
infix fun <E> E.shouldNotBeEqualTo(another: E) = assertNotEquals(another, this)
