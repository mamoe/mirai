/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.Testing
import net.mamoe.mirai.console.Testing.withTesting
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.getRegisteredCommands
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.registerCommand
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterCommand
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.descriptor.buildCommandArgumentContext
import net.mamoe.mirai.console.initTestEnvironment
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.flattenCommandComponents
import net.mamoe.mirai.message.data.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.*

object TestCompositeCommand : CompositeCommand(
    ConsoleCommandOwner,
    "testComposite", "tsC"
) {
    @SubCommand
    fun mute(seconds: Int = 60) {
        Testing.ok(seconds)
    }

    @SubCommand
    fun mute(target: Long, seconds: Int) {
        Testing.ok(seconds)
    }
}


object TestSimpleCommand : RawCommand(owner, "testSimple", "tsS") {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        Testing.ok(args)
    }
}

internal val sender by lazy { ConsoleCommandSender }
internal val owner by lazy { ConsoleCommandOwner }


@OptIn(ExperimentalCommandDescriptors::class)
internal class TestCommand {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            initTestEnvironment()
        }

        @AfterAll
        @JvmStatic
        fun destroy() {
            MiraiConsole.cancel()
        }
    }

    @Test
    fun testRegister() {
        error(TestCompositeCommand.usage)
        try {
            unregisterAllCommands(ConsoleCommandOwner) // builtins
            unregisterCommand(TestSimpleCommand)

            assertTrue(TestCompositeCommand.register())
            assertFalse(TestCompositeCommand.register())

            assertEquals(1, getRegisteredCommands(ConsoleCommandOwner).size)

            assertEquals(1, CommandManagerImpl._registeredCommands.size)
            assertEquals(2,
                CommandManagerImpl.requiredPrefixCommandMap.size,
                CommandManagerImpl.requiredPrefixCommandMap.entries.joinToString { it.toString() })
        } finally {
            unregisterCommand(TestCompositeCommand)
        }
    }

    @Test
    fun testSimpleExecute() = runBlocking {
        TestSimpleCommand.withRegistration {
            assertEquals("test", withTesting<MessageChain> {
                assertSuccess(TestSimpleCommand.execute(sender, "test"))
            }.contentToString())
        }
    }

    @Test
    fun `test flattenCommandArgs`() {
        val result = arrayOf("test", image).flattenCommandComponents().toTypedArray()

        assertEquals("test", result[0].content)
        assertSame(image, result[1])

        assertEquals(2, result.size)
    }

    @Test
    fun testSimpleArgsSplitting() = runBlocking {
        TestSimpleCommand.withRegistration {
            assertEquals(arrayOf("test", "ttt", "tt").joinToString(), withTesting<MessageChain> {
                assertSuccess(TestSimpleCommand.execute(sender, PlainText("test ttt tt")))
            }.joinToString())
        }
    }

    val image = Image("/f8f1ab55-bf8e-4236-b55e-955848d7069f")

    @Test
    fun `PlainText and Image args splitting`() = runBlocking {
        TestSimpleCommand.withRegistration {
            val result = withTesting<MessageChain> {
                assertSuccess(TestSimpleCommand.execute(sender, buildMessageChain {
                    +"test"
                    +image
                    +"tt"
                }))
            }
            assertEquals<Any>(arrayOf("test", image, "tt").joinToString(), result.toTypedArray().joinToString())
            assertSame(image, result[1])
        }
    }

    @Test
    fun `test throw Exception`() {
        runBlocking {
            assertTrue(sender.executeCommand("").isFailure())
        }
    }

    @Test
    fun `executing command by string command`() = runBlocking {
        TestCompositeCommand.withRegistration {
            val result = withTesting<Int> {
                assertSuccess(sender.executeCommand("/testComposite mute 1"))
            }

            assertEquals(1, result)
        }
    }

    @Test
    fun `composite command descriptors`() {
        val overloads = TestCompositeCommand.overloads
        assertEquals("CommandSignatureVariant(<mute>, seconds: Int = ...)", overloads[0].toString())
        assertEquals("CommandSignatureVariant(<mute>, target: Long, seconds: Int)", overloads[1].toString())
    }

    @Test
    fun `composite command executing`() = runBlocking {
        TestCompositeCommand.withRegistration {
            assertEquals(1, withTesting {
                assertSuccess(TestCompositeCommand.execute(sender, "mute 1"))
            })
        }
    }

    @Test
    fun `composite sub command resolution conflict`() {
        runBlocking {
            val composite = object : CompositeCommand(
                ConsoleCommandOwner,
                "tr"
            ) {
                @Suppress("UNUSED_PARAMETER")
                @SubCommand
                fun mute(seconds: Int) {
                    Testing.ok(1)
                }

                @Suppress("UNUSED_PARAMETER")
                @SubCommand
                fun mute(seconds: Int, arg2: Int = 1) {
                    Testing.ok(2)
                }
            }

            registerCommand(composite)

            println(composite.overloads.joinToString())

            composite.withRegistration {
                assertEquals(1, withTesting { assertSuccess(composite.execute(sender, "mute 123")) }) // one arg, resolves to mute(Int)
                assertEquals(2, withTesting { assertSuccess(composite.execute(sender, "mute 123 1")) }) // two arg, resolved to mute(Int, Int)
            }
        }
    }

    @Test
    fun `composite sub command parsing`() {
        runBlocking {
            class MyClass(
                val value: Int,
            )

            val composite = object : CompositeCommand(
                ConsoleCommandOwner,
                "test22",
                overrideContext = buildCommandArgumentContext {
                    add(object : CommandValueArgumentParser<MyClass> {
                        override fun parse(raw: String, sender: CommandSender): MyClass {
                            return MyClass(raw.toInt())
                        }

                        override fun parse(raw: MessageContent, sender: CommandSender): MyClass {
                            if (raw is PlainText) return parse(raw.content, sender)
                            assertSame(image, raw)
                            return MyClass(2)
                        }
                    })
                }
            ) {
                @SubCommand
                fun mute(seconds: MyClass) {
                    Testing.ok(seconds)
                }
            }

            composite.withRegistration {
                assertEquals(333, withTesting<MyClass> { assertSuccess(execute(sender, "mute 333")) }.value)
                assertEquals(2, withTesting<MyClass> {
                    assertSuccess(
                        execute(sender, buildMessageChain {
                            +"mute"
                            +image
                        })
                    )
                }.value)
            }
        }
    }

    @Test
    fun `test simple command`() {
        runBlocking {

            val simple = object : SimpleCommand(owner, "test") {
                @Handler
                fun onCommand(string: String) {
                    Testing.ok(string)
                }
            }

            simple.withRegistration {
                // assertEquals("xxx", withTesting { simple.execute(sender, "xxx") })
                assertEquals("xxx", withTesting { assertSuccess(sender.executeCommand("/test xxx")) })
            }
        }
    }

    @Test
    fun `test optional argument command`() {
        runBlocking {
            val optionCommand = object : CompositeCommand(
                ConsoleCommandOwner,
                "testOptional"
            ) {
                @SubCommand
                fun optional(arg1: String, arg2: String = "Here is optional", arg3: String? = null) {
                    println(arg1)
                    println(arg2)
                    println(arg3)
//                    println(arg3)
                    Testing.ok(Unit)
                }
            }
            optionCommand.withRegistration {
                withTesting<Unit> {
                    assertSuccess(sender.executeCommand("/testOptional optional 1"))
                }
            }
        }
    }

    @Test
    fun `test vararg`() {
        runBlocking {
            val optionCommand = object : CompositeCommand(
                ConsoleCommandOwner,
                "test"
            ) {
                @SubCommand
                fun vararg(arg1: Int, vararg x: String) {
                    assertEquals(1, arg1)
                    Testing.ok(x)
                }
            }
            optionCommand.withRegistration {
                assertArrayEquals(
                    emptyArray<String>(),
                    withTesting {
                        assertSuccess(sender.executeCommand("/test vararg 1"))
                    }
                )

                assertArrayEquals(
                    arrayOf("s"),
                    withTesting<Array<String>> {
                        assertSuccess(sender.executeCommand("/test vararg 1 s"))
                    }
                )
                assertArrayEquals(
                    arrayOf("s", "s", "s"),
                    withTesting {
                        assertSuccess(sender.executeCommand("/test vararg 1 s s s"))
                    }
                )
            }
        }
    }
}

fun <T> assertArrayEquals(expected: Array<out T>, actual: Array<out T>, message: String? = null) {
    asserter.assertEquals(message, expected.contentToString(), actual.contentToString())
}

@OptIn(ExperimentalCommandDescriptors::class)
internal fun assertSuccess(result: CommandExecuteResult) {
    if (result.isFailure()) {
        throw result.exception ?: AssertionError(result.toString())
    }
}