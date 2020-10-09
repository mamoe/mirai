/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.descriptor.MessageContentTypeVariant
import net.mamoe.mirai.console.command.descriptor.NoValueArgumentMappingException
import net.mamoe.mirai.console.command.descriptor.TypeVariant
import net.mamoe.mirai.message.data.MessageContent
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf


/**
 * For developing use, to be inlined in the future.
 */
public typealias RawCommandArgument = MessageContent

/**
 * @see CommandValueArgument
 */
@ExperimentalCommandDescriptors
public interface CommandArgument

/**
 * @see InvariantCommandValueArgument
 */
@ExperimentalCommandDescriptors
public interface CommandValueArgument : CommandArgument {
    public val value: RawCommandArgument
    public val typeVariants: List<TypeVariant<*>>
}

/**
 * The [CommandValueArgument] that doesn't vary in type (remaining [MessageContent]).
 */
@ExperimentalCommandDescriptors
public data class InvariantCommandValueArgument(
    public override val value: RawCommandArgument,
) : CommandValueArgument {
    override val typeVariants: List<TypeVariant<*>> = listOf(MessageContentTypeVariant)
}

@ExperimentalCommandDescriptors
public fun <T> CommandValueArgument.mapValue(typeVariant: TypeVariant<T>): T = typeVariant.mapValue(this.value)


@OptIn(ExperimentalStdlibApi::class)
@ExperimentalCommandDescriptors
public inline fun <reified T> CommandValueArgument.mapToType(): T =
    mapToTypeOrNull() ?: throw  NoValueArgumentMappingException(this, typeOf<T>())

@ExperimentalCommandDescriptors
public inline fun <reified T> CommandValueArgument.mapToTypeOrNull(): T? {
    @OptIn(ExperimentalStdlibApi::class)
    val expectingType = typeOf<T>()
    val result = typeVariants
        .filter { it.outType.isSubtypeOf(expectingType) }
        .also {
            if (it.isEmpty()) return null
        }
        .reduce { acc, typeVariant ->
            if (acc.outType.isSubtypeOf(typeVariant.outType))
                acc
            else typeVariant
        }
    return result.mapValue(value) as T
}