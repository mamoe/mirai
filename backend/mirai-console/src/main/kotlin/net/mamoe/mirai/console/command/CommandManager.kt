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
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 指令的所有者.
 * @see PluginCommandOwner
 */
sealed class CommandOwner

@MiraiInternalAPI
object TestCommandOwner : CommandOwner()

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
internal abstract class ConsoleCommandOwner : CommandOwner()

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
fun Command.register(override: Boolean = false): Boolean = InternalCommandManager.modifyLock.withLock {
    if (!override) {
        if (findDuplicate() != null) return false
    }
    InternalCommandManager.registeredCommands.add(this@register)
    if (this.prefixOptional) {
        for (name in this.names) {
            InternalCommandManager.optionalPrefixCommandMap[name] = this
        }
    } else {
        for (name in this.names) {
            InternalCommandManager.optionalPrefixCommandMap.remove(name) // ensure resolution consistency
            InternalCommandManager.requiredPrefixCommandMap[name] = this
        }
    }
    return true
}

/**
 * 查找并返回重名的指令. 返回重名指令.
 *
 * @see JCommandManager.findDuplicate Java 方法
 */
fun Command.findDuplicate(): Command? =
    InternalCommandManager.registeredCommands.firstOrNull { it.names intersects this.names }

/**
 * 取消注册这个指令. 若指令未注册, 返回 `false`.
 *
 * @see JCommandManager.unregister Java 方法
 */
fun Command.unregister(): Boolean = InternalCommandManager.modifyLock.withLock {
    InternalCommandManager.registeredCommands.remove(this)
}

//// executing without detailed result (faster)

/**
 * 解析并执行一个指令
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
    return executeCommandInternal(messages, messages[0].toString().substringBefore(' '))
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
    return executeCommandInternal(message, message[0].toString())
}


/**
 * 在 [executeCommand] 中, [Command.onCommand] 抛出异常时包装的异常.
 */
class CommandExecutionException(
    /**
     * 执行过程发生异常的指令
     */
    val command: Command,
    /**
     * 匹配到的指令名
     */
    val name: String,
    /**
     * 基础分割后的实际参数列表, 元素类型可能为 [Message] 或 [String]
     */
    val args: Array<out Any>,
    cause: Throwable
) : RuntimeException(
    "Exception while executing command '${command.primaryName}' with args ${args.joinToString { "'$it'" }}",
    cause
) {
    override fun toString(): String =
        "CommandExecutionException(command=$command, name='$name', args=${args.contentToString()})"
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
