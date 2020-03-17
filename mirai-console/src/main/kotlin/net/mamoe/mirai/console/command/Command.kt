/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.SimpleLogger.LogPriority

interface CommandSender {
    /**
     * 立刻发送一条Message
     */

    suspend fun sendMessage(messageChain: MessageChain)

    suspend fun sendMessage(message: String)

    /**
     * 写入要发送的内容 所有内容最后会被以一条发出
     */
    fun appendMessage(message: String)

    fun sendMessageBlocking(messageChain: MessageChain) = runBlocking { sendMessage(messageChain) }
    fun sendMessageBlocking(message: String) = runBlocking { sendMessage(message) }
}

abstract class CommandSenderImpl : CommandSender {
    internal val builder = StringBuilder()

    override fun appendMessage(message: String) {
        builder.append(message).append("\n")
    }

    internal open suspend fun flushMessage() {
        if (!builder.isEmpty()) {
            sendMessage(builder.toString().removeSuffix("\n"))
        }
    }
}

object ConsoleCommandSender : CommandSenderImpl() {
    override suspend fun sendMessage(messageChain: MessageChain) {
        MiraiConsole.logger("[Command]", 0, messageChain.toString())
    }

    override suspend fun sendMessage(message: String) {
        MiraiConsole.logger("[Command]", 0, message)
    }

    override suspend fun flushMessage() {
        super.flushMessage()
        builder.clear()
    }
}

open class ContactCommandSender(val contact: Contact) : CommandSenderImpl() {
    override suspend fun sendMessage(messageChain: MessageChain) {
        contact.sendMessage(messageChain)
    }

    override suspend fun sendMessage(message: String) {
        contact.sendMessage(message)
    }
}

interface Command {
    val name: String
    val alias: List<String>
    val description: String
    val usage: String

    suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean
}

inline fun Command.register() = CommandManager.register(this)


fun registerCommand(builder: CommandBuilder.() -> Unit): Command {
    return CommandBuilder().apply(builder).register()
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
     * 最高优先级监听器
     * 如果 return `false`, 这次指令不会被 [PluginBase] 的全局 onCommand 监听器监听
     * */
    final override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            onCommandBlocking(sender, args)
        }
    }

    abstract fun onCommandBlocking(sender: CommandSender, args: List<String>): Boolean
}

class AnonymousCommand internal constructor(
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

class CommandBuilder internal constructor() {
    var name: String? = null
    var alias: List<String>? = null
    var description: String = ""
    var usage: String = "use /help for help"
    internal var onCommand: (suspend CommandSender.(args: List<String>) -> Boolean)? = null

    fun onCommand(commandProcess: suspend CommandSender.(args: List<String>) -> Boolean) {
        onCommand = commandProcess
    }
}

private fun CommandBuilder.register(): AnonymousCommand {
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
    ).also { it.register() }
}