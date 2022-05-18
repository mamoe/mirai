/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.visitor

import net.mamoe.mirai.internal.testFramework.codegen.descriptors.*

interface ValueDescVisitor<D, R> {
    fun visitValue(desc: ValueDesc, data: D): R

    fun visitPlain(desc: PlainValueDesc, data: D): R {
        return visitValue(desc, data)
    }

    fun visitArray(desc: CollectionLikeValueDesc, data: D): R {
        return visitValue(desc, data)
    }

    fun visitObjectArray(desc: ObjectArrayValueDesc, data: D): R {
        return visitArray(desc, data)
    }

    fun visitCollection(desc: CollectionValueDesc, data: D): R {
        return visitArray(desc, data)
    }

    fun visitMap(desc: MapValueDesc, data: D): R {
        return visitValue(desc, data)
    }

    fun visitPrimitiveArray(desc: PrimitiveArrayValueDesc, data: D): R {
        return visitArray(desc, data)
    }

    fun <T : Any> visitClass(desc: ClassValueDesc<T>, data: D): R {
        return visitValue(desc, data)
    }
}
