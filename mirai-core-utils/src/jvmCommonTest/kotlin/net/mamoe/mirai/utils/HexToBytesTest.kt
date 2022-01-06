/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HexToBytesTest {
    private fun Byte.Companion.parseFromHexChunk(char1: String): Byte = parseFromHexChunk(char1[0], char1[1])

    @Test
    fun `Byte parseFromHexChunk`() {
        assertEquals(0xff.toByte(), Byte.parseFromHexChunk("FF"))
        assertEquals(0xff.toByte(), Byte.parseFromHexChunk("ff"))
        assertEquals(0xff.toByte(), Byte.parseFromHexChunk("fF"))
        assertEquals(0xff.toByte(), Byte.parseFromHexChunk("Ff"))

        assertEquals(0x00.toByte(), Byte.parseFromHexChunk("00"))
        assertEquals(0x0f.toByte(), Byte.parseFromHexChunk("0f"))
        assertEquals(0x34.toByte(), Byte.parseFromHexChunk("34"))
        assertEquals(0x7f.toByte(), Byte.parseFromHexChunk("7f"))
    }

    @Test
    fun `test countHexBytes`() {
        assertEquals(0, "".countHexBytes())

        assertEquals(1, "01".countHexBytes())
        assertEquals(1, "FF".countHexBytes())
        assertEquals(1, "ff".countHexBytes())
        assertEquals(1, "Ff".countHexBytes())
        assertEquals(1, "fF".countHexBytes())
        assertEquals(1, "0F".countHexBytes())
        assertEquals(1, "F0".countHexBytes())
        assertEquals(1, "0f".countHexBytes())
        assertEquals(1, "f0".countHexBytes())

        assertEquals(1, "01 ".countHexBytes())
        assertEquals(1, "FF ".countHexBytes())
        assertEquals(1, "ff ".countHexBytes())
        assertEquals(1, "Ff ".countHexBytes())
        assertEquals(1, "fF ".countHexBytes())
        assertEquals(1, "0F ".countHexBytes())
        assertEquals(1, "F0 ".countHexBytes())
        assertEquals(1, "0f ".countHexBytes())
        assertEquals(1, "f0 ".countHexBytes())

        assertEquals(1, " 01 ".countHexBytes())
        assertEquals(1, " FF ".countHexBytes())
        assertEquals(1, " ff ".countHexBytes())
        assertEquals(1, " Ff ".countHexBytes())
        assertEquals(1, " fF ".countHexBytes())
        assertEquals(1, " 0F ".countHexBytes())
        assertEquals(1, " F0 ".countHexBytes())
        assertEquals(1, " 0f ".countHexBytes())
        assertEquals(1, " f0 ".countHexBytes())

        assertEquals(1, " 01    ".countHexBytes())
        assertEquals(1, " FF    ".countHexBytes())
        assertEquals(1, " ff    ".countHexBytes())
        assertEquals(1, " Ff    ".countHexBytes())
        assertEquals(1, " fF    ".countHexBytes())
        assertEquals(1, " 0F    ".countHexBytes())
        assertEquals(1, " F0    ".countHexBytes())
        assertEquals(1, " 0f    ".countHexBytes())
        assertEquals(1, " f0    ".countHexBytes())

        assertEquals(2, " 01   01   ".countHexBytes())
        assertEquals(2, " FF   FF   ".countHexBytes())
        assertEquals(2, " ff   ff   ".countHexBytes())
        assertEquals(2, " Ff   Ff   ".countHexBytes())
        assertEquals(2, " fF   fF   ".countHexBytes())
        assertEquals(2, " 0F   0F   ".countHexBytes())
        assertEquals(2, " F0   F0   ".countHexBytes())
        assertEquals(2, " 0f   0f   ".countHexBytes())
        assertEquals(2, " f0   f0   ".countHexBytes())

        assertEquals(2, " 0101   ".countHexBytes())
        assertEquals(2, " FFFF   ".countHexBytes())
        assertEquals(2, " ffff   ".countHexBytes())
        assertEquals(2, " FfFf   ".countHexBytes())
        assertEquals(2, " fFfF   ".countHexBytes())
        assertEquals(2, " 0F0F   ".countHexBytes())
        assertEquals(2, " F0F0   ".countHexBytes())
        assertEquals(2, " 0f0f   ".countHexBytes())
        assertEquals(2, " f0f0   ".countHexBytes())

        assertFailsWith<IllegalArgumentException> { "1".countHexBytes() }
        assertFailsWith<IllegalArgumentException> { "0_1".countHexBytes() }
        assertFailsWith<IllegalArgumentException> { "0 1".countHexBytes() }
        assertFailsWith<IllegalArgumentException> { "g".countHexBytes() }
        assertFailsWith<IllegalArgumentException> { "_".countHexBytes() }
        assertFailsWith<IllegalArgumentException> { "123".countHexBytes() }
        assertFailsWith<IllegalArgumentException> { "0x12".countHexBytes() }
        assertFailsWith<IllegalArgumentException> { "12 3".countHexBytes() }
    }

    @Test
    fun `test hexToBytes`() {
        assertContentEquals(byteArrayOf(0xff.toByte()), "FF".hexToBytes())
        assertContentEquals(byteArrayOf(0xff.toByte()), "ff".hexToBytes())
        assertContentEquals(byteArrayOf(0xff.toByte()), "fF".hexToBytes())
        assertContentEquals(byteArrayOf(0xff.toByte()), "Ff".hexToBytes())

        assertContentEquals(byteArrayOf(0x00.toByte()), "00".hexToBytes())
        assertContentEquals(byteArrayOf(0x0f.toByte()), "0f".hexToBytes())
        assertContentEquals(byteArrayOf(0x34.toByte()), "34".hexToBytes())
        assertContentEquals(byteArrayOf(0x7f.toByte()), "7f".hexToBytes())

        assertContentEquals(byteArrayOf(0xff.toByte(), 0xff.toByte()), "     FF   FF  ".hexToBytes())
        assertContentEquals(byteArrayOf(0xff.toByte(), 0xff.toByte()), "     ff   ff  ".hexToBytes())
        assertContentEquals(byteArrayOf(0xff.toByte(), 0xff.toByte()), "     fF   fF  ".hexToBytes())
        assertContentEquals(byteArrayOf(0xff.toByte(), 0xff.toByte()), "     Ff   Ff  ".hexToBytes())

        assertContentEquals(byteArrayOf(0x00.toByte(), 0x00.toByte()), "     00   00  ".hexToBytes())
        assertContentEquals(byteArrayOf(0x0f.toByte(), 0x0f.toByte()), "     0f   0f  ".hexToBytes())
        assertContentEquals(byteArrayOf(0x34.toByte(), 0x34.toByte()), "     34   34  ".hexToBytes())
        assertContentEquals(byteArrayOf(0x7f.toByte(), 0x7f.toByte()), "     7f   7f  ".hexToBytes())
    }

    @Test
    fun `test hexToUBytes`() {
        // implementations of hexToBytes and hexToUBytes are very similar.

        assertContentEquals(ubyteArrayOf(0x7f.toUByte(), 0x7f.toUByte()), "     7f   7f  ".hexToUBytes())
    }
}

