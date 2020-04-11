package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.JceId

@Serializable
internal class GroupMngReqJce(
    @JceId(0) val reqtype: Int,
    @JceId(1) val uin: Long,
    @JceId(2) val vecBody: ByteArray,
    @JceId(3) val checkInGroup: Byte? = null,
    @JceId(4) val sGroupLocation: String? = "",
    @JceId(5) val statOption: Byte? = null,
    @JceId(6) val wSourceID: Int? = null,
    @JceId(7) val wSourceSubID: Int? = null,
    @JceId(8) val isSupportAuthQuestionJoin: Byte? = null,
    @JceId(9) val ifGetAuthInfo: Byte? = null,
    @JceId(10) val dwDiscussUin: Long? = null,
    @JceId(11) val sJoinGroupKey: String? = "",
    @JceId(12) val sJoinGroupPicUrl: String? = "",
    @JceId(13) val vecJoinGroupRichMsg: ByteArray? = null,
    @JceId(14) val sJoinGroupAuth: String? = "",
    @JceId(15) val sJoinGroupVerifyToken: String? = "",
    @JceId(16) val dwJoinVerifyType: Long? = null
) : JceStruct

@Serializable
internal class GroupMngRes(
    @JceId(0) val reqtype: Int,
    @JceId(1) val result: Byte,
    @JceId(2) val vecBody: ByteArray,
    @JceId(3) val errorString: String = "",
    @JceId(4) val errorCode: Short = 0,
    @JceId(5) val isInGroup: Byte? = null,
    @JceId(6) val sGroupLocation: String? = "",
    @JceId(7) val isMemInvite: Byte? = null,
    @JceId(8) val sAuthGrpInfo: String? = "",
    @JceId(9) val sJoinQuestion: String? = "",
    @JceId(10) val sJoinAnswer: String? = "",
    @JceId(11) val dwDis2GrpLimitType: Long? = null
) : JceStruct
