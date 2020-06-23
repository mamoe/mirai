/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("PRE_RELEASE_CLASS")

package net.mamoe.mirai.console.codegen


object ValueKtCodegen {
    object SettingCodegen {
        object PrimitiveValuesCodegen : RegionCodegen(), DefaultInvoke {
            override val defaultInvokeArgs: List<KtType>
                get() = KtType.Primitives + KtString

            override fun StringBuilder.apply(ktType: KtType) {
                @Suppress("ClassName")
                appendKCode(
                    """
                    /**
                     * Represents a non-null [$ktType] value.
                     */
                    interface ${ktType}Value : PrimitiveValue<$ktType>
                """
                )
            }
        }

    }

    @JvmStatic
    fun main(args: Array<String>) {
        codegen("Value.kt") {
            SettingCodegen.PrimitiveValuesCodegen()
        }
    }
}

