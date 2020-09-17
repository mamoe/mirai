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

package net.mamoe.mirai.console.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.console.internal.util.SemVersionInternal
import net.mamoe.mirai.console.util.SemVersion.Companion.equals

/**
 * 语义化版本支持
 *
 * 在阅读此文件前, 请先阅读 https://semver.org/lang/zh-CN/
 * 该文档说明了语义化版本是什么, 此文件不再过多描述
 *
 * ----
 *
 * 这是一个例子 `1.0.0-M4+c25733b8`
 *
 * 将会解析出三个内容, mainVersion(核心版本号), identifier(先行版本号) 和 metadata(元数据).
 *
 * 对这个例子进行解析会得到
 * ```
 * SemVersion(
 *   mainVersion = IntArray [1, 0, 0],
 *   identifier  = "M4"
 *   metadata    = "c25733b8"
 * )
 * ```
 * 其中 identifier 和 metadata 都是可选的, 该实现对于 mainVersion 的最大长度不作出限制,
 * 也建议 mainVersion 的长度不要过长或过短
 * 但是必须至少拥有两位及以上的版本描述符, (即必须拥有主版本号和次版本号).
 *
 * 比如 `1-M4` 是不合法的, 但是 `1.0-M4` 是合法的
 *
 */
@Serializable
public data class SemVersion internal constructor(
    /** 核心版本号, 至少包含一个主版本号和一个次版本号 */
    public val mainVersion: IntArray,
    /** 先行版本号识别符 */
    public val identifier: String? = null,
    /** 版本号元数据, 不参与版本号对比([compareTo]), 但是参与版本号严格对比([equals]) */
    public val metadata: String? = null
) : Comparable<SemVersion> {
    /**
     * 一条依赖规则
     * @see [parseRangeRequirement]
     */
    public fun interface RangeRequirement {
        /** 在 [version] 满足此要求时返回 true */
        public fun check(version: SemVersion): Boolean
    }

    public companion object {
        /** 解析核心版本号, eg: `1.0.0` -> IntArray[1, 0, 0] */
        @JvmStatic
        private fun String.parseMainVersion(): IntArray =
            split('.').map { it.toInt() }.toIntArray()

        /**
         * 解析一个版本号, 将会返回一个 [SemVersion],
         * 如果发生解析错误将会抛出一个 [IllegalArgumentException] 或者 [NumberFormatException]
         *
         * 对于版本号的组成, 我们有以下规定:
         * - 必须包含主版本号和次版本号
         * - 存在 先行版本号 的时候 先行版本号 不能为空
         * - 存在 元数据 的时候 元数据 不能为空
         *
         * 注意情况:
         * - 第一个 `+` 之后的所有内容全部识别为元数据
         *     - `1.0+METADATA-M4`, metadata="METADATA-M4"
         */
        @Throws(IllegalArgumentException::class, NumberFormatException::class)
        @JvmStatic
        public fun parse(version: String): SemVersion {
            var mainVersionEnd: Int = 0
            kotlin.run {
                val iterator = version.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    if (next == '-' || next == '+') {
                        break
                    }
                    mainVersionEnd++
                }
            }
            var identifier: String? = null
            var metadata: String? = null
            if (mainVersionEnd != version.length) {
                when (version[mainVersionEnd]) {
                    '-' -> {
                        val metadataSplitter = version.indexOf('+', startIndex = mainVersionEnd)
                        if (metadataSplitter == -1) {
                            identifier = version.substring(mainVersionEnd + 1)
                        } else {
                            identifier = version.substring(mainVersionEnd + 1, metadataSplitter)
                            metadata = version.substring(metadataSplitter + 1)
                        }
                    }
                    '+' -> {
                        metadata = version.substring(mainVersionEnd + 1)
                    }
                }
            }
            return SemVersion(
                mainVersion = version.substring(0, mainVersionEnd).also { mainVersion ->
                    if (mainVersion.indexOf('.') == -1) {
                        throw IllegalArgumentException("$mainVersion must has more than one label")
                    }
                    if (mainVersion.last() == '.') {
                        throw IllegalArgumentException("Version string cannot end-with `.`")
                    }
                }.parseMainVersion(),
                identifier = identifier?.also {
                    if (it.isBlank()) {
                        throw IllegalArgumentException("The identifier cannot be blank.")
                    }
                },
                metadata = metadata?.also {
                    if (it.isBlank()) {
                        throw IllegalArgumentException("The metadata cannot be blank.")
                    }
                }
            )
        }

        /**
         * 解析一条依赖需求描述, 在无法解析的时候抛出 [IllegalArgumentException]
         *
         * 对于一条规则, 有以下方式可选
         *
         * - `1.0.0-M4`       要求 1.0.0-M4 版本, 且只能是 1.0.0-M4 版本
         * - `1.x`            要求 1.x 版本
         * - `1.0.0 - 1.2.0`  要求 1.0.0 到 1.2.0 的任意版本, 注意 `-` 两边必须要有空格
         * - `[1.0.0, 1.2.0]` 与 `1.0.0 - 1.2.0` 一致
         * - `> 1.0.0-RC`     要求 1.0.0-RC 之后的版本, 不能是 1.0.0-RC
         * - `>= 1.0.0-RC`    要求 1.0.0-RC 或之后的版本, 可以是 1.0.0-RC
         * - `< 1.0.0-RC`     要求 1.0.0-RC 之前的版本, 不能是 1.0.0-RC
         * - `<= 1.0.0-RC`    要求 1.0.0-RC 或之前的版本, 可以是 1.0.0-RC
         *
         * 对于多个规则, 也允许使用 `||` 拼接在一起.
         * 例如:
         * - `1.x || 2.x || 3.0`
         * - `<= 0.5.3 || >= 1.0.0`
         *
         * 特别注意:
         * - 依赖规则版本号不需要携带版本号元数据, 元数据不参与依赖需求的检查
         * - 如果目标版本号携带有先行版本号, 请不要忘记先行版本号
         */
        @Throws(IllegalArgumentException::class)
        @JvmStatic
        public fun parseRangeRequirement(requirement: String): RangeRequirement {
            return SemVersionInternal.parseRangeRequirement(requirement)
        }

        /** @see [RangeRequirement.check] */
        @JvmStatic
        public fun RangeRequirement.check(version: String): Boolean = check(parse(version))

        /**
         * 当满足 [requirement] 时返回 true, 否则返回 false
         */
        @JvmStatic
        public fun SemVersion.satisfies(requirement: RangeRequirement): Boolean = requirement.check(this)

        /** for Kotlin only */
        @JvmStatic
        @JvmSynthetic
        public operator fun RangeRequirement.contains(version: SemVersion): Boolean = check(version)

        /** for Kotlin only */
        @JvmStatic
        @JvmSynthetic
        public operator fun RangeRequirement.contains(version: String): Boolean = check(version)
    }

    @Transient
    private var toString: String? = null // For cache.
    override fun toString(): String {
        return toString ?: kotlin.run {
            buildString {
                mainVersion.joinTo(this, ".")
                identifier?.let { identifier ->
                    append('-')
                    append(identifier)
                }
                metadata?.let { metadata ->
                    append('+')
                    append(metadata)
                }
            }.also { toString = it }
        }
    }

    /**
     * 将 [SemVersion] 转为 Kotlin data class 风格的 [String]
     */
    public fun toStructuredString(): String {
        return "SemVersion(mainVersion=${mainVersion.contentToString()}, identifier=$identifier, metadata=$metadata)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SemVersion

        return compareTo(other) == 0 && other.identifier == identifier && other.metadata == metadata
    }

    override fun hashCode(): Int {
        var result = mainVersion.contentHashCode()
        result = 31 * result + (identifier?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        return result
    }

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    public override operator fun compareTo(other: SemVersion): Int {
        return SemVersionInternal.run { compareInternal(other) }
    }
}
