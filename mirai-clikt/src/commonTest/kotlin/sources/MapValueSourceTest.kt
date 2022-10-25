package net.mamoe.mirai.clikt.sources

import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import net.mamoe.mirai.clikt.core.context
import net.mamoe.mirai.clikt.core.subcommands
import net.mamoe.mirai.clikt.parameters.options.option
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.parse
import kotlin.test.Test


class MapValueSourceTest {
    @Test
    fun getKey() = forAll(
        row("p_", null, false, "-", "p_foo-bar"),
        row("", ":", false, "-", "sub:foo-bar"),
        row("", ":", true, ":", "SUB:FOO:BAR"),
        row("", null, true, "-", "FOO-BAR"),
        row("", null, false, "_", "foo_bar")
    ) { p, j, c, r, k ->
        class Root : TestCommand()
        class Sub : TestCommand() {
            init {
                context {
                    valueSource = MapValueSource(
                        mapOf(
                            "other" to "other",
                            "FX" to "fixed",
                            k to "foo"
                        ), getKey = ValueSource.getKey(p, j, c, r)
                    )
                }
            }

            val fooBar by option()
            val fixed by option(valueSourceKey = "FX")
            override fun run_() {
                fooBar shouldBe "foo"
                fixed shouldBe "fixed"
            }
        }
        Root().subcommands(Sub()).parse("sub")
    }
}