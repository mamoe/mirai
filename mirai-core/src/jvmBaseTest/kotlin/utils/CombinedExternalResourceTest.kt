/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.text.toByteArray

class CombinedExternalResourceTest : AbstractTest() {
    @Test
    fun `work`() {
        val res1 = STRING_1.toByteArray().toExternalResource()
        val res2 = STRING_2.toByteArray().toExternalResource()

        val combined1 = buildPacket {
            res1.input().use { it.copyTo(this) }
            res2.input().use { it.copyTo(this) }
        }.readBytes().toExternalResource()

        val combined2 = CombinedExternalResource(res1, res2)

        assertEquals(combined1.size, combined2.size)
        assertTrue { combined1.md5.contentEquals(combined2.md5) }
        assertTrue { combined1.sha1.contentEquals(combined2.sha1) }
    }


    private val STRING_1 = """
        b4FNDvv49gMInP29t82fPJuWQ4ArG1k1YVeCN3UReWXplm4H2S4Rp7zTpt8WXRQEtTL7VemlTIytPbwUkus7qgPVsyUCFreRR1vB3QhRznXqcT06fDkXJQJKyyBGEdwddNWZAkqZcdrOk679sG14kKK5GexaQUmdfTivT5VPO8w1yoWPcUHPfpjB0shCEzjkHI84LJbWNRCVjoZhy0jZAKZxLrsi1sGhl30QcXCFnHpPhWbED8Er9c8gVbjYsG8ejaUlbeNNdKW3GoOpgjFLbwZoQI4QZZgvP5jhBWUPiMG3MCcPlYRSgTf70JpDVTE0YOLhXdJJxz87S8MR4M7rU0WO7ZRkoFOQpFHdmfMmJxbiATHHkOyHVhu1mvA0L72MNtDQP5GcKlDbDcdJL7om4FmekAVVnh7R
    """.trimIndent()
    private val STRING_2 = """
        FdDoAZt2hJkKAfEWBNWO44R0tJRmApqIwHDD05oW0jyLVVPOdcPaFjY1muYM1qa6jbhZppWYm1oOmgbpFgdPZRYDgzznR0kSapdqXeSSevV4ww4E1U71ELDMsq4f0a1Y8K6UxIOpQl1n20eoe80fHuXKkfN6kbhROBXcwGbiFRpPg5k8G5hCerQQunQyNoeEZrbKacq2OYkOEJV57LuSbBTF4FMZYxCEp1a8omnK1EUHC1Go5pGy0dovz78KpCshPr7MHNMnRu0FiuJ1WYT8ri8iXWsTx3AMxHRjCYfJgrtqc86L3HW0V6Wr8FqFMJLtFl4PgXj5etfRSaaqRJFIZ3nWiRqW48JMRqdGRvLTUWs1Zoa8H11bych18MVypUQJOyxghLLJw0ZP4CvSNUeJOEMitxFxyzjC
    """.trimIndent()
}