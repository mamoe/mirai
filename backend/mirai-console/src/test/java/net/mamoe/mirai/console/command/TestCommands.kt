/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.message.data.toMessage
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


val plugin: PluginBase = object : PluginBase() {

}

internal object TestCommand : PluginCommand(
    plugin,
    CommandDescriptor("test") {
        param<String>()
    }
) {
    override suspend fun onCommand(sender: CommandSender, args: CommandArgs): Boolean {
        val s = args.getReified<String>()
        sender.sendMessage(s)
        return true
    }
}

internal class TestCommands {
    @Test
    fun testFlatten() {
        assertEquals(listOf("test", "v1"), "test v1".flattenCommandComponents().toList())
        assertEquals(listOf("test", "v1"), PlainText("test v1").flattenCommandComponents().toList())
        assertEquals(listOf("test", "v1"), arrayOf("test ", "v1", "   ").flattenCommandComponents().toList())
        assertEquals(
            listOf("test", "v1"),
            messageChainOf("test v1".toMessage(), " ".toMessage()).flattenCommandComponents().toList()
        )
    }

    @Test
    fun testRegister() {
        assertTrue(TestCommand.register())
        assertEquals(listOf("test"), TestCommand.allNames.single().toList())

        assertFalse(TestCommand.register())
        assertFalse(
            object : PluginCommand(
                plugin,
                CommandDescriptor("test") {
                    param<String>()
                }
            ) {
                override suspend fun onCommand(sender: CommandSender, args: CommandArgs): Boolean {
                    val s = args.getReified<String>()
                    sender.sendMessage(s)
                    return true
                }
            }.register()
        )
    }
}

