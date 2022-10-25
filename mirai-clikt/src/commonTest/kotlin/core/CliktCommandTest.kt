package net.mamoe.mirai.clikt.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.mamoe.mirai.clikt.parameters.arguments.argument
import net.mamoe.mirai.clikt.parameters.arguments.multiple
import net.mamoe.mirai.clikt.parameters.groups.OptionGroup
import net.mamoe.mirai.clikt.parameters.groups.cooccurring
import net.mamoe.mirai.clikt.parameters.groups.mutuallyExclusiveOptions
import net.mamoe.mirai.clikt.parameters.groups.provideDelegate
import net.mamoe.mirai.clikt.parameters.options.flag
import net.mamoe.mirai.clikt.parameters.options.option
import net.mamoe.mirai.clikt.parameters.options.required
import net.mamoe.mirai.clikt.parameters.types.int
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused")
class CliktCommandTest {
    @Test
    @Suppress("ClassName")
    @JsName("inferring_command_name")
    fun `inferring command name`() {
        class ListAllValuesCommand : TestCommand()
        class LGTMMeansLookingGoodToMe : TestCommand()
        class `nothing-to-change` : TestCommand()
        ListAllValuesCommand().commandName shouldBe "list-all-values"
        LGTMMeansLookingGoodToMe().commandName shouldBe "lgtmmeans-looking-good-to-me"
        `nothing-to-change`().commandName shouldBe "nothing-to-change"
    }

    @Test
    @JsName("invokeWithoutSubcommand_false")
    fun `invokeWithoutSubcommand=false`() {
        shouldThrow<PrintHelpMessage> {
            TestCommand(called = false).subcommands(TestCommand(called = false)).parse("")
        }.error shouldBe true

        val child = TestCommand(called = true, name = "foo")
        TestCommand(called = true).subcommands(child).apply {
            parse("foo")
            currentContext.invokedSubcommand shouldBe child
            child.currentContext.invokedSubcommand shouldBe null
        }
    }

    @Test
    @JsName("invokeWithoutSubcommand_true")
    fun `invokeWithoutSubcommand=true`() {
        TestCommand(called = true, invokeWithoutSubcommand = true).subcommands(TestCommand(called = false)).apply {
            parse("")
            currentContext.invokedSubcommand shouldBe null
        }

        val child = TestCommand(called = true, name = "foo")
        TestCommand(called = true, invokeWithoutSubcommand = true).subcommands(child).apply {
            parse("foo")
            currentContext.invokedSubcommand shouldBe child
            child.currentContext.invokedSubcommand shouldBe null
        }
    }


    @Test
    @JsName("printHelpOnEmptyArgs__true")
    fun `printHelpOnEmptyArgs = true`() {
        class C : TestCommand(called = false, printHelpOnEmptyArgs = true)
        shouldThrow<PrintHelpMessage> {
            C().parse("")
        }.error shouldBe true
    }

    @Test
    @JsName("shortHelp_extraction")
    fun `shortHelp extraction`() = forAll(
        row("", ""),
        row("foo bar", "foo bar"),
        row("\n  \tfoo bar", "foo bar"),
        row("```\n  foo bar", "foo bar"),
        row("```\n  foo bar", "foo bar"),
        row("```foo\nbar", "foo")
    ) { help, expected ->
        class C : NoOpCliktCommand(help = help) {
            val sh = shortHelp()
        }
        C().sh shouldBe expected
    }

    @Test
    fun aliases() = forAll(
        row("-xx", "x", emptyList()),
        row("a", "a", listOf("b")),
        row("a", "a", listOf("b")),
        row("b", null, listOf("-xa")),
        row("recurse", null, listOf("recurse")),
        row("recurse2", "foo", listOf("recurse", "recurse2"))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            val y by argument().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }

