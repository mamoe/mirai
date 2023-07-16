/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.console.data.PluginDataHolder
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

@OptIn(ConsoleExperimentalApi::class)
internal class EndUserReadmeData : ReadOnlyPluginConfig("EndUserReadme") {
    val data: MutableMap<String, String> by value()

    private lateinit var storage_: PluginDataStorage
    private lateinit var owner_: PluginDataHolder
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        this.storage_ = storage
        this.owner_ = owner
    }

    internal fun saveNow() {
        storage_.store(owner_, this)
    }
}
