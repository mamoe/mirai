/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.command

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.Command.Companion.allNames
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.findDuplicate
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCallParser.Companion.parseCommandCall
import net.mamoe.mirai.console.command.resolve.CommandCallResolver.Companion.resolve
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import java.util.concurrent.locks.ReentrantLock

@OptIn(ExperimentalCommandDescriptors::class)
internal object CommandManagerImpl : CommandManager, CoroutineScope by MiraiConsole.childScope("CommandManagerImpl") {
    private val logger: MiraiLogger by lazy {
        MiraiConsole.createLogger("command")
    }

    @Suppress("ObjectPropertyName")
    @JvmField
    internal val _registeredCommands: MutableList<Command> = mutableListOf()

    @JvmField
    internal val requiredPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val optionalPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val modifyLock = ReentrantLock()


    /**
     * 从原始的 command 中解析出 Command 对象
     */
    override fun matchCommand(commandName: String): Command? {
        if (commandName.startsWith(commandPrefix)) {
            return requiredPrefixCommandMap[commandName.substringAfter(commandPrefix).toLowerCase()]
        }
        return optionalPrefixCommandMap[commandName.toLowerCase()]
    }

    internal val commandListener: Listener<MessageEvent> by lazy {
        subscribeAlways(
            coroutineContext = CoroutineExceptionHandler { _, throwable ->
                logger.error(throwable)
            },
            concurrency = Listener.ConcurrencyKind.CONCURRENT,
            priority = Listener.EventPriority.HIGH
        ) {
            val sender = this.toCommandSender()

            when (val result = executeCommand(sender, message)) {
                is CommandExecuteResult.PermissionDenied -> {
                    if (!result.command.prefixOptional || message.content.startsWith(CommandManager.commandPrefix)) {
                        sender.sendMessage("权限不足")
                        intercept()
                    }
                }
                is CommandExecuteResult.IllegalArgument -> {
                    result.exception.message?.let { sender.sendMessage(it) }
                    intercept()
                }
                is CommandExecuteResult.Success -> {
                    intercept()
                }
                is CommandExecuteResult.ExecutionFailed -> {
                    sender.catchExecutionException(result.exception)
                    intercept()
                }
                is CommandExecuteResult.UnresolvedCall -> {
                    // noop
                }
            }
        }
    }


    ///// IMPL


    override fun getRegisteredCommands(owner: CommandOwner): List<Command> = _registeredCommands.filter { it.owner == owner }
    override val allRegisteredCommands: List<Command> get() = _registeredCommands.toList() // copy
    override val commandPrefix: String get() = CommandConfig.commandPrefix
    override fun unregisterAllCommands(owner: CommandOwner) {
        for (registeredCommand in getRegisteredCommands(owner)) {
            unregisterCommand(registeredCommand)
        }
    }

    override fun registerCommand(command: Command, override: Boolean): Boolean {
        if (command is CompositeCommand) {
            command.overloads  // init lazy
        }
        kotlin.runCatching {
            command.permission // init lazy
            command.secondaryNames // init lazy
            command.description // init lazy
            command.usage // init lazy
        }.onFailure {
            throw IllegalStateException("Failed to init command ${command}.", it)
        }

        this@CommandManagerImpl.modifyLock.withLock {
            if (!override) {
                if (command.findDuplicate() != null) return false
            }
            this@CommandManagerImpl._registeredCommands.add(command)
            if (command.prefixOptional) {
                for (name in command.allNames) {
                    val lowerCaseName = name.toLowerCase()
                    this@CommandManagerImpl.optionalPrefixCommandMap[lowerCaseName] = command
                    this@CommandManagerImpl.requiredPrefixCommandMap[lowerCaseName] = command
                }
            } else {
                for (name in command.allNames) {
                    val lowerCaseName = name.toLowerCase()
                    this@CommandManagerImpl.optionalPrefixCommandMap.remove(lowerCaseName) // ensure resolution consistency
                    this@CommandManagerImpl.requiredPrefixCommandMap[lowerCaseName] = command
                }
            }
            return true
        }
    }

    override fun findDuplicateCommand(command: Command): Command? =
        _registeredCommands.firstOrNull { it.allNames intersectsIgnoringCase command.allNames }

    override fun unregisterCommand(command: Command): Boolean = modifyLock.withLock {
        if (command.prefixOptional) {
            command.allNames.forEach {
                optionalPrefixCommandMap.remove(it.toLowerCase())
            }
        }
        command.allNames.forEach {
            requiredPrefixCommandMap.remove(it.toLowerCase())
        }
        _registeredCommands.remove(command)
    }

    override fun isCommandRegistered(command: Command): Boolean = command in _registeredCommands
}


// Don't move into CommandManager, compilation error / VerifyError
@OptIn(ExperimentalCommandDescriptors::class)
internal suspend fun executeCommandImpl(
    message: Message,
    caller: CommandSender,
    checkPermission: Boolean,
): CommandExecuteResult {
    val call = message.asMessageChain().parseCommandCall(caller) ?: return CommandExecuteResult.UnresolvedCall("")
    val resolved = call.resolve() ?: return CommandExecuteResult.UnresolvedCall(call.calleeName)

    val command = resolved.callee

    if (checkPermission && !command.permission.testPermission(caller)) {
        return CommandExecuteResult.PermissionDenied(command, call.calleeName)
    }

    return try {
        resolved.calleeSignature.call(resolved)
        CommandExecuteResult.Success(resolved.callee, call.calleeName, EmptyMessageChain)
    } catch (e: Throwable) {
        CommandExecuteResult.ExecutionFailed(e, resolved.callee, call.calleeName, EmptyMessageChain)
    }
}

