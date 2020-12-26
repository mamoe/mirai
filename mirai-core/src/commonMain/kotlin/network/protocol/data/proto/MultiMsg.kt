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

@Serializable
internal class MultiMsg : ProtoBuf {
    @Serializable
    internal class ExternMsg(
        @ProtoNumber(1) @JvmField val channelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyDownReq(
        @ProtoNumber(1) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgType: Int = 0,
        @ProtoNumber(3) @JvmField val srcUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyDownRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val uint32DownIp: List<Int> = emptyList(),
        @ProtoNumber(5) @JvmField val uint32DownPort: List<Int> = emptyList(),
        @ProtoNumber(6) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val msgExternInfo: ExternMsg? = null,
        @ProtoNumber(8) @JvmField val bytesDownIpV6: List<ByteArray> = emptyList(),
        @ProtoNumber(9) @JvmField val uint32DownV6Port: List<Int> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyUpReq(
        @ProtoNumber(1) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgSize: Long = 0L,
        @ProtoNumber(3) @JvmField val msgMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val msgType: Int = 0,
        @ProtoNumber(5) @JvmField val applyId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyUpRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: String = "",
        @ProtoNumber(3) @JvmField val msgUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val uint32UpIp: List<Int> = emptyList(),
        @ProtoNumber(5) @JvmField val uint32UpPort: List<Int> = emptyList(),
        @ProtoNumber(6) @JvmField val blockSize: Long = 0L,
        @ProtoNumber(7) @JvmField val upOffset: Long = 0L,
        @ProtoNumber(8) @JvmField val applyId: Int = 0,
        @ProtoNumber(9) @JvmField val msgKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val msgSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val msgExternInfo: ExternMsg? = null,
        @ProtoNumber(12) @JvmField val bytesUpIpV6: List<ByteArray> = emptyList(),
        @ProtoNumber(13) @JvmField val uint32UpV6Port: List<Int> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val termType: Int = 0,
        @ProtoNumber(3) @JvmField val platformType: Int = 0,
        @ProtoNumber(4) @JvmField val netType: Int = 0,
        @ProtoNumber(5) @JvmField val buildVer: String = "",
        @ProtoNumber(6) @JvmField val multimsgApplyupReq: List<MultiMsg.MultiMsgApplyUpReq> = emptyList(),
        @ProtoNumber(7) @JvmField val multimsgApplydownReq: List<MultiMsg.MultiMsgApplyDownReq> = emptyList(),
        @ProtoNumber(8) @JvmField val buType: Int = 0,
        @ProtoNumber(9) @JvmField val reqChannelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val multimsgApplyupRsp: List<MultiMsg.MultiMsgApplyUpRsp> = emptyList(),
        @ProtoNumber(3) @JvmField val multimsgApplydownRsp: List<MultiMsg.MultiMsgApplyDownRsp> = emptyList()
    ) : ProtoBuf
}
