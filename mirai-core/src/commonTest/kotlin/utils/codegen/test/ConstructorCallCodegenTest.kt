/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.codegen.test

import net.mamoe.mirai.internal.utils.codegen.ConstructorCallCodegenFacade
import net.mamoe.mirai.internal.utils.codegen.analyzeAndGenerate
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstructorCallCodegenTest {

    @Test
    fun `test plain`() {
        assertEquals(
            "\"test\"",
            ConstructorCallCodegenFacade.analyzeAndGenerate("test")
        )
        assertEquals(
            "1",
            ConstructorCallCodegenFacade.analyzeAndGenerate(1)
        )
        assertEquals(
            "1.0",
            ConstructorCallCodegenFacade.analyzeAndGenerate(1.0)
        )
    }

    @Test
    fun `test array`() {
        assertEquals(
            "arrayOf(1, 2)",
            ConstructorCallCodegenFacade.analyzeAndGenerate(arrayOf(1, 2))
        )
        assertEquals(
            "arrayOf(5.0)",
            ConstructorCallCodegenFacade.analyzeAndGenerate(arrayOf(5.0))
        )
        assertEquals(
            "arrayOf(\"1\")",
            ConstructorCallCodegenFacade.analyzeAndGenerate(arrayOf("1"))
        )
        assertEquals(
            "arrayOf(arrayOf(1))",
            ConstructorCallCodegenFacade.analyzeAndGenerate(arrayOf(arrayOf(1)))
        )
    }

    data class TestClass(
        val value: String
    )

    data class TestClass2(
        val value: Any
    )

    @Test
    fun `test class`() {
        assertEquals(
            """
                ${TestClass::class.qualifiedName!!}(
                value="test",
                )
            """.trimIndent(),
            ConstructorCallCodegenFacade.analyzeAndGenerate(TestClass("test"))
        )
        assertEquals(
            """
                ${TestClass2::class.qualifiedName!!}(
                value="test",
                )
            """.trimIndent(),
            ConstructorCallCodegenFacade.analyzeAndGenerate(TestClass2("test"))
        )
        assertEquals(
            """
                ${TestClass2::class.qualifiedName!!}(
                value=1,
                )
            """.trimIndent(),
            ConstructorCallCodegenFacade.analyzeAndGenerate(TestClass2(1))
        )
    }

    data class TestNesting(
        val nested: Nested
    ) {
        data class Nested(
            val value: String
        )
    }

    @Test
    fun `test nesting`() {
        assertEquals(
            """
                net.mamoe.mirai.internal.utils.codegen.test.ConstructorCallCodegenTest.TestNesting(
                nested=net.mamoe.mirai.internal.utils.codegen.test.ConstructorCallCodegenTest.TestNesting.Nested(
                value="test",
                ),
                )
            """.trimIndent(),
            ConstructorCallCodegenFacade.analyzeAndGenerate(TestNesting(TestNesting.Nested("test")))
        )
    }
}