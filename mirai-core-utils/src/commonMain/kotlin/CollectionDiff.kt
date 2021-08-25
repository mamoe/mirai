/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

public class CollectionDiff<E> {
    private var save: Collection<E> = listOf()

    public fun save(collection: Collection<E>) {
        save = collection.toList()
    }

    public fun subtract(collection: Collection<E>): Collection<E> = collection subtract save

    public fun subtractAndSave(collection: Collection<E>): Collection<E> {
        return subtract(collection).also { save(collection) }
    }
}