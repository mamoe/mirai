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

class GroupOpenSysMsg : ProtoBuf {
    @Serializable
    class LightApp(
        @SerialId(1) val app: String = "",
        @SerialId(2) val view: String = "",
        @SerialId(3) val desc: String = "",
        @SerialId(4) val prompt: String = "",
        @SerialId(5) val ver: String = "",
        @SerialId(6) val meta: String = "",
        @SerialId(7) val config: String = "",
        @SerialId(8) val source: Source? = null
    ) : ProtoBuf

    @Serializable
    class RichMsg(
        @SerialId(1) val title: String = "",
        @SerialId(2) val desc: String = "",
        @SerialId(3) val brief: String = "",
        @SerialId(4) val cover: String = "",
        @SerialId(5) val url: String = "",
        @SerialId(6) val source: Source? = null
    ) : ProtoBuf

    @Serializable
    class Sender(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val nick: String = "",
        @SerialId(3) val avatar: String = "",
        @SerialId(4) val url: String = ""
    ) : ProtoBuf

    @Serializable
    class Source(
        @SerialId(1) val name: String = "",
        @SerialId(2) val icon: String = "",
        @SerialId(3) val url: String = ""
    ) : ProtoBuf

    @Serializable
    class SysMsgBody(
        @SerialId(1) val groupId: Long = 0L,
        @SerialId(2) val appid: Long = 0L,
        @SerialId(3) val sender: Sender? = null,
        @SerialId(4) val msgType: Int = 0,
        @SerialId(5) val content: String = "",
        @SerialId(6) val richMsg: RichMsg? = null,
        @SerialId(7) val lightApp: LightApp? = null
    ) : ProtoBuf
}

@Serializable
class TroopTips0x857 : ProtoBuf {
    @Serializable
    class AIOGrayTipsInfo(
        @SerialId(1) val optUint32ShowLastest: Int = 0,
        @SerialId(2) val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val optUint32Remind: Int = 0,
        @SerialId(4) val optBytesBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val receiverUin: Long = 0L,
        @SerialId(6) val reliaoAdminOpt: Int = 0,
        @SerialId(7) val robotGroupOpt: Int = 0
    ) : ProtoBuf

