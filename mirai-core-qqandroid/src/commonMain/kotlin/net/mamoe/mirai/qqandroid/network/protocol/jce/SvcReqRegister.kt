package net.mamoe.mirai.qqandroid.network.protocol.jce

import net.mamoe.mirai.qqandroid.network.io.JceInput
import net.mamoe.mirai.qqandroid.network.io.JceOutput
import net.mamoe.mirai.qqandroid.network.io.JceStruct

class SvcReqRegister(
    var bIsOnline: Byte = 0,
    var bIsSetStatus: Byte = 0,
    var bIsShowOnline: Byte = 0,
    var bKikPC: Byte = 0,
    var bKikWeak: Byte = 0,
    var bOnlinePush: Byte = 0,
    var bOpenPush: Byte = 1,
    var bRegType: Byte = 0,
    var bSetMute: Byte = 0,
    var bSlientPush: Byte = 0,
    var bytes_0x769_reqbody: ByteArray? = null,
    var cConnType: Byte = 0,
    var cNetType: Byte = 0,
    var iLargeSeq: Long = 0L,
    var iLastWatchStartTime: Long = 0L,
    var iLocaleID: Int = 2052,
    var iOSVersion: Long = 0L,
    var iStatus: Int = 11,
    var lBid: Long = 0L,
    var lCpId: Long = 0L,
    var lUin: Long = 0L,
    var sBuildVer: String? = "",
    var sChannelNo: String? = "",
    var sOther: String = "",
    var strDevName: String? = null,
    var strDevType: String? = null,
    var strIOSIdfa: String? = "",
    var strOSVer: String? = null,
    var strVendorName: String? = null,
    var strVendorOSName: String? = null,
    var timeStamp: Long = 0L,
    var uNewSSOIp: Long = 0L,
    var uOldSSOIp: Long = 0L,
    var vecDevParam: ByteArray? = null,
    var vecGuid: ByteArray? = null,
    var vecServerBuf: ByteArray? = null,
    var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct() {
    constructor() : this(0)
    companion object : Factory<SvcReqRegister> {
        override fun newInstanceFrom(input: JceInput): SvcReqRegister  = SvcReqRegister().apply {
            this.lUin = input.readLong(0)
            this.lBid = input.readLong(1)
            this.cConnType = input.readByte(2)
            this.sOther = input.readString(3)
            this.iStatus = input.readInt(4)
            this.bOnlinePush = input.readByte(5)
            this.bIsOnline = input.readByte(6)
            this.bIsShowOnline = input.readByte(7)
            this.bKikPC = input.readByte(8)
            this.bKikWeak = input.readByte(9)
            this.timeStamp = input.readLong(10)
            this.iOSVersion = input.readLong(11)
            this.cNetType = input.readByte(12)
            this.sBuildVer = input.readStringOrNull(13)
            this.bRegType = input.readByte(14)
            this.vecDevParam = input.readByteArrayOrNull(15)
            this.vecGuid = input.readByteArrayOrNull(16)
            this.iLocaleID = input.readIntOrNull(17) ?: this.iLocaleID
            this.bSlientPush = input.readByteOrNull(18) ?: this.bSlientPush
            this.strDevName = input.readStringOrNull(19) ?: this.strDevName
            this.strDevType = input.readStringOrNull(20) ?: this.strDevType
            this.strOSVer = input.readStringOrNull(21) ?: this.strOSVer
            this.bOpenPush = input.readByteOrNull(22) ?: this.bOpenPush
            this.iLargeSeq = input.readLongOrNull(23) ?: this.iLargeSeq
            this.iLastWatchStartTime = input.readLongOrNull(24) ?: this.iLastWatchStartTime
          //  this.vecBindUin = input.readObject(this.vecBindUin, 25) ?: this.iLocaleID
            this.uOldSSOIp = input.readLongOrNull(26) ?: this.uOldSSOIp
            this.uNewSSOIp = input.readLongOrNull(27) ?: this.uNewSSOIp
            this.sChannelNo = input.readStringOrNull(28) ?: this.sChannelNo
            this.lCpId = input.readLongOrNull(29) ?: this.lCpId
            this.strVendorName = input.readStringOrNull(30) ?: this.strVendorName
            this.strVendorOSName = input.readStringOrNull(31) ?: this.strVendorOSName
            this.strIOSIdfa = input.readStringOrNull(32) ?: this.strIOSIdfa
            this.bytes_0x769_reqbody = input.readByteArrayOrNull(33) ?: this.bytes_0x769_reqbody
            this.bIsSetStatus = input.readByteOrNull(34) ?: this.bIsSetStatus
            this.vecServerBuf = input.readByteArrayOrNull(35) ?: this.vecServerBuf
            this.bSetMute = input.readByteOrNull(36) ?: this.bSetMute
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
            builder.write(sBuildVer!!, 13)
        }

        builder.write(bRegType, 14)
        if (vecDevParam != null) {
            builder.write(vecDevParam!!, 15)
        }

        if (vecGuid != null) {
            builder.write(vecGuid!!, 16)
        }

        builder.write(iLocaleID, 17)
        builder.write(bSlientPush, 18)
        if (strDevName != null) {
            builder.write(strDevName!!, 19)
        }

        if (strDevType != null) {
            builder.write(strDevType!!, 20)
        }

        if (strOSVer != null) {
            builder.write(strOSVer!!, 21)
        }

        builder.write(bOpenPush, 22)
        builder.write(iLargeSeq, 23)
        builder.write(iLastWatchStartTime, 24)
        if (this.vecBindUin != null) {
            builder.write(this.vecBindUin!!, 25)
        }

        builder.write(uOldSSOIp, 26)
        builder.write(uNewSSOIp, 27)
        if (sChannelNo != null) {
            builder.write(sChannelNo!!, 28)
        }

        builder.write(lCpId, 29)
        if (strVendorName != null) {
            builder.write(strVendorName!!, 30)
        }

        if (strVendorOSName != null) {
            builder.write(strVendorOSName!!, 31)
        }

        if (strIOSIdfa != null) {
            builder.write(strIOSIdfa!!, 32)
        }

        if (bytes_0x769_reqbody != null) {
            builder.write(bytes_0x769_reqbody!!, 33)
        }

        builder.write(bIsSetStatus, 34)
        if (vecServerBuf != null) {
            builder.write(vecServerBuf!!, 35)
        }

        builder.write(bSetMute, 36)
    }
}