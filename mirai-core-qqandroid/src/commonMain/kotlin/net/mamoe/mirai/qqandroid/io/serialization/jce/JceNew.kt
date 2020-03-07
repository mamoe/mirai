/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.io.serialization.jce

import kotlinx.io.core.Input
import kotlinx.io.core.Output
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import net.mamoe.mirai.qqandroid.io.serialization.IOFormat
import net.mamoe.mirai.qqandroid.io.serialization.JceCharset

/**
 * Jce 数据结构序列化和反序列化器.
 *
 * @author Him188
 */
class JceNew(
    override val context: SerialModule,
    val charset: JceCharset
) : SerialFormat, IOFormat {
    override fun <T> dump(serializer: SerializationStrategy<T>, output: Output): ByteArray {
        TODO("Not yet implemented")
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, input: Input): T {
        return JceDecoder(JceInput(input, charset), context).decodeSerializableValue(deserializer)
    }

    companion object {
        val UTF_8 = JceNew(EmptyModule, JceCharset.UTF8)
        val GBK = JceNew(EmptyModule, JceCharset.GBK)
    }
}