/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalStdlibApi::class)

package net.mamoe.mirai.internal.testFramework.codegen.test.visitors

import net.mamoe.mirai.internal.testFramework.codegen.ValueDescAnalyzer
import net.mamoe.mirai.internal.testFramework.codegen.analyze
import net.mamoe.mirai.internal.testFramework.codegen.visitors.ValueDescToStringRenderer
import net.mamoe.mirai.internal.testFramework.codegen.visitors.renderToString
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ValueDescToStringRendererTest {
    private val renderer = ValueDescToStringRenderer()

    @Test
    fun `plain value`() {
        assertEquals("\"str\"", ValueDescAnalyzer.analyze("str").renderToString(renderer))
        assertEquals("1", ValueDescAnalyzer.analyze(1).renderToString(renderer))
    }

    @Test
    fun `object array value`() {
        assertEquals(
            "arrayOf(\"str\", \"obj\")",
            ValueDescAnalyzer.analyze(arrayOf("str", "obj")).renderToString(renderer)
        )
        assertEquals("arrayOf(1, 2)", ValueDescAnalyzer.analyze(arrayOf(1, 2)).renderToString(renderer))
    }

    @Test
    fun `object array value nested`() {
        assertEquals(
            """
                arrayOf(
                    arrayOf("str", "obj"), 
                    arrayOf("str", "obj"), 
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(arrayOf(arrayOf("str", "obj"), arrayOf("str", "obj"))).renderToString(renderer)
        )
        assertEquals(
            """
                arrayOf(
                    arrayOf(1, 2), 
                    arrayOf(1, 2), 
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(arrayOf(arrayOf(1, 2), arrayOf(1, 2))).renderToString(renderer)
        )
    }

    @Test
    fun `primitive array value`() {
        assertEquals("intArrayOf(1, 2)", ValueDescAnalyzer.analyze(intArrayOf(1, 2)).renderToString(renderer))
    }

    @Test
    fun `collection value`() {
        assertEquals("mutableListOf(1, 2)", ValueDescAnalyzer.analyze(listOf(1, 2)).renderToString(renderer))
        assertEquals("mutableSetOf(1, 2)", ValueDescAnalyzer.analyze(setOf(1, 2)).renderToString(renderer))
    }

    @Test
    fun `collection value nested`() {
        assertEquals(
            """
                mutableListOf(
                    mutableListOf(1, 2), 
                    mutableListOf(1, 2), 
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(listOf(listOf(1, 2), listOf(1, 2))).renderToString(renderer)
        )
        assertEquals(
            """
                mutableSetOf(
                    mutableListOf(1, 2), 
                    mutableListOf(2, 2), 
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(setOf(listOf(1, 2), listOf(2, 2))).renderToString(renderer)
        )
    }

    @Test
    fun `map value`() {
        assertEquals(
            """
            mutableMapOf(
                1 to 2,
                3 to 2,
            )
        """.trimIndent(), ValueDescAnalyzer.analyze(mapOf(1 to 2, 3 to 2)).renderToString(renderer)
        )
    }

    @Test
    fun `map value nested`() {
        assertEquals(
            """
            |mutableMapOf(
            |    1 to 2,
            |    5 to mutableMapOf(
            |        1 to 2,
            |        3 to 2,
            |    ),
            |)
        """.trimMargin(),
            ValueDescAnalyzer.analyze(mapOf(1 to 2, 5 to mapOf(1 to 2, 3 to 2)))
                .renderToString(renderer)
        )
    }

    data class MyClass(
        val str: String,
        val int: Int,
        val self: MyClass?,
    )

    @Suppress("ClassName")
    data class `MyClass$3`(
        val str: String,
        val int: Int,
        val self: `MyClass$3`?,
    )

    @Test
    fun `class value`() {
        assertEquals(
            """
                ${MyClass::class.qualifiedName}(
                    str = "str",
                    int = 1,
                    self = null,
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(MyClass("str", 1, null)).renderToString(renderer)
        )
    }

    @Test
    fun `local class`() {
        data class MyClass2(
            val str: String,
            val int: Int,
            val self: MyClass2?,
        )

        assertEquals(
            """
                ${MyClass2::class.simpleName}(
                    str = "str",
                    int = 1,
                    self = null,
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(MyClass2("str", 1, null)).renderToString(renderer)
        )
    }

    @Test
    fun `class with special name`() {
        assertEquals(
            """
                `${`MyClass$3`::class.qualifiedName}`(
                    str = "str",
                    int = 1,
                    self = null,
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(`MyClass$3`("str", 1, null)).renderToString(renderer)
        )
    }


    @Test
    fun `class value nested`() {
        data class MyClass2(
            val str: String,
            val int: Int,
            val self: MyClass2?,
        )

        assertEquals(
            """
                ${MyClass::class.qualifiedName}(
                    str = "str",
                    int = 1,
                    self = ${MyClass::class.qualifiedName}(
                        str = "str",
                        int = 1,
                        self = null,
                    ),
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(MyClass("str", 1, MyClass("str", 1, null))).renderToString(renderer)
        )

        assertEquals(
            """
                ${MyClass2::class.simpleName}(
                    str = "str",
                    int = 1,
                    self = ${MyClass2::class.simpleName}(
                        str = "str",
                        int = 1,
                        self = null,
                    ),
                )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(MyClass2("str", 1, MyClass2("str", 1, null))).renderToString(renderer)
        )
    }
}