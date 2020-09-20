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

@file:Suppress("unused")

package net.mamoe.mirai.console.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.internal.data.map
import net.mamoe.mirai.console.internal.util.SemVersionInternal
import net.mamoe.mirai.console.util.SemVersion.Companion.equals
import net.mamoe.mirai.console.util.SemVersion.RangeRequirement

/**
 * [语义化版本](https://semver.org/lang/zh-CN/) 支持
 *
 * 解析示例:
 *
 * `1.0.0-M4+c25733b8` 将会解析出三个内容, mainVersion (核心版本号), [identifier] (先行版本号) 和 [metadata] (元数据).
 * ```
 * SemVersion(
 *   mainVersion = IntArray [1, 0, 0],
 *   identifier  = "M4"
 *   metadata    = "c25733b8"
 * )
 * ```
 * 其中 identifier 和 metadata 都是可选的.
 *
 * 对于核心版本号, 此实现稍微比 semver 宽松一些, 允许 x.y 的存在.
 *
 * @see RangeRequirement
 */
@Serializable(with = SemVersion.SemVersionAsStringSerializer::class)
public data class SemVersion internal constructor(
    /** 核心版本号, 由主版本号, 次版本号和修订号组成, 其中修订号不一定存在 */
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
        public fun test(version: SemVersion): Boolean
    }

    public object SemVersionAsStringSerializer : KSerializer<SemVersion> by String.serializer().map(
        serializer = { it.toString() },
        deserializer = { parse(it) }
    )

    public companion object {
        /**
         * 解析一个版本号, 将会返回一个 [SemVersion],
         * 如果发生解析错误将会抛出一个 [IllegalArgumentException] 或者 [NumberFormatException]
         *
         * 对于版本号的组成, 有以下规定:
         * - 必须包含主版本号和次版本号
         * - 存在 先行版本号 的时候 先行版本号 不能为空
         * - 存在 元数据 的时候 元数据 不能为空
         * - 核心版本号只允许 `x.y` 和 `x.y.z` 的存在
         *     - `1.0-RC` 是合法的
         *     - `1.0.0-RC` 也是合法的, 与 `1.0-RC` 一样
         *     - `1.0.0.0-RC` 是不合法的, 将会抛出一个 [IllegalArgumentException]
         *
         * 注意情况:
         * - 第一个 `+` 之后的所有内容全部识别为元数据
         *     - `1.0+METADATA-M4`, metadata="METADATA-M4"
         * - 如果不确定版本号是否合法, 可以使用 [regex101.com](https://regex101.com/r/vkijKf/1/) 进行检查
         *     - 此实现使用的正则表达式为 `^(0|[1-9]\d*)\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$`
         */
        @Throws(IllegalArgumentException::class, NumberFormatException::class)
        @JvmStatic
        public fun parse(version: String): SemVersion = SemVersionInternal.parse(version)

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
         * 对于多个规则, 也允许使用 `||` 拼接.
         * 例如:
         * - `1.x || 2.x || 3.0.0`
         * - `<= 0.5.3 || >= 1.0.0`
         *
         * 特别注意:
         * - 依赖规则版本号不需要携带版本号元数据, 元数据不参与依赖需求的检查
         * - 如果目标版本号携带有先行版本号, 请不要忘记先行版本号
         */
        @Throws(IllegalArgumentException::class)
        @JvmStatic
        public fun parseRangeRequirement(requirement: String): RangeRequirement = SemVersionInternal.parseRangeRequirement(requirement)

        /** @see [RangeRequirement.test] */
        @JvmStatic
        public fun RangeRequirement.test(version: String): Boolean = test(parse(version))

        /**
         * 当满足 [requirement] 时返回 true, 否则返回 false
         */
        @JvmStatic
        public fun SemVersion.satisfies(requirement: RangeRequirement): Boolean = requirement.test(this)

        /** for Kotlin only */
        @JvmStatic
        @JvmSynthetic
        public operator fun RangeRequirement.contains(version: SemVersion): Boolean = test(version)

        /** for Kotlin only */
        @JvmStatic
        @JvmSynthetic
        public operator fun RangeRequirement.contains(version: String): Boolean = test(version)
    }

    @Transient
    private val toString: String by lazy(LazyThreadSafetyMode.NONE) {
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
        }
    }

    override fun toString(): String = toString

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
        return SemVersionInternal.run { compareInternal(this@SemVersion, other) }
    }
}
