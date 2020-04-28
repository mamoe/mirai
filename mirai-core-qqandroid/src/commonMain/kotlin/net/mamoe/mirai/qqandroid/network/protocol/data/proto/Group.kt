/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class GroupLabel : ProtoBuf {
    @Serializable
    internal class Label(
        @ProtoId(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val enumType: Int /* enum */ = 1,
        @ProtoId(3) @JvmField val textColor: Color? = null,
        @ProtoId(4) @JvmField val edgingColor: Color? = null,
        @ProtoId(5) @JvmField val labelAttr: Int = 0,
        @ProtoId(6) @JvmField val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val error: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val groupInfo: List<GroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class SourceId(
        @ProtoId(1) @JvmField val sourceId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) @JvmField val int32Result: Int = 0,
        @ProtoId(2) @JvmField val groupCode: Long = 0L,
        @ProtoId(3) @JvmField val groupLabel: List<Label>? = null
    ) : ProtoBuf

    @Serializable
    internal class Color(
        @ProtoId(1) @JvmField val r: Int = 0,
        @ProtoId(2) @JvmField val g: Int = 0,
        @ProtoId(3) @JvmField val b: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val sourceId: SourceId? = null,
        @ProtoId(2) @JvmField val uinInfo: UinInfo? = null,
        @ProtoId(3) @JvmField val numberLabel: Int = 5,
        @ProtoId(4) @JvmField val groupCode: List<Long>? = null,
        @ProtoId(5) @JvmField val labelStyle: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class UinInfo(
        @ProtoId(1) @JvmField val int64Longitude: Long = 0L,
        @ProtoId(2) @JvmField val int64Latitude: Long = 0L
    ) : ProtoBuf
}