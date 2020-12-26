/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

@Serializable
internal class Vec0xd50 : ProtoBuf {
    @Serializable
    internal class ExtSnsFrdData(
        @ProtoNumber(1) @JvmField val frdUin: Long = 0L,
        @ProtoNumber(91001) @JvmField val musicSwitch: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(101001) @JvmField val mutualmarkAlienation: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(141001) @JvmField val mutualmarkScore: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(151001) @JvmField val ksingSwitch: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(181001) @JvmField val lbsShare: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgUpdateData: List<ExtSnsFrdData> = emptyList(),
        @ProtoNumber(11) @JvmField val over: Int = 0,
        @ProtoNumber(12) @JvmField val nextStart: Int = 0,
        @ProtoNumber(13) @JvmField val uint64UnfinishedUins: List<Long> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val appid: Long = 0L,
        @ProtoNumber(2) @JvmField val maxPkgSize: Int = 0,
        @ProtoNumber(3) @JvmField val startTime: Int = 0,
        @ProtoNumber(4) @JvmField val startIndex: Int = 0,
        @ProtoNumber(5) @JvmField val reqNum: Int = 0,
        @ProtoNumber(6) @JvmField val uinList: List<Long> = emptyList(),
        @ProtoNumber(91001) @JvmField val reqMusicSwitch: Int = 0,
        @ProtoNumber(101001) @JvmField val reqMutualmarkAlienation: Int = 0,
        @ProtoNumber(141001) @JvmField val reqMutualmarkScore: Int = 0,
        @ProtoNumber(151001) @JvmField val reqKsingSwitch: Int = 0,
        @ProtoNumber(181001) @JvmField val reqMutualmarkLbsshare: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class KSingRelationInfo(
        @ProtoNumber(1) @JvmField val flag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Vec0xd6b : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val maxPkgSize: Int = 0,
        @ProtoNumber(2) @JvmField val startTime: Int = 0,
        @ProtoNumber(11) @JvmField val uinList: List<Long> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(11) @JvmField val msgMutualmarkData: List<MutualMarkData> = emptyList(),
        @ProtoNumber(12) @JvmField val uint64UnfinishedUins: List<Long> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class MutualMarkData(
        @ProtoNumber(1) @JvmField val frdUin: Long = 0L,
        @ProtoNumber(2) @JvmField val result: Int = 0
        // @SerialId(11) @JvmField val mutualmarkInfo: List<Mutualmark.MutualMark> = emptyList()
    ) : ProtoBuf
}