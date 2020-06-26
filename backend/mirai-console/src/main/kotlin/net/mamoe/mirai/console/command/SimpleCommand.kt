package net.mamoe.mirai.console.command

abstract class SimpleCommand(
    override val owner: CommandOwner,
    override vararg val names: String,
    override val usage: String,
    override val description: String,
    override val permission: CommandPermission = CommandPermission.Default,
    override val prefixOptional: Boolean = false
) : Command {
    abstract override suspend fun CommandSender.onCommand(args: Array<out Any>)
}
