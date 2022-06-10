/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.ep.pddd

import net.mamoe.console.integrationtest.canVmLoad
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import org.objectweb.asm.Opcodes
import kotlin.test.assertEquals

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
    init {
        Class.forName("com.google.gson.Gson") // shared
        Class.forName("com.zaxxer.sparsebits.SparseBitSet") // private
    }

    override fun PluginComponentStorage.onLoad() {
        Class.forName("com.google.gson.Gson") // shared
        Class.forName("com.zaxxer.sparsebits.SparseBitSet") // private

        // console-non-hard-link dependency
        // mirai-core used 1.64 current
        val bc = Class.forName("org.bouncycastle.LICENSE")
        assertEquals(
            "1.63.0",
            bc.`package`.implementationVersion,
            message = "$bc <- ${bc.classLoader}"
        )
        // #2009
        if (canVmLoad(Opcodes.V11)) {
            logger.info { "V11" }
            Class.forName("java.net.http.HttpClient")
        }
    }
}
