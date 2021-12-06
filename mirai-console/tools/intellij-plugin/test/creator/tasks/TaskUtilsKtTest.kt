/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.creator.tasks

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TaskUtilsKtTest {

    private fun useClassNameCases(mustBeTrue: (String) -> Boolean) {
        val success = listOf("A", "A_B", "A0", "A_0", "A_B0")
        val failure = listOf("", "0", "_", "-", ".", "/", "A/", "A.", "A.")

        success.forEach { assertEquals(true, mustBeTrue(it), it) }
        failure.forEach { assertEquals(false, mustBeTrue(it), it) }
    }

    @Test
    fun isValidPackageName() {
        useClassNameCases { it.isValidPackageName() }
    }

    @Test
    fun isValidClassName() {
        useClassNameCases { it.isValidSimpleClassName() }
    }

    @Test
    fun adjustToClassName() {
        assertEquals("Test", "Test".adjustToClassName())
        assertEquals("TeSt", "Te_st".adjustToClassName())
        assertEquals("TeSt", "Te_St".adjustToClassName())
        assertEquals("TeSt", "Te-st".adjustToClassName())
        assertEquals("TeSt", "Te-St".adjustToClassName())

        assertEquals("TestAA", "Test//!@#$%^&*()AA".adjustToClassName())

        assertEquals(null, "0".adjustToClassName())
        assertEquals(null, "_0".adjustToClassName())
        assertEquals(null, "_0A".adjustToClassName())
        assertEquals("A1", "A1".adjustToClassName())

        assertEquals("A1", "A_1".adjustToClassName())
        assertEquals("A1", "A-1".adjustToClassName())

        assertEquals("MiraiConsoleExample", "mirai-console-example".adjustToClassName())
    }

    @Test
    fun qualifiedClassname() {
        useClassNameCases { it.isValidQualifiedClassName() }
        assertTrue { "a.b.c".isValidQualifiedClassName() }
    }
}