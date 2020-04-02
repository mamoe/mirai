/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.plugins.PluginBase

/**
 * 指令
 *
 * @see register 注册这个指令
 * @see registerCommand 注册指令 DSL
 */
interface Command {
    /**
     * 指令主名称
     */
    val name: String

    /**
     * 别名
     */
    val alias: List<String>

    /**
     * 描述, 将会显示在 "/help" 指令中
     */
    val description: String

    /**
     * 用法说明
     */
    val usage: String

    suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean
}

/**
 * 注册这个指令
 */
inline fun Command.register(commandOwner: CommandOwner) = CommandManager.register(commandOwner,this)

internal inline fun registerConsoleCommands(builder: CommandBuilder.() -> Unit):Command{
    return CommandBuilder().apply(builder).register(ConsoleCommandOwner)
}

inline fun PluginBase.registerCommand(builder: CommandBuilder.() -> Unit):Command{
    return CommandBuilder().apply(builder).register(this.asCommandOwner())
}


// for java
@Suppress("unused")
abstract class BlockingCommand(
    override val name: String,
    override val alias: List<String> = listOf(),
    override val description: String = "",
    override val usage: String = ""
) : Command {
    /**
     * 最高优先级监听器.
     *
     * 指令调用将优先触发 [Command.onCommand], 若该函数返回 `false`, 则不会调用 [PluginBase.onCommand]
     * */
    final override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            onCommandBlocking(sender, args)
        }
    }

    abstract fun onCommandBlocking(sender: CommandSender, args: List<String>): Boolean
}

/**
 * @see registerCommand
 */
class CommandBuilder @PublishedApi internal constructor() {
    var name: String? = null
    var alias: List<String>? = null
    var description: String = ""
    var usage: String = "use /help for help"

    internal var onCommand: (suspend CommandSender.(args: List<String>) -> Boolean)? = null

    fun onCommand(commandProcess: suspend CommandSender.(args: List<String>) -> Boolean) {
        onCommand = commandProcess
    }
}


// internal


internal class AnonymousCommand internal constructor(
    override val name: String,
    override val alias: List<String>,
    override val description: String,
    override val usage: String = "",
    val onCommand: suspend CommandSender.(args: List<String>) -> Boolean
) : Command {
    override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
        return onCommand.invoke(sender, args)
    }
}

@PublishedApi
internal fun CommandBuilder.register(commandOwner: CommandOwner): AnonymousCommand {
    if (name == null || onCommand == null) {
        error("CommandBuilder not complete")
    }
    if (alias == null) {
        alias = listOf()
    }
    return AnonymousCommand(
        name!!,
        alias!!,
        description,
        usage,
        onCommand!!
    ).also { it.register(commandOwner) }
}