package net.mamoe.mirai.console.command

abstract class RawCommand(
    override val owner: CommandOwner,
    override vararg val names: String,
    override val usage: String = "<no usages given>",
    override val description: String = "<no descriptions given>",
    override val permission: CommandPermission = CommandPermission.Default,
    override val prefixOptional: Boolean = false
) : Command {
    abstract override suspend fun CommandSender.onCommand(args: Array<out Any>)
}
