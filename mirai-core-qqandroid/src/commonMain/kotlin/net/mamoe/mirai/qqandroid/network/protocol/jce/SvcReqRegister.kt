package net.mamoe.mirai.qqandroid.network.protocol.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
class SvcReqRegister(
    @SerialId(6) var bIsOnline: Byte = 0,
    @SerialId(34) var bIsSetStatus: Byte = 0,
    @SerialId(7) var bIsShowOnline: Byte = 0,
    @SerialId(8) var bKikPC: Byte = 0,
    @SerialId(9) var bKikWeak: Byte = 0,
    @SerialId(5) var bOnlinePush: Byte = 0,
    @SerialId(22) var bOpenPush: Byte = 1,
    @SerialId(14) var bRegType: Byte = 0,
    @SerialId(36) var bSetMute: Byte = 0,
    @SerialId(18) var bSlientPush: Byte = 0,
    @SerialId(33) var bytes_0x769_reqbody: ByteArray? = null,
    @SerialId(2) var cConnType: Byte = 0,
    @SerialId(12) var cNetType: Byte = 0,
    @SerialId(23) var iLargeSeq: Long = 0L,
    @SerialId(24) var iLastWatchStartTime: Long = 0L,
    @SerialId(17) var iLocaleID: Int = 2052,
    @SerialId(11) var iOSVersion: Long = 0L,
    @SerialId(4) var iStatus: Int = 11,
    @SerialId(1) var lBid: Long = 0L,
    @SerialId(29) var lCpId: Long = 0L,
    @SerialId(0) var lUin: Long = 0L,
    @SerialId(13) var sBuildVer: String? = "",
    @SerialId(28) var sChannelNo: String? = "",
    @SerialId(3) var sOther: String = "",
    @SerialId(19) var strDevName: String? = null,
    @SerialId(20) var strDevType: String? = null,
    @SerialId(32) var strIOSIdfa: String? = "",
    @SerialId(21) var strOSVer: String? = null,
    @SerialId(30) var strVendorName: String? = null,
    @SerialId(31) var strVendorOSName: String? = null,
    @SerialId(10) var timeStamp: Long = 0L,
    @SerialId(27) var uNewSSOIp: Long = 0L,
    @SerialId(26) var uOldSSOIp: Long = 0L,
    @SerialId(15) var vecDevParam: ByteArray? = null,
    @SerialId(16) var vecGuid: ByteArray? = null,
    @SerialId(35) var vecServerBuf: ByteArray? = null
    // @SerialId(25) var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct