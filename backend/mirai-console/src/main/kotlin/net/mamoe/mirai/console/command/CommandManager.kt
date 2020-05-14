@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:JvmName("CommandManager")

package net.mamoe.mirai.console.command

import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import java.util.concurrent.locks.ReentrantLock

sealed class CommandOwner

abstract class PluginCommandOwner(plugin: PluginBase) : CommandOwner()

// 由前端实现
internal abstract class ConsoleCommandOwner : CommandOwner()

val CommandOwner.registeredCommands: List<Command> get() = InternalCommandManager.registeredCommands.filter { it.owner == this }

@get:JvmName("getCommandPrefix")
val CommandPrefix: String
    get() = InternalCommandManager._commandPrefix

fun CommandOwner.unregisterAllCommands() {
    for (registeredCommand in registeredCommands) {
        registeredCommand.unregister()
    }
}

/**
 * 注册一个指令. 若此指令已经注册或有已经注册的指令与 [SubCommandDescriptor] 重名, 返回 `false`
 */
fun Command.register(): Boolean = InternalCommandManager.modifyLock.withLock {
    if (findDuplicate() != null) {
        return false
    }
    InternalCommandManager.registeredCommands.add(this@register)
    return true
}

/**
 * 查找是否有重名的指令. 返回重名的指令.
 */
fun Command.findDuplicate(): Command? {
    return InternalCommandManager.registeredCommands.firstOrNull {
        it.descriptor.base.bakedSubNames intersects this.descriptor.base.bakedSubNames
    }
}

private infix fun <T> Array<T>.intersects(other: Array<T>): Boolean {
    val max = this.size.coerceAtMost(other.size)
    for (i in 0 until max) {
        if (this[i] == other[i]) return true
    }
    return false
}

/**
 * 取消注册这个指令. 若指令未注册, 返回 `false`
 */
fun Command.unregister(): Boolean = InternalCommandManager.modifyLock.withLock {
    return InternalCommandManager.registeredCommands.remove(this)
}

/**
 * 解析并执行一个指令
 * @param args 接受 [String] 或 [Message]
 * @return 是否成功解析到指令. 返回 `false` 代表无任何指令匹配
 */
@MiraiExperimentalAPI
suspend fun CommandSender.executeCommand(vararg args: Any): Boolean {
    val command = InternalCommandManager.matchCommand(args[0].toString()) ?: return false

    return args.flattenCommandComponents().executeCommand(this)
}

/**
 * 解析并执行一个指令
 * @return 是否成功解析到指令. 返回 `false` 代表无任何指令匹配
 */
suspend fun MessageChain.executeAsCommand(sender: CommandSender): Boolean {
    return this.flattenCommandComponents().executeCommand(sender)
}

/**
 * 检查指令参数并直接执行一个指令.
 */
suspend inline fun CommandSender.execute(command: CommandDescriptor.SubCommandDescriptor, args: CommandArgs): Boolean {
    command.checkArgs(args)
    return command.onCommand(this@execute, args)
}

/**
 * 检查指令参数并直接执行一个指令.
 */
suspend inline fun Command.execute(sender: CommandSender, args: CommandArgs): Boolean = sender.execute(this, args)

/**
 * 核心执行指令
 */
internal suspend fun List<Any>.executeCommand(origin: String, sender: CommandSender): Boolean {
    if (this.isEmpty()) return false
    val command = InternalCommandManager.matchCommand(origin) ?: return false
    TODO()
    /*
    command.descriptor.subCommands.forEach { sub ->
    }.run {
        sender.onDefault(
            CommandArgs.parseFrom(command, sender, this@executeCommand.drop(command.fullName.size))
        )
    }*/
}

internal infix fun Array<String>.matchesBeginning(list: List<Any>): Boolean {
    this.forEachIndexed { index, any ->
        if (list[index] != any) return false
    }
    return true
}

internal object InternalCommandManager {
    @JvmField
    internal val registeredCommands: MutableList<Command> = mutableListOf()

    @JvmField
    internal val modifyLock = ReentrantLock()

    internal var _commandPrefix: String = "/"

    internal fun matchCommand(name: String): Command? {
        return registeredCommands.firstOrNull { command ->
            command.descriptor.base.bakedSubNames.any {
                name.startsWith(it[0]) && (name.length <= it[0].length || name[it[0].length] == ' ') // 判断跟随空格
            }
        }
    }
}