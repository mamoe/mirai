/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.plugin

object CommandManager {
    private val registeredCommand: MutableMap<String, Command> = mutableMapOf()


    fun register(command: Command) {
        val allNames = mutableListOf<String>(command.name).also { it.addAll(command.alias) }
        allNames.forEach {
            if (registeredCommand.containsKey(it)) {
                error("Command Name(or Alias) $it is already registered, consider if same function plugin was installed")
            }
        }
        allNames.forEach {
            registeredCommand[it] = command
        }
    }

    fun runCommand(fullCommand: String): Boolean {
        val blocks = fullCommand.split(" ")
        val commandHead = blocks[0].replace("/", "")
        if (!registeredCommand.containsKey(commandHead)) {
            return false
        }
        val args = blocks.subList(1, blocks.size)
        registeredCommand[commandHead]?.run {
            if (onCommand(
                    blocks.subList(1, blocks.size)
                )
            ) {
                PluginManager.onCommand(this, args)
            }
        }
        return true
    }


}

abstract class Command(
    val name: String,
    val alias: List<String> = listOf()
) {
    /**
     * 最高优先级监听器
     * 如果return [false] 这次指令不会被[PluginBase]的全局onCommand监听器监听
     * */
    open fun onCommand(args: List<String>): Boolean {
        return true
    }
}
