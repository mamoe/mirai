/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package pdepdep2054sec

import issue2054.modulea.ModuleA
import issue2054.moduleb.ModuleB
import issue2108.PrivateModule
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info
import kotlin.test.assertSame

public object PDepDependOnDepSec : KotlinPlugin(
    JvmPluginDescription("net.mamoe.console.itest.plugin-dep-dependon-dep-sec", "0.0.0") {
        dependsOn("net.mamoe.console.itest.plugin-dep-dependon-dep")
    }
) {
    override fun onEnable() {
        jvmPluginClasspath.downloadAndAddToPath(
            jvmPluginClasspath.pluginIndependentLibrariesClassLoader,
            listOf("net.mamoe.consoleit.issue2054:modb:1.0.0")
        )
        jvmPluginClasspath.downloadAndAddToPath(
            jvmPluginClasspath.pluginIndependentLibrariesClassLoader,
            listOf("net.mamoe.consoleit.issue2108:private-module:1.0.0")
        )

        assertSame(ModuleA, ModuleB.getModuleA)
        logger.info { "issue 2054" }

        ModuleB.act { ModuleA.act { logger.info(Throwable("Stack trace")) } }

        logger.info("issue 2108", PrivateModule.stack())
        assertSame(
            jvmPluginClasspath.pluginIndependentLibrariesClassLoader,
            PrivateModule.javaClass.classLoader,
            "Failed to load private module from " + jvmPluginClasspath.pluginIndependentLibrariesClassLoader
        )
    }
}
