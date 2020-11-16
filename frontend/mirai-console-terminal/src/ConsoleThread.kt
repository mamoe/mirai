/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandExecuteResult.*
import net.mamoe.mirai.console.command.CommandExecuteResult.CommandExecuteStatus.*
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.terminal.noconsole.NoConsole
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.requestInput
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.warning
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException

val consoleLogger by lazy { DefaultLogger("console") }

@OptIn(ConsoleInternalApi::class, ConsoleTerminalExperimentalApi::class, ExperimentalCommandDescriptors::class)
internal fun startupConsoleThread() {
    if (terminal is NoConsole) return

    MiraiConsole.launch(CoroutineName("Input Cancelling Daemon")) {
        while (isActive) {
            delay(2000)
        }
    }.invokeOnCompletion {
        runCatching<Unit> {
            // 应该仅关闭用户输入
            terminal.reader().shutdown()
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
                when (val result = ConsoleCommandSender.executeCommand(next)) {
                    is Success -> {
                    }
                    is IllegalArgument -> {
                        result.exception.message?.let { consoleLogger.warning(it) } ?: kotlin.run {
                            consoleLogger.warning(result.exception)
                        }
                    }
                    is ExecutionFailed -> {
                        consoleLogger.error(result.exception)
                    }
                    is UnresolvedCommand -> {
                        consoleLogger.warning { "未知指令: ${next}, 输入 ? 获取帮助" }
                    }
                    is PermissionDenied -> {
                        consoleLogger.warning { "权限不足." }
                    }
                    is UnmatchedSignature -> {
                        consoleLogger.warning { "参数不匹配: " + result.failureReasons.joinToString("\n") { it.render() } }
                    }
                    is Failure -> {
                        consoleLogger.warning { result.toString() }
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

@OptIn(ExperimentalCommandDescriptors::class)
internal fun UnmatchedCommandSignature.render(): String {
    return this.signature.toString() + "    ${failureReason.render()}"
}

@OptIn(ExperimentalCommandDescriptors::class)
internal fun FailureReason.render(): String {
    return when (this) {
        is FailureReason.InapplicableArgument -> "参数类型错误"
        is FailureReason.TooManyArguments -> "参数过多"
        is FailureReason.NotEnoughArguments -> "参数不足"
        is FailureReason.ResolutionAmbiguity -> "调用歧义"
        is FailureReason.ArgumentLengthMismatch -> {
            // should not happen, render it anyway.
            "参数长度不匹配"
        }
    }
}