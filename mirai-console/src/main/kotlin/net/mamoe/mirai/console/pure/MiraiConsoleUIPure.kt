/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.pure

import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.utils.DefaultLoginSolver
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger.LogPriority
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MiraiConsoleUIPure : MiraiConsoleUI {
    private var requesting = false
    private var requestStr = ""

    @Suppress("unused")
    companion object {
        // ANSI color codes
        const val COLOR_RED = "\u001b[38;5;196m"
        const val COLOR_CYAN = "\u001b[38;5;87m"
        const val COLOR_GREEN = "\u001b[38;5;82m"

        // use a dark yellow(more like orange) instead of light one to save Solarized-light users
        const val COLOR_YELLOW = "\u001b[38;5;220m"
        const val COLOR_GREY = "\u001b[38;5;244m"
        const val COLOR_BLUE = "\u001b[38;5;27m"
        const val COLOR_NAVY = "\u001b[38;5;24m" // navy uniform blue
        const val COLOR_PINK = "\u001b[38;5;207m"
        const val COLOR_RESET = "\u001b[39;49m"
    }

    init {
        thread {
            while (true) {
                val input = readLine() ?: return@thread
                if (requesting) {
                    requestStr = input
                    requesting = false
                } else {
                    MiraiConsole.CommandProcessor.runConsoleCommandBlocking(input)
                }
            }
        }
    }

    val sdf by lazy {
        SimpleDateFormat("HH:mm:ss")
    }

    override fun pushLog(identity: Long, message: String) {
        println("\u001b[0m " + sdf.format(Date()) + " $message")
    }

    override fun pushLog(priority: LogPriority, identityStr: String, identity: Long, message: String) {
        var priorityStr = "[${priority.name}]"
        val _message = message + COLOR_RESET
        if (MiraiConsole.frontEnd is MiraiConsoleUIPure) {
            /**
             * 通过ANSI控制码添加颜色
             * 更多的颜色定义在 [MiraiConsoleUIPure] 的  companion
             */
            priorityStr = when (priority) {
                LogPriority.ERROR
                -> COLOR_RED + priorityStr + COLOR_RESET

                LogPriority.WARNING
                -> COLOR_RED + priorityStr + COLOR_RESET

                LogPriority.VERBOSE
                -> COLOR_NAVY + priorityStr + COLOR_RESET

                LogPriority.DEBUG
                -> COLOR_PINK + priorityStr + COLOR_RESET

                else -> priorityStr
            }
            println("\u001b[0m " + sdf.format(Date()) + " $priorityStr $identityStr $_message")
        }
    }

    override fun prePushBot(identity: Long) {

    }

    override fun pushBot(bot: Bot) {

    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {

    }

    override suspend fun requestInput(): String {
        requesting = true
        while (true) {
            delay(50)
            if (!requesting) {
                return requestStr
            }
        }
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) {

    }

    override fun createLoginSolver(): LoginSolver {
        return DefaultLoginSolver(
            input = suspend {
                requestInput()
            }
        )
    }

}


