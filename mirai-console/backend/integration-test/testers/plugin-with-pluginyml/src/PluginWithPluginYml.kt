package net.mamoe.console.itest.pluginwithpluginyml

import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import kotlin.test.assertTrue

/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

internal object PluginWithPluginYml : KotlinPlugin() {
    override fun onEnable() {
        println(description)
        println(description.id)
        val pluginId = description.id

        assertTrue {
            PluginManager.plugins.first { it.description.id == pluginId } === PluginWithPluginYml
        }
    }
}