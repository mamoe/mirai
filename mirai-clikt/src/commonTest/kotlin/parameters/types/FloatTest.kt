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

class FloatTest {
    @Test
    @JsName("float_option")
    fun `float option`() = forAll(
        row("", null),
        row("--xx=4.0", 4f),
        row("-x5.5", 5.5f)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").float()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("float_option_error")
    fun `float option error`() {
        class C : TestCommand(called = false) {
            val foo by option().float()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .message shouldBe "Invalid value for \"--foo\": bar is not a valid floating point value"
    }

    @Test
    @JsName("float_option_with_default")
    fun `float option with default`() = forAll(
        row("", -1f),
        row("--xx=4.0", 4f),
        row("-x5.5", 5.5f)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").float().default(-1f)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("float_argument")
    fun `float argument`() = forAll(
        row("", null, emptyList()),
        row("1.1 2", 1.1f, listOf(2f)),
        row("1.1 2 3", 1.1f, listOf(2f, 3f))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().float().optional()
            val y by argument().float().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }
}