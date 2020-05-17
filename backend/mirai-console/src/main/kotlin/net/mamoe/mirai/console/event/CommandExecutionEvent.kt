package net.mamoe.mirai.console.event

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent

data class CommandExecutionEvent(
    val sender: CommandSender,
    val command: Command,
    val rawArgs: Array<Any>
) : AbstractEvent(), CancellableEvent, ConsoleEvent {
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
