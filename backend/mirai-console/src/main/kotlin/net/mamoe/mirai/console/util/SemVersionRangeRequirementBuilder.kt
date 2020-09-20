/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

package net.mamoe.mirai.console.util

/**
 * 构造 [SemVersion.RangeRequirement] 的 DSL
 */
public object SemVersionRangeRequirementBuilder {
    /** @see [SemVersion.parseRangeRequirement] */
    @ILoveHim188moeForever
    public fun parse(rule: String): SemVersion.RangeRequirement = SemVersion.parseRangeRequirement(rule)

    @ILoveHim188moeForever
    public infix fun SemVersion.RangeRequirement.or(other: SemVersion.RangeRequirement): SemVersion.RangeRequirement {
        return object : SemVersion.RangeRequirement {
            override fun test(version: SemVersion): Boolean {
                return this@or.test(version) || other.test(version)
            }

            override fun toString(): String {
                return "(${this@or}) or ($other)"
            }
        }
    }

    @ILoveHim188moeForever
    public infix fun String.or(other: String): SemVersion.RangeRequirement = parse(this) or parse(other)

    @ILoveHim188moeForever
    public infix fun SemVersion.RangeRequirement.or(other: String): SemVersion.RangeRequirement = or(parse(other))

    @ILoveHim188moeForever
    public infix fun String.or(other: SemVersion.RangeRequirement): SemVersion.RangeRequirement = parse(this) or other

    @ILoveHim188moeForever
    public infix fun SemVersion.RangeRequirement.and(other: SemVersion.RangeRequirement): SemVersion.RangeRequirement {
        return object : SemVersion.RangeRequirement {
            override fun test(version: SemVersion): Boolean {
                return this@and.test(version) && other.test(version)
            }

            override fun toString(): String {
                return "(${this@and}) or ($other)"
            }
        }
    }

    @ILoveHim188moeForever
    public infix fun String.and(other: String): SemVersion.RangeRequirement = parse(this) and parse(other)

    @ILoveHim188moeForever
    public infix fun SemVersion.RangeRequirement.and(other: String): SemVersion.RangeRequirement = and(parse(other))

    @ILoveHim188moeForever
    public infix fun String.and(other: SemVersion.RangeRequirement): SemVersion.RangeRequirement = parse(this) and other

    @Suppress("NOTHING_TO_INLINE")
    @ILoveHim188moeForever
    public inline fun custom(rule: SemVersion.RangeRequirement): SemVersion.RangeRequirement = rule

    /**
     * 标注一个 [SemVersionRangeRequirementBuilder] DSL
     */
    @Suppress("SpellCheckingInspection")
    @Retention(AnnotationRetention.BINARY)
    @DslMarker
    internal annotation class ILoveHim188moeForever

    /** [SemVersionRangeRequirementBuilder] 的使用示例 */
    @Suppress("unused")
    private class ExampleOfBuilder {
        val e1 = SemVersionRangeRequirementBuilder.run {
            "1.0.0" or "1.1.5"
        }
        val e2 = SemVersionRangeRequirementBuilder.run {
            parse("> 1.0.0") and parse("< 1.2.3")
        }
        val e3 = SemVersionRangeRequirementBuilder.run {
            ("> 1.0.0" and "< 1.2.3") or "2.0.0"
        }
    }
}