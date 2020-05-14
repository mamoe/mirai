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

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.message.data.*
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
    override suspend fun CommandSender.onCommand(args: CommandArgs): Boolean {
        val s = args.getReified<String>()
        sendMessage(s)
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
                override suspend fun CommandSender.onCommand(args: CommandArgs): Boolean {
                    val s = args.getReified<String>()
                    sendMessage(s)
                    return true
                }
            }.register()
        )
    }

    @Test
    fun testExecute() = runBlocking {
        TestCommand.register()
        assertEquals(
            "ok",
            withSender {
                execute("test", "arg")
            }.contentToString()
        )
    }
}


internal inline fun withSender(block: CommandSender.() -> Unit): MessageChain {
    val result = MessageChainBuilder()
    val sender: CommandSender = object : CommandSender {
        override val bot: Bot?
            get() = null

        override suspend fun sendMessage(message: Message) {
            result.add(message)
        }

        override fun appendMessage(message: String) {
            result.add(message)
        }
    }
    sender.let(block)
    return result.asMessageChain()
}