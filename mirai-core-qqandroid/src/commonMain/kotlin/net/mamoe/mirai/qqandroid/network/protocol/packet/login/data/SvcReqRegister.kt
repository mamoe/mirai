package net.mamoe.mirai.qqandroid.network.protocol.packet.login.data

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
class SvcReqRegister(
    @SerialId(0) val lUin: Long = 0L,
    @SerialId(1) val lBid: Long = 0L,
    @SerialId(2) val cConnType: Byte = 0,
    @SerialId(3) val sOther: String = "",
    @SerialId(4) val iStatus: Int = 11,
    @SerialId(5) val bOnlinePush: Byte = 0,
    @SerialId(6) val bIsOnline: Byte = 0,
    @SerialId(7) val bIsShowOnline: Byte = 0,
    @SerialId(8) val bKikPC: Byte = 0,
    @SerialId(9) val bKikWeak: Byte = 0,
    @SerialId(10) val timeStamp: Long = 0L,
    @SerialId(11) val iOSVersion: Long = 0L,
    @SerialId(12) val cNetType: Byte = 0,
    @SerialId(13) val sBuildVer: String? = "",
    @SerialId(14) val bRegType: Byte = 0,
    @SerialId(15) val vecDevParam: ByteArray? = null,
    @SerialId(16) val vecGuid: ByteArray? = null,
    @SerialId(17) val iLocaleID: Int = 2052,
    @SerialId(18) val bSlientPush: Byte = 0,
    @SerialId(19) val strDevName: String? = null,
    @SerialId(20) val strDevType: String? = null,
    @SerialId(21) val strOSVer: String? = null,
    @SerialId(22) val bOpenPush: Byte = 1,
    @SerialId(23) val iLargeSeq: Long = 0L,
    @SerialId(24) val iLastWatchStartTime: Long = 0L,
    @SerialId(26) val uOldSSOIp: Long = 0L,
    @SerialId(27) val uNewSSOIp: Long = 0L,
    @SerialId(28) val sChannelNo: String? = null,
    @SerialId(29) val lCpId: Long = 0L,
    @SerialId(30) val strVendorName: String? = null,
    @SerialId(31) val strVendorOSName: String? = null,
    @SerialId(32) val strIOSIdfa: String? = null,
    @SerialId(33) val bytes_0x769_reqbody: ByteArray? = null,
    @SerialId(34) val bIsSetStatus: Byte = 0,
    @SerialId(35) val vecServerBuf: ByteArray? = null,
    @SerialId(36) val bSetMute: Byte = 0
    // @SerialId(25) var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct