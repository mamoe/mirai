/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

/*
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */

package net.mamoe.mirai.console.internal.util

import net.mamoe.mirai.console.util.SemVersion
import kotlin.math.max
import kotlin.math.min

@Suppress("RegExpRedundantEscape")
internal object SemVersionInternal {
    private val directVersion = """^[0-9]+(\.[0-9]+)+(|[\-+].+)$""".toRegex()
    private val versionSelect = """^[0-9]+(\.[0-9]+)*\.x$""".toRegex()
    private val versionRange = """([0-9]+(\.[0-9]+)+(|[\-+].+))\s*\-\s*([0-9]+(\.[0-9]+)+(|[\-+].+))""".toRegex()
    private val versionMathRange =
        """\[([0-9]+(\.[0-9]+)+(|[\-+].+))\s*\,\s*([0-9]+(\.[0-9]+)+(|[\-+].+))\]""".toRegex()
    private val versionRule = """^((\>\=)|(\<\=)|(\=)|(\>)|(\<))\s*([0-9]+(\.[0-9]+)+(|[\-+].+))$""".toRegex()
    private fun Collection<*>.dump() {
        forEachIndexed { index, value ->
            println("$index, $value")
        }
    }

    @JvmStatic
    private fun String.parseRule(): SemVersion.RangeRequirement {
        val trimmed = trim()
        if (directVersion.matches(trimmed)) {
            val parsed = SemVersion.parse(trimmed)
            return SemVersion.RangeRequirement {
                it.compareTo(parsed) == 0
            }
        }
        if (versionSelect.matches(trimmed)) {
            val regex = ("^" +
                    trimmed.replace(".", "\\.")
                        .replace("x", ".+") +
                    "$"
                    ).toRegex()
            return SemVersion.RangeRequirement {
                regex.matches(it.toString())
            }
        }
        (versionRange.matchEntire(trimmed) ?: versionMathRange.matchEntire(trimmed))?.let { range ->
            var start = SemVersion.parse(range.groupValues[1])
            var end = SemVersion.parse(range.groupValues[4])
            if (start > end) {
                val c = end
                end = start
                start = c
            }
            val compareRange = start..end
            return SemVersion.RangeRequirement {
                it in compareRange
            }
        }
        versionRule.matchEntire(trimmed)?.let { result ->
            val operator = result.groupValues[1]
            val version = SemVersion.parse(result.groupValues[7])
            return when (operator) {
                ">=" -> {
                    SemVersion.RangeRequirement { it >= version }
                }
                ">" -> {
                    SemVersion.RangeRequirement { it > version }
                }
                "<=" -> {
                    SemVersion.RangeRequirement { it <= version }
                }
                "<" -> {
                    SemVersion.RangeRequirement { it < version }
                }
                "=" -> {
                    SemVersion.RangeRequirement { it.compareTo(version) == 0 }
                }
                else -> throw AssertionError("operator=$operator, version=$version")
            }
        }
        throw UnsupportedOperationException("Cannot parse $this")
    }

    private fun SemVersion.RangeRequirement.withRule(rule: String): SemVersion.RangeRequirement {
        return object : SemVersion.RangeRequirement {
            override fun check(version: SemVersion): Boolean {
                return this@withRule.check(version)
            }

            override fun toString(): String {
                return rule
            }
        }
    }

    @JvmStatic
    fun parseRangeRequirement(requirement: String): SemVersion.RangeRequirement {
        if (requirement.isBlank()) {
            throw IllegalArgumentException("Invalid requirement: Empty requirement rule.")
        }
        return requirement.split("||").map {
            it.parseRule().withRule(it)
        }.let { checks ->
            if (checks.size == 1) return checks[0]
            SemVersion.RangeRequirement {
                checks.forEach { rule ->
                    if (rule.check(it)) return@RangeRequirement true
                }
                return@RangeRequirement false
            }.withRule(requirement)
        }
    }

    @JvmStatic
    fun SemVersion.compareInternal(other: SemVersion): Int {
        // ignored metadata in comparing

        // If $this equals $other (without metadata),
        // return same.
        if (other.mainVersion.contentEquals(mainVersion) && identifier == other.identifier) {
            return 0
        }
        fun IntArray.getSafe(index: Int) = getOrElse(index) { 0 }

        // Compare main-version
        for (index in 0 until (max(mainVersion.size, other.mainVersion.size))) {
            val result = mainVersion.getSafe(index).compareTo(other.mainVersion.getSafe(index))
            if (result != 0) return result
        }
        // If main-versions are same.
        var identifier0 = identifier
        var identifier1 = other.identifier
        // If anyone doesn't have the identifier...
        if (identifier0 == null || identifier1 == null) {
            return when (identifier0) {
                identifier1 -> { // null == null
                    // Nobody has identifier
                    0
                }
                null -> {
                    // $other has identifier, but $this don't have identifier
                    // E.g:
                    //   this    = 1.0.0
                    //   other   = 1.0.0-dev
                    1
                }
                // It is the opposite of the above.
                else -> -1
            }
        }
        fun String.getSafe(index: Int) = getOrElse(index) { ' ' }

        // ignored same prefix
        fun getSameSize(s1: String, s2: String): Int {
            val size = min(s1.length, s2.length)
            //   1.0-RC19  -> 19
            //   1.0-RC107 -> 107
            var realSameSize = 0
            for (index in 0 until size) {
                if (s1[index] != s2[index]) {
                    return realSameSize
                } else {
                    if (!s1[index].isDigit()) {
                        realSameSize = index + 1
                    }
                }
            }
            return realSameSize
        }

        // We ignore the same parts. Because we only care about the differences.
        // E.g:
        //  1.0-RC1 -> 1
        //  1.0-RC2 -> 2
        val ignoredSize = getSameSize(identifier0, identifier1)
        identifier0 = identifier0.substring(ignoredSize)
        identifier1 = identifier1.substring(ignoredSize)
        // Multi-chunk comparing
        val chunks0 = identifier0.split('-', '.', '_')
        val chunks1 = identifier1.split('-', '.', '_')
        chunkLoop@ for (index in 0 until (max(chunks0.size, chunks1.size))) {
            val value0 = chunks0.getOrNull(index)
            val value1 = chunks1.getOrNull(index)
            // Any chunk is null
            if (value0 == null || value1 == null) {
                // value0 == null && value1 == null is impossible
                return if (value0 == null) {
                    // E.g:
                    //  value0 = 1.0-RC-dev
                    //  value1 = 1.0-RC-dev-1
                    -1
                } else {
                    // E.g:
                    //  value0 = 1.0-RC-dev-1
                    //  value1 = 1.0-RC-dev
                    1
                }
            }
            try {
                val result = value0.toInt().compareTo(value1.toInt())
                if (result != 0) {
                    return result
                }
                continue@chunkLoop
            } catch (ignored: NumberFormatException) {
            }
            // compare chars
            for (index0 in 0 until (max(value0.length, value1.length))) {
                val result = value0.getSafe(index0).compareTo(value1.getSafe(index0))
                if (result != 0)
                    return result
            }
        }
        return 0
    }
}