/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.CommandExecuteStatus
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.terminal.noconsole.NoConsole
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.requestInput
import net.mamoe.mirai.utils.DefaultLogger
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException

val consoleLogger by lazy { DefaultLogger("console") }

@OptIn(ConsoleInternalApi::class, ConsoleTerminalExperimentalApi::class)
internal fun startupConsoleThread() {
    if (terminal is NoConsole) return

    MiraiConsole.launch(CoroutineName("Input Cancelling Daemon")) {
        while (true) {
            delay(2000)
        }
    }.invokeOnCompletion {
        runCatching<Unit> {
            terminal.close()
            ConsoleInputImpl.thread.shutdownNow()
            runCatching {
                ConsoleInputImpl.executingCoroutine?.cancel(EndOfFileException())
            }
        }.exceptionOrNull()?.printStackTrace()
    }
    MiraiConsole.launch(CoroutineName("Console Command")) {
        while (true) {
            try {
                val next = MiraiConsole.requestInput("").let {
                    when {
                        it.isBlank() -> it
                        it.startsWith(CommandManager.commandPrefix) -> it
                        it == "?" -> CommandManager.commandPrefix + BuiltInCommands.HelpCommand.primaryName
                        else -> CommandManager.commandPrefix + it
                    }
                }
                if (next.isBlank()) {
                    continue
                }
                // consoleLogger.debug("INPUT> $next")
                val result = ConsoleCommandSender.executeCommand(next)
                when (result.status) {
                    CommandExecuteStatus.SUCCESSFUL -> {
                    }
                    CommandExecuteStatus.EXECUTION_EXCEPTION -> {
                        result.exception?.let(consoleLogger::error)
                    }
                    CommandExecuteStatus.COMMAND_NOT_FOUND -> {
                        consoleLogger.warning("未知指令: ${result.commandName}, 输入 ? 获取帮助")
                    }
                    CommandExecuteStatus.PERMISSION_DENIED -> {
                        consoleLogger.warning("Permission denied.")
                    }
                }
            } catch (e: InterruptedException) {
                return@launch
            } catch (e: CancellationException) {
                return@launch
            } catch (e: UserInterruptException) {
                BuiltInCommands.StopCommand.run { ConsoleCommandSender.handle() }
                return@launch
            } catch (eof: EndOfFileException) {
                consoleLogger.warning("Closing input service...")
                return@launch
            } catch (e: Throwable) {
                consoleLogger.error("Unhandled exception", e)
            }
        }
    }
}
