/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandCallParser
import net.mamoe.mirai.console.command.parse.RawCommandArgument
import net.mamoe.mirai.message.data.MessageContent
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Implicit type variant specified by [CommandCallParser].
 *
 * [TypeVariant] is not necessary for all [CommandCall]s.
 */
@ExperimentalCommandDescriptors
public interface TypeVariant<out OutType> {
    /**
     * The reified type of [OutType]
     */
    public val outType: KType

    public fun mapValue(valueParameter: MessageContent): OutType

    public companion object {
        @OptIn(ExperimentalStdlibApi::class)
        @JvmSynthetic
        public inline operator fun <reified OutType> invoke(crossinline block: (valueParameter: RawCommandArgument) -> OutType): TypeVariant<OutType> {
            return object : TypeVariant<OutType> {
                override val outType: KType = typeOf<OutType>()
                override fun mapValue(valueParameter: MessageContent): OutType = block(valueParameter)
            }
        }
    }
}

@ExperimentalCommandDescriptors
public object MessageContentTypeVariant : TypeVariant<MessageContent> {
    @OptIn(ExperimentalStdlibApi::class)
    override val outType: KType = typeOf<String>()
    override fun mapValue(valueParameter: MessageContent): MessageContent = valueParameter
}
