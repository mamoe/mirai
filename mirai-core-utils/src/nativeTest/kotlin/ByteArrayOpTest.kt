/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ByteArrayOpTest {

    @Test
    fun testAvailableProcessors() {
        val processors = availableProcessors()
        assertTrue(processors.toString()) { processors > 0 }
    }

    @Test
    fun testMd5() {
        val str = getRandomString(10, Random(1))
        println(str)
        val hash = str.md5()
        assertContentEquals(
            "30 3B 36 B3 42 00 39 E2 EC 18 22 79 10 32 05 48".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testMd5WithOffset() {
        val str = getRandomString(10, Random(1))
        println(str)
        val hash = (byteArrayOf(1) + str.toByteArray()).md5(1)
        assertContentEquals(
            "30 3B 36 B3 42 00 39 E2 EC 18 22 79 10 32 05 48".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testSha1() {
        val str = getRandomString(10, Random(1))
        println(str)
        val hash = str.sha1()
        assertContentEquals(
            "54 98 CD 62 6C DE E3 9B 96 D4 34 5E 13 51 48 BB FC 32 1C 48".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testDeflate() {
        val str = "qGnJ1RrFC9"
        println(str)
        val hash = str.toByteArray().deflate()
        assertContentEquals(
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testInflate() {
        val result =
            "78 9C 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 12 82 03 28".hexToBytes()
                .inflate().decodeToString()
        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }

    @Test
    fun testGzip() {
        val str = "qGnJ1RrFC9"
        println(str)
        val hash = str.toByteArray().gzip()
        assertContentEquals(
            "1F 8B 08 00 00 00 00 00 00 13 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 A8 35 6D D9 0A 00 00 00".hexToBytes(),
            hash,
            message = hash.toUHexString()
        )
    }

    @Test
    fun testUnGzip() {
        val result =
            "1F 8B 08 00 00 00 00 00 00 FF 2B 74 CF F3 32 0C 2A 72 73 B6 04 00 A8 35 6D D9 0A 00 00 00".hexToBytes()
                .ungzip().decodeToString()
        assertEquals(
            "qGnJ1RrFC9",
            result,
            message = result
        )
    }
}