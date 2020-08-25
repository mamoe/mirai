/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.pure

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.CommandExecuteStatus
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.console.util.requestInput
import net.mamoe.mirai.utils.DefaultLogger
import org.fusesource.jansi.Ansi
import org.jline.reader.UserInterruptException
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@OptIn(ConsoleInternalAPI::class)
internal fun startupConsoleThread() {
    val mutex = Mutex()
    ConsoleUtils.miraiLineReader = { hint ->
        mutex.withLock {
            withContext(Dispatchers.IO) {
                println("Requesting input")
                ConsoleUtils.lineReader.readLine(
                    if (hint.isNotEmpty()) {
                        ConsoleUtils.lineReader.printAbove(
                            Ansi.ansi()
                                .fgCyan().a(sdf.format(Date())).a(" ")
                                .fgMagenta().a(hint)
                                .reset()
                                .toString()
                        )
                        "$hint > "
                    } else "> "
                )
            }
        }
    }

    val consoleLogger = DefaultLogger("console")

    val inputThread = thread(start = true, isDaemon = false, name = "Console Input") {
        try {
            runBlocking {
                while (true) {
                    try {
                        val next = MiraiConsole.requestInput("").let {
                            when {
                                it.startsWith(CommandManager.commandPrefix) -> it
                                it == "?" -> CommandManager.commandPrefix + BuiltInCommands.Help.primaryName
                                else -> CommandManager.commandPrefix + it
                            }
                        }
                        exitProcess(123456)
                        if (next.isBlank()) {
                            continue
                        }
                        // consoleLogger.debug("INPUT> $next")
                        val result = ConsoleCommandSenderImpl.executeCommand(next)
                        when (result.status) {
                            CommandExecuteStatus.SUCCESSFUL -> {
                            }
                            CommandExecuteStatus.EXECUTION_EXCEPTION -> {
                                result.exception?.printStackTrace()
                            }
                            CommandExecuteStatus.COMMAND_NOT_FOUND -> {
                                consoleLogger.warning("未知指令: ${result.commandName}, 输入 ? 获取帮助")
                            }
                            CommandExecuteStatus.PERMISSION_DENIED -> {
                                consoleLogger.warning("Permission denied.")
                            }
                        }
                    } catch (e: InterruptedException) {
                        return@runBlocking
                    } catch (e: CancellationException) {
                        return@runBlocking
                    } catch (e: UserInterruptException) {
                        MiraiConsole.cancel()
                        return@runBlocking
                    } catch (e: Throwable) {
                        consoleLogger.error("Unhandled exception", e)
                    }
                }
            }
        } catch (e: InterruptedException) {
            return@thread
        } catch (e: CancellationException) {
            return@thread
        } catch (e: UserInterruptException) {
            MiraiConsole.cancel()
            return@thread
        } catch (e: Throwable) {
            consoleLogger.error("Unhandled exception", e)
        }
    }

    MiraiConsole.job.invokeOnCompletion {
        runCatching {
            inputThread.interrupt()
        }.exceptionOrNull()?.printStackTrace()
        runCatching {
            ConsoleUtils.terminal.close()
        }.exceptionOrNull()?.printStackTrace()
    }
}
