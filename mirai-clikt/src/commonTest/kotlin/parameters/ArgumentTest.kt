package net.mamoe.mirai.clikt.parameters

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.contain
import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.parameters.arguments.*
import net.mamoe.mirai.clikt.parameters.options.counted
import net.mamoe.mirai.clikt.parameters.options.default
import net.mamoe.mirai.clikt.parameters.options.option
import net.mamoe.mirai.clikt.parameters.types.int
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused")
class ArgumentTest {
    @Test
    @JsName("one_required_argument")
    fun `one required argument`() {
        class C : TestCommand(called = false) {
            val foo by argument()
        }

        shouldThrow<MissingArgument> { C().parse("") }
            .message shouldBe "Missing argument \"FOO\""
    }

    @Test
    @JsName("one_optional_argument")
    fun `one optional argument`() = forAll(
        row("", null),
        row("a=b", "a=b"),
        row("-- --", "--")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by argument().optional()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("one_default_argument")
    fun `one default argument`() = forAll(
        row("", "def")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by argument().default("def")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("defaultLazy_argument")
    fun `defaultLazy argument`() {
        var called = false

        class C : TestCommand() {
            val x by argument().defaultLazy { called = true; "default" }
            override fun run_() {
                x shouldBe "default"
                called shouldBe true
            }
        }

        called shouldBe false
        C().parse("")
    }

    @Test
    @JsName("defaultLazy_referencing_other_argument")
    fun `defaultLazy referencing other argument`() {
        class C : TestCommand() {
            val y by argument().defaultLazy { x }
            val x by argument().default("def")
            override fun run_() {
                y shouldBe "def"
            }
        }

        C().parse("")
    }

    @Test
    @JsName("defaultLazy_referencing_option")
    fun `defaultLazy referencing option`() {
        class C : TestCommand() {
            val y by argument().defaultLazy { x }
            val x by option().default("def")
            override fun run_() {
                y shouldBe "def"
            }
        }

        C().parse("")
    }

    @Test
    @JsName("one_argument_nvalues_2")
    fun `one argument nvalues=2`() {
        class C : TestCommand() {
            val x by argument().pair()
            override fun run_() {
                x shouldBe ("1" to "2")
            }
        }

        C().parse("1 2")

        shouldThrow<MissingArgument> { C().parse("") }
            .message shouldBe "Missing argument \"X\""
    }

    @Test
    @JsName("one_optional_argument_nvalues_2")
    fun `one optional argument nvalues=2`() = forAll(
        row("", null)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by argument().pair().optional()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("one_optional_argument_nvalues_3")
    fun `one optional argument nvalues=3`() = forAll(
        row("", null)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by argument().triple().optional()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("misused_arguments_with_nvalues_2")
    fun `misused arguments with nvalues=2`() {
        class C : TestCommand() {
            val x by argument().pair()
        }
        shouldThrow<IncorrectArgumentValueCount> { C().parse("foo") }
            .message shouldBe "argument X requires 2 values"
        shouldThrow<UsageError> { C().parse("foo bar baz") }
            .message shouldBe "Got unexpected extra argument (baz)"
        shouldThrow<UsageError> { C().parse("foo bar baz qux") }
            .message shouldBe "Got unexpected extra arguments (baz qux)"
    }

    @Test
    @JsName("misused_arguments_with_nvalues_3")
    fun `misused arguments with nvalues=3`() {
        class C : TestCommand() {
            val x by argument().triple()
        }

        shouldThrow<IncorrectArgumentValueCount> { C().parse("foo bar") }
            .message shouldBe "argument X requires 3 values"
        shouldThrow<UsageError> { C().parse("foo bar baz qux") }
            .message shouldBe "Got unexpected extra argument (qux)"

    }

    @Test
    @JsName("one_argument_multiple_minus_unique_nvalues_minus_1")
    fun `one argument multiple-unique nvalues=-1`() = forAll(
        row("", emptySet()),
        row("foo foo", setOf("foo")),
        row("foo bar", setOf("foo", "bar"))
    ) { argv, expected ->
        val command = object : TestCommand() {
            val x by argument().multiple().unique()
            override fun run_() {
                x shouldBe expected
            }
        }
        command.parse(argv)
    }

    @Test
    @JsName("one_argument_nvalues_minus_1")
    fun `one argument nvalues=-1`() = forAll(
        row("", emptyList()),
        row("foo", listOf("foo")),
        row("foo bar", listOf("foo", "bar")),
        row("foo bar baz", listOf("foo", "bar", "baz"))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by argument().multiple()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("one_required_argument_nvalues_minus_1")
    fun `one required argument nvalues=-1`() = forAll(
        row("foo", listOf("foo")),
        row("foo bar", listOf("foo", "bar")),
        row("foo bar baz", listOf("foo", "bar", "baz"))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by argument().multiple(required = true)
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("one_required_argument_nvalues_minus_1_empty_argv")
    fun `one required argument nvalues=-1 empty argv`() {
        class C : TestCommand() {
            val x by argument().multiple(required = true)
        }

        shouldThrow<MissingArgument> { C().parse("") }
    }

    @Test
    @JsName("two_arguments_nvalues_minus_1_1")
    fun `two arguments nvalues=-1_1`() = forAll(
        row("foo", emptyList(), "foo"),
        row("foo bar baz", listOf("foo", "bar"), "baz")
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val foo by argument().multiple()
            val bar by argument()
            override fun run_() {
                foo shouldBe ex
                bar shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("two_arguments_nvalues_minus_1_1_empty_argv")
    fun `two arguments nvalues=-1_1 empty argv`() {
        class C : TestCommand(called = false) {
            val foo by argument().multiple()
            val bar by argument()
        }
        shouldThrow<MissingArgument> {
            C().parse("")
        }.message!! should contain("BAR")
    }

    @Test
    @JsName("two_arguments_nvalues_1_minus_1")
    fun `two arguments nvalues=1_-1`() = forAll(
        row("", null, emptyList()),
        row("foo bar", "foo", listOf("bar")),
        row("foo bar baz", "foo", listOf("bar", "baz"))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val foo by argument().optional()
            val bar by argument().multiple()
            override fun run_() {
                foo shouldBe ex
                bar shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("two_arguments_nvalues_1_minus_1_empty_argv")
    fun `two arguments nvalues=1_-1 empty argv`() {
        class C : TestCommand(called = false) {
            val foo by argument()
            val bar by argument().multiple()
        }

        val ex = shouldThrow<MissingArgument> { C().parse("") }
        ex.message!! should contain("Missing argument \"FOO\"")
    }

    @Test
    @JsName("value_minus_minus_with_argument")
    fun `value -- with argument`() = forAll(
        row("--xx --xx -- --xx", "--xx", "--xx")
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            val y by argument()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("argument_validator_non_minus_null")
    fun `argument validator non-null`() {
        var called = false

        class C : TestCommand() {
            val x: String by argument().validate {
                called = true
                require(it == "foo")
            }
        }

        C().parse("foo")
        called shouldBe true

        shouldThrow<MissingArgument> { C().parse("") }
    }

    @Test
    @JsName("argument_validator_nullable")
    fun `argument validator nullable`() {
        var called = false

        class C : TestCommand() {
            val x: String? by argument().optional().validate {
                called = true
                require(it == "foo")
            }
        }

        C().parse("foo")
        called shouldBe true

        called = false
        C().parse("")
        called shouldBe false
    }

    @Test
    @JsName("argument_check")
    fun `argument check`() = forAll(
        row("bar foo", "Invalid value for \"X\": bar"),
        row("foo bar", "Invalid value for \"Y\": fail bar"),
        row("foo foo bar", "Invalid value for \"Z\": bar"),
        row("foo foo foo bar", "Invalid value for \"W\": fail bar")
    ) { argv, message ->
        class C : TestCommand() {
            val x by argument().check { it == "foo" }
            val y by argument().check(lazyMessage = { "fail $it" }) { it == "foo" }

            val z by argument().optional().check { it == "foo" }
            val w by argument().optional().check(lazyMessage = { "fail $it" }) { it == "foo" }
        }

        shouldThrow<BadParameterValue> { C().parse(argv) }.message shouldBe message
    }

    @Test
    @JsName("eager_option_with_required_argument_not_given")
    fun `eager option with required argument not given`() {
        class C : TestCommand(called = false) {
            val x by argument()
        }

        shouldThrow<PrintHelpMessage> { C().parse("--help") }
    }

    @Test
    @JsName("allowInterspersedArgs_true")
    fun `allowInterspersedArgs=true`() {
        class C : TestCommand() {
            val x by argument()
            val y by option("-y").counted()
            val z by argument()
        }

        C().context { allowInterspersedArgs = true }.apply {
            parse("-y 1 -y 2 -y")
            x shouldBe "1"
            y shouldBe 3
            z shouldBe "2"
        }
    }

    @Test
    @JsName("allowInterspersedArgs_false")
    fun `allowInterspersedArgs=false`() {
        class C : TestCommand() {
            val x by argument()
            val y by option("-y").counted()
            val z by argument()
        }

        C().context { allowInterspersedArgs = false }.apply {
            parse("-y 1 -y")
            x shouldBe "1"
            y shouldBe 1
            z shouldBe "-y"
        }
    }

    @Test
    @JsName("convert_catches_exceptions")
    fun `convert catches exceptions`() {
        class C : TestCommand() {
            val x by argument().convert {
                when (it) {
                    "uerr" -> fail("failed")
                    "err" -> throw NumberFormatException("failed")
                }
                it
            }
        }

        var ex = shouldThrow<BadParameterValue> { C().parse("uerr") }
        ex.argument shouldNotBe null
        ex.argument?.name shouldBe "X"

        ex = shouldThrow { C().parse("err") }
        ex.argument shouldNotBe null
        ex.argument?.name shouldBe "X"
    }

    @Test
    @JsName("multiple_args_with_nvalues_minus_1")
    fun `multiple args with nvalues=-1`() {
        class C : TestCommand(called = false) {
            val foo by argument().multiple()
            val bar by argument().multiple()
        }
        shouldThrow<IllegalArgumentException> { C() }
    }

    @Test
    @JsName("argument_with_default")
    fun `argument with default`() {
        val default = listOf("def")

        class C : TestCommand() {
            val foo by argument().multiple(default = default)
        }
        C().parse("").foo shouldBe listOf("def")
        C().parse("a b").foo shouldBe listOf("a", "b")
    }

    @Test
    @JsName("punctuation_in_arg_prefix_unix_style")
    fun `punctuation in arg prefix unix style`() = forAll(
        row("/foo")
    ) { argv ->
        class C : TestCommand() {
            val x by argument()
            override fun run_() {
                x shouldBe argv
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("punctuation_in_arg_prefix_unix_style_error")
    fun `punctuation in arg prefix unix style error`() {
        class C : TestCommand(called = false) {
            val x by argument()
        }
        shouldThrow<NoSuchOption> { C().parse("-foo") }
    }

    @Test
    @JsName("punctuation_in_arg_prefix_windows_style")
    fun `punctuation in arg prefix windows style`() = forAll(
        row("-foo"),
        row("--foo")
    ) { argv ->
        class C : TestCommand() {
            init {
                context { helpOptionNames = setOf("/help") }
            }

            val x by argument()
            override fun run_() {
                x shouldBe argv
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("punctuation_in_arg_prefix_windows_style_error")
    fun `punctuation in arg prefix windows style error`() {
        class C : TestCommand(called = false) {
            init {
                context { helpOptionNames = setOf("/help") }
            }

            val x by argument()
        }
        shouldThrow<NoSuchOption> { C().parse("/foo") }
    }

    @Test
    @JsName("wrapValue_argument")
    fun `chained convert argument`() = forAll(
        row("", null),
        row("1", listOf(1))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by argument().int().convert { listOf(it) }.optional()
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }
}