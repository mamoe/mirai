/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalSerializationApi::class)

package net.mamoe.mirai.internal.message

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import net.mamoe.mirai.utils.cast
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

internal abstract class AbstractPolymorphicSerializer<T : Any> internal constructor() : KSerializer<T> {

    /**
     * Base class for all classes that this polymorphic serializer can serialize or deserialize.
     */
    abstract val baseClass: KClass<T>

    final override fun serialize(encoder: Encoder, value: T) {
        val actualSerializer = findPolymorphicSerializer(encoder, value)
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, actualSerializer.descriptor.serialName)
            encodeSerializableElement(descriptor, 1, actualSerializer.cast(), value)
        }
    }

    final override fun deserialize(decoder: Decoder): T = decoder.decodeStructure(descriptor) {
        var klassName: String? = null
        var value: Any? = null
        if (decodeSequentially()) {
            return@decodeStructure decodeSequentially(this)
        }

        mainLoop@ while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> {
                    break@mainLoop
                }
                0 -> {
                    klassName = decodeStringElement(descriptor, index)
                }
                1 -> {
                    klassName = requireNotNull(klassName) { "Cannot read polymorphic value before its type token" }
                    val serializer = findPolymorphicSerializer(this, klassName)
                    value = decodeSerializableElement(descriptor, index, serializer)
                }
                else -> throw SerializationException(
                    "Invalid index in polymorphic deserialization of " +
                            (klassName ?: "unknown class") +
                            "\n Expected 0, 1 or DECODE_DONE(-1), but found $index"
                )
            }
        }
        @Suppress("UNCHECKED_CAST")
        requireNotNull(value) { "Polymorphic value has not been read for class $klassName" } as T
    }

    private fun decodeSequentially(compositeDecoder: CompositeDecoder): T {
        val klassName = compositeDecoder.decodeStringElement(descriptor, 0)
        val serializer = findPolymorphicSerializer(compositeDecoder, klassName)
        return compositeDecoder.decodeSerializableElement(descriptor, 1, serializer)
    }

    /**
     * Lookups an actual serializer for given [klassName] withing the current [base class][baseClass].
     * May use context from the [decoder].
     */
    open fun findPolymorphicSerializerOrNull(
        decoder: CompositeDecoder,
        klassName: String?
    ): DeserializationStrategy<T>? = decoder.serializersModule.getPolymorphic(baseClass, klassName)


    /**
     * Lookups an actual serializer for given [value] within the current [base class][baseClass].
     * May use context from the [encoder].
     */
    open fun findPolymorphicSerializerOrNull(
        encoder: Encoder,
        value: T
    ): SerializationStrategy<T>? =
        encoder.serializersModule.getPolymorphic(baseClass, value)
}


internal fun <T : Any> AbstractPolymorphicSerializer<T>.findPolymorphicSerializer(
    decoder: CompositeDecoder,
    klassName: String?
): DeserializationStrategy<T> =
    findPolymorphicSerializerOrNull(decoder, klassName) ?: throwSubtypeNotRegistered(klassName, baseClass)

internal fun <T : Any> AbstractPolymorphicSerializer<T>.findPolymorphicSerializer(
    encoder: Encoder,
    value: T
): SerializationStrategy<T> =
    findPolymorphicSerializerOrNull(encoder, value) ?: throwSubtypeNotRegistered(value::class, baseClass)

@JvmName("throwSubtypeNotRegistered")
internal fun throwSubtypeNotRegistered(subClassName: String?, baseClass: KClass<*>): Nothing {
    val scope = "in the scope of '${baseClass.simpleName}'"
    throw SerializationException(
        if (subClassName == null)
            "Class discriminator was missing and no default polymorphic serializers were registered $scope"
        else
            "Class '$subClassName' is not registered for polymorphic serialization $scope.\n" +
                    "Mark the base class as 'sealed' or register the serializer explicitly."
    )
}

@JvmName("throwSubtypeNotRegistered")
internal fun throwSubtypeNotRegistered(subClass: KClass<*>, baseClass: KClass<*>): Nothing =
    throwSubtypeNotRegistered(subClass.simpleName ?: "$subClass", baseClass)
