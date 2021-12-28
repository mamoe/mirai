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
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import kotlin.test.*

/*
MCITBSelfAssertions: 用于检查 Integration Test 可以正常加载 AbstractTestPointAsPlugin 与 外部测试插件
 */
internal object MCITBSelfAssertions : AbstractTestPointAsPlugin() {
    override fun newPluginDescription(): JvmPluginDescription {
        return JvmPluginDescription(
            id = "net.mamoe.testpoint.mirai-console-self-assertions",
            version = "1.0.0",
            name = "MCITBSelfAssertions",
        )
    }

    var called = false

    override fun KotlinPlugin.onEnable0() {
        called = true
        assertFails { error("") }
        assertTrue { true }
        assertFalse { false }
        assertFailsWith<InternalError> { throw InternalError("") }
        assertEquals("", "")
        assertSame(this, this)
    }

    override fun onConsoleStartSuccessfully() {
        assertTrue(called, "Mirai Console IntegrationTestBootstrap Internal Error")

        assertTrue("MCITSelfTestPlugin not found") {
            PluginManager.plugins.any { it.description.id == "net.mamoe.tester.mirai-console-self-test" }
        }
    }
}