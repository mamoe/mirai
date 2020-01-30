package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
internal class Vec0xd50 : ProtoBuf {
    @Serializable
    internal class ExtSnsFrdData(
        @SerialId(1) val frdUin: Long = 0L,
        @SerialId(91001) val musicSwitch: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(101001) val mutualmarkAlienation: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(141001) val mutualmarkScore: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(151001) val ksingSwitch: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(181001) val lbsShare: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @SerialId(1) val msgUpdateData: List<Vec0xd50.ExtSnsFrdData>? = null,
        @SerialId(11) val over: Int = 0,
        @SerialId(12) val nextStart: Int = 0,
        @SerialId(13) val uint64UnfinishedUins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @SerialId(1) val appid: Long = 0L,
        @SerialId(2) val maxPkgSize: Int = 0,
        @SerialId(3) val startTime: Int = 0,
        @SerialId(4) val startIndex: Int = 0,
        @SerialId(5) val reqNum: Int = 0,
        @SerialId(6) val uinList: List<Long>? = null,
        @SerialId(91001) val reqMusicSwitch: Int = 0,
        @SerialId(101001) val reqMutualmarkAlienation: Int = 0,
        @SerialId(141001) val reqMutualmarkScore: Int = 0,
        @SerialId(151001) val reqKsingSwitch: Int = 0,
        @SerialId(181001) val reqMutualmarkLbsshare: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class KSingRelationInfo(
        @SerialId(1) val flag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Vec0xd6b : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @SerialId(1) val maxPkgSize: Int = 0,
        @SerialId(2) val startTime: Int = 0,
        @SerialId(11) val uinList: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @SerialId(11) val msgMutualmarkData: List<Vec0xd6b.MutualMarkData>? = null,
        @SerialId(12) val uint64UnfinishedUins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class MutualMarkData(
        @SerialId(1) val frdUin: Long = 0L,
        @SerialId(2) val result: Int = 0
        // @SerialId(11) val mutualmarkInfo: List<Mutualmark.MutualMark>? = null
    ) : ProtoBuf
}

@Serializable
internal class Mutualmark : ProtoBuf {
    @Serializable
    internal class MutualmarkInfo(
        @SerialId(1) val lastActionTime: Long = 0L,
        @SerialId(2) val level: Int = 0,
        @SerialId(3) val lastChangeTime: Long = 0L,
        @SerialId(4) val continueDays: Int = 0,
        @SerialId(5) val wildcardWording: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val notifyTime: Long = 0L,
        @SerialId(7) val iconStatus: Long = 0L,
        @SerialId(8) val iconStatusEndTime: Long = 0L,
        @SerialId(9) val closeFlag: Int = 0,
        @SerialId(10) val resourceInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ResourceInfo17(
        @SerialId(1) val dynamicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val staticUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val cartoonUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val cartoonMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val playCartoon: Int = 0,
        @SerialId(6) val word: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


