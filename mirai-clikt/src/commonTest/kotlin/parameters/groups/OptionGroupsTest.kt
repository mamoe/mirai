@file:Suppress("UnusedImport")

package net.mamoe.mirai.clikt.parameters.groups

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith
import net.mamoe.mirai.clikt.core.BadParameterValue
import net.mamoe.mirai.clikt.core.MissingOption
import net.mamoe.mirai.clikt.core.MutuallyExclusiveGroupException
import net.mamoe.mirai.clikt.core.UsageError
import net.mamoe.mirai.clikt.parameters.options.*
import net.mamoe.mirai.clikt.parameters.types.int
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.fail

@Suppress("unused")
class OptionGroupsTest {
    @Test
    @JsName("plain_option_group")
    fun `plain option group`() = forAll(
        row("", null, "d", "d"),
        row("--x=1", "1", "d", "d"),
        row("--y=2", null, "2", "d"),
        row("--x=1 --y=2", "1", "2", "d"),
        row("--x=1 --y=2 --o=3", "1", "2", "3")
    ) { argv, ex, ey, eo ->
        class G : OptionGroup() {
            val x by option()
            val y by option().default("d")
        }

        class C : TestCommand() {
            val g by G()
            val o by option().default("d")

            override fun run_() {
                o shouldBe eo
                g.x shouldBe ex
                g.y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("plain_option_group_with_required_option")
    fun `plain option group with required option`() {
        class G : OptionGroup() {
            val x by option().required()
        }

        class C : TestCommand() {
            val g by G()
            override fun run_() {
                g.x shouldBe "foo"
            }
        }

        C().parse("--x=foo")

        shouldThrow<MissingOption> {
            C().parse("")
        }.message shouldBe "Missing option \"--x\""
    }

    @Test
    @JsName("plain_option_group_duplicate_option_name")
    fun `plain option group duplicate option name`() {
        class G : OptionGroup() {
            val x by option()
        }

        class H : OptionGroup() {
            val x by option()
        }

        class C : TestCommand(called = false) {
            val g by G()
            val h by H()
        }

        shouldThrow<IllegalArgumentException> { C() }
            .message shouldBe "Duplicate option name --x"
    }

    @Test
    @JsName("mutually_exclusive_group")
    fun `mutually exclusive group`() = forAll(
        row("", null, "d"),
        row("--x=1", "1", "d"),
        row("--x=1 --y=2", "2", "d"),
        row("--y=3", "3", "d"),
        row("--x=4 --o=5", "4", "5")
    ) { argv, eg, eo ->
        class C : TestCommand() {
            val o by option().default("d")
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"))

            override fun run_() {
                o shouldBe eo
                g shouldBe eg
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("mutually_exclusive_group_single")
    fun `mutually exclusive group single`() {
        class C(called: Boolean) : TestCommand(called) {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"), option("--z")).single()
        }

        C(true).apply { parse("--x=1") }.g shouldBe "1"
        C(true).apply { parse("--y=1 --y=2") }.g shouldBe "2"

        shouldThrow<MutuallyExclusiveGroupException> { C(false).parse("--x=1 --y=2") }
            .message shouldBe "option --x cannot be used with --y or --z"

        shouldThrow<MutuallyExclusiveGroupException> { C(false).parse("--y=1 --z=2") }
            .message shouldBe "option --x cannot be used with --y or --z"
    }

    @Test
    @JsName("mutually_exclusive_group_validate")
    fun `mutually exclusive group validate`() {
        class C(called: Boolean) : TestCommand(called) {
            val g by mutuallyExclusiveOptions(
                option("--x").convert { Sealed.Sealed1 }.check { true },
                option("--y").convert { Sealed.Sealed2 }.check { false },
            )
        }

        C(true).apply { parse("--x=1") }.g shouldBe Sealed.Sealed1

        shouldThrow<BadParameterValue> { C(false).parse("--y=1") }
            .message should startWith("Invalid value for \"--y\"")
    }

    @Test
    @JsName("multiple_mutually_exclusive_groups")
    fun `multiple mutually exclusive groups`() = forAll(
        row("", null, null),
        row("--x=1", "1", null),
        row("--y=2", "2", null),
        row("--z=3", null, "3"),
        row("--w=4", null, "4"),
        row("--x=5 --w=6", "5", "6")
    ) { argv, eg, eh ->
        class C : TestCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"))
            val h by mutuallyExclusiveOptions(option("--z"), option("--w"))
            override fun run_() {
                g shouldBe eg
                h shouldBe eh
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("mutually_exclusive_group_duplicate_option_name")
    fun `mutually exclusive group duplicate option name`() {
        class C : TestCommand(called = false) {
            val g by mutuallyExclusiveOptions(
                option("--x"),
                option("--x")
            )
        }

        shouldThrow<IllegalArgumentException> { C() }
            .message shouldBe "Duplicate option name --x"
    }

    @Test
    @JsName("mutually_exclusive_group_default")
    fun `mutually exclusive group default`() = forAll(
        row("", "d"),
        row("--x=1", "1"),
        row("--x=2", "2")
    ) { argv, eg ->
        class C : TestCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y")).default("d")

            override fun run_() {
                g shouldBe eg
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("mutually_exclusive_group_required")
    fun `mutually exclusive group required`() {
        class C : TestCommand(called = false) {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y")).required()
        }
        shouldThrow<UsageError> { C().parse("") }
            .message shouldBe "Must provide one of --x, --y"
    }

    @Test
    @JsName("mutually_exclusive_group_default_flag_single")
    fun `mutually exclusive group flag single`() = forAll(
//        row("", false),
//        row("--x", true),
        row("--y", true)
    ) { argv, eg ->
        class C : TestCommand() {
            val g by mutuallyExclusiveOptions(
                option("--x").flag(),
                option("--y").flag()
            ).single()

            override fun run_() {
                g shouldBe eg
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("co_occurring_option_group")
    fun `co-occurring option group`() = forAll(
        row("", false, null, null),
        row("--x=1", true, "1", null),
        row("--x=1 --y=2", true, "1", "2")
    ) { argv, eg, ex, ey ->
        class G : OptionGroup() {
            val x by option().required()
            val y by option()
        }

        class C : TestCommand() {
            val g by G().cooccurring()

            override fun run_() {
                if (eg) {
                    g?.x shouldBe ex
                    g?.y shouldBe ey
                } else {
                    g shouldBe null
                }
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("co_occurring_option_group_enforcement")
    fun `co-occurring option group enforcement`() {
        class GGG : OptionGroup() {
            val x by option().required()
            val y by option()
        }

        class C : TestCommand(called = false) {
            val g by GGG().cooccurring()
        }

        shouldThrow<UsageError> { C().parse("--y=2") }
            .message shouldBe "Missing option \"--x\""
    }

    @Test
    @JsName("co_occurring_option_group_with_no_required_options")
    fun `co-occurring option group with no required options`() {
        class GGG : OptionGroup() {
            val x by option()
            val y by option()
        }

        class C : TestCommand(called = false) {
            val g by GGG().cooccurring()
        }

        shouldThrow<IllegalArgumentException> { C() }
            .message shouldBe "At least one option in a co-occurring group must use `required()`"
    }

    @Test
    @JsName("choice_group")
    fun `choice group`() {
        class C : TestCommand() {
            val g by option().groupChoice("1" to Group1(), "2" to Group2())
        }
        forAll(
            row("", 0, null, null),
            row("--g11=1 --g21=1", 0, null, null),
            row("--g=1 --g11=2", 1, 2, null),
            row("--g=1 --g11=2 --g12=3", 1, 2, 3),
            row("--g=1 --g11=2 --g12=3", 1, 2, 3),
            row("--g=2 --g21=2 --g22=3", 2, 2, 3),
            row("--g=2 --g11=2 --g12=3 --g21=2 --g22=3", 2, 2, 3)
        ) { argv, eg, eg1, eg2 ->
            with(C()) {
                parse(argv)
                when (eg) {
                    0 -> {
                        g shouldBe null
                    }

                    1 -> {
                        (g as Group1).g11 shouldBe eg1
                        (g as Group1).g12 shouldBe eg2
                    }

                    2 -> {
                        (g as Group2).g21 shouldBe eg1
                        (g as Group2).g22 shouldBe eg2
                    }
                }
            }
        }

        shouldThrow<BadParameterValue> { C().parse("--g=3") }
            .message shouldBe "Invalid value for \"--g\": invalid choice: 3. (choose from 1, 2)"
    }

    @Test
    @JsName("switch_group")
    fun `switch group`() {
        class C : TestCommand() {
            val g by option().groupSwitch("--a" to Group1(), "--b" to Group2())
        }
        forAll(
            row("", 0, null, null),
            row("--g11=1 --g21=1", 0, null, null),
            row("--a --g11=2", 1, 2, null),
            row("--a --g11=2 --g12=3", 1, 2, 3),
            row("--a --g11=2 --g12=3", 1, 2, 3),
            row("--b --g21=2 --g22=3", 2, 2, 3),
            row("--b --g11=2 --g12=3 --g21=2 --g22=3", 2, 2, 3)
        ) { argv, eg, eg1, eg2 ->
            with(C()) {
                parse(argv)
                when (eg) {
                    0 -> {
                        g shouldBe null
                    }

                    1 -> {
                        (g as Group1).g11 shouldBe eg1
                        (g as Group1).g12 shouldBe eg2
                    }

                    2 -> {
                        (g as Group2).g21 shouldBe eg1
                        (g as Group2).g22 shouldBe eg2
                    }
                }
            }
        }
    }

    @Test
    @JsName("plain_option_group_validation")
    fun `plain option group validation`() = forAll(
        row("", null, true),
        row("--x=1", 1, true),
        row("--x=2", null, false)
    ) { argv, ex, ec ->
        class G : OptionGroup() {
            val x by option().int().validate {
                require(it == 1) { "fail" }
            }
        }

        class C : TestCommand(called = ec) {
            val g by G()

            override fun run_() {
                g.x shouldBe ex
            }
        }

        if (ec) C().parse(argv)
        else shouldThrow<BadParameterValue> { C().parse(argv) }.message shouldBe "Invalid value for \"--x\": fail"
    }

    @Test
    @JsName("cooccurring_option_group_validation")
    fun `cooccurring option group validation`() = forAll(
        row("", null, true, null),
        row("--x=1 --y=1", 1, true, null),
        row("--x=2", null, false, "Missing option \"--y\""),
        row("--x=2 --y=1", null, false, "Invalid value for \"--x\": fail")
    ) { argv, ex, ec, em ->
        class G : OptionGroup() {
            val x by option().int().check("fail") { it == 1 }
            val y by option().required()
        }

        class C : TestCommand(called = ec) {
            val g by G().cooccurring()

            override fun run_() {
                g?.x shouldBe ex
            }
        }

        if (ec) C().parse(argv)
        else shouldThrow<UsageError> { C().parse(argv) }.message shouldBe em
    }

    @Test
    @JsName("mutually_exclusive_group_validation")
    fun `mutually exclusive group validation`() = forAll(
        row("", null, true),
        row("--x=1", 1, true),
        row("--y=1", 1, true),
        row("--x=2", 2, true),
        row("--y=2", 2, false)
    ) { argv, eg, ec ->
        class C : TestCommand(called = ec) {
            val g by mutuallyExclusiveOptions(
                option("--x").int(),
                option("--y").int().validate {
                    require(it == 1) { "fail" }
                }
            )

            override fun run_() {
                g shouldBe eg
            }
        }
        if (ec) C().parse(argv)
        else shouldThrow<UsageError> { C().parse(argv) }.message shouldBe "Invalid value for \"--y\": fail"
    }

    @Test
    @JsName("group_choice_with_defaultByName")
    fun `groupChoice with defaultByName`() = forAll(
        row("--g=3", "Group3"),
        row("", "Group4")
    ) { argv, ex ->
        class C : TestCommand() {
            val g by option().groupChoice("3" to Group3(), "4" to Group4())
                .defaultByName("4")

            override fun run_() {
                g::class.simpleName shouldBe ex
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("group_switch_with_defaultByName")
    fun `groupSwitch with defaultByName`() = forAll(
        row("--x", "Group3"),
        row("", "Group4")
    ) { argv, ex ->
        class C : TestCommand() {
            val g by option().groupSwitch("--x" to Group3(), "--y" to Group4())
                .defaultByName("--y")

            override fun run_() {
                g::class.simpleName shouldBe ex
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("groupSwitch_with_defaultByName_with_invalid_name")
    fun `groupSwitch with defaultByName with invalid name`() {
        class C : TestCommand(called = false) {
            val g by option().groupSwitch("--x" to Group1(), "--y" to Group2())
                .defaultByName("--z")
        }
        shouldThrow<IllegalArgumentException> { C() }
    }

    @Test
    @JsName("groupChoice_with_defaultByName_with_invalid_name")
    fun `groupChoice with defaultByName with invalid name`() {
        class C : TestCommand(called = false) {
            val g by option().groupChoice("1" to Group1(), "2" to Group2())
                .defaultByName("3")
        }
        shouldThrow<IllegalArgumentException> { C() }
    }

    @Test
    @JsName("groupChoice_with_defaultByName_and_default_options")
    fun `groupChoice with defaultByName and default options`() = forAll(
        row("", "Group5", 1, ""),
        row("--g=a", "Group5", 1, ""),
        row("--opt1=2 --opt2=foo", "Group5", 2, ""),
        row("--g=a --opt1=2", "Group5", 2, ""),
        row("--g=b --opt2=foo", "Group6", 0, "foo")
    ) { argv, eg, e1, e2 ->
        class C : TestCommand() {
            val g by option().groupChoice("a" to Group5(), "b" to Group6())
                .defaultByName("a")

            override fun run_() {
                g::class.simpleName shouldBe eg
                when (val it = g) {
                    is Group5 -> it.opt1 shouldBe e1
                    is Group6 -> it.opt2 shouldBe e2
                    else -> fail("unknown type $g")
                }
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("groupSwitch_with_defaultByName_and_default_options")
    fun `groupSwitch with defaultByName and default options`() = forAll(
        row("", "Group5", 1, ""),
        row("--x", "Group5", 1, ""),
        row("--opt1=2 --opt2=foo", "Group5", 2, ""),
        row("--x --opt1=2", "Group5", 2, ""),
        row("--y --opt2=foo", "Group6", 0, "foo")
    ) { argv, eg, e1, e2 ->
        class C : TestCommand() {
            val g by option().groupSwitch("--x" to Group5(), "--y" to Group6())
                .defaultByName("--x")

            override fun run_() {
                g::class.simpleName shouldBe eg
                when (val it = g) {
                    is Group5 -> it.opt1 shouldBe e1
                    is Group6 -> it.opt2 shouldBe e2
                    else -> fail("unknown type $g")
                }
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("groupChoice_with_defaultByName_and_validate")
    fun `groupChoice with defaultByName and validate`() {
        class GroupA : OptionGroup() {
            val opt by option().int().default(1).validate { require(it < 2) }
        }

        class GroupB : OptionGroup()

        class C(private val ea: Int) : TestCommand() {
            val g by option().groupChoice("a" to GroupA(), "b" to GroupB())
                .defaultByName("a")

            override fun run_() {
                when (val it = g) {
                    is GroupA -> it.opt shouldBe ea
                    is GroupB -> {
                    }

                    else -> fail("unknown type $g")
                }
            }
        }
        C(1).parse("")
        C(0).parse("--opt=0")
        C(0).parse("--g=a --opt=0")

        shouldThrow<UsageError> { C(3).parse("--g=a --opt=3") }
        shouldThrow<UsageError> { C(3).parse("--opt=3") }
    }
}

private class Group1 : OptionGroup() {
    val g11 by option().int().required()
    val g12 by option().int()
}

private class Group2 : OptionGroup() {
    val g21 by option().int().required()
    val g22 by option().int()
}

private class Group3 : OptionGroup() {
    val g11 by option().int()
    val g12 by option().int()
}

private class Group4 : OptionGroup() {
    val g21 by option().int()
    val g22 by option().int()
}

private class Group5 : OptionGroup() {
    val opt1 by option().int().default(1)
}

private class Group6 : OptionGroup() {
    val opt2 by option()
}

private sealed class Sealed {
    object Sealed1 : Sealed()
    object Sealed2 : Sealed()
}