package net.mamoe.mirai.qqandroid.network.protocol.jce

import net.mamoe.mirai.qqandroid.network.io.JceInput
import net.mamoe.mirai.qqandroid.network.io.JceOutput
import net.mamoe.mirai.qqandroid.network.io.JceStruct

class SvcReqRegister(
    val bIsOnline: Byte = 0,
    val bIsSetStatus: Byte = 0,
    val bIsShowOnline: Byte = 0,
    val bKikPC: Byte = 0,
    val bKikWeak: Byte = 0,
    val bOnlinePush: Byte = 0,
    val bOpenPush: Byte = 1,
    val bRegType: Byte = 0,
    val bSetMute: Byte = 0,
    val bSlientPush: Byte = 0,
    val bytes_0x769_reqbody: ByteArray? = null,
    val cConnType: Byte = 0,
    val cNetType: Byte = 0,
    val iLargeSeq: Long = 0L,
    val iLastWatchStartTime: Long = 0L,
    val iLocaleID: Int = 2052,
    val iOSVersion: Long = 0L,
    val iStatus: Int = 11,
    val lBid: Long = 0L,
    val lCpId: Long = 0L,
    val lUin: Long = 0L,
    val sBuildVer: String? = null,
    val sChannelNo: String? = null,
    val sOther: String = "",
    val strDevName: String? = null,
    val strDevType: String? = null,
    val strIOSIdfa: String? = null,
    val strOSVer: String? = null,
    val strVendorName: String? = null,
    val strVendorOSName: String? = null,
    val timeStamp: Long = 0L,
    val uNewSSOIp: Long = 0L,
    val uOldSSOIp: Long = 0L,
    val vecDevParam: ByteArray? = null,
    val vecGuid: ByteArray? = null,
    val vecServerBuf: ByteArray? = null
) : JceStruct() {
    companion object : Factory<RequestPacket> {
        override fun newInstanceFrom(input: JceInput): RequestPacket {
            TODO("not implemented")
        }
    }

    override fun writeTo(builder: JceOutput) {
        builder.write(lUin, 0)
        builder.write(lBid, 1)
        builder.write(cConnType, 2)
        builder.write(sOther, 3)
        builder.write(iStatus, 4)
        builder.write(bOnlinePush, 5)
        builder.write(bIsOnline, 6)
        builder.write(bIsShowOnline, 7)
        builder.write(bKikPC, 8)
        builder.write(bKikWeak, 9)
        builder.write(timeStamp, 10)
        builder.write(iOSVersion, 11)
        builder.write(cNetType, 12)
        if (sBuildVer != null) {
            builder.write(sBuildVer, 13)
        }

        builder.write(bRegType, 14)
        if (vecDevParam != null) {
            builder.write(vecDevParam, 15)
        }

        if (vecGuid != null) {
            builder.write(vecGuid, 16)
        }

        builder.write(iLocaleID, 17)
        builder.write(bSlientPush, 18)
        if (strDevName != null) {
            builder.write(strDevName, 19)
        }

        if (strDevType != null) {
            builder.write(strDevType, 20)
        }

        if (strOSVer != null) {
            builder.write(strOSVer, 21)
        }

        builder.write(bOpenPush, 22)
        builder.write(iLargeSeq, 23)
        builder.write(iLastWatchStartTime, 24)
       // if (this.vecBindUin != null) {
       //     builder.write(this.vecBindUin, 25)
       // }

        builder.write(uOldSSOIp, 26)
        builder.write(uNewSSOIp, 27)
        if (sChannelNo != null) {
            builder.write(sChannelNo, 28)
        }

        builder.write(lCpId, 29)
        if (strVendorName != null) {
            builder.write(strVendorName, 30)
        }

        if (strVendorOSName != null) {
            builder.write(strVendorOSName, 31)
        }

        if (strIOSIdfa != null) {
            builder.write(strIOSIdfa, 32)
        }

        if (bytes_0x769_reqbody != null) {
            builder.write(bytes_0x769_reqbody, 33)
        }

        builder.write(bIsSetStatus, 34)
        if (vecServerBuf != null) {
            builder.write(vecServerBuf, 35)
        }

        builder.write(bSetMute, 36)
    }
}