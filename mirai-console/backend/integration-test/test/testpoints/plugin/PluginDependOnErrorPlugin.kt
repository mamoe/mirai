/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.testpoints.plugin

import net.mamoe.console.integrationtest.AbstractTestPointAsPlugin
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.internal.plugin.ConsoleJvmPluginFuncCallbackStatus
import net.mamoe.mirai.console.internal.plugin.ConsoleJvmPluginFuncCallbackStatusExcept
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.fail

@ConsoleJvmPluginFuncCallbackStatusExcept.OnEnable(ConsoleJvmPluginFuncCallbackStatus.FAILED)
internal object PluginDependOnErrorPlugin : AbstractTestPointAsPlugin() {
    private var isOnEnabledExecuted: Boolean = false

    override fun newPluginDescription(): JvmPluginDescription {
        return JvmPluginDescription(
            id = "net.mamoe.testpoint.plugin-depend-on-error-plugin",
            version = "1.0.0",
            name = "PluginDependOnErrorPlugin",
        ) {
            dependsOn("net.mamoe.testpoint.plugin-with-exception-test")
        }
    }

    override fun beforeConsoleStartup() {
        isOnEnabledExecuted = false
    }

    override fun KotlinPlugin.onLoad0(storage: PluginComponentStorage) {

    }

    override fun KotlinPlugin.onEnable0() {
        // unreachable
        isOnEnabledExecuted = true
        fail("net.mamoe.testpoint.plugin-depend-on-error-plugin enabled")
    }

    override fun onConsoleStartSuccessfully() {
        assertFalse { isOnEnabledExecuted }
        assertFalse {
            PluginManager
                .plugins
                .first { it.id == "net.mamoe.testpoint.plugin-with-exception-test" }
                .isEnabled
        }
        assertFalse {
            PluginManager
                .plugins
                .first { it.id == "net.mamoe.testpoint.plugin-depend-on-error-plugin" }
                .isEnabled
        }
    }
}
