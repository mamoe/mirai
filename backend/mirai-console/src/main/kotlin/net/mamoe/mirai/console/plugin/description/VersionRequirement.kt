package net.mamoe.mirai.console.plugin.description

import com.vdurmont.semver4j.Requirement
import com.vdurmont.semver4j.Semver

public sealed class VersionRequirement {
    public abstract operator fun contains(version: Semver): Boolean
    public fun contains(version: String): Boolean = contains(Semver(version, Semver.SemverType.LOOSE))

    public data class Exact(
        val version: Semver,
    ) : VersionRequirement() {
        public constructor(version: String) : this(Semver(version, Semver.SemverType.LOOSE))

        override fun contains(version: Semver): Boolean = this.version.isEqualTo(version)
    }

    public data class MatchesNpmPattern(
        val pattern: String,
    ) : VersionRequirement() {
        private val requirement = Requirement.buildNPM(pattern)
        override fun contains(version: Semver): Boolean = requirement.isSatisfiedBy(version)
    }

    public data class MatchesIvyPattern(
        val pattern: String,
    ) : VersionRequirement() {
        private val requirement = Requirement.buildIvy(pattern)
        override fun contains(version: Semver): Boolean = requirement.isSatisfiedBy(version)
    }


    public data class MatchesCocoapodsPattern(
        val pattern: String,
    ) : VersionRequirement() {
        private val requirement = Requirement.buildCocoapods(pattern)
        override fun contains(version: Semver): Boolean = requirement.isSatisfiedBy(version)
    }

    public abstract class Custom : VersionRequirement()

    public data class InRange(
        val begin: Semver,
        val beginInclusive: Boolean,
        val end: Semver,
        val endInclusive: Boolean,
    ) : VersionRequirement() {
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
            return if (beginInclusive) {
                version.isGreaterThanOrEqualTo(begin)
            } else {
                version.isGreaterThan(begin)
            } && if (endInclusive) {
                version.isLowerThanOrEqualTo(begin)
            } else {
                version.isLowerThan(begin)
            }
        }
    }


    @Suppress("unused")
    public class Builder {
        @ILoveKafuuChinoForever
        public fun exact(version: Semver): VersionRequirement = Exact(version)

        @ILoveKafuuChinoForever
        public fun exact(version: String): VersionRequirement = Exact(version)

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

        @ILoveKafuuChinoForever
        public fun range(
            begin: Semver,
            beginInclusive: Boolean,
            end: Semver,
            endInclusive: Boolean,
        ): VersionRequirement = InRange(begin, beginInclusive, end, endInclusive)

        @ILoveKafuuChinoForever
        public fun range(
            begin: String,
            beginInclusive: Boolean,
            end: Semver,
            endInclusive: Boolean,
        ): VersionRequirement = InRange(begin, beginInclusive, end, endInclusive)

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

        @ILoveKafuuChinoForever
        public operator fun String.rangeTo(endInclusive: Semver): VersionRequirement {
            return InRange(Semver(this, Semver.SemverType.LOOSE), true, endInclusive, true)
        }


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