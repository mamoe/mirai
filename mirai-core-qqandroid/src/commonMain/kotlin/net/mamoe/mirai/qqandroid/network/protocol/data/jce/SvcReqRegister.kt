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
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class SvcReqRegister(
    @JceId(0) @JvmField val lUin: Long = 0L,
    @JceId(1) @JvmField val lBid: Long = 0L,
    @JceId(2) @JvmField val cConnType: Byte = 0,
    @JceId(3) @JvmField val sOther: String = "",
    @JceId(4) @JvmField val iStatus: Int = 11,
    @JceId(5) @JvmField val bOnlinePush: Byte = 0,
    @JceId(6) @JvmField val bIsOnline: Byte = 0,
    @JceId(7) @JvmField val bIsShowOnline: Byte = 0,
    @JceId(8) @JvmField val bKikPC: Byte = 0,
    @JceId(9) @JvmField val bKikWeak: Byte = 0,
    @JceId(10) @JvmField val timeStamp: Long = 0L,
    @JceId(11) @JvmField val iOSVersion: Long = 0L,
    @JceId(12) @JvmField val cNetType: Byte = 0,
    @JceId(13) @JvmField val sBuildVer: String? = "",
    @JceId(14) @JvmField val bRegType: Byte = 0,
    @JceId(15) @JvmField val vecDevParam: ByteArray? = null,
    @JceId(16) @JvmField val vecGuid: ByteArray? = null,
    @JceId(17) @JvmField val iLocaleID: Int = 2052,
    @JceId(18) @JvmField val bSlientPush: Byte = 0,
    @JceId(19) @JvmField val strDevName: String? = null,
    @JceId(20) @JvmField val strDevType: String? = null,
    @JceId(21) @JvmField val strOSVer: String? = null,
    @JceId(22) @JvmField val bOpenPush: Byte = 1,
    @JceId(23) @JvmField val iLargeSeq: Long = 0L,
    @JceId(24) @JvmField val iLastWatchStartTime: Long = 0L,
    @JceId(26) @JvmField val uOldSSOIp: Long = 0L,
    @JceId(27) @JvmField val uNewSSOIp: Long = 0L,
    @JceId(28) @JvmField val sChannelNo: String? = null,
    @JceId(29) @JvmField val lCpId: Long = 0L,
    @JceId(30) @JvmField val strVendorName: String? = null,
    @JceId(31) @JvmField val strVendorOSName: String? = null,
    @JceId(32) @JvmField val strIOSIdfa: String? = null,
    @JceId(33) @JvmField val bytes_0x769_reqbody: ByteArray? = null,
    @JceId(34) @JvmField val bIsSetStatus: Byte = 0,
    @JceId(35) @JvmField val vecServerBuf: ByteArray? = null,
    @JceId(36) @JvmField val bSetMute: Byte = 0
    // @SerialId(25) var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct