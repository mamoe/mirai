@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("CommandManager")

package net.mamoe.mirai.console.command

import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import java.util.*
import java.util.concurrent.locks.ReentrantLock

typealias CommandFullName = Array<out Any>

interface CommandOwner

val CommandOwner.registeredCommands: List<Command> get() = InternalCommandManager.registeredCommands.filter { it.owner == this }

fun CommandOwner.unregisterAllCommands() {
    for (registeredCommand in registeredCommands) {
        registeredCommand.unregister()
    }
}

/**
 * 注册一个指令. 若此指令已经注册或有已经注册的指令与 [allNames] 重名, 返回 `false`
 */
fun Command.register(): Boolean = InternalCommandManager.modifyLock.withLock {
    with(descriptor) {
        if (findDuplicate() != null) {
            return false
        }
        InternalCommandManager.registeredCommands.add(this@register)
        for (name in this.allNames) {
            InternalCommandManager.nameToCommandMap[name] = this@register
        }
        return true
    }
}

/**
 * 查找是否有重名的指令. 返回重名的指令.
 */
fun Command.findDuplicate(): Command? {
    return InternalCommandManager.nameToCommandMap.entries.firstOrNull { (names, _) ->
        this.allNames.any { it.contentEquals(names) }
    }?.value
}

/**
 * 取消注册这个指令. 若指令未注册, 返回 `false`
 */
fun Command.unregister(): Boolean = InternalCommandManager.modifyLock.withLock {
    if (!InternalCommandManager.registeredCommands.contains(this)) {
        return false
    }
    InternalCommandManager.registeredCommands.remove(this)
    for (name in this.allNames) {
        InternalCommandManager.nameToCommandMap.entries.removeIf {
            it.key.contentEquals(this.fullName)
        }
    }
    return true
}

/**
 * 解析并执行一个指令
 * @param args 接受 [String] 或 [Message]
 * @return 是否成功解析到指令. 返回 `false` 代表无任何指令匹配
 */
suspend fun CommandSender.executeCommand(vararg args: Any): Boolean {
    return args.flattenCommandComponents().toList().executeCommand(this)
}

/**
 * 解析并执行一个指令
 * @return 是否成功解析到指令. 返回 `false` 代表无任何指令匹配
 */
suspend fun MessageChain.executeAsCommand(sender: CommandSender): Boolean {
    return this.flattenCommandComponents().toList().executeCommand(sender)
}

suspend fun CommandSender.execute(command: Command, args: CommandArgs): Boolean = with(command) {
    checkArgs(args)
    return onCommand(this@execute, args)
}

suspend fun Command.execute(sender: CommandSender, args: CommandArgs): Boolean = sender.execute(this, args)
suspend fun Command.execute(sender: CommandSender, vararg args: Any): Boolean = sender.execute(this, args)
suspend fun CommandSender.execute(command: Command, vararg args: Any): Boolean = command.execute(this, args)


internal suspend fun List<Any>.executeCommand(sender: CommandSender): Boolean {
    val command = InternalCommandManager.matchCommand(this) ?: return false
    return command.onCommand(sender, CommandArgs.parseFrom(command, sender, this.drop(command.fullName.size)))
}

internal infix fun CommandFullName.matchesBeginning(list: List<Any>): Boolean {
    this.forEachIndexed { index, any ->
        if (list[index] != any) return false
    }
    return true
}

internal object InternalCommandManager {
    @JvmField
    internal val registeredCommands: MutableList<Command> = mutableListOf()

    @JvmField
    internal val nameToCommandMap: TreeMap<CommandFullName, Command> = TreeMap(Comparator.comparingInt { it.size })

    @JvmField
    internal val modifyLock = ReentrantLock()


    internal fun matchCommand(splitted: List<Any>): Command? {
        nameToCommandMap.entries.forEach {
            if (it.key matchesBeginning splitted) return it.value
        }
        return null
    }
}