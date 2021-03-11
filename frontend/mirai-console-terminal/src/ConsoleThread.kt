/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(ExperimentalCommandDescriptors::class)

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandExecuteResult.*
import net.mamoe.mirai.console.command.descriptor.AbstractCommandValueParameter.StringConstant
import net.mamoe.mirai.console.command.descriptor.CommandReceiverParameter
import net.mamoe.mirai.console.command.descriptor.CommandValueParameter
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.terminal.noconsole.NoConsole
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.console.util.requestInput
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.warning
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

val consoleLogger by lazy { MiraiLogger.create("console") }

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
            val next = try {
                MiraiConsole.requestInput("").let {
                    when {
                        it.isBlank() -> it
                        it.startsWith(CommandManager.commandPrefix) -> it
                        it == "?" -> CommandManager.commandPrefix + BuiltInCommands.HelpCommand.primaryName
                        else -> CommandManager.commandPrefix + it
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
                consoleLogger.error("Error in reading next command", e)
                consoleLogger.warning("Closing input service...")
                return@launch
            }
            if (next.isBlank()) {
                continue
            }
            try {
                // consoleLogger.debug("INPUT> $next")
                when (val result = ConsoleCommandSender.executeCommand(next)) {
                    is Success -> {
                    }
                    is IllegalArgument -> { // user wouldn't want stacktrace for a parser error unless it is in debugging mode (to do).
                        val message = result.exception.message
                        if (message != null) {
                            consoleLogger.warning(message)
                        } else consoleLogger.warning(result.exception)
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
                        consoleLogger.warning { "参数不匹配, 你是否想执行: \n" + result.failureReasons.render(result.command, result.call) }
                    }
                    is Failure -> {
                        consoleLogger.warning { result.toString() }
                    }
                }
            } catch (e: InterruptedException) {
                return@launch
            } catch (e: CancellationException) {
                return@launch
            } catch (e: Throwable) {
                consoleLogger.error("Unhandled exception", e)
            }
        }
    }
}

@OptIn(ExperimentalCommandDescriptors::class)
private fun List<UnmatchedCommandSignature>.render(command: Command, call: CommandCall): String {
    val list =
        this.filter lambda@{ signature ->
            if (signature.failureReason.safeCast<FailureReason.InapplicableValueArgument>()?.parameter is StringConstant) return@lambda false
            if (signature.signature.valueParameters.anyStringConstantUnmatched(call.valueArguments)) return@lambda false
            true
        }
    if (list.isEmpty()) {
        return command.usage
    }
    return list.joinToString("\n") { it.render(command) }
}

private fun List<CommandValueParameter<*>>.anyStringConstantUnmatched(arguments: List<CommandValueArgument>): Boolean {
    return this.zip(arguments).any { (parameter, argument) ->
        parameter is StringConstant && !parameter.accepts(argument, null)
    }
}

@OptIn(ExperimentalCommandDescriptors::class)
internal fun UnmatchedCommandSignature.render(command: Command): String {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    val usage = net.mamoe.mirai.console.internal.command.CommandReflector.generateUsage(command, null, listOf(this.signature))
    return usage.trim() + "    (${failureReason.render()})"
}

@OptIn(ExperimentalCommandDescriptors::class)
internal fun FailureReason.render(): String {
    return when (this) {
        is FailureReason.InapplicableReceiverArgument -> "需要由 ${this.parameter.renderAsName()} 执行"
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

@OptIn(ExperimentalCommandDescriptors::class)
internal fun CommandReceiverParameter<*>.renderAsName(): String {
    val classifier = this.type.classifier.cast<KClass<out CommandSender>>()
    return when {
        classifier.isSubclassOf(ConsoleCommandSender::class) -> "控制台"
        classifier.isSubclassOf(FriendCommandSenderOnMessage::class) -> "好友私聊"
        classifier.isSubclassOf(FriendCommandSender::class) -> "好友"
        classifier.isSubclassOf(MemberCommandSenderOnMessage::class) -> "群内发言"
        classifier.isSubclassOf(MemberCommandSender::class) -> "群成员"
        classifier.isSubclassOf(GroupTempCommandSenderOnMessage::class) -> "群临时会话"
        classifier.isSubclassOf(GroupTempCommandSender::class) -> "群临时好友"
        classifier.isSubclassOf(UserCommandSender::class) -> "用户"
        else -> classifier.simpleName ?: classifier.toString()
    }
}