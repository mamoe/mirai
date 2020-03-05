/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.plugins.PluginManager

object CommandManager {
    private val registeredCommand: MutableMap<String, Command> = mutableMapOf()

    val commands: Collection<Command> get() = registeredCommand.values

    fun register(command: Command) {
        val allNames = mutableListOf(command.name).also { it.addAll(command.alias) }
        allNames.forEach {
            if (registeredCommand.containsKey(it)) {
                error("Command Name(or Alias) $it is already registered, consider if same functional plugin was installed")
            }
        }
        allNames.forEach {
            registeredCommand[it] = command
        }
    }

    fun unregister(command: Command) {
        (command.alias.asSequence() + command.name).forEach {
            registeredCommand.remove(it)
        } // label compilation failed
    }

    fun unregister(commandName: String): Boolean {
        return registeredCommand.remove(commandName) != null
    }

    /*
    * Index: MiraiConsole
    */
    internal suspend fun runCommand(sender: CommandSender, fullCommand: String): Boolean {
        val blocks = fullCommand.split(" ")
        val commandHead = blocks[0].replace("/", "")
        if (!registeredCommand.containsKey(commandHead)) {
            return false
        }
        val args = blocks.drop(1)
        registeredCommand[commandHead]?.run {
            try {
                if (onCommand(sender, blocks.drop(1))) {
                    PluginManager.onCommand(this, sender, args)
                } else {
                    sender.sendMessage(this.usage)
                }
            } catch (e: Exception) {
                sender.sendMessage("在运行指令时出现了未知错误")
                e.printStackTrace()
            } finally {
                (sender as CommandSenderImpl).flushMessage()
            }
        }
        return true
    }

}