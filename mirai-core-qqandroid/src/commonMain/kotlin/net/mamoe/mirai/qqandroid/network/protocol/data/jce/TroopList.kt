package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class GetTroopListReqV2Simplify(
    @SerialId(0) val uin: Long,
    @SerialId(1) val getMSFMsgFlag: Byte? = null,
    @SerialId(2) val vecCookies: ByteArray? = null,
    @SerialId(3) val vecGroupInfo: List<stTroopNumSimplify>? = null,
    @SerialId(4) val groupFlagExt: Byte? = null,
    @SerialId(5) val shVersion: Int? = null,
    @SerialId(6) val dwCompanyId: Long? = null,
    @SerialId(7) val versionNum: Long? = null,
    @SerialId(8) val getLongGroupName: Byte? = null
) : JceStruct

@Serializable
internal class stTroopNumSimplify(
    @SerialId(0) val groupCode: Long,
    @SerialId(1) val dwGroupInfoSeq: Long? = null,
    @SerialId(2) val dwGroupFlagExt: Long? = null,
    @SerialId(3) val dwGroupRankSeq: Long? = null
) : JceStruct
