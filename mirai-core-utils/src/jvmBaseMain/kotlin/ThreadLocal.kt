/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.reflect.KProperty


public fun <T : Any?> threadLocal(newInstance: () -> T): ThreadLocal<T> {
    return object : ThreadLocal<T>() {
        override fun initialValue(): T = newInstance()
    }
}

public operator fun <T> ThreadLocal<T>.getValue(t: Any?, property: KProperty<Any?>): T =
    this.get() as T // `get()` is from Java and has type of `T!`
