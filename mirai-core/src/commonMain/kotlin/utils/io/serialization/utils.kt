/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("SerializationUtils")
@file:JvmMultifileClass
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.internal.utils.io.serialization

import kotlinx.io.core.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestDataVersion2
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestDataVersion3
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.tars.Tars
import net.mamoe.mirai.internal.utils.soutv
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.utils.readPacketExact
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal typealias KtProtoBuf = kotlinx.serialization.protobuf.ProtoBuf

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
    this.readPacketExact(length).use {
        return Tars.UTF_8.load(serializer, it)
    }
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

internal fun <T : ProtoBuf> BytePacketBuilder.writeOidb(
    command: Int = 0,
    serviceType: Int = 0,
    serializer: SerializationStrategy<T>,
    v: T,
    clientVersion: String = "android 8.4.8",
) {
    return this.writeProtoBuf(
        OidbSso.OIDBSSOPkg.serializer(),
        OidbSso.OIDBSSOPkg(
            command = command,
            serviceType = serviceType,
            clientVersion = clientVersion,
            bodybuffer = v.toByteArray(serializer)
        )
    )
}

/**
 * dump
 */
internal fun <T : ProtoBuf> T.toByteArray(serializer: SerializationStrategy<T>): ByteArray {
    return KtProtoBuf.encodeToByteArray(serializer, this)
}

/**
 * load
 */
internal fun <T : ProtoBuf> ByteArray.loadAs(deserializer: DeserializationStrategy<T>): T {
    return KtProtoBuf.decodeFromByteArray(deserializer, this)
}

internal fun <T : ProtoBuf> ByteArray.loadOidb(deserializer: DeserializationStrategy<T>, log: Boolean = false): T {
    val oidb = loadAs(OidbSso.OIDBSSOPkg.serializer())
    if (log) {
        oidb.soutv("OIDB")
    }
    return oidb.bodybuffer.loadAs(deserializer)
}

/**
 * load
 */
internal fun <T : ProtoBuf> ByteReadPacket.readProtoBuf(
    serializer: DeserializationStrategy<T>,
    length: Int = this.remaining.toInt()
): T = KtProtoBuf.decodeFromByteArray(serializer, this.readBytes(length))

@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
@JvmInline
internal value class OidbBodyOrFailure<T : ProtoBuf> private constructor(
    private val v: Any
) {
    internal class Failure(
        val oidb: OidbSso.OIDBSSOPkg
    )

    inline fun <R> fold(
        onSuccess: T.(T) -> R,
        onFailure: OidbSso.OIDBSSOPkg.(OidbSso.OIDBSSOPkg) -> R,
    ): R {
        contract {
            callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
            callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
        }
        @Suppress("UNCHECKED_CAST")
        return if (v is Failure) {
            onFailure(v.oidb, v.oidb)
        } else {
            val t = v as T
            onSuccess(t, t)
        }
    }

    companion object {
        fun <T : ProtoBuf> success(t: T): OidbBodyOrFailure<T> = OidbBodyOrFailure(t)
        fun <T : ProtoBuf> failure(oidb: OidbSso.OIDBSSOPkg): OidbBodyOrFailure<T> = OidbBodyOrFailure(Failure(oidb))
    }
}

/**
 * load
 */
internal inline fun <T : ProtoBuf> ByteReadPacket.readOidbSsoPkg(
    serializer: DeserializationStrategy<T>,
    length: Int = this.remaining.toInt()
): OidbBodyOrFailure<T> {
    val oidb = readBytes(length).loadAs(OidbSso.OIDBSSOPkg.serializer())
    return if (oidb.result == 0) {
        OidbBodyOrFailure.success(oidb.bodybuffer.loadAs(serializer))
    } else {
        OidbBodyOrFailure.failure(oidb)
    }
}

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

internal inline fun jceRequestSBuffer(block: JceRequestSBufferBuilder.() -> Unit): ByteArray {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return JceRequestSBufferBuilder().apply(block).complete()
}

internal class JceRequestSBufferBuilder {
    val map: MutableMap<String, ByteArray> = LinkedHashMap()
    operator fun <T : JceStruct> String.invoke(
        serializer: SerializationStrategy<T>,
        jceStruct: T
    ) {
        map[this] = JCE_STRUCT_HEAD_OF_TAG_0 + jceStruct.toByteArray(serializer) + JCE_STRUCT_TAIL_OF_TAG_0
    }

    fun complete(): ByteArray = RequestDataVersion3(map).toByteArray(RequestDataVersion3.serializer())
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
