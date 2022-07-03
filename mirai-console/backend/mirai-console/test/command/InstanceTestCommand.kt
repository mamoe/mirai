/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "UNUSED_PARAMETER")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsoleImplementation
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
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.command.flattenCommandComponents
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.testFramework.AbstractConsoleInstanceTest
import net.mamoe.mirai.message.data.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.*
import java.time.temporal.TemporalAccessor
import kotlin.reflect.KClass
import kotlin.test.*




class TestContainerCompositeCommand : CompositeCommand(
    owner,
    "testContainerComposite", "tsPC"
) {

    class TopGroup : AbstractSubCommandGroup() {

        class NestGroup : AbstractSubCommandGroup() {
            @AnotherSubCommand
            fun foo2(seconds: Int) {
                Testing.ok(seconds)
            }
        }

        @AnotherCombinedCommand
        val provider: SubCommandGroup = NestGroup()

        @AnotherSubCommand
        fun foo1(seconds: Int) {
            Testing.ok(seconds)
        }
    }

    @CombinedCommand
    val provider: SubCommandGroup = TopGroup()

    @SubCommand
    fun foo0(seconds: Int) {
        Testing.ok(seconds)
    }

    @SubCommand
    fun containerBar(seconds: Int) {
        Testing.ok(seconds)
    }
}

class TestCompositeCommand : CompositeCommand(
    owner,
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

class TestRawCommand : RawCommand(
    owner,
    "testRaw"
) {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        Testing.ok(args)
    }
}


class TestSimpleCommand : RawCommand(owner, "testSimple", "tsS") {
    override suspend fun CommandSender.onCommand(args: MessageChain) {
        Testing.ok(args)
    }
}

@Suppress("EnumEntryName")
class TestEnumArgCommand : CompositeCommand(owner, "testenum") {
    enum class TestEnum {
        V1, V2, V3
    }

    enum class TestCase {
        A, a
    }

    enum class TestCamelCase {
        A, B, A_B
    }

    @SubCommand("tcc")
    fun CommandSender.testCamelCase(enum: TestCamelCase) {
        Testing.ok(enum)
    }

    @SubCommand("tc")
    fun CommandSender.testCase(enum: TestCase) {
        Testing.ok(enum)
    }

    @SubCommand
    fun CommandSender.e1(enum: TestEnum) {
        Testing.ok(enum)
    }
}

class TestTemporalArgCommand : CompositeCommand(owner, "testtemporal") {

    @SubCommand
    fun CommandSender.instant(temporal: Instant) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.year(temporal: Year) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.yearmonth(temporal: YearMonth) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.localdate(temporal: LocalDate) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.localtime(temporal: LocalTime) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.localdatetime(temporal: LocalDateTime) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.offsettime(temporal: OffsetTime) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.offsetdatetime(temporal: OffsetDateTime) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.zoneddatetime(temporal: ZonedDateTime) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.monthday(temporal: MonthDay) {
        Testing.ok(temporal)
    }

    @SubCommand
    fun CommandSender.zoneoffset(temporal: ZoneOffset) {
        Testing.ok(temporal)
    }
}

private val sender get() = ConsoleCommandSender
private val owner get() = ConsoleCommandOwner

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@OptIn(ExperimentalCommandDescriptors::class)
internal class InstanceTestCommand : AbstractConsoleInstanceTest() {
    private val manager by lazy { MiraiConsoleImplementation.getBridge().commandManager as CommandManagerImpl }

    private val simpleCommand by lazy { TestSimpleCommand() }
    private val rawCommand by lazy { TestRawCommand() }
    private val compositeCommand by lazy { TestCompositeCommand() }
    private val containerCompositeCommand by lazy { TestContainerCompositeCommand() }

    @BeforeEach
    fun grantPermission() {
        ConsoleCommandSender.permit(simpleCommand.permission)
        ConsoleCommandSender.permit(compositeCommand.permission)
    }

    @Test
    fun testRegister() {
        try {
            unregisterAllCommands(ConsoleCommandOwner) // builtins
            unregisterAllCommands(owner) // testing unit
            unregisterCommand(simpleCommand)

            assertTrue(compositeCommand.register())
            assertFalse(compositeCommand.register())

            assertEquals(1, getRegisteredCommands(owner).size)

            assertEquals(1, manager._registeredCommands.size)
            assertEquals(2,
                manager.requiredPrefixCommandMap.size,
                manager.requiredPrefixCommandMap.entries.joinToString { it.toString() })
        } finally {
            unregisterCommand(compositeCommand)
        }
    }

