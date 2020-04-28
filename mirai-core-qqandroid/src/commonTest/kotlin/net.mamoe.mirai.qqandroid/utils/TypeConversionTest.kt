/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

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
        assertEquals(
            ubyteArrayOf(0x7fu, 0xffu, 0xffu, 0xffu).toByteArray().toUHexString(),
            Int.MAX_VALUE.toByteArray().toUHexString()
        )
    }
}