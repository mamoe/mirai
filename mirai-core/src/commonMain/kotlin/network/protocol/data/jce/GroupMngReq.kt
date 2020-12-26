/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

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
