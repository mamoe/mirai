/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray

@Serializable
internal class ImCommon : ProtoBuf {
    @Serializable
    internal class GroupInfo(
        @ProtoNumber(1) @JvmField val groupId: Long = 0L,
        @ProtoNumber(2) @JvmField val groupType: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class Signature(
        @ProtoNumber(1) @JvmField val keyType: Int = 0,
        @ProtoNumber(2) @JvmField val sessionAppId: Int = 0,
        @ProtoNumber(3) @JvmField val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Token(
        @ProtoNumber(1) @JvmField val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val c2cType: Int = 0,
        @ProtoNumber(3) @JvmField val serviceType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class User(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val appId: Int = 0,
        @ProtoNumber(3) @JvmField val instanceId: Int = 0,
        @ProtoNumber(4) @JvmField val appType: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(5) @JvmField val clientIp: Int = 0,
        @ProtoNumber(6) @JvmField val version: Int = 0,
        @ProtoNumber(7) @JvmField val phoneNumber: String = "",
        @ProtoNumber(8) @JvmField val platformId: Int = 0,
        @ProtoNumber(9) @JvmField val language: Int = 0,
        @ProtoNumber(10) @JvmField val equipKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class ImImagent : ProtoBuf {
    @Serializable
    internal class ImAgentHead(
        @ProtoNumber(1) @JvmField val command: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val seq: Int = 0,
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val err: String = "",
        @ProtoNumber(5) @JvmField val echoBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val reqUser: ImCommon.User? = null,
        @ProtoNumber(7) @JvmField val reqInfo: Requestinfo? = null,
        @ProtoNumber(8) @JvmField val signature: Signature? = null,
        @ProtoNumber(9) @JvmField val subCmd: Int = 0,
        @ProtoNumber(10) @JvmField val serverIp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ImAgentPackage(
        @ProtoNumber(1) @JvmField val head: ImAgentHead? = null,
        @ProtoNumber(11) @JvmField val msgSendReq: ImMsg.MsgSendReq? = null,
        @ProtoNumber(12) @JvmField val msgSendResp: ImMsg.MsgSendResp? = null
    ) : ProtoBuf

    @Serializable
    internal class Requestinfo(
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(1) @JvmField val reqIp: Int = 0,
        @ProtoNumber(2) @JvmField val reqPort: Int = 0,
        @ProtoNumber(3) @JvmField val reqFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Signature(
        @ProtoNumber(1) @JvmField val keyType: Int = 0,
        @ProtoNumber(2) @JvmField val sessionAppId: Int = 0,
        @ProtoNumber(3) @JvmField val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class ImMsg : ProtoBuf {
    @Serializable
    internal class C2C(
        @ProtoNumber(1) @JvmField val sender: ImCommon.User? = null,
        @ProtoNumber(2) @JvmField val receiver: ImCommon.User? = null,
        @ProtoNumber(3) @JvmField val c2cRelation: C2CRelation? = null
    ) : ProtoBuf

    @Serializable
    internal class C2CRelation(
        @ProtoNumber(1) @JvmField val c2cType: Int /* enum */ = 0,
        @ProtoNumber(2) @JvmField val groupInfo: ImCommon.GroupInfo? = null,
        @ProtoNumber(3) @JvmField val token: ImCommon.Token? = null
    ) : ProtoBuf

    @Serializable
    internal class ContentHead(
        @ProtoNumber(1) @JvmField val pkgNum: Int = 1,
        @ProtoNumber(2) @JvmField val pkgIndex: Int = 0,
        @ProtoNumber(3) @JvmField val seq: Int = 0,
        @ProtoNumber(4) @JvmField val dateTime: Int = 0,
        @ProtoNumber(5) @JvmField val msgType: Int = 0,
        @ProtoNumber(6) @JvmField val divSeq: Int = 0,
        @ProtoNumber(7) @JvmField val msgdbUin: Long = 0L,
        @ProtoNumber(8) @JvmField val msgdbSeq: Int = 0,
        @ProtoNumber(9) @JvmField val wordMsgSeq: Int = 0,
        @ProtoNumber(10) @JvmField val msgRand: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Group(
        @ProtoNumber(1) @JvmField val sender: ImCommon.User? = null,
        @ProtoNumber(2) @JvmField val receiver: ImCommon.User? = null,
        @ProtoNumber(3) @JvmField val groupInfo: ImCommon.GroupInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class Msg(
        @ProtoNumber(1) @JvmField val head: MsgHead? = null,
        @ProtoNumber(2) @JvmField val body: ImMsgBody.MsgBody? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgHead(
        @ProtoNumber(1) @JvmField val routingHead: RoutingHead? = null,
        @ProtoNumber(2) @JvmField val contentHead: ContentHead? = null,
        @ProtoNumber(3) @JvmField val gbkTmpMsgBody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgSendReq(
        @ProtoNumber(1) @JvmField val msg: Msg? = null,
        @ProtoNumber(2) @JvmField val buMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgTailId: Int = 0,
        @ProtoNumber(4) @JvmField val connMsgFlag: Int = 0,
        @ProtoNumber(5) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgSendResp

    @Serializable
    internal class RoutingHead(
        @ProtoNumber(1) @JvmField val c2c: C2C? = null,
        @ProtoNumber(2) @JvmField val group: Group? = null
    ) : ProtoBuf
}

@Serializable
internal class ImMsgBody : ProtoBuf {
    @Serializable
    internal class AnonymousGroupMsg(
        @ProtoNumber(1) @JvmField val flags: Int = 0,
        @ProtoNumber(2) @JvmField val anonId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val anonNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val headPortrait: Int = 0,
        @ProtoNumber(5) @JvmField val expireTime: Int = 0,
        @ProtoNumber(6) @JvmField val bubbleId: Int = 0,
        @ProtoNumber(7) @JvmField val rankColor: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ApolloActMsg(
        @ProtoNumber(1) @JvmField val actionId: Int = 0,
        @ProtoNumber(2) @JvmField val actionName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val actionText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val flag: Int = 0,
        @ProtoNumber(5) @JvmField val peerUin: Int = 0,
        @ProtoNumber(6) @JvmField val senderTs: Int = 0,
        @ProtoNumber(7) @JvmField val peerTs: Int = 0,
        @ProtoNumber(8) @JvmField val int32SenderStatus: Int = 0,
        @ProtoNumber(9) @JvmField val int32PeerStatus: Int = 0,
        @ProtoNumber(10) @JvmField val diytextId: Int = 0,
        @ProtoNumber(11) @JvmField val diytextContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val inputText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ArkAppElem(
        @ProtoNumber(1) @JvmField val appName: String = "",
        @ProtoNumber(2) @JvmField val minVersion: String = "",
        @ProtoNumber(3) @JvmField val xmlTemplate: String = "",
        @ProtoNumber(4) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Attr(
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(1) @JvmField val codePage: Int = -1,
        @ProtoNumber(2) @JvmField val time: Int = 1,
        @ProtoNumber(3) @JvmField val random: Int = 0,
        @ProtoNumber(4) @JvmField val color: Int = 0,
        @ProtoNumber(5) @JvmField val size: Int = 10,
        @ProtoNumber(6) @JvmField val effect: Int = 7,
        @ProtoNumber(7) @JvmField val charSet: Int = 78,
        @ProtoNumber(8) @JvmField val pitchAndFamily: Int = 90,
        @ProtoNumber(9) @JvmField val fontName: String = "Times New Roman",
        @ProtoNumber(10) @JvmField val reserveData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BitAppMsg(
        @ProtoNumber(1) @JvmField val buf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BlessingMessage(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val exFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommonElem(
        @ProtoNumber(1) @JvmField val serviceType: Int = 0,
        @ProtoNumber(2) @JvmField val pbElem: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val businessType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ConferenceTipsInfo(
        @ProtoNumber(1) @JvmField val sessionType: Int = 0,
        @ProtoNumber(2) @JvmField val sessionUin: Long = 0L,
        @ProtoNumber(3) @JvmField val text: String = ""
    ) : ProtoBuf

    @Serializable
    internal class CrmElem(
        @ProtoNumber(1) @JvmField val crmBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val qidianFlag: Int = 0,
        @ProtoNumber(4) @JvmField val pushFlag: Int = 0,
        @ProtoNumber(5) @JvmField val countFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CustomElem(
        @ProtoNumber(1) @JvmField val desc: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val enumType: Int /* enum */ = 1,
        @ProtoNumber(4) @JvmField val ext: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val sound: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CustomFace(
        @ProtoNumber(1) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val filePath: String = "",
        @ProtoNumber(3) @JvmField val shortcut: String = "",
        @ProtoNumber(4) @JvmField val buffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val oldData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val fileId: Int = 0,
        @ProtoNumber(8) @JvmField val serverIp: Int = 0,
        @ProtoNumber(9) @JvmField val serverPort: Int = 0,
        @ProtoNumber(10) @JvmField val fileType: Int = 0,
        @ProtoNumber(11) @JvmField val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val useful: Int = 0,
        @ProtoNumber(13) @JvmField val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val thumbUrl: String = "",
        @ProtoNumber(15) @JvmField val bigUrl: String = "",
        @ProtoNumber(16) @JvmField val origUrl: String = "",
        @ProtoNumber(17) @JvmField val bizType: Int = 0,
        @ProtoNumber(18) @JvmField val repeatIndex: Int = 0,
        @ProtoNumber(19) @JvmField val repeatImage: Int = 0,
        @ProtoNumber(20) @JvmField val imageType: Int = 0,
        @ProtoNumber(21) @JvmField val index: Int = 0,
        @ProtoNumber(22) @JvmField val width: Int = 0,
        @ProtoNumber(23) @JvmField val height: Int = 0,
        @ProtoNumber(24) @JvmField val source: Int = 0,
        @ProtoNumber(25) @JvmField val size: Int = 0,
        @ProtoNumber(26) @JvmField val origin: Int = 0,
        @ProtoNumber(27) @JvmField val thumbWidth: Int = 0,
        @ProtoNumber(28) @JvmField val thumbHeight: Int = 0,
        @ProtoNumber(29) @JvmField val showLen: Int = 0,
        @ProtoNumber(30) @JvmField val downloadLen: Int = 0,
        @ProtoNumber(31) @JvmField val _400Url: String = "",
        @ProtoNumber(32) @JvmField val _400Width: Int = 0,
        @ProtoNumber(33) @JvmField val _400Height: Int = 0,
        @ProtoNumber(34) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class DeliverGiftMsg(
        @ProtoNumber(1) @JvmField val grayTipContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val animationPackageId: Int = 0,
        @ProtoNumber(3) @JvmField val animationPackageUrlA: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val animationPackageUrlI: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val remindBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val giftId: Int = 0,
        @ProtoNumber(7) @JvmField val giftCount: Int = 0,
        @ProtoNumber(8) @JvmField val animationBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val senderUin: Long = 0L,
        @ProtoNumber(10) @JvmField val receiverUin: Long = 0L,
        @ProtoNumber(11) @JvmField val stmessageTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val stmessageSubtitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val stmessageMessage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val stmessageGiftpicid: Int = 0,
        @ProtoNumber(15) @JvmField val stmessageComefrom: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(16) @JvmField val stmessageExflag: Int = 0,
        @ProtoNumber(17) @JvmField val toAllGiftId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(18) @JvmField val comefromLink: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(19) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20) @JvmField val receiverName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(21) @JvmField val receiverPic: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(22) @JvmField val stmessageGifturl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class EIMInfo(
        @ProtoNumber(1) @JvmField val rootId: Long = 0L,
        @ProtoNumber(2) @JvmField val flag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Elem(
        @ProtoNumber(1) @JvmField val text: Text? = null,
        @ProtoNumber(2) @JvmField val face: Face? = null,
        @ProtoNumber(3) @JvmField val onlineImage: OnlineImage? = null,
        @ProtoNumber(4) @JvmField val notOnlineImage: NotOnlineImage? = null,
        @ProtoNumber(5) @JvmField val transElemInfo: TransElem? = null,
        @ProtoNumber(6) @JvmField val marketFace: MarketFace? = null,
        @ProtoNumber(7) @JvmField val elemFlags: ElemFlags? = null,
        @ProtoNumber(8) @JvmField val customFace: CustomFace? = null,
        @ProtoNumber(9) @JvmField val elemFlags2: ElemFlags2? = null,
        @ProtoNumber(10) @JvmField val funFace: FunFace? = null,
        @ProtoNumber(11) @JvmField val secretFile: SecretFileMsg? = null,
        @ProtoNumber(12) @JvmField val richMsg: RichMsg? = null,
        @ProtoNumber(13) @JvmField val groupFile: GroupFile? = null,
        @ProtoNumber(14) @JvmField val pubGroup: PubGroup? = null,
        @ProtoNumber(15) @JvmField val marketTrans: MarketTrans? = null,
        @ProtoNumber(16) @JvmField val extraInfo: ExtraInfo? = null,
        @ProtoNumber(17) @JvmField val shakeWindow: ShakeWindow? = null,
        @ProtoNumber(18) @JvmField val pubAccount: PubAccount? = null,
        @ProtoNumber(19) @JvmField val videoFile: VideoFile? = null,
        @ProtoNumber(20) @JvmField val tipsInfo: TipsInfo? = null,
        @ProtoNumber(21) @JvmField val anonGroupMsg: AnonymousGroupMsg? = null,
        @ProtoNumber(22) @JvmField val qqLiveOld: QQLiveOld? = null,
        @ProtoNumber(23) @JvmField val lifeOnline: LifeOnlineAccount? = null,
        @ProtoNumber(24) @JvmField val qqwalletMsg: QQWalletMsg? = null,
        @ProtoNumber(25) @JvmField val crmElem: CrmElem? = null,
        @ProtoNumber(26) @JvmField val conferenceTipsInfo: ConferenceTipsInfo? = null,
        @ProtoNumber(27) @JvmField val redbagInfo: RedBagInfo? = null,
        @ProtoNumber(28) @JvmField val lowVersionTips: LowVersionTips? = null,
        @ProtoNumber(29) @JvmField val bankcodeCtrlInfo: ByteArray? = null,
        @ProtoNumber(30) @JvmField val nearByMsg: NearByMessageType? = null,
        @ProtoNumber(31) @JvmField val customElem: CustomElem? = null,
        @ProtoNumber(32) @JvmField val locationInfo: LocationInfo? = null,
        @ProtoNumber(33) @JvmField val pubAccInfo: PubAccInfo? = null,
        @ProtoNumber(34) @JvmField val smallEmoji: SmallEmoji? = null,
        @ProtoNumber(35) @JvmField val fsjMsgElem: FSJMessageElem? = null,
        @ProtoNumber(36) @JvmField val arkApp: ArkAppElem? = null,
        @ProtoNumber(37) @JvmField val generalFlags: GeneralFlags? = null,
        @ProtoNumber(38) @JvmField val hcFlashPic: CustomFace? = null,
        @ProtoNumber(39) @JvmField val deliverGiftMsg: DeliverGiftMsg? = null,
        @ProtoNumber(40) @JvmField val bitappMsg: BitAppMsg? = null,
        @ProtoNumber(41) @JvmField val openQqData: OpenQQData? = null,
        @ProtoNumber(42) @JvmField val apolloMsg: ApolloActMsg? = null,
        @ProtoNumber(43) @JvmField val groupPubAccInfo: GroupPubAccountInfo? = null,
        @ProtoNumber(44) @JvmField val blessMsg: BlessingMessage? = null,
        @ProtoNumber(45) @JvmField val srcMsg: SourceMsg? = null,
        @ProtoNumber(46) @JvmField val lolaMsg: LolaMsg? = null,
        @ProtoNumber(47) @JvmField val groupBusinessMsg: GroupBusinessMsg? = null,
        @ProtoNumber(48) @JvmField val msgWorkflowNotify: WorkflowNotifyMsg? = null,
        @ProtoNumber(49) @JvmField val patElem: PatsElem? = null,
        @ProtoNumber(50) @JvmField val groupPostElem: GroupPostElem? = null,
        @ProtoNumber(51) @JvmField val lightApp: LightAppElem? = null,
        @ProtoNumber(52) @JvmField val eimInfo: EIMInfo? = null,
        @ProtoNumber(53) @JvmField val commonElem: CommonElem? = null
    ) : ProtoBuf

    @Serializable
    internal class ElemFlags(
        @ProtoNumber(1) @JvmField val flags1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val businessData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ElemFlags2(
        @ProtoNumber(1) @JvmField val colorTextId: Int = 0,
        @ProtoNumber(2) @JvmField val msgId: Long = 0L,
        @ProtoNumber(3) @JvmField val whisperSessionId: Int = 0,
        @ProtoNumber(4) @JvmField val pttChangeBit: Int = 0,
        @ProtoNumber(5) @JvmField val vipStatus: Int = 0,
        @ProtoNumber(6) @JvmField val compatibleId: Int = 0,
        @ProtoNumber(7) @JvmField val insts: List<Inst> = emptyList(),
        @ProtoNumber(8) @JvmField val msgRptCnt: Int = 0,
        @ProtoNumber(9) @JvmField val srcInst: Inst? = null,
        @ProtoNumber(10) @JvmField val longtitude: Int = 0,
        @ProtoNumber(11) @JvmField val latitude: Int = 0,
        @ProtoNumber(12) @JvmField val customFont: Int = 0,
        @ProtoNumber(13) @JvmField val pcSupportDef: PcSupportDef? = null,
        @ProtoNumber(14) @JvmField val crmFlags: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class Inst(
            @ProtoNumber(1) @JvmField val appId: Int = 0,
            @ProtoNumber(2) @JvmField val instId: Int = 0
        )
    }

    @Serializable
    internal class ExtraInfo(
        @ProtoNumber(1) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val groupCard: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val level: Int = 0,
        @ProtoNumber(4) @JvmField val flags: Int = 0,
        @ProtoNumber(5) @JvmField val groupMask: Int = 0,
        @ProtoNumber(6) @JvmField val msgTailId: Int = 0,
        @ProtoNumber(7) @JvmField val senderTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val apnsTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val uin: Long = 0L,
        @ProtoNumber(10) @JvmField val msgStateFlag: Int = 0,
        @ProtoNumber(11) @JvmField val apnsSoundType: Int = 0,
        @ProtoNumber(12) @JvmField val newGroupFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Face(
        @ProtoNumber(1) @JvmField val index: Int = 0,
        @ProtoNumber(2) @JvmField val old: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val buf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class FSJMessageElem(
        @ProtoNumber(1) @JvmField val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FunFace(
        @ProtoNumber(1) @JvmField val msgTurntable: Turntable? = null,
        @ProtoNumber(2) @JvmField val msgBomb: Bomb? = null
    ) {
        @Serializable
        internal class Bomb(
            @ProtoNumber(1) @JvmField val boolBurst: Boolean = false
        )

        @Serializable
        internal class Turntable(
            @ProtoNumber(1) @JvmField val uint64UinList: List<Long> = emptyList(),
            @ProtoNumber(2) @JvmField val hitUin: Long = 0L,
            @ProtoNumber(3) @JvmField val hitUinNick: String = ""
        )
    }

    @Serializable
    internal class GeneralFlags(
        @ProtoNumber(1) @JvmField val bubbleDiyTextId: Int = 0,
        @ProtoNumber(2) @JvmField val groupFlagNew: Int = 0,
        @ProtoNumber(3) @JvmField val uin: Long = 0L,
        @ProtoNumber(4) @JvmField val rpId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val prpFold: Int = 0,
        @ProtoNumber(6) @JvmField val longTextFlag: Int = 0,
        @ProtoNumber(7) @JvmField val longTextResid: String = "",
        @ProtoNumber(8) @JvmField val groupType: Int = 0,
        @ProtoNumber(9) @JvmField val toUinFlag: Int = 0,
        @ProtoNumber(10) @JvmField val glamourLevel: Int = 0,
        @ProtoNumber(11) @JvmField val memberLevel: Int = 0,
        @ProtoNumber(12) @JvmField val groupRankSeq: Long = 0L,
        @ProtoNumber(13) @JvmField val olympicTorch: Int = 0,
        @ProtoNumber(14) @JvmField val babyqGuideMsgCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(15) @JvmField val uin32ExpertFlag: Int = 0,
        @ProtoNumber(16) @JvmField val bubbleSubId: Int = 0,
        @ProtoNumber(17) @JvmField val pendantId: Long = 0L,
        @ProtoNumber(18) @JvmField val rpIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(19) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY // 78 00 F8 01 00 C8 02 00
    ) : ProtoBuf

    @Serializable
    internal class GroupBusinessMsg(
        @ProtoNumber(1) @JvmField val flags: Int = 0,
        @ProtoNumber(2) @JvmField val headUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val headClkUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val nickColor: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val rank: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val rankColor: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val rankBgcolor: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupFile(
        @ProtoNumber(1) @JvmField val filename: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fileSize: Long = 0L,
        @ProtoNumber(3) @JvmField val fileId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val batchId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val mark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val sequence: Long = 0L,
        @ProtoNumber(8) @JvmField val batchItemId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val feedMsgTime: Int = 0,
        @ProtoNumber(10) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupPostElem(
        @ProtoNumber(1) @JvmField val transType: Int = 0,
        @ProtoNumber(2) @JvmField val transMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupPubAccountInfo(
        @ProtoNumber(1) @JvmField val pubAccount: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class LifeOnlineAccount(
        @ProtoNumber(1) @JvmField val uniqueId: Long = 0L,
        @ProtoNumber(2) @JvmField val op: Int = 0,
        @ProtoNumber(3) @JvmField val showTime: Int = 0,
        @ProtoNumber(4) @JvmField val report: Int = 0,
        @ProtoNumber(5) @JvmField val ack: Int = 0,
        @ProtoNumber(6) @JvmField val bitmap: Long = 0L,
        @ProtoNumber(7) @JvmField val gdtImpData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val gdtCliData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val viewId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LightAppElem(
        @ProtoNumber(1) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LocationInfo(
        @ProtoNumber(1) @JvmField val longitude: Double = 0.0,
        @ProtoNumber(2) @JvmField val latitude: Double = 0.0,
        @ProtoNumber(3) @JvmField val desc: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LolaMsg(
        @ProtoNumber(1) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val encodeContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val longMsgUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val downloadKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class LowVersionTips(
        @ProtoNumber(1) @JvmField val businessId: Int = 0,
        @ProtoNumber(2) @JvmField val sessionType: Int = 0,
        @ProtoNumber(3) @JvmField val sessionUin: Long = 0L,
        @ProtoNumber(4) @JvmField val senderUin: Long = 0L,
        @ProtoNumber(5) @JvmField val text: String = ""
    ) : ProtoBuf

    @Serializable
    internal data class MarketFace(
        @ProtoNumber(1) @JvmField val faceName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val itemType: Int = 0,
        @ProtoNumber(3) @JvmField val faceInfo: Int = 0,
        @ProtoNumber(4) @JvmField val faceId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val tabId: Int = 0,
        @ProtoNumber(6) @JvmField val subType: Int = 0,
        @ProtoNumber(7) @JvmField val key: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val param: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val mediaType: Int = 0,
        @ProtoNumber(10) @JvmField val imageWidth: Int = 0,
        @ProtoNumber(11) @JvmField val imageHeight: Int = 0,
        @ProtoNumber(12) @JvmField val mobileParam: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Suppress("DuplicatedCode")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MarketFace

            if (!faceName.contentEquals(other.faceName)) return false
            if (itemType != other.itemType) return false
            if (faceInfo != other.faceInfo) return false
            if (!faceId.contentEquals(other.faceId)) return false
            if (tabId != other.tabId) return false
            if (subType != other.subType) return false
            if (!key.contentEquals(other.key)) return false
            if (!param.contentEquals(other.param)) return false
            if (mediaType != other.mediaType) return false
            if (imageWidth != other.imageWidth) return false
            if (imageHeight != other.imageHeight) return false
            if (!mobileParam.contentEquals(other.mobileParam)) return false
            if (!pbReserve.contentEquals(other.pbReserve)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = faceName.contentHashCode()
            result = 31 * result + itemType
            result = 31 * result + faceInfo
            result = 31 * result + faceId.contentHashCode()
            result = 31 * result + tabId
            result = 31 * result + subType
            result = 31 * result + key.contentHashCode()
            result = 31 * result + param.contentHashCode()
            result = 31 * result + mediaType
            result = 31 * result + imageWidth
            result = 31 * result + imageHeight
            result = 31 * result + mobileParam.contentHashCode()
            result = 31 * result + pbReserve.contentHashCode()
            return result
        }
    }

    @Serializable
    internal class MarketTrans(
        @ProtoNumber(1) @JvmField val int32Flag: Int = 0,
        @ProtoNumber(2) @JvmField val xml: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val ability: Int = 0,
        @ProtoNumber(5) @JvmField val minAbility: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @ProtoNumber(1) @JvmField val richText: RichText = RichText(),
        @ProtoNumber(2) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgEncryptContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgBodySubtype4(
        @ProtoNumber(1) @JvmField val msgNotOnlineFile: NotOnlineFile? = null,
        @ProtoNumber(2) @JvmField val msgTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class NearByMessageType(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val identifyType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class NotOnlineFile(
        @ProtoNumber(1) @JvmField val fileType: Int = 0,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val fileSize: Long = 0L,
        @ProtoNumber(7) @JvmField val note: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val reserved: Int = 0,
        @ProtoNumber(9) @JvmField val subcmd: Int = 0,
        @ProtoNumber(10) @JvmField val microCloud: Int = 0,
        @ProtoNumber(11) @JvmField val bytesFileUrls: List<ByteArray> = emptyList(),
        @ProtoNumber(12) @JvmField val downloadFlag: Int = 0,
        @ProtoNumber(50) @JvmField val dangerEvel: Int = 0,
        @ProtoNumber(51) @JvmField val lifeTime: Int = 0,
        @ProtoNumber(52) @JvmField val uploadTime: Int = 0,
        @ProtoNumber(53) @JvmField val absFileType: Int = 0,
        @ProtoNumber(54) @JvmField val clientType: Int = 0,
        @ProtoNumber(55) @JvmField val expireTime: Int = 0,
        @ProtoNumber(56) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NotOnlineImage(
        @ProtoNumber(1) @JvmField val filePath: String = "",
        @ProtoNumber(2) @JvmField val fileLen: Int = 0,
        @ProtoNumber(3) @JvmField val downloadPath: String = "",
        @ProtoNumber(4) @JvmField val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val imgType: Int = 0,
        @ProtoNumber(6) @JvmField val previewsImage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val picMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val picHeight: Int = 0,
        @ProtoNumber(9) @JvmField val picWidth: Int = 0,
        @ProtoNumber(10) @JvmField val resId: String = "",
        @ProtoNumber(11) @JvmField val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val thumbUrl: String = "",
        @ProtoNumber(13) @JvmField val original: Int = 0,
        @ProtoNumber(14) @JvmField val bigUrl: String = "",
        @ProtoNumber(15) @JvmField val origUrl: String = "",
        @ProtoNumber(16) @JvmField val bizType: Int = 0,
        @ProtoNumber(17) @JvmField val result: Int = 0,
        @ProtoNumber(18) @JvmField val index: Int = 0,
        @ProtoNumber(19) @JvmField val opFaceBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20) @JvmField val oldPicMd5: Boolean = false,
        @ProtoNumber(21) @JvmField val thumbWidth: Int = 0,
        @ProtoNumber(22) @JvmField val thumbHeight: Int = 0,
        @ProtoNumber(23) @JvmField val fileId: Int = 0,
        @ProtoNumber(24) @JvmField val showLen: Int = 0,
        @ProtoNumber(25) @JvmField val downloadLen: Int = 0,
        @ProtoNumber(26) @JvmField val _400Url: String = "",
        @ProtoNumber(27) @JvmField val _400Width: Int = 0,
        @ProtoNumber(28) @JvmField val _400Height: Int = 0,
        @ProtoNumber(29) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable // 非官方.
    internal class PbReserve(
        @ProtoNumber(1) @JvmField val unknown1: Int = 1,
        @ProtoNumber(2) @JvmField val unknown2: Int = 0,
        @ProtoNumber(6) @JvmField val unknown3: Int = 0,
        @ProtoNumber(8) @JvmField val hint: String = "[动画表情]",
        @ProtoNumber(10) @JvmField val unknown5: Int = 0,
        @ProtoNumber(15) @JvmField val unknwon6: Int = 5
    ) : ProtoBuf {
        companion object {
            @JvmField
            val DEFAULT: ByteArray = PbReserve().toByteArray(serializer())
        }
    }

    @Serializable
    internal class OnlineImage(
        @ProtoNumber(1) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class OpenQQData(
        @ProtoNumber(1) @JvmField val carQqData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PatsElem(
        @ProtoNumber(1) @JvmField val patType: Int = 0,
        @ProtoNumber(2) @JvmField val patCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PcSupportDef(
        @ProtoNumber(1) @JvmField val pcPtlBegin: Int = 0,
        @ProtoNumber(2) @JvmField val pcPtlEnd: Int = 0,
        @ProtoNumber(3) @JvmField val macPtlBegin: Int = 0,
        @ProtoNumber(4) @JvmField val macPtlEnd: Int = 0,
        @ProtoNumber(5) @JvmField val ptlsSupport: List<Int> = emptyList(),
        @ProtoNumber(6) @JvmField val ptlsNotSupport: List<Int> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class Ptt(
        @ProtoNumber(1) @JvmField val fileType: Int = 0,
        @ProtoNumber(2) @JvmField val srcUin: Long = 0L,
        @ProtoNumber(3) @JvmField val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val fileSize: Int = 0,
        @ProtoNumber(7) @JvmField val reserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val fileId: Int = 0,
        @ProtoNumber(9) @JvmField val serverIp: Int = 0,
        @ProtoNumber(10) @JvmField val serverPort: Int = 0,
        @ProtoNumber(11) @JvmField val boolValid: Boolean = false,
        @ProtoNumber(12) @JvmField val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val shortcut: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(15) @JvmField val magicPttIndex: Int = 0,
        @ProtoNumber(16) @JvmField val voiceSwitch: Int = 0,
        @ProtoNumber(17) @JvmField val pttUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(18) @JvmField val groupFileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(19) @JvmField val time: Int = 0,
        @ProtoNumber(20) @JvmField val downPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(29) @JvmField val format: Int = 0,
        @ProtoNumber(30) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(31) @JvmField val bytesPttUrls: List<ByteArray> = emptyList(),
        @ProtoNumber(32) @JvmField val downloadFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PubAccInfo(
        @ProtoNumber(1) @JvmField val isInterNum: Int = 0,
        @ProtoNumber(2) @JvmField val ingMsgTemplateId: String = "",
        @ProtoNumber(3) @JvmField val ingLongMsgUrl: String = "",
        @ProtoNumber(4) @JvmField val downloadKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PubAccount(
        @ProtoNumber(1) @JvmField val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val pubAccountUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PubGroup(
        @ProtoNumber(1) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val gender: Int = 0,
        @ProtoNumber(3) @JvmField val age: Int = 0,
        @ProtoNumber(4) @JvmField val distance: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQLiveOld(
        @ProtoNumber(1) @JvmField val subCmd: Int = 0,
        @ProtoNumber(2) @JvmField val showText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val param: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val introduce: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQWalletAioBody(
        @ProtoNumber(1) @JvmField val senduin: Long = 0L,
        @ProtoNumber(2) @JvmField val sender: QQWalletAioElem? = null,
        @ProtoNumber(3) @JvmField val receiver: QQWalletAioElem? = null,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(4) @JvmField val sint32Channelid: Int = 0,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(5) @JvmField val sint32Templateid: Int = 0,
        @ProtoNumber(6) @JvmField val resend: Int = 0,
        @ProtoNumber(7) @JvmField val msgPriority: Int = 0,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(8) @JvmField val sint32Redtype: Int = 0,
        @ProtoNumber(9) @JvmField val billno: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(11) @JvmField val sint32Sessiontype: Int = 0,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(12) @JvmField val sint32Msgtype: Int = 0,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(13) @JvmField val sint32Envelopeid: Int = 0,
        @ProtoNumber(14) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(15) @JvmField val sint32Conftype: Int = 0,
        @ProtoType(ProtoIntegerType.SIGNED) @ProtoNumber(16) @JvmField val sint32MsgFrom: Int = 0,
        @ProtoNumber(17) @JvmField val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(18) @JvmField val ingIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(19) @JvmField val redchannel: Int = 0,
        @ProtoNumber(20) @JvmField val grapUin: List<Long> = emptyList(),
        @ProtoNumber(21) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQWalletAioElem(
        @ProtoNumber(1) @JvmField val background: Int = 0,
        @ProtoNumber(2) @JvmField val icon: Int = 0,
        @ProtoNumber(3) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val subtitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val linkurl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val blackstripe: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val notice: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val titleColor: Int = 0,
        @ProtoNumber(10) @JvmField val subtitleColor: Int = 0,
        @ProtoNumber(11) @JvmField val actionsPriority: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val nativeIos: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val nativeAndroid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(15) @JvmField val iconurl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(16) @JvmField val contentColor: Int = 0,
        @ProtoNumber(17) @JvmField val contentBgcolor: Int = 0,
        @ProtoNumber(18) @JvmField val aioImageLeft: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(19) @JvmField val aioImageRight: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20) @JvmField val cftImage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(21) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQWalletMsg(
        @ProtoNumber(1) @JvmField val aioBody: QQWalletAioBody? = null
    ) : ProtoBuf

    @Serializable
    internal class RedBagInfo(
        @ProtoNumber(1) @JvmField val redbagType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RichMsg(
        @ProtoNumber(1) @JvmField val template1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val serviceId: Int = 0,
        @ProtoNumber(3) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val rand: Int = 0,
        @ProtoNumber(5) @JvmField val seq: Int = 0,
        @ProtoNumber(6) @JvmField val flags: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RichText(
        @ProtoNumber(1) @JvmField val attr: Attr? = null,
        @ProtoNumber(2) @JvmField val elems: MutableList<Elem> = mutableListOf(),
        @ProtoNumber(3) @JvmField val notOnlineFile: NotOnlineFile? = null,
        @ProtoNumber(4) @JvmField val ptt: Ptt? = null,
        @ProtoNumber(5) @JvmField val tmpPtt: TmpPtt? = null,
        @ProtoNumber(6) @JvmField val trans211TmpMsg: Trans211TmpMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class SecretFileMsg(
        @ProtoNumber(1) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(3) @JvmField val toUin: Long = 0L,
        @ProtoNumber(4) @JvmField val status: Int = 0,
        @ProtoNumber(5) @JvmField val ttl: Int = 0,
        @ProtoNumber(6) @JvmField val type: Int = 0,
        @ProtoNumber(7) @JvmField val encryptPreheadLength: Int = 0,
        @ProtoNumber(8) @JvmField val encryptType: Int = 0,
        @ProtoNumber(9) @JvmField val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val readTimes: Int = 0,
        @ProtoNumber(11) @JvmField val leftTime: Int = 0,
        @ProtoNumber(12) @JvmField val notOnlineImage: NotOnlineImage? = null,
        @ProtoNumber(13) @JvmField val elemFlags2: ElemFlags2? = null,
        @ProtoNumber(14) @JvmField val opertype: Int = 0,
        @ProtoNumber(15) @JvmField val fromphonenum: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ShakeWindow(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val reserve: Int = 0,
        @ProtoNumber(3) @JvmField val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class SmallEmoji(
        @ProtoNumber(1) @JvmField val packIdSum: Int = 0,
        @ProtoNumber(2) @JvmField val imageType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SourceMsg(
        @ProtoNumber(1) @JvmField val origSeqs: IntArray = intArrayOf(),
        @ProtoNumber(2) @JvmField val senderUin: Long = 0L,
        @ProtoNumber(3) @JvmField val time: Int = 0,
        @ProtoNumber(4) @JvmField val flag: Int = 0,
        @ProtoNumber(5) @JvmField val elems: List<Elem> = emptyList(),
        @ProtoNumber(6) @JvmField val type: Int = 0,
        @ProtoNumber(7) @JvmField val richMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val srcMsg: ByteArray? = null,
        @ProtoNumber(10) @JvmField val toUin: Long = 0L,
        @ProtoNumber(11) @JvmField val troopName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Text(
        @ProtoNumber(1) @JvmField val str: String = "",
        @ProtoNumber(2) @JvmField val link: String = "",
        @ProtoNumber(3) @JvmField val attr6Buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val attr7Buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TipsInfo(
        @ProtoNumber(1) @JvmField val text: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TmpPtt(
        @ProtoNumber(1) @JvmField val fileType: Int = 0,
        @ProtoNumber(2) @JvmField val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileSize: Int = 0,
        @ProtoNumber(6) @JvmField val pttTimes: Int = 0,
        @ProtoNumber(7) @JvmField val userType: Int = 0,
        @ProtoNumber(8) @JvmField val ptttransFlag: Int = 0,
        @ProtoNumber(9) @JvmField val busiType: Int = 0,
        @ProtoNumber(10) @JvmField val msgId: Long = 0L,
        @ProtoNumber(30) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(31) @JvmField val pttEncodeData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Trans211TmpMsg(
        @ProtoNumber(1) @JvmField val msgBody: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val c2cCmd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransElem(
        @ProtoNumber(1) @JvmField val elemType: Int = 0,
        @ProtoNumber(2) @JvmField val elemValue: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class VideoFile(
        @ProtoNumber(1) @JvmField val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val fileFormat: Int = 0,
        @ProtoNumber(5) @JvmField val fileTime: Int = 0,
        @ProtoNumber(6) @JvmField val fileSize: Int = 0,
        @ProtoNumber(7) @JvmField val thumbWidth: Int = 0,
        @ProtoNumber(8) @JvmField val thumbHeight: Int = 0,
        @ProtoNumber(9) @JvmField val thumbFileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val source: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val thumbFileSize: Int = 0,
        @ProtoNumber(12) @JvmField val busiType: Int = 0,
        @ProtoNumber(13) @JvmField val fromChatType: Int = 0,
        @ProtoNumber(14) @JvmField val toChatType: Int = 0,
        @ProtoNumber(15) @JvmField val boolSupportProgressive: Boolean = false,
        @ProtoNumber(16) @JvmField val fileWidth: Int = 0,
        @ProtoNumber(17) @JvmField val fileHeight: Int = 0,
        @ProtoNumber(18) @JvmField val subBusiType: Int = 0,
        @ProtoNumber(19) @JvmField val videoAttr: Int = 0,
        @ProtoNumber(20) @JvmField val bytesThumbFileUrls: List<ByteArray> = emptyList(),
        @ProtoNumber(21) @JvmField val bytesVideoFileUrls: List<ByteArray> = emptyList(),
        @ProtoNumber(22) @JvmField val thumbDownloadFlag: Int = 0,
        @ProtoNumber(23) @JvmField val videoDownloadFlag: Int = 0,
        @ProtoNumber(24) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class WorkflowNotifyMsg(
        @ProtoNumber(1) @JvmField val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val createUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class ImMsgHead : ProtoBuf {
    @Serializable
    internal class C2CHead(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(3) @JvmField val ccType: Int = 0,
        @ProtoNumber(4) @JvmField val ccCmd: Int = 0,
        @ProtoNumber(5) @JvmField val authPicSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val authSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val authBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val serverTime: Int = 0,
        @ProtoNumber(9) @JvmField val clientTime: Int = 0,
        @ProtoNumber(10) @JvmField val rand: Int = 0,
        @ProtoNumber(11) @JvmField val ingPhoneNumber: String = ""
    ) : ProtoBuf

    @Serializable
    internal class CSHead(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val command: Int = 0,
        @ProtoNumber(3) @JvmField val seq: Int = 0,
        @ProtoNumber(4) @JvmField val version: Int = 0,
        @ProtoNumber(5) @JvmField val retryTimes: Int = 0,
        @ProtoNumber(6) @JvmField val clientType: Int = 0,
        @ProtoNumber(7) @JvmField val pubno: Int = 0,
        @ProtoNumber(8) @JvmField val localid: Int = 0,
        @ProtoNumber(9) @JvmField val timezone: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(10) @JvmField val clientIp: Int = 0,
        @ProtoNumber(11) @JvmField val clientPort: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(12) @JvmField val connIp: Int = 0,
        @ProtoNumber(13) @JvmField val connPort: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(14) @JvmField val interfaceIp: Int = 0,
        @ProtoNumber(15) @JvmField val interfacePort: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(16) @JvmField val actualIp: Int = 0,
        @ProtoNumber(17) @JvmField val flag: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(18) @JvmField val timestamp: Int = 0,
        @ProtoNumber(19) @JvmField val subcmd: Int = 0,
        @ProtoNumber(20) @JvmField val result: Int = 0,
        @ProtoNumber(21) @JvmField val appId: Int = 0,
        @ProtoNumber(22) @JvmField val instanceId: Int = 0,
        @ProtoNumber(23) @JvmField val sessionId: Long = 0L,
        @ProtoNumber(24) @JvmField val idcId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DeltaHead(
        @ProtoNumber(1) @JvmField val totalLen: Long = 0L,
        @ProtoNumber(2) @JvmField val offset: Long = 0L,
        @ProtoNumber(3) @JvmField val ackOffset: Long = 0L,
        @ProtoNumber(4) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val ackCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val result: Int = 0,
        @ProtoNumber(7) @JvmField val flags: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Head(
        @ProtoNumber(1) @JvmField val headType: Int = 0,
        @ProtoNumber(2) @JvmField val msgCsHead: CSHead? = null,
        @ProtoNumber(3) @JvmField val msgS2cHead: S2CHead? = null,
        @ProtoNumber(4) @JvmField val msgHttpconnHead: HttpConnHead? = null,
        @ProtoNumber(5) @JvmField val paintFlag: Int = 0,
        @ProtoNumber(6) @JvmField val msgLoginSig: LoginSig? = null,
        @ProtoNumber(7) @JvmField val msgDeltaHead: DeltaHead? = null,
        @ProtoNumber(8) @JvmField val msgC2cHead: C2CHead? = null,
        @ProtoNumber(9) @JvmField val msgSconnHead: SConnHead? = null,
        @ProtoNumber(10) @JvmField val msgInstCtrl: InstCtrl? = null
    ) : ProtoBuf

    @Serializable
    internal class HttpConnHead(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val command: Int = 0,
        @ProtoNumber(3) @JvmField val subCommand: Int = 0,
        @ProtoNumber(4) @JvmField val seq: Int = 0,
        @ProtoNumber(5) @JvmField val version: Int = 0,
        @ProtoNumber(6) @JvmField val retryTimes: Int = 0,
        @ProtoNumber(7) @JvmField val clientType: Int = 0,
        @ProtoNumber(8) @JvmField val pubNo: Int = 0,
        @ProtoNumber(9) @JvmField val localId: Int = 0,
        @ProtoNumber(10) @JvmField val timeZone: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(11) @JvmField val clientIp: Int = 0,
        @ProtoNumber(12) @JvmField val clientPort: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(13) @JvmField val qzhttpIp: Int = 0,
        @ProtoNumber(14) @JvmField val qzhttpPort: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(15) @JvmField val sppIp: Int = 0,
        @ProtoNumber(16) @JvmField val sppPort: Int = 0,
        @ProtoNumber(17) @JvmField val flag: Int = 0,
        @ProtoNumber(18) @JvmField val key: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(19) @JvmField val compressType: Int = 0,
        @ProtoNumber(20) @JvmField val originSize: Int = 0,
        @ProtoNumber(21) @JvmField val errorCode: Int = 0,
        @ProtoNumber(22) @JvmField val msgRedirect: RedirectMsg? = null,
        @ProtoNumber(23) @JvmField val commandId: Int = 0,
        @ProtoNumber(24) @JvmField val serviceCmdid: Int = 0,
        @ProtoNumber(25) @JvmField val msgOidbhead: TransOidbHead? = null
    ) : ProtoBuf

    @Serializable
    internal class InstCtrl(
        @ProtoNumber(1) @JvmField val msgSendToInst: List<InstInfo> = emptyList(),
        @ProtoNumber(2) @JvmField val msgExcludeInst: List<InstInfo> = emptyList(),
        @ProtoNumber(3) @JvmField val msgFromInst: InstInfo? = InstInfo()
    ) : ProtoBuf

    @Serializable
    internal class InstInfo(
        @ProtoNumber(1) @JvmField val apppid: Int = 0,
        @ProtoNumber(2) @JvmField val instid: Int = 0,
        @ProtoNumber(3) @JvmField val platform: Int = 0,
        @ProtoNumber(10) @JvmField val enumDeviceType: Int /* enum */ = 0
    ) : ProtoBuf

    @Serializable
    internal class LoginSig(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RedirectMsg(
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(1) @JvmField val lastRedirectIp: Int = 0,
        @ProtoNumber(2) @JvmField val lastRedirectPort: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(3) @JvmField val redirectIp: Int = 0,
        @ProtoNumber(4) @JvmField val redirectPort: Int = 0,
        @ProtoNumber(5) @JvmField val redirectCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class S2CHead(
        @ProtoNumber(1) @JvmField val subMsgtype: Int = 0,
        @ProtoNumber(2) @JvmField val msgType: Int = 0,
        @ProtoNumber(3) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(4) @JvmField val msgId: Int = 0,
        @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(5) @JvmField val relayIp: Int = 0,
        @ProtoNumber(6) @JvmField val relayPort: Int = 0,
        @ProtoNumber(7) @JvmField val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class SConnHead : ProtoBuf

    @Serializable
    internal class TransOidbHead(
        @ProtoNumber(1) @JvmField val command: Int = 0,
        @ProtoNumber(2) @JvmField val serviceType: Int = 0,
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val errorMsg: String = ""
    ) : ProtoBuf
}

@Serializable
internal class ImReceipt : ProtoBuf {
    @Serializable
    internal class MsgInfo(
        @ProtoNumber(1) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(2) @JvmField val toUin: Long = 0L,
        @ProtoNumber(3) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(4) @JvmField val msgRandom: Int = 0
    ) : ProtoBuf

    @Serializable
    internal data class ReceiptInfo(
        @ProtoNumber(1) @JvmField val readTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReceiptReq(
        @ProtoNumber(1) @JvmField val command: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val msgInfo: MsgInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ReceiptResp(
        @ProtoNumber(1) @JvmField val command: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val receiptInfo: ReceiptInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class ObjMsg : ProtoBuf {
    @Serializable
    internal class MsgContentInfo(
        @ProtoNumber(1) @JvmField val contentInfoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgFile: MsgFile? = null
    ) : ProtoBuf {
        @Serializable
        internal class MsgFile(
            @ProtoNumber(1) @JvmField val busId: Int = 0,
            @ProtoNumber(2) @JvmField val filePath: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(3) @JvmField val fileSize: Long = 0L,
            @ProtoNumber(4) @JvmField val fileName: String = "",
            @ProtoNumber(5) @JvmField val int64DeadTime: Long = 0L,
            @ProtoNumber(6) @JvmField val fileSha1: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(7) @JvmField val ext: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class MsgPic(
        @ProtoNumber(1) @JvmField val smallPicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val originalPicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val localPicId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ObjMsg(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val bytesAbstact: List<ByteArray> = emptyList(),
        @ProtoNumber(5) @JvmField val titleExt: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val msgPic: List<MsgPic> = emptyList(),
        @ProtoNumber(7) @JvmField val msgContentInfo: List<MsgContentInfo> = emptyList(),
        @ProtoNumber(8) @JvmField val reportIdShow: Int = 0
    ) : ProtoBuf
}
