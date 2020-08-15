/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("SerializationUtils")
@file:JvmMultifileClass

package net.mamoe.mirai.qqandroid.utils.io.serialization

import kotlinx.io.core.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import moe.him188.jcekt.Jce
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestDataVersion2
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestDataVersion3
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import net.mamoe.mirai.qqandroid.utils.io.readPacketExact
import net.mamoe.mirai.qqandroid.utils.read
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

internal fun <T : JceStruct> ByteArray.loadWithUniPacket(
    deserializer: DeserializationStrategy<T>,
    name: String? = null
): T = this.read { readUniPacket(deserializer, name) }

internal fun <T : JceStruct> ByteArray.loadAs(
    deserializer: DeserializationStrategy<T>
): T = this.read { Jce.UTF_8.load(deserializer, this) }

internal fun <T : JceStruct> BytePacketBuilder.writeJceStruct(
    serializer: SerializationStrategy<T>,
    struct: T
) {
    Jce.UTF_8.dumpTo(serializer, struct, this)
}

internal fun <T : JceStruct> ByteReadPacket.readJceStruct(
    serializer: DeserializationStrategy<T>,
    length: Int = this.remaining.toInt()
): T {
    return Jce.UTF_8.load(serializer, this.readPacketExact(length))
}

/**
 * 先解析为 [RequestPacket], 即 `UniRequest`, 再按版本解析 map, 再找出指定数据并反序列化
 */
internal fun <T : JceStruct> ByteReadPacket.readUniPacket(
    deserializer: DeserializationStrategy<T>,
    name: String? = null
): T {
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
internal fun <T : ProtoBuf> ByteReadPacket.readUniPacket(
    deserializer: DeserializationStrategy<T>,
    name: String? = null
): T {
    return decodeUniRequestPacketAndDeserialize(name) {
        it.read {
            discardExact(1)
            this.readProtoBuf(deserializer, (this.remaining - 1).toInt())
        }
    }
}

private fun <K, V> Map<K, V>.firstValue(): V = this.entries.first().value

private fun <R> ByteReadPacket.decodeUniRequestPacketAndDeserialize(name: String? = null, block: (ByteArray) -> R): R {
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

internal fun <T : JceStruct> T.toByteArray(
    serializer: SerializationStrategy<T>
): ByteArray = Jce.UTF_8.dump(serializer, this)

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
): T = ProtoBufWithNullableSupport.load(serializer, this.readBytes(length))

/**
 * 构造 [RequestPacket] 的 [RequestPacket.sBuffer]
 */
internal fun <T : JceStruct> jceRequestSBuffer(
    name: String,
    serializer: SerializationStrategy<T>,
    jceStruct: T
): ByteArray {
    return RequestDataVersion3(
        mapOf(
            name to JCE_STRUCT_HEAD_OF_TAG_0 + jceStruct.toByteArray(serializer) + JCE_STRUCT_TAIL_OF_TAG_0
        )
    ).toByteArray(RequestDataVersion3.serializer())
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
