package net.mamoe.mirai.clikt.parameters

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import net.mamoe.mirai.clikt.core.Abort
import net.mamoe.mirai.clikt.core.PrintHelpMessage
import net.mamoe.mirai.clikt.core.PrintMessage
import net.mamoe.mirai.clikt.parameters.options.eagerOption
import net.mamoe.mirai.clikt.parameters.options.versionOption
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("BooleanLiteralArgument")
class EagerOptionsTest {
    @Test
    @JsName("custom_eager_option")
    fun `custom eager option`() = forAll(
        row("", false, false),
        row("--option", true, false),
        row("-p", false, true),
        row("-op", true, true)
    ) { argv, eO, eP ->
        var calledO = false
        var calledP = false

        class C : TestCommand() {
            init {
                eagerOption("-o", "--option") { calledO = true }
                eagerOption(listOf("-p")) { calledP = true }
            }
        }

        with(C()) {
            parse(argv)
            calledO shouldBe eO
            calledP shouldBe eP
        }
    }

    @Test
    @JsName("custom_eager_option_throwing_abort")
    fun `custom eager option throwing abort`() {
        class C : TestCommand(called = false)

        shouldThrow<Abort> {
            C().eagerOption("--foo") { throw Abort(error = false) }
                .parse("--foo")
        }.error shouldBe false
    }

    @Test
    @JsName("version_default")
    fun `version default`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        shouldThrow<PrintMessage> {
            C().parse("--version")
        }.message shouldBe "prog version 1.2.3"
    }

    @Test
    @JsName("version_custom_message")
    fun `version custom message`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3", names = setOf("--foo")) { "$it bar" }
            }
        }

        shouldThrow<PrintMessage> {
            C().parse("--foo")
        }.message shouldBe "1.2.3 bar"
    }

    @Test
    @JsName("multiple_eager_options")
    fun `multiple eager options`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        shouldThrow<PrintHelpMessage> {
            C().parse("--help --version")
        }

        shouldThrow<PrintMessage> {
            C().parse("--version --help")
        }.message shouldBe "prog version 1.2.3"
    }
}