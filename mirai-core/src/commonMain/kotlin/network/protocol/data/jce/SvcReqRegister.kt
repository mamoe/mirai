/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

@Serializable
internal class SvcReqRegister(
    @TarsId(0) @JvmField val lUin: Long = 0L,
    @TarsId(1) @JvmField val lBid: Long = 0L,
    @TarsId(2) @JvmField val cConnType: Byte = 0,
    @TarsId(3) @JvmField val sOther: String = "",
    @TarsId(4) @JvmField val iStatus: Int = 11,
    @TarsId(5) @JvmField val bOnlinePush: Byte = 0,
    @TarsId(6) @JvmField val bIsOnline: Byte = 0,
    @TarsId(7) @JvmField val bIsShowOnline: Byte = 0,
    @TarsId(8) @JvmField val bKikPC: Byte = 0,
    @TarsId(9) @JvmField val bKikWeak: Byte = 0,
    @TarsId(10) @JvmField val timeStamp: Long = 0L,
    @TarsId(11) @JvmField val iOSVersion: Long = 0L,
    @TarsId(12) @JvmField val cNetType: Byte = 0,
    @TarsId(13) @JvmField val sBuildVer: String? = "",
    @TarsId(14) @JvmField val bRegType: Byte = 0,
    @TarsId(15) @JvmField val vecDevParam: ByteArray? = null,
    @TarsId(16) @JvmField val vecGuid: ByteArray? = null,
    @TarsId(17) @JvmField val iLocaleID: Int = 2052,
    @TarsId(18) @JvmField val bSlientPush: Byte = 0,
    @TarsId(19) @JvmField val strDevName: String? = null,
    @TarsId(20) @JvmField val strDevType: String? = null,
    @TarsId(21) @JvmField val strOSVer: String? = null,
    @TarsId(22) @JvmField val bOpenPush: Byte = 1,
    @TarsId(23) @JvmField val iLargeSeq: Long = 0L,
    @TarsId(24) @JvmField val iLastWatchStartTime: Long = 0L,
    @TarsId(26) @JvmField val uOldSSOIp: Long = 0L,
    @TarsId(27) @JvmField val uNewSSOIp: Long = 0L,
    @TarsId(28) @JvmField val sChannelNo: String? = null,
    @TarsId(29) @JvmField val lCpId: Long = 0L,
    @TarsId(30) @JvmField val strVendorName: String? = null,
    @TarsId(31) @JvmField val strVendorOSName: String? = null,
    @TarsId(32) @JvmField val strIOSIdfa: String? = null,
    @TarsId(33) @JvmField val bytes_0x769_reqbody: ByteArray? = null,
    @TarsId(34) @JvmField val bIsSetStatus: Byte = 0,
    @TarsId(35) @JvmField val vecServerBuf: ByteArray? = null,
    @TarsId(36) @JvmField val bSetMute: Byte = 0
    // @SerialId(25) var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct