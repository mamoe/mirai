/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.util

@Suppress("RegExpRedundantEscape")
object RequirementHelper {
    private val directVersion = """^[0-9]+(\.[0-9]+)+(|[\-+].+)$""".toRegex()
    private val versionSelect = """^[0-9]+(\.[0-9]+)*\.x$""".toRegex()
    private val versionMathRange =
        """([\[\(])([0-9]+(\.[0-9]+)+(|[\-+].+))\s*\,\s*([0-9]+(\.[0-9]+)+(|[\-+].+))([\]\)])""".toRegex()
    private val versionRule = """^((\>\=)|(\<\=)|(\=)|(\!\=)|(\>)|(\<))\s*([0-9]+(\.[0-9]+)+(|[\-+].+))$""".toRegex()

    private val SEM_VERSION_REGEX =
        """^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$""".toRegex()

    fun isValid(rule: String): Boolean {
        return rule.trim().let {
            directVersion.matches(it) ||
                    versionSelect.matches(it) ||
                    versionMathRange.matches(it) ||
                    versionRule.matches(it)
        }
    }

    internal object RequirementChecker : RequirementParser.ProcessorBase<Unit>() {
        override fun processLogic(isAnd: Boolean, chunks: Iterable<Unit>) {
        }

        override fun processString(reader: RequirementParser.TokenReader, token: RequirementParser.Token.Content) {
            if (!isValid(token.content)) {
                token.ia(reader, "`${token.content}` 无效.")
            }
        }

    }
}