            override fun aliases() = mapOf(
                "y" to listOf("-x"),
                "a" to listOf("-xa", "b"),
                "b" to listOf("--", "-xa"),
                "recurse" to listOf("recurse"),
                "recurse2" to listOf("recurse", "--xx=foo", "recurse2")
            )
        }

        C().parse(argv)
    }

    @Test
    @JsName("command_usage")
    fun `command usage`() {
        class Parent : TestCommand(called = false) {
            val arg by argument()
        }

        shouldThrow<UsageError> {
            Parent().parse("")
        }.helpMessage() shouldBe """
            |Usage: parent [OPTIONS] ARG
            |
            |Error: Missing argument "ARG"
            """.trimMargin()
    }

    @Test
    @JsName("command_toString")
    fun `command toString`() {
        class Cmd : TestCommand() {
            val opt by option("-o", "--option")
            val int by option().int()
            val arg by argument()
        }

        class Sub : TestCommand() {
            val foo by option()
        }

        Cmd().toString() shouldBe "<Cmd name=cmd options=[--option --int] arguments=[ARG]>"
        Cmd().apply { parse("--int=123 bar") }
            .toString() shouldBe "<Cmd name=cmd options=[--option=null --int=123 --help] arguments=[ARG=bar]>"
        Cmd().apply { parse("foo") }
            .toString() shouldBe "<Cmd name=cmd options=[--option=null --int=null --help] arguments=[ARG=foo]>"

        Cmd().subcommands(Sub()).apply { parse("-ooo bar sub --foo=baz") }.toString().shouldBe(
            "<Cmd name=cmd options=[--option=oo --int=null --help] arguments=[ARG=bar] " +
                    "subcommands=[<Sub name=sub options=[--foo=baz --help]>]>"
        )
    }

    @Test
    @JsName("command_with_groups_toString")
    fun `command with groups toString`() {
        class G : OptionGroup() {
            val opt by option("-o", "--option")
        }

        class G2 : OptionGroup() {
            val foo by option().required()
            val bar by option()
        }

        class Cmd : TestCommand() {
            val g by G()
            val g2 by G2().cooccurring()
            val ge by mutuallyExclusiveOptions(
                option("--e1"),
                option("--e2")
            )
        }

        Cmd().toString() shouldBe "<Cmd name=cmd options=[--option --foo --bar --e1 --e2]>"
        Cmd().apply { parse("-oo --foo=f --e1=1") }
            .toString() shouldBe "<Cmd name=cmd options=[--option=o --foo=f --bar=null --e1=1 --e2=null --help]>"
    }

    // https://github.com/ajalt/clikt/issues/64
    @Test
    @JsName("context_is_initialized_when_helpOptionNames_is_null")
    fun `context is initialized when helpOptionNames is null`() {
        class D : TestCommand() {
            override fun run_() {
                currentContext shouldNotBe null
            }
        }

        TestCommand().context { helpOptionNames = emptySet() }.subcommands(D()).parse("d")
    }

    @Test
    @JsName("command_registered_functions")
    fun `command registered functions`() {
        val child1 = TestCommand(name = "foo", called = false)
        val child2 = TestCommand(name = "bar", called = false)

        class G : OptionGroup() {
            val og by option()
        }

        val g = G()

        class C : TestCommand(called = false) {
            val o1 by option()
            val o2 by option().flag()
            val a by argument()
            val g by g
        }

        val c = C()
        c.registeredSubcommands() should beEmpty()
        c.subcommands(child1, child2)

        c.registeredSubcommands().shouldContainExactlyInAnyOrder(child1, child2)
        c.registeredOptions().map { it.names.single() }.shouldContainExactlyInAnyOrder(
            "--o1", "--o2", "--og"
        )
        c.registeredArguments().map { it.name } shouldBe listOf("A")
        c.registeredParameterGroups() shouldBe listOf(g)
    }

    @Test
    @JsName("treat_unknown_options_as_arguments")
    fun `treat unknown options as arguments`() {
        class C : TestCommand(treatUnknownOptionsAsArgs = true) {
            val foo by option().flag()
            val args by argument().multiple()

            override fun run_() {
                foo shouldBe true
                args shouldBe listOf("--bar", "baz", "--qux=qoz")
            }
        }

        C().parse("--bar --foo baz --qux=qoz")
    }

    @Test
    @JsName("treat_unknown_options_as_arguments_with_grouped_flag")
    fun `treat unknown options as arguments with grouped flag`() {
        class C(called: Boolean) : TestCommand(called = called, treatUnknownOptionsAsArgs = true) {
            val foo by option("-f").flag()
            val args by argument().multiple()
        }

        val c = C(true)
        c.parse("-f -g -i")
        c.foo shouldBe true
        c.args shouldBe listOf("-g", "-i")
        shouldThrow<NoSuchOption> {
            C(false).parse("-fgi")
        }.message shouldBe "no such option: \"-g\""
    }
}