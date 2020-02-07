package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

class OnlinePushPack {
    @Serializable
    internal class DelMsgInfo(
        @SerialId(0) val fromUin: Long,
        @SerialId(1) val uMsgTime: Long,
        @SerialId(2) val shMsgSeq: Short,
        @SerialId(3) val vMsgCookies: ByteArray? = null,
        @SerialId(4) val wCmd: Short? = null,
        @SerialId(5) val uMsgType: Long? = null,
        @SerialId(6) val uAppId: Long? = null,
        @SerialId(7) val sendTime: Long? = null,
        @SerialId(8) val ssoSeq: Int? = null,
        @SerialId(9) val ssoIp: Int? = null,
        @SerialId(10) val clientIp: Int? = null
    ) : JceStruct

    @Serializable
    internal class DeviceInfo(
        @SerialId(0) val netType: Byte? = null,
        @SerialId(1) val devType: String? = "",
        @SerialId(2) val oSVer: String? = "",
        @SerialId(3) val vendorName: String? = "",
        @SerialId(4) val vendorOSName: String? = "",
        @SerialId(5) val iOSIdfa: String? = ""
    ) : JceStruct

    @Serializable
    internal class Name(
        @SerialId(0) val fromUin: Long,
        @SerialId(1) val uMsgTime: Long,
        @SerialId(2) val shMsgType: Short,
        @SerialId(3) val shMsgSeq: Short,
        @SerialId(4) val msg: String = "",
        @SerialId(5) val uRealMsgTime: Int? = null,
        @SerialId(6) val vMsg: ByteArray? = null,
        @SerialId(7) val uAppShareID: Long? = null,
        @SerialId(8) val vMsgCookies: ByteArray? = null,
        @SerialId(9) val vAppShareCookie: ByteArray? = null,
        @SerialId(10) val msgUid: Long? = null,
        @SerialId(11) val lastChangeTime: Long? = 1L,
        @SerialId(12) val vCPicInfo: List<CPicInfo>? = null,
        @SerialId(13) val stShareData: ShareData? = null,
        @SerialId(14) val fromInstId: Long? = null,
        @SerialId(15) val vRemarkOfSender: ByteArray? = null,
        @SerialId(16) val fromMobile: String? = "",
        @SerialId(17) val fromName: String? = "",
        @SerialId(18) val vNickName: List<String>? = null,
        @SerialId(19) val stC2CTmpMsgHead: TempMsgHead? = null
    ) : JceStruct

    @Serializable
    internal class SvcReqPushMsg(
        @SerialId(0) val uin: Long,
        @SerialId(1) val uMsgTime: Long,
        @SerialId(2) val vMsgInfos: List<MsgInfo>,
        @SerialId(3) val svrip: Int? = 0,
        @SerialId(4) val vSyncCookie: ByteArray? = null,
        @SerialId(5) val vUinPairMsg: List<UinPairMsg>? = null,
        @SerialId(6) val mPreviews: Map<String, ByteArray>? = null,
        @SerialId(7) val wUserActive: Int? = null,
        @SerialId(12) val wGeneralFlag: Int? = null
    ) : JceStruct

    @Serializable
    internal class SvcRespPushMsg(
        @SerialId(0) val uin: Long,
        @SerialId(1) val vDelInfos: List<DelMsgInfo>,
        @SerialId(2) val svrip: Int,
        @SerialId(3) val pushToken: ByteArray? = null,
        @SerialId(4) val serviceType: Int? = null,
        @SerialId(5) val deviceInfo: DeviceInfo? = null
    ) : JceStruct

    @Serializable
    internal class UinPairMsg(
        @SerialId(1) val uLastReadTime: Long? = null,
        @SerialId(2) val peerUin: Long? = null,
        @SerialId(3) val uMsgCompleted: Long? = null,
        @SerialId(4) val vMsgInfos: List<MsgInfo>? = null
    ) : JceStruct
}