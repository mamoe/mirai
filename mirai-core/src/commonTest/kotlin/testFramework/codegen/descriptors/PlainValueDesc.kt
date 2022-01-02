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

class PlainValueDesc(
    override val parent: ValueDesc?,
    var value: String,
    override val origin: Any?
) : ValueDesc {
    init {
        require(value.isNotBlank())
    }

    override fun <D, R> accept(visitor: ValueDescVisitor<D, R>, data: D): R {
        return visitor.visitPlain(this, data)
    }

    override fun <D> acceptChildren(visitor: ValueDescVisitor<D, *>, data: D) {
    }

    override fun <D> transformChildren(visitor: ValueDescTransformer<D>, data: D) {
    }
}