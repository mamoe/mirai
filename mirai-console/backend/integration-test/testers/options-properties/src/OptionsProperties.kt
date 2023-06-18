/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package consoleittest.optionproperties.main

import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public object OptionsProperties : KotlinPlugin(
    JvmPluginDescription("net.mamoe.console.itest.options_properties.main", "0.0.0")
) {
    override fun PluginComponentStorage.onLoad() {
        assertTrue { jvmPluginClasspath.shouldResolveConsoleSystemResource }
        assertFalse { jvmPluginClasspath.shouldBeResolvableToIndependent }
        assertFalse { jvmPluginClasspath.shouldResolveIndependent }
    }

    override fun onEnable() {
        assertFails {
            // class.loading.load-independent = false
            Class.forName("consoleittest.optionproperties.independent.Independent")
        }
    }
}