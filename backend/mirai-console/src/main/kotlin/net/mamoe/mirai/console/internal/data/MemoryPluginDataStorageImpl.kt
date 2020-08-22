/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.util.ConsoleInternalAPI

internal class MemoryPluginDataStorageImpl(
    private val onChanged: MemoryPluginDataStorage.OnChangedCallback
) : PluginDataStorage, MemoryPluginDataStorage,
    MutableMap<Class<out PluginData>, PluginData> by mutableMapOf() {

    internal inner class MemoryPluginDataImpl : AbstractPluginData() {
        @ConsoleInternalAPI
        override fun onValueChanged(value: Value<*>) {
            onChanged.onChanged(this@MemoryPluginDataStorageImpl, value)
        }

        override fun setStorage(storage: PluginDataStorage) {
            check(storage is MemoryPluginDataStorageImpl) { "storage is not MemoryPluginDataStorageImpl" }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : PluginData> load(holder: PluginDataHolder, dataClass: Class<T>): T = (synchronized(this) {
        this.getOrPut(dataClass) {
            dataClass.kotlin.run {
                objectInstance ?: createInstanceOrNull() ?: kotlin.run {
                    if (dataClass != PluginData::class.java) {
                        throw IllegalArgumentException(
                            "Cannot create PluginData instance. Make sure dataClass is PluginData::class.java or a Kotlin's object, " +
                                    "or has a constructor which either has no parameters or all parameters of which are optional"
                        )
                    }
                    MemoryPluginDataImpl()
                }
            }
        }
    } as T).also { it.setStorage(this) }

    override fun store(holder: PluginDataHolder, pluginData: PluginData) {
        synchronized(this) {
            this[pluginData::class.java] = pluginData
        }
    }
}