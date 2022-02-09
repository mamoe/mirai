/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER")

package net.mamoe.console.integrationtest.testpoints.terminal

import net.mamoe.console.integrationtest.AbstractTestPoint
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.info
import java.io.File
import java.util.*
import kotlin.test.assertTrue

internal object TestTerminalLogging : AbstractTestPoint() {
    override fun onConsoleStartSuccessfully() {
        val stub = "Terminal Test: STUB" + UUID.randomUUID()
        MiraiConsole.mainLogger.info { stub }
        assertTrue { File("logs/latest.log").isFile }
        assertTrue { File("logs/latest.log").readText().contains(stub) }
        MiraiConsoleImplementation.getInstance()
            .cast<MiraiConsoleImplementationTerminal>()
            .logService.switchLogFileNow.invoke()
        assertTrue { File("logs/latest.log").isFile }
        assertTrue { !File("logs/latest.log").readText().contains(stub) }
        assertTrue { File("logs/log-0.log").isFile }
        // Don't check content of "logs/log-0.log" because maybe daily auto split invoked
    }
}