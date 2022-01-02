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
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescTransformer
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescVisitor
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class MapValueDesc(
    override val parent: ValueDesc?,
    var value: Map<Any?, Any?>,
    override val origin: Map<Any?, Any?> = value,
    val mapType: KType,
    val keyType: KType = mapType.arguments.first().type ?: Any::class.createType(),
    val valueType: KType = mapType.arguments[1].type ?: Any::class.createType(),
) : ValueDesc {
    val elements: MutableMap<ValueDesc, ValueDesc> by lazy {
        value.map {
            ValueDescAnalyzer.analyze(it.key, keyType) to ValueDescAnalyzer.analyze(
                it.value,
                valueType
            )
        }.toMap(mutableMapOf())
    }

    override fun <D, R> accept(visitor: ValueDescVisitor<D, R>, data: D): R = visitor.visitMap(this, data)

    override fun <D> acceptChildren(visitor: ValueDescVisitor<D, *>, data: D) {
        for ((key, value) in elements.entries) {
            key.accept(visitor, data)
            value.accept(visitor, data)
        }
    }

    override fun <D> transformChildren(visitor: ValueDescTransformer<D>, data: D) {
        val resultMap = mutableMapOf<ValueDesc, ValueDesc>()
        for (entry in this.elements.entries) {
            val newKey = entry.key.accept(visitor, data)
            val newValue = entry.value.accept(visitor, data)
            if (newKey != null && newValue != null) {
                resultMap[newKey] = newValue
            }
        }
        this.elements.clear()
        this.elements.putAll(resultMap)
    }
}