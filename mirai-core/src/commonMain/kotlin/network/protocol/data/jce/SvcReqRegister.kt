/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class SvcReqRegister(
    @TarsId(0) @JvmField var lUin: Long = 0L,
    @TarsId(1) @JvmField var lBid: Long = 0L,
    @TarsId(2) @JvmField var cConnType: Byte = 0,
    @TarsId(3) @JvmField var sOther: String = "",
    @TarsId(4) @JvmField var iStatus: Int = 11,
    @TarsId(5) @JvmField var bOnlinePush: Byte = 0,
    @TarsId(6) @JvmField var bIsOnline: Byte = 0,
    @TarsId(7) @JvmField var bIsShowOnline: Byte = 0,
    @TarsId(8) @JvmField var bKikPC: Byte = 0,
    @TarsId(9) @JvmField var bKikWeak: Byte = 0,
    @TarsId(10) @JvmField var timeStamp: Long = 0L,
    @TarsId(11) @JvmField var iOSVersion: Long = 0L,
    @TarsId(12) @JvmField var cNetType: Byte = 0,
    @TarsId(13) @JvmField var sBuildVer: String? = "",
    @TarsId(14) @JvmField var bRegType: Byte = 0,
    @TarsId(15) @JvmField var vecDevParam: ByteArray? = null,
    @TarsId(16) @JvmField var vecGuid: ByteArray? = null,
    @TarsId(17) @JvmField var iLocaleID: Int = 2052,
    @TarsId(18) @JvmField var bSlientPush: Byte = 0,
    @TarsId(19) @JvmField var strDevName: String? = null,
    @TarsId(20) @JvmField var strDevType: String? = null,
    @TarsId(21) @JvmField var strOSVer: String? = null,
    @TarsId(22) @JvmField var bOpenPush: Byte,
    @TarsId(23) @JvmField var iLargeSeq: Long,
    @TarsId(24) @JvmField var iLastWatchStartTime: Long = 0L,
    @TarsId(26) @JvmField var uOldSSOIp: Long = 0L,
    @TarsId(27) @JvmField var uNewSSOIp: Long = 0L,
    @TarsId(28) @JvmField var sChannelNo: String? = null,
    @TarsId(29) @JvmField var lCpId: Long = 0L,
    @TarsId(30) @JvmField var strVendorName: String? = null,
    @TarsId(31) @JvmField var strVendorOSName: String? = null,
    @TarsId(32) @JvmField var strIOSIdfa: String? = null,
    @TarsId(33) @JvmField var bytes_0x769_reqbody: ByteArray? = null,
    @TarsId(34) @JvmField var bIsSetStatus: Byte = 0,
    @TarsId(35) @JvmField var vecServerBuf: ByteArray? = null,
    @TarsId(36) @JvmField var bSetMute: Byte = 0,
    // @SerialId(25) var vecBindUin: ArrayList<*>? = null // ?? 未知泛型
) : JceStruct