/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Suppress("ArrayInDataClass")
@Serializable
internal data class RequestPushNotify(
    @ProtoId(0) val uin: Long? = 0L,
    @ProtoId(1) val ctype: Byte = 0,
    @ProtoId(2) val strService: String?,
    @ProtoId(3) val strCmd: String?,
    @ProtoId(4) val vNotifyCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @ProtoId(5) val usMsgType: Int?,
    @ProtoId(6) val wUserActive: Int?,
    @ProtoId(7) val wGeneralFlag: Int?,
    @ProtoId(8) val bindedUin: Long?,
    @ProtoId(9) val stMsgInfo: MsgInfo?,
    @ProtoId(10) val msgCtrlBuf: String?,
    @ProtoId(11) val serverBuf: ByteArray?,
    @ProtoId(12) val pingFlag: Long?,
    @ProtoId(13) val svrip: Int?
) : JceStruct, Packet

@Serializable
internal class MsgInfo(
    @ProtoId(0) val lFromUin: Long? = 0L,
    @ProtoId(1) val uMsgTime: Long? = 0L,
    @ProtoId(2) val shMsgType: Short,
    @ProtoId(3) val shMsgSeq: Short,
    @ProtoId(4) val strMsg: String?,
    @ProtoId(5) val uRealMsgTime: Int?,
    @ProtoId(6) val vMsg: ByteArray?,
    @ProtoId(7) val uAppShareID: Long?,
    @ProtoId(8) val vMsgCookies: ByteArray? = EMPTY_BYTE_ARRAY,
    @ProtoId(9) val vAppShareCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @ProtoId(10) val lMsgUid: Long?,
    @ProtoId(11) val lLastChangeTime: Long?,
    @ProtoId(12) val vCPicInfo: List<CPicInfo>?,
    @ProtoId(13) val stShareData: ShareData?,
    @ProtoId(14) val lFromInstId: Long?,
    @ProtoId(15) val vRemarkOfSender: ByteArray?,
    @ProtoId(16) val strFromMobile: String?,
    @ProtoId(17) val strFromName: String?,
    @ProtoId(18) val vNickName: List<String>?//,
    //@SerialId(19) val stC2CTmpMsgHead: TempMsgHead?
) : JceStruct


@Serializable
internal class ShareData(
    @ProtoId(0) val pkgname: String = "",
    @ProtoId(1) val msgtail: String = "",
    @ProtoId(2) val picurl: String = "",
    @ProtoId(3) val url: String = ""
) : JceStruct

@Serializable
internal class TempMsgHead(
    @ProtoId(0) val c2c_type: Int? = 0,
    @ProtoId(1) val serviceType: Int? = 0
) : JceStruct

@Serializable
internal class CPicInfo(
    @ProtoId(0) val vPath: ByteArray = EMPTY_BYTE_ARRAY,
    @ProtoId(1) val vHost: ByteArray? = EMPTY_BYTE_ARRAY
) : JceStruct