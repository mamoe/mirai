package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class GetFriendListReq(
    @SerialId(0) val reqtype: Int? = null,
    @SerialId(1) val ifReflush: Byte? = null,
    @SerialId(2) val uin: Long? = null,
    @SerialId(3) val startIndex: Short? = null,
    @SerialId(4) val getfriendCount: Short? = null,
    @SerialId(5) val groupid: Byte? = null,
    @SerialId(6) val ifGetGroupInfo: Byte? = null,
    @SerialId(7) val groupstartIndex: Byte? = null,
    @SerialId(8) val getgroupCount: Byte? = null,
    @SerialId(9) val ifGetMSFGroup: Byte? = null,
    @SerialId(10) val ifShowTermType: Byte? = null,
    @SerialId(11) val version: Long? = null,
    @SerialId(12) val uinList: List<Long>? = null,
    @SerialId(13) val eAppType: Int = 0,
    @SerialId(14) val ifGetDOVId: Byte? = null,
    @SerialId(15) val ifGetBothFlag: Byte? = null,
    @SerialId(16) val vec0xd50Req: ByteArray? = null,
    @SerialId(17) val vec0xd6bReq: ByteArray? = null,
    @SerialId(18) val vecSnsTypelist: List<Long>? = null
) : JceStruct


