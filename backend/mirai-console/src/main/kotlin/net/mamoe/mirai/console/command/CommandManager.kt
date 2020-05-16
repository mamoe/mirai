@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:JvmName("CommandManager")

package net.mamoe.mirai.console.command

import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain

sealed class CommandOwner

abstract class PluginCommandOwner(plugin: PluginBase) : CommandOwner()

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
suspend fun CommandSender.executeCommand(vararg messages: Any): Boolean =
    executeCommandInternal(messages) { messages.getOrNull(it) }

internal inline fun <reified T> List<T>.dropToTypedArray(n: Int): Array<T> = Array(size - n) { this[n + it] }

/**
 * 解析并执行一个指令
 * @return 是否成功解析到指令. 返回 `false` 代表无任何指令匹配
 */
suspend fun CommandSender.executeCommand(message: MessageChain): Boolean =
    executeCommandInternal(message) { message.getOrNull(it) }

internal suspend inline fun CommandSender.executeCommandInternal(
    messages: Any,
    iterator: (index: Int) -> Any?
): Boolean {
    val command = InternalCommandManager.matchCommand(getCommandName(iterator)) ?: return false
    val rawInput = messages.flattenCommandComponents()
    command.onCommand(this, rawInput.dropToTypedArray(1))
    return true
}