/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.component

internal class ComponentStorageDelegate(
    private val instance: () -> ComponentStorage
) : ComponentStorage {
    override val size: Int get() = instance().size
    override fun <T : Any> get(key: ComponentKey<T>): T = instance()[key]
    override fun <T : Any> getOrNull(key: ComponentKey<T>): T? = instance().getOrNull(key)
    override val keys: Set<ComponentKey<*>> get() = instance().keys
    override fun toString(): String = instance().toString()
}