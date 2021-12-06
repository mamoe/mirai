/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.data

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.framework.AbstractConsoleTest
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.plugin.id
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.name
import org.junit.jupiter.api.Test

class PluginMovingTests : AbstractConsoleTest() {
    private val mockPluginWithName = object : KotlinPlugin(JvmPluginDescription("org.test1.test1", "1.0.0", "test1")) {}
    private val mockPluginWithName2 =
        object : KotlinPlugin(JvmPluginDescription("org.test2.test2", "1.0.0", "test2")) {}
    private val mockPluginWithName3 =
        object : KotlinPlugin(JvmPluginDescription("org.test2.test3", "1.0.0", "test3")) {}

    private fun mkdir(abstractPath: String) = PluginManager.pluginsDataPath.resolve(abstractPath).mkdir()

    @Test
    fun movingPluginPath() {
        // Normal move
        mkdir(mockPlugin.name)
        mockPlugin.load()
        assert(!MiraiConsole.job.isCancelled)
        // when id == name
        mkdir(mockPluginWithName.name)
        mockPluginWithName.load()
        assert(!MiraiConsole.job.isCancelled)
        // move to empty folder
        mkdir(mockPluginWithName2.name)
        mkdir(mockPluginWithName2.id)
        mockPluginWithName2.load()
        assert(!MiraiConsole.job.isCancelled)
        // fail move
        mkdir(mockPluginWithName3.name)
        mkdir(mockPluginWithName3.id)
        PluginManager.pluginsDataPath.resolve(mockPluginWithName3.id).toFile().resolve("x").createNewFile()
        mockPluginWithName3.load()
        assert(MiraiConsole.job.isCancelled)
    }
}