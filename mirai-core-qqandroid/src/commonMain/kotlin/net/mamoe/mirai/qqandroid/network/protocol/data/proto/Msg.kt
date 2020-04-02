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
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray

@Serializable
internal class ImCommon : ProtoBuf {
    @Serializable
    internal class GroupInfo(
        @ProtoId(1) val groupId: Long = 0L,
        @ProtoId(2) val groupType: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class Signature(
        @ProtoId(1) val keyType: Int = 0,
        @ProtoId(2) val sessionAppId: Int = 0,
        @ProtoId(3) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Token(
        @ProtoId(1) val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val c2cType: Int = 0,
        @ProtoId(3) val serviceType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class User(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val appId: Int = 0,
        @ProtoId(3) val instanceId: Int = 0,
        @ProtoId(4) val appType: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(5) val clientIp: Int = 0,
        @ProtoId(6) val version: Int = 0,
        @ProtoId(7) val phoneNumber: String = "",
        @ProtoId(8) val platformId: Int = 0,
        @ProtoId(9) val language: Int = 0,
        @ProtoId(10) val equipKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class ImImagent : ProtoBuf {
    @Serializable
    internal class ImAgentHead(
        @ProtoId(1) val command: Int /* enum */ = 1,
        @ProtoId(2) val seq: Int = 0,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val err: String = "",
        @ProtoId(5) val echoBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val reqUser: ImCommon.User? = null,
        @ProtoId(7) val reqInfo: Requestinfo? = null,
        @ProtoId(8) val signature: Signature? = null,
        @ProtoId(9) val subCmd: Int = 0,
        @ProtoId(10) val serverIp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ImAgentPackage(
        @ProtoId(1) val head: ImAgentHead? = null,
        @ProtoId(11) val msgSendReq: ImMsg.MsgSendReq? = null,
        @ProtoId(12) val msgSendResp: ImMsg.MsgSendResp? = null
    ) : ProtoBuf

    @Serializable
    internal class Requestinfo(
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(1) val reqIp: Int = 0,
        @ProtoId(2) val reqPort: Int = 0,
        @ProtoId(3) val reqFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Signature(
        @ProtoId(1) val keyType: Int = 0,
        @ProtoId(2) val sessionAppId: Int = 0,
        @ProtoId(3) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class ImMsg : ProtoBuf {
    @Serializable
    internal class C2C(
        @ProtoId(1) val sender: ImCommon.User? = null,
        @ProtoId(2) val receiver: ImCommon.User? = null,
        @ProtoId(3) val c2cRelation: C2CRelation? = null
    ) : ProtoBuf

    @Serializable
    internal class C2CRelation(
        @ProtoId(1) val c2cType: Int /* enum */ = 0,
        @ProtoId(2) val groupInfo: ImCommon.GroupInfo? = null,
        @ProtoId(3) val token: ImCommon.Token? = null
    ) : ProtoBuf

    @Serializable
    internal class ContentHead(
        @ProtoId(1) val pkgNum: Int = 1,
        @ProtoId(2) val pkgIndex: Int = 0,
        @ProtoId(3) val seq: Int = 0,
        @ProtoId(4) val dateTime: Int = 0,
        @ProtoId(5) val msgType: Int = 0,
        @ProtoId(6) val divSeq: Int = 0,
        @ProtoId(7) val msgdbUin: Long = 0L,
        @ProtoId(8) val msgdbSeq: Int = 0,
        @ProtoId(9) val wordMsgSeq: Int = 0,
        @ProtoId(10) val msgRand: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Group(
        @ProtoId(1) val sender: ImCommon.User? = null,
        @ProtoId(2) val receiver: ImCommon.User? = null,
        @ProtoId(3) val groupInfo: ImCommon.GroupInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class Msg(
        @ProtoId(1) val head: MsgHead? = null,
        @ProtoId(2) val body: ImMsgBody.MsgBody? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgHead(
        @ProtoId(1) val routingHead: RoutingHead? = null,
        @ProtoId(2) val contentHead: ContentHead? = null,
        @ProtoId(3) val gbkTmpMsgBody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgSendReq(
        @ProtoId(1) val msg: Msg? = null,
        @ProtoId(2) val buMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgTailId: Int = 0,
        @ProtoId(4) val connMsgFlag: Int = 0,
        @ProtoId(5) val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgSendResp

    @Serializable
    internal class RoutingHead(
        @ProtoId(1) val c2c: C2C? = null,
        @ProtoId(2) val group: Group? = null
    ) : ProtoBuf
}

@Serializable
internal class ImMsgBody : ProtoBuf {
    @Serializable
    internal class AnonymousGroupMsg(
        @ProtoId(1) val flags: Int = 0,
        @ProtoId(2) val anonId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val anonNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val headPortrait: Int = 0,
        @ProtoId(5) val expireTime: Int = 0,
        @ProtoId(6) val bubbleId: Int = 0,
        @ProtoId(7) val rankColor: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ApolloActMsg(
        @ProtoId(1) val actionId: Int = 0,
        @ProtoId(2) val actionName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val actionText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val flag: Int = 0,
        @ProtoId(5) val peerUin: Int = 0,
        @ProtoId(6) val senderTs: Int = 0,
        @ProtoId(7) val peerTs: Int = 0,
        @ProtoId(8) val int32SenderStatus: Int = 0,
        @ProtoId(9) val int32PeerStatus: Int = 0,
        @ProtoId(10) val diytextId: Int = 0,
        @ProtoId(11) val diytextContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val inputText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ArkAppElem(
        @ProtoId(1) val appName: String = "",
        @ProtoId(2) val minVersion: String = "",
        @ProtoId(3) val xmlTemplate: String = "",
        @ProtoId(4) val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Attr(
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(1) val codePage: Int = -1,
        @ProtoId(2) val time: Int = 1,
        @ProtoId(3) val random: Int = 0,
        @ProtoId(4) val color: Int = 0,
        @ProtoId(5) val size: Int = 10,
        @ProtoId(6) val effect: Int = 7,
        @ProtoId(7) val charSet: Int = 78,
        @ProtoId(8) val pitchAndFamily: Int = 90,
        @ProtoId(9) val fontName: String = "Times New Roman",
        @ProtoId(10) val reserveData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BitAppMsg(
        @ProtoId(1) val buf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BlessingMessage(
        @ProtoId(1) val msgType: Int = 0,
        @ProtoId(2) val exFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommonElem(
        @ProtoId(1) val serviceType: Int = 0,
        @ProtoId(2) val pbElem: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val businessType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ConferenceTipsInfo(
        @ProtoId(1) val sessionType: Int = 0,
        @ProtoId(2) val sessionUin: Long = 0L,
        @ProtoId(3) val text: String = ""
    ) : ProtoBuf

    @Serializable
    internal class CrmElem(
        @ProtoId(1) val crmBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val qidianFlag: Int = 0,
        @ProtoId(4) val pushFlag: Int = 0,
        @ProtoId(5) val countFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CustomElem(
        @ProtoId(1) val desc: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val data: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val enumType: Int /* enum */ = 1,
        @ProtoId(4) val ext: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val sound: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CustomFace(
        @ProtoId(1) val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val filePath: String = "",
        @ProtoId(3) val shortcut: String = "",
        @ProtoId(4) val buffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val oldData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val fileId: Int = 0,
        @ProtoId(8) val serverIp: Int = 0,
        @ProtoId(9) val serverPort: Int = 0,
        @ProtoId(10) val fileType: Int = 0,
        @ProtoId(11) val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val useful: Int = 0,
        @ProtoId(13) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val thumbUrl: String = "",
        @ProtoId(15) val bigUrl: String = "",
        @ProtoId(16) val origUrl: String = "",
        @ProtoId(17) val bizType: Int = 0,
        @ProtoId(18) val repeatIndex: Int = 0,
        @ProtoId(19) val repeatImage: Int = 0,
        @ProtoId(20) val imageType: Int = 0,
        @ProtoId(21) val index: Int = 0,
        @ProtoId(22) val width: Int = 0,
        @ProtoId(23) val height: Int = 0,
        @ProtoId(24) val source: Int = 0,
        @ProtoId(25) val size: Int = 0,
        @ProtoId(26) val origin: Int = 0,
        @ProtoId(27) val thumbWidth: Int = 0,
        @ProtoId(28) val thumbHeight: Int = 0,
        @ProtoId(29) val showLen: Int = 0,
        @ProtoId(30) val downloadLen: Int = 0,
        @ProtoId(31) val _400Url: String = "",
        @ProtoId(32) val _400Width: Int = 0,
        @ProtoId(33) val _400Height: Int = 0,
        @ProtoId(34) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class DeliverGiftMsg(
        @ProtoId(1) val grayTipContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val animationPackageId: Int = 0,
        @ProtoId(3) val animationPackageUrlA: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val animationPackageUrlI: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val remindBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val giftId: Int = 0,
        @ProtoId(7) val giftCount: Int = 0,
        @ProtoId(8) val animationBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val senderUin: Long = 0L,
        @ProtoId(10) val receiverUin: Long = 0L,
        @ProtoId(11) val stmessageTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val stmessageSubtitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val stmessageMessage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val stmessageGiftpicid: Int = 0,
        @ProtoId(15) val stmessageComefrom: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(16) val stmessageExflag: Int = 0,
        @ProtoId(17) val toAllGiftId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(18) val comefromLink: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20) val receiverName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21) val receiverPic: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(22) val stmessageGifturl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class EIMInfo(
        @ProtoId(1) val rootId: Long = 0L,
        @ProtoId(2) val flag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Elem(
        @ProtoId(1) val text: Text? = null,
        @ProtoId(2) val face: Face? = null,
        @ProtoId(3) val onlineImage: OnlineImage? = null,
        @ProtoId(4) val notOnlineImage: NotOnlineImage? = null,
        @ProtoId(5) val transElemInfo: TransElem? = null,
        @ProtoId(6) val marketFace: MarketFace? = null,
        @ProtoId(7) val elemFlags: ElemFlags? = null,
        @ProtoId(8) val customFace: CustomFace? = null,
        @ProtoId(9) val elemFlags2: ElemFlags2? = null,
        @ProtoId(10) val funFace: FunFace? = null,
        @ProtoId(11) val secretFile: SecretFileMsg? = null,
        @ProtoId(12) val richMsg: RichMsg? = null,
        @ProtoId(13) val groupFile: GroupFile? = null,
        @ProtoId(14) val pubGroup: PubGroup? = null,
        @ProtoId(15) val marketTrans: MarketTrans? = null,
        @ProtoId(16) val extraInfo: ExtraInfo? = null,
        @ProtoId(17) val shakeWindow: ShakeWindow? = null,
        @ProtoId(18) val pubAccount: PubAccount? = null,
        @ProtoId(19) val videoFile: VideoFile? = null,
        @ProtoId(20) val tipsInfo: TipsInfo? = null,
        @ProtoId(21) val anonGroupMsg: AnonymousGroupMsg? = null,
        @ProtoId(22) val qqLiveOld: QQLiveOld? = null,
        @ProtoId(23) val lifeOnline: LifeOnlineAccount? = null,
        @ProtoId(24) val qqwalletMsg: QQWalletMsg? = null,
        @ProtoId(25) val crmElem: CrmElem? = null,
        @ProtoId(26) val conferenceTipsInfo: ConferenceTipsInfo? = null,
        @ProtoId(27) val redbagInfo: RedBagInfo? = null,
        @ProtoId(28) val lowVersionTips: LowVersionTips? = null,
        @ProtoId(29) val bankcodeCtrlInfo: ByteArray? = null,
        @ProtoId(30) val nearByMsg: NearByMessageType? = null,
        @ProtoId(31) val customElem: CustomElem? = null,
        @ProtoId(32) val locationInfo: LocationInfo? = null,
        @ProtoId(33) val pubAccInfo: PubAccInfo? = null,
        @ProtoId(34) val smallEmoji: SmallEmoji? = null,
        @ProtoId(35) val fsjMsgElem: FSJMessageElem? = null,
        @ProtoId(36) val arkApp: ArkAppElem? = null,
        @ProtoId(37) val generalFlags: GeneralFlags? = null,
        @ProtoId(38) val hcFlashPic: CustomFace? = null,
        @ProtoId(39) val deliverGiftMsg: DeliverGiftMsg? = null,
        @ProtoId(40) val bitappMsg: BitAppMsg? = null,
        @ProtoId(41) val openQqData: OpenQQData? = null,
        @ProtoId(42) val apolloMsg: ApolloActMsg? = null,
        @ProtoId(43) val groupPubAccInfo: GroupPubAccountInfo? = null,
        @ProtoId(44) val blessMsg: BlessingMessage? = null,
        @ProtoId(45) val srcMsg: SourceMsg? = null,
        @ProtoId(46) val lolaMsg: LolaMsg? = null,
        @ProtoId(47) val groupBusinessMsg: GroupBusinessMsg? = null,
        @ProtoId(48) val msgWorkflowNotify: WorkflowNotifyMsg? = null,
        @ProtoId(49) val patElem: PatsElem? = null,
        @ProtoId(50) val groupPostElem: GroupPostElem? = null,
        @ProtoId(51) val lightApp: LightAppElem? = null,
        @ProtoId(52) val eimInfo: EIMInfo? = null,
        @ProtoId(53) val commonElem: CommonElem? = null
    ) : ProtoBuf

    @Serializable
    internal class ElemFlags(
        @ProtoId(1) val flags1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val businessData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ElemFlags2(
        @ProtoId(1) val colorTextId: Int = 0,
        @ProtoId(2) val msgId: Long = 0L,
        @ProtoId(3) val whisperSessionId: Int = 0,
        @ProtoId(4) val pttChangeBit: Int = 0,
        @ProtoId(5) val vipStatus: Int = 0,
        @ProtoId(6) val compatibleId: Int = 0,
        @ProtoId(7) val insts: List<Inst>? = null,
        @ProtoId(8) val msgRptCnt: Int = 0,
        @ProtoId(9) val srcInst: Inst? = null,
        @ProtoId(10) val longtitude: Int = 0,
        @ProtoId(11) val latitude: Int = 0,
        @ProtoId(12) val customFont: Int = 0,
        @ProtoId(13) val pcSupportDef: PcSupportDef? = null,
        @ProtoId(14) val crmFlags: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class Inst(
            @ProtoId(1) val appId: Int = 0,
            @ProtoId(2) val instId: Int = 0
        )
    }

    @Serializable
    internal class ExtraInfo(
        @ProtoId(1) val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val groupCard: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val level: Int = 0,
        @ProtoId(4) val flags: Int = 0,
        @ProtoId(5) val groupMask: Int = 0,
        @ProtoId(6) val msgTailId: Int = 0,
        @ProtoId(7) val senderTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val apnsTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val uin: Long = 0L,
        @ProtoId(10) val msgStateFlag: Int = 0,
        @ProtoId(11) val apnsSoundType: Int = 0,
        @ProtoId(12) val newGroupFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Face(
        @ProtoId(1) val index: Int = 0,
        @ProtoId(2) val old: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val buf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class FSJMessageElem(
        @ProtoId(1) val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FunFace(
        @ProtoId(1) val msgTurntable: Turntable? = null,
        @ProtoId(2) val msgBomb: Bomb? = null
    ) {
        @Serializable
        internal class Bomb(
            @ProtoId(1) val boolBurst: Boolean = false
        )

        @Serializable
        internal class Turntable(
            @ProtoId(1) val uint64UinList: List<Long>? = null,
            @ProtoId(2) val hitUin: Long = 0L,
            @ProtoId(3) val hitUinNick: String = ""
        )
    }

    @Serializable
    internal class GeneralFlags(
        @ProtoId(1) val bubbleDiyTextId: Int = 0,
        @ProtoId(2) val groupFlagNew: Int = 0,
        @ProtoId(3) val uin: Long = 0L,
        @ProtoId(4) val rpId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val prpFold: Int = 0,
        @ProtoId(6) val longTextFlag: Int = 0,
        @ProtoId(7) val longTextResid: String = "",
        @ProtoId(8) val groupType: Int = 0,
        @ProtoId(9) val toUinFlag: Int = 0,
        @ProtoId(10) val glamourLevel: Int = 0,
        @ProtoId(11) val memberLevel: Int = 0,
        @ProtoId(12) val groupRankSeq: Long = 0L,
        @ProtoId(13) val olympicTorch: Int = 0,
        @ProtoId(14) val babyqGuideMsgCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) val uin32ExpertFlag: Int = 0,
        @ProtoId(16) val bubbleSubId: Int = 0,
        @ProtoId(17) val pendantId: Long = 0L,
        @ProtoId(18) val rpIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY // 78 00 F8 01 00 C8 02 00
    ) : ProtoBuf

    @Serializable
    internal class GroupBusinessMsg(
        @ProtoId(1) val flags: Int = 0,
        @ProtoId(2) val headUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val headClkUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val nickColor: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val rank: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val rankColor: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val rankBgcolor: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupFile(
        @ProtoId(1) val filename: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fileSize: Long = 0L,
        @ProtoId(3) val fileId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val batchId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val mark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val sequence: Long = 0L,
        @ProtoId(8) val batchItemId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val feedMsgTime: Int = 0,
        @ProtoId(10) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupPostElem(
        @ProtoId(1) val transType: Int = 0,
        @ProtoId(2) val transMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupPubAccountInfo(
        @ProtoId(1) val pubAccount: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class LifeOnlineAccount(
        @ProtoId(1) val uniqueId: Long = 0L,
        @ProtoId(2) val op: Int = 0,
        @ProtoId(3) val showTime: Int = 0,
        @ProtoId(4) val report: Int = 0,
        @ProtoId(5) val ack: Int = 0,
        @ProtoId(6) val bitmap: Long = 0L,
        @ProtoId(7) val gdtImpData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val gdtCliData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val viewId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LightAppElem(
        @ProtoId(1) val data: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LocationInfo(
        @ProtoId(1) val longitude: Double = 0.0,
        @ProtoId(2) val latitude: Double = 0.0,
        @ProtoId(3) val desc: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LolaMsg(
        @ProtoId(1) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val encodeContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val longMsgUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val downloadKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LowVersionTips(
        @ProtoId(1) val businessId: Int = 0,
        @ProtoId(2) val sessionType: Int = 0,
        @ProtoId(3) val sessionUin: Long = 0L,
        @ProtoId(4) val senderUin: Long = 0L,
        @ProtoId(5) val text: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MarketFace(
        @ProtoId(1) val faceName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val itemType: Int = 0,
        @ProtoId(3) val faceInfo: Int = 0,
        @ProtoId(4) val faceId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val tabId: Int = 0,
        @ProtoId(6) val subType: Int = 0,
        @ProtoId(7) val key: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val param: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val mediaType: Int = 0,
        @ProtoId(10) val imageWidth: Int = 0,
        @ProtoId(11) val imageHeight: Int = 0,
        @ProtoId(12) val mobileparam: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MarketTrans(
        @ProtoId(1) val int32Flag: Int = 0,
        @ProtoId(2) val xml: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val ability: Int = 0,
        @ProtoId(5) val minAbility: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @ProtoId(1) val richText: RichText = RichText(),
        @ProtoId(2) val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgEncryptContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgBodySubtype4(
        @ProtoId(1) val msgNotOnlineFile: NotOnlineFile? = null,
        @ProtoId(2) val msgTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class NearByMessageType(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val identifyType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class NotOnlineFile(
        @ProtoId(1) val fileType: Int = 0,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val fileSize: Long = 0L,
        @ProtoId(7) val note: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val reserved: Int = 0,
        @ProtoId(9) val subcmd: Int = 0,
        @ProtoId(10) val microCloud: Int = 0,
        @ProtoId(11) val bytesFileUrls: List<ByteArray>? = listOf(),
        @ProtoId(12) val downloadFlag: Int = 0,
        @ProtoId(50) val dangerEvel: Int = 0,
        @ProtoId(51) val lifeTime: Int = 0,
        @ProtoId(52) val uploadTime: Int = 0,
        @ProtoId(53) val absFileType: Int = 0,
        @ProtoId(54) val clientType: Int = 0,
        @ProtoId(55) val expireTime: Int = 0,
        @ProtoId(56) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NotOnlineImage(
        @ProtoId(1) val filePath: String = "",
        @ProtoId(2) val fileLen: Int = 0,
        @ProtoId(3) val downloadPath: String = "",
        @ProtoId(4) val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val imgType: Int = 0,
        @ProtoId(6) val previewsImage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val picMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val picHeight: Int = 0,
        @ProtoId(9) val picWidth: Int = 0,
        @ProtoId(10) val resId: String = "",
        @ProtoId(11) val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val thumbUrl: String = "",
        @ProtoId(13) val original: Int = 0,
        @ProtoId(14) val bigUrl: String = "",
        @ProtoId(15) val origUrl: String = "",
        @ProtoId(16) val bizType: Int = 0,
        @ProtoId(17) val result: Int = 0,
        @ProtoId(18) val index: Int = 0,
        @ProtoId(19) val opFaceBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20) val oldPicMd5: Boolean = false,
        @ProtoId(21) val thumbWidth: Int = 0,
        @ProtoId(22) val thumbHeight: Int = 0,
        @ProtoId(23) val fileId: Int = 0,
        @ProtoId(24) val showLen: Int = 0,
        @ProtoId(25) val downloadLen: Int = 0,
        @ProtoId(26) val _400Url: String = "",
        @ProtoId(27) val _400Width: Int = 0,
        @ProtoId(28) val _400Height: Int = 0,
        @ProtoId(29) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable // 非官方.
    internal class PbReserve(
        @ProtoId(1) val unknown1: Int = 1,
        @ProtoId(2) val unknown2: Int = 0,
        @ProtoId(6) val unknown3: Int = 0,
        @ProtoId(8) val hint: String = "[动画表情]",
        @ProtoId(10) val unknown5: Int = 0,
        @ProtoId(15) val unknwon6: Int = 5
    ) : ProtoBuf {
        companion object {
            val DEFAULT: ByteArray = PbReserve().toByteArray(serializer())
        }
    }

    @Serializable
    internal class OnlineImage(
        @ProtoId(1) val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class OpenQQData(
        @ProtoId(1) val carQqData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PatsElem(
        @ProtoId(1) val patType: Int = 0,
        @ProtoId(2) val patCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PcSupportDef(
        @ProtoId(1) val pcPtlBegin: Int = 0,
        @ProtoId(2) val pcPtlEnd: Int = 0,
        @ProtoId(3) val macPtlBegin: Int = 0,
        @ProtoId(4) val macPtlEnd: Int = 0,
        @ProtoId(5) val ptlsSupport: List<Int>? = null,
        @ProtoId(6) val ptlsNotSupport: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    internal class Ptt(
        @ProtoId(1) val fileType: Int = 0,
        @ProtoId(2) val srcUin: Long = 0L,
        @ProtoId(3) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val fileSize: Int = 0,
        @ProtoId(7) val reserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val fileId: Int = 0,
        @ProtoId(9) val serverIp: Int = 0,
        @ProtoId(10) val serverPort: Int = 0,
        @ProtoId(11) val boolValid: Boolean = false,
        @ProtoId(12) val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val shortcut: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) val magicPttIndex: Int = 0,
        @ProtoId(16) val voiceSwitch: Int = 0,
        @ProtoId(17) val pttUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(18) val groupFileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) val time: Int = 0,
        @ProtoId(20) val downPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(29) val format: Int = 0,
        @ProtoId(30) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(31) val bytesPttUrls: List<ByteArray>? = listOf(),
        @ProtoId(32) val downloadFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PubAccInfo(
        @ProtoId(1) val isInterNum: Int = 0,
        @ProtoId(2) val ingMsgTemplateId: String = "",
        @ProtoId(3) val ingLongMsgUrl: String = "",
        @ProtoId(4) val downloadKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PubAccount(
        @ProtoId(1) val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val pubAccountUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PubGroup(
        @ProtoId(1) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val gender: Int = 0,
        @ProtoId(3) val age: Int = 0,
        @ProtoId(4) val distance: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQLiveOld(
        @ProtoId(1) val subCmd: Int = 0,
        @ProtoId(2) val showText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val param: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val introduce: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQWalletAioBody(
        @ProtoId(1) val senduin: Long = 0L,
        @ProtoId(2) val sender: QQWalletAioElem? = null,
        @ProtoId(3) val receiver: QQWalletAioElem? = null,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(4) val sint32Channelid: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(5) val sint32Templateid: Int = 0,
        @ProtoId(6) val resend: Int = 0,
        @ProtoId(7) val msgPriority: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(8) val sint32Redtype: Int = 0,
        @ProtoId(9) val billno: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(11) val sint32Sessiontype: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(12) val sint32Msgtype: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(13) val sint32Envelopeid: Int = 0,
        @ProtoId(14) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(15) val sint32Conftype: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(16) val sint32MsgFrom: Int = 0,
        @ProtoId(17) val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(18) val ingIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) val redchannel: Int = 0,
        @ProtoId(20) val grapUin: List<Long>? = null,
        @ProtoId(21) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQWalletAioElem(
        @ProtoId(1) val background: Int = 0,
        @ProtoId(2) val icon: Int = 0,
        @ProtoId(3) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val subtitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val content: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val linkurl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val blackstripe: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val notice: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val titleColor: Int = 0,
        @ProtoId(10) val subtitleColor: Int = 0,
        @ProtoId(11) val actionsPriority: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val nativeIos: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val nativeAndroid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) val iconurl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(16) val contentColor: Int = 0,
        @ProtoId(17) val contentBgcolor: Int = 0,
        @ProtoId(18) val aioImageLeft: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) val aioImageRight: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20) val cftImage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQWalletMsg(
        @ProtoId(1) val aioBody: QQWalletAioBody? = null
    ) : ProtoBuf

    @Serializable
    internal class RedBagInfo(
        @ProtoId(1) val redbagType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RichMsg(
        @ProtoId(1) val template1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val serviceId: Int = 0,
        @ProtoId(3) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val rand: Int = 0,
        @ProtoId(5) val seq: Int = 0,
        @ProtoId(6) val flags: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RichText(
        @ProtoId(1) val attr: Attr? = null,
        @ProtoId(2) val elems: MutableList<Elem> = mutableListOf(),
        @ProtoId(3) val notOnlineFile: NotOnlineFile? = null,
        @ProtoId(4) val ptt: Ptt? = null,
        @ProtoId(5) val tmpPtt: TmpPtt? = null,
        @ProtoId(6) val trans211TmpMsg: Trans211TmpMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class SecretFileMsg(
        @ProtoId(1) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fromUin: Long = 0L,
        @ProtoId(3) val toUin: Long = 0L,
        @ProtoId(4) val status: Int = 0,
        @ProtoId(5) val ttl: Int = 0,
        @ProtoId(6) val type: Int = 0,
        @ProtoId(7) val encryptPreheadLength: Int = 0,
        @ProtoId(8) val encryptType: Int = 0,
        @ProtoId(9) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val readTimes: Int = 0,
        @ProtoId(11) val leftTime: Int = 0,
        @ProtoId(12) val notOnlineImage: NotOnlineImage? = null,
        @ProtoId(13) val elemFlags2: ElemFlags2? = null,
        @ProtoId(14) val opertype: Int = 0,
        @ProtoId(15) val fromphonenum: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ShakeWindow(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val reserve: Int = 0,
        @ProtoId(3) val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class SmallEmoji(
        @ProtoId(1) val packIdSum: Int = 0,
        @ProtoId(2) val imageType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SourceMsg(
        @ProtoId(1) val origSeqs: List<Int>? = null,
        @ProtoId(2) val senderUin: Long = 0L,
        @ProtoId(3) val time: Int = 0,
        @ProtoId(4) val flag: Int = 0,
        @ProtoId(5) val elems: List<Elem>? = null,
        @ProtoId(6) val type: Int = 0,
        @ProtoId(7) val richMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val srcMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val toUin: Long = 0L,
        @ProtoId(11) val troopName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Text(
        @ProtoId(1) val str: String = "",
        @ProtoId(2) val link: String = "",
        @ProtoId(3) val attr6Buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val attr7Buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TipsInfo(
        @ProtoId(1) val text: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TmpPtt(
        @ProtoId(1) val fileType: Int = 0,
        @ProtoId(2) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileSize: Int = 0,
        @ProtoId(6) val pttTimes: Int = 0,
        @ProtoId(7) val userType: Int = 0,
        @ProtoId(8) val ptttransFlag: Int = 0,
        @ProtoId(9) val busiType: Int = 0,
        @ProtoId(10) val msgId: Long = 0L,
        @ProtoId(30) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(31) val pttEncodeData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Trans211TmpMsg(
        @ProtoId(1) val msgBody: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val c2cCmd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransElem(
        @ProtoId(1) val elemType: Int = 0,
        @ProtoId(2) val elemValue: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class VideoFile(
        @ProtoId(1) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val fileFormat: Int = 0,
        @ProtoId(5) val fileTime: Int = 0,
        @ProtoId(6) val fileSize: Int = 0,
        @ProtoId(7) val thumbWidth: Int = 0,
        @ProtoId(8) val thumbHeight: Int = 0,
        @ProtoId(9) val thumbFileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val source: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val thumbFileSize: Int = 0,
        @ProtoId(12) val busiType: Int = 0,
        @ProtoId(13) val fromChatType: Int = 0,
        @ProtoId(14) val toChatType: Int = 0,
        @ProtoId(15) val boolSupportProgressive: Boolean = false,
        @ProtoId(16) val fileWidth: Int = 0,
        @ProtoId(17) val fileHeight: Int = 0,
        @ProtoId(18) val subBusiType: Int = 0,
        @ProtoId(19) val videoAttr: Int = 0,
        @ProtoId(20) val bytesThumbFileUrls: List<ByteArray>? = null,
        @ProtoId(21) val bytesVideoFileUrls: List<ByteArray>? = null,
        @ProtoId(22) val thumbDownloadFlag: Int = 0,
        @ProtoId(23) val videoDownloadFlag: Int = 0,
        @ProtoId(24) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class WorkflowNotifyMsg(
        @ProtoId(1) val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val createUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class ImMsgHead : ProtoBuf {
    @Serializable
    internal class C2CHead(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val fromUin: Long = 0L,
        @ProtoId(3) val ccType: Int = 0,
        @ProtoId(4) val ccCmd: Int = 0,
        @ProtoId(5) val authPicSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val authSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val authBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val serverTime: Int = 0,
        @ProtoId(9) val clientTime: Int = 0,
        @ProtoId(10) val rand: Int = 0,
        @ProtoId(11) val ingPhoneNumber: String = ""
    ) : ProtoBuf

    @Serializable
    internal class CSHead(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val command: Int = 0,
        @ProtoId(3) val seq: Int = 0,
        @ProtoId(4) val version: Int = 0,
        @ProtoId(5) val retryTimes: Int = 0,
        @ProtoId(6) val clientType: Int = 0,
        @ProtoId(7) val pubno: Int = 0,
        @ProtoId(8) val localid: Int = 0,
        @ProtoId(9) val timezone: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(10) val clientIp: Int = 0,
        @ProtoId(11) val clientPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(12) val connIp: Int = 0,
        @ProtoId(13) val connPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(14) val interfaceIp: Int = 0,
        @ProtoId(15) val interfacePort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(16) val actualIp: Int = 0,
        @ProtoId(17) val flag: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(18) val timestamp: Int = 0,
        @ProtoId(19) val subcmd: Int = 0,
        @ProtoId(20) val result: Int = 0,
        @ProtoId(21) val appId: Int = 0,
        @ProtoId(22) val instanceId: Int = 0,
        @ProtoId(23) val sessionId: Long = 0L,
        @ProtoId(24) val idcId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DeltaHead(
        @ProtoId(1) val totalLen: Long = 0L,
        @ProtoId(2) val offset: Long = 0L,
        @ProtoId(3) val ackOffset: Long = 0L,
        @ProtoId(4) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val ackCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val result: Int = 0,
        @ProtoId(7) val flags: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Head(
        @ProtoId(1) val headType: Int = 0,
        @ProtoId(2) val msgCsHead: CSHead? = null,
        @ProtoId(3) val msgS2cHead: S2CHead? = null,
        @ProtoId(4) val msgHttpconnHead: HttpConnHead? = null,
        @ProtoId(5) val paintFlag: Int = 0,
        @ProtoId(6) val msgLoginSig: LoginSig? = null,
        @ProtoId(7) val msgDeltaHead: DeltaHead? = null,
        @ProtoId(8) val msgC2cHead: C2CHead? = null,
        @ProtoId(9) val msgSconnHead: SConnHead? = null,
        @ProtoId(10) val msgInstCtrl: InstCtrl? = null
    ) : ProtoBuf

    @Serializable
    internal class HttpConnHead(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val command: Int = 0,
        @ProtoId(3) val subCommand: Int = 0,
        @ProtoId(4) val seq: Int = 0,
        @ProtoId(5) val version: Int = 0,
        @ProtoId(6) val retryTimes: Int = 0,
        @ProtoId(7) val clientType: Int = 0,
        @ProtoId(8) val pubNo: Int = 0,
        @ProtoId(9) val localId: Int = 0,
        @ProtoId(10) val timeZone: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(11) val clientIp: Int = 0,
        @ProtoId(12) val clientPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(13) val qzhttpIp: Int = 0,
        @ProtoId(14) val qzhttpPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(15) val sppIp: Int = 0,
        @ProtoId(16) val sppPort: Int = 0,
        @ProtoId(17) val flag: Int = 0,
        @ProtoId(18) val key: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(19) val compressType: Int = 0,
        @ProtoId(20) val originSize: Int = 0,
        @ProtoId(21) val errorCode: Int = 0,
        @ProtoId(22) val msgRedirect: RedirectMsg? = null,
        @ProtoId(23) val commandId: Int = 0,
        @ProtoId(24) val serviceCmdid: Int = 0,
        @ProtoId(25) val msgOidbhead: TransOidbHead? = null
    ) : ProtoBuf

    @Serializable
    internal class InstCtrl(
        @ProtoId(1) val msgSendToInst: List<InstInfo>? = listOf(),
        @ProtoId(2) val msgExcludeInst: List<InstInfo>? = listOf(),
        @ProtoId(3) val msgFromInst: InstInfo? = InstInfo()
    ) : ProtoBuf

    @Serializable
    internal class InstInfo(
        @ProtoId(1) val apppid: Int = 0,
        @ProtoId(2) val instid: Int = 0,
        @ProtoId(3) val platform: Int = 0,
        @ProtoId(10) val enumDeviceType: Int /* enum */ = 0
    ) : ProtoBuf

    @Serializable
    internal class LoginSig(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RedirectMsg(
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(1) val lastRedirectIp: Int = 0,
        @ProtoId(2) val lastRedirectPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(3) val redirectIp: Int = 0,
        @ProtoId(4) val redirectPort: Int = 0,
        @ProtoId(5) val redirectCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class S2CHead(
        @ProtoId(1) val subMsgtype: Int = 0,
        @ProtoId(2) val msgType: Int = 0,
        @ProtoId(3) val fromUin: Long = 0L,
        @ProtoId(4) val msgId: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(5) val relayIp: Int = 0,
        @ProtoId(6) val relayPort: Int = 0,
        @ProtoId(7) val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class SConnHead : ProtoBuf

    @Serializable
    internal class TransOidbHead(
        @ProtoId(1) val command: Int = 0,
        @ProtoId(2) val serviceType: Int = 0,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val errorMsg: String = ""
    ) : ProtoBuf
}

@Serializable
internal class ImReceipt : ProtoBuf {
    @Serializable
    internal class MsgInfo(
        @ProtoId(1) val fromUin: Long = 0L,
        @ProtoId(2) val toUin: Long = 0L,
        @ProtoId(3) val msgSeq: Int = 0,
        @ProtoId(4) val msgRandom: Int = 0
    ) : ProtoBuf

    @Serializable
    data internal class ReceiptInfo(
        @ProtoId(1) val readTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReceiptReq(
        @ProtoId(1) val command: Int /* enum */ = 1,
        @ProtoId(2) val msgInfo: MsgInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ReceiptResp(
        @ProtoId(1) val command: Int /* enum */ = 1,
        @ProtoId(2) val receiptInfo: ReceiptInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class ObjMsg : ProtoBuf {
    @Serializable
    internal class MsgContentInfo(
        @ProtoId(1) val contentInfoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val msgFile: MsgFile? = null
    ) : ProtoBuf {
        @Serializable
        internal class MsgFile(
            @ProtoId(1) val busId: Int = 0,
            @ProtoId(2) val filePath: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val fileSize: Long = 0L,
            @ProtoId(4) val fileName: String = "",
            @ProtoId(5) val int64DeadTime: Long = 0L,
            @ProtoId(6) val fileSha1: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val ext: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class MsgPic(
        @ProtoId(1) val smallPicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val originalPicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val localPicId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ObjMsg(
        @ProtoId(1) val msgType: Int = 0,
        @ProtoId(2) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val bytesAbstact: List<ByteArray>? = null,
        @ProtoId(5) val titleExt: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val msgPic: List<MsgPic>? = null,
        @ProtoId(7) val msgContentInfo: List<MsgContentInfo>? = null,
        @ProtoId(8) val reportIdShow: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Submsgtype0xc7 : ProtoBuf {
    @Serializable
    internal class RelationalChainChange(
        @ProtoId(1) val appid: Long = 0L,
        @ProtoId(2) val srcUin: Long = 0L,
        @ProtoId(3) val dstUin: Long = 0L,
        @ProtoId(4) val changeType: Int /* enum */ = 1,
        @ProtoId(5) val msgRelationalChainInfoOld: RelationalChainInfo? = null,
        @ProtoId(6) val msgRelationalChainInfoNew: RelationalChainInfo? = null,
        @ProtoId(7) val msgToDegradeInfo: ToDegradeInfo? = null,
        @ProtoId(20) val relationalChainInfos: List<RelationalChainInfos>? = null,
        @ProtoId(100) val uint32FeatureId: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    internal class FriendShipFlagNotify(
        @ProtoId(1) val dstUin: Long = 0L,
        @ProtoId(2) val level1: Int = 0,
        @ProtoId(3) val level2: Int = 0,
        @ProtoId(4) val continuityDays: Int = 0,
        @ProtoId(5) val chatFlag: Int = 0,
        @ProtoId(6) val lastChatTime: Long = 0L,
        @ProtoId(7) val notifyTime: Long = 0L,
        @ProtoId(8) val seq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ToDegradeItem(
        @ProtoId(1) val type: Int /* enum */ = 1,
        @ProtoId(2) val oldLevel: Int = 0,
        @ProtoId(3) val newLevel: Int = 0,
        @ProtoId(11) val continuityDays: Int = 0,
        @ProtoId(12) val lastActionTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RelationalChainInfo(
        @ProtoId(1) val type: Int /* enum */ = 1,
        @ProtoId(2) val attr: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(1002) val intimateInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(91001) val musicSwitch: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(101001) val mutualmarkAlienation: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ForwardBody(
        @ProtoId(1) val notifyType: Int = 0,
        @ProtoId(2) val opType: Int = 0,
        @ProtoId(3000) val msgHotFriendNotify: HotFriendNotify? = null,
        @ProtoId(4000) val msgRelationalChainChange: RelationalChainChange? = null
    ) : ProtoBuf

    @Serializable
    internal class HotFriendNotify(
        @ProtoId(1) val dstUin: Long = 0L,
        @ProtoId(2) val praiseHotLevel: Int = 0,
        @ProtoId(3) val chatHotLevel: Int = 0,
        @ProtoId(4) val praiseHotDays: Int = 0,
        @ProtoId(5) val chatHotDays: Int = 0,
        @ProtoId(6) val closeLevel: Int = 0,
        @ProtoId(7) val closeDays: Int = 0,
        @ProtoId(8) val praiseFlag: Int = 0,
        @ProtoId(9) val chatFlag: Int = 0,
        @ProtoId(10) val closeFlag: Int = 0,
        @ProtoId(11) val notifyTime: Long = 0L,
        @ProtoId(12) val lastPraiseTime: Long = 0L,
        @ProtoId(13) val lastChatTime: Long = 0L,
        @ProtoId(14) val qzoneHotLevel: Int = 0,
        @ProtoId(15) val qzoneHotDays: Int = 0,
        @ProtoId(16) val qzoneFlag: Int = 0,
        @ProtoId(17) val lastQzoneTime: Long = 0L,
        @ProtoId(51) val showRecheckEntry: Int = 0,
        @ProtoId(52) val wildcardWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(100) val loverFlag: Int = 0,
        @ProtoId(200) val keyHotLevel: Int = 0,
        @ProtoId(201) val keyHotDays: Int = 0,
        @ProtoId(202) val keyFlag: Int = 0,
        @ProtoId(203) val lastKeyTime: Long = 0L,
        @ProtoId(204) val keyWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(205) val keyTransFlag: Int = 0,
        @ProtoId(206) val loverKeyBusinessType: Int = 0,
        @ProtoId(207) val loverKeySubBusinessType: Int = 0,
        @ProtoId(208) val loverKeyMainWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(209) val loverKeyLinkWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(300) val boatLevel: Int = 0,
        @ProtoId(301) val boatDays: Int = 0,
        @ProtoId(302) val boatFlag: Int = 0,
        @ProtoId(303) val lastBoatTime: Int = 0,
        @ProtoId(304) val boatWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(400) val notifyType: Int = 0,
        @ProtoId(401) val msgFriendshipFlagNotify: FriendShipFlagNotify? = null
    ) : ProtoBuf

    @Serializable
    internal class RelationalChainInfos(
        @ProtoId(1) val msgRelationalChainInfoOld: RelationalChainInfo? = null,
        @ProtoId(2) val msgRelationalChainInfoNew: RelationalChainInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ToDegradeInfo(
        @ProtoId(1) val toDegradeItem: List<ToDegradeItem>? = null,
        @ProtoId(2) val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val notifyTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @ProtoId(1) val msgModInfos: List<ForwardBody>? = null
    ) : ProtoBuf
}
