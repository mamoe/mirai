/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.setting.internal

import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.setting.Value
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf


@PublishedApi
@Suppress("UnsafeCall", "SMARTCAST_IMPOSSIBLE")
internal fun Setting.valueFromKTypeImpl(type: KType): Value<*> {
    require(type.classifier is KClass<*>)

    if (type.classifier.isPrimitiveOrBuiltInSerializableValue()) {
        TODO("是基础类型, 可以直接创建 ValueImpl. ")
    }

    // 复合类型

    when {
        type.classifier.isSubclassOf(Map::class) -> {

            TODO()
        }
        type.classifier.isSubclassOf(List::class) -> {

            TODO()
        }
        type.classifier.isSubclassOf(Set::class) -> {
            TODO()
        }
        else -> error("Custom composite value is not supported yet (${type.classifier.qualifiedName})")
    }
}

internal fun KClass<*>.isPrimitiveOrBuiltInSerializableValue(): Boolean {
    when (this) {
        Byte::class, Short::class, Int::class, Long::class,
        Boolean::class,
        Char::class, String::class,
        Pair::class, Triple::class
        -> return true
    }

    return false
}

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal inline fun <R, T> T.cast(): R = this as R