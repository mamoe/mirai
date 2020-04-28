/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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