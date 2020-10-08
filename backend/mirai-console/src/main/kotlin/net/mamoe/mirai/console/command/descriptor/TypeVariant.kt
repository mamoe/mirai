/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.descriptor

import kotlin.reflect.KType
import kotlin.reflect.typeOf

@ExperimentalCommandDescriptors
public interface TypeVariant<out OutType> {
    public val outType: KType

    public fun mapValue(valueParameter: String): OutType

    public companion object {
        @OptIn(ExperimentalStdlibApi::class)
        @JvmSynthetic
        public inline operator fun <reified OutType> invoke(crossinline block: (valueParameter: String) -> OutType): TypeVariant<OutType> {
            return object : TypeVariant<OutType> {
                override val outType: KType = typeOf<OutType>()
                override fun mapValue(valueParameter: String): OutType = block(valueParameter)
            }
        }
    }
}

@ExperimentalCommandDescriptors
public object StringTypeVariant : TypeVariant<String> {
    @OptIn(ExperimentalStdlibApi::class)
    override val outType: KType = typeOf<String>()
    override fun mapValue(valueParameter: String): String = valueParameter
}
