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
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class Oidb0x858 : ProtoBuf {
    @Serializable
internal class GoldMsgTipsElem(
        @ProtoId(1) @JvmField val type: Int = 0,
        @ProtoId(2) @JvmField val billno: String = "",
        @ProtoId(3) @JvmField val result: Int = 0,
        @ProtoId(4) @JvmField val amount: Int = 0,
        @ProtoId(5) @JvmField val total: Int = 0,
        @ProtoId(6) @JvmField val interval: Int = 0,
        @ProtoId(7) @JvmField val finish: Int = 0,
        @ProtoId(8) @JvmField val uin: List<Long>? = null,
        @ProtoId(9) @JvmField val action: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MessageRecallReminder(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val recalledMsgList: List<MessageMeta> = listOf(),
        @ProtoId(4) @JvmField val reminderContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
internal class MessageMeta(
            @ProtoId(1) @JvmField val seq: Int = 0,
            @ProtoId(2) @JvmField val time: Int = 0,
            @ProtoId(3) @JvmField val msgRandom: Int = 0
        ) : ProtoBuf
    }

    @Serializable
internal class NotifyMsgBody(
        @ProtoId(1) @JvmField val optEnumType: Int /* enum */ = 5,
        @ProtoId(2) @JvmField val optUint64MsgTime: Long = 0L,
        @ProtoId(3) @JvmField val optUint64MsgExpires: Long = 0L,
        @ProtoId(4) @JvmField val optUint64ConfUin: Long = 0L,
        @ProtoId(5) @JvmField val optMsgRedtips: RedGrayTipsInfo? = null,
        @ProtoId(6) @JvmField val optMsgRecallReminder: MessageRecallReminder? = null,
        @ProtoId(7) @JvmField val optMsgObjUpdate: NotifyObjmsgUpdate? = null,
        // @SerialId(8) @JvmField val optStcmGameState: ApolloGameStatus.STCMGameMessage? = null,
        // @SerialId(9) @JvmField val aplloMsgPush: ApolloPushMsgInfo.STPushMsgElem? = null,
        @ProtoId(10) @JvmField val optMsgGoldtips: GoldMsgTipsElem? = null
    ) : ProtoBuf

    @Serializable
internal class NotifyObjmsgUpdate(
        @ProtoId(1) @JvmField val objmsgId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val updateType: Int = 0,
        @ProtoId(3) @JvmField val extMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class RedGrayTipsInfo(
        @ProtoId(1) @JvmField val optUint32ShowLastest: Int = 0,
        @ProtoId(2) @JvmField val senderUin: Long = 0L,
        @ProtoId(3) @JvmField val receiverUin: Long = 0L,
        @ProtoId(4) @JvmField val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(7) @JvmField val sint32Msgtype: Int = 0,
        @ProtoId(8) @JvmField val luckyFlag: Int = 0,
        @ProtoId(9) @JvmField val hideFlag: Int = 0,
        @ProtoId(10) @JvmField val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val icon: Int = 0,
        @ProtoId(12) @JvmField val luckyUin: Long = 0L,
        @ProtoId(13) @JvmField val time: Int = 0,
        @ProtoId(14) @JvmField val random: Int = 0,
        @ProtoId(15) @JvmField val broadcastRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(16) @JvmField val idiom: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(17) @JvmField val idiomSeq: Int = 0,
        @ProtoId(18) @JvmField val idiomAlpha: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) @JvmField val jumpurl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}
