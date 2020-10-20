/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.MessageContent
import kotlin.reflect.KType
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
 * @see DefaultCommandValueArgument
 */
@ExperimentalCommandDescriptors
public interface CommandValueArgument : CommandArgument {
    public val type: KType
    public val value: RawCommandArgument
    public val typeVariants: List<TypeVariant<*>>
}

/**
 * The [CommandValueArgument] that doesn't vary in type (remaining [MessageContent]).
 */
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public data class DefaultCommandValueArgument(
    public override val value: RawCommandArgument,
) : CommandValueArgument {
    @OptIn(ExperimentalStdlibApi::class)
    override val type: KType = typeOf<MessageContent>()
    override val typeVariants: List<TypeVariant<*>> = listOf(
        MessageContentTypeVariant,
        MessageChainTypeVariant,
        ContentStringTypeVariant,
    )
}

@ExperimentalCommandDescriptors
public fun <T> CommandValueArgument.mapValue(typeVariant: TypeVariant<T>): T = typeVariant.mapValue(this.value)


@OptIn(ExperimentalStdlibApi::class)
@ExperimentalCommandDescriptors
public inline fun <reified T> CommandValueArgument.mapToType(): T =
    mapToTypeOrNull() ?: throw  NoValueArgumentMappingException(this, typeOf<T>())

@OptIn(ExperimentalStdlibApi::class)
@ExperimentalCommandDescriptors
public fun <T> CommandValueArgument.mapToType(type: KType): T =
    mapToTypeOrNull(type) ?: throw  NoValueArgumentMappingException(this, type)

@ExperimentalCommandDescriptors
public fun <T> CommandValueArgument.mapToTypeOrNull(expectingType: KType): T? {
    @OptIn(ExperimentalStdlibApi::class)
    val result = typeVariants
        .filter { it.outType.isSubtypeOf(expectingType) }
        .ifEmpty {
            return null
        }
        .reduce { acc, typeVariant ->
            if (acc.outType.isSubtypeOf(typeVariant.outType))
                acc
            else typeVariant
        }
    @Suppress("UNCHECKED_CAST")
    return result.mapValue(value) as T
}

@ExperimentalCommandDescriptors
public inline fun <reified T> CommandValueArgument.mapToTypeOrNull(): T? {
    @OptIn(ExperimentalStdlibApi::class)
    return mapToTypeOrNull(typeOf<T>())
}