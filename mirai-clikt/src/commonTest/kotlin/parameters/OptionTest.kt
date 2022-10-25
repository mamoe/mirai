package net.mamoe.mirai.clikt.parameters

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.clikt.core.*
import net.mamoe.mirai.clikt.parameters.arguments.argument
import net.mamoe.mirai.clikt.parameters.arguments.default
import net.mamoe.mirai.clikt.parameters.options.*
import net.mamoe.mirai.clikt.parameters.types.int
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertTrue

@Suppress("unused", "BooleanLiteralArgument")
class OptionTest {
    @Test
    @JsName("zero_options")
    fun `zero options`() {
        runBlocking {
            TestCommand(called = true).parse(arrayOf())
        }
    }

    @Test
    @JsName("no_such_option")
    fun `no such option`() = forAll(
        row("--qux", "no such option: \"--qux\""),
        row("--fo", "no such option: \"--fo\". Did you mean \"--foo\"?"),
        row("--fop", "no such option: \"--fop\". Did you mean \"--foo\"?"),
        row("--car", "no such option: \"--car\". Did you mean \"--bar\"?"),
        row("--ba", "no such option: \"--ba\". (Possible options: --bar, --baz)")
    ) { argv, message ->
        class C : TestCommand(called = false) {
            val foo by option()
            val bar by option()
            val baz by option()
            val fob by option(hidden = true)
        }

        shouldThrow<NoSuchOption> {
            C().parse(argv)
        }.message shouldBe message
    }

    @Test
    @JsName("no_such_short_option_with_long")
    fun `no such short option with long`() = forAll(
        row("-long", "no such option: \"-l\". Did you mean \"--long\"?"),
        row("-foo", "no such option: \"-f\". Did you mean \"--foo\"?"),
        row("-oof", "no such option: \"-f\". Did you mean \"--oof\"?"),
    ) { argv, message ->
        class C : TestCommand(called = false) {
            val short by option("-o").flag()
            val long by option()
            val foo by option()
            val oof by option()
        }

        shouldThrow<NoSuchOption> {
            C().parse(argv)
        }.message shouldBe message
    }

