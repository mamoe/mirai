/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.ep.dependonother

import net.mamoe.console.integrationtest.ep.mcitselftest.MCITSelfTestPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

/*
PluginDependOnOther: 测试插件依赖其他插件的情况
 */
public object PluginDependOnOther : KotlinPlugin(
    JvmPluginDescription(
        id = "net.mamoe.tester.plugin-depend-on-other",
        version = "1.0.0",
        name = "Plugin Depend On Other",
    ) {
        dependsOn("net.mamoe.tester.mirai-console-self-test")
        dependsOn("net.mamoe.tester.plugin-dynamic-dependencies-download")
    }
) {
    override fun onEnable() {
        logger.info { "Do dependency call: " + MCITSelfTestPlugin::class.java }
        logger.info { "No Depends on: " + Class.forName("samepkg.P") }
        logger.info(Throwable("Stack trace"))
        MCITSelfTestPlugin.someAction()
        logger.info { "Shared library: " + Class.forName("net.mamoe.console.it.psl.PluginSharedLib") }
        assertNotEquals(javaClass.classLoader, Class.forName("net.mamoe.console.it.psl.PluginSharedLib").classLoader)

        // dependencies-shared
        kotlin.run {
            val pluginDepDynDownload = Class.forName("net.mamoe.console.integrationtest.ep.pddd.P")
            val gsonC = Class.forName("com.google.gson.Gson")
            logger.info { "Gson located $gsonC <${gsonC.classLoader}>" }
            assertSame(gsonC, Class.forName(gsonC.name, false, pluginDepDynDownload.classLoader))
            assertFailsWith<ClassNotFoundException> {
                val c = Class.forName("com.zaxxer.sparsebits.SparseBitSet") // private in dynamic-dep-download
                logger.error { "C: $c, from: ${c.classLoader}" }
            }
            assertFailsWith<ClassNotFoundException> {
                Class.forName("net.mamoe.assertion.something.not.existing")
            }
        }

        // region https://github.com/mamoe/mirai/issues/1920
        Class.forName("net.mamoe.console.integrationtest.ep.pddd.p2.PDOO_OtherClass")
        Class.forName("net.mamoe.console.integrationtest.ep.pddd.PDOO_OtherClass")
        // endregion
    }
}
