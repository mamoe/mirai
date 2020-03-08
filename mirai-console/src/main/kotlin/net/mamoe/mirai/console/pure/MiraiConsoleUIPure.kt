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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MiraiConsoleUIPure : MiraiConsoleUI {
    private var requesting = false
    private var requestStr = ""

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

    val sdf by lazy{
        SimpleDateFormat("HH:mm:ss")
    }
    override fun pushLog(identity: Long, message: String) {
        println("\u001b[0m " + sdf.format(Date()) +" $message")
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


