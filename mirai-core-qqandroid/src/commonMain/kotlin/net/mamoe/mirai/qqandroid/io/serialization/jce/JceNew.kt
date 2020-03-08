/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization.jce

import kotlinx.io.core.*
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import net.mamoe.mirai.qqandroid.io.serialization.IOFormat
import net.mamoe.mirai.qqandroid.io.serialization.JceCharset
import net.mamoe.mirai.qqandroid.io.serialization.JceOld
import net.mamoe.mirai.utils.io.toReadPacket

/**
 * Jce 数据结构序列化和反序列化器.
 *
 * @author Him188
 */
class Jce(
    override val context: SerialModule,
    val charset: JceCharset
) : SerialFormat, IOFormat, BinaryFormat {
    override fun <T> dumpTo(serializer: SerializationStrategy<T>, ojb: T, output: Output) {
        output.writePacket(JceOld.byCharSet(this.charset).dumpAsPacket(serializer, ojb))
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, input: Input): T {
        return JceDecoder(JceInput(input, charset), context).decodeSerializableValue(deserializer)
    }

    override fun <T> dump(serializer: SerializationStrategy<T>, value: T): ByteArray {
        return buildPacket { dumpTo(serializer, value, this) }.readBytes()
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        return load(deserializer, bytes.toReadPacket())
    }

    companion object {
        val UTF_8 = Jce(EmptyModule, JceCharset.UTF8)
        val GBK = Jce(EmptyModule, JceCharset.GBK)

        fun byCharSet(c: JceCharset): Jce {
            return if (c == JceCharset.UTF8) UTF_8 else GBK
        }

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
    }
}