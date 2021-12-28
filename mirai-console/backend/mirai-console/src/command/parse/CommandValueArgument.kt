/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.internal.data.castOrInternalError
import net.mamoe.mirai.console.internal.data.classifierAsKClass
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.SingleMessage
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf


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

    /**
     * [MessageContent] if single argument
     * [MessageChain] is vararg
     */
    public val value: Message

    /**
     * Intrinsic variants of this argument.
     *
     * @see TypeVariant
     */
    public val typeVariants: List<TypeVariant<*>>
}

/**
 * The [CommandValueArgument] that doesn't vary in type (remaining [MessageContent]).
 */
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public data class DefaultCommandValueArgument(
    public override val value: Message,
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
    if (expectingType.isSubtypeOf(ARRAY_OUT_ANY_TYPE)) {
        val arrayElementType = expectingType.arguments.single().type ?: ANY_TYPE

        val result = ArrayList<Any?>()

        when (val value = value) {
            is MessageChain -> {
                for (message in value) {
                    result.add(mapToTypeOrNullImpl(arrayElementType, message))
                }
            }
            else -> { // single
                value.castOrInternalError<SingleMessage>()
                result.add(mapToTypeOrNullImpl(arrayElementType, value))
            }
        }


        @Suppress("UNCHECKED_CAST")
        return result.toArray(arrayElementType.createArray(result.size)) as T
    }

    @Suppress("UNCHECKED_CAST")
    return mapToTypeOrNullImpl(expectingType, value) as T
}

private fun KType.createArray(size: Int): Array<Any?> {
    return java.lang.reflect.Array.newInstance(this.classifierAsKClass().javaObjectType, size).castOrInternalError()
}

@OptIn(ExperimentalCommandDescriptors::class)
private fun CommandValueArgument.mapToTypeOrNullImpl(expectingType: KType, value: Message): Any? {
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
    return result.mapValue(value)
}

@ExperimentalCommandDescriptors
public inline fun <reified T> CommandValueArgument.mapToTypeOrNull(): T? {
    @OptIn(ExperimentalStdlibApi::class)
    return mapToTypeOrNull(typeOf<T>())
}