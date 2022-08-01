/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.testpoints.plugin

import net.mamoe.console.integrationtest.AbstractTestPointAsPlugin
import net.mamoe.console.integrationtest.assertNotExists
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import java.io.File
import kotlin.test.assertEquals

internal object PluginDataRenameToIdTest : AbstractTestPointAsPlugin() {

    object TestData : AutoSavePluginData("testdata") {
        public val test: String by value("")
    }

    object TestConf : AutoSavePluginConfig("testconf") {
        public val test: String by value("")
    }

    override fun newPluginDescription(): JvmPluginDescription {
        return JvmPluginDescription(
            id = "net.mamoe.testpoint.plugin-data-rename-to-id-test",
            version = "1.0.0",
            name = "PluginDataRenameToIdTest",
        )
    }

    override fun beforeConsoleStartup() {
        File("config/PluginDataRenameToIdTest").mkdirs()
        File("config/PluginDataRenameToIdTest/test.txt").createNewFile()
        File("config/PluginDataRenameToIdTest/testconf.yml").writeText(
            """
            test: a
            """.trimIndent()
        )
        File("data/PluginDataRenameToIdTest").mkdirs()
        File("data/PluginDataRenameToIdTest/test.txt").createNewFile()
        File("data/PluginDataRenameToIdTest/testdata.yml").writeText(
            """
            test: a
            """.trimIndent()
        )
    }

    override fun KotlinPlugin.onLoad0(storage: PluginComponentStorage) {

        File("config/PluginDataRenameToIdTest").assertNotExists()
        File("data/PluginDataRenameToIdTest").assertNotExists()

    }

    override fun KotlinPlugin.onEnable0() {
        TestData.reload()
        TestConf.reload()
        TestData.save()
        TestConf.save()

        assertEquals("a", TestConf.test)
        assertEquals("a", TestData.test)
    }

    override fun onConsoleStartSuccessfully() {
        File("config/PluginDataRenameToIdTest/test.txt").assertNotExists()
        File("data/PluginDataRenameToIdTest/test.txt").assertNotExists()


        File("config/PluginDataRenameToIdTest/testconf.yml").assertNotExists()
        File("data/PluginDataRenameToIdTest/testdata.yml").assertNotExists()
    }
}
