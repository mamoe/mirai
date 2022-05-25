/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.test

import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.testFramework.codegen.ValueDescAnalyzer
import net.mamoe.mirai.internal.testFramework.codegen.analyze
import net.mamoe.mirai.internal.testFramework.codegen.descriptors.transform
import net.mamoe.mirai.internal.testFramework.codegen.visitors.OptimizeByteArrayAsHexStringTransformer
import net.mamoe.mirai.internal.testFramework.codegen.visitors.ValueDescToStringRenderer
import net.mamoe.mirai.internal.testFramework.codegen.visitors.renderToString
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OptimizeByteArrayAsHexStringTransformerTest : AbstractTest() {

    private inline fun <reified T> analyzeTransformAndRender(
        value: T,
        renderer: ValueDescToStringRenderer = ValueDescToStringRenderer()
    ): String {
        return ValueDescAnalyzer.analyze(value)
            .transform(OptimizeByteArrayAsHexStringTransformer())
            .renderToString(renderer)
    }

    @Test
    fun `can optimize as string`() {
        assertEquals(
            """
            "test".toByteArray() /* 74 65 73 74 */
        """.trimIndent(), analyzeTransformAndRender("test".toByteArray())
        )
    }

    @Test
    fun `can optimize as hex`() {
        assertEquals(
            """
            "4F 02".hexToBytes()
        """.trimIndent(), analyzeTransformAndRender(byteArrayOf(0x4f, 0x02))
        )
    }
}