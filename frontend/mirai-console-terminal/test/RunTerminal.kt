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
import net.mamoe.mirai.console.MiraiConsole
import java.io.File

fun main() {
    configureUserDir()

    MiraiConsoleTerminalLoader.startAsDaemon()
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