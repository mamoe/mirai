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
    @Serializable
    internal class ReqPush(
        @SerialId(1) val unknown1:ByteArray=ByteArray(107),
        @SerialId(2) val groupid1:Int=0,
        @SerialId(3) val unknown2:ByteArray= ByteArray(18),
        @SerialId(4) val groupid2:Int=0,
        @SerialId(5) val unknown3:ByteArray= ByteArray(2),
        @SerialId(6) val uin:Int=0,
        @SerialId(7) val unknown4:ByteArray= ByteArray(6),
        @SerialId(8) val muteuin:Int=0,
        @SerialId(9) val mutetime:Int=0,
        @SerialId(10) val unknown5:ByteArray= ByteArray(109)
        ) : ProtoBuf
}