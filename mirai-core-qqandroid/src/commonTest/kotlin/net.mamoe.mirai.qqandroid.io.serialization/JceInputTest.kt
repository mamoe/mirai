@file:Suppress("unused")

package net.mamoe.mirai.qqandroid.io.serialization

import io.ktor.utils.io.core.Output
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeInt
import net.mamoe.mirai.qqandroid.io.serialization.jce.JceInput
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun testHeadStack() {

    }

    @Test
    fun testReadInt() {
        val input = JceInput(buildPacket {
            writeHead(INT, 0)
            writeInt(123456)
        }, JceCharset.UTF8)
        assertEquals(123456, input.readJceIntValue(input.nextHead()))
    }


    @PublishedApi
    internal fun Output.writeHead(type: Byte, tag: Int) {
        if (tag < 15) {
            writeByte(((tag shl 4) or type.toInt()).toByte())
            return
        }
        if (tag < 256) {
            writeByte((type.toInt() or 0xF0).toByte())
            writeByte(tag.toByte())
            return
        }
        error("tag is too large: $tag")
    }
}