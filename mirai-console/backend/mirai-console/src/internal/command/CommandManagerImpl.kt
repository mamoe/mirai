/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.command

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation.ConsoleDataScope.Companion.get
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.Command.Companion.allNames
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.findDuplicate
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCallParser.Companion.parseCommandCall
import net.mamoe.mirai.console.command.resolve.CommandCallInterceptor.Companion.intercepted
import net.mamoe.mirai.console.command.resolve.CommandCallResolver.Companion.resolve
import net.mamoe.mirai.console.command.resolve.getOrElse
import net.mamoe.mirai.console.internal.data.builtins.DataScope
import net.mamoe.mirai.console.internal.util.ifNull
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.childScope
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCommandDescriptors::class)
internal class CommandManagerImpl(
    parentCoroutineContext: CoroutineContext
) : CommandManager, CoroutineScope by parentCoroutineContext.childScope("CommandManagerImpl") {
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
            return requiredPrefixCommandMap[commandName.substringAfter(commandPrefix).lowercase()]
        }
        return optionalPrefixCommandMap[commandName.lowercase()]
    }
    ///// IMPL


    override fun getRegisteredCommands(owner: CommandOwner): List<Command> =
        _registeredCommands.filter { it.owner == owner }

    override val allRegisteredCommands: List<Command> get() = _registeredCommands.toList() // copy
    override val commandPrefix: String get() = DataScope.get<CommandConfig>().commandPrefix
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

        modifyLock.withLock {
            if (!override) {
                if (command.findDuplicate() != null) return false
            }
            _registeredCommands.add(command)
            if (command.prefixOptional) {
                for (name in command.allNames) {
                    val lowerCaseName = name.lowercase()
                    optionalPrefixCommandMap[lowerCaseName] = command
                    requiredPrefixCommandMap[lowerCaseName] = command
                }
            } else {
                for (name in command.allNames) {
                    val lowerCaseName = name.lowercase()
                    optionalPrefixCommandMap.remove(lowerCaseName) // ensure resolution consistency
                    requiredPrefixCommandMap[lowerCaseName] = command
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
                optionalPrefixCommandMap.remove(it.lowercase())
            }
        }
        command.allNames.forEach {
            requiredPrefixCommandMap.remove(it.lowercase())
        }
        _registeredCommands.remove(command)
    }

    override fun isCommandRegistered(command: Command): Boolean = command in _registeredCommands
}


// Don't move into CommandManager, compilation error / VerifyError
@OptIn(ExperimentalCommandDescriptors::class)
internal suspend fun executeCommandImpl(
    message0: Message,
    caller: CommandSender,
    checkPermission: Boolean,
): CommandExecuteResult {
    val message = message0
        .intercepted(caller)
        .getOrElse { return CommandExecuteResult.Intercepted(null, null, null, it) }

    val call = message.toMessageChain()
        .parseCommandCall(caller)
        .ifNull { return CommandExecuteResult.UnresolvedCommand(null) }
        .let { raw ->
            raw.intercepted()
                .getOrElse { return CommandExecuteResult.Intercepted(raw, null, null, it) }
        }

    val resolved = call
        .resolve()
        .getOrElse { return it }
        .let { raw ->
            raw.intercepted()
                .getOrElse { return CommandExecuteResult.Intercepted(call, raw, null, it) }
        }

    val command = resolved.callee

    if (checkPermission && !command.permission.testPermission(caller)) {
        return CommandExecuteResult.PermissionDenied(command, call, resolved)
    }

    return try {
        resolved.calleeSignature.call(resolved)
        CommandExecuteResult.Success(resolved.callee, call, resolved)
    } catch (e: CommandArgumentParserException) {
        CommandExecuteResult.IllegalArgument(e, resolved.callee, call, resolved)
    } catch (e: Throwable) {
        CommandExecuteResult.ExecutionFailed(e, resolved.callee, call, resolved)
    }
}

