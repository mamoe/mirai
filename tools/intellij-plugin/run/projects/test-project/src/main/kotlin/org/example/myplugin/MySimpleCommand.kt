package org.example.myplugin

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand

object MySimpleCommand000 : SimpleCommand(
    MyPluginMain, "foo",
    description = "示例指令"
) {
    @Handler
    suspend fun CommandSender.handle(int: Int, str: String) {

    }
}
