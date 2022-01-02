/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.descriptors

import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescTransformer
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescVisitor
import kotlin.reflect.KType
import kotlin.reflect.typeOf

sealed interface CollectionLikeValueDesc : ValueDesc {
    val value: Any

    val arrayType: KType
    val elementType: KType
    val elements: MutableList<ValueDesc>

    override fun <D> acceptChildren(visitor: ValueDescVisitor<D, *>, data: D) {
        for (element in elements) {
            element.accept(visitor, data)
        }
    }

    override fun <D> transformChildren(visitor: ValueDescTransformer<D>, data: D) {
        elements.transform { it.accept(visitor, data) }
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun createOrNull(array: Any, type: KType, parent: ValueDesc?): CollectionLikeValueDesc? {
            if (array is Array<*>) return ObjectArrayValueDesc(parent, array, arrayType = type)
            return when (array) {
                is IntArray -> PrimitiveArrayValueDesc(parent, array, arrayType = type, elementType = typeOf<Int>())
                is ByteArray -> PrimitiveArrayValueDesc(parent, array, arrayType = type, elementType = typeOf<Byte>())
                is ShortArray -> PrimitiveArrayValueDesc(parent, array, arrayType = type, elementType = typeOf<Short>())
                is CharArray -> PrimitiveArrayValueDesc(parent, array, arrayType = type, elementType = typeOf<Char>())
                is LongArray -> PrimitiveArrayValueDesc(parent, array, arrayType = type, elementType = typeOf<Long>())
                is FloatArray -> PrimitiveArrayValueDesc(parent, array, arrayType = type, elementType = typeOf<Float>())
                is DoubleArray -> PrimitiveArrayValueDesc(
                    parent,
                    array,
                    arrayType = type,
                    elementType = typeOf<Double>()
                )
                is BooleanArray -> PrimitiveArrayValueDesc(
                    parent,
                    array,
                    arrayType = type,
                    elementType = typeOf<Boolean>()
                )
                else -> return null
            }
        }
    }
}

fun <E> MutableList<E>.transform(transformer: (E) -> E?) {
    val result = this.asSequence().mapNotNull(transformer).toMutableList()
    this.clear()
    this.addAll(result)
}