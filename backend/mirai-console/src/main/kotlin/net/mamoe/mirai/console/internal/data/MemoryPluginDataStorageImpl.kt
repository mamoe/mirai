/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import net.mamoe.mirai.console.data.MemoryPluginDataStorage
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.PluginDataHolder
import net.mamoe.mirai.console.data.PluginDataStorage

internal class MemoryPluginDataStorageImpl : PluginDataStorage, MemoryPluginDataStorage,
    MutableMap<Class<out PluginData>, PluginData> by mutableMapOf() {

    @Suppress("UNCHECKED_CAST")
    override fun load(holder: PluginDataHolder, instance: PluginData) {
        instance.onInit(holder, this)
    }

    override fun store(holder: PluginDataHolder, instance: PluginData) {
        // no-op
    }
}