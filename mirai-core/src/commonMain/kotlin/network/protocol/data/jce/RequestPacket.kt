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
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

private val EMPTY_MAP = mapOf<String, String>()

@Serializable
internal class RequestPacket(
    @TarsId(1) @JvmField val version: Short? = 3,
    @TarsId(2) @JvmField val cPacketType: Byte = 0,
    @TarsId(3) @JvmField val iMessageType: Int = 0,
    @TarsId(4) @JvmField val requestId: Int = 0,
    @TarsId(5) @JvmField val servantName: String = "",
    @TarsId(6) @JvmField val funcName: String = "",
    @TarsId(7) @JvmField val sBuffer: ByteArray = EMPTY_BYTE_ARRAY,
    @TarsId(8) @JvmField val iTimeout: Int? = 0,
    @TarsId(9) @JvmField val context: Map<String, String>? = EMPTY_MAP,
    @TarsId(10) @JvmField val status: Map<String, String>? = EMPTY_MAP
) : JceStruct

@Serializable
internal class RequestDataVersion3(
    @TarsId(0) @JvmField val map: Map<String, ByteArray> // 注意: ByteArray 不能直接放序列化的 JceStruct!! 要放类似 RequestDataStructSvcReqRegister 的
) : JceStruct

@Serializable
internal class RequestDataVersion2(
    @TarsId(0) @JvmField val map: Map<String, Map<String, ByteArray>>
) : JceStruct

@Serializable
internal class RequestDataStructSvcReqRegister(
    @TarsId(0) @JvmField val struct: SvcReqRegister
) : JceStruct