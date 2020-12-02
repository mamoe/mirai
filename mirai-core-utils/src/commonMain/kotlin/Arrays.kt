/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

public inline fun <A, reified B> Array<A>.mapToArray(block: (element: A) -> B): Array<B> {
    val result = arrayOfNulls<B>(size)
    this.forEachIndexed { index, element ->
        result[index] = block(element)
    }
    return result.cast()
}

public inline fun <A, reified B> Collection<A>.mapToArray(block: (element: A) -> B): Array<B> {
    val result = arrayOfNulls<B>(size)
    this.forEachIndexed { index, element ->
        result[index] = block(element)
    }
    return result.cast()
}

public inline fun <A> Collection<A>.mapToIntArray(block: (element: A) -> Int): IntArray {
    val result = IntArray(size)
    this.forEachIndexed { index, element ->
        result[index] = block(element)
    }
    return result.cast()
}