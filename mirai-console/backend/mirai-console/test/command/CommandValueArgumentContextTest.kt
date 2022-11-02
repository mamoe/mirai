/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalCommandDescriptors::class)

package net.mamoe.mirai.console.command

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.Testing
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParserException
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.descriptor.buildCommandArgumentContext
import net.mamoe.mirai.console.command.java.JCompositeCommand
import net.mamoe.mirai.console.command.java.JSimpleCommand
import net.mamoe.mirai.console.internal.data.classifierAsKClass
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

@Suppress("UNUSED_PARAMETER", "unused")
internal class CommandValueArgumentContextTest : AbstractCommandTest() {

    inner class CustomBooleanParser : CommandValueArgumentParser<Boolean> {
        @Throws(CommandArgumentParserException::class)
        override fun parse(raw: String, sender: CommandSender): Boolean {
            return raw == "TRUE!"
        }

        @Throws(CommandArgumentParserException::class)
        override fun parse(raw: MessageContent, sender: CommandSender): Boolean {
            // 将一个图片认为是 'true'
            return if (raw is Image && raw.imageId == "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                true
            } else super.parse(raw, sender)
        }
    }

    inner class JavaComposite : JCompositeCommand(mockPlugin, "main") {
        init {
            addArgumentContext(
                buildCommandArgumentContext { java.lang.Boolean.TYPE with CustomBooleanParser() }
            )
        }

        @SubCommand("name")
        fun foo(context: CommandContext, arg: String, b: Boolean) {
            Testing.ok(b)
        }
    }

    inner class JavaSimple : JSimpleCommand(mockPlugin, "main") {
        init {
            addArgumentContext(
                buildCommandArgumentContext { java.lang.Boolean.TYPE with CustomBooleanParser() }
            )
        }

        @Handler
        fun foo(context: CommandContext, arg: String, b: Boolean) {
            Testing.ok(b)
        }
    }

    @TestFactory
    fun test(): List<DynamicTest> {
        return listOf(
            JavaComposite() to listOf(
                messageChainOf(PlainText("/main"), PlainText("name"), PlainText("aaa"), PlainText("TRUE!")),
                messageChainOf(
                    PlainText("/main"),
                    PlainText("name"),
                    PlainText("aaa"),
                    Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg")
                )
            ), JavaSimple() to listOf(
                messageChainOf(PlainText("/main"), PlainText("aaa"), PlainText("TRUE!")),
                messageChainOf(
                    PlainText("/main"),
                    PlainText("aaa"),
                    Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg")
                )
            )
        ).flatMap { (instance, cmds) ->
            cmds.map { message ->
                DynamicTest.dynamicTest(instance::class.supertypes.first().classifierAsKClass().simpleName) {
                    runBlocking {
                        instance.withRegistration {
                            assertEquals(
                                true,
                                Testing.withTesting {
                                    assertSuccess(
                                        CommandManager.executeCommand(sender, message, checkPermission = false)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}