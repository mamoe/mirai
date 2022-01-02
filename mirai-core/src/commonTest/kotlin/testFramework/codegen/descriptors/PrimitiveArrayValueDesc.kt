/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.descriptors

import net.mamoe.mirai.internal.testFramework.codegen.ValueDescAnalyzer
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescVisitor
import kotlin.reflect.KType

class PrimitiveArrayValueDesc(
    override val parent: ValueDesc?,
    value: Any,
    override val origin: Any = value,
    override val arrayType: KType,
    override val elementType: KType
) : CollectionLikeValueDesc {
    override var value: Any = value
        set(value) {
            field = value
            elements.clear()
            elements.addAll(initializeElements(value))
        }

    override val elements: MutableList<ValueDesc> by lazy {
        initializeElements(value)
    }

    private fun initializeElements(value: Any): MutableList<ValueDesc> = when (value) {
        is IntArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        is ByteArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        is ShortArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        is CharArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        is LongArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        is FloatArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        is DoubleArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        is BooleanArray -> value.mapTo(mutableListOf()) { ValueDescAnalyzer.analyze(it, elementType) }
        else -> error("$value is not an array.")
    }

    override fun <D, R> accept(visitor: ValueDescVisitor<D, R>, data: D): R {
        return visitor.visitPrimitiveArray(this, data)
    }
}