package org.example.myplugin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object MySimpleCommand000 : SimpleCommand(
    MyPluginMain, "foo",
    description = "示例指令"
) {
    @Handler
    suspend fun CommandSender.handle(int: Int, str: String) {

    }
}

object DataTest : AutoSavePluginConfig("data") {
    val pp by value(NoDefaultValue(1))
}

@Serializable
data class HasDefaultValue(
    val x: Int = 0,
)

@Serializable
data class NoDefaultValue(
    val y: Int,
)
