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

@Serializable
internal class Vec0xd50 : ProtoBuf {
    @Serializable
    internal class ExtSnsFrdData(
        @ProtoId(1) val frdUin: Long = 0L,
        @ProtoId(91001) val musicSwitch: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(101001) val mutualmarkAlienation: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(141001) val mutualmarkScore: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(151001) val ksingSwitch: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(181001) val lbsShare: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgUpdateData: List<Vec0xd50.ExtSnsFrdData>? = null,
        @ProtoId(11) val over: Int = 0,
        @ProtoId(12) val nextStart: Int = 0,
        @ProtoId(13) val uint64UnfinishedUins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val appid: Long = 0L,
        @ProtoId(2) val maxPkgSize: Int = 0,
        @ProtoId(3) val startTime: Int = 0,
        @ProtoId(4) val startIndex: Int = 0,
        @ProtoId(5) val reqNum: Int = 0,
        @ProtoId(6) val uinList: List<Long>? = null,
        @ProtoId(91001) val reqMusicSwitch: Int = 0,
        @ProtoId(101001) val reqMutualmarkAlienation: Int = 0,
        @ProtoId(141001) val reqMutualmarkScore: Int = 0,
        @ProtoId(151001) val reqKsingSwitch: Int = 0,
        @ProtoId(181001) val reqMutualmarkLbsshare: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class KSingRelationInfo(
        @ProtoId(1) val flag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Vec0xd6b : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val maxPkgSize: Int = 0,
        @ProtoId(2) val startTime: Int = 0,
        @ProtoId(11) val uinList: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(11) val msgMutualmarkData: List<Vec0xd6b.MutualMarkData>? = null,
        @ProtoId(12) val uint64UnfinishedUins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class MutualMarkData(
        @ProtoId(1) val frdUin: Long = 0L,
        @ProtoId(2) val result: Int = 0
        // @SerialId(11) val mutualmarkInfo: List<Mutualmark.MutualMark>? = null
    ) : ProtoBuf
}

@Serializable
internal class Mutualmark : ProtoBuf {
    @Serializable
    internal class MutualmarkInfo(
        @ProtoId(1) val lastActionTime: Long = 0L,
        @ProtoId(2) val level: Int = 0,
        @ProtoId(3) val lastChangeTime: Long = 0L,
        @ProtoId(4) val continueDays: Int = 0,
        @ProtoId(5) val wildcardWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val notifyTime: Long = 0L,
        @ProtoId(7) val iconStatus: Long = 0L,
        @ProtoId(8) val iconStatusEndTime: Long = 0L,
        @ProtoId(9) val closeFlag: Int = 0,
        @ProtoId(10) val resourceInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ResourceInfo17(
        @ProtoId(1) val dynamicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val staticUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val cartoonUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val cartoonMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val playCartoon: Int = 0,
        @ProtoId(6) val word: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


