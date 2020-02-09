/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
internal class MsgOnlinePush {
    @Serializable
    internal class PbPushMsg(
        @SerialId(1) val msg: MsgComm.Msg,
        @SerialId(2) val svrip: Int = 0,
        @SerialId(3) val pushToken: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val pingFlag: Int = 0,
        @SerialId(9) val generalFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
class OnlinePushTrans : ProtoBuf {
    @Serializable
    class ExtGroupKeyInfo(
        @SerialId(1) val curMaxSeq: Int = 0,
        @SerialId(2) val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    class PbMsgInfo(
        @SerialId(1) val fromUin: Long = 0L,
        @SerialId(2) val toUin: Long = 0L,
        @SerialId(3) val msgType: Int = 0,
        @SerialId(4) val msgSubtype: Int = 0,
        @SerialId(5) val msgSeq: Int = 0,
        @SerialId(6) val msgUid: Long = 0L,
        @SerialId(7) val msgTime: Int = 0,
        @SerialId(8) val realMsgTime: Int = 0,
        @SerialId(9) val nickName: String = "",
        @SerialId(10) val msgData: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val svrIp: Int = 0,
        @SerialId(12) val extGroupKeyInfo: OnlinePushTrans.ExtGroupKeyInfo? = null,
        @SerialId(17) val generalFlag: Int = 0
    ) : ProtoBuf
}