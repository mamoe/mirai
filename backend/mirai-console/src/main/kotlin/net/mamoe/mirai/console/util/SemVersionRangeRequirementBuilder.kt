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
 * 构造 [SemVersion.Requirement] 的 DSL
 */
public object SemVersionRangeRequirementBuilder {
    /** @see [SemVersion.parseRangeRequirement] */
    @ILoveHim188moeForever
    public fun parse(rule: String): SemVersion.Requirement = SemVersion.parseRangeRequirement(rule)

    @ILoveHim188moeForever
    public infix fun SemVersion.Requirement.or(other: SemVersion.Requirement): SemVersion.Requirement {
        return object : SemVersion.Requirement {
            override fun test(version: SemVersion): Boolean {
                return this@or.test(version) || other.test(version)
            }

            override fun toString(): String {
                return "{${this@or}} || {$other}"
            }
        }
    }

    @ILoveHim188moeForever
    public infix fun String.or(other: String): SemVersion.Requirement = parse(this) or parse(other)

    @ILoveHim188moeForever
    public infix fun SemVersion.Requirement.or(other: String): SemVersion.Requirement = or(parse(other))

    @ILoveHim188moeForever
    public infix fun String.or(other: SemVersion.Requirement): SemVersion.Requirement = parse(this) or other

    @ILoveHim188moeForever
    public infix fun SemVersion.Requirement.and(other: SemVersion.Requirement): SemVersion.Requirement {
        return object : SemVersion.Requirement {
            override fun test(version: SemVersion): Boolean {
                return this@and.test(version) && other.test(version)
            }

            override fun toString(): String {
                return "{${this@and}} && {$other}"
            }
        }
    }

    @ILoveHim188moeForever
    public infix fun String.and(other: String): SemVersion.Requirement = parse(this) and parse(other)

    @ILoveHim188moeForever
    public infix fun SemVersion.Requirement.and(other: String): SemVersion.Requirement = and(parse(other))

    @ILoveHim188moeForever
    public infix fun String.and(other: SemVersion.Requirement): SemVersion.Requirement = parse(this) and other

    @Suppress("NOTHING_TO_INLINE")
    @ILoveHim188moeForever
    public fun custom(rule: (SemVersion) -> Boolean): SemVersion.Requirement = object : SemVersion.Requirement {
        override fun test(version: SemVersion): Boolean = rule(version)
        override fun toString(): String {
            return "Custom{$rule}"
        }
    }

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