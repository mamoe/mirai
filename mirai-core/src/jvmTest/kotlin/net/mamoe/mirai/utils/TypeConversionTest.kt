package net.mamoe.mirai.utils

import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.toByteArray
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TypeConversionTest {

    @ExperimentalUnsignedTypes
    @Test
    fun testConversions() {
        assertEquals("01", byteArrayOf(1).toUHexString())
        assertEquals("7F", byteArrayOf(0x7F).toUHexString())
        assertEquals("FF", ubyteArrayOf(0xffu).toUHexString())
        assertEquals("7F", ubyteArrayOf(0x7fu).toUHexString())
        assertTrue { 1994701021.toByteArray().contentEquals("76 E4 B8 DD".hexToBytes()) }
        assertEquals(byteArrayOf(0, 0, 0, 0x01).toUHexString(), 1.toByteArray().toUHexString())
        assertEquals(ubyteArrayOf(0x7fu, 0xffu, 0xffu, 0xffu).toByteArray().toUHexString(), Int.MAX_VALUE.toByteArray().toUHexString())
    }
}