/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package consoleittest.optionproperties.independent

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import kotlin.test.assertFails

public object Independent : KotlinPlugin(
    JvmPluginDescription("net.mamoe.console.itest.options_properties.independent_plugin", "0.0.0")
) {
    override fun onEnable() {
        assertFails {
            // parent's class.loading.be-resolvable-to-independent = false
            Class.forName("consoleittest.optionproperties.main.OptionsProperties")
        }
    }
}