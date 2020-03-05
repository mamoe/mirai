/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
internal class MsgOnlinePush {
    @Serializable
    internal class PbPushMsg(
        @ProtoId(1) val msg: MsgComm.Msg,
        @ProtoId(2) val svrip: Int = 0,
        @ProtoId(3) val pushToken: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val pingFlag: Int = 0,
        @ProtoId(9) val generalFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
class OnlinePushTrans : ProtoBuf {
    @Serializable
    class ExtGroupKeyInfo(
        @ProtoId(1) val curMaxSeq: Int = 0,
        @ProtoId(2) val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    class PbMsgInfo(
        @ProtoId(1) val fromUin: Long = 0L,
        @ProtoId(2) val toUin: Long = 0L,
        @ProtoId(3) val msgType: Int = 0,
        @ProtoId(4) val msgSubtype: Int = 0,
        @ProtoId(5) val msgSeq: Int = 0,
        @ProtoId(6) val msgUid: Long = 0L,
        @ProtoId(7) val msgTime: Int = 0,
        @ProtoId(8) val realMsgTime: Int = 0,
        @ProtoId(9) val nickName: String = "",
        @ProtoId(10) val msgData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val svrIp: Int = 0,
        @ProtoId(12) val extGroupKeyInfo: OnlinePushTrans.ExtGroupKeyInfo? = null,
        @ProtoId(17) val generalFlag: Int = 0
    ) : ProtoBuf
}