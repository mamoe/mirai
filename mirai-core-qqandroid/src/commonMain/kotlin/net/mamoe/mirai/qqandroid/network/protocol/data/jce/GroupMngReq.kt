package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class GroupMngReqJce(
    @TarsId(0) @JvmField val reqtype: Int,
    @TarsId(1) @JvmField val uin: Long,
    @TarsId(2) @JvmField val vecBody: ByteArray,
    @TarsId(3) @JvmField val checkInGroup: Byte? = null,
    @TarsId(4) @JvmField val sGroupLocation: String? = "",
    @TarsId(5) @JvmField val statOption: Byte? = null,
    @TarsId(6) @JvmField val wSourceID: Int? = null,
    @TarsId(7) @JvmField val wSourceSubID: Int? = null,
    @TarsId(8) @JvmField val isSupportAuthQuestionJoin: Byte? = null,
    @TarsId(9) @JvmField val ifGetAuthInfo: Byte? = null,
    @TarsId(10) @JvmField val dwDiscussUin: Long? = null,
    @TarsId(11) @JvmField val sJoinGroupKey: String? = "",
    @TarsId(12) @JvmField val sJoinGroupPicUrl: String? = "",
    @TarsId(13) @JvmField val vecJoinGroupRichMsg: ByteArray? = null,
    @TarsId(14) @JvmField val sJoinGroupAuth: String? = "",
    @TarsId(15) @JvmField val sJoinGroupVerifyToken: String? = "",
    @TarsId(16) @JvmField val dwJoinVerifyType: Long? = null
) : JceStruct

@Serializable
internal class GroupMngRes(
    @TarsId(0) @JvmField val reqtype: Int,
    @TarsId(1) @JvmField val result: Byte,
    @TarsId(2) @JvmField val vecBody: ByteArray,
    @TarsId(3) @JvmField val errorString: String = "",
    @TarsId(4) @JvmField val errorCode: Short = 0,
    @TarsId(5) @JvmField val isInGroup: Byte? = null,
    @TarsId(6) @JvmField val sGroupLocation: String? = "",
    @TarsId(7) @JvmField val isMemInvite: Byte? = null,
    @TarsId(8) @JvmField val sAuthGrpInfo: String? = "",
    @TarsId(9) @JvmField val sJoinQuestion: String? = "",
    @TarsId(10) @JvmField val sJoinAnswer: String? = "",
    @TarsId(11) @JvmField val dwDis2GrpLimitType: Long? = null
) : JceStruct
