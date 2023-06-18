/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY

internal class SSOReserveField {
    @Serializable
    internal class ReserveFields(
        @ProtoNumber(8) @JvmField val clientIpcookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val flag: Int = 0,
        @ProtoNumber(10) @JvmField val envId: Int = 0,
        @ProtoNumber(11) @JvmField val localeId: Int = 2052,
        @ProtoNumber(12) @JvmField val qimei: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val env: String = "",
        @ProtoNumber(14) @JvmField val newconnFlag: Int = 0,
        @ProtoNumber(15) @JvmField val traceParent: String = "",
        @ProtoNumber(16) @JvmField val uid: String = "",
        @ProtoNumber(18) @JvmField val imsi: Int = 0,
        @ProtoNumber(19) @JvmField val networkType: Int = 0,
        @ProtoNumber(20) @JvmField val ipStackType: Int = 0,
        @ProtoNumber(21) @JvmField val messageType: Int = 0,
        @ProtoNumber(22) @JvmField val trpcRsp: SsoTrpcResponse? = null,
        @ProtoNumber(23) @JvmField val transInfo: List<SsoMapEntry> = emptyList(),
        @ProtoNumber(24) @JvmField val secInfo: SsoSecureInfo? = null,
        @ProtoNumber(25) @JvmField val secSigFlag: Int = 0,
        @ProtoNumber(26) @JvmField val ntCoreVersion: Int = 0,
        @ProtoNumber(27) @JvmField val ssoRouteCost: Int = 0,
        @ProtoNumber(28) @JvmField val ssoIpOrigin: Int = 0,
        @ProtoNumber(30) @JvmField val presureToken: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class SsoMapEntry(
        @ProtoNumber(1) @JvmField val key: String = "",
        @ProtoNumber(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class SsoSecureInfo(
        @ProtoNumber(1) @JvmField val secSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val secDeviceToken: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val secExtra: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class SsoTrpcResponse(
        @ProtoNumber(1) @JvmField val ret: Int = 0,
        @ProtoNumber(2) @JvmField val funcRet: Int = 0,
        @ProtoNumber(3) @JvmField val errorMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}