/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal actual class ByteArrayOpTest : CommonByteArrayOpTest() {


    private fun <R> withZlibBufferSize(size: Long, block: () -> R): R {
        val o = ZLIB_BUFFER_SIZE
        ZLIB_BUFFER_SIZE = size
        try {
            return block()
        } finally {
            ZLIB_BUFFER_SIZE = o
        }
    }

    @Test
    fun testDeflateBig() {
        withZlibBufferSize(8192) { // use smaller buffer to check.
            val str = sampleLongText.repeat(8) // 4000+ chars
            println("Input size: ${str.length}")
            println("Deflating")
            println()
            val bytes = str.toByteArray().deflate()
            println()
            println("Deflated, size = ${bytes.size}, content = ${bytes.toUHexString()}")
            println()
            println("Inflating")
            println()
            val inflated = bytes.inflate()
            println()
            println("Inflated, size = ${inflated.size}, content = ${inflated.toUHexString()}")
            println()
            assertEquals(str, inflated.decodeToString())
        }
    }


    @Test
    fun testInflateBigDataFromJvm() {
        withZlibBufferSize(8192) { // use smaller buffer to check.
            val input =
                "78 9C ED 92 31 8E 1B 31 0C 45 AF C2 2E CD C2 B0 8B 5D 20 65 4A 03 0E B0 57 E0 68 68 0F 61 89 12 44 CA B3 73 FB E5 C8 09 E2 23 A4 50 27 41 E4 E7 E7 D7 BB E4 4A 09 CE 45 5B 02 56 50 4E 25 6E 30 B7 94 36 30 FA 32 C8 57 B0 85 A0 54 16 63 B9 01 CA 0C B6 15 52 B2 7E 67 99 9B 5A DD 0E 70 79 91 5A 50 61 22 92 DE FB B7 E4 87 EB 9B F7 63 9D 5F 27 D0 83 AA 0F 96 40 BD FA F4 7E 3C EA 1B AC 8B 77 A3 40 93 BB E4 55 9E 06 BC D0 72 BE 03 C2 0D 63 A4 AD BB 73 33 DD 95 86 8A 69 8A 34 03 9B 97 41 C2 BB 3F 3C DF B5 50 E0 E4 8A 93 B7 1F E0 6C DD A1 B6 FA E0 87 37 48 F6 45 C5 17 BF FA 15 02 89 B5 CA E4 2E A6 66 80 51 73 77 16 09 8B 2F E3 D2 14 29 58 CD C2 E1 35 8B 37 F0 00 90 65 8F 85 54 5D 85 DD E5 E6 2B 84 05 E5 46 73 1F BC FA E0 92 4B 8B 58 59 77 B3 CF 90 4E 3F 3F 8E 0A 2B DB D2 AF D5 47 A0 D2 BE E0 85 AC FA D1 40 17 22 53 08 59 EC CF 94 D7 C8 0B AA E2 6D 37 BD 87 91 FC C5 45 F6 55 DC 41 97 9D 49 EF 96 0B 94 36 45 D6 65 EF D7 7C B5 15 BD 32 B2 67 F5 2B FA 3F C1 A7 8B FC F6 E8 AA 3B 0B B1 CD 7B 9D FF 90 72 16 ED 76 FE CD 3C 5C 06 3D 83 9E 41 CF A0 67 D0 33 E8 19 F4 0C 7A 06 3D 83 9E 41 CF A0 67 D0 33 E8 19 F4 FC 87 F4 7C 03 ED CB 93 EB"
                    .hexToBytes()
            println()
            val bytes = input.inflate()
            println("Inflated, size = ${bytes.size}, content = ${bytes.toUHexString()}")
            println()
            val expected = sampleLongText.repeat(8)
            val actual = bytes.decodeToString()
            println(expected)
            println(actual)
            assertEquals(expected.length, actual.length)
            assertEquals(expected, actual)
        }
    }

}