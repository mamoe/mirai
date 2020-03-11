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

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
class Oidb0x858 : ProtoBuf {
    @Serializable
    class GoldMsgTipsElem(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val billno: String = "",
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val amount: Int = 0,
        @ProtoId(5) val total: Int = 0,
        @ProtoId(6) val interval: Int = 0,
        @ProtoId(7) val finish: Int = 0,
        @ProtoId(8) val uin: List<Long>? = null,
        @ProtoId(9) val action: Int = 0
    ) : ProtoBuf

    @Serializable
    class MessageRecallReminder(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val recalledMsgList: List<MessageMeta> = listOf(),
        @ProtoId(4) val reminderContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        class MessageMeta(
            @ProtoId(1) val seq: Int = 0,
            @ProtoId(2) val time: Int = 0,
            @ProtoId(3) val msgRandom: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    class NotifyMsgBody(
        @ProtoId(1) val optEnumType: Int /* enum */ = 5,
        @ProtoId(2) val optUint64MsgTime: Long = 0L,
        @ProtoId(3) val optUint64MsgExpires: Long = 0L,
        @ProtoId(4) val optUint64ConfUin: Long = 0L,
        @ProtoId(5) val optMsgRedtips: RedGrayTipsInfo? = null,
        @ProtoId(6) val optMsgRecallReminder: MessageRecallReminder? = null,
        @ProtoId(7) val optMsgObjUpdate: NotifyObjmsgUpdate? = null,
        // @SerialId(8) val optStcmGameState: ApolloGameStatus.STCMGameMessage? = null,
        // @SerialId(9) val aplloMsgPush: ApolloPushMsgInfo.STPushMsgElem? = null,
        @ProtoId(10) val optMsgGoldtips: GoldMsgTipsElem? = null
    ) : ProtoBuf

    @Serializable
    class NotifyObjmsgUpdate(
        @ProtoId(1) val objmsgId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val updateType: Int = 0,
        @ProtoId(3) val extMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class RedGrayTipsInfo(
        @ProtoId(1) val optUint32ShowLastest: Int = 0,
        @ProtoId(2) val senderUin: Long = 0L,
        @ProtoId(3) val receiverUin: Long = 0L,
        @ProtoId(4) val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(7) val sint32Msgtype: Int = 0,
        @ProtoId(8) val luckyFlag: Int = 0,
        @ProtoId(9) val hideFlag: Int = 0,
        @ProtoId(10) val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val icon: Int = 0,
        @ProtoId(12) val luckyUin: Long = 0L,
        @ProtoId(13) val time: Int = 0,
        @ProtoId(14) val random: Int = 0,
        @ProtoId(15) val broadcastRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(16) val idiom: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(17) val idiomSeq: Int = 0,
        @ProtoId(18) val idiomAlpha: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) val jumpurl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}
