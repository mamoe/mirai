/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("SpellCheckingInspection")

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
class Oidb0x858 : ProtoBuf {
    @Serializable
    class GoldMsgTipsElem(
        @SerialId(1) val type: Int = 0,
        @SerialId(2) val billno: String = "",
        @SerialId(3) val result: Int = 0,
        @SerialId(4) val amount: Int = 0,
        @SerialId(5) val total: Int = 0,
        @SerialId(6) val interval: Int = 0,
        @SerialId(7) val finish: Int = 0,
        @SerialId(8) val uin: List<Long>? = null,
        @SerialId(9) val action: Int = 0
    ) : ProtoBuf

    @Serializable
    class MessageRecallReminder(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val recalledMsgList: List<MessageMeta> = listOf(),
        @SerialId(4) val reminderContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        class MessageMeta(
            @SerialId(1) val seq: Int = 0,
            @SerialId(2) val time: Int = 0,
            @SerialId(3) val msgRandom: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    class NotifyMsgBody(
        @SerialId(1) val optEnumType: Int /* enum */ = 5,
        @SerialId(2) val optUint64MsgTime: Long = 0L,
        @SerialId(3) val optUint64MsgExpires: Long = 0L,
        @SerialId(4) val optUint64ConfUin: Long = 0L,
        @SerialId(5) val optMsgRedtips: RedGrayTipsInfo? = null,
        @SerialId(6) val optMsgRecallReminder: MessageRecallReminder? = null,
        @SerialId(7) val optMsgObjUpdate: NotifyObjmsgUpdate? = null,
        // @SerialId(8) val optStcmGameState: ApolloGameStatus.STCMGameMessage? = null,
        // @SerialId(9) val aplloMsgPush: ApolloPushMsgInfo.STPushMsgElem? = null,
        @SerialId(10) val optMsgGoldtips: GoldMsgTipsElem? = null
    ) : ProtoBuf

    @Serializable
    class NotifyObjmsgUpdate(
        @SerialId(1) val objmsgId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val updateType: Int = 0,
        @SerialId(3) val extMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class RedGrayTipsInfo(
        @SerialId(1) val optUint32ShowLastest: Int = 0,
        @SerialId(2) val senderUin: Long = 0L,
        @SerialId(3) val receiverUin: Long = 0L,
        @SerialId(4) val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(7) val sint32Msgtype: Int = 0,
        @SerialId(8) val luckyFlag: Int = 0,
        @SerialId(9) val hideFlag: Int = 0,
        @SerialId(10) val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val icon: Int = 0,
        @SerialId(12) val luckyUin: Long = 0L,
        @SerialId(13) val time: Int = 0,
        @SerialId(14) val random: Int = 0,
        @SerialId(15) val broadcastRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(16) val idiom: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(17) val idiomSeq: Int = 0,
        @SerialId(18) val idiomAlpha: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(19) val jumpurl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}
