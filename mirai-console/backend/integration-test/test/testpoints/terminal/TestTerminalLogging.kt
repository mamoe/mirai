/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.console.integrationtest.testpoints.terminal

import net.mamoe.console.integrationtest.AbstractTestPoint
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.terminal.LoggingServiceI
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.info
import java.io.File
import java.util.*
import kotlin.math.min
import kotlin.test.assertTrue
import kotlin.test.fail

internal object TestTerminalLogging : AbstractTestPoint() {
    override fun beforeConsoleStartup() {
        System.setProperty("mirai.console.terminal.log.buffer", "10")
    }

    override fun onConsoleStartSuccessfully() {
        val logService = MiraiConsoleImplementation.getInstance()
            .origin
            .cast<MiraiConsoleImplementationTerminal>()
            .logService.cast<LoggingServiceI>()

        logService.autoSplitTask.cancel(true)
        File("logs/log-0.log").delete()

        val stub = "Terminal Test: STUB" + UUID.randomUUID()
        MiraiConsole.mainLogger.info { stub }
        Thread.sleep(200L)

        assertTrue { File("logs/latest.log").isFile }
        assertTrue { File("logs/latest.log").readText().contains(stub) }

        logService.switchLogFileNow.invoke()

        assertTrue { File("logs/latest.log").isFile }
        assertTrue { !File("logs/latest.log").readText().contains(stub) }
        assertTrue { File("logs/log-0.log").isFile }
        assertTrue { File("logs/log-0.log").readText().contains(stub) }

        MiraiConsole.mainLogger.info("Pipeline size: " + logService.pipelineSize)

        val logs = mutableListOf<String>()
        logs.add("1================================================================")
        repeat(100) { logs.add("TEST LINE $it -") }
        logs.add("2================================================================")

        logs.forEach { MiraiConsole.mainLogger.info(it) }

        Thread.sleep(200L)

        val lns = File("logs/latest.log").readLines().mapNotNull { line ->
            logs.forEach { lx ->
                if (line.contains(lx)) {
                    return@mapNotNull lx
                }
            }
            return@mapNotNull null
        }

        logService.switchLogFileNow.invoke()
        // lns.forEach { println(it) }
        var matched = 0
        for (i in 0 until min(lns.size, logs.size)) {
            if (lns[i] == logs[i]) matched++
        }
        println("Matched line: $matched, logs: ${logs.size}")
        if (matched < (logs.size * 80 / 100)) {
            lns.forEach { System.err.println(it) }
            fail()
        }
    }
}