/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.internal.test.AbstractTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RefineContextTest : AbstractTest() {
    @Test
    fun `merge test`() {
        val Key1 = RefineContextKey<Int>("KeyInt")
        val Key2 = RefineContextKey<Double>("KeyDouble")
        val Key3 = RefineContextKey<String>("KeyString")
        val Key4 = RefineContextKey<ByteArray>("KeyBytes")

        val context1 = SimpleRefineContext(
            Key1 to 114514,
            Key2 to 1919.810,
            Key3 to "sodayo"
        )

        val context2 = SimpleRefineContext(
            Key2 to 1919.811,
            Key3 to "yarimasune",
            Key4 to byteArrayOf(11, 45, 14)
        )

        val combinedOverride = context1.merge(context2, override = true)
        val combinedNotOverride = context1.merge(context2, override = false)

        val context3 = SimpleRefineContext(
            Key2 to 1919.811,
            Key3 to "yarimasune"
        )

        assertEquals(context1, context1.merge(context3, false))
        assertTrue(combinedOverride != combinedNotOverride)

        assertEquals(4, combinedOverride.entries().size)
        assertEquals(1919.811, combinedOverride[Key2])
        assertEquals(1919.810, combinedNotOverride[Key2])
        assertEquals("sodayo", combinedNotOverride[Key3])
        assertTrue(byteArrayOf(11, 45, 14).contentEquals(combinedNotOverride[Key4]))
    }
}