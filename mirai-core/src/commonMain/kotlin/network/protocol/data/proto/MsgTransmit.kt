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

internal class MsgTransmit : ProtoBuf {
    @Serializable
internal class PbMultiMsgItem(
        @ProtoNumber(1) @JvmField val fileName: String = "",
        @ProtoNumber(2) @JvmField val buffer: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class PbMultiMsgNew(
        @ProtoNumber(1) @JvmField val msg: List<MsgComm.Msg> = emptyList()
    ) : ProtoBuf

    @Serializable
internal class PbMultiMsgTransmit(
        @ProtoNumber(1) @JvmField val msg: List<MsgComm.Msg> = emptyList(),
        @ProtoNumber(2) @JvmField val pbItemList: List<MsgTransmit.PbMultiMsgItem> = emptyList()
    ) : ProtoBuf
}