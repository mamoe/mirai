/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import java.util.concurrent.locks.ReentrantLock


internal infix fun Array<String>.matchesBeginning(list: List<Any>): Boolean {
    this.forEachIndexed { index, any ->
        if (list[index] != any) return false
    }
    return true
}

internal object InternalCommandManager {
    const val COMMAND_PREFIX = "/"

    @JvmField
    internal val registeredCommands: MutableList<Command> = mutableListOf()

    /**
     * 全部注册的指令
     * /mute -> MuteCommand
     * /jinyan -> MuteCommand
     */
    @JvmField
    internal val requiredPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    /**
     * Command name of commands that are prefix optional
     * mute -> MuteCommand
     */
    @JvmField
    internal val optionalPrefixCommandMap: MutableMap<String, Command> = mutableMapOf()

    @JvmField
    internal val modifyLock = ReentrantLock()


    /**
     * 从原始的 command 中解析出 Command 对象
     */
    internal fun matchCommand(rawCommand: String): Command? {
        if (rawCommand.startsWith(COMMAND_PREFIX)) {
            return requiredPrefixCommandMap[rawCommand.substringAfter(COMMAND_PREFIX)]
        }
        return optionalPrefixCommandMap[rawCommand]
    }
}

internal infix fun <T> Array<out T>.intersects(other: Array<out T>): Boolean {
    val max = this.size.coerceAtMost(other.size)
    for (i in 0 until max) {
        if (this[i] == other[i]) return true
    }
    return false
}