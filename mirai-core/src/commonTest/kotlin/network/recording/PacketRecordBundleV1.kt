/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.recording

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.KnownPacketFactories
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.map
import java.io.File
import kotlin.time.Duration

@Serializable
internal class PacketRecordBundleWrapper(
    @ProtoNumber(1) val version: Int,
    @ProtoNumber(2) val v1: PacketRecordBundleV1,
) : ProtoBuf {
    companion object
}


internal typealias PacketRecordBundle = PacketRecordBundleV1

@Serializable
internal class PacketRecordBundleV1(
    @ProtoNumber(1) val version: Int = 1,
    @ProtoNumber(2) val time: Long, // timestamp
    @ProtoNumber(3) val note: String,
    @ProtoNumber(4) val seed: Int,
    @ProtoNumber(100) val records: List<PacketRecord>,
) : ProtoBuf {
    companion object
}

@Serializable
internal data class PacketRecord(
    @ProtoNumber(1) val timeFromStart: @Serializable(DurationSerializer::class) Duration, // duration from start
    @ProtoNumber(2) val data: RawIncomingPacket,
) {
    val isIncoming: Boolean = KnownPacketFactories.findPacketFactory(data.commandName) is IncomingPacketFactory
}

@OptIn(InternalSerializationApi::class)
internal class DurationSerializer : KSerializer<Duration> by Long.serializer().map(
    resultantDescriptor = buildSerialDescriptor("Duration", PrimitiveKind.LONG),
    deserialize = { Duration.milliseconds(it) },
    serialize = { it.inWholeMilliseconds }
)

internal fun PacketRecordBundle.saveTo(file: File) = file.writeBytes(
    PacketRecordBundleWrapper(this.version, this).toByteArray(PacketRecordBundleWrapper.serializer())
)

internal fun PacketRecordBundleV1.Companion.loadFrom(bytes: ByteArray): PacketRecordBundle {
    return bytes.loadAs(PacketRecordBundleWrapper.serializer()).v1
}
