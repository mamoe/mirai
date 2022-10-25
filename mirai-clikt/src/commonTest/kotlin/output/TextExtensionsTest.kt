package net.mamoe.mirai.clikt.output

import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

private fun String.wrap(
    width: Int = 78,
    initialIndent: String = "",
    subsequentIndent: String = "",
): String = buildString {
    unshow().wrapText(this, width, initialIndent, subsequentIndent)
}.show()

private fun String.show() = replace("\n", "⏎")
private fun String.unshow() = replace("⏎", "\n")

class TextExtensionsTest {
    private fun r(
        input: String,
        expected: String,
        width: Int = 78,
        initialIndent: String = "",
        subsequentIndent: String = "",
    ) = row(input, expected, width, initialIndent, subsequentIndent)

    @Test
    fun wrapText() = forAll(
        r("", ""),
        r("abc", "abc"),
        r("abc⏎", "abc"),
        r("abc⏎", "abc", width = 2),
        r("abc", "abc", width = 2),
        r("a c", "a⏎c", width = 2),
        r("a b c", "a b⏎c", width = 3),
        r("a bc", "a⏎bc", width = 3),
        r("abc", "=abc", initialIndent = "=", subsequentIndent = "-"),
        r("a b c", "=a b⏎-c", width = 4, initialIndent = "=", subsequentIndent = "-"),
        r("a b c", "= a⏎b c", width = 3, initialIndent = "= ", subsequentIndent = ""),
        r("a⏎⏎b", "a⏎⏎b", width = 3),
        r("a b c⏎⏎d e f", "a b⏎c⏎⏎d e⏎f", width = 3),
        r("a b c⏎⏎d e f", "=a b⏎-c⏎⏎-d e⏎-f", width = 4, initialIndent = "=", subsequentIndent = "-"),
        r("a⏎b", "...a b", initialIndent = "...", subsequentIndent = "--"),
        r("a b${NEL}c d", "a b⏎c d"),
        r("a b$NEL⏎c d", "a b⏎c d", width = 4),
        r("a$NEL${NEL}b$NEL", "a⏎⏎b⏎", width = 4),
        r("a b$NEL⏎c d", ".a b⏎-c d", width = 4, initialIndent = ".", subsequentIndent = "-")
    ) { input, expected, width, initialIndent, subsequentIndent ->
        input.wrap(width, initialIndent, subsequentIndent) shouldBe expected
    }

    @Test
    fun splitParagraphs() = forAll(
        row("a", listOf("a")),
        row(NEL, listOf(NEL)),
        row("```", listOf("```")),
        row("```$NEL", listOf("```$NEL")),
        row("a⏎b", listOf("a⏎b")),
        row("a⏎⏎b", listOf("a", "b")),
        row(" a ⏎ ⏎ b ", listOf("a", "b")),
        row("a⏎⏎```b```", listOf("a", "```b```")),
        row("```a```⏎⏎ b ", listOf("```a```", "b")),
        row("a⏎⏎```⏎b⏎```", listOf("a", "```⏎b⏎```")),
        row("a⏎```⏎b⏎```", listOf("a", "```⏎b⏎```")),
        row(" a ⏎ ``` ⏎ b ⏎ ``` ", listOf("a", "``` ⏎ b ⏎ ```")),
        row("a ⏎ ⏎ ⏎ ```⏎b⏎```⏎```⏎c⏎```", listOf("a", "```⏎b⏎```", "```⏎c⏎```"))
    ) { text, expected ->
        splitParagraphs(text.unshow()).map { it.show() } shouldBe expected
    }

    @Test
    @JsName("wrapText_pre_minus_format_leading_indent")
    fun `wrapText pre-format leading indent`() {
        """
        a
        ```
        b    b
         c  c
          d
        ```
        e
        """.wrap() shouldBe """
        |a
        |
        |b    b
        | c  c
        |  d
        |
        |e
        """.trimMargin().show()
    }
}