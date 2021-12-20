/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.internal.testFramework.codegen.ValueDescAnalyzer
import net.mamoe.mirai.internal.testFramework.codegen.analyze
import net.mamoe.mirai.internal.testFramework.codegen.descriptors.transformChildren
import net.mamoe.mirai.internal.testFramework.codegen.visitors.OptimizeByteArrayAsHexStringTransformer
import net.mamoe.mirai.internal.testFramework.codegen.visitors.ValueDescToStringRenderer
import net.mamoe.mirai.internal.testFramework.codegen.visitors.renderToString

internal class StructureToStringTransformerNew : StructureToStringTransformer {
    private val legacy = StructureToStringTransformerLegacy()

    override fun transform(any: Any?): String =
        kotlin.runCatching {
            val desc = ValueDescAnalyzer.analyze(any)
            desc.transformChildren(OptimizeByteArrayAsHexStringTransformer())
            desc.renderToString(
                ValueDescToStringRenderer()
            )
        }.getOrElse { legacy.transform(any) }
}