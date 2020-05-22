package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class GroupMngReqJce(
    @JceId(0) @JvmField val reqtype: Int,
    @JceId(1) @JvmField val uin: Long,
    @JceId(2) @JvmField val vecBody: ByteArray,
    @JceId(3) @JvmField val checkInGroup: Byte? = null,
    @JceId(4) @JvmField val sGroupLocation: String? = "",
    @JceId(5) @JvmField val statOption: Byte? = null,
    @JceId(6) @JvmField val wSourceID: Int? = null,
    @JceId(7) @JvmField val wSourceSubID: Int? = null,
    @JceId(8) @JvmField val isSupportAuthQuestionJoin: Byte? = null,
    @JceId(9) @JvmField val ifGetAuthInfo: Byte? = null,
    @JceId(10) @JvmField val dwDiscussUin: Long? = null,
    @JceId(11) @JvmField val sJoinGroupKey: String? = "",
    @JceId(12) @JvmField val sJoinGroupPicUrl: String? = "",
    @JceId(13) @JvmField val vecJoinGroupRichMsg: ByteArray? = null,
    @JceId(14) @JvmField val sJoinGroupAuth: String? = "",
    @JceId(15) @JvmField val sJoinGroupVerifyToken: String? = "",
    @JceId(16) @JvmField val dwJoinVerifyType: Long? = null
) : JceStruct

@Serializable
internal class GroupMngRes(
    @JceId(0) @JvmField val reqtype: Int,
    @JceId(1) @JvmField val result: Byte,
    @JceId(2) @JvmField val vecBody: ByteArray,
    @JceId(3) @JvmField val errorString: String = "",
    @JceId(4) @JvmField val errorCode: Short = 0,
    @JceId(5) @JvmField val isInGroup: Byte? = null,
    @JceId(6) @JvmField val sGroupLocation: String? = "",
    @JceId(7) @JvmField val isMemInvite: Byte? = null,
    @JceId(8) @JvmField val sAuthGrpInfo: String? = "",
    @JceId(9) @JvmField val sJoinQuestion: String? = "",
    @JceId(10) @JvmField val sJoinAnswer: String? = "",
    @JceId(11) @JvmField val dwDis2GrpLimitType: Long? = null
) : JceStruct
