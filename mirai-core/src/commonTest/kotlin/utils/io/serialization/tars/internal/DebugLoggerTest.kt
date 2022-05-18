/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.io.serialization.tars.internal

import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.readJceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.Tars
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.toReadPacket
import net.mamoe.mirai.utils.toUHexString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class DebugLoggerTest : AbstractTest() {

    fun String.uniteLine(): String = replace("\r\n", "\n").replace("\r", "\n")

    @Serializable
    data class Struct(
        @TarsId(1) val str: String,
        @TarsId(2) val int: Int,
    ) : JceStruct

    @Test
    fun `can log`() {
        val out = BytePacketBuilder()
        val logger = DebugLogger(out)
        val original = Struct("string", 1)
        val bytes = original.toByteArray(Struct.serializer())
        val value = bytes.toReadPacket().use { Tars.UTF_8.load(Struct.serializer(), it, logger) }
        assertEquals(original, value)
        assertEquals(
            """
            beginStructure: net.mamoe.mirai.internal.utils.io.serialization.tars.internal.DebugLoggerTest.Struct, CLASS
                decodeElementIndex: TarsHead(tag=1, type=6(String1))
                name=str
                decodeElementIndex: TarsHead(tag=2, type=0(Byte))
                name=int
                decodeElementIndex: currentHead == null
            endStructure: net.mamoe.mirai.internal.utils.io.serialization.tars.internal.DebugLoggerTest.Struct, null, null
        """.trimIndent(), out.build().readBytes().decodeToString().trim().uniteLine()
        )
    }

    @Test
    fun `can auto log`() {
        val original = Struct("string", 1)
        val bytes = original.toByteArray(Struct.serializer())
        println(bytes.toUHexString()) // 16 06 73 74 72 69 6E 67 20 01
        bytes[bytes.lastIndex - 1] = 0x30.toByte() // change tag
        val exception = assertFails { bytes.toReadPacket().use { it.readJceStruct(Struct.serializer()) } }
        assertEquals(
            """
            在 解析 net.mamoe.mirai.internal.utils.io.serialization.tars.internal.DebugLoggerTest.Struct 时遇到了意料之中的问题. 请完整复制此日志提交给 mirai: https://github.com/mamoe/mirai/issues/new/choose    调试信息: 
            Data: 
            16 06 73 74 72 69 6E 67 30 01
            Trace:
            
            beginStructure: net.mamoe.mirai.internal.utils.io.serialization.tars.internal.DebugLoggerTest.Struct, CLASS
                decodeElementIndex: TarsHead(tag=1, type=6(String1))
                name=str
                decodeElementIndex: TarsHead(tag=3, type=0(Byte))
                skipping Byte
                decodeElementIndex EOF
            endStructure: net.mamoe.mirai.internal.utils.io.serialization.tars.internal.DebugLoggerTest.Struct, null, null
        """.trimIndent().trim(), exception.message!!.trim().uniteLine()
        )
    }
}