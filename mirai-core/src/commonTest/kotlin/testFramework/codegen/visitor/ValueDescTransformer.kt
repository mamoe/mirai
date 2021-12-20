/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalStdlibApi::class)

package net.mamoe.mirai.internal.testFramework.codegen.visitor

import net.mamoe.mirai.internal.testFramework.codegen.descriptors.*
import kotlin.reflect.KParameter

abstract class ValueDescTransformer<D> : ValueDescVisitor<D, ValueDesc?> {
    override fun visitValue(desc: ValueDesc, data: D): ValueDesc? {
        return desc
    }

    override fun visitObjectArray(desc: ObjectArrayValueDesc, data: D): ValueDesc? {
        val newElements = desc.elements.mapNotNull { element ->
            element.acceptChildren(this, data)
            element.accept(this, data)
        }
        return ObjectArrayValueDesc(desc.parent, desc.value, desc.origin, desc.arrayType, desc.elementType).apply {
            elements.clear()
            elements.addAll(newElements)
        }
    }

    override fun visitPrimitiveArray(desc: PrimitiveArrayValueDesc, data: D): ValueDesc? {
        val newElements = desc.elements.mapNotNull { element ->
            element.acceptChildren(this, data)
            element.accept(this, data)
        }
        return PrimitiveArrayValueDesc(desc.parent, desc.value, desc.origin, desc.arrayType, desc.elementType).apply {
            elements.clear()
            elements.addAll(newElements)
        }
    }

    override fun visitCollection(desc: CollectionValueDesc, data: D): ValueDesc? {
        val newElements = desc.elements.mapNotNull { element ->
            element.acceptChildren(this, data)
            element.accept(this, data)
        }
        return CollectionValueDesc(desc.parent, desc.value, desc.origin, desc.arrayType, desc.elementType).apply {
            elements.clear()
            elements.addAll(newElements)
        }
    }

    override fun <T : Any> visitClass(desc: ClassValueDesc<T>, data: D): ValueDesc? {
        val resultMap = mutableMapOf<KParameter, ValueDesc>()
        for ((param, value) in desc.properties) {
            value.accept(this, data)?.let { resultMap.put(param, it) }
        }
        return ClassValueDesc(desc.parent, desc.origin, resultMap)
    }
}

