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
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class SvcReqRegister(
    @ProtoId(0) val lUin: Long = 0L,
    @ProtoId(1) val lBid: Long = 0L,
    @ProtoId(2) val cConnType: Byte = 0,
    @ProtoId(3) val sOther: String = "",
    @ProtoId(4) val iStatus: Int = 11,
    @ProtoId(5) val bOnlinePush: Byte = 0,
    @ProtoId(6) val bIsOnline: Byte = 0,
    @ProtoId(7) val bIsShowOnline: Byte = 0,
    @ProtoId(8) val bKikPC: Byte = 0,
    @ProtoId(9) val bKikWeak: Byte = 0,
    @ProtoId(10) val timeStamp: Long = 0L,
    @ProtoId(11) val iOSVersion: Long = 0L,
    @ProtoId(12) val cNetType: Byte = 0,
    @ProtoId(13) val sBuildVer: String? = "",
    @ProtoId(14) val bRegType: Byte = 0,
    @ProtoId(15) val vecDevParam: ByteArray? = null,
    @ProtoId(16) val vecGuid: ByteArray? = null,
    @ProtoId(17) val iLocaleID: Int = 2052,
    @ProtoId(18) val bSlientPush: Byte = 0,
    @ProtoId(19) val strDevName: String? = null,
    @ProtoId(20) val strDevType: String? = null,
    @ProtoId(21) val strOSVer: String? = null,
    @ProtoId(22) val bOpenPush: Byte = 1,
    @ProtoId(23) val iLargeSeq: Long = 0L,
    @ProtoId(24) val iLastWatchStartTime: Long = 0L,
    @ProtoId(26) val uOldSSOIp: Long = 0L,
    @ProtoId(27) val uNewSSOIp: Long = 0L,
    @ProtoId(28) val sChannelNo: String? = null,
    @ProtoId(29) val lCpId: Long = 0L,
    @ProtoId(30) val strVendorName: String? = null,
    @ProtoId(31) val strVendorOSName: String? = null,
    @ProtoId(32) val strIOSIdfa: String? = null,
    @ProtoId(33) val bytes_0x769_reqbody: ByteArray? = null,
    @ProtoId(34) val bIsSetStatus: Byte = 0,
    @ProtoId(35) val vecServerBuf: ByteArray? = null,
    @ProtoId(36) val bSetMute: Byte = 0
    // @SerialId(25) var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct