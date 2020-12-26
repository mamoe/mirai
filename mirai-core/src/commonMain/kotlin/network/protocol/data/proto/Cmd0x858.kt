/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

@Serializable
internal class Oidb0x858 : ProtoBuf {
    @Serializable
internal class GoldMsgTipsElem(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val billno: String = "",
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val amount: Int = 0,
        @ProtoNumber(5) @JvmField val total: Int = 0,
        @ProtoNumber(6) @JvmField val interval: Int = 0,
        @ProtoNumber(7) @JvmField val finish: Int = 0,
        @ProtoNumber(8) @JvmField val uin: List<Long> = emptyList(),
        @ProtoNumber(9) @JvmField val action: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MessageRecallReminder(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val recalledMsgList: List<MessageMeta> = emptyList(),
        @ProtoNumber(4) @JvmField val reminderContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
internal class MessageMeta(
            @ProtoNumber(1) @JvmField val seq: Int = 0,
            @ProtoNumber(2) @JvmField val time: Int = 0,
            @ProtoNumber(3) @JvmField val msgRandom: Int = 0
        ) : ProtoBuf
    }

    @Serializable
internal class NotifyMsgBody(
        @ProtoNumber(1) @JvmField val optEnumType: Int /* enum */ = 5,
        @ProtoNumber(2) @JvmField val optUint64MsgTime: Long = 0L,
        @ProtoNumber(3) @JvmField val optUint64MsgExpires: Long = 0L,
        @ProtoNumber(4) @JvmField val optUint64ConfUin: Long = 0L,
        @ProtoNumber(5) @JvmField val optMsgRedtips: RedGrayTipsInfo? = null,
        @ProtoNumber(6) @JvmField val optMsgRecallReminder: MessageRecallReminder? = null,
        @ProtoNumber(7) @JvmField val optMsgObjUpdate: NotifyObjmsgUpdate? = null,
        // @SerialId(8) @JvmField val optStcmGameState: ApolloGameStatus.STCMGameMessage? = null,
        // @SerialId(9) @JvmField val aplloMsgPush: ApolloPushMsgInfo.STPushMsgElem? = null,
        @ProtoNumber(10) @JvmField val optMsgGoldtips: GoldMsgTipsElem? = null
    ) : ProtoBuf

    @Serializable
internal class NotifyObjmsgUpdate(
        @ProtoNumber(1) @JvmField val objmsgId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val updateType: Int = 0,
        @ProtoNumber(3) @JvmField val extMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class RedGrayTipsInfo(
        @ProtoNumber(1) @JvmField val optUint32ShowLastest: Int = 0,
        @ProtoNumber(2) @JvmField val senderUin: Long = 0L,
        @ProtoNumber(3) @JvmField val receiverUin: Long = 0L,
        @ProtoNumber(4) @JvmField val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(7) @JvmField val sint32Msgtype: Int = 0,
        @ProtoNumber(8) @JvmField val luckyFlag: Int = 0,
        @ProtoNumber(9) @JvmField val hideFlag: Int = 0,
        @ProtoNumber(10) @JvmField val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val icon: Int = 0,
        @ProtoNumber(12) @JvmField val luckyUin: Long = 0L,
        @ProtoNumber(13) @JvmField val time: Int = 0,
        @ProtoNumber(14) @JvmField val random: Int = 0,
        @ProtoNumber(15) @JvmField val broadcastRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(16) @JvmField val idiom: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(17) @JvmField val idiomSeq: Int = 0,
        @ProtoNumber(18) @JvmField val idiomAlpha: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(19) @JvmField val jumpurl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}
