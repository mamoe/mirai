package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Suppress("ArrayInDataClass")
@Serializable
internal class RequestPushNotify(
    @JceId(0) @JvmField val uin: Long? = 0L,
    @JceId(1) @JvmField val ctype: Byte = 0,
    @JceId(2) @JvmField val strService: String?,
    @JceId(3) @JvmField val strCmd: String?,
    @JceId(4) @JvmField val vNotifyCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @JceId(5) @JvmField val usMsgType: Int?,
    @JceId(6) @JvmField val wUserActive: Int?,
    @JceId(7) @JvmField val wGeneralFlag: Int?,
    @JceId(8) @JvmField val bindedUin: Long?,
    @JceId(9) @JvmField val stMsgInfo: MsgInfo?,
    @JceId(10) @JvmField val msgCtrlBuf: String?,
    @JceId(11) @JvmField val serverBuf: ByteArray?,
    @JceId(12) @JvmField val pingFlag: Long?,
    @JceId(13) @JvmField val svrip: Int?
) : JceStruct, Packet, Packet.NoLog

@Serializable
internal class MsgInfo(
    @JceId(0) @JvmField val lFromUin: Long = 0L,
    @JceId(1) @JvmField val uMsgTime: Long = 0L,
    @JceId(2) @JvmField val shMsgType: Short,
    @JceId(3) @JvmField val shMsgSeq: Short,
    @JceId(4) @JvmField val strMsg: String?,
    @JceId(5) @JvmField val uRealMsgTime: Int?,
    @JceId(6) @JvmField val vMsg: ByteArray,
    @JceId(7) @JvmField val uAppShareID: Long?,
    @JceId(8) @JvmField val vMsgCookies: ByteArray? = EMPTY_BYTE_ARRAY,
    @JceId(9) @JvmField val vAppShareCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @JceId(10) @JvmField val lMsgUid: Long?,
    @JceId(11) @JvmField val lLastChangeTime: Long?,
    @JceId(12) @JvmField val vCPicInfo: List<CPicInfo>?,
    @JceId(13) @JvmField val stShareData: ShareData?,
    @JceId(14) @JvmField val lFromInstId: Long?,
    @JceId(15) @JvmField val vRemarkOfSender: ByteArray?,
    @JceId(16) @JvmField val strFromMobile: String?,
    @JceId(17) @JvmField val strFromName: String?,
    @JceId(18) @JvmField val vNickName: List<String>?//,
    //@SerialId(19) @JvmField val stC2CTmpMsgHead: TempMsgHead?
) : JceStruct


@Serializable
internal class ShareData(
    @JceId(0) @JvmField val pkgname: String = "",
    @JceId(1) @JvmField val msgtail: String = "",
    @JceId(2) @JvmField val picurl: String = "",
    @JceId(3) @JvmField val url: String = ""
) : JceStruct

@Serializable
internal class TempMsgHead(
    @JceId(0) @JvmField val c2c_type: Int? = 0,
    @JceId(1) @JvmField val serviceType: Int? = 0
) : JceStruct

@Serializable
internal class CPicInfo(
    @JceId(0) @JvmField val vPath: ByteArray = EMPTY_BYTE_ARRAY,
    @JceId(1) @JvmField val vHost: ByteArray? = EMPTY_BYTE_ARRAY
) : JceStruct