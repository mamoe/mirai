/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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

internal class TlvMapTest {
    private fun dumpTlvMap(map: TlvMap) = buildString {
        append("tlvMap {\n")
        map.forEach { (k, v) ->
            append("  ").append(k.toUHexString()).append(" = ").append(v.toUHexString()).append("\n")
        }
        append("}")
    }

    private fun assertTlvMapEquals(
        expected: TlvMap, actual: TlvMap,
    ) {
        assertEquals(expected.size, actual.size, "map size not match")

        expected.keys.forEach { key ->
            assertTrue("Missing key[$key] in actual") { actual.containsKey(key) }
        }
        actual.keys.forEach { key ->
            assertTrue("Missing key[$key] in expected") { expected.containsKey(key) }
        }

        expected.forEach { (key, value) ->
            assertContentEquals(value, actual[key])
        }
    }

    @Test
    fun testTlvWriterNoLength() {
        testTlvWriter(true)
    }

    @Test
    fun testTlvWriterWithCount() {
        testTlvWriter(false)
    }

    private fun testTlvWriter(withCount: Boolean) {
        repeat(500) {
            val tlvMap = TlvMap()
            val rand = buildPacket {
                _writeTlvMap(Short.SIZE_BYTES, includeCount = withCount) {

                    repeat(Random.nextInt().and(0xFF).coerceAtLeast(20)) {
                        val nextKey = Random.nextInt().and(0xFF0)
                        if (!tlvMap.containsKey(nextKey)) {
                            val randData = ByteArray(Random.nextInt().and(0xFFF))
                            Random.nextBytes(randData)

                            tlvMap[nextKey] = randData

                            tlv(nextKey, randData)
                        }
                    }

                }
            }.also { pkg ->
                if (withCount) pkg.discardExact(2)
            }._readTLVMap()

            try {
                assertTlvMapEquals(tlvMap, rand)
            } catch (e: Throwable) {

                println("gen:  " + dumpTlvMap(tlvMap))
                println("read: " + dumpTlvMap(rand))

                throw e
            }

        }
    }

    @Test
    fun testTlvWriterWithCounter() {
        val expected = buildPacket {
            writeShort(4) // count of TLVs

            writeShort(0x01)
            writeHexWithLength("66ccff")

            writeShort(0x04)
            writeHexWithLength("114514")

            writeShort(0x19)
            writeHexWithLength("198100")

            writeShort(0x233)
            writeHexWithLength("666666")
        }.readBytes()

        val actual = buildPacket {
            _writeTlvMap {
                tlv(0x001) { writeHex("66ccff") }
                tlv(0x004) { writeHex("114514") }
                tlv(0x019) { writeHex("198100") }
                tlv(0x233) { writeHex("666666") }

                println("counter = $counter")
            }
        }.readBytes()

        println(expected.toUHexString())
        println(actual.toUHexString())

        assertContentEquals(expected, actual)
    }

    private fun Output.writeHex(data: String) {
        writeFully(data.hexToBytes())
    }

    private fun Output.writeHexWithLength(data: String) {
        val hxd = data.hexToBytes()
        writeShort(hxd.size.toShort())
        writeFully(hxd)
    }
}