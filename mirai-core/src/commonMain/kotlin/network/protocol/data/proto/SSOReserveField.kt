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
        @JvmField @ProtoNumber(1) val client_ipcookie: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val flag: Int = 0,
        @JvmField @ProtoNumber(3) val env_id: Int = 0,
        @JvmField @ProtoNumber(4) val locale_id: Int = 0,
        @JvmField @ProtoNumber(5) val qimei: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(6) val env: String = "",
        @JvmField @ProtoNumber(7) val newconn_flag: Int = 0,
        @JvmField @ProtoNumber(8) val trace_parent: String = "",
        @JvmField @ProtoNumber(9) val uid: String = "",
        @JvmField @ProtoNumber(10) val imsi: Int = 0,
        @JvmField @ProtoNumber(11) val network_type: Int = 0,
        @JvmField @ProtoNumber(12) val ip_stack_type: Int = 0,
        @JvmField @ProtoNumber(13) val message_type: Int = 0,
        @JvmField @ProtoNumber(14) val trpc_rsp: SsoTrpcResponse? = null,
        @JvmField @ProtoNumber(15) val trans_info: List<SsoMapEntry>? = null,
        @JvmField @ProtoNumber(16) val sec_info: SsoSecureInfo? = null,
        @JvmField @ProtoNumber(17) val sec_sig_flag: Int = 0,
        @JvmField @ProtoNumber(18) val nt_core_version: Int = 0,
        @JvmField @ProtoNumber(19) val sso_route_cost: Int = 0,
        @JvmField @ProtoNumber(20) val sso_ip_origin: Int = 0,
        @JvmField @ProtoNumber(21) val presure_token: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class SsoSecureInfo(
        @JvmField @ProtoNumber(1) val sec_sig: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val sec_device_token: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val sec_extra: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class SsoTrpcResponse(
        @JvmField @ProtoNumber(1) val ret: Int = 0,
        @JvmField @ProtoNumber(2) val func_ret: Int = 0,
        @JvmField @ProtoNumber(3) val error_msg: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class SsoMapEntry(
        @JvmField @ProtoNumber(1) val key: String = "",
        @JvmField @ProtoNumber(2) val value: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf
}