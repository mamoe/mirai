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
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.JceId

@Suppress("ArrayInDataClass")
@Serializable
internal class RequestPushNotify(
    @JceId(0) val uin: Long? = 0L,
    @JceId(1) val ctype: Byte = 0,
    @JceId(2) val strService: String?,
    @JceId(3) val strCmd: String?,
    @JceId(4) val vNotifyCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @JceId(5) val usMsgType: Int?,
    @JceId(6) val wUserActive: Int?,
    @JceId(7) val wGeneralFlag: Int?,
    @JceId(8) val bindedUin: Long?,
    @JceId(9) val stMsgInfo: MsgInfo?,
    @JceId(10) val msgCtrlBuf: String?,
    @JceId(11) val serverBuf: ByteArray?,
    @JceId(12) val pingFlag: Long?,
    @JceId(13) val svrip: Int?
) : JceStruct, Packet

@Serializable
internal class MsgInfo(
    @JceId(0) val lFromUin: Long? = 0L,
    @JceId(1) val uMsgTime: Long? = 0L,
    @JceId(2) val shMsgType: Short,
    @JceId(3) val shMsgSeq: Short,
    @JceId(4) val strMsg: String?,
    @JceId(5) val uRealMsgTime: Int?,
    @JceId(6) val vMsg: ByteArray,
    @JceId(7) val uAppShareID: Long?,
    @JceId(8) val vMsgCookies: ByteArray? = EMPTY_BYTE_ARRAY,
    @JceId(9) val vAppShareCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @JceId(10) val lMsgUid: Long?,
    @JceId(11) val lLastChangeTime: Long?,
    @JceId(12) val vCPicInfo: List<CPicInfo>?,
    @JceId(13) val stShareData: ShareData?,
    @JceId(14) val lFromInstId: Long?,
    @JceId(15) val vRemarkOfSender: ByteArray?,
    @JceId(16) val strFromMobile: String?,
    @JceId(17) val strFromName: String?,
    @JceId(18) val vNickName: List<String>?//,
    //@SerialId(19) val stC2CTmpMsgHead: TempMsgHead?
) : JceStruct


@Serializable
internal class ShareData(
    @JceId(0) val pkgname: String = "",
    @JceId(1) val msgtail: String = "",
    @JceId(2) val picurl: String = "",
    @JceId(3) val url: String = ""
) : JceStruct

@Serializable
internal class TempMsgHead(
    @JceId(0) val c2c_type: Int? = 0,
    @JceId(1) val serviceType: Int? = 0
) : JceStruct

@Serializable
internal class CPicInfo(
    @JceId(0) val vPath: ByteArray = EMPTY_BYTE_ARRAY,
    @JceId(1) val vHost: ByteArray? = EMPTY_BYTE_ARRAY
) : JceStruct