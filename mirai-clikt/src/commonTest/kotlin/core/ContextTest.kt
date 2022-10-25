package net.mamoe.mirai.clikt.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test

class ContextTest {
    class Foo

    @Test
    @JsName("find_functions_single_context")
    fun `find functions single context`() {
        class C : TestCommand() {
            val o1 by findObject<String>()
            val o2 by findOrSetObject { "foo" }
            val o3 by findObject<String>()
            val o4 by findObject<Int>()

            override fun run_() {
                currentContext.findRoot() shouldBe currentContext
            }
        }

        val c = C().apply { runBlocking { parse(emptyArray()) } }

        c.o1 shouldBe null
        c.o2 shouldBe "foo"
        c.o3 shouldBe "foo"
        c.o4 shouldBe null
    }

    @Test
    @JsName("find_functions_parent_context")
    fun `find functions parent context`() {
        val foo = Foo()

        class C : TestCommand(invokeWithoutSubcommand = true) {
            val o1 by findObject<Foo>()
            val o2 by findOrSetObject { foo }
            val o3 by findObject<Foo>()
            val o4 by findObject<Int>()

            override fun run_() {
                currentContext.findRoot() shouldBe currentContext
            }
        }

        val child = C()
        val parent = C().subcommands(child).apply { runBlocking { parse(emptyArray()) } }
        parent.o1 shouldBe child.o1
        parent.o1 shouldBe null
        parent.o2 shouldBe child.o2
        parent.o2 shouldBe foo
        parent.o3 shouldBe child.o3
        parent.o3 shouldBe foo
        parent.o4 shouldBe child.o4
        parent.o4 shouldBe null
    }

    @Test
    @JsName("requireObject_with_parent_context")
    fun `requireObject with parent context`() {
        class C : TestCommand(invokeWithoutSubcommand = true) {
            val o1 by findOrSetObject { Foo() }
            val o2 by requireObject<Foo>()
        }

        val child = C()
        val parent = C().subcommands(child).apply { runBlocking { parse(emptyArray()) } }

        shouldThrow<NullPointerException> { parent.o2 }
        shouldThrow<NullPointerException> { child.o2 }

        parent.o1 should beInstanceOf(Foo::class)
        parent.o2 shouldBeSameInstanceAs parent.o1
        child.o1 shouldBeSameInstanceAs parent.o1
        child.o2 shouldBeSameInstanceAs parent.o1
    }

    @Test
    @JsName("default_help_option_names")
    fun `default help option names`() {
        class C : TestCommand()

        shouldThrow<PrintHelpMessage> { C().parse("--help") }
        shouldThrow<PrintHelpMessage> { C().parse("-h") }
        shouldThrow<PrintHelpMessage> {
            C().context { helpOptionNames = setOf("-x") }.parse("-x")
        }
        shouldThrow<NoSuchOption> {
            C().context { helpOptionNames = setOf("--x") }.parse("--help")
        }
    }
}