/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package pluginresolveselfdepoverconsoleones

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.resources.*
import net.mamoe.console.integrationtest.assertClassSame
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

// 若插件定义依赖, 则使用插件依赖而不要使用 mirai-console 依赖
public class PluginResolveSelfDependenciesOverConsoleOnes :
    KotlinPlugin(JvmPluginDescription("net.mamoe.tester.plugin-resolve-self-dependencies-over-console-ones", "1.0.0")) {

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        logger.info {
            HttpClient(Java) {
                install(Resources)
            }.toString()
        }
        val hcC = Class.forName("io.ktor.client.HttpClient")
        assertClassSame(hcC, jvmPluginClasspath.pluginIndependentLibrariesClassLoader.loadClass(hcC.name))
    }
}