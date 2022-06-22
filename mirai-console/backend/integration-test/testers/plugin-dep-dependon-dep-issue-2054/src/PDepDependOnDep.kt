/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package pdepdep2054

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

public object PDepDependOnDep : KotlinPlugin(
    JvmPluginDescription("net.mamoe.console.itest.plugin-dep-dependon-dep", "0.0.0")
) {
    init {
        jvmPluginClasspath.downloadAndAddToPath(
            jvmPluginClasspath.pluginSharedLibrariesClassLoader,
            listOf("net.mamoe.consoleit.issue2054:moda:1.0.0")
        )
        jvmPluginClasspath.downloadAndAddToPath(
            jvmPluginClasspath.pluginIndependentLibrariesClassLoader,
            listOf("net.mamoe.consoleit.issue2108:private-module:1.0.0")
        )
    }
}
