/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.component

import net.mamoe.mirai.utils.TestOnly
import net.mamoe.mirai.utils.cast

internal interface MutableComponentStorage : ComponentStorage {
    override operator fun <T : Any> get(key: ComponentKey<T>): T
    operator fun <T : Any> set(key: ComponentKey<T>, value: T)
    fun <T : Any> remove(key: ComponentKey<T>): T?

}

@TestOnly
internal fun MutableComponentStorage.setAll(other: ComponentStorage) {
    for (key in other.keys) {
        set(key.cast(), other[key])
    }
}

internal operator fun <T : Any> MutableComponentStorage.set(key: ComponentKey<T>, value: T?) {
    if (value == null) {
        remove(key)
    } else {
        set(key, value)
    }
}