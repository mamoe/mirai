package net.mamoe.mirai.console.plugin.description

import com.vdurmont.semver4j.Requirement
import com.vdurmont.semver4j.Semver

public sealed class VersionRequirement {
    public abstract operator fun contains(version: Semver): Boolean
    public fun contains(version: String): Boolean = contains(Semver(version, Semver.SemverType.LOOSE))

    public class Exact
    @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
    constructor(
        version: Semver,
    ) : VersionRequirement() {
        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        public val version: Semver = version.toStrict()

        @Suppress("DEPRECATION_ERROR")
        public constructor(version: String) : this(Semver(version, Semver.SemverType.LOOSE))

        @Suppress("DEPRECATION_ERROR")
        override fun contains(version: Semver): Boolean = this.version.isEquivalentTo(version.toStrict())
    }

    public data class MatchesNpmPattern(
        val pattern: String,
    ) : VersionRequirement() {
        private val requirement = Requirement.buildNPM(pattern)
        override fun contains(version: Semver): Boolean = requirement.isSatisfiedBy(version.toStrict())
    }

    public data class MatchesIvyPattern(
        val pattern: String,
    ) : VersionRequirement() {
        private val requirement = Requirement.buildIvy(pattern)
        override fun contains(version: Semver): Boolean = requirement.isSatisfiedBy(version.toStrict())
    }


    public data class MatchesCocoapodsPattern(
        val pattern: String,
    ) : VersionRequirement() {
        private val requirement = Requirement.buildCocoapods(pattern)
        override fun contains(version: Semver): Boolean = requirement.isSatisfiedBy(version.toStrict())
    }

    public abstract class Custom : VersionRequirement()

    @Suppress("MemberVisibilityCanBePrivate")
    public class InRange(
        begin: Semver,
        public val beginInclusive: Boolean,
        end: Semver,
        public val endInclusive: Boolean,
    ) : VersionRequirement() {
        public val end: Semver = end.toStrict()
        public val begin: Semver = begin.toStrict()

        public constructor(
            begin: String,
            beginInclusive: Boolean,
            end: Semver,
            endInclusive: Boolean,
        ) : this(Semver(begin, Semver.SemverType.LOOSE), beginInclusive, end, endInclusive)

        public constructor(
            begin: String,
            beginInclusive: Boolean,
            end: String,
            endInclusive: Boolean,
        ) : this(Semver(begin, Semver.SemverType.LOOSE),
            beginInclusive,
            Semver(end, Semver.SemverType.LOOSE),
            endInclusive)

        public constructor(
            begin: Semver,
            beginInclusive: Boolean,
            end: String,
            endInclusive: Boolean,
        ) : this(begin, beginInclusive, Semver(end, Semver.SemverType.LOOSE), endInclusive)

        override fun contains(version: Semver): Boolean {
            val strict = version.toStrict()
            return if (beginInclusive) {
                strict.isGreaterThanOrEqualTo(begin)
            } else {
                strict.isGreaterThan(begin)
            } && if (endInclusive) {
                strict.isLowerThanOrEqualTo(end)
            } else {
                strict.isLowerThan(end)
            }
        }

        override fun toString(): String {
            return buildString {
                append(if (beginInclusive) "[" else "(")
                append(begin)
                append(",")
                append(end)
                append(if (endInclusive) "]" else ")")
            }
        }
    }


    @Suppress("unused", "DeprecatedCallableAddReplaceWith")
    public class Builder {
        @Suppress("DEPRECATION_ERROR")
        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public fun exact(version: Semver): VersionRequirement = Exact(version)

        @ILoveKafuuChinoForever
        public fun exact(version: String): VersionRequirement = Exact(version)

        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public fun custom(checker: (version: Semver) -> Boolean): VersionRequirement {
            return object : Custom() {
                override fun contains(version: Semver): Boolean = checker(version)
            }
        }

        /**
         * @see Semver.SemverType.NPM
         */
        @ILoveKafuuChinoForever
        public fun npmPattern(versionPattern: String): VersionRequirement {
            return MatchesNpmPattern(versionPattern)
        }

        /**
         * @see Semver.SemverType.IVY
         */
        @ILoveKafuuChinoForever
        public fun ivyPattern(versionPattern: String): VersionRequirement {
            return MatchesIvyPattern(versionPattern)
        }

        /**
         * @see Semver.SemverType.COCOAPODS
         */
        @ILoveKafuuChinoForever
        public fun cocoapodsPattern(versionPattern: String): VersionRequirement {
            return MatchesCocoapodsPattern(versionPattern)
        }

        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public fun range(
            begin: Semver,
            beginInclusive: Boolean,
            end: Semver,
            endInclusive: Boolean,
        ): VersionRequirement = InRange(begin, beginInclusive, end, endInclusive)

        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public fun range(
            begin: String,
            beginInclusive: Boolean,
            end: Semver,
            endInclusive: Boolean,
        ): VersionRequirement = InRange(begin, beginInclusive, end, endInclusive)

        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public fun range(
            begin: Semver,
            beginInclusive: Boolean,
            end: String,
            endInclusive: Boolean,
        ): VersionRequirement = InRange(begin, beginInclusive, end, endInclusive)

        @ILoveKafuuChinoForever
        public fun range(
            begin: String,
            beginInclusive: Boolean,
            end: String,
            endInclusive: Boolean,
        ): VersionRequirement = InRange(begin, beginInclusive, end, endInclusive)


        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public operator fun Semver.rangeTo(endInclusive: Semver): VersionRequirement {
            return InRange(this, true, endInclusive, true)
        }

        @ILoveKafuuChinoForever
        public operator fun Semver.rangeTo(endInclusive: String): VersionRequirement {
            return InRange(this, true, Semver(endInclusive, Semver.SemverType.LOOSE), true)
        }

        @ILoveKafuuChinoForever
        public operator fun String.rangeTo(endInclusive: String): VersionRequirement {
            return InRange(Semver(this, Semver.SemverType.LOOSE),
                true,
                Semver(endInclusive, Semver.SemverType.LOOSE),
                true)
        }

        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public operator fun String.rangeTo(endInclusive: Semver): VersionRequirement {
            return InRange(Semver(this, Semver.SemverType.LOOSE), true, endInclusive, true)
        }


        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public infix fun Semver.until(endExclusive: Semver): VersionRequirement {
            return InRange(this, true, endExclusive, false)
        }

        @ILoveKafuuChinoForever
        public infix fun Semver.until(endExclusive: String): VersionRequirement {
            return InRange(this, true, Semver(endExclusive, Semver.SemverType.LOOSE), false)
        }

        @ILoveKafuuChinoForever
        public infix fun String.until(endExclusive: String): VersionRequirement {
            return InRange(Semver(this, Semver.SemverType.LOOSE),
                true,
                Semver(endExclusive, Semver.SemverType.LOOSE),
                false)
        }

        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @ILoveKafuuChinoForever
        public infix fun String.until(endExclusive: Semver): VersionRequirement {
            return InRange(Semver(this, Semver.SemverType.LOOSE), true, endExclusive, false)
        }

        @Suppress("SpellCheckingInspection")
        @Retention(AnnotationRetention.SOURCE)
        @DslMarker
        private annotation class ILoveKafuuChinoForever
    }
}