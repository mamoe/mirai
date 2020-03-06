@file:Suppress("unused")

package net.mamoe.mirai.qqandroid.io.serialization

import io.ktor.utils.io.core.*
import net.mamoe.mirai.qqandroid.io.serialization.jce.JceInput
import net.mamoe.mirai.qqandroid.io.serialization.jce.writeJceHead
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal const val BYTE: Byte = 0
internal const val DOUBLE: Byte = 5
internal const val FLOAT: Byte = 4
internal const val INT: Byte = 2
internal const val JCE_MAX_STRING_LENGTH = 104857600
internal const val LIST: Byte = 9
internal const val LONG: Byte = 3
internal const val MAP: Byte = 8
internal const val SHORT: Byte = 1
internal const val SIMPLE_LIST: Byte = 13
internal const val STRING1: Byte = 6
internal const val STRING4: Byte = 7
internal const val STRUCT_BEGIN: Byte = 10
internal const val STRUCT_END: Byte = 11
internal const val ZERO_TYPE: Byte = 12

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@Suppress("INVISIBLE_MEMBER") // bug
internal class JceInputTest {

    @Test
    fun testHeadSkip() {
        val input = JceInput(buildPacket {
            writeJceHead(BYTE, 0)
            writeByte(66)
            writeJceHead(SHORT, 1)
            writeShort(123)
            writeJceHead(INT, 3)
            writeInt(123456)
            writeJceHead(FLOAT, 8)
            writeFloat(123f)
            writeJceHead(LONG, 15)
            writeLong(123456789123456789L)
            writeJceHead(DOUBLE, 16)
            writeDouble(123456.0)
            writeJceHead(BYTE, 17)
            writeByte(1) // boolean
        }, JceCharset.UTF8)

        assertEquals(123456, input.skipToHeadAndUseIfPossibleOrFail(3) { input.readJceIntValue(it) })

        assertEquals(true, input.skipToHeadAndUseIfPossibleOrFail(17) { input.readJceBooleanValue(it) })

        assertFailsWith<EOFException> {
            input.skipToHeadAndUseIfPossibleOrFail(18) {
                error("test failed")
            }
        }
    }

    @Test
    fun testReadPrimitive() {
        val input = JceInput(buildPacket {
            writeJceHead(BYTE, 0)
            writeByte(66)
            writeJceHead(SHORT, 1)
            writeShort(123)
            writeJceHead(INT, 3)
            writeInt(123456)
            writeJceHead(FLOAT, 8)
            writeFloat(123f)
            writeJceHead(LONG, 15)
            writeLong(123456789123456789L)
            writeJceHead(DOUBLE, 16)
            writeDouble(123456.0)
            writeJceHead(BYTE, 17)
            writeByte(1) // boolean
        }, JceCharset.UTF8)
        assertEquals(66, input.useHead { input.readJceByteValue(it) })
        assertEquals(123, input.useHead { input.readJceShortValue(it) })
        assertEquals(123456, input.useHead { input.readJceIntValue(it) })
        assertEquals(123f, input.useHead { input.readJceFloatValue(it) })
        assertEquals(123456789123456789, input.useHead { input.readJceLongValue(it) })
        assertEquals(123456.0, input.useHead { input.readJceDoubleValue(it) })
        assertEquals(true, input.useHead { input.readJceBooleanValue(it) })
    }
}