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

import net.mamoe.mirai.console.util.SemVersion.Companion.check
import org.junit.jupiter.api.Test

internal class TestSemVersion {
    @Test
    internal fun testCompare() {
        fun String.sem(): SemVersion = SemVersion.parse(this)
        assert("1.0".sem() < "1.0.1".sem())
        assert("1.0.0".sem() == "1.0".sem())
        assert("1.1".sem() > "1.0.0.1".sem())
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
    internal fun testRequirement() {
        fun SemVersion.RangeRequirement.assert(version: String): SemVersion.RangeRequirement {
            assert(check(version)) { version }
            return this
        }

        fun SemVersion.RangeRequirement.assertFalse(version: String): SemVersion.RangeRequirement {
            assert(!check(version)) { version }
            return this
        }
        SemVersion.parseRangeRequirement("1.0")
            .assert("1.0").assert("1.0.0")
            .assert("1.0.0.0")
            .assertFalse("1.1.0").assertFalse("2.0.0")
        SemVersion.parseRangeRequirement("1.x")
            .assert("1.0").assert("1.1")
            .assert("1.5").assert("1.14514")
            .assertFalse("2.33")
        SemVersion.parseRangeRequirement("2.0 || 1.2.x")
            .assert("2.0").assert("2.0.0")
            .assertFalse("2.1").assertFalse("2.0.0.1")
            .assert("1.2.5").assert("1.2.0").assertFalse("1.2")
            .assertFalse("1.0.0")
        SemVersion.parseRangeRequirement("1.0.0 - 114.514.1919.810")
            .assert("1.0.0")
            .assert("114.514").assert("114.514.1919.810")
            .assertFalse("0.0.1")
            .assertFalse("4444.4444")
        SemVersion.parseRangeRequirement("[1.0.0, 19190.0]")
            .assert("1.0.0").assertFalse("0.1.0")
            .assert("19190.0").assertFalse("19198.10")
        SemVersion.parseRangeRequirement(" >= 1.0.0")
            .assert("1.0.0")
            .assert("114.514.1919.810")
            .assertFalse("0.0.0")
            .assertFalse("0.98774587")
        SemVersion.parseRangeRequirement("> 1.0.0")
            .assertFalse("1.0.0")
        kotlin.runCatching { SemVersion.parseRangeRequirement("WPOXAXW") }
            .onSuccess { assert(false) }

    }

    @Test
    internal fun testSemVersionParsing() {
        fun String.check() {
            val sem = SemVersion.parse(this)
            assert(this == sem.toString()) { "$this != $sem" }
        }

        fun String.checkInvalid() {
            kotlin.runCatching { SemVersion.parse(this) }
                .onSuccess { assert(false) { "$this not a invalid sem-version" } }
                .onFailure { println("$this - $it") }
        }
        "0.0".check()
        "1.0.0".check()
        "1.2.3.4.5.6.7.8".check()
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
}