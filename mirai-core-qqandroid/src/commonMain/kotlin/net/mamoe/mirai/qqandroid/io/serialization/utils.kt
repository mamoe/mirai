/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("SerializationUtils")
@file:JvmMultifileClass

package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.io.core.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.io.serialization.jce.Jce
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestDataVersion2
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestDataVersion3
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.qqandroid.utils.read
import net.mamoe.mirai.qqandroid.utils.io.readPacketExact
import net.mamoe.mirai.qqandroid.utils.toReadPacket
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


internal fun <T : JceStruct> ByteArray.loadAs(deserializer: DeserializationStrategy<T>, c: JceCharset = JceCharset.UTF8): T {
    return Jce.byCharSet(c).load(deserializer, this.toReadPacket())
}

internal fun <T : JceStruct> BytePacketBuilder.writeJceStruct(
    serializer: SerializationStrategy<T>,
    struct: T,
    charset: JceCharset = JceCharset.GBK
) {
    Jce.byCharSet(charset).dumpTo(serializer, struct, this)
}

internal fun <T : JceStruct> ByteReadPacket.readJceStruct(
    serializer: DeserializationStrategy<T>,
    charset: JceCharset = JceCharset.UTF8,
    length: Int = this.remaining.toInt()
): T {
    @OptIn(MiraiInternalAPI::class)
    return Jce.byCharSet(charset).load(serializer, this.readPacketExact(length))
}

/**
 * 先解析为 [RequestPacket], 即 `UniRequest`, 再按版本解析 map, 再找出指定数据并反序列化
 */
internal fun <T : JceStruct> ByteReadPacket.decodeUniPacket(deserializer: DeserializationStrategy<T>, name: String? = null): T {
    return decodeUniRequestPacketAndDeserialize(name) {
        it.read {
            discardExact(1)
            this.readJceStruct(deserializer, length = (this.remaining - 1).toInt())
        }
    }
}

/**
 * 先解析为 [RequestPacket], 即 `UniRequest`, 再按版本解析 map, 再找出指定数据并反序列化
 */
internal fun <T : ProtoBuf> ByteReadPacket.decodeUniPacket(deserializer: DeserializationStrategy<T>, name: String? = null): T {
    return decodeUniRequestPacketAndDeserialize(name) {
        it.read {
            discardExact(1)
            this.readProtoBuf(deserializer, (this.remaining - 1).toInt())
        }
    }
}
private fun <K, V> Map<K, V>.firstValue(): V = this.entries.first().value

internal fun <R> ByteReadPacket.decodeUniRequestPacketAndDeserialize(name: String? = null, block: (ByteArray) -> R): R {
    val request = this.readJceStruct(RequestPacket.serializer())

    return block(if (name == null) when (request.iVersion?.toInt() ?: 3) {
        2 -> request.sBuffer.loadAs(RequestDataVersion2.serializer()).map.firstValue().firstValue()
        3 -> request.sBuffer.loadAs(RequestDataVersion3.serializer()).map.firstValue()
        else -> error("unsupported version ${request.iVersion}")
    } else when (request.iVersion?.toInt() ?: 3) {
        2 -> request.sBuffer.loadAs(RequestDataVersion2.serializer()).map.getOrElse(name) { error("cannot find $name") }
            .firstValue()
        3 -> request.sBuffer.loadAs(RequestDataVersion3.serializer()).map.getOrElse(name) { error("cannot find $name") }
        else -> error("unsupported version ${request.iVersion}")
    })
}

internal fun <T : JceStruct> T.toByteArray(serializer: SerializationStrategy<T>, c: JceCharset = JceCharset.GBK): ByteArray =
    Jce.byCharSet(c).dump(serializer, this)

internal fun <T : ProtoBuf> BytePacketBuilder.writeProtoBuf(serializer: SerializationStrategy<T>, v: T) {
    this.writeFully(v.toByteArray(serializer))
}

/**
 * dump
 */
internal fun <T : ProtoBuf> T.toByteArray(serializer: SerializationStrategy<T>): ByteArray {
    return ProtoBufWithNullableSupport.dump(serializer, this)
}

/**
 * load
 */
internal fun <T : ProtoBuf> ByteArray.loadAs(deserializer: DeserializationStrategy<T>): T {
    return ProtoBufWithNullableSupport.load(deserializer, this)
}

/**
 * load
 */
internal fun <T : ProtoBuf> ByteReadPacket.readProtoBuf(
    serializer: DeserializationStrategy<T>,
    length: Int = this.remaining.toInt()
): T {
    return ProtoBufWithNullableSupport.load(serializer, this.readBytes(length))
}

/**
 * 构造 [RequestPacket] 的 [RequestPacket.sBuffer]
 */
internal fun <T : JceStruct> jceRequestSBuffer(name: String, serializer: SerializationStrategy<T>, jceStruct: T): ByteArray {
    return jceRequestSBuffer(name, serializer, jceStruct, JceCharset.GBK)
}

internal fun <T : JceStruct> jceRequestSBuffer(
    name: String,
    serializer: SerializationStrategy<T>,
    jceStruct: T,
    charset: JceCharset
): ByteArray {
    return RequestDataVersion3(
        mapOf(
            name to JCE_STRUCT_HEAD_OF_TAG_0 + jceStruct.toByteArray(serializer) + JCE_STRUCT_TAIL_OF_TAG_0
        )
    ).toByteArray(RequestDataVersion3.serializer(), charset)
}

private val JCE_STRUCT_HEAD_OF_TAG_0 = byteArrayOf(0x0A)
private val JCE_STRUCT_TAIL_OF_TAG_0 = byteArrayOf(0x0B)

internal inline fun <reified A : Annotation> SerialDescriptor.findAnnotation(elementIndex: Int): A? {
    val candidates = getElementAnnotations(elementIndex).filterIsInstance<A>()
    return when (candidates.size) {
        0 -> null
        1 -> candidates[0]
        else -> throw IllegalStateException("There are duplicate annotations of type ${A::class} in the descriptor $this")
    }
}
