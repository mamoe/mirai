/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization.jce

import io.ktor.utils.io.core.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.io.serialization.IOFormat
import net.mamoe.mirai.qqandroid.io.serialization.Jce
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.BYTE
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.DOUBLE
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.FLOAT
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.INT
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.LIST
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.LONG
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.MAP
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.SHORT
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.SIMPLE_LIST
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.STRING1
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.STRING4
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.STRUCT_BEGIN
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.STRUCT_END
import net.mamoe.mirai.qqandroid.io.serialization.Jce.Companion.ZERO_TYPE
import net.mamoe.mirai.qqandroid.io.serialization.JceCharset
import net.mamoe.mirai.utils.io.readString

/**
 * Jce 数据结构序列化和反序列化器.
 *
 * @author Him188
 */
class JceNew(
    override val context: SerialModule
) : SerialFormat, IOFormat {
    companion object Default : IOFormat by JceNew(
        EmptyModule
    )

    override fun <T> dump(serializer: SerializationStrategy<T>, input: Input): ByteArray {
        TODO("Not yet implemented")
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, output: Output): T {
        TODO("Not yet implemented")
    }

}