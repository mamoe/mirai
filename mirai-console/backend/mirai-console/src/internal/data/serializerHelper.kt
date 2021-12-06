/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.internal.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import net.mamoe.yamlkt.YamlDynamicSerializer
import net.mamoe.yamlkt.YamlNullableDynamicSerializer
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass
import kotlin.reflect.KType


/**
 * Copied from kotlinx.serialization, modifications are marked with "/* mamoe modify */"
 * Copyright 2017-2020 JetBrains s.r.o.
 */
@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
@Suppress(
    "UNCHECKED_CAST",
    "NO_REFLECTION_IN_CLASS_PATH",
    "UNSUPPORTED",
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "IMPLICIT_CAST_TO_ANY"
)
internal fun serializerMirai(type: KType): KSerializer<Any?> {
    fun serializerByKTypeImpl(type: KType): KSerializer<Any> {
        val rootClass = type.classifierAsKClass()

        val typeArguments = type.arguments
            .map { requireNotNull(it.type) { "Star projections in type arguments are not allowed, but had $type" } }
        return when {
            typeArguments.isEmpty() -> rootClass.serializer()
            else -> {
                val serializers = typeArguments
                    .map(::serializerMirai)
                // Array is not supported, see KT-32839
                when (rootClass) {
                    List::class, MutableList::class, ArrayList::class -> ListSerializer(serializers[0])
                    HashSet::class -> SetSerializer(serializers[0])
                    Set::class, MutableSet::class, LinkedHashSet::class -> SetSerializer(serializers[0])
                    HashMap::class -> MapSerializer(serializers[0], serializers[1])
                    ConcurrentHashMap::class, ConcurrentMap::class -> MapSerializer(serializers[0], serializers[1]).map(
                        serializer = { HashMap(it as Map<*, *>) },
                        deserializer = { ConcurrentHashMap(it) }
                    )
                    Map::class, MutableMap::class, LinkedHashMap::class -> MapSerializer(serializers[0], serializers[1])
                    Map.Entry::class -> MapEntrySerializer(serializers[0], serializers[1])
                    Pair::class -> PairSerializer(serializers[0], serializers[1])
                    Triple::class -> TripleSerializer(serializers[0], serializers[1], serializers[2])
                    /* mamoe modify */ Any::class -> if (type.isMarkedNullable) YamlNullableDynamicSerializer else YamlDynamicSerializer
                    else -> {
                        if (rootClass.java.isArray) {
                            return ArraySerializer(
                                typeArguments[0].classifier as KClass<Any>,
                                serializers[0]
                            ).cast()
                        }
                        requireNotNull(rootClass.constructSerializerForGivenTypeArgs(*serializers.toTypedArray())) {
                            "Can't find a method to construct serializer for type ${rootClass.simpleName}. " +
                                    "Make sure this class is marked as @Serializable or provide serializer explicitly."
                        }
                    }
                }
            }
        }.cast()
    }

    val result = serializerByKTypeImpl(type)
    return if (type.isMarkedNullable) result.nullable else result.cast()
}


/**
 * Copied from kotlinx.serialization, modifications are marked with "/* mamoe modify */"
 * Copyright 2017-2020 JetBrains s.r.o.
 */
@Suppress(
    "UNCHECKED_CAST",
    "NO_REFLECTION_IN_CLASS_PATH",
    "UNSUPPORTED",
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "IMPLICIT_CAST_TO_ANY"
)
private fun <T : Any> KClass<T>.constructSerializerForGivenTypeArgs(vararg args: KSerializer<Any?>): KSerializer<T>? {
    val jClass = this.java
    // Search for serializer defined on companion object.
    val companion =
        jClass.declaredFields.singleOrNull { it.name == "Companion" }?.apply { isAccessible = true }?.get(null)
    if (companion != null) {
        val serializer = companion.javaClass.methods
            .find { method ->
                method.name == "serializer" && method.parameterTypes.size == args.size && method.parameterTypes.all { it == KSerializer::class.java }
            }
            ?.invoke(companion, *args) as? KSerializer<T>
        if (serializer != null) return serializer
    }
    // Check whether it's serializable object
    findObjectSerializer(jClass)?.let { return it }
    // Search for default serializer if no serializer is defined in companion object.
    return try {
        jClass.declaredClasses.singleOrNull { it.simpleName == ("\$serializer") }
            ?.getField("INSTANCE")?.get(null) as? KSerializer<T>
    } catch (e: NoSuchFieldException) {
        null
    }
}

private fun <T : Any> findObjectSerializer(jClass: Class<T>): KSerializer<T>? {
    // Check it is an object without using kotlin-reflect
    val field =
        jClass.declaredFields.singleOrNull { it.name == "INSTANCE" && it.type == jClass && Modifier.isStatic(it.modifiers) }
            ?: return null
    // Retrieve its instance and call serializer()
    val instance = field.get(null)
    val method =
        jClass.methods.singleOrNull { it.name == "serializer" && it.parameters.isEmpty() && it.returnType == KSerializer::class.java }
            ?: return null
    val result = method.invoke(instance)
    @Suppress("UNCHECKED_CAST")
    return result as? KSerializer<T>
}

internal inline fun <E> KSerializer<E>.bind(
    crossinline setter: (E) -> Unit,
    crossinline getter: () -> E
): KSerializer<E> {
    return object : KSerializer<E> {
        override val descriptor: SerialDescriptor get() = this@bind.descriptor
        override fun deserialize(decoder: Decoder): E = this@bind.deserialize(decoder).also { setter(it) }

        @Suppress("UNCHECKED_CAST")
        override fun serialize(encoder: Encoder, value: E) =
            this@bind.serialize(encoder, getter())
    }
}

internal inline fun <E, R> KSerializer<E>.map(
    crossinline serializer: (R) -> E,
    crossinline deserializer: (E) -> R
): KSerializer<R> {
    return object : KSerializer<R> {
        override val descriptor: SerialDescriptor get() = this@map.descriptor
        override fun deserialize(decoder: Decoder): R = this@map.deserialize(decoder).let(deserializer)
        override fun serialize(encoder: Encoder, value: R) = this@map.serialize(encoder, value.let(serializer))
    }
}
