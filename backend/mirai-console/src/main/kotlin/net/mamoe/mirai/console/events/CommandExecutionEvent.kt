/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.events

/*
data class CommandExecutionEvent( // TODO: 2020/6/26  impl CommandExecutionEvent
    val sender: CommandSender,
    val command: Command,
    val rawArgs: Array<Any>
) : CancellableEvent, ConsoleEvent, AbstractEvent() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandExecutionEvent

        if (sender != other.sender) return false
        if (command != other.command) return false
        if (!rawArgs.contentEquals(other.rawArgs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + rawArgs.contentHashCode()
        return result
    }
}
*/