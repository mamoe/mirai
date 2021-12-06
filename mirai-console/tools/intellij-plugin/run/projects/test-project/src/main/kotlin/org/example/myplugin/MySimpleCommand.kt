/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package org.example.myplugin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value

object MySimpleCommand0001 : SimpleCommand(
    ConsoleCommandOwner, "foo",
    description = "示例指令"
) {}

object MySimpleCommand000 : SimpleCommand(
    MyPluginMain, "foo",
    description = "示例指令"
) {
    @Handler
    suspend fun CommandSender.handle(int: Int, str: String) {

    }

    @Handler
    suspend fun String.bad(int: Int, str: String) {

    }
}

object DataTest : AutoSavePluginConfig("data") {
    val pp by value(NoDefaultValue(1))
}

object DataTest1 : ReadOnlyPluginConfig("data") {
    var pp by value<String>()
    // var should be reported
}

@Serializable
data class HasDefaultValue(
    val x: Int = 0,
)

@Serializable
data class NoDefaultValue(
    val y: Int,
)
