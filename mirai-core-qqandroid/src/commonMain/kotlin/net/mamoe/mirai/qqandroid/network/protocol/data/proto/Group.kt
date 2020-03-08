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
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
class GroupLabel : ProtoBuf {
    @Serializable
    class Label(
        @ProtoId(1) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val enumType: Int /* enum */ = 1,
        @ProtoId(3) val textColor: GroupLabel.Color? = null,
        @ProtoId(4) val edgingColor: GroupLabel.Color? = null,
        @ProtoId(5) val labelAttr: Int = 0,
        @ProtoId(6) val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @ProtoId(1) val error: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val groupInfo: List<GroupLabel.GroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    class SourceId(
        @ProtoId(1) val sourceId: Int = 0
    ) : ProtoBuf

    @Serializable
    class GroupInfo(
        @ProtoId(1) val int32Result: Int = 0,
        @ProtoId(2) val groupCode: Long = 0L,
        @ProtoId(3) val groupLabel: List<GroupLabel.Label>? = null
    ) : ProtoBuf

    @Serializable
    class Color(
        @ProtoId(1) val r: Int = 0,
        @ProtoId(2) val g: Int = 0,
        @ProtoId(3) val b: Int = 0
    ) : ProtoBuf

    @Serializable
    class ReqBody(
        @ProtoId(1) val sourceId: GroupLabel.SourceId? = null,
        @ProtoId(2) val uinInfo: GroupLabel.UinInfo? = null,
        @ProtoId(3) val numberLabel: Int = 5,
        @ProtoId(4) val groupCode: List<Long>? = null,
        @ProtoId(5) val labelStyle: Int = 0
    ) : ProtoBuf

    @Serializable
    class UinInfo(
        @ProtoId(1) val int64Longitude: Long = 0L,
        @ProtoId(2) val int64Latitude: Long = 0L
    ) : ProtoBuf
}