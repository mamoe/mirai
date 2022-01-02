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
import kotlin.reflect.full.createType

class ObjectArrayValueDesc(
    override val parent: ValueDesc?,
    value: Array<*>,
    override val origin: Array<*> = value,
    override val arrayType: KType,
    override val elementType: KType = arrayType.arguments.first().type ?: Any::class.createType(),
) : CollectionLikeValueDesc {

    override var value: Array<*> = value
        set(value) {
            field = value
            elements.clear()
            elements.addAll(initializeElements(value))
        }

    override val elements: MutableList<ValueDesc> by lazy {
        initializeElements(value)
    }

    private fun initializeElements(value: Array<*>) = value.mapTo(ArrayList(value.size)) {
        ValueDescAnalyzer.analyze(it, elementType)
    }


    override fun <D, R> accept(visitor: ValueDescVisitor<D, R>, data: D): R = visitor.visitObjectArray(this, data)

}