/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "NOTHING_TO_INLINE", "unused", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE",
    "MemberVisibilityCanBePrivate"
)
@file:JvmName("CommandManagerKt")

package net.mamoe.mirai.console.command

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.MiraiConsoleInternal
import net.mamoe.mirai.console.command.internal.*
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain

/**
 * 指令的所有者.
 * @see PluginCommandOwner
 */
sealed class CommandOwner

/**
 * 插件指令所有者. 插件只能通过 [PluginCommandOwner] 管理指令.
 */
abstract class PluginCommandOwner(val plugin: Plugin) : CommandOwner() {
    init {
        if (plugin is CoroutineScope) { // JVM Plugin
            plugin.coroutineContext[Job]?.invokeOnCompletion {
                this.unregisterAllCommands()
            }
        }
    }
}

/**
 * 代表控制台所有者. 所有的 mirai-console 内建的指令都属于 [ConsoleCommandOwner].
 *
 * 由前端实现
 */
internal abstract class ConsoleCommandOwner : CommandOwner() {
    companion object {
        internal val instance get() = MiraiConsoleInternal.consoleCommandOwner
    }
}

/**
 * 获取已经注册了的属于这个 [CommandOwner] 的指令列表.
 * @see JCommandManager.getRegisteredCommands Java 方法
 */
val CommandOwner.registeredCommands: List<Command> get() = InternalCommandManager.registeredCommands.filter { it.owner == this }

/**
 * 指令前缀, 如 '/'
 * @see JCommandManager.getCommandPrefix Java 方法
 */
@get:JvmName("getCommandPrefix")
val CommandPrefix: String
    get() = InternalCommandManager.COMMAND_PREFIX

/**
 * 取消注册所有属于 [this] 的指令
 * @see JCommandManager.unregisterAllCommands Java 方法
 */
fun CommandOwner.unregisterAllCommands() {
    for (registeredCommand in registeredCommands) {
        registeredCommand.unregister()
    }
}

/**
 * 注册一个指令.
 *
 * @param override 是否覆盖重名指令.
 *
 * 若原有指令 P, 其 [Command.names] 为 'a', 'b', 'c'.
 * 新指令 Q, 其 [Command.names] 为 'b', 将会覆盖原指令 A 注册的 'b'.
 *
 * 即注册完成后, 'a' 和 'c' 将会解析到指令 P, 而 'b' 会解析到指令 Q.
 *
 * @return
 * 若已有重名指令, 且 [override] 为 `false`, 返回 `false`;
 * 若已有重名指令, 但 [override] 为 `true`, 覆盖原有指令并返回 `true`.
 *
 * @see JCommandManager.register Java 方法
 */
@JvmOverloads
fun Command.register(override: Boolean = false): Boolean {
    if (this is CompositeCommand) this.subCommands // init

    InternalCommandManager.modifyLock.withLock {
        if (!override) {
            if (findDuplicate() != null) return false
        }
        InternalCommandManager.registeredCommands.add(this@register)
        if (this.prefixOptional) {
            for (name in this.names) {
                val lowerCaseName = name.toLowerCase()
                InternalCommandManager.optionalPrefixCommandMap[lowerCaseName] = this
                InternalCommandManager.requiredPrefixCommandMap[lowerCaseName] = this
            }
        } else {
            for (name in this.names) {
                val lowerCaseName = name.toLowerCase()
                InternalCommandManager.optionalPrefixCommandMap.remove(lowerCaseName) // ensure resolution consistency
                InternalCommandManager.requiredPrefixCommandMap[lowerCaseName] = this
            }
        }
        return true
    }
}

/**
 * 查找并返回重名的指令. 返回重名指令.
 *
 * @see JCommandManager.findDuplicate Java 方法
 */
fun Command.findDuplicate(): Command? =
    InternalCommandManager.registeredCommands.firstOrNull { it.names intersectsIgnoringCase this.names }

/**
 * 取消注册这个指令. 若指令未注册, 返回 `false`.
 *
 * @see JCommandManager.unregister Java 方法
 */
fun Command.unregister(): Boolean = InternalCommandManager.modifyLock.withLock {
    if (this.prefixOptional) {
        this.names.forEach {
            InternalCommandManager.optionalPrefixCommandMap.remove(it)
        }
    }
    this.names.forEach {
        InternalCommandManager.requiredPrefixCommandMap.remove(it)
    }
    InternalCommandManager.registeredCommands.remove(this)
}

