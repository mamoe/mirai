/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.setting.internal

import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import net.mamoe.mirai.console.setting.SerializerAwareValue
import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.setting.serializableValueWith
import net.mamoe.mirai.console.setting.valueFromKType
import net.mamoe.yamlkt.YamlDynamicSerializer
import net.mamoe.yamlkt.YamlNullableDynamicSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance as createInstanceKotlin

private val primitiveCollectionsImplemented by lazy {
    false
}

@PublishedApi
@OptIn(ExperimentalStdlibApi::class)
internal inline fun <reified T> typeOf0(): KType = kotlin.reflect.typeOf<T>()

@PublishedApi
@Suppress("UnsafeCall", "SMARTCAST_IMPOSSIBLE", "UNCHECKED_CAST")
internal fun Setting.valueFromKTypeImpl(type: KType): SerializerAwareValue<*> {
    val classifier = type.classifier
    require(classifier is KClass<*>)

    if (classifier.isPrimitiveOrBuiltInSerializableValue()) {
        return valueImplPrimitive(classifier) as SerializerAwareValue<*>
    }

    // TODO: 2020/6/24 优化性能: 预先根据类型生成 V -> Value<V> 的 mapper

    when (classifier) {
        MutableMap::class,
        Map::class,
        LinkedHashMap::class,
        HashMap::class
        -> {
            val keyClass = type.arguments[0].type?.classifier
            require(keyClass is KClass<*>)

            val valueClass = type.arguments[1].type?.classifier
            require(valueClass is KClass<*>)

            if (primitiveCollectionsImplemented && keyClass.isPrimitiveOrBuiltInSerializableValue() && valueClass.isPrimitiveOrBuiltInSerializableValue()) {
                // PrimitiveIntIntMap
                // ...
                TODO()
            } else {
                return createCompositeMapValueImpl<Any?, Any?>(
                    kToValue = { k -> valueFromKType(type.arguments[0].type!!, k) },
                    vToValue = { v -> valueFromKType(type.arguments[1].type!!, v) }
                ).serializableValueWith(serializerMirai(type) as KSerializer<Map<Any?, Any?>>) // erased
            }
        }
        MutableList::class,
        List::class,
        ArrayList::class
        -> {
            val elementClass = type.arguments[0].type?.classifier
            require(elementClass is KClass<*>)

            if (primitiveCollectionsImplemented && elementClass.isPrimitiveOrBuiltInSerializableValue()) {
                // PrimitiveIntList
                // ...
                TODO()
            } else {
                return createCompositeListValueImpl<Any?> { v -> valueFromKType(type.arguments[0].type!!, v) }
                    .serializableValueWith(serializerMirai(type) as KSerializer<List<Any?>>)
            }
        }
        MutableSet::class,
        Set::class,
        LinkedHashSet::class,
        HashSet::class
        -> {
            val elementClass = type.arguments[0].type?.classifier
            require(elementClass is KClass<*>)

            if (primitiveCollectionsImplemented && elementClass.isPrimitiveOrBuiltInSerializableValue()) {
                // PrimitiveIntSet
                // ...
                TODO()
            } else {
                return createCompositeSetValueImpl<Any?> { v -> valueFromKType(type.arguments[0].type!!, v) }
                    .serializableValueWith(serializerMirai(type) as KSerializer<Set<Any?>>)
            }
        }
        else -> error("Custom composite value is not supported yet (${classifier.qualifiedName})")
    }
}

@PublishedApi
internal fun KClass<*>.createInstance(): Any? {
    return when (this) {
        MutableMap::class,
        Map::class,
        LinkedHashMap::class,
        HashMap::class
        -> mutableMapOf<Any?, Any?>()

        MutableList::class,
        List::class,
        ArrayList::class
        -> mutableListOf<Any?>()

        MutableSet::class,
        Set::class,
        LinkedHashSet::class,
        HashSet::class
        -> mutableSetOf<Any?>()

        else -> createInstanceKotlin()
    }
}

internal fun KClass<*>.isPrimitiveOrBuiltInSerializableValue(): Boolean {
    when (this) {
        Byte::class, Short::class, Int::class, Long::class,
        Boolean::class,
        Char::class, String::class,
        Pair::class, Triple::class // TODO: 2020/6/24 支持 PairValue, TripleValue
        -> return true
    }

    return false
}

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal inline fun <R> Any.cast(): R = this as R

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
@OptIn(ImplicitReflectionSerializer::class)
internal fun serializerMirai(type: KType): KSerializer<Any?> {
    fun serializerByKTypeImpl(type: KType): KSerializer<Any> {
        val rootClass = when (val t = type.classifier) {
            is KClass<*> -> t
            else -> error("Only KClass supported as classifier, got $t")
        } as KClass<Any>

        val typeArguments = type.arguments
            .map { requireNotNull(it.type) { "Star projections are not allowed, had $it instead" } }
        return when {
            typeArguments.isEmpty() -> rootClass.serializer()
            else -> {
                val serializers = typeArguments
                    .map(::serializer)
                // Array is not supported, see KT-32839
                when (rootClass) {
                    List::class, MutableList::class, ArrayList::class -> ListSerializer(serializers[0])
                    HashSet::class -> SetSerializer(serializers[0])
                    Set::class, MutableSet::class, LinkedHashSet::class -> SetSerializer(serializers[0])
                    HashMap::class -> MapSerializer(serializers[0], serializers[1])
                    Map::class, MutableMap::class, LinkedHashMap::class -> MapSerializer(serializers[0], serializers[1])
                    Map.Entry::class -> MapEntrySerializer(serializers[0], serializers[1])
                    Pair::class -> PairSerializer(serializers[0], serializers[1])
                    Triple::class -> TripleSerializer(serializers[0], serializers[1], serializers[2])
                    /* mamoe modify */ Any::class -> if (type.isMarkedNullable) YamlNullableDynamicSerializer else YamlDynamicSerializer
                    else -> {
                        if (isReferenceArray(type, rootClass)) {
                            @Suppress("RemoveExplicitTypeArguments")
                            return ArraySerializer<Any, Any?>(
                                typeArguments[0].classifier as KClass<Any>,
                                serializers[0]
                            ).cast()
                        }
                        requireNotNull(rootClass.constructSerializerForGivenTypeArgs(*serializers.toTypedArray())) {
                            "Can't find a method to construct serializer for type ${rootClass.simpleName()}. " +
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
