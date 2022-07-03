/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalCommandDescriptors::class)
@file:Suppress("unused", "UNUSED_PARAMETER")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.Testing
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.java.JCompositeCommand
import net.mamoe.mirai.console.command.java.JRawCommand
import net.mamoe.mirai.console.command.java.JSimpleCommand
import net.mamoe.mirai.console.internal.data.classifierAsKClass
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast
import org.apache.commons.lang3.ArrayUtils.isSameType
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

internal class CommandContextTest : AbstractCommandTest() {
    private class MyMetadata : MessageMetadata {
        override fun hashCode(): Int = javaClass.hashCode()
        override fun equals(other: Any?): Boolean = isSameType(this, other)

        override fun toString(): String = "MyMetadata"

        companion object Key : AbstractMessageKey<MyMetadata>({ it.safeCast() })
    }


    ///////////////////////////////////////////////////////////////////////////
    // RawCommand
    ///////////////////////////////////////////////////////////////////////////

    @TestFactory
    fun `can execute with sender`(): List<DynamicTest> {
        return listOf(
            object : RawCommand(owner, "test") {
                override suspend fun CommandContext.onCommand(args: MessageChain) {
                    Testing.ok(args)
                }
            } to "/test foo",
            object : SimpleCommand(owner, "test") {
                @Handler
                fun CommandContext.foo(arg: MessageChain) {
                    Testing.ok(arg)
                }
            } to "/test foo",
            object : CompositeCommand(owner, "test") {
                @SubCommand
                fun CommandContext.sub(arg: MessageChain) {
                    Testing.ok(arg)
                }
            } to "/test sub foo",

            object : JRawCommand(owner, "test") {
                override fun onCommand(context: CommandContext, args: MessageChain) {
                    Testing.ok(args)
                }
            } to "/test foo",
            object : JSimpleCommand(owner, "test") {
                @Handler
                fun foo(context: CommandContext, arg: MessageChain) {
                    Testing.ok(arg)
                }
            } to "/test foo",
            object : JCompositeCommand(owner, "test") {
                @SubCommand
                fun sub(context: CommandContext, arg: MessageChain) {
                    Testing.ok(arg)
                }
            } to "/test sub foo",
        ).map { (instance, cmd) ->
            DynamicTest.dynamicTest(instance::class.supertypes.first().classifierAsKClass().simpleName) {
                runBlocking {
                    instance.withRegistration {
                        assertEquals(
                            messageChainOf(PlainText("foo")),
                            Testing.withTesting {
                                assertSuccess(sender.executeCommand(cmd, checkPermission = false))
                            }
                        )
                    }
                }
            }
        }
    }

    @TestFactory
    fun `RawCommand can execute and get original chain`(): List<DynamicTest> {
        return listOf(
            object : RawCommand(owner, "test") {
                override suspend fun CommandContext.onCommand(args: MessageChain) {
                    Testing.ok(originalMessage)
                }
            } to "/test foo",
            object : SimpleCommand(owner, "test") {
                @Handler
                fun CommandContext.foo(arg: MessageChain) {
                    Testing.ok(originalMessage)
                }
            } to "/test foo",
            object : CompositeCommand(owner, "test") {
                @SubCommand
                fun CommandContext.sub(arg: MessageChain) {
                    Testing.ok(originalMessage)
                }
            } to "/test sub foo",

            object : JRawCommand(owner, "test") {
                override fun onCommand(context: CommandContext, args: MessageChain) {
                    Testing.ok(context.originalMessage)
                }
            } to "/test foo",
            object : JSimpleCommand(owner, "test") {
                @Handler
                fun foo(context: CommandContext, arg: MessageChain) {
                    Testing.ok(context.originalMessage)
                }
            } to "/test foo",
            object : JCompositeCommand(owner, "test") {
                @SubCommand
                fun sub(context: CommandContext, arg: MessageChain) {
                    Testing.ok(context.originalMessage)
                }
            } to "/test sub foo",
        ).map { (instance, cmd) ->
            DynamicTest.dynamicTest(instance::class.supertypes.first().classifierAsKClass().simpleName) {
                runBlocking {
                    instance.withRegistration {
                        assertEquals(
                            cmd,
                            Testing.withTesting<MessageChain> {
                                assertSuccess(sender.executeCommand(cmd, checkPermission = false))
                            }.contentToString()
                        )
                    }
                }
            }
        }
    }

