/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import kotlin.jvm.JvmField

@Serializable
internal class GroupOpenSysMsg : ProtoBuf {
    @Serializable
    internal class LightApp(
        @ProtoNumber(1) @JvmField val app: String = "",
        @ProtoNumber(2) @JvmField val view: String = "",
        @ProtoNumber(3) @JvmField val desc: String = "",
        @ProtoNumber(4) @JvmField val prompt: String = "",
        @ProtoNumber(5) @JvmField val ver: String = "",
        @ProtoNumber(6) @JvmField val meta: String = "",
        @ProtoNumber(7) @JvmField val config: String = "",
        @ProtoNumber(8) @JvmField val source: Source? = null,
    ) : ProtoBuf

    @Serializable
    internal class RichMsg(
        @ProtoNumber(1) @JvmField val title: String = "",
        @ProtoNumber(2) @JvmField val desc: String = "",
        @ProtoNumber(3) @JvmField val brief: String = "",
        @ProtoNumber(4) @JvmField val cover: String = "",
        @ProtoNumber(5) @JvmField val url: String = "",
        @ProtoNumber(6) @JvmField val source: Source? = null,
    ) : ProtoBuf

    @Serializable
    internal class Sender(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val nick: String = "",
        @ProtoNumber(3) @JvmField val avatar: String = "",
        @ProtoNumber(4) @JvmField val url: String = "",
    ) : ProtoBuf

    @Serializable
    internal class Source(
        @ProtoNumber(1) @JvmField val name: String = "",
        @ProtoNumber(2) @JvmField val icon: String = "",
        @ProtoNumber(3) @JvmField val url: String = "",
    ) : ProtoBuf

    @Serializable
    internal class SysMsgBody(
        @ProtoNumber(1) @JvmField val groupId: Long = 0L,
        @ProtoNumber(2) @JvmField val appid: Long = 0L,
        @ProtoNumber(3) @JvmField val sender: Sender? = null,
        @ProtoNumber(4) @JvmField val msgType: Int = 0,
        @ProtoNumber(5) @JvmField val content: String = "",
        @ProtoNumber(6) @JvmField val richMsg: RichMsg? = null,
        @ProtoNumber(7) @JvmField val lightApp: LightApp? = null,
    ) : ProtoBuf
}

@Serializable
internal class TroopTips0x857 : ProtoBuf {
    @Serializable
    internal class AIOGrayTipsInfo(
        @ProtoNumber(1) @JvmField val optUint32ShowLastest: Int = 0,
        @ProtoNumber(2) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val optUint32Remind: Int = 0,
        @ProtoNumber(4) @JvmField val optBytesBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val receiverUin: Long = 0L,
        @ProtoNumber(6) @JvmField val reliaoAdminOpt: Int = 0,
        @ProtoNumber(7) @JvmField val robotGroupOpt: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class AIOTopTipsInfo(
        @ProtoNumber(1) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val optUint32Icon: Int = 0,
        @ProtoNumber(3) @JvmField val optEnumAction: Int /* enum */ = 1,
        @ProtoNumber(4) @JvmField val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val optBytesData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val optBytesDataI: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val optBytesDataA: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val optBytesDataP: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class FloatedTipsInfo(
        @ProtoNumber(1) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class GeneralGrayTipInfo(
        @ProtoNumber(1) @JvmField val busiType: Long = 0L,
        @ProtoNumber(2) @JvmField val busiId: Long = 0L,
        @ProtoNumber(3) @JvmField val ctrlFlag: Int = 0,
        @ProtoNumber(4) @JvmField val c2cType: Int = 0,
        @ProtoNumber(5) @JvmField val serviceType: Int = 0,
        @ProtoNumber(6) @JvmField val templId: Long = 0L,
        @ProtoNumber(7) @JvmField val msgTemplParam: List<TemplParam> = emptyList(),
        @ProtoNumber(8) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val tipsSeqId: Long = 0L,
        @ProtoNumber(100) @JvmField val pbReserv: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

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
        @ProtoNumber(9) @JvmField val action: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class GrayData(
        @ProtoNumber(1) @JvmField val allRead: Int = 0,
        @ProtoNumber(2) @JvmField val feedId: String = "",
    ) : ProtoBuf

    @Serializable
    internal class GroupAnnounceTBCInfo(
        @ProtoNumber(1) @JvmField val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val groupId: Long = 0L,
        @ProtoNumber(3) @JvmField val action: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class GroupAsyncNotify(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val msgSeq: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class GroupInfoChange(
        @ProtoNumber(1) @JvmField val groupHonorSwitch: Int = 0,
        @ProtoNumber(2) @JvmField val groupMemberLevelSwitch: Int = 0,
        @ProtoNumber(3) @JvmField val groupFlagext4: Int = 0,
        @ProtoNumber(4) @JvmField val appealDeadline: Int = 0,
        @ProtoNumber(5) @JvmField val groupFlag: Int = 0,
        @ProtoNumber(7) @JvmField val groupFlagext3: Int = 0,
        @ProtoNumber(8) @JvmField val groupClassExt: Int = 0,
        @ProtoNumber(9) @JvmField val groupInfoExtSeq: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class GroupNotifyInfo(
        @ProtoNumber(1) @JvmField val optUint32AutoPullFlag: Int = 0,
        @ProtoNumber(2) @JvmField val optBytesFeedsId: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class InstCtrl(
        @ProtoNumber(1) @JvmField val msgSendToInst: List<InstInfo> = emptyList(),
        @ProtoNumber(2) @JvmField val msgExcludeInst: List<InstInfo> = emptyList(),
        @ProtoNumber(3) @JvmField val msgFromInst: InstInfo? = null,
    ) : ProtoBuf

    @Serializable
    internal class InstInfo(
        @ProtoNumber(1) @JvmField val apppid: Int = 0,
        @ProtoNumber(2) @JvmField val instid: Int = 0,
        @ProtoNumber(3) @JvmField val platform: Int = 0,
        @ProtoNumber(4) @JvmField val openAppid: Int = 0,
        @ProtoNumber(5) @JvmField val productid: Int = 0,
        @ProtoNumber(6) @JvmField val ssoBid: Int = 0,
        @ProtoNumber(7) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val verMin: Int = 0,
        @ProtoNumber(9) @JvmField val verMax: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class LbsShareChangePushInfo(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val groupId: Long = 0L,
        @ProtoNumber(5) @JvmField val operUin: Long = 0L,
        @ProtoNumber(6) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val msgSeq: Long = 0L,
        @ProtoNumber(8) @JvmField val joinNums: Int = 0,
        @ProtoNumber(99) @JvmField val pushType: Int = 0,
        @ProtoNumber(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class LuckyBagNotify(
        @ProtoNumber(1) @JvmField val msgTips: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class MediaChangePushInfo(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val groupId: Long = 0L,
        @ProtoNumber(5) @JvmField val operUin: Long = 0L,
        @ProtoNumber(6) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val msgSeq: Long = 0L,
        @ProtoNumber(8) @JvmField val joinNums: Int = 0,
        @ProtoNumber(9) @JvmField val msgPerSetting: PersonalSetting? = null,
        @ProtoNumber(10) @JvmField val playMode: Int = 0,
        @ProtoNumber(11) @JvmField val isJoinWhenStart: Boolean = false,
        @ProtoNumber(99) @JvmField val mediaType: Int = 0,
        @ProtoNumber(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf {
        @Serializable
        internal class PersonalSetting(
            @ProtoNumber(1) @JvmField val themeId: Int = 0,
            @ProtoNumber(2) @JvmField val playerId: Int = 0,
            @ProtoNumber(3) @JvmField val fontId: Int = 0,
        ) : ProtoBuf
    }

    @Serializable
    internal class MessageBoxInfo(
        @ProtoNumber(1) @JvmField val optBytesContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val optBytesTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val optBytesButton: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class MessageRecallReminder(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val recalledMsgList: List<MessageMeta> = emptyList(),
        @ProtoNumber(4) @JvmField val reminderContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val groupType: Int = 0,
        @ProtoNumber(7) @JvmField val opType: Int = 0,
        @ProtoNumber(8) @JvmField val adminUin: Long = 0L,
        @ProtoNumber(9) @JvmField val msgWordingInfo: WithDrawWordingInfo? = null,
    ) : ProtoBuf {
        @Serializable
        internal class MessageMeta(
            @ProtoNumber(1) @JvmField val seq: Int = 0,
            @ProtoNumber(2) @JvmField val time: Int = 0,
            @ProtoNumber(3) @JvmField val msgRandom: Int = 0,
            @ProtoNumber(4) @JvmField val msgType: Int = 0,
            @ProtoNumber(5) @JvmField val msgFlag: Int = 0,
            @ProtoNumber(6) @JvmField val authorUin: Long = 0L,
            @ProtoNumber(7) @JvmField val isAnonyMsg: Int = 0,
        ) : ProtoBuf

        @Serializable
        internal class WithDrawWordingInfo(
            @ProtoNumber(1) @JvmField val int32ItemId: Int = 0,
            @ProtoNumber(2) @JvmField val itemName: String = "",
        ) : ProtoBuf
    }

    @Serializable
    internal class MiniAppNotify(
        @ProtoNumber(1) @JvmField val msg: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class NotifyMsgBody(
        @ProtoNumber(1) @JvmField val optEnumType: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val optUint64MsgTime: Long = 0L,
        @ProtoNumber(3) @JvmField val optUint64MsgExpires: Long = 0L,
        @ProtoNumber(4) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoNumber(5) @JvmField val optMsgGraytips: AIOGrayTipsInfo? = null,
        @ProtoNumber(6) @JvmField val optMsgMessagebox: MessageBoxInfo? = null,
        @ProtoNumber(7) @JvmField val optMsgFloatedtips: FloatedTipsInfo? = null,
        @ProtoNumber(8) @JvmField val optMsgToptips: AIOTopTipsInfo? = null,
        @ProtoNumber(9) @JvmField val optMsgRedtips: RedGrayTipsInfo? = null,
        @ProtoNumber(10) @JvmField val optMsgGroupNotify: GroupNotifyInfo? = null,
        @ProtoNumber(11) @JvmField val optMsgRecall: MessageRecallReminder? = null,
        @ProtoNumber(12) @JvmField val optMsgThemeNotify: ThemeStateNotify? = null,
        @ProtoNumber(13) @JvmField val serviceType: Int = 0,
        @ProtoNumber(14) @JvmField val optMsgObjmsgUpdate: NotifyObjmsgUpdate? = null,
        @ProtoNumber(15) @JvmField val optMsgWerewolfPush: WereWolfPush? = null,
        //@ProtoNumber(16) @JvmField val optStcmGameState: ApolloGameStatus.STCMGameMessage? = null,
        //@ProtoNumber(17) @JvmField val aplloMsgPush: ApolloPushMsgInfo.STPushMsgElem? = null,
        @ProtoNumber(18) @JvmField val optMsgGoldtips: GoldMsgTipsElem? = null,
        @ProtoNumber(20) @JvmField val optMsgMiniappNotify: MiniAppNotify? = null,
        @ProtoNumber(21) @JvmField val optUint64SenderUin: Long = 0L,
        @ProtoNumber(22) @JvmField val optMsgLuckybagNotify: LuckyBagNotify? = null,
        @ProtoNumber(23) @JvmField val optMsgTroopformtipsPush: TroopFormGrayTipsInfo? = null,
        @ProtoNumber(24) @JvmField val optMsgMediaPush: MediaChangePushInfo? = null,
        @ProtoNumber(26) @JvmField val optGeneralGrayTip: GeneralGrayTipInfo? = null,
        @ProtoNumber(27) @JvmField val optMsgVideoPush: VideoChangePushInfo? = null,
        @ProtoNumber(28) @JvmField val optLbsShareChangePlusInfo: LbsShareChangePushInfo? = null,
        @ProtoNumber(29) @JvmField val optMsgSingPush: SingChangePushInfo? = null,
        @ProtoNumber(30) @JvmField val optMsgGroupInfoChange: GroupInfoChange? = null,
        @ProtoNumber(31) @JvmField val optGroupAnnounceTbcInfo: GroupAnnounceTBCInfo? = null,
        @ProtoNumber(32) @JvmField val optQqVedioGamePushInfo: QQVedioGamePushInfo? = null,
        @ProtoNumber(33) @JvmField val optQqGroupDigestMsg: QQGroupDigestMsg? = null,
        @ProtoNumber(34) @JvmField val optStudyRoomMemberMsg: StudyRoomMemberChangePush? = null,
        @ProtoNumber(35) @JvmField val optQqLiveNotify: QQVaLiveNotifyMsg? = null,
        @ProtoNumber(36) @JvmField val optGroupAsyncNotidy: GroupAsyncNotify? = null,
        @ProtoNumber(37) @JvmField val optUint64GroupCurMsgSeq: Long = 0L,
        @ProtoNumber(38) @JvmField val optGroupDigestMsgSummary: QQGroupDigestMsgSummary? = null
    ) : ProtoBuf

    @Serializable
    internal class NotifyObjmsgUpdate(
        @ProtoNumber(1) @JvmField val objmsgId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val updateType: Int = 0,
        @ProtoNumber(3) @JvmField val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class QQGroupDigestMsg(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(3) @JvmField val msgRandom: Int = 0,
        @ProtoNumber(4) @JvmField val opType: Int = 0,
        @ProtoNumber(5) @JvmField val msgSender: Long = 0L,
        @ProtoNumber(6) @JvmField val digestOper: Long = 0L,
        @ProtoNumber(7) @JvmField val opTime: Int = 0,
        @ProtoNumber(8) @JvmField val lastestMsgSeq: Int = 0,
        @ProtoNumber(9) @JvmField val operNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val senderNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val extInfo: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class QQGroupDigestMsgSummary(
        @ProtoNumber(1) @JvmField val digestOper: Long = 0L,
        @ProtoNumber(2) @JvmField val opType: Int = 0,
        @ProtoNumber(3) @JvmField val opTime: Int = 0,
        @ProtoNumber(4) @JvmField val digestNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val succCnt: Int = 0,
        @ProtoNumber(6) @JvmField val summaryInfo: List<QQGroupDigestSummaryInfo> = emptyList(),
    ) : ProtoBuf

    @Serializable
    internal class QQGroupDigestSummaryInfo(
        @ProtoNumber(1) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(2) @JvmField val msgRandom: Int = 0,
        @ProtoNumber(3) @JvmField val errorCode: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class QQVaLiveNotifyMsg(
        @ProtoNumber(1) @JvmField val uid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val notifyType: Int = 0,
        @ProtoNumber(3) @JvmField val ext1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val ext2: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val ext3: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class QQVedioGamePushInfo(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(3) @JvmField val operUin: Long = 0L,
        @ProtoNumber(4) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY,
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
        @ProtoNumber(19) @JvmField val jumpurl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20) @JvmField val subchannel: Int = 0,
        @ProtoNumber(21) @JvmField val poemRule: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val uint64Memberuins: List<Long> = emptyList(),
        @ProtoNumber(3) @JvmField val optUint32Offline: Int = 0,
        @ProtoNumber(4) @JvmField val msgInstCtrl: InstCtrl? = null,
        @ProtoNumber(5) @JvmField val optBytesMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val optUint32BusiType: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val optUint64GroupCode: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class SingChangePushInfo(
        @ProtoNumber(1) @JvmField val seq: Long = 0L,
        @ProtoNumber(2) @JvmField val actionType: Int = 0,
        @ProtoNumber(3) @JvmField val groupId: Long = 0L,
        @ProtoNumber(4) @JvmField val operUin: Long = 0L,
        @ProtoNumber(5) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val joinNums: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class StudyRoomMemberChangePush(
        @ProtoNumber(1) @JvmField val memberCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TemplParam(
        @ProtoNumber(1) @JvmField val name: String = "",
        @ProtoNumber(2) @JvmField val value: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ThemeStateNotify(
        @ProtoNumber(1) @JvmField val state: Int = 0,
        @ProtoNumber(2) @JvmField val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val themeName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val actionUin: Long = 0L,
        @ProtoNumber(5) @JvmField val createUin: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class TroopFormGrayTipsInfo(
        @ProtoNumber(1) @JvmField val writerUin: Long = 0L,
        @ProtoNumber(2) @JvmField val creatorUin: Long = 0L,
        @ProtoNumber(3) @JvmField val richContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val creatorNick: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class VideoChangePushInfo(
        @ProtoNumber(1) @JvmField val seq: Long = 0L,
        @ProtoNumber(2) @JvmField val actionType: Int = 0,
        @ProtoNumber(3) @JvmField val groupId: Long = 0L,
        @ProtoNumber(4) @JvmField val operUin: Long = 0L,
        @ProtoNumber(5) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val joinNums: Int = 0,
        @ProtoNumber(7) @JvmField val joinState: Int = 0,
        @ProtoNumber(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class WereWolfPush(
        @ProtoNumber(1) @JvmField val pushType: Int = 0,
        @ProtoNumber(2) @JvmField val gameRoom: Long = 0L,
        @ProtoNumber(3) @JvmField val enumGameState: Int = 0,
        @ProtoNumber(4) @JvmField val gameRound: Int = 0,
        @ProtoNumber(5) @JvmField val roles: List<Role> = emptyList(),
        @ProtoNumber(6) @JvmField val speaker: Long = 0L,
        @ProtoNumber(7) @JvmField val judgeUin: Long = 0L,
        @ProtoNumber(8) @JvmField val judgeWords: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val enumOperation: Int = 0,
        @ProtoNumber(10) @JvmField val srcUser: Long = 0L,
        @ProtoNumber(11) @JvmField val dstUser: Long = 0L,
        @ProtoNumber(12) @JvmField val deadUsers: List<Long> = emptyList(),
        @ProtoNumber(13) @JvmField val gameResult: Int = 0,
        @ProtoNumber(14) @JvmField val timeoutSec: Int = 0,
        @ProtoNumber(15) @JvmField val killConfirmed: Int = 0,
        @ProtoNumber(16) @JvmField val judgeNickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(17) @JvmField val votedTieUsers: List<Long> = emptyList(),
    ) : ProtoBuf {
        @Serializable
        internal class GameRecord(
            @ProtoNumber(1) @JvmField val total: Int = 0,
            @ProtoNumber(2) @JvmField val win: Int = 0,
            @ProtoNumber(3) @JvmField val lose: Int = 0,
            @ProtoNumber(4) @JvmField val draw: Int = 0,
        ) : ProtoBuf

        @Serializable
        internal class Role(
            @ProtoNumber(1) @JvmField val uin: Long = 0L,
            @ProtoNumber(2) @JvmField val enumType: Int = 0,
            @ProtoNumber(3) @JvmField val enumState: Int = 0,
            @ProtoNumber(4) @JvmField val canSpeak: Int = 0,
            @ProtoNumber(5) @JvmField val canListen: Int = 0,
            @ProtoNumber(6) @JvmField val position: Int = 0,
            @ProtoNumber(7) @JvmField val canVote: Int = 0,
            @ProtoNumber(8) @JvmField val canVoted: Int = 0,
            @ProtoNumber(9) @JvmField val alreadyChecked: Int = 0,
            @ProtoNumber(10) @JvmField val alreadySaved: Int = 0,
            @ProtoNumber(11) @JvmField val alreadyPoisoned: Int = 0,
            @ProtoNumber(12) @JvmField val playerState: Int = 0,
            @ProtoNumber(13) @JvmField val enumDeadOp: Int = 0,
            @ProtoNumber(14) @JvmField val enumOperation: Int = 0,
            @ProtoNumber(15) @JvmField val dstUser: Long = 0L,
            @ProtoNumber(16) @JvmField val operationRound: Int = 0,
            @ProtoNumber(17) @JvmField val msgGameRecord: GameRecord? = null,
            @ProtoNumber(18) @JvmField val isWerewolf: Int = 0,
            @ProtoNumber(19) @JvmField val defendedUser: Long = 0L,
            @ProtoNumber(20) @JvmField val isSheriff: Int = 0,
        ) : ProtoBuf
    }
}