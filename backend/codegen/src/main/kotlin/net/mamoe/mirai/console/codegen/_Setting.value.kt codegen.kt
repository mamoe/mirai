/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("ClassName")

package net.mamoe.mirai.console.codegen

object _Setting_value_ktCodegen {
    object BuiltInSerializerConstantsPrimitivesCodegen : RegionCodegen("BuiltInSerializerConstants primitives"),
        DefaultInvoke {
        override val defaultInvokeArgs: List<KtType> = KtPrimitives + KtString

        override fun StringBuilder.apply(ktType: KtType) {
            appendLine(
                kCode(
                    """
                @JvmStatic
                val ${ktType.standardName}SerializerDescriptor = ${ktType.standardName}.serializer().descriptor 
            """
                ).lines().joinToString("\n") { "    $it" }
            )
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        codegen("_Setting.value.kt") {
            BuiltInSerializerConstantsPrimitivesCodegen()
        }
    }
}