    @TestFactory
    fun `can execute and get metadata`(): List<DynamicTest> {
        val metadata = MyMetadata()
        return listOf(
            object : RawCommand(owner, "test") {
                override suspend fun CommandContext.onCommand(args: MessageChain) {
                    Testing.ok(originalMessage[MyMetadata])
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : SimpleCommand(owner, "test") {
                @Handler
                fun CommandContext.foo(arg: MessageChain) {
                    Testing.ok(originalMessage[MyMetadata])
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : CompositeCommand(owner, "test") {
                @SubCommand
                fun CommandContext.sub(arg: MessageChain) {
                    Testing.ok(originalMessage[MyMetadata])
                }
            } to messageChainOf(PlainText("/test"), PlainText("sub"), metadata, PlainText("foo")),


            object : JRawCommand(owner, "test") {
                override fun onCommand(context: CommandContext, args: MessageChain) {
                    Testing.ok(context.originalMessage[MyMetadata])
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : JSimpleCommand(owner, "test") {
                @Handler
                fun foo(context: CommandContext, arg: MessageChain) {
                    Testing.ok(context.originalMessage[MyMetadata])
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : JCompositeCommand(owner, "test") {
                @SubCommand
                fun sub(context: CommandContext, arg: MessageChain) {
                    Testing.ok(context.originalMessage[MyMetadata])
                }
            } to messageChainOf(PlainText("/test"), PlainText("sub"), metadata, PlainText("foo")),
        ).map { (instance, cmd) ->
            DynamicTest.dynamicTest(instance::class.supertypes.first().classifierAsKClass().simpleName) {
                runBlocking {
                    instance.withRegistration {
                        assertEquals(
                            metadata,
                            Testing.withTesting {
                                assertSuccess(CommandManager.executeCommand(sender, cmd, checkPermission = false))
                            }
                        )
                    }
                }
            }
        }
    }

    @TestFactory
    fun `RawCommand can execute and get chain including metadata`(): List<DynamicTest> {
        val metadata = MyMetadata()
        return listOf(
            object : RawCommand(owner, "test") {
                override suspend fun CommandContext.onCommand(args: MessageChain) {
                    Testing.ok(originalMessage)
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : SimpleCommand(owner, "test") {
                @Handler
                fun CommandContext.foo(arg: MessageChain) {
                    Testing.ok(originalMessage)
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : CompositeCommand(owner, "test") {
                @SubCommand
                fun CommandContext.sub(arg: MessageChain) {
                    Testing.ok(originalMessage)
                }
            } to messageChainOf(PlainText("/test"), PlainText("sub"), metadata, PlainText("foo")),


            object : JRawCommand(owner, "test") {
                override fun onCommand(context: CommandContext, args: MessageChain) {
                    Testing.ok(context.originalMessage)
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : JSimpleCommand(owner, "test") {
                @Handler
                fun foo(context: CommandContext, arg: MessageChain) {
                    Testing.ok(context.originalMessage)
                }
            } to messageChainOf(PlainText("/test"), metadata, PlainText("foo")),
            object : JCompositeCommand(owner, "test") {
                @SubCommand
                fun sub(context: CommandContext, arg: MessageChain) {
                    Testing.ok(context.originalMessage)
                }
            } to messageChainOf(PlainText("/test"), PlainText("sub"), metadata, PlainText("foo")),
        ).map { (instance, cmd) ->
            DynamicTest.dynamicTest(instance::class.supertypes.first().classifierAsKClass().simpleName) {
                runBlocking {
                    instance.withRegistration {
                        assertEquals(
                            cmd,
                            Testing.withTesting {
                                assertSuccess(CommandManager.executeCommand(sender, cmd, checkPermission = false))
                            }
                        )
                    }
                }
            }
        }
    }
}