package net.mamoe.mirai.clikt.core

private val SPLITTER = Regex("""(?<!\\)\s+""")
private val ESCAPE = Regex("""\\(.)""")
private val QUOTE_START = Regex("""(?<!\\)\s"|^\s"|^"""")
private val QUOTE_END = Regex("""(?<!\\)"\s|(?<!\\)"$""")

/**
 * split by a space ` `, can escape char by `\` or inside a pair of quotation mark `"`
 *
 * for example, `this is "command foo" char\ escape` split to `["this", "is", "command foo", "char escape"]`
 */
public fun String.parseToArgs(): List<String> =
    if (isBlank()) {
        emptyList()
    } else {
        split(QUOTE_START, 2)
            .flatMapIndexed { idx, s ->
                if (idx > 0) s.split(QUOTE_END, 2) else listOf(s)
            }
            .let { unquoted ->
                if (unquoted.size == 3) {
                    unquoted[0].parseToArgs() + listOf(unquoted[1]) +
                            unquoted[2].parseToArgs()
                } else {
                    split(SPLITTER).filterNot { it.isBlank() }
                        .map { it.replace(ESCAPE, "$1") }.toList()
                }
            }
    }