    @Serializable
    class AIOTopTipsInfo(
        @SerialId(1) val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val optUint32Icon: Int = 0,
        @SerialId(3) val optEnumAction: Int /* enum */ = 1,
        @SerialId(4) val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val optBytesData: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val optBytesDataI: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val optBytesDataA: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val optBytesDataP: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class FloatedTipsInfo(
        @SerialId(1) val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GeneralGrayTipInfo(
        @SerialId(1) val busiType: Long = 0L,
        @SerialId(2) val busiId: Long = 0L,
        @SerialId(3) val ctrlFlag: Int = 0,
        @SerialId(4) val c2cType: Int = 0,
        @SerialId(5) val serviceType: Int = 0,
        @SerialId(6) val templId: Long = 0L,
        @SerialId(7) val msgTemplParam: List<TemplParam>? = null,
        @SerialId(8) val content: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val tipsSeqId: Long = 0L,
        @SerialId(100) val pbReserv: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

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
    class GroupInfoChange(
        @SerialId(1) val groupHonorSwitch: Int = 0
    ) : ProtoBuf

    @Serializable
    class GroupNotifyInfo(
        @SerialId(1) val optUint32AutoPullFlag: Int = 0,
        @SerialId(2) val optBytesFeedsId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class InstCtrl(
        @SerialId(1) val msgSendToInst: List<InstInfo>? = null,
        @SerialId(2) val msgExcludeInst: List<InstInfo>? = null,
        @SerialId(3) val msgFromInst: InstInfo? = null
    ) : ProtoBuf

    @Serializable
    class InstInfo(
        @SerialId(1) val apppid: Int = 0,
        @SerialId(2) val instid: Int = 0,
        @SerialId(3) val platform: Int = 0,
        @SerialId(4) val openAppid: Int = 0,
        @SerialId(5) val productid: Int = 0,
        @SerialId(6) val ssoBid: Int = 0,
        @SerialId(7) val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val verMin: Int = 0,
        @SerialId(9) val verMax: Int = 0
    ) : ProtoBuf

    @Serializable
    class LbsShareChangePushInfo(
        @SerialId(1) val msgType: Int = 0,
        @SerialId(2) val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val groupId: Long = 0L,
        @SerialId(5) val operUin: Long = 0L,
        @SerialId(6) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val msgSeq: Long = 0L,
        @SerialId(8) val joinNums: Int = 0,
        @SerialId(99) val pushType: Int = 0,
        @SerialId(100) val extInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class LuckyBagNotify(
        @SerialId(1) val msgTips: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MediaChangePushInfo(
        @SerialId(1) val msgType: Int = 0,
        @SerialId(2) val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val groupId: Long = 0L,
        @SerialId(5) val operUin: Long = 0L,
        @SerialId(6) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val msgSeq: Long = 0L,
        @SerialId(8) val joinNums: Int = 0,
        @SerialId(9) val msgPerSetting: PersonalSetting? = null,
        @SerialId(10) val playMode: Int = 0,
        @SerialId(99) val mediaType: Int = 0,
        @SerialId(100) val extInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        class PersonalSetting(
            @SerialId(1) val themeId: Int = 0,
            @SerialId(2) val playerId: Int = 0,
            @SerialId(3) val fontId: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    class MessageBoxInfo(
        @SerialId(1) val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val optBytesTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val optBytesButton: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MessageRecallReminder(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val recalledMsgList: List<MessageMeta> = listOf(),
        @SerialId(4) val reminderContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val userdef: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val groupType: Int = 0,
        @SerialId(7) val opType: Int = 0
    ) : ProtoBuf {
        @Serializable
        class MessageMeta(
            @SerialId(1) val seq: Int = 0,
            @SerialId(2) val time: Int = 0,
            @SerialId(3) val msgRandom: Int = 0,
            @SerialId(4) val msgType: Int = 0,
            @SerialId(5) val msgFlag: Int = 0,
            @SerialId(6) val authorUin: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    class MiniAppNotify(
        @SerialId(1) val msg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class NotifyMsgBody(
        @SerialId(1) val optEnumType: Int /* enum */ = 1,
        @SerialId(2) val optUint64MsgTime: Long = 0L,
        @SerialId(3) val optUint64MsgExpires: Long = 0L,
        @SerialId(4) val optUint64GroupCode: Long = 0L,
        @SerialId(5) val optMsgGraytips: AIOGrayTipsInfo? = null,
        @SerialId(6) val optMsgMessagebox: MessageBoxInfo? = null,
        @SerialId(7) val optMsgFloatedtips: FloatedTipsInfo? = null,
        @SerialId(8) val optMsgToptips: AIOTopTipsInfo? = null,
        @SerialId(9) val optMsgRedtips: RedGrayTipsInfo? = null,
        @SerialId(10) val optMsgGroupNotify: GroupNotifyInfo? = null,
        @SerialId(11) val optMsgRecall: MessageRecallReminder? = null,
        @SerialId(12) val optMsgThemeNotify: ThemeStateNotify? = null,
        @SerialId(13) val serviceType: Int = 0,
        @SerialId(14) val optMsgObjmsgUpdate: NotifyObjmsgUpdate? = null,
        @SerialId(15) val optMsgWerewolfPush: WereWolfPush? = null,
        // @SerialId(16) val optStcmGameState: ApolloGameStatus.STCMGameMessage? = null,
        // @SerialId(17) val aplloMsgPush: ApolloPushMsgInfo.STPushMsgElem? = null,
        @SerialId(18) val optMsgGoldtips: GoldMsgTipsElem? = null,
        @SerialId(20) val optMsgMiniappNotify: MiniAppNotify? = null,
        @SerialId(21) val optUint64SenderUin: Long = 0L,
        @SerialId(22) val optMsgLuckybagNotify: LuckyBagNotify? = null,
        @SerialId(23) val optMsgTroopformtipsPush: TroopFormGrayTipsInfo? = null,
        @SerialId(24) val optMsgMediaPush: MediaChangePushInfo? = null,
        @SerialId(26) val optGeneralGrayTip: GeneralGrayTipInfo? = null,
        @SerialId(27) val optMsgVideoPush: VideoChangePushInfo? = null,
        @SerialId(28) val optLbsShareChangePlusInfo: LbsShareChangePushInfo? = null,
        @SerialId(29) val optMsgSingPush: SingChangePushInfo? = null,
        @SerialId(30) val optMsgGroupInfoChange: GroupInfoChange? = null
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

    @Serializable
    class ReqBody(
        @SerialId(1) val optUint64GroupCode: Long = 0L,
        @SerialId(2) val uint64Memberuins: List<Long>? = null,
        @SerialId(3) val optUint32Offline: Int = 0,
        @SerialId(4) val msgInstCtrl: InstCtrl? = null,
        @SerialId(5) val optBytesMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val optUint32BusiType: Int = 0
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @SerialId(1) val optUint64GroupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    class SingChangePushInfo(
        @SerialId(1) val seq: Long = 0L,
        @SerialId(2) val actionType: Int = 0,
        @SerialId(3) val groupId: Long = 0L,
        @SerialId(4) val operUin: Long = 0L,
        @SerialId(5) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val joinNums: Int = 0
    ) : ProtoBuf

    @Serializable
    class TemplParam(
        @SerialId(1) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ThemeStateNotify(
        @SerialId(1) val state: Int = 0,
        @SerialId(2) val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val themeName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val actionUin: Long = 0L,
        @SerialId(5) val createUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class TroopFormGrayTipsInfo(
        @SerialId(1) val writerUin: Long = 0L,
        @SerialId(2) val creatorUin: Long = 0L,
        @SerialId(3) val richContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val creatorNick: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class VideoChangePushInfo(
        @SerialId(1) val seq: Long = 0L,
        @SerialId(2) val actionType: Int = 0,
        @SerialId(3) val groupId: Long = 0L,
        @SerialId(4) val operUin: Long = 0L,
        @SerialId(5) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val joinNums: Int = 0,
        @SerialId(100) val extInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class WereWolfPush(
        @SerialId(1) val pushType: Int = 0,
        @SerialId(2) val gameRoom: Long = 0L,
        @SerialId(3) val enumGameState: Int = 0,
        @SerialId(4) val gameRound: Int = 0,
        @SerialId(5) val roles: List<Role>? = null,
        @SerialId(6) val speaker: Long = 0L,
        @SerialId(7) val judgeUin: Long = 0L,
        @SerialId(8) val judgeWords: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val enumOperation: Int = 0,
        @SerialId(10) val srcUser: Long = 0L,
        @SerialId(11) val dstUser: Long = 0L,
        @SerialId(12) val deadUsers: List<Long>? = null,
        @SerialId(13) val gameResult: Int = 0,
        @SerialId(14) val timeoutSec: Int = 0,
        @SerialId(15) val killConfirmed: Int = 0,
        @SerialId(16) val judgeNickname: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(17) val votedTieUsers: List<Long>? = null
    ) : ProtoBuf {
        @Serializable
        class GameRecord(
            @SerialId(1) val total: Int = 0,
            @SerialId(2) val win: Int = 0,
            @SerialId(3) val lose: Int = 0,
            @SerialId(4) val draw: Int = 0
        ) : ProtoBuf

        @Serializable
        class Role(
            @SerialId(1) val uin: Long = 0L,
            @SerialId(2) val enumType: Int = 0,
            @SerialId(3) val enumState: Int = 0,
            @SerialId(4) val canSpeak: Int = 0,
            @SerialId(5) val canListen: Int = 0,
            @SerialId(6) val position: Int = 0,
            @SerialId(7) val canVote: Int = 0,
            @SerialId(8) val canVoted: Int = 0,
            @SerialId(9) val alreadyChecked: Int = 0,
            @SerialId(10) val alreadySaved: Int = 0,
            @SerialId(11) val alreadyPoisoned: Int = 0,
            @SerialId(12) val playerState: Int = 0,
            @SerialId(13) val enumDeadOp: Int = 0,
            @SerialId(14) val enumOperation: Int = 0,
            @SerialId(15) val dstUser: Long = 0L,
            @SerialId(16) val operationRound: Int = 0,
            @SerialId(17) val msgGameRecord: GameRecord? = null,
            @SerialId(18) val isWerewolf: Int = 0,
            @SerialId(19) val defendedUser: Long = 0L,
            @SerialId(20) val isSheriff: Int = 0
        ) : ProtoBuf
    }
}