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
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescTransformerNotNull
import net.mamoe.mirai.internal.testFramework.codegen.visitor.ValueDescVisitor

sealed interface ValueDesc {
    val origin: Any?
    val parent: ValueDesc?

    fun <D, R> accept(visitor: ValueDescVisitor<D, R>, data: D): R
    fun <D> acceptChildren(visitor: ValueDescVisitor<D, *>, data: D)

    fun <D> transform(visitor: ValueDescTransformer<D>, data: D): ValueDesc? = this.accept(visitor, data)
    fun <D> transformChildren(visitor: ValueDescTransformer<D>, data: D)
}

fun <R> ValueDesc.accept(visitor: ValueDescVisitor<Nothing?, R>): R = accept(visitor, null)
fun ValueDesc.transform(visitor: ValueDescTransformer<Nothing?>) = transform(visitor, null)
fun ValueDesc.transform(visitor: ValueDescTransformerNotNull<Nothing?>) = transform(visitor, null)!!
fun <R> ValueDesc.acceptChildren(visitor: ValueDescVisitor<Nothing?, R>) = acceptChildren(visitor, null)
fun ValueDesc.transformChildren(visitor: ValueDescTransformer<Nothing?>) = transformChildren(visitor, null)

val ValueDesc.parents
    get() = sequence {
        var parent = parent
        do {
            parent ?: return@sequence
            yield(parent)
            parent = parent.parent
        } while (true)
    }

inline fun <reified T : ValueDesc> ValueDesc.findParent(): T? = parents.filterIsInstance<T>().firstOrNull()
