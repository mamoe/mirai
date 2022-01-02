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
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

class ClassValueDesc<T : Any>(
    override val parent: ValueDesc?,
    override val origin: T,
    val properties: MutableMap<KParameter, ValueDesc>,
) : ValueDesc {
    val type: KClass<out T> by lazy { origin::class }

    override fun <D, R> accept(visitor: ValueDescVisitor<D, R>, data: D): R {
        return visitor.visitClass(this, data)
    }

    override fun <D> acceptChildren(visitor: ValueDescVisitor<D, *>, data: D) {
        properties.forEach { (_, u) ->
            u.accept(visitor, data)
        }
    }

    override fun <D> transformChildren(visitor: ValueDescTransformer<D>, data: D) {
        val result = mutableMapOf<KParameter, ValueDesc>()
        for (entry in this.properties.entries) {
            entry.value.acceptChildren(visitor, data)
            val newValue = entry.value.accept(visitor, data)
            if (newValue != null) {
                result[entry.key] = newValue
            }
        }
        this.properties.clear()
        this.properties.putAll(result)
    }
}