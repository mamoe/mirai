/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import io.ktor.utils.io.core.EOFException
import io.ktor.utils.io.errors.*
import net.mamoe.mirai.internal.message.image.calculateImageInfo
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.MiraiFile
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.readBytes
import net.mamoe.mirai.utils.withUse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ImageReadingTest : AbstractTest() {
    @Test
    fun `test read apng`() {
        "89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00 00 01 E0 00 00 01 90 08 06 00 00 00 76 F6 B3 54 00 00 00 08 61 63 54 4C 00 00 00 22 00 00 00 00 32 4C BC 74 00 00 00 1A 66 63 54 4C 00".testMatch(
            ImageType.APNG
        )
        assertFailsWith(IOException::class) { "89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00".testMatch(ImageType.APNG) }
    }

    @Test
    fun `test read png`() {
        "89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00 00 01 E0 00 00 01 90 08 06 00 00 00 76 F6 B3 54 00 00 00 01 73 52 47 42 00 AE".testMatch(
            ImageType.PNG
        )
        assertFailsWith(IOException::class) { "89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49".testMatch(ImageType.PNG) }
    }

    @Test
    fun `test read gif`() {
        "47 49 46 38 39 61 E0 01 90 01 F7 FF 00 30 A3 B0".testMatch(ImageType.GIF)
        assertFailsWith(IOException::class) { "47 49 46 38 39 61 E0".testMatch(ImageType.GIF) }
    }

    @Test
    fun `test read jpg`() {
        //SOF0
        "FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 78 00 78 00 00 FF E1 00 5A 45 78 69 66 00 00 4D 4D 00 2A 00 00 00 08 00 05 03 01 00 05 00 00 00 01 00 00 00 4A 03 03 00 01 00 00 00 01 00 00 00 00 51 10 00 01 00 00 00 01 01 00 00 00 51 11 00 04 00 00 00 01 00 00 12 74 51 12 00 04 00 00 00 01 00 00 12 74 00 00 00 00 00 01 86 A0 00 00 B1 8F FF DB 00 43 00 02 01 01 02 01 01 02 02 02 02 02 02 02 02 03 05 03 03 03 03 03 06 04 04 03 05 07 06 07 07 07 06 07 07 08 09 0B 09 08 08 0A 08 07 07 0A 0D 0A 0A 0B 0C 0C 0C 0C 07 09 0E 0F 0D 0C 0E 0B 0C 0C 0C FF DB 00 43 01 02 02 02 03 03 03 06 03 03 06 0C 08 07 08 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C FF C0 00 11 08 01 90 01 E0 03 01 22 00 02 11 01 03 11 01 FF DA".testMatch(
            ImageType.JPG
        )
        //SOF2
        "FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 78 00 78 00 00 FF E1 00 5A 45 78 69 66 00 00 4D 4D 00 2A 00 00 00 08 00 05 03 01 00 05 00 00 00 01 00 00 00 4A 03 03 00 01 00 00 00 01 00 00 00 00 51 10 00 01 00 00 00 01 01 00 00 00 51 11 00 04 00 00 00 01 00 00 12 74 51 12 00 04 00 00 00 01 00 00 12 74 00 00 00 00 00 01 86 A0 00 00 B1 8F FF DB 00 43 00 02 01 01 02 01 01 02 02 02 02 02 02 02 02 03 05 03 03 03 03 03 06 04 04 03 05 07 06 07 07 07 06 07 07 08 09 0B 09 08 08 0A 08 07 07 0A 0D 0A 0A 0B 0C 0C 0C 0C 07 09 0E 0F 0D 0C 0E 0B 0C 0C 0C FF DB 00 43 01 02 02 02 03 03 03 06 03 03 06 0C 08 07 08 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C FF C2 00 11 08 01 90 01 E0 03 01 22 00 02 11 01 03 11 01 FF DA".testMatch(
            ImageType.JPG
        )
        //FF 01
        "FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 78 00 78 00 00 FF 01 FF E1 00 5A 45 78 69 66 00 00 4D 4D 00 2A 00 00 00 08 00 05 03 01 00 05 00 00 00 01 00 00 00 4A 03 03 00 01 00 00 00 01 00 00 00 00 51 10 00 01 00 00 00 01 01 00 00 00 51 11 00 04 00 00 00 01 00 00 12 74 51 12 00 04 00 00 00 01 00 00 12 74 00 00 00 00 00 01 86 A0 00 00 B1 8F FF DB 00 43 00 02 01 01 02 01 01 02 02 02 02 02 02 02 02 03 05 03 03 03 03 03 06 04 04 03 05 07 06 07 07 07 06 07 07 08 09 0B 09 08 08 0A 08 07 07 0A 0D 0A 0A 0B 0C 0C 0C 0C 07 09 0E 0F 0D 0C 0E 0B 0C 0C 0C FF DB 00 43 01 02 02 02 03 03 03 06 03 03 06 0C 08 07 08 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C FF C2 00 11 08 01 90 01 E0 03 01 22 00 02 11 01 03 11 01 FF DA".testMatch(
            ImageType.JPG
        )
        //FF 00
        "FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 78 00 78 00 00 FF 00 FF E1 00 5A 45 78 69 66 00 00 4D 4D 00 2A 00 00 00 08 00 05 03 01 00 05 00 00 00 01 00 00 00 4A 03 03 00 01 00 00 00 01 00 00 00 00 51 10 00 01 00 00 00 01 01 00 00 00 51 11 00 04 00 00 00 01 00 00 12 74 51 12 00 04 00 00 00 01 00 00 12 74 00 00 00 00 00 01 86 A0 00 00 B1 8F FF DB 00 43 00 02 01 01 02 01 01 02 02 02 02 02 02 02 02 03 05 03 03 03 03 03 06 04 04 03 05 07 06 07 07 07 06 07 07 08 09 0B 09 08 08 0A 08 07 07 0A 0D 0A 0A 0B 0C 0C 0C 0C 07 09 0E 0F 0D 0C 0E 0B 0C 0C 0C FF DB 00 43 01 02 02 02 03 03 03 06 03 03 06 0C 08 07 08 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C FF C2 00 11 08 01 90 01 E0 03 01 22 00 02 11 01 03 11 01 FF DA".testMatch(
            ImageType.JPG
        )
        //RST[0-7]
        "FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 78 00 78 00 00 FF D0 FF D1 FF D2 FF D3 FF D4 FF D5 FF D6 FF D7 FF E1 00 5A 45 78 69 66 00 00 4D 4D 00 2A 00 00 00 08 00 05 03 01 00 05 00 00 00 01 00 00 00 4A 03 03 00 01 00 00 00 01 00 00 00 00 51 10 00 01 00 00 00 01 01 00 00 00 51 11 00 04 00 00 00 01 00 00 12 74 51 12 00 04 00 00 00 01 00 00 12 74 00 00 00 00 00 01 86 A0 00 00 B1 8F FF DB 00 43 00 02 01 01 02 01 01 02 02 02 02 02 02 02 02 03 05 03 03 03 03 03 06 04 04 03 05 07 06 07 07 07 06 07 07 08 09 0B 09 08 08 0A 08 07 07 0A 0D 0A 0A 0B 0C 0C 0C 0C 07 09 0E 0F 0D 0C 0E 0B 0C 0C 0C FF DB 00 43 01 02 02 02 03 03 03 06 03 03 06 0C 08 07 08 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C FF C2 00 11 08 01 90 01 E0 03 01 22 00 02 11 01 03 11 01 FF DA".testMatch(
            ImageType.JPG
        )
        println("Current path: " + net.mamoe.mirai.utils.MiraiFile.create(".").absolutePath)
        //Issue 1610
        MiraiFile.create("./src/commonTest/resources/image/jpeg-header-issue-1610.bin").readBytes().testRead(
            ImageType.JPG
        )
        //Failed to find
        assertFailsWith(IllegalArgumentException::class) {
            "FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 78 00 78 00 00 FF E1 00 5A 45 78 69 66 00 00 4D 4D 00 2A 00 00 00 08 00 05 03 01 00 05 00 00 00 01 00 00 00 4A 03 03 00 01 00 00 00 01 00 00 00 00 51 10 00 01 00 00 00 01 01 00 00 00 51 11 00 04 00 00 00 01 00 00 12 74 51 12 00 04 00 00 00 01 00 00 12 74 00 00 00 00 00 01 86 A0 00 00 B1 8F FF DB 00 43 00 02 01 01 02 01 01 02 02 02 02 02 02 02 02 03 05 03 03 03 03 03 06 04 04 03 05 07 06 07 07 07 06 07 07 08 09 0B 09 08 08 0A 08 07 07 0A 0D 0A 0A 0B 0C 0C 0C 0C 07 09 0E 0F 0D 0C 0E 0B 0C 0C 0C FF DB 00 43 01 02 02 02 03 03 03 06 03 03 06 0C 08 07 08 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C 0C FF DA".testMatch(
                ImageType.JPG
            )
        }
        assertFailsWith(EOFException::class) {
            "FF D8 FF E0 00 10 4A 46 49 46 00 01 01 01 00 78 00 78 00 00 FF E1 00 5A".testMatch(
                ImageType.JPG
            )
        }
    }

    @Test
    fun `test read bmp`() {
        "42 4D 36 CA 08 00 00 00 00 00 36 00 00 00 28 00 00 00 E0 01 00 00 90 01 00 00 01 00 18 00 00 00 00 00 00 CA 08 00 74 12 00 00 74 12 00 00 00 00 00 00 00 00 00 00"
            .testMatch(ImageType.BMP)
        assertFailsWith(IOException::class) {
            "42 4D 36 CA 08 00 00 00 00 00 36 00 00 00 28 00 00 00 E0 01 00 00 90".testMatch(
                ImageType.BMP
            )
        }
    }

    @Test
    fun `test read fail`() {
        assertFailsWith(IllegalArgumentException::class) {
            "CA FE FE".testMatch(ImageType.BMP)
        }
    }

    private fun ByteArray.testRead(type: ImageType) {
        this.toExternalResource().withUse {
            calculateImageInfo().run {
                assertEquals(type, imageType, "imageType")
            }
        }
    }

    private fun String.testMatch(type: ImageType) {
        this.hexToBytes().toExternalResource().withUse {
            calculateImageInfo().run {
                assertEquals(480, width, "width")
                assertEquals(400, height, "height")
                assertEquals(type, imageType, "imageType")
            }
        }
    }
}