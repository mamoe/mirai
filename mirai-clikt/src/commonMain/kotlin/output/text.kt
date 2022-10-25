package net.mamoe.mirai.clikt.output

internal const val NEL = "\u0085"

internal fun String.wrapText(
    sb: StringBuilder,
    width: Int = 78,
    initialIndent: String = "",
    subsequentIndent: String = "",
) {
    require(initialIndent.length < width) { "initialIndent >= width: ${initialIndent.length} >= $width" }
    require(subsequentIndent.length < width) { "subsequentIndent >= width: ${subsequentIndent.length} >= $width" }

    for ((i, paragraph) in splitParagraphs(this).withIndex()) {
        if (i > 0) sb.append("\n\n")
        sb.wrapParagraph(paragraph, width, if (i == 0) initialIndent else subsequentIndent, subsequentIndent)
    }
}

private val TEXT_START_REGEX = Regex("\\S")
private val PRE_P_END_REGEX = Regex("""```[ \t]*(?:\n\s*|[ \t]*$)""")
private val PLAIN_P_END_REGEX = Regex("""[ \t]*\n(?:\s*```|[ \t]*\n\s*)|$NEL?\s*$""")
private val LINE_BREAK_REGEX = Regex("\r?\n")
private val WHITESPACE_OR_NEL_REGEX = Regex("""\s+|$NEL""")
private val WORD_OR_NEL_REGEX = Regex("""[^\s$NEL]+|$NEL""")

// there's no dotall flag on JS, so we have to use [\s\S] instead
private val PRE_P_CONTENTS_REGEX = Regex("""```([\s\S]*?)```""")

internal fun splitParagraphs(text: String): List<String> {
    val paragraphs = mutableListOf<String>()
    var i = TEXT_START_REGEX.find(text)?.range?.first ?: return emptyList()
    while (i < text.length) {
        val range = if (text.startsWith("```", startIndex = i)) {
            PRE_P_END_REGEX.find(text, i + 3)?.let {
                (it.range.first + 3)..it.range.last
            }
        } else {
            PLAIN_P_END_REGEX.find(text, i)?.let {
                when {
                    it.value.startsWith(NEL) -> it.range.first + 1..it.range.first + 1
                    it.value.endsWith("```") -> it.range.first..(it.range.last - 3)
                    else -> it.range
                }
            }
        } ?: text.length..text.length
        paragraphs += text.substring(i, range.first)
        i = range.last + 1
    }
    return paragraphs
}

private fun StringBuilder.tryPreformat(text: String, initialIndent: String, subsequentIndent: String): Boolean {
    val value = PRE_P_CONTENTS_REGEX.matchEntire(text)?.groups?.get(1)?.value
    val pre = value?.replaceIndent(subsequentIndent)?.removePrefix(subsequentIndent)
        ?: return false

    for ((i, line) in pre.split(LINE_BREAK_REGEX).withIndex()) {
        if (i == 0) append(initialIndent)
        else append("\n")
        append(line.trimEnd())
    }
    return true
}

private fun StringBuilder.wrapParagraph(text: String, width: Int, initialIndent: String, subsequentIndent: String) {
    if (tryPreformat(text, initialIndent, subsequentIndent)) return
    val breakLine = "\n$subsequentIndent"

    if (initialIndent.length + text.length <= width) {
        val newText = text.trim().replace(WHITESPACE_OR_NEL_REGEX) {
            if (it.value == NEL) breakLine else " "
        }
        append(initialIndent).append(newText)
        return
    }

    val words = WORD_OR_NEL_REGEX.findAll(text).map { it.value }.toList()
    append(initialIndent)
    var currentWidth = initialIndent.length
    var prevWasNel = false
    for ((i, word) in words.withIndex()) {
        if (word == NEL) {
            append(breakLine)
            currentWidth = subsequentIndent.length
            prevWasNel = true
            continue
        }

        if (i > 0 && !prevWasNel) {
            if (currentWidth + word.length + 1 > width) {
                append(breakLine)
                currentWidth = subsequentIndent.length
            } else {
                append(" ")
                currentWidth += 1
            }
        }

        prevWasNel = false
        append(word)
        currentWidth += word.length
    }
}