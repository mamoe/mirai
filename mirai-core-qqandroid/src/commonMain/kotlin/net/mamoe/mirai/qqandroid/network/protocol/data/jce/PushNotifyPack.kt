/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Suppress("ArrayInDataClass")
@Serializable
internal data class RequestPushNotify(
    @SerialId(0) val uin: Long? = 0L,
    @SerialId(1) val ctype: Byte = 0,
    @SerialId(2) val strService: String?,
    @SerialId(3) val strCmd: String?,
    @SerialId(4) val vNotifyCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @SerialId(5) val usMsgType: Int?,
    @SerialId(6) val wUserActive: Int?,
    @SerialId(7) val wGeneralFlag: Int?,
    @SerialId(8) val bindedUin: Long?,
    @SerialId(9) val stMsgInfo: MsgInfo?,
    @SerialId(10) val msgCtrlBuf: String?,
    @SerialId(11) val serverBuf: ByteArray?,
    @SerialId(12) val pingFlag: Long?,
    @SerialId(13) val svrip: Int?
) : JceStruct, Packet

@Serializable
internal class MsgInfo(
    @SerialId(0) val lFromUin: Long? = 0L,
    @SerialId(1) val uMsgTime: Long? = 0L,
    @SerialId(2) val shMsgType: Short,
    @SerialId(3) val shMsgSeq: Short,
    @SerialId(4) val strMsg: String?,
    @SerialId(5) val uRealMsgTime: Int?,
    @SerialId(6) val vMsg: ByteArray?,
    @SerialId(7) val uAppShareID: Long?,
    @SerialId(8) val vMsgCookies: ByteArray? = EMPTY_BYTE_ARRAY,
    @SerialId(9) val vAppShareCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @SerialId(10) val lMsgUid: Long?,
    @SerialId(11) val lLastChangeTime: Long?,
    @SerialId(12) val vCPicInfo: List<CPicInfo>?,
    @SerialId(13) val stShareData: ShareData?,
    @SerialId(14) val lFromInstId: Long?,
    @SerialId(15) val vRemarkOfSender: ByteArray?,
    @SerialId(16) val strFromMobile: String?,
    @SerialId(17) val strFromName: String?,
    @SerialId(18) val vNickName: List<String>?//,
    //@SerialId(19) val stC2CTmpMsgHead: TempMsgHead?
) : JceStruct


@Serializable
internal class ShareData(
    @SerialId(0) val pkgname: String = "",
    @SerialId(1) val msgtail: String = "",
    @SerialId(2) val picurl: String = "",
    @SerialId(3) val url: String = ""
) : JceStruct

@Serializable
internal class TempMsgHead(
    @SerialId(0) val c2c_type: Int? = 0,
    @SerialId(1) val serviceType: Int? = 0
) : JceStruct

@Serializable
internal class CPicInfo(
    @SerialId(0) val vPath: ByteArray = EMPTY_BYTE_ARRAY,
    @SerialId(1) val vHost: ByteArray? = EMPTY_BYTE_ARRAY
) : JceStruct