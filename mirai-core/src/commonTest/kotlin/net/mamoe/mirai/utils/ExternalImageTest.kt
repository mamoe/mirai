package net.mamoe.mirai.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ExternalImageTest {

    @Test
    fun testByteArrayGet() {
        assertEquals("0F", byteArrayOf(0x0f)[0, 0])
        assertEquals("10", byteArrayOf(0x10)[0, 0])
        assertEquals("0FFE", byteArrayOf(0x0F, 0xFE.toByte())[0, 1])
    }
}