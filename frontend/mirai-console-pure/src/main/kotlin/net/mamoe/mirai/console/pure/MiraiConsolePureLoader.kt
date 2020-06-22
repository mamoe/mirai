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
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.executeCommand
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.PlatformLogger
import org.fusesource.jansi.Ansi
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
                kotlin.runCatching {
                    if (!ConsoleCS.executeCommand(PlainText(next))) { // No such command
                        consoleLogger.warning("Unknown command: " + next.split(' ')[0])
                    }
                }.onFailure {
                    consoleLogger.error("Exception in executing command: $next", it)
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