/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DefaultLoginSolver
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.LoginSolverInputReader
import kotlin.concurrent.thread

class MiraiConsoleUIPure : MiraiConsoleUI {
    var requesting = false
    var requestStr = ""

    init {
        thread {
            while (true) {
                val input = readLine() ?: return@thread
                if (requesting) {
                    requestStr = input
                    requesting = false
                } else {
                    runBlocking {
                        MiraiConsole.CommandListener.commandChannel.send(input)
                    }
                }
            }
        }
    }

    override fun pushLog(identity: Long, message: String) {
        println(message)
    }

    override fun prePushBot(identity: Long) {

    }

    override fun pushBot(bot: Bot) {

    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {

    }

    override suspend fun requestInput(question: String): String {
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
            reader = object : LoginSolverInputReader {
                override suspend fun read(question: String): String? {
                    return requestInput(question)
                }
            }
        )
    }

}


class MiraiConsolePureLoader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MiraiConsole.start(MiraiConsoleUIPure())
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                MiraiConsole.stop()
            })
        }
    }
}