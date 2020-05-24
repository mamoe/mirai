/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:JvmName("CommandManager")

package net.mamoe.mirai.console.command

import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.console.plugins.Plugin
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage

sealed class CommandOwner

object TestCommandOwner : CommandOwner()

abstract class PluginCommandOwner(plugin: Plugin) : CommandOwner()

// 由前端实现
internal abstract class ConsoleCommandOwner : CommandOwner()

/**
 * 获取已经注册了的指令列表
 */
val CommandOwner.registeredCommands: List<Command> get() = InternalCommandManager.registeredCommands.filter { it.owner == this }

@get:JvmName("getCommandPrefix")
val CommandPrefix: String
    get() = InternalCommandManager.COMMAND_PREFIX

fun CommandOwner.unregisterAllCommands() {
    for (registeredCommand in registeredCommands) {
        registeredCommand.unregister()
    }
}

/**
 * 注册一个指令. 若此指令已经注册或有已经注册的指令与 [SubCommandDescriptor] 重名, 返回 `false`
 */
fun Command.register(): Boolean = InternalCommandManager.modifyLock.withLock {
    if (findDuplicate() != null) return false
    InternalCommandManager.registeredCommands.add(this@register)
    if (this.prefixOptional) {
        for (name in this.names) {
            InternalCommandManager.optionalPrefixCommandMap[name] = this
        }
    } else {
        for (name in this.names) {
            InternalCommandManager.requiredPrefixCommandMap[name] = this
        }
    }
    return true
}

/**
 * 查找是否有重名的指令. 返回重名的指令.
 */
fun Command.findDuplicate(): Command? =
    InternalCommandManager.registeredCommands.firstOrNull { it.names intersects this.names }

/**
 * 取消注册这个指令. 若指令未注册, 返回 `false`
 */
fun Command.unregister(): Boolean = InternalCommandManager.modifyLock.withLock {
    InternalCommandManager.registeredCommands.remove(this)
}

//// executing

/**
 * 解析并执行一个指令
 *
 * @param messages 接受 [String] 或 [Message], 其他对象将会被 [Any.toString]
 * @return 是否成功解析到指令. 返回 `false` 代表无任何指令匹配
 */
suspend fun CommandSender.executeCommand(vararg messages: Any): Boolean {
    if (messages.isEmpty()) return false
    return executeCommandInternal(
        messages,
        messages[0].let { if (it is SingleMessage) it.toString() else it.toString().substringBefore(' ') })
}

internal inline fun <reified T> List<T>.dropToTypedArray(n: Int): Array<T> = Array(size - n) { this[n + it] }

/**
 * 解析并执行一个指令
 * @return 是否成功解析到指令. 返回 `false` 代表无任何指令匹配
 */
suspend fun CommandSender.executeCommand(message: MessageChain): Boolean {
    if (message.isEmpty()) return false
    return executeCommandInternal(message, message[0].toString())
}

internal suspend inline fun CommandSender.executeCommandInternal(
    messages: Any,
    commandName: String
): Boolean {
    val command = InternalCommandManager.matchCommand(commandName) ?: return false
    val rawInput = messages.flattenCommandComponents()
    command.onCommand(this, rawInput.dropToTypedArray(1))
    return true
}