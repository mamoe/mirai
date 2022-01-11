/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.testpoints

import net.mamoe.console.integrationtest.AbstractTestPointAsPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

/*
DoNothingPoint: Example
 */
internal object DoNothingPoint : AbstractTestPointAsPlugin() {
    var enableCalled = false
    override fun newPluginDescription(): JvmPluginDescription {
        return JvmPluginDescription(
            id = "net.mamoe.testpoint.do-nothing",
            version = "1.1.0",
            name = "DoNothing",
        )
    }

    override fun KotlinPlugin.onEnable0() {
        logger.info { "DoNothing.onEnable()  called" }
        enableCalled = true
    }

    override fun KotlinPlugin.onDisable0() {
        logger.info { "DoNothing.onDisable() called" }
    }

    override fun onConsoleStartSuccessfully() {
        assert(enableCalled) {
            "DoNothing.onEnable() not called."
        }
    }
}