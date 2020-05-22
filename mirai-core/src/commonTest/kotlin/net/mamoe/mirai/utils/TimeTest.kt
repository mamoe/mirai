/*
 * Copyright 2020 Mamoe Technologies and contributors.
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

internal class TimeTest {
    @ExperimentalTime
    @Test
    fun testTimeHumanReadable() {
        val time0 = 1.toDuration(DurationUnit.DAYS) +
                20.toDuration(DurationUnit.HOURS) +
                15.toDuration(DurationUnit.MINUTES) +
                2057.toDuration(DurationUnit.MILLISECONDS)
        println(time0.asHumanReadable)
        assertTrue { time0.asHumanReadable == "1d 20h 15min 2.057s" }
        val time1 = 1.toDuration(DurationUnit.DAYS) + 59.toDuration(DurationUnit.MINUTES)
        println(time1.asHumanReadable)
        assertTrue { time1.asHumanReadable == "1d 59min 0.0s" }
    }
}