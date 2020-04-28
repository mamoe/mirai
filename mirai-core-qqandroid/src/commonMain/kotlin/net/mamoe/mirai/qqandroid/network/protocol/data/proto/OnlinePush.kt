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
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class MsgOnlinePush {
    @Serializable
    internal class PbPushMsg(
        @ProtoId(1) @JvmField val msg: MsgComm.Msg,
        @ProtoId(2) @JvmField val svrip: Int = 0,
        @ProtoId(3) @JvmField val pushToken: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val pingFlag: Int = 0,
        @ProtoId(9) @JvmField val generalFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class OnlinePushTrans : ProtoBuf {
    @Serializable
    internal class ExtGroupKeyInfo(
        @ProtoId(1) @JvmField val curMaxSeq: Int = 0,
        @ProtoId(2) @JvmField val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbMsgInfo(
        @ProtoId(1) @JvmField val fromUin: Long = 0L,
        @ProtoId(2) @JvmField val toUin: Long = 0L,
        @ProtoId(3) @JvmField val msgType: Int = 0,
        @ProtoId(4) @JvmField val msgSubtype: Int = 0,
        @ProtoId(5) @JvmField val msgSeq: Int = 0,
        @ProtoId(6) @JvmField val msgUid: Long = 0L,
        @ProtoId(7) @JvmField val msgTime: Int = 0,
        @ProtoId(8) @JvmField val realMsgTime: Int = 0,
        @ProtoId(9) @JvmField val nickName: String = "",
        @ProtoId(10) @JvmField val msgData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val svrIp: Int = 0,
        @ProtoId(12) @JvmField val extGroupKeyInfo: ExtGroupKeyInfo? = null,
        @ProtoId(17) @JvmField val generalFlag: Int = 0
    ) : ProtoBuf
}