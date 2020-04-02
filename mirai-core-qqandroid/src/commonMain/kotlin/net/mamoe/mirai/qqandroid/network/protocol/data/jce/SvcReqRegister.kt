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
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.JceId

@Serializable
internal class SvcReqRegister(
    @JceId(0) val lUin: Long = 0L,
    @JceId(1) val lBid: Long = 0L,
    @JceId(2) val cConnType: Byte = 0,
    @JceId(3) val sOther: String = "",
    @JceId(4) val iStatus: Int = 11,
    @JceId(5) val bOnlinePush: Byte = 0,
    @JceId(6) val bIsOnline: Byte = 0,
    @JceId(7) val bIsShowOnline: Byte = 0,
    @JceId(8) val bKikPC: Byte = 0,
    @JceId(9) val bKikWeak: Byte = 0,
    @JceId(10) val timeStamp: Long = 0L,
    @JceId(11) val iOSVersion: Long = 0L,
    @JceId(12) val cNetType: Byte = 0,
    @JceId(13) val sBuildVer: String? = "",
    @JceId(14) val bRegType: Byte = 0,
    @JceId(15) val vecDevParam: ByteArray? = null,
    @JceId(16) val vecGuid: ByteArray? = null,
    @JceId(17) val iLocaleID: Int = 2052,
    @JceId(18) val bSlientPush: Byte = 0,
    @JceId(19) val strDevName: String? = null,
    @JceId(20) val strDevType: String? = null,
    @JceId(21) val strOSVer: String? = null,
    @JceId(22) val bOpenPush: Byte = 1,
    @JceId(23) val iLargeSeq: Long = 0L,
    @JceId(24) val iLastWatchStartTime: Long = 0L,
    @JceId(26) val uOldSSOIp: Long = 0L,
    @JceId(27) val uNewSSOIp: Long = 0L,
    @JceId(28) val sChannelNo: String? = null,
    @JceId(29) val lCpId: Long = 0L,
    @JceId(30) val strVendorName: String? = null,
    @JceId(31) val strVendorOSName: String? = null,
    @JceId(32) val strIOSIdfa: String? = null,
    @JceId(33) val bytes_0x769_reqbody: ByteArray? = null,
    @JceId(34) val bIsSetStatus: Byte = 0,
    @JceId(35) val vecServerBuf: ByteArray? = null,
    @JceId(36) val bSetMute: Byte = 0
    // @SerialId(25) var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct