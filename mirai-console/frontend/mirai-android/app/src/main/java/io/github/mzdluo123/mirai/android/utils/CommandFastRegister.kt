package io.github.mzdluo123.mirai.android.utils

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.CommandSender

fun CommandOwner.register(
    _alias: List<String> = listOf(),
    _description: String,
    _name: String,
    _usage: String = "/$_name",
    _block :suspend (CommandSender, List<String>) -> Boolean
){
    CommandManager.register(this, object : Command {
        override val alias: List<String>
            get() = _alias
        override val description: String
            get() = _description
        override  val name: String
            get() = _name
        override val usage: String
            get() = _usage
        override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
            return _block(sender,args)
        }
    })
}