    @Test
    @JsName("one_option")
    fun `one option`() = forAll(
        row("", null),
        row("--xx 3", "3"),
        row("--xx --xx", "--xx"),
        row("--xx=asd", "asd"),
        row("-x 4", "4"),
        row("-x -x", "-x"),
        row("-xfoo", "foo"),
        row("-x a=b", "a=b"),
        row("-xa=b", "a=b"),
        row("--xx a=b", "a=b"),
        row("--xx=a=b", "a=b")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("two_options_one_name_each")
    fun `two options one name each`() {
        class C : TestCommand() {
            val x by option("-x")
            val y by option("--yy")
            override fun run_() {
                x shouldBe "3"
                y shouldBe "4"
            }
        }
        C().parse("-x 3 --yy 4")
    }

    @Test
    @JsName("two_options")
    fun `two options`() = forAll(
        row("--xx 3 --yy 4", "3", "4"),
        row("-x 3 --yy 4", "3", "4"),
        row("-x3 --yy 4", "3", "4"),
        row("--xx 3 -y4", "3", "4"),
        row("--xx=3 --yy=4", "3", "4"),
        row("-x3 --yy=4", "3", "4"),
        row("-x 3 -y 4", "3", "4"),
        row("-x3 -y 4", "3", "4"),
        row("-x 3 -y4", "3", "4"),
        row("-x3 -y4", "3", "4"),
        row("--yy 4", null, "4"),
        row("--yy=4", null, "4"),
        row("-y 4", null, "4"),
        row("-y4", null, "4"),
        row("--xx 3", "3", null),
        row("--xx=3", "3", null),
        row("-x 3", "3", null),
        row("-x3", "3", null)
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }


    @Test
    @JsName("two_options_nvalues_2")
    fun `two options nvalues=2`() = forAll(
        row("", null, null),
        row("--yy 5 7", null, "5" to "7"),
        row("--xx 1 3 --yy 5 7", "1" to "3", "5" to "7"),
        row("--xx 1 3 -y 5 7", "1" to "3", "5" to "7"),
        row("-x 1 3 --yy 5 7", "1" to "3", "5" to "7"),
        row("-x1 3 --yy 5 7", "1" to "3", "5" to "7"),
        row("--xx 1 3 -y5 7", "1" to "3", "5" to "7"),
        row("--xx=1 3 --yy=5 7", "1" to "3", "5" to "7"),
        row("-x1 3 --yy=5 7", "1" to "3", "5" to "7"),
        row("-x 1 3 -y 5 7", "1" to "3", "5" to "7"),
        row("-x1 3 -y 5 7", "1" to "3", "5" to "7"),
        row("-x 1 3 -y5 7", "1" to "3", "5" to "7"),
        row("-x1 3 -y5 7", "1" to "3", "5" to "7")
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx").pair()
            val y by option("-y", "--yy").pair()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("two_options_nvalues_3")
    fun `two options nvalues=3`() {
        val xvalue = Triple("1", "2", "3")
        val yvalue = Triple("5", "6", "7")
        forAll(
            row("", null, null),
            row("--yy 5 6 7", null, yvalue),
            row("--xx 1 2 3 --yy 5 6 7", xvalue, yvalue),
            row("--xx 1 2 3 -y 5 6 7", xvalue, yvalue),
            row("-x 1 2 3 --yy 5 6 7", xvalue, yvalue),
            row("-x1 2 3 --yy 5 6 7", xvalue, yvalue),
            row("--xx 1 2 3 -y5 6 7", xvalue, yvalue),
            row("--xx=1 2 3 --yy=5 6 7", xvalue, yvalue),
            row("-x1 2 3 --yy=5 6 7", xvalue, yvalue),
            row("-x 1 2 3 -y 5 6 7", xvalue, yvalue),
            row("-x1 2 3 -y 5 6 7", xvalue, yvalue),
            row("-x 1 2 3 -y5 6 7", xvalue, yvalue),
            row("-x1 2 3 -y5 6 7", xvalue, yvalue)
        ) { argv, ex, ey ->
            class C : TestCommand() {
                val x by option("-x", "--xx").triple()
                val y by option("-y", "--yy").triple()
                override fun run_() {
                    x shouldBe ex
                    y shouldBe ey
                }
            }

            C().parse(argv)
        }
    }

    @Test
    @JsName("two_options_nvalues_2_usage_errors")
    fun `two options nvalues=2 usage errors`() {
        class C : TestCommand(called = false) {
            val x by option("-x", "--xx").pair()
            val y by option("-y", "--yy").pair()
        }
        shouldThrow<IncorrectOptionValueCount> { C().parse("-x") }.message shouldBe
                "option -x requires 2 values"
        shouldThrow<UsageError> { C().parse("--yy foo bar baz") }.message shouldBe
                "Got unexpected extra argument (baz)"
    }

    @Test
    @JsName("two_options_with_split")
    fun `two options with split`() = forAll(
        row("", null, null),
        row("-x 5 -y a", listOf(5), listOf("a")),
        row("-x 5,6 -y a:b", listOf(5, 6), listOf("a", "b"))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x").int().split(",")
            val y by option("-y").split(Regex(":"))
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("flag_options")
    fun `flag options`() = forAll(
        row("", false, false, null),
        row("-xx", true, false, null),
        row("-xX", false, false, null),
        row("-Xx", true, false, null),
        row("-x --no-xx", false, false, null),
        row("--xx", true, false, null),
        row("--no-xx", false, false, null),
        row("--no-xx --xx", true, false, null),
        row("-y", false, true, null),
        row("--yy", false, true, null),
        row("-xy", true, true, null),
        row("-yx", true, true, null),
        row("-x -y", true, true, null),
        row("--xx --yy", true, true, null),
        row("-x -y -z foo", true, true, "foo"),
        row("--xx --yy --zz foo", true, true, "foo"),
        row("-xy -z foo", true, true, "foo"),
        row("-xyzxyz", true, true, "xyz"),
        row("-xXyzXyz", false, true, "Xyz"),
        row("-xzfoo", true, false, "foo")
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option("-x", "--xx").flag("-X", "--no-xx")
            val y by option("-y", "--yy").flag()
            val z by option("-z", "--zz")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("flag_convert")
    fun `flag convert`() = forAll(
        row("", E.B, "false"),
        row("-xx", E.A, "false"),
        row("-xX", E.B, "false"),
        row("-Xx", E.A, "false"),
        row("-x --no-xx", E.B, "false"),
        row("--xx", E.A, "false"),
        row("--no-xx", E.B, "false"),
        row("--no-xx --xx", E.A, "false"),
        row("-y", E.B, "true"),
        row("--yy", E.B, "true"),
        row("-xy", E.A, "true"),
        row("-yx", E.A, "true"),
        row("-x -y", E.A, "true"),
        row("--xx --yy", E.A, "true")
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx").flag("-X", "--no-xx")
                .convert { if (it) E.A else E.B }
            val y by option("-y", "--yy").flag()
                .convert { it.toString() }

            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("flag_convert_validate")
    fun `flag convert validate`() {
        class C : TestCommand() {
            val x by option().flag("--no-x")
                .convert { if (it) E.A else E.B }
                .validate { require(it == E.A) }
        }

        C().parse("--x").x shouldBe E.A
        shouldThrow<UsageError> { C().parse("--no-x") }
    }

    @Test
    @JsName("switch_options")
    fun `switch options`() = forAll(
        row("", null, -1, -2),
        row("-xyz", 1, 3, 5),
        row("--xx -yy -zz", 2, 4, 6),
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option().switch("-x" to 1, "--xx" to 2)
            val y by option().switch("-y" to 3, "-yy" to 4).default(-1)
            val z by option().switch("-z" to 5, "-zz" to 6).defaultLazy { -2 }
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("required_switch_options")
    fun `required switch options`() {
        class C : TestCommand() {
            val x by option().switch("-x" to 1, "-xx" to 2).required()
        }

        C().parse("-x").x shouldBe 1
        C().parse("-xx").x shouldBe 2
        shouldThrow<MissingOption> { C().parse("") }
    }

    @Test
    @JsName("counted_options")
    fun `counted options`() = forAll(
        row("", 0, false, null),
        row("-x -x", 2, false, null),
        row("-xx", 2, false, null),
        row("-xx -xx", 4, false, null),
        row("--xx -y --xx", 2, true, null),
        row("--xx", 1, false, null),
        row("-y", 0, true, null),
        row("--yy", 0, true, null),
        row("-xy", 1, true, null),
        row("-yx", 1, true, null),
        row("-x -y", 1, true, null),
        row("--xx --yy", 1, true, null),
        row("-x -y -z foo", 1, true, "foo"),
        row("--xx --yy --zz foo", 1, true, "foo"),
        row("-xy -z foo", 1, true, "foo"),
        row("-xyx", 2, true, null),
        row("-xyxzxyz", 2, true, "xyz"),
        row("-xyzxyz", 1, true, "xyz"),
        row("-xzfoo", 1, false, "foo")
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option("-x", "--xx").counted()
            val y by option("-y", "--yy").flag()
            val z by option("-z", "--zz")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("default_option")
    fun `default option`() = forAll(
        row("", "def"),
        row("-x4", "4")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").default("def")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("defaultLazy_option")
    fun `defaultLazy option`() = forAll(
        row("", "default", true),
        row("-xbar", "bar", false)
    ) { argv, expected, ec ->
        var called = false

        class C : TestCommand() {
            val x by option("-x", "--x").defaultLazy { called = true; "default" }
            override fun run_() {
                x shouldBe expected
                called shouldBe ec
            }
        }

        called shouldBe false
        C().parse(argv)
    }

    @Test
    @JsName("defaultLazy_option_referencing_other_option")
    fun `defaultLazy option referencing other option`() {
        class C : TestCommand() {
            val y by option().defaultLazy { x }
            val x by option().default("def")
            override fun run_() {
                y shouldBe "def"
            }
        }

        C().parse("")
    }

    @Test
    @JsName("defaultLazy_option_referencing_argument")
    fun `defaultLazy option referencing argument`() {
        class C : TestCommand() {
            val y by option().defaultLazy { x }
            val x by argument().default("def")
            override fun run_() {
                y shouldBe "def"
            }
        }

        C().parse("")
    }

    @Test
    @JsName("required_option")
    fun `required option`() {
        class C : TestCommand() {
            val x by option().required()
            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().parse("--x=foo")

        shouldThrow<MissingOption> {
            C().parse("")
        }.message shouldBe "Missing option \"--x\""
    }

    @Test
    @JsName("multiple_option_default")
    fun `multiple option default`() {
        class C : TestCommand() {
            val x by option().multiple()
            override fun run_() {
                x shouldBe listOf()
            }
        }

        C().parse("")
    }

    @Test
    @JsName("multiple_option_custom_default")
    fun `multiple option custom default`() {
        class C : TestCommand() {
            val x by option().multiple(listOf("foo"))
            override fun run_() {
                x shouldBe listOf("foo")
            }
        }

        C().parse("")
    }

    @Test
    @JsName("multiple_with_unique_option_default")
    fun `multiple with unique option default`() {
        val command = object : TestCommand() {
            val x by option().multiple().unique()
            override fun run_() {
                x shouldBe emptySet()
            }
        }

        command.parse("")
    }

    @Test
    @JsName("multiple_with_unique_option_custom_default")
    fun `multiple with unique option custom default`() {
        val command = object : TestCommand() {
            val x by option().multiple(listOf("foo", "bar", "bar")).unique()
            override fun run_() {
                x shouldBe setOf("foo", "bar")
            }
        }

        command.parse("")
    }

    @Test
    @JsName("multiple_with_unique_option_parsed")
    fun `multiple with unique option parsed`() = forAll(
        row("--arg foo", setOf("foo")),
        row("--arg foo --arg bar --arg baz", setOf("foo", "bar", "baz")),
        row("--arg foo --arg foo --arg foo", setOf("foo"))
    ) { argv, expected ->
        val command = object : TestCommand() {
            val arg by option().multiple().unique()
            override fun run_() {
                arg shouldBe expected
            }
        }
        command.parse(argv)
    }

    @Test
    @JsName("split_with_unique_option_default")
    fun `split with unique option default`() {
        val command = object : TestCommand() {
            val x: Set<String> by option().split(",").default(emptyList()).unique()
            override fun run_() {
                x shouldBe setOf("1", "2")
            }
        }

        command.parse("--x=1,2,1")
    }

    @Test
    @JsName("multiple_required_option")
    fun `multiple required option`() {
        class C(called: Boolean) : TestCommand(called) {
            val x by option().multiple(required = true)
        }

        C(true).apply { parse("--x 1"); x shouldBe listOf("1") }
        C(true).apply { parse("--x 2 --x 3"); x shouldBe listOf("2", "3") }

        shouldThrow<MissingOption> { C(false).parse("") }
            .message shouldBe "Missing option \"--x\""
    }

    @Test
    @JsName("option_metavars")
    fun `option metavars`() {
        class C : TestCommand() {
            val x by option()
            val y by option(metavar = "FOO").default("")
            val z by option(metavar = "FOO").convert("BAR") { it }
            val w by option().convert("BAR") { it }
            val u by option().flag()
            override fun run_() {
                registeredOptions().forEach {
                    assertTrue(
                        it is EagerOption || // skip help option
                                "--x" in it.names && it.metavar(currentContext) == "TEXT" ||
                                "--y" in it.names && it.metavar(currentContext) == "FOO" ||
                                "--z" in it.names && it.metavar(currentContext) == "FOO" ||
                                "--w" in it.names && it.metavar(currentContext) == "BAR" ||
                                "--u" in it.names && it.metavar(currentContext) == null,
                        message = "bad option $it"
                    )
                }
            }
        }

        C().parse("")
    }

    @Test
    @JsName("option_validator_basic")
    fun `option validator basic`() {

        var called = false

        class C : TestCommand() {
            val x by option().validate {
                called = true
                require(it == "foo") { "invalid value $it" }
            }
        }

        with(C()) {
            parse("--x=foo")
            x shouldBe "foo"
        }
        called shouldBe true

        called = false
        C().parse("")
        called shouldBe false
    }

    @Test
    @JsName("option_check")
    fun `option check`() = forAll(
        row("--x=bar --y=foo --w=foo", "Invalid value for \"--x\": bar"),
        row("--y=bar --w=foo", "Invalid value for \"--y\": bar"),
        row("--y=foo --z=bar --w=foo", "Invalid value for \"--z\": fail"),
        row("--y=foo --w=bar", "Invalid value for \"--w\": fail bar")
    ) { argv, message ->
        class C : TestCommand() {
            val x by option().check { it == "foo" }
            val y by option().required().check { it == "foo" }

            val z by option().check("fail") { it == "foo" }
            val w by option().required().check(lazyMessage = { "fail $it" }) { it == "foo" }
        }

        shouldThrow<BadParameterValue> { C().parse(argv) }.message shouldBe message
    }

    @Test
    @JsName("option_validator_required")
    fun `option validator required`() {
        var called = false

        class C : TestCommand() {
            val x by option().required().validate {
                called = true
                require(it == "foo") { "invalid value $it" }
            }

            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().parse("--x=foo")
        called shouldBe true

        called = false
        shouldThrow<MissingOption> { C().parse("") }
    }

    @Test
    @JsName("option_validator_flag")
    fun `option validator flag`() {
        var called = false

        class C : TestCommand() {
            val x by option().flag().validate {
                called = true
                require(it)
            }

            override fun run_() {
                x shouldBe true
            }
        }

        C().parse("--x")
        called shouldBe true
    }


    @Test
    @JsName("convert_catches_exceptions")
    fun `convert catches exceptions`() {
        class C : TestCommand(called = false) {
            init {
                context { allowInterspersedArgs = false }
            }

            val x by option().convert {
                when (it) {
                    "uerr" -> fail("failed")
                    "err" -> throw NumberFormatException("failed")
                }
                it
            }
        }

        shouldThrow<BadParameterValue> { C().parse("--x=uerr") }.paramName shouldBe "--x"
        shouldThrow<BadParameterValue> { C().parse("--x=err") }.paramName shouldBe "--x"
    }

    @Test
    @JsName("one_option_with_slash_prefix")
    fun `one option with slash prefix`() = forAll(
        row("", null),
        row("/xx 3", "3"),
        row("/xx=asd", "asd"),
        row("/x 4", "4"),
        row("/x /xx /xx foo", "foo"),
        row("/xfoo", "foo")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("/x", "/xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("one_option_with_java_prefix")
    fun `one option with java prefix`() = forAll(
        row("", null),
        row("-xx 3", "3"),
        row("-xx=asd", "asd"),
        row("-x 4", "4"),
        row("-x -xx -xx foo", "foo"),
        row("-xfoo", "foo")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "-xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("two_options_with_chmod_prefixes")
    fun `two options with chmod prefixes`() = forAll(
        row("", false, false),
        row("-x", false, false),
        row("-x +x", true, false),
        row("+x -x", false, false),
        row("+y", false, true),
        row("-y", false, false),
        row("-y +y", false, true),
        row("+y -y", false, false),
        row("-x -y", false, false),
        row("-x -y +xy", true, true)
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("+x").flag("-x")
            val y by option("+y").flag("-y")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("normalized_tokens")
    fun `normalized tokens`() = forAll(
        row("", null),
        row("--XX=FOO", "FOO"),
        row("--xx=FOO", "FOO"),
        row("-XX", "X")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().context { tokenTransformer = { it.lowercase() } }.parse(argv)
    }

    @Test
    @JsName("aliased_tokens")
    fun `aliased tokens`() = forAll(
        row("", null),
        row("--yy 3", "3")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().context { tokenTransformer = { "--xx" } }.parse(argv)
    }

    @Test
    @JsName("deprecated_warning_options")
    fun `deprecated warning options`() {
        class C : TestCommand() {
            val g by option()
            val f by option().flag().deprecated()
            val x by option().deprecated()
            val y by option().deprecated("warn")
            val z by option().deprecated()
            override fun run_() {
                messages shouldBe listOf(
                    "WARNING: option --f is deprecated",
                    "WARNING: option --x is deprecated",
                    "warn"
                )
            }
        }
        C().parse("--g=0 --f --x=1 --y=2")
    }

    @Test
    @JsName("deprecated_error_option")
    fun `deprecated error option`() {
        class C : TestCommand(called = false) {
            val x by option().flag().deprecated(error = true)
            val y by option().deprecated("err", error = true)
        }
        shouldThrow<CliktError> { C().parse("--x") }
            .message shouldBe "ERROR: option --x is deprecated"

        shouldThrow<CliktError> { C().parse("--y=1") }
            .message shouldBe "err"
    }

    @Test
    @JsName("options_with_chained_convert")
    fun `options with chained convert`() = forAll(
        row("", null),
        row("--x=1", listOf(1))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option(names = arrayOf()).int().convert { listOf(it) }
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("associate_options")
    fun `associate options`() = forAll(
        row("", emptyMap()),
        row("-Xfoo=bar", mapOf("foo" to "bar")),
        row("-Xfoo=bar -X baz=qux", mapOf("foo" to "bar", "baz" to "qux")),
        row("-Xfoo=bar -Xfoo=baz", mapOf("foo" to "baz")),
        row("-Xfoo -Xbaz=qux", mapOf("foo" to "", "baz" to "qux"))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-X").associate()
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("customized_splitPair")
    fun `customized splitPair`() = forAll(
        row("", null),
        row("-Xfoo:1", "foo|1"),
        row("-Xfoo:1 -Xbar:2", "bar|2"),
        row("-Xfoo:1 -Xfoo", "foo|"),
        row("-Xfoo:=", "foo|="),
        row("-Xfoo:1=1", "foo|1=1")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-X").splitPair(":").convert { "${it.first}|${it.second}" }
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }
}


private enum class E { A, B }