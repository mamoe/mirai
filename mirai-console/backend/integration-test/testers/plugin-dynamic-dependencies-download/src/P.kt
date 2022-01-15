/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.ep.pddd

import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

/*
PluginDynamicDependenciesDownload: 测试动态运行时下载
 */
internal object P : KotlinPlugin(
    JvmPluginDescription(
        id = "net.mamoe.tester.plugin-dynamic-dependencies-download",
        version = "1.0.0",
        name = "Plugin Dynamic Dependencies Download",
    )
) {
    override fun PluginComponentStorage.onLoad() {
        Class.forName("com.google.gson.Gson") // shared
        Class.forName("com.zaxxer.sparsebits.SparseBitSet") // private
    }
}
