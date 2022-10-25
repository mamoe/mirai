package net.mamoe.mirai.clikt.parameters.types

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.parameters.arguments.argument
import net.mamoe.mirai.clikt.parameters.arguments.multiple
import net.mamoe.mirai.clikt.parameters.arguments.optional
import net.mamoe.mirai.clikt.parameters.options.default
import net.mamoe.mirai.clikt.parameters.options.option
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test

class ChoiceTypeTest {
    private enum class TestEnum { A, B }

    @Test
    @JsName("choice_option_strings")
    fun `choice option strings`() {
        class C : TestCommand() {
            val x by option("-x", "--xx").choice("foo", "bar")
        }

        C().apply {
            parse("-xfoo")
            x shouldBe "foo"
        }

        C().apply {
            parse("--xx=bar")
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse("--xx baz") }
            .message shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("--xx FOO") }
            .message shouldBe "Invalid value for \"--xx\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_option_map")
    fun `choice option map`() {
        class C : TestCommand() {
            val x by option("-x", "--xx")
                .choice("foo" to 1, "bar" to 2)
        }

        C().apply {
            parse("-xfoo")
            x shouldBe 1
        }

        C().apply {
            parse("--xx=bar")
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("-x baz") }
            .message shouldBe "Invalid value for \"-x\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("--xx=baz") }
            .message shouldBe "Invalid value for \"--xx\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("-x FOO") }
            .message shouldBe "Invalid value for \"-x\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_option_insensitive")
    fun `choice option insensitive`() {
        class C : TestCommand() {
            val x by option("-x").choice("foo", "bar", ignoreCase = true)
            val y by option("-y").choice("foo" to 1, "bar" to 2, ignoreCase = true)
        }

        C().apply {
            parse("-xFOO -yFOO")
            x shouldBe "foo"
            y shouldBe 1
        }

        C().apply {
            parse("-xbar -ybAR")
            x shouldBe "bar"
            y shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("-xbaz") }
            .message shouldBe "Invalid value for \"-x\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_argument_strings")
    fun `choice argument strings`() {
        class C : TestCommand() {
            val x by argument().choice("foo", "bar")
            override fun run_() {
                registeredArguments()[0].name shouldBe "X"
            }
        }

        C().apply {
            parse("foo")
            x shouldBe "foo"
        }

        C().apply {
            parse("bar")
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse("baz") }
            .message shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("FOO") }
            .message shouldBe "Invalid value for \"X\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_argument_map")
    fun `choice argument map`() {
        class C : TestCommand() {
            val x by argument().choice("foo" to 1, "bar" to 2)
            override fun run_() {
                registeredArguments()[0].name shouldBe "X"
            }
        }

        C().apply {
            parse("foo")
            x shouldBe 1
        }

        C().apply {
            parse("bar")
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("baz") }
            .message shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("FOO") }
            .message shouldBe "Invalid value for \"X\": invalid choice: FOO. (choose from foo, bar)"
    }

    @Test
    @JsName("choice_argument_insensitive")
    fun `choice argument insensitive`() {
        class C : TestCommand() {
            val x by argument().choice("foo", "bar", ignoreCase = true)
            val y by argument().choice("foo" to 1, "bar" to 2, ignoreCase = true)
        }

        C().apply {
            parse("FOO FOO")
            x shouldBe "foo"
            y shouldBe 1
        }

        C().apply {
            parse("bar bAR")
            x shouldBe "bar"
            y shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("baz qux") }
            .message shouldBe "Invalid value for \"X\": invalid choice: baz. (choose from foo, bar)"
    }

    @Test
    @JsName("enum_option")
    fun `enum option`() = forAll(
        row("", null),
        row("--xx A", TestEnum.A),
        row("--xx a", TestEnum.A),
        row("--xx=A", TestEnum.A),
        row("-xB", TestEnum.B),
        row("-xb", TestEnum.B)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").enum<TestEnum>()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_option_key")
    fun `enum option key`() = forAll(
        row("", null),
        row("-xAz", TestEnum.A),
        row("-xaZ", TestEnum.A),
        row("-xBz", TestEnum.B),
        row("-xBZ", TestEnum.B)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x").enum<TestEnum> { it.name + "z" }
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_option_error")
    fun `enum option error`() {
        @Suppress("unused")
        class C : TestCommand() {
            val foo by option().enum<TestEnum>(ignoreCase = false)
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .message shouldBe "Invalid value for \"--foo\": invalid choice: bar. (choose from A, B)"

        shouldThrow<BadParameterValue> { C().parse("--foo a") }
            .message shouldBe "Invalid value for \"--foo\": invalid choice: a. (choose from A, B)"
    }

    @Test
    @JsName("enum_option_with_default")
    fun `enum option with default`() = forAll(
        row("", TestEnum.B),
        row("--xx A", TestEnum.A),
        row("--xx=A", TestEnum.A),
        row("-xA", TestEnum.A)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").enum<TestEnum>().default(TestEnum.B)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("enum_argument")
    fun `enum argument`() = forAll(
        row("", null, emptyList()),
        row("A", TestEnum.A, emptyList()),
        row("b", TestEnum.B, emptyList()),
        row("A a B", TestEnum.A, listOf(TestEnum.A, TestEnum.B))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().enum<TestEnum>().optional()
            val y by argument().enum<TestEnum>().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_argument_key")
    fun `enum argument key`() = forAll(
        row("", emptyList()),
        row("az", listOf(TestEnum.A)),
        row("AZ", listOf(TestEnum.A)),
        row("aZ Bz", listOf(TestEnum.A, TestEnum.B))
    ) { argv, ex ->
        class C : TestCommand() {
            val x by argument().enum<TestEnum> { it.name + "z" }.multiple()
            override fun run_() {
                x shouldBe ex
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("enum_argument_error")
    fun `enum argument error`() {
        @Suppress("unused")
        class C : TestCommand() {
            val foo by argument().enum<TestEnum>(ignoreCase = false)
        }

        shouldThrow<BadParameterValue> { C().parse("bar") }
            .message shouldBe "Invalid value for \"FOO\": invalid choice: bar. (choose from A, B)"

        shouldThrow<BadParameterValue> { C().parse("a") }
            .message shouldBe "Invalid value for \"FOO\": invalid choice: a. (choose from A, B)"
    }
}