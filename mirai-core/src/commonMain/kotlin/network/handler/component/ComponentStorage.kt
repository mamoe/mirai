/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler.component

import org.jetbrains.annotations.TestOnly

/**
 * Facade for [component][ComponentKey]s. Implementation must be thread-safe.
 * @see MutableComponentStorage
 * @see ConcurrentComponentStorage
 */
internal interface ComponentStorage {
    @get:TestOnly
    val size: Int

    @Throws(NoSuchComponentException::class)
    operator fun <T : Any> get(key: ComponentKey<T>): T
    fun <T : Any> getOrNull(key: ComponentKey<T>): T?
}

