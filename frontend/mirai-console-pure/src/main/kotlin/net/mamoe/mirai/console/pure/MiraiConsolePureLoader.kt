/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "INVISIBLE_SETTER",
    "INVISIBLE_GETTER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPE_WARNING"
)

package net.mamoe.mirai.console.pure

import net.mamoe.mirai.console.MiraiConsoleInitializer
import net.mamoe.mirai.console.command.CommandExecuteStatus
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.executeCommandDetailed
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.DefaultLogger
import kotlin.concurrent.thread

object MiraiConsolePureLoader {
    @JvmStatic
    fun main(args: Array<String>?) {
        startup()
    }
}


internal fun startup() {
    MiraiConsoleInitializer.init(MiraiConsolePure)
    startConsoleThread()
}

internal fun startConsoleThread() {
    thread(name = "Console", isDaemon = false) {
        val consoleLogger = DefaultLogger("Console")
        kotlinx.coroutines.runBlocking {
            while (true) {
                val next = MiraiConsoleFrontEndPure.requestInput("")
                consoleLogger.debug("INPUT> $next")
                val result = ConsoleCS.executeCommandDetailed(next)
                when (result.status) {
                    CommandExecuteStatus.SUCCESSFUL -> {
                    }
                    CommandExecuteStatus.EXECUTION_EXCEPTION -> {
                    }
                    CommandExecuteStatus.COMMAND_NOT_FOUND -> {
                        consoleLogger.warning("Unknown command: ${result.commandName}")
                    }
                }
            }
        }
    }
}

object ConsoleCS : ConsoleCommandSender() {
    override suspend fun sendMessage(message: Message) {
        ConsoleUtils.lineReader.printAbove(message.contentToString())
    }
}