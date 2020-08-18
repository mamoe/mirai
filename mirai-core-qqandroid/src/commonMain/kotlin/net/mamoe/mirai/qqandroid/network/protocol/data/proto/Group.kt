package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class GroupLabel : ProtoBuf {
    @Serializable
    internal class Label(
        @ProtoNumber(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val enumType: Int /* enum */ = 1,
        @ProtoNumber(3) @JvmField val textColor: Color? = null,
        @ProtoNumber(4) @JvmField val edgingColor: Color? = null,
        @ProtoNumber(5) @JvmField val labelAttr: Int = 0,
        @ProtoNumber(6) @JvmField val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val error: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val groupInfo: List<GroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class SourceId(
        @ProtoNumber(1) @JvmField val sourceId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoNumber(1) @JvmField val int32Result: Int = 0,
        @ProtoNumber(2) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(3) @JvmField val groupLabel: List<Label>? = null
    ) : ProtoBuf

    @Serializable
    internal class Color(
        @ProtoNumber(1) @JvmField val r: Int = 0,
        @ProtoNumber(2) @JvmField val g: Int = 0,
        @ProtoNumber(3) @JvmField val b: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val sourceId: SourceId? = null,
        @ProtoNumber(2) @JvmField val uinInfo: UinInfo? = null,
        @ProtoNumber(3) @JvmField val numberLabel: Int = 5,
        @ProtoNumber(4) @JvmField val groupCode: List<Long>? = null,
        @ProtoNumber(5) @JvmField val labelStyle: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class UinInfo(
        @ProtoNumber(1) @JvmField val int64Longitude: Long = 0L,
        @ProtoNumber(2) @JvmField val int64Latitude: Long = 0L
    ) : ProtoBuf
}