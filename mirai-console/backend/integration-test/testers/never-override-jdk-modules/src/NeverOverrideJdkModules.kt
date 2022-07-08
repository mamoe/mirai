/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package neveroverridejdkmodules

import net.mamoe.console.integrationtest.assertClassSame
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

public object NeverOverrideJdkModules : KotlinPlugin(
    JvmPluginDescription("net.mamoe.console.itest.never-override-jdk-modules", "0.0.0")
) {
    init {
        jvmPluginClasspath.downloadAndAddToPath(
            jvmPluginClasspath.pluginSharedLibrariesClassLoader,
            listOf("net.mamoe.consoleit.issue2141:javax-xml:1.0.0")
        )
    }

    @Suppress("Since15")
    override fun onEnable() {
        val platformC = ClassLoader.getSystemClassLoader().loadClass("javax.xml.parsers.SAXParser")
        assertClassSame(
            platformC,
            Class.forName("javax.xml.parsers.SAXParser"),
        )
        assertClassSame(
            platformC,
            jvmPluginClasspath.pluginClassLoader.loadClass("javax.xml.parsers.SAXParser"),
        )
        assertClassSame(
            platformC,
            jvmPluginClasspath.pluginIndependentLibrariesClassLoader.loadClass("javax.xml.parsers.SAXParser"),
        )
        assertClassSame(
            platformC,
            jvmPluginClasspath.pluginSharedLibrariesClassLoader.loadClass("javax.xml.parsers.SAXParser"),
        )
    }
}
