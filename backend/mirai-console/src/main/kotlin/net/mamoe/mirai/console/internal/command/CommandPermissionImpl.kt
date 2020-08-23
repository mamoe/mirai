package net.mamoe.mirai.console.internal.command

import net.mamoe.mirai.console.command.CommandPermission
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.hasPermission

internal class OrCommandPermissionImpl(
    private val first: CommandPermission,
    private val second: CommandPermission
) : CommandPermission {
    override fun CommandSender.hasPermission(): Boolean {
        return this.hasPermission(first) || this.hasPermission(second)
    }
}

internal class AndCommandPermissionImpl(
    private val first: CommandPermission,
    private val second: CommandPermission
) : CommandPermission {
    override fun CommandSender.hasPermission(): Boolean {
        return this.hasPermission(first) && this.hasPermission(second)
    }
}