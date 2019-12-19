package net.mamoe.mirai.utils.io

import kotlin.test.Test
import kotlin.test.assertEquals


class TypeConversionTest {

    @ExperimentalUnsignedTypes
    @Test
    fun testConversions() {
        assertEquals("01", byteArrayOf(1).toUHexString())
        assertEquals("7F", byteArrayOf(0x7F).toUHexString())
        assertEquals("FF", ubyteArrayOf(0xffu).toUHexString())
        assertEquals("7F", ubyteArrayOf(0x7fu).toUHexString())
        assertEquals(byteArrayOf(0, 0, 0, 0x01).toUHexString(), 1.toByteArray().toUHexString())
        assertEquals(ubyteArrayOf(0x7fu, 0xffu, 0xffu, 0xffu).toByteArray().toUHexString(), Int.MAX_VALUE.toByteArray().toUHexString())
    }
}