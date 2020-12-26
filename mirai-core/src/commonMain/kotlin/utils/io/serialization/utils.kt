/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("SerializationUtils")
@file:JvmMultifileClass

package net.mamoe.mirai.internal.utils.io.serialization

import kotlinx.io.core.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestDataVersion2
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestDataVersion3
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.tars.Tars
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.utils.readPacketExact

internal fun <T : JceStruct> ByteArray.loadWithUniPacket(
    deserializer: DeserializationStrategy<T>,
    name: String? = null
): T = this.read { readUniPacket(deserializer, name) }

internal fun <T : JceStruct> ByteArray.loadAs(
    deserializer: DeserializationStrategy<T>
): T = this.read { Tars.UTF_8.load(deserializer, this) }

internal fun <T : JceStruct> BytePacketBuilder.writeJceStruct(
    serializer: SerializationStrategy<T>,
    struct: T
) {
    Tars.UTF_8.dumpTo(serializer, struct, this)
}

internal fun <T : JceStruct> ByteReadPacket.readJceStruct(
    serializer: DeserializationStrategy<T>,
    length: Int = this.remaining.toInt()
): T {
    return Tars.UTF_8.load(serializer, this.readPacketExact(length))
}

internal fun <T : JceStruct> BytePacketBuilder.writeJceRequestPacket(
    version: Int = 3,
    servantName: String,
    funcName: String,
    name: String = funcName,
    serializer: SerializationStrategy<T>,
    body: T
) = writeJceStruct(
    RequestPacket.serializer(),
    RequestPacket(
        requestId = 0,
        version = version.toShort(),
        servantName = servantName,
        funcName = funcName,
        sBuffer = jceRequestSBuffer(name, serializer, body)
    )
)

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

private fun <K, V> Map<K, V>.singleValue(): V = this.entries.single().value

internal fun <R> ByteReadPacket.decodeUniRequestPacketAndDeserialize(name: String? = null, block: (ByteArray) -> R): R {
    val request = this.readJceStruct(RequestPacket.serializer())

    return block(if (name == null) when (request.version?.toInt() ?: 3) {
        2 -> request.sBuffer.loadAs(RequestDataVersion2.serializer()).map.singleValue().singleValue()
        3 -> request.sBuffer.loadAs(RequestDataVersion3.serializer()).map.singleValue()
        else -> error("unsupported version ${request.version}")
    } else when (request.version?.toInt() ?: 3) {
        2 -> request.sBuffer.loadAs(RequestDataVersion2.serializer()).map.getOrElse(name) { error("cannot find $name") }
            .singleValue()
        3 -> request.sBuffer.loadAs(RequestDataVersion3.serializer()).map.getOrElse(name) { error("cannot find $name") }
        else -> error("unsupported version ${request.version}")
    })
}

internal fun <T : JceStruct> T.toByteArray(
    serializer: SerializationStrategy<T>
): ByteArray = Tars.UTF_8.encodeToByteArray(serializer, this)

internal fun <T : ProtoBuf> BytePacketBuilder.writeProtoBuf(serializer: SerializationStrategy<T>, v: T) {
    this.writeFully(v.toByteArray(serializer))
}

/**
 * dump
 */
internal fun <T : ProtoBuf> T.toByteArray(serializer: SerializationStrategy<T>): ByteArray {
    return ProtoBufWithNullableSupport.encodeToByteArray(serializer, this)
}

/**
 * load
 */
internal fun <T : ProtoBuf> ByteArray.loadAs(deserializer: DeserializationStrategy<T>): T {
    return ProtoBufWithNullableSupport.decodeFromByteArray(deserializer, this)
}

/**
 * load
 */
internal fun <T : ProtoBuf> ByteReadPacket.readProtoBuf(
    serializer: DeserializationStrategy<T>,
    length: Int = this.remaining.toInt()
): T = ProtoBufWithNullableSupport.decodeFromByteArray(serializer, this.readBytes(length))

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
