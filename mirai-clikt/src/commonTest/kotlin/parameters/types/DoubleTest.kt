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

@Suppress("unused")
class DoubleTest {
    @Test
    @JsName("double_option")
    fun `double option`() = forAll(
        row("", null),
        row("--xx 3", 3.0),
        row("--xx=4.0", 4.0),
        row("-x5.5", 5.5)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").double()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("double_option_error")
    fun `double option error`() {
        class C : TestCommand() {
            val foo by option().double()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .message shouldBe "Invalid value for \"--foo\": bar is not a valid floating point value"
    }

    @Test
    @JsName("double_option_with_default")
    fun `double option with default`() = forAll(
        row("", -1.0),
        row("--xx=4.0", 4.0),
        row("-x5.5", 5.5)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").double().default(-1.0)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("double_argument")
    fun `double argument`() = forAll(
        row("", null, emptyList<Float>()),
        row("1.1 2", 1.1, listOf(2.0)),
        row("1.1 2 3", 1.1, listOf(2.0, 3.0))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().double().optional()
            val y by argument().double().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }
}