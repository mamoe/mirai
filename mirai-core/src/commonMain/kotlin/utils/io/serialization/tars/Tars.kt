/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.internal.utils.io.serialization.tars

import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.mamoe.mirai.internal.utils.io.serialization.tars.internal.DebugLogger
import net.mamoe.mirai.internal.utils.io.serialization.tars.internal.TarsDecoder
import net.mamoe.mirai.internal.utils.io.serialization.tars.internal.TarsInput
import net.mamoe.mirai.internal.utils.io.serialization.tars.internal.TarsOld
import net.mamoe.mirai.utils.read
import kotlin.jvm.JvmStatic

/**
 * The main entry point to work with Tars serialization.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class Tars(
    override val serializersModule: SerializersModule = EmptySerializersModule,
    val charset: Charset = Charsets.UTF_8,
) : SerialFormat, BinaryFormat {
    private val old = TarsOld(charset)

    fun <T> dumpTo(serializer: SerializationStrategy<T>, ojb: T, output: Output) {
        old.dumpAsPacket(serializer, ojb).use {
            output.writePacket(it)
        }
    }

    fun <T> load(deserializer: DeserializationStrategy<T>, input: Input, debugLogger: DebugLogger? = null): T {
        val l = debugLogger ?: DebugLogger(null)
        return TarsDecoder(TarsInput(input, charset, l), serializersModule, l).decodeSerializableValue(deserializer)
    }

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return buildPacket { dumpTo(serializer, value, this) }.readBytes()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        bytes.read {
            return load(deserializer, this, null)
        }
    }

    companion object {
        @JvmStatic
        val UTF_8: Tars = Tars()

        internal const val BYTE: Byte = 0
        internal const val DOUBLE: Byte = 5
        internal const val FLOAT: Byte = 4
        internal const val INT: Byte = 2
        internal const val Tars_MAX_STRING_LENGTH = 104857600
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
    }
}
