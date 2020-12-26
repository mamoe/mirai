/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

internal class LongMsg : ProtoBuf {
    @Serializable
internal class MsgDeleteReq(
        @ProtoNumber(1) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgDeleteRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgDownReq(
        @ProtoNumber(1) @JvmField val srcUin: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgType: Int = 0,
        @ProtoNumber(4) @JvmField val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgDownRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgUpReq(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(3) @JvmField val msgId: Int = 0,
        @ProtoNumber(4) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val storeType: Int = 0,
        @ProtoNumber(6) @JvmField val msgUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgUpRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgId: Int = 0,
        @ProtoNumber(3) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ReqBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val termType: Int = 0,
        @ProtoNumber(3) @JvmField val platformType: Int = 0,
        @ProtoNumber(4) @JvmField val msgUpReq: List<LongMsg.MsgUpReq> = emptyList(),
        @ProtoNumber(5) @JvmField val msgDownReq: List<LongMsg.MsgDownReq> = emptyList(),
        @ProtoNumber(6) @JvmField val msgDelReq: List<LongMsg.MsgDeleteReq> = emptyList(),
        @ProtoNumber(10) @JvmField val agentType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class RspBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val msgUpRsp: List<LongMsg.MsgUpRsp> = emptyList(),
        @ProtoNumber(3) @JvmField val msgDownRsp: List<LongMsg.MsgDownRsp> = emptyList(),
        @ProtoNumber(4) @JvmField val msgDelRsp: List<LongMsg.MsgDeleteRsp> = emptyList()
    ) : ProtoBuf
}