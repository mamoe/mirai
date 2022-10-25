package net.mamoe.mirai.clikt.sources

import io.kotest.matchers.shouldBe
import net.mamoe.mirai.clikt.core.context
import net.mamoe.mirai.clikt.parameters.options.option
import net.mamoe.mirai.clikt.testing.TestCommand
import net.mamoe.mirai.clikt.testing.TestSource
import net.mamoe.mirai.clikt.testing.parse
import kotlin.js.JsName
import kotlin.test.Test

class ChainedValueSourceTest {
    @Test
    @JsName("reads_from_the_first_available_value")
    fun `reads from the first available value`() {
        val sources = arrayOf(
            TestSource(),
            TestSource("foo" to "bar"),
            TestSource("foo" to "baz")
        )

        class C : TestCommand() {
            init {
                context {
                    valueSources(*sources)
                }
            }

            val foo by option()

            override fun run_() {
                foo shouldBe "bar"
            }
        }

        C().parse("")
        sources[0].assert(read = true)
        sources[1].assert(read = true)
        sources[2].assert(read = false)
    }
}