    @Test
    fun testSimpleExecute() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals("test", withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, "test"))
            }.contentToString())
        }
    }

    @Test
    fun `test raw command`() = runBlocking {
        rawCommand.withRegistration {
            val result = withTesting<MessageChain> {
                assertSuccess(rawCommand.execute(sender, PlainText("a1"), PlainText("a2"), PlainText("a3")))
            }
            assertEquals(3, result.size)
            assertEquals("a1, a2, a3", result.joinToString())
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
    fun `test enum argument`() = runBlocking {
        val enum = TestEnumArgCommand()
        enum.withRegistration {

            assertEquals(TestEnumArgCommand.TestEnum.V1, withTesting {
                assertSuccess(enum.execute(sender, PlainText("e1"), PlainText("V1")))
            })
            assertEquals(TestEnumArgCommand.TestEnum.V2, withTesting {
                assertSuccess(enum.execute(sender, PlainText("e1"), PlainText("V2")))
            })
            assertEquals(TestEnumArgCommand.TestEnum.V3, withTesting {
                assertSuccess(enum.execute(sender, PlainText("e1"), PlainText("V3")))
            })
            withTesting<Unit> {
                assertFailure(enum.execute(sender, PlainText("e1"), PlainText("ENUM_NOT_FOUND")))
                Testing.ok(Unit)
            }
            assertEquals(TestEnumArgCommand.TestEnum.V1, withTesting {
                assertSuccess(enum.execute(sender, PlainText("e1"), PlainText("v1")))
            })
            assertEquals(TestEnumArgCommand.TestEnum.V2, withTesting {
                assertSuccess(enum.execute(sender, PlainText("e1"), PlainText("v2")))
            })
            assertEquals(TestEnumArgCommand.TestEnum.V3, withTesting {
                assertSuccess(enum.execute(sender, PlainText("e1"), PlainText("v3")))
            })


            assertEquals(TestEnumArgCommand.TestCase.A, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tc"), PlainText("A")))
            })
            assertEquals(TestEnumArgCommand.TestCase.a, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tc"), PlainText("a")))
            })
            withTesting<Unit> {
                assertFailure(enum.execute(sender, PlainText("tc"), PlainText("ENUM_NOT_FOUND")))
                Testing.ok(Unit)
            }


            assertEquals(TestEnumArgCommand.TestCamelCase.A, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tcc"), PlainText("A")))
            })
            assertEquals(TestEnumArgCommand.TestCamelCase.A, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tcc"), PlainText("a")))
            })
            assertEquals(TestEnumArgCommand.TestCamelCase.B, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tcc"), PlainText("B")))
            })
            assertEquals(TestEnumArgCommand.TestCamelCase.B, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tcc"), PlainText("b")))
            })
            assertEquals(TestEnumArgCommand.TestCamelCase.A_B, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tcc"), PlainText("A_B")))
            })
            assertEquals(TestEnumArgCommand.TestCamelCase.A_B, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tcc"), PlainText("a_b")))
            })
            assertEquals(TestEnumArgCommand.TestCamelCase.A_B, withTesting {
                assertSuccess(enum.execute(sender, PlainText("tcc"), PlainText("aB")))
            })
            withTesting<Unit> {
                assertFailure(enum.execute(sender, PlainText("tc"), PlainText("ENUM_NOT_FOUND")))
                Testing.ok(Unit)
            }

        }
    }

    @Test
    fun `test temporal argument`() = runBlocking {
        val command = TestTemporalArgCommand()
        command.withRegistration {
            val temporal: List<KClass<out TemporalAccessor>> = listOf(
                Instant::class,
                Year::class,
                YearMonth::class,
                LocalDate::class,
                LocalTime::class,
                LocalDateTime::class,
                OffsetTime::class,
                OffsetDateTime::class,
                ZonedDateTime::class,
                MonthDay::class,
                ZoneOffset::class
            )

            for (kClass in temporal) {
                val subCommand = kClass.simpleName!!
                val implement: TemporalAccessor = withTesting {
                    assertSuccess(execute(sender, PlainText(subCommand), PlainText("now")))
                }
                assertTrue { kClass.isInstance(implement) }
                assertEquals(implement, withTesting {
                    assertSuccess(execute(sender, PlainText(subCommand), PlainText("$implement")))
                })
            }
        }
    }

    @Test
    fun testSimpleArgsSplitting() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "ttt", "tt").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test ttt tt")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsEscape() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "esc ape").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test esc\\ ape")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsQuote() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "esc ape").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test \"esc ape\"")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsQuoteReject() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "es\"c", "ape\"").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test es\"c ape\"")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsQuoteEscape() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "\"esc", "ape\"").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test \\\"esc ape\"")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsMultipleQuotes() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "esc ape", "1 2").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test \"esc ape\" \"1 2\"")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsMisplacedQuote() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "esc ape", "1\"", "\"2").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test \"esc ape\" 1\" \"2 ")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsQuoteSpaceEscape() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test \"esc", "ape\"").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test\\ \"esc ape\"")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsStopParse() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "esc ape  ").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test -- esc ape  ")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsStopParse2() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "esc ape  test\\12\"\"3").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test --  esc ape  test\\12\"\"3")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsStopParseReject() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test--", "esc", "ape").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test-- esc ape  ")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsStopParseEscape() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "--", "esc", "ape").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test \\-- esc ape")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsStopParseEscape2() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", " --", "esc", "ape").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test \\ -- esc ape")))
            }.joinToString())
        }
    }

    @Test
    fun testSimpleArgsStopParseQuote() = runBlocking {
        simpleCommand.withRegistration {
            assertEquals(arrayOf("test", "--", "esc", "ape").joinToString(), withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, PlainText("test \"--\" esc ape")))
            }.joinToString())
        }
    }

    val image = Image("/f8f1ab55-bf8e-4236-b55e-955848d7069f")

    @Test
    fun `PlainText and Image args splitting`() = runBlocking {
        simpleCommand.withRegistration {
            val result = withTesting<MessageChain> {
                assertSuccess(simpleCommand.execute(sender, buildMessageChain {
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
        compositeCommand.withRegistration {
            val result = withTesting<Int> {
                assertSuccess(sender.executeCommand("/testComposite mute 1"))
            }

            assertEquals(1, result)
        }
    }

    @Test
    fun `composite command descriptors`() {
        val overloads = compositeCommand.overloads
        assertEquals("CommandSignature(<mute>, seconds: Int = ...)", overloads[0].toString())
        assertEquals("CommandSignature(<mute>, target: Long, seconds: Int)", overloads[1].toString())
    }

    @Test
    fun `composite command executing`() = runBlocking {
        compositeCommand.withRegistration {
            assertEquals(1, withTesting {
                assertSuccess(compositeCommand.execute(sender, "mute 1"))
            })
        }
    }

    @Test
    fun `container composite command executing`() = runBlocking {
        containerCompositeCommand.withRegistration {
            assertEquals(0, withTesting {
                assertSuccess(containerCompositeCommand.execute(sender, "foo0 0"))
            })
            assertEquals(1, withTesting {
                assertSuccess(containerCompositeCommand.execute(sender, "foo1 1"))
            })
            assertEquals(2, withTesting {
                assertSuccess(containerCompositeCommand.execute(sender, "foo2 2"))
            })
        }
    }

    @Test
    fun `test first param command sender`() = runBlocking {
        object : CompositeCommand(owner, "cmd") {
            @SubCommand
            fun handle(sender: CommandSender, arg: String) {
                Testing.ok(arg)
            }
        }.withRegistration {
            assertEquals("test", withTesting { assertSuccess(execute(sender, "handle test")) })
        }

        object : SimpleCommand(owner, "cmd") {
            @Handler
            fun handle(sender: CommandSender, arg: String) {
                Testing.ok(arg)
            }
        }.withRegistration {
            assertEquals("hello", withTesting { assertSuccess(execute(sender, "hello")) })
        }

        object : SimpleCommand(owner, "cmd") {
            @Handler
            fun handle(arg: String, sender: CommandSender) {
                Testing.ok(arg)
            }
        }.withRegistration {
            assertFailure(execute(sender, "hello"))
        }
    }

    @Test
    fun `composite sub command resolution conflict`() {
        runBlocking {
            val composite = object : CompositeCommand(
                owner,
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
                assertEquals(
                    1,
                    withTesting {
                        assertSuccess(
                            composite.execute(
                                sender,
                                "mute 123"
                            )
                        )
                    }) // one arg, resolves to mute(Int)
                assertEquals(
                    2,
                    withTesting {
                        assertSuccess(
                            composite.execute(
                                sender,
                                "mute 123 1"
                            )
                        )
                    }) // two arg, resolved to mute(Int, Int)
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
                owner,
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
                owner,
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
                owner,
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

@OptIn(ExperimentalCommandDescriptors::class)
internal fun assertFailure(result: CommandExecuteResult) {
    if (!result.isFailure()) {
        throw AssertionError("$result not a failure")
    }
}
