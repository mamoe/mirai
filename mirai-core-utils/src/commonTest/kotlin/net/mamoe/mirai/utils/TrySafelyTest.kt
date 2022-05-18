/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.errors.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class TrySafelyTest {
    @Test
    fun `can run block`() {
        assertEquals(
            1,
            trySafely(
                block = { 1 },
                finally = { }
            )
        )
    }

    @Test
    fun `can run finally when no exception in block`() {
        var x = 0
        trySafely(
            block = { },
            finally = { x = 1 }
        )
        assertEquals(1, x)
    }

    @Test
    fun `can run finally when exception in block`() {
        var x = 0
        assertFailsWith<Exception> {
            trySafely(
                block = { throw Exception() },
                finally = { x = 1 }
            )
        }
        assertEquals(1, x)
    }

    @Test
    fun `can run finally catching`() {
        assertFailsWith<NoSuchElementException> {
            trySafely(
                block = { throw NoSuchElementException() },
                finally = { throw IOException("") }
            )
        }.let { e ->
            assertIs<IOException>(e.suppressedExceptions.single())
        }
    }
}