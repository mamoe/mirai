/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.codegen

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

sealed interface ValueDesc {
    val origin: Any?

    fun accept(visitor: ValueDescVisitor)
}

sealed interface ArrayValueDesc : ValueDesc {
    val value: Any

    val arrayType: KType
    val elementType: KType
    val elements: MutableList<ValueDesc>

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun createOrNull(array: Any, type: KType): ArrayValueDesc? {
            if (array is Array<*>) return ObjectArrayValueDesc(array, arrayType = type)
            return when (array) {
                is IntArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Int>())
                is ByteArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Byte>())
                is ShortArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Short>())
                is CharArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Char>())
                is LongArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Long>())
                is FloatArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Float>())
                is DoubleArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Double>())
                is BooleanArray -> PrimitiveArrayValueDesc(array, arrayType = type, elementType = typeOf<Boolean>())
                else -> return null
            }
        }
    }
}

class ObjectArrayValueDesc(
    override var value: Array<*>,
    override val origin: Array<*> = value,
    override val arrayType: KType,
    override val elementType: KType = arrayType.arguments.first().type ?: Any::class.createType()
) : ArrayValueDesc {
    override val elements: MutableList<ValueDesc> by lazy {
        value.mapTo(mutableListOf()) {
            ConstructorCallCodegenFacade.analyze(it, elementType)
        }
    }

    override fun accept(visitor: ValueDescVisitor) {
        visitor.visitObjectArray(this)
    }
}

class CollectionValueDesc(
    override var value: Collection<*>,
    override val origin: Collection<*> = value,
    override val arrayType: KType,
    override val elementType: KType = arrayType.arguments.first().type ?: Any::class.createType()
) : ArrayValueDesc {
    override val elements: MutableList<ValueDesc> by lazy {
        value.mapTo(mutableListOf()) {
            ConstructorCallCodegenFacade.analyze(it, elementType)
        }
    }

    override fun accept(visitor: ValueDescVisitor) {
        visitor.visitCollection(this)
    }
}

class MapValueDesc(
    var value: Map<Any?, Any?>,
    override val origin: Map<Any?, Any?> = value,
    val mapType: KType,
    val keyType: KType = mapType.arguments.first().type ?: Any::class.createType(),
    val valueType: KType = mapType.arguments[1].type ?: Any::class.createType(),
) : ValueDesc {
    val elements: MutableMap<ValueDesc, ValueDesc> by lazy {
        value.map {
            ConstructorCallCodegenFacade.analyze(it.key, keyType) to ConstructorCallCodegenFacade.analyze(
                it.value,
                valueType
            )
        }.toMap(mutableMapOf())
    }

    override fun accept(visitor: ValueDescVisitor) {
        visitor.visitMap(this)
    }
}

class PrimitiveArrayValueDesc(
    override var value: Any,
    override val origin: Any = value,
    override val arrayType: KType,
    override val elementType: KType
) : ArrayValueDesc {
    override val elements: MutableList<ValueDesc> by lazy {
        when (val value = value) {
            is IntArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            is ByteArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            is ShortArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            is CharArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            is LongArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            is FloatArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            is DoubleArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            is BooleanArray -> value.mapTo(mutableListOf()) { ConstructorCallCodegenFacade.analyze(it, elementType) }
            else -> error("$value is not an array.")
        }
    }

    override fun accept(visitor: ValueDescVisitor) {
        visitor.visitPrimitiveArray(this)
    }
}

class PlainValueDesc(
    var value: String,
    override val origin: Any?
) : ValueDesc {
    init {
        require(value.isNotBlank())
    }

    override fun accept(visitor: ValueDescVisitor) {
        visitor.visitPlain(this)
    }
}

class ClassValueDesc<T : Any>(
    override val origin: T,
    val properties: MutableMap<KParameter, ValueDesc>,
) : ValueDesc {
    val type: KClass<out T> by lazy { origin::class }

    override fun accept(visitor: ValueDescVisitor) {
        visitor.visitClass(this)
    }
}