/**
 * 当 [this] 已经 [注册][register] 后返回 `true`
 */
fun Command.isRegistered(): Boolean = this in InternalCommandManager.registeredCommands

//// executing without detailed result (faster)

/**
 * 解析并执行一个指令. 将会检查指令权限, 在无权限时抛出
 *
 * @param messages 接受 [String] 或 [Message], 其他对象将会被 [Any.toString]
 *
 * @return 成功执行的指令, 在无匹配指令时返回 `null`
 * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
 *
 * @see JCommandManager.executeCommand Java 方法
 */
suspend fun CommandSender.executeCommand(vararg messages: Any): Command? {
    if (messages.isEmpty()) return null
    return matchAndExecuteCommandInternal(messages, messages[0].toString().substringBefore(' '))
}

/**
 * 解析并执行一个指令
 *
 * @return 成功执行的指令, 在无匹配指令时返回 `null`
 * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
 *
 * @see JCommandManager.executeCommand Java 方法
 */
@Throws(CommandExecutionException::class)
suspend fun CommandSender.executeCommand(message: MessageChain): Command? {
    if (message.isEmpty()) return null
    return matchAndExecuteCommandInternal(message, message[0].toString().substringBefore(' '))
}

/**
 * 执行一个指令
 *
 * @return 成功执行的指令, 在无匹配指令时返回 `null`
 * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
 *
 * @see JCommandManager.executeCommand Java 方法
 */
@JvmOverloads
@Throws(CommandExecutionException::class)
suspend fun Command.execute(sender: CommandSender, args: MessageChain, checkPermission: Boolean = true) {
    sender.executeCommandInternal(
        this,
        args.flattenCommandComponents().toTypedArray(),
        this.primaryName,
        checkPermission
    )
}

/**
 * 执行一个指令
 *
 * @return 成功执行的指令, 在无匹配指令时返回 `null`
 * @throws CommandExecutionException 当 [Command.onCommand] 抛出异常时包装并附带相关指令信息抛出
 *
 * @see JCommandManager.executeCommand Java 方法
 */
@JvmOverloads
@Throws(CommandExecutionException::class)
suspend fun Command.execute(sender: CommandSender, vararg args: Any, checkPermission: Boolean = true) {
    sender.executeCommandInternal(
        this,
        args.flattenCommandComponents().toTypedArray(),
        this.primaryName,
        checkPermission
    )
}

//// execution with detailed result

/**
 * 解析并执行一个指令, 获取详细的指令参数等信息
 *
 * @param messages 接受 [String] 或 [Message], 其他对象将会被 [Any.toString]
 *
 * @return 执行结果
 *
 * @see JCommandManager.executeCommandDetailed Java 方法
 */
suspend fun CommandSender.executeCommandDetailed(vararg messages: Any): CommandExecuteResult {
    if (messages.isEmpty()) return CommandExecuteResult.CommandNotFound("")
    return executeCommandDetailedInternal(messages, messages[0].toString().substringBefore(' '))
}

/**
 * 解析并执行一个指令, 获取详细的指令参数等信息
 *
 * 执行过程中产生的异常将不会直接抛出, 而会包装为 [CommandExecuteResult.ExecutionException]
 *
 * @return 执行结果
 *
 * @see JCommandManager.executeCommandDetailed Java 方法
 */
suspend fun CommandSender.executeCommandDetailed(messages: MessageChain): CommandExecuteResult {
    if (messages.isEmpty()) return CommandExecuteResult.CommandNotFound("")
    return executeCommandDetailedInternal(messages, messages[0].toString())
}

@JvmSynthetic
internal suspend inline fun CommandSender.executeCommandDetailedInternal(
    messages: Any,
    commandName: String
): CommandExecuteResult {
    val command =
        InternalCommandManager.matchCommand(commandName) ?: return CommandExecuteResult.CommandNotFound(commandName)
    val args = messages.flattenCommandComponents().dropToTypedArray(1)

    if (!command.testPermission(this)) {
        return CommandExecuteResult.PermissionDenied(command, commandName)
    }
    kotlin.runCatching {
        command.onCommand(this, args)
    }.fold(
        onSuccess = {
            return CommandExecuteResult.Success(
                commandName = commandName,
                command = command,
                args = args
            )
        },
        onFailure = {
            return CommandExecuteResult.ExecutionException(
                commandName = commandName,
                command = command,
                exception = it,
                args = args
            )
        }
    )
}
