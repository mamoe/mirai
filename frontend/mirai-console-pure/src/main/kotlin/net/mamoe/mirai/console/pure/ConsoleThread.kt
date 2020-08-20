/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.pure

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommandDetailed
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.DefaultLogger
import org.fusesource.jansi.Ansi
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread

@ConsoleInternalAPI
internal fun startupConsoleThread() {
    val service = Executors.newSingleThreadExecutor { code ->
        thread(start = false, isDaemon = false, name = "Console Input", block = code::run)
    }
    val dispatch = service.asCoroutineDispatcher()
    ConsoleUtils.miraiLineReader = { hint ->
        withContext(dispatch) {
            ConsoleUtils.lineReader.readLine(
                if (hint.isNotEmpty()) {
                    ConsoleUtils.lineReader.printAbove(
                        Ansi.ansi()
                            .fgCyan().a(MiraiConsoleFrontEndPure.sdf.format(Date())).a(" ")
                            .fgMagenta().a(hint)
                            .reset()
                            .toString()
                    )
                    "$hint > "
                } else "> "
            )
        }
    }

    /*
    object : AbstractCommand(ConsoleCommandOwner, "test") {
        override val usage: String
            get() = "? Why usage"

        override suspend fun CommandSender.onCommand(args: Array<out Any>) {
            withContext(Dispatchers.IO) {
                launch { sendMessage("I1> " + MiraiConsole.frontEnd.requestInput("Value 1")) }
                launch { sendMessage("I2> " + MiraiConsole.frontEnd.requestInput("Value 2")) }
            }
        }

    }.register(true)
    */

    CoroutineScope(dispatch).launch {
        val consoleLogger = DefaultLogger("Console")
        while (isActive) {
            try {
                val next = MiraiConsoleFrontEndPure.requestInput("").let {
                    when {
                        it.startsWith(CommandManager.commandPrefix) -> {
                            it
                        }
                        it == "?" -> CommandManager.commandPrefix + BuiltInCommands.Help.primaryName
                        else -> CommandManager.commandPrefix + it
                    }
                }
                if (next.isBlank()) {
                    continue
                }
                consoleLogger.debug("INPUT> $next")
                val result = ConsoleCommandSenderImpl.executeCommandDetailed(next)
                when (result.status) {
                    CommandExecuteStatus.SUCCESSFUL -> {
                    }
                    CommandExecuteStatus.EXECUTION_EXCEPTION -> {
                        result.exception?.printStackTrace()
                    }
                    CommandExecuteStatus.COMMAND_NOT_FOUND -> {
                        consoleLogger.warning("Unknown command: ${result.commandName}")
                    }
                    CommandExecuteStatus.PERMISSION_DENIED -> {
                        consoleLogger.warning("Permission denied.")
                    }
                }
            } catch (e: InterruptedException) {
                return@launch
            } catch (e: Throwable) {
                consoleLogger.error("Unhandled exception", e)
            }
        }
    }.let { consoleJob ->
        MiraiConsole.job.invokeOnCompletion {
            runCatching {
                consoleJob.cancel()
            }.exceptionOrNull()?.printStackTrace()
            runCatching {
                service.shutdownNow()
            }.exceptionOrNull()?.printStackTrace()
            runCatching {
                ConsoleUtils.terminal.close()
            }.exceptionOrNull()?.printStackTrace()
        }
    }
}