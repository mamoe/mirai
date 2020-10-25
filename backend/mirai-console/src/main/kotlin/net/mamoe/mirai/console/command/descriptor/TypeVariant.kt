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
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.internal.data.castOrNull
import net.mamoe.mirai.console.internal.data.kClassQualifiedName
import net.mamoe.mirai.message.data.*
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

    /**
     * @see CommandValueArgument.value
     */
    public fun mapValue(valueParameter: Message): OutType

    public companion object {
        @OptIn(ExperimentalStdlibApi::class)
        @JvmSynthetic
        public inline operator fun <reified OutType> invoke(crossinline block: (valueParameter: Message) -> OutType): TypeVariant<OutType> {
            return object : TypeVariant<OutType> {
                override val outType: KType = typeOf<OutType>()
                override fun mapValue(valueParameter: Message): OutType = block(valueParameter)
            }
        }
    }
}

@ExperimentalCommandDescriptors
public object MessageContentTypeVariant : TypeVariant<MessageContent> {
    @OptIn(ExperimentalStdlibApi::class)
    override val outType: KType = typeOf<MessageContent>()
    override fun mapValue(valueParameter: Message): MessageContent =
        valueParameter.castOrNull<MessageContent>() ?: error("Accepts MessageContent only but given ${valueParameter.kClassQualifiedName}")
}

@ExperimentalCommandDescriptors
public object MessageChainTypeVariant : TypeVariant<MessageChain> {
    @OptIn(ExperimentalStdlibApi::class)
    override val outType: KType = typeOf<MessageChain>()
    override fun mapValue(valueParameter: Message): MessageChain = valueParameter.asMessageChain()
}

@ExperimentalCommandDescriptors
public object ContentStringTypeVariant : TypeVariant<String> {
    @OptIn(ExperimentalStdlibApi::class)
    override val outType: KType = typeOf<String>()
    override fun mapValue(valueParameter: Message): String = valueParameter.content
}
