/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.test

import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.utils.StructureToStringTransformerNew
import net.mamoe.mirai.utils.StructureToStringTransformer
import net.mamoe.mirai.utils.structureToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class StructureToStringTransformerNewTest : AbstractTest() {

    data class MyClass(
        val value: String
    )

    @Test
    fun `can load service`() {
        assertIs<StructureToStringTransformerNew>(StructureToStringTransformer.instance)
    }

    @Test
    fun `can use`() {
        assertEquals(
            """
            ${MyClass::class.qualifiedName}(
                value = "1",
            )
        """.trimIndent(),
            MyClass("1").structureToString()
        )
    }
}