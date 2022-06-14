/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extensions.BotConfigurationAlterer
import net.mamoe.mirai.console.logging.LoggerController
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger
import java.io.File

fun main() {
    configureUserDir()

    val terminal = object : MiraiConsoleImplementationTerminal() {
        override val loggerController: LoggerController = object : LoggerController {
            override fun shouldLog(identity: String?, priority: SimpleLogger.LogPriority): Boolean = true
        }
    }

    val mockPlugin = object : KotlinPlugin(JvmPluginDescription("org.test.test", "1.0.0")) {}

    MiraiConsoleTerminalLoader.startAsDaemon(terminal)

    terminal.backendAccess.globalComponentStorage.contribute(
        BotConfigurationAlterer,
        mockPlugin,
        BotConfigurationAlterer { _, configuration ->
            configuration.networkLoggerSupplier = { MiraiLogger.Factory.create(Bot::class, "Net.${it.id}") } // deploy
            configuration
        }
    )

    runCatching { runBlocking { MiraiConsole.job.join() } }
}

internal fun configureUserDir() {
    val projectDir = runCatching {
        File(".").resolve("frontend").resolve("mirai-console-terminal").takeIf { it.isDirectory }
            ?: File(".").resolve("mirai-console/frontend").resolve("mirai-console-terminal")
    }.getOrElse { return }
    if (projectDir.isDirectory) {
        val run = projectDir.resolve("run")
        run.mkdir()
        System.setProperty("user.dir", run.absolutePath)
        println("[Mirai Console] Set user.dir = ${run.absolutePath}")
    }
}