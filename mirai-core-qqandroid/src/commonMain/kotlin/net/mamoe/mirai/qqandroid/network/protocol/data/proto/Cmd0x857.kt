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

class GroupOpenSysMsg : ProtoBuf {
    @Serializable
    internal class LightApp(
        @ProtoId(1) @JvmField val app: String = "",
        @ProtoId(2) @JvmField val view: String = "",
        @ProtoId(3) @JvmField val desc: String = "",
        @ProtoId(4) @JvmField val prompt: String = "",
        @ProtoId(5) @JvmField val ver: String = "",
        @ProtoId(6) @JvmField val meta: String = "",
        @ProtoId(7) @JvmField val config: String = "",
        @ProtoId(8) @JvmField val source: Source? = null
    ) : ProtoBuf

    @Serializable
    internal class RichMsg(
        @ProtoId(1) @JvmField val title: String = "",
        @ProtoId(2) @JvmField val desc: String = "",
        @ProtoId(3) @JvmField val brief: String = "",
        @ProtoId(4) @JvmField val cover: String = "",
        @ProtoId(5) @JvmField val url: String = "",
        @ProtoId(6) @JvmField val source: Source? = null
    ) : ProtoBuf

    @Serializable
    internal class Sender(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val nick: String = "",
        @ProtoId(3) @JvmField val avatar: String = "",
        @ProtoId(4) @JvmField val url: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Source(
        @ProtoId(1) @JvmField val name: String = "",
        @ProtoId(2) @JvmField val icon: String = "",
        @ProtoId(3) @JvmField val url: String = ""
    ) : ProtoBuf

    @Serializable
    internal class SysMsgBody(
        @ProtoId(1) @JvmField val groupId: Long = 0L,
        @ProtoId(2) @JvmField val appid: Long = 0L,
        @ProtoId(3) @JvmField val sender: Sender? = null,
        @ProtoId(4) @JvmField val msgType: Int = 0,
        @ProtoId(5) @JvmField val content: String = "",
        @ProtoId(6) @JvmField val richMsg: RichMsg? = null,
        @ProtoId(7) @JvmField val lightApp: LightApp? = null
    ) : ProtoBuf
}

@Serializable
internal class TroopTips0x857 : ProtoBuf {
    @Serializable
    internal class AIOGrayTipsInfo(
        @ProtoId(1) @JvmField val optUint32ShowLastest: Int = 0,
        @ProtoId(2) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val optUint32Remind: Int = 0,
        @ProtoId(4) @JvmField val optBytesBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val receiverUin: Long = 0L,
        @ProtoId(6) @JvmField val reliaoAdminOpt: Int = 0,
        @ProtoId(7) @JvmField val robotGroupOpt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AIOTopTipsInfo(
        @ProtoId(1) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val optUint32Icon: Int = 0,
        @ProtoId(3) @JvmField val optEnumAction: Int /* enum */ = 1,
        @ProtoId(4) @JvmField val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val optBytesData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val optBytesDataI: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val optBytesDataA: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val optBytesDataP: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class FloatedTipsInfo(
        @ProtoId(1) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GeneralGrayTipInfo(
        @ProtoId(1) @JvmField val busiType: Long = 0L,
        @ProtoId(2) @JvmField val busiId: Long = 0L,
        @ProtoId(3) @JvmField val ctrlFlag: Int = 0,
        @ProtoId(4) @JvmField val c2cType: Int = 0,
        @ProtoId(5) @JvmField val serviceType: Int = 0,
        @ProtoId(6) @JvmField val templId: Long = 0L,
        @ProtoId(7) @JvmField val msgTemplParam: List<TemplParam>? = null,
        @ProtoId(8) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val tipsSeqId: Long = 0L,
        @ProtoId(100) @JvmField val pbReserv: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

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
    internal class GroupInfoChange(
        @ProtoId(1) @JvmField val groupHonorSwitch: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupNotifyInfo(
        @ProtoId(1) @JvmField val optUint32AutoPullFlag: Int = 0,
        @ProtoId(2) @JvmField val optBytesFeedsId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class InstCtrl(
        @ProtoId(1) @JvmField val msgSendToInst: List<InstInfo>? = null,
        @ProtoId(2) @JvmField val msgExcludeInst: List<InstInfo>? = null,
        @ProtoId(3) @JvmField val msgFromInst: InstInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class InstInfo(
        @ProtoId(1) @JvmField val apppid: Int = 0,
        @ProtoId(2) @JvmField val instid: Int = 0,
        @ProtoId(3) @JvmField val platform: Int = 0,
        @ProtoId(4) @JvmField val openAppid: Int = 0,
        @ProtoId(5) @JvmField val productid: Int = 0,
        @ProtoId(6) @JvmField val ssoBid: Int = 0,
        @ProtoId(7) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val verMin: Int = 0,
        @ProtoId(9) @JvmField val verMax: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class LbsShareChangePushInfo(
        @ProtoId(1) @JvmField val msgType: Int = 0,
        @ProtoId(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val groupId: Long = 0L,
        @ProtoId(5) @JvmField val operUin: Long = 0L,
        @ProtoId(6) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val msgSeq: Long = 0L,
        @ProtoId(8) @JvmField val joinNums: Int = 0,
        @ProtoId(99) @JvmField val pushType: Int = 0,
        @ProtoId(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LuckyBagNotify(
        @ProtoId(1) @JvmField val msgTips: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MediaChangePushInfo(
        @ProtoId(1) @JvmField val msgType: Int = 0,
        @ProtoId(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val groupId: Long = 0L,
        @ProtoId(5) @JvmField val operUin: Long = 0L,
        @ProtoId(6) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val msgSeq: Long = 0L,
        @ProtoId(8) @JvmField val joinNums: Int = 0,
        @ProtoId(9) @JvmField val msgPerSetting: PersonalSetting? = null,
        @ProtoId(10) @JvmField val playMode: Int = 0,
        @ProtoId(99) @JvmField val mediaType: Int = 0,
        @ProtoId(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class PersonalSetting(
            @ProtoId(1) @JvmField val themeId: Int = 0,
            @ProtoId(2) @JvmField val playerId: Int = 0,
            @ProtoId(3) @JvmField val fontId: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class MessageBoxInfo(
        @ProtoId(1) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val optBytesTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val optBytesButton: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MessageRecallReminder(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val recalledMsgList: List<MessageMeta> = listOf(),
        @ProtoId(4) @JvmField val reminderContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val groupType: Int = 0,
        @ProtoId(7) @JvmField val opType: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MessageMeta(
            @ProtoId(1) @JvmField val seq: Int = 0,
            @ProtoId(2) @JvmField val time: Int = 0,
            @ProtoId(3) @JvmField val msgRandom: Int = 0,
            @ProtoId(4) @JvmField val msgType: Int = 0,
            @ProtoId(5) @JvmField val msgFlag: Int = 0,
            @ProtoId(6) @JvmField val authorUin: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class MiniAppNotify(
        @ProtoId(1) @JvmField val msg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NotifyMsgBody(
        @ProtoId(1) @JvmField val optEnumType: Int /* enum */ = 1,
        @ProtoId(2) @JvmField val optUint64MsgTime: Long = 0L,
        @ProtoId(3) @JvmField val optUint64MsgExpires: Long = 0L,
        @ProtoId(4) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoId(5) @JvmField val optMsgGraytips: AIOGrayTipsInfo? = null,
        @ProtoId(6) @JvmField val optMsgMessagebox: MessageBoxInfo? = null,
        @ProtoId(7) @JvmField val optMsgFloatedtips: FloatedTipsInfo? = null,
        @ProtoId(8) @JvmField val optMsgToptips: AIOTopTipsInfo? = null,
        @ProtoId(9) @JvmField val optMsgRedtips: RedGrayTipsInfo? = null,
        @ProtoId(10) @JvmField val optMsgGroupNotify: GroupNotifyInfo? = null,
        @ProtoId(11) @JvmField val optMsgRecall: MessageRecallReminder? = null,
        @ProtoId(12) @JvmField val optMsgThemeNotify: ThemeStateNotify? = null,
        @ProtoId(13) @JvmField val serviceType: Int = 0,
        @ProtoId(14) @JvmField val optMsgObjmsgUpdate: NotifyObjmsgUpdate? = null,
        @ProtoId(15) @JvmField val optMsgWerewolfPush: WereWolfPush? = null,
        // @SerialId(16) @JvmField val optStcmGameState: ApolloGameStatus.STCMGameMessage? = null,
        // @SerialId(17) @JvmField val aplloMsgPush: ApolloPushMsgInfo.STPushMsgElem? = null,
        @ProtoId(18) @JvmField val optMsgGoldtips: GoldMsgTipsElem? = null,
        @ProtoId(20) @JvmField val optMsgMiniappNotify: MiniAppNotify? = null,
        @ProtoId(21) @JvmField val optUint64SenderUin: Long = 0L,
        @ProtoId(22) @JvmField val optMsgLuckybagNotify: LuckyBagNotify? = null,
        @ProtoId(23) @JvmField val optMsgTroopformtipsPush: TroopFormGrayTipsInfo? = null,
        @ProtoId(24) @JvmField val optMsgMediaPush: MediaChangePushInfo? = null,
        @ProtoId(26) @JvmField val optGeneralGrayTip: GeneralGrayTipInfo? = null,
        @ProtoId(27) @JvmField val optMsgVideoPush: VideoChangePushInfo? = null,
        @ProtoId(28) @JvmField val optLbsShareChangePlusInfo: LbsShareChangePushInfo? = null,
        @ProtoId(29) @JvmField val optMsgSingPush: SingChangePushInfo? = null,
        @ProtoId(30) @JvmField val optMsgGroupInfoChange: GroupInfoChange? = null
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

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoId(2) @JvmField val uint64Memberuins: List<Long>? = null,
        @ProtoId(3) @JvmField val optUint32Offline: Int = 0,
        @ProtoId(4) @JvmField val msgInstCtrl: InstCtrl? = null,
        @ProtoId(5) @JvmField val optBytesMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val optUint32BusiType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val optUint64GroupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class SingChangePushInfo(
        @ProtoId(1) @JvmField val seq: Long = 0L,
        @ProtoId(2) @JvmField val actionType: Int = 0,
        @ProtoId(3) @JvmField val groupId: Long = 0L,
        @ProtoId(4) @JvmField val operUin: Long = 0L,
        @ProtoId(5) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val joinNums: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TemplParam(
        @ProtoId(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ThemeStateNotify(
        @ProtoId(1) @JvmField val state: Int = 0,
        @ProtoId(2) @JvmField val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val themeName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val actionUin: Long = 0L,
        @ProtoId(5) @JvmField val createUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TroopFormGrayTipsInfo(
        @ProtoId(1) @JvmField val writerUin: Long = 0L,
        @ProtoId(2) @JvmField val creatorUin: Long = 0L,
        @ProtoId(3) @JvmField val richContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val creatorNick: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class VideoChangePushInfo(
        @ProtoId(1) @JvmField val seq: Long = 0L,
        @ProtoId(2) @JvmField val actionType: Int = 0,
        @ProtoId(3) @JvmField val groupId: Long = 0L,
        @ProtoId(4) @JvmField val operUin: Long = 0L,
        @ProtoId(5) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val joinNums: Int = 0,
        @ProtoId(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class WereWolfPush(
        @ProtoId(1) @JvmField val pushType: Int = 0,
        @ProtoId(2) @JvmField val gameRoom: Long = 0L,
        @ProtoId(3) @JvmField val enumGameState: Int = 0,
        @ProtoId(4) @JvmField val gameRound: Int = 0,
        @ProtoId(5) @JvmField val roles: List<Role>? = null,
        @ProtoId(6) @JvmField val speaker: Long = 0L,
        @ProtoId(7) @JvmField val judgeUin: Long = 0L,
        @ProtoId(8) @JvmField val judgeWords: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val enumOperation: Int = 0,
        @ProtoId(10) @JvmField val srcUser: Long = 0L,
        @ProtoId(11) @JvmField val dstUser: Long = 0L,
        @ProtoId(12) @JvmField val deadUsers: List<Long>? = null,
        @ProtoId(13) @JvmField val gameResult: Int = 0,
        @ProtoId(14) @JvmField val timeoutSec: Int = 0,
        @ProtoId(15) @JvmField val killConfirmed: Int = 0,
        @ProtoId(16) @JvmField val judgeNickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(17) @JvmField val votedTieUsers: List<Long>? = null
    ) : ProtoBuf {
        @Serializable
        internal class GameRecord(
            @ProtoId(1) @JvmField val total: Int = 0,
            @ProtoId(2) @JvmField val win: Int = 0,
            @ProtoId(3) @JvmField val lose: Int = 0,
            @ProtoId(4) @JvmField val draw: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Role(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val enumType: Int = 0,
            @ProtoId(3) @JvmField val enumState: Int = 0,
            @ProtoId(4) @JvmField val canSpeak: Int = 0,
            @ProtoId(5) @JvmField val canListen: Int = 0,
            @ProtoId(6) @JvmField val position: Int = 0,
            @ProtoId(7) @JvmField val canVote: Int = 0,
            @ProtoId(8) @JvmField val canVoted: Int = 0,
            @ProtoId(9) @JvmField val alreadyChecked: Int = 0,
            @ProtoId(10) @JvmField val alreadySaved: Int = 0,
            @ProtoId(11) @JvmField val alreadyPoisoned: Int = 0,
            @ProtoId(12) @JvmField val playerState: Int = 0,
            @ProtoId(13) @JvmField val enumDeadOp: Int = 0,
            @ProtoId(14) @JvmField val enumOperation: Int = 0,
            @ProtoId(15) @JvmField val dstUser: Long = 0L,
            @ProtoId(16) @JvmField val operationRound: Int = 0,
            @ProtoId(17) @JvmField val msgGameRecord: GameRecord? = null,
            @ProtoId(18) @JvmField val isWerewolf: Int = 0,
            @ProtoId(19) @JvmField val defendedUser: Long = 0L,
            @ProtoId(20) @JvmField val isSheriff: Int = 0
        ) : ProtoBuf
    }
}