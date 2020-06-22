/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:JvmName("CommandManagerKt")

package net.mamoe.mirai.console.command

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage
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

//// executing

/**
 * 解析并执行一个指令
 *
 * Java 调用方式: `<static> CommandManager.executeCommand(Command)`
 *
 * @param messages 接受 [String] 或 [Message], 其他对象将会被 [Any.toString]
 * @see CommandExecuteResult
 *
 * @see JCommandManager.executeCommand Java 方法
 */
suspend fun CommandSender.executeCommand(vararg messages: Any): CommandExecuteResult {
    if (messages.isEmpty()) return CommandExecuteResult(
        status = CommandExecuteStatus.EMPTY_COMMAND
    )
    return executeCommandInternal(
        messages,
        messages[0].let { if (it is SingleMessage) it.toString() else it.toString().substringBefore(' ') })
}

@JvmSynthetic
internal inline fun <reified T> List<T>.dropToTypedArray(n: Int): Array<T> = Array(size - n) { this[n + it] }

/**
 * 解析并执行一个指令
 * @see CommandExecuteResult
 *
 * @see JCommandManager.executeCommand Java 方法
 */
suspend fun CommandSender.executeCommand(message: MessageChain): CommandExecuteResult {
    if (message.isEmpty()) return CommandExecuteResult(
        status = CommandExecuteStatus.EMPTY_COMMAND
    )
    return executeCommandInternal(message, message[0].toString())
}

@JvmSynthetic
internal suspend inline fun CommandSender.executeCommandInternal(
    messages: Any,
    commandName: String
): CommandExecuteResult {
    val command = InternalCommandManager.matchCommand(commandName) ?: return CommandExecuteResult(
        status = CommandExecuteStatus.COMMAND_NOT_FOUND,
        commandName = commandName
    )
    val rawInput = messages.flattenCommandComponents()
    kotlin.runCatching {
        command.onCommand(this, rawInput.dropToTypedArray(1))
    }.onFailure {
        return CommandExecuteResult(
            status = CommandExecuteStatus.FAILED,
            commandName = commandName,
            command = command,
            exception = it
        )
    }
    return CommandExecuteResult(
        status = CommandExecuteStatus.SUCCESSFUL,
        commandName = commandName,
        command = command
    )
}

/**
 * 命令的执行返回
 *
 * @param status 命令最终执行状态
 * @param exception 命令执行时发生的错误(如果有)
 * @param command 尝试执行的命令 (status = SUCCESSFUL | FAILED)
 * @param commandName 尝试执行的命令的名字 (status != EMPTY_COMMAND)
 *
 *
 * @see CommandExecuteStatus
 */
class CommandExecuteResult(
    val status: CommandExecuteStatus,
    val exception: Throwable? = null,
    val command: Command? = null,
    val commandName: String? = null
) {
    /**
     * 命令的执行状态
     *
     * 当为 [SUCCESSFUL] 的时候，代表命令执行成功
     *
     * 当为 [FAILED] 的时候, 代表命令执行出现了错误
     *
     * 当为 [COMMAND_NOT_FOUND] 的时候，代表没有匹配的命令
     *
     * 当为 [EMPTY_COMMAND] 的时候, 代表尝试执行 ""
     *
     */
    enum class CommandExecuteStatus {
        SUCCESSFUL, FAILED, COMMAND_NOT_FOUND, EMPTY_COMMAND
    }

}

@Suppress("RemoveRedundantQualifierName")
typealias CommandExecuteStatus = CommandExecuteResult.CommandExecuteStatus
