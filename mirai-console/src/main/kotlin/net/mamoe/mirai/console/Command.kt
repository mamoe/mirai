/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.data.MessageChain

object CommandManager {
    private val registeredCommand: MutableMap<String, Command> = mutableMapOf()

    fun getCommands(): Collection<Command> {
        return registeredCommand.values
    }


    fun register(command: Command) {
        val allNames = mutableListOf(command.name).also { it.addAll(command.alias) }
        allNames.forEach {
            if (registeredCommand.containsKey(it)) {
                error("net.mamoe.mirai.Command Name(or Alias) $it is already registered, consider if same function plugin was installed")
            }
        }
        allNames.forEach {
            registeredCommand[it] = command
        }
    }

    fun unregister(command: Command) {
        val allNames = mutableListOf<String>(command.name).also { it.addAll(command.alias) }
        allNames.forEach {
            registeredCommand.remove(it)
        }
    }

    fun unregister(commandName: String) {
        registeredCommand.remove(commandName)
    }

    suspend fun runCommand(sender: CommandSender, fullCommand: String): Boolean {
        val blocks = fullCommand.split(" ")
        val commandHead = blocks[0].replace("/", "")
        if (!registeredCommand.containsKey(commandHead)) {
            return false
        }
        val args = blocks.subList(1, blocks.size)
        registeredCommand[commandHead]?.run {
            if (onCommand(
                    sender,
                    blocks.subList(1, blocks.size)
                )
            ) {
                PluginManager.onCommand(this, args)
            }
        }
        return true
    }

}

interface CommandSender {
    suspend fun sendMessage(messageChain: MessageChain)

    suspend fun sendMessage(message: String)

    fun sendMessageBlocking(messageChain: MessageChain) = runBlocking { sendMessage(messageChain) }
    fun sendMessageBlocking(message: String) = runBlocking { sendMessage(message) }
}

object ConsoleCommandSender : CommandSender {
    override suspend fun sendMessage(messageChain: MessageChain) {
        MiraiConsole.logger(messageChain.toString())
    }

    override suspend fun sendMessage(message: String) {
        MiraiConsole.logger(message)
    }
}

class ContactCommandSender(val contact: Contact) : CommandSender {
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
    suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean
    fun register()
}

abstract class BlockingCommand(
    override val name: String,
    override val alias: List<String> = listOf(),
    override val description: String = ""
) : Command {
    /**
     * 最高优先级监听器
     * 如果 return `false` 这次指令不会被 [PluginBase] 的全局 onCommand 监听器监听
     * */
    final override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            onCommandBlocking(sender, args)
        }
    }

    abstract fun onCommandBlocking(sender: CommandSender, args: List<String>): Boolean

    override fun register() {
        CommandManager.register(this)
    }
}

class AnonymousCommand internal constructor(
    override val name: String,
    override val alias: List<String>,
    override val description: String,
    val onCommand: suspend CommandSender.(args: List<String>) -> Boolean
) : Command {
    override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
        return onCommand.invoke(sender, args)
    }

    override fun register() {
        CommandManager.register(this)
    }
}

class CommandBuilder internal constructor() {
    var name: String? = null
    var alias: List<String>? = null
    var description: String = ""
    var onCommand: (suspend CommandSender.(args: List<String>) -> Boolean)? = null

    fun onCommand(commandProcess: suspend CommandSender.(args: List<String>) -> Boolean) {
        onCommand = commandProcess
    }

    fun register(): Command {
        if (name == null || onCommand == null) {
            error("net.mamoe.mirai.CommandBuilder not complete")
        }
        if (alias == null) {
            alias = listOf()
        }
        return AnonymousCommand(name!!, alias!!, description, onCommand!!).also { it.register() }
    }
}

fun registerCommand(builder: CommandBuilder.() -> Unit): Command {
    return CommandBuilder().apply(builder).register()
}

