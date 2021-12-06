/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

/*
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */

package net.mamoe.mirai.console.util

import net.mamoe.mirai.console.util.SemVersion.Companion.test
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class TestSemVersion {
    @Test
    internal fun testCompare() {
        fun String.sem(): SemVersion = SemVersion.invoke(this)
        assert("1.0".sem() < "1.0.1".sem())
        assert("1.0.0".sem() != "1.0".sem())
        assert("1.0.0".sem().compareTo("1.0".sem()) == 0)
        assert("1.1".sem() > "1.0.0".sem())
        assert("1.0-M4".sem() < "1.0-M5".sem())
        assert("1.0-M5-dev-7".sem() < "1.0-M5-dev-15".sem())
        assert("1.0-M5-dev-79".sem() < "1.0-M5-dev-7001".sem())
        assert("1.0-M6".sem() > "1.0-M5-dev-15".sem())
        assert("1.0-RC".sem() > "1.0-M5-dev-15".sem())
        assert("1.0-RC2".sem() > "1.0-RC".sem())
        // example on semver
        // 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0
        assert("1.0.0-alpha".sem() < "1.0.0-alpha.1".sem())
        assert("1.0.0-alpha.1".sem() < "1.0.0-alpha.beta".sem())
        assert("1.0.0-alpha.beta".sem() < "1.0.0-beta".sem())
        assert("1.0.0-beta".sem() < "1.0.0-beta.2".sem())
        assert("1.0.0-beta.2".sem() < "1.0.0-beta.11".sem())
        assert("1.0.0-beta.11".sem() < "1.0.0-rc.1".sem())
        assert("1.0.0-rc.1".sem() < "1.0.0".sem())
    }

    @Test
    internal fun testRequirementCopy() {
        fun SemVersion.Requirement.check(a: SemVersion.Requirement.() -> SemVersion.Requirement) {
            assert(a().impl !== this.impl)
        }
        SemVersion.parseRangeRequirement("1.0").check { copy() }
        SemVersion.parseRangeRequirement("1.0").check { copy("2.0") }
        SemVersion.parseRangeRequirement("1.0").check { copy("1.0") }
    }

    @Test
    internal fun testRequirement() {
        fun SemVersion.Requirement.assert(version: String): SemVersion.Requirement {
            assert(test(version)) { version }
            return this
        }

        fun assertInvalid(requirement: String) {
            assertFails(requirement) {
                SemVersion.parseRangeRequirement(requirement)
            }
        }

        fun SemVersion.Requirement.assertFalse(version: String): SemVersion.Requirement {
            assert(!test(version)) { version }
            return this
        }
        SemVersion.parseRangeRequirement("1.0")
            .assert("1.0").assert("1.0.0")
            .assertFalse("1.1.0").assertFalse("2.0.0")
        SemVersion.parseRangeRequirement("1.x")
            .assert("1.0").assert("1.1")
            .assert("1.5").assert("1.14514")
            .assertFalse("2.33")
        SemVersion.parseRangeRequirement("2.0||1.2.x")
        SemVersion.parseRangeRequirement("{2.0||1.2.x} && 1.1.0 &&1.2.3")
        SemVersion.parseRangeRequirement("2.0 || 1.2.x")
            .assert("2.0").assert("2.0.0")
            .assertFalse("2.1")
            .assert("1.2.5").assert("1.2.0").assertFalse("1.2")
            .assertFalse("1.0.0")
        SemVersion.parseRangeRequirement("[1.0.0, 19190.0]")
            .assert("1.0.0").assertFalse("0.1.0")
            .assert("19190.0").assertFalse("19198.10")
        SemVersion.parseRangeRequirement("[1.0.0, 2.0.0)")
            .assert("1.0.0").assert("1.2.3").assertFalse("2.0.0")
        SemVersion.parseRangeRequirement("(2.0.0, 1.0.0]")
            .assert("1.0.0").assert("1.2.3").assertFalse("2.0.0")
        SemVersion.parseRangeRequirement("(2.0.0, 1.0.0)")
            .assertFalse("1.0.0").assert("1.2.3").assertFalse("2.0.0")
        SemVersion.parseRangeRequirement("(1.0.0, 2.0.0)")
            .assertFalse("1.0.0").assert("1.2.3").assertFalse("2.0.0")
        SemVersion.parseRangeRequirement(" >= 1.0.0")
            .assert("1.0.0")
            .assert("114.514.1919")
            .assertFalse("0.0.0")
            .assertFalse("0.98774587")
        SemVersion.parseRangeRequirement("> 1.0.0")
            .assertFalse("1.0.0")
        SemVersion.parseRangeRequirement("!= 1.0.0 && != 2.0.0")
            .assert("1.2.3").assert("2.1.1")
            .assertFalse("1.0").assertFalse("1.0.0")
            .assertFalse("2.0").assertFalse("2.0.0")
            .assert("2.0.1").assert("1.0.1")

        SemVersion.parseRangeRequirement("> 1.0.0 || < 0.9.0")
            .assertFalse("1.0.0")
            .assert("0.8.0")
            .assertFalse("0.9.0")
        SemVersion.parseRangeRequirement("{>= 1.0.0 && <= 1.2.3} || {>= 2.0.0 && <= 2.2.3}")
            .assertFalse("1.3.0")
            .assert("1.0.0").assert("1.2.3")
            .assertFalse("0.9.0")
            .assert("2.0.0").assert("2.2.3").assertFalse("2.3.4")

        assertInvalid("WPOXAXW")
        assertInvalid("1.0.0 || 1.0.0 && 1.0.0")
        assertInvalid("{")
        assertInvalid("}")
        assertInvalid("")
        assertInvalid("1.2.3 - 3.2.1")
        assertInvalid("1.5.78 &&")
        assertInvalid("|| 1.0.0")
    }

    private fun String.check() {
        val sem = SemVersion.invoke(this)
        assert(this == sem.toString()) { "$this != $sem" }
    }

    private fun String.checkInvalid() {
        kotlin.runCatching { SemVersion.invoke(this) }
            .onSuccess { assert(false) { "$this not a invalid sem-version" } }
    }

    @Test
    internal fun testSemVersionParsing() {
        "0.0".check()
        "1.0.0".check()
        "1.2.3.4.5.6.7.8".checkInvalid()
        "5555.0-A".check()
        "5555.0-A+METADATA".check()
        "5555.0+METADATA".check()
        "987.0+wwwxx-wk".check()
        "NOT.NUMBER".checkInvalid()
        "0".checkInvalid()
        "".checkInvalid()
        "1.".checkInvalid()
        "0.1-".checkInvalid()
        "1.9+".checkInvalid()
        "5.1+68-7".check()
        "5.1+68-".check()
    }

    @Test
    internal fun testSemVersionOfficial() {
        """
            1.0-RC
            0.0.4
            1.2.3
            10.20.30
            1.1.2-prerelease+meta
            1.1.2+meta
            1.1.2+meta-valid
            1.0.0-alpha
            1.0.0-beta
            1.0.0-alpha.beta
            1.0.0-alpha.beta.1
            1.0.0-alpha.1
            1.0.0-alpha0.valid
            1.0.0-alpha.0valid
            1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay
            1.0.0-rc.1+build.1
            2.0.0-rc.1+build.123
            1.2.3-beta
            10.2.3-DEV-SNAPSHOT
            1.2.3-SNAPSHOT-123
            1.0.0
            2.0.0
            1.1.7
            2.0.0+build.1848
            2.0.1-alpha.1227
            1.0.0-alpha+beta
            1.2.3----RC-SNAPSHOT.12.9.1--.12+788
            1.2.3----R-S.12.9.1--.12+meta
            1.2.3----RC-SNAPSHOT.12.9.1--.12
            1.0.0+0.build.1-rc.10000aaa-kk-0.1
            1.0.0-0A.is.legal
        """.trimIndent().split('\n').asSequence()
            .filter { it.isNotBlank() }.map { it.trim() }.forEach { it.check() }
        """
            1
            1.2.3-0123
            1.2.3-0123.0123
            1.1.2+.123
            +invalid
            -invalid
            -invalid+invalid
            -invalid.01
            alpha
            alpha.beta
            alpha.beta.1
            alpha.1
            alpha+beta
            alpha_beta
            alpha.
            alpha..
            beta
            1.0.0-alpha_beta
            -alpha.
            1.0.0-alpha..
            1.0.0-alpha..1
            1.0.0-alpha...1
            1.0.0-alpha....1
            1.0.0-alpha.....1
            1.0.0-alpha......1
            1.0.0-alpha.......1
            01.1.1
            1.01.1
            1.1.01
            1.2.3.DEV
            1.2.31.2.3----RC-SNAPSHOT.12.09.1--..12+788
            1.2.31.2.3-RC
            -1.0.3-gamma+b7718
            +justmeta
            9.8.7+meta+meta
            9.8.7-whatever+meta+meta
            99999999999999999999999.999999999999999999.99999999999999999----RC-SNAPSHOT.12.09.1--------------------------------..12
        """.trimIndent().split('\n').asSequence()
            .filter { it.isNotBlank() }.map { it.trim() }.forEach { it.checkInvalid() }
    }
}