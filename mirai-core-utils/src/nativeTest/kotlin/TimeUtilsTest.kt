/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class TimeUtilsTest {

    @Test
    fun `can get currentTimeMillis`() {
        val time = currentTimeMillis()
        assertTrue(time.toString()) { time > 1654209523269 }
    }

    @Test
    fun `can get currentTimeFormatted`() {
        // 2022-28-26 18:28:28
        assertTrue { currentTimeFormatted().matches(Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$""")) }
    }

    @Test
    fun `can parse explicit timestamp`() {
        val epochMilli = 1681174590123 // 2023-04-11 00:56:30 GMT
        val regex = Regex("""^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$""")

        val formatted = regex.find(formatTime(epochMilli, null))
        assertNotNull(formatted)

        formatted.groupValues.run {
            assertEquals(get(1), "2023")
            assertEquals(get(2), "04")
            assertTrue { get(3) == "11" || get(3) == "10" }
            assertTrue { get(4).toInt() in 0..23 }
            assertEquals(get(5), "56")
            assertEquals(get(6), "30")
        }
    }

    @Test
    fun `can format with custom formatter`() {
        fun formatTimeAndPrint(formatter: String?): String {
            return formatTime(currentTimeMillis(), formatter).also { println("custom formatted time: $it") }
        }

        assertTrue {
            formatTimeAndPrint("MmMm").matches(Regex("""^MmMm$"""))
        }
        assertTrue {
            formatTimeAndPrint("MM-mm").matches(Regex("""^\d{2}-\d{2}$"""))
        }
        assertTrue {
            formatTimeAndPrint("yyyyMMddHHmmss").matches(Regex("""^\d{14}$"""))
        }
        assertTrue {
            formatTimeAndPrint("yyyyMMddHHmmSS").matches(Regex("""^\d{12}SS$"""))
        }
        assertTrue {
            formatTimeAndPrint(null).matches(Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$"""))
        }
        assertTrue {
            formatTimeAndPrint("yyyy-MM-dd 114514").matches(Regex("""^\d{4}-\d{2}-\d{2} 114514$"""))
        }
        assertTrue {
            formatTimeAndPrint("yyyyMM-114 514--mm-SS").matches(Regex("""^\d{4}\d{2}-114 514--\d{2}-SS$"""))
        }
        assertTrue {
            formatTimeAndPrint("yyyy-MM-dd HH-mm-ss").matches(Regex("""^\d{4}-\d{2}-\d{2} \d{2}-\d{2}-\d{2}$"""))
        }
        assertTrue {
            formatTimeAndPrint("yyyy/MM\\dd HH:mm-ss").matches(Regex("""^\d{4}/\d{2}\\\d{2} \d{2}:\d{2}-\d{2}$"""))
        }
    }
}