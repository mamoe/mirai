/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils


public class ListIndexer @PublishedApi internal constructor(
    private val lastIndex: Int
) {
    @PublishedApi
    internal var currentIndex: Int = 0

    public fun setNextIndex(index: Int) {
        require(index <= lastIndex) { "it must follows index <= lastIndex" }
        currentIndex = index
    }

    @PublishedApi
    internal fun nextIndex(): Boolean {
        val next = currentIndex + 1
        if (next <= lastIndex) {
            currentIndex = next
            return true
        }
        return false
    }
}

public inline fun <C, L> L.forEachWithIndexer(block: ListIndexer.(item: C) -> Unit) where L : List<C>, L : RandomAccess {
    ListIndexer(lastIndex).run {
        do {
            block(get(currentIndex))
        } while (nextIndex())
    }
}

