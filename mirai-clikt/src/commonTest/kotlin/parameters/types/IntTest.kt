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
class IntTypeTest {
    @Test
    @JsName("int_option")
    fun `int option`() = forAll(
        row("", null),
        row("--xx=4", 4),
        row("-x5", 5)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").int()
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("int_option_error")
    fun `int option error`() {
        class C : TestCommand(called = false) {
            val foo by option().int()
        }

        shouldThrow<BadParameterValue> { C().parse("--foo bar") }
            .message shouldBe "Invalid value for \"--foo\": bar is not a valid integer"
    }

    @Test
    @JsName("int_option_with_default")
    fun `int option with default`() = forAll(
        row("", 111),
        row("--xx=4", 4),
        row("-x5", 5)
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").int().default(111)
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("int_argument")
    fun `int argument`() = forAll(
        row("", null, emptyList()),
        row("1 2", 1, listOf(2)),
        row("1 2 3", 1, listOf(2, 3))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by argument().int().optional()
            val y by argument().int().multiple()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }
}