package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
class ImCommon : ProtoBuf {
    @Serializable
    class GroupInfo(
        @SerialId(1) val groupId: Long = 0L,
        @SerialId(2) val groupType: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    class Signature(
        @SerialId(1) val keyType: Int = 0,
        @SerialId(2) val sessionAppId: Int = 0,
        @SerialId(3) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class Token(
        @SerialId(1) val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val c2cType: Int = 0,
        @SerialId(3) val serviceType: Int = 0
    ) : ProtoBuf

    @Serializable
    class User(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val appId: Int = 0,
        @SerialId(3) val instanceId: Int = 0,
        @SerialId(4) val appType: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(5) val clientIp: Int = 0,
        @SerialId(6) val version: Int = 0,
        @SerialId(7) val phoneNumber: String = "",
        @SerialId(8) val platformId: Int = 0,
        @SerialId(9) val language: Int = 0,
        @SerialId(10) val equipKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
class ImImagent : ProtoBuf {
    @Serializable
    class ImAgentHead(
        @SerialId(1) val command: Int /* enum */ = 1,
        @SerialId(2) val seq: Int = 0,
        @SerialId(3) val result: Int = 0,
        @SerialId(4) val err: String = "",
        @SerialId(5) val echoBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val reqUser: ImCommon.User? = null,
        @SerialId(7) val reqInfo: Requestinfo? = null,
        @SerialId(8) val signature: Signature? = null,
        @SerialId(9) val subCmd: Int = 0,
        @SerialId(10) val serverIp: Int = 0
    ) : ProtoBuf

    @Serializable
    class ImAgentPackage(
        @SerialId(1) val head: ImAgentHead? = null,
        @SerialId(11) val msgSendReq: ImMsg.MsgSendReq? = null,
        @SerialId(12) val msgSendResp: ImMsg.MsgSendResp? = null
    ) : ProtoBuf

    @Serializable
    class Requestinfo(
        @ProtoType(ProtoNumberType.FIXED) @SerialId(1) val reqIp: Int = 0,
        @SerialId(2) val reqPort: Int = 0,
        @SerialId(3) val reqFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class Signature(
        @SerialId(1) val keyType: Int = 0,
        @SerialId(2) val sessionAppId: Int = 0,
        @SerialId(3) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
class ImMsg : ProtoBuf {
    @Serializable
    class C2C(
        @SerialId(1) val sender: ImCommon.User? = null,
        @SerialId(2) val receiver: ImCommon.User? = null,
        @SerialId(3) val c2cRelation: C2CRelation? = null
    ) : ProtoBuf

    @Serializable
    class C2CRelation(
        @SerialId(1) val c2cType: Int /* enum */ = 0,
        @SerialId(2) val groupInfo: ImCommon.GroupInfo? = null,
        @SerialId(3) val token: ImCommon.Token? = null
    ) : ProtoBuf

    @Serializable
    class ContentHead(
        @SerialId(1) val pkgNum: Int = 1,
        @SerialId(2) val pkgIndex: Int = 0,
        @SerialId(3) val seq: Int = 0,
        @SerialId(4) val dateTime: Int = 0,
        @SerialId(5) val msgType: Int = 0,
        @SerialId(6) val divSeq: Int = 0,
        @SerialId(7) val msgdbUin: Long = 0L,
        @SerialId(8) val msgdbSeq: Int = 0,
        @SerialId(9) val wordMsgSeq: Int = 0,
        @SerialId(10) val msgRand: Int = 0
    ) : ProtoBuf

    @Serializable
    class Group(
        @SerialId(1) val sender: ImCommon.User? = null,
        @SerialId(2) val receiver: ImCommon.User? = null,
        @SerialId(3) val groupInfo: ImCommon.GroupInfo? = null
    ) : ProtoBuf

    @Serializable
    class Msg(
        @SerialId(1) val head: MsgHead? = null,
        @SerialId(2) val body: ImMsgBody.MsgBody? = null
    ) : ProtoBuf

    @Serializable
    class MsgHead(
        @SerialId(1) val routingHead: RoutingHead? = null,
        @SerialId(2) val contentHead: ContentHead? = null,
        @SerialId(3) val gbkTmpMsgBody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MsgSendReq(
        @SerialId(1) val msg: Msg? = null,
        @SerialId(2) val buMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val msgTailId: Int = 0,
        @SerialId(4) val connMsgFlag: Int = 0,
        @SerialId(5) val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MsgSendResp

    @Serializable
    class RoutingHead(
        @SerialId(1) val c2c: C2C? = null,
        @SerialId(2) val group: Group? = null
    ) : ProtoBuf
}

@Serializable
class ImMsgBody : ProtoBuf {
    @Serializable
    class AnonymousGroupMsg(
        @SerialId(1) val flags: Int = 0,
        @SerialId(2) val anonId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val anonNick: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val headPortrait: Int = 0,
        @SerialId(5) val expireTime: Int = 0,
        @SerialId(6) val bubbleId: Int = 0,
        @SerialId(7) val rankColor: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ApolloActMsg(
        @SerialId(1) val actionId: Int = 0,
        @SerialId(2) val actionName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val actionText: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val flag: Int = 0,
        @SerialId(5) val peerUin: Int = 0,
        @SerialId(6) val senderTs: Int = 0,
        @SerialId(7) val peerTs: Int = 0,
        @SerialId(8) val int32SenderStatus: Int = 0,
        @SerialId(9) val int32PeerStatus: Int = 0,
        @SerialId(10) val diytextId: Int = 0,
        @SerialId(11) val diytextContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val inputText: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(13) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ArkAppElem(
        @SerialId(1) val appName: String = "",
        @SerialId(2) val minVersion: String = "",
        @SerialId(3) val xmlTemplate: String = "",
        @SerialId(4) val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class Attr(
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(1) val codePage: Int = -1,
        @SerialId(2) val time: Int = 1,
        @SerialId(3) val random: Int = 0,
        @SerialId(4) val color: Int = 0,
        @SerialId(5) val size: Int = 10,
        @SerialId(6) val effect: Int = 7,
        @SerialId(7) val charSet: Int = 78,
        @SerialId(8) val pitchAndFamily: Int = 90,
        @SerialId(9) val fontName: String = "Times New Roman",
        @SerialId(10) val reserveData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class BitAppMsg(
        @SerialId(1) val buf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class BlessingMessage(
        @SerialId(1) val msgType: Int = 0,
        @SerialId(2) val exFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class CommonElem(
        @SerialId(1) val serviceType: Int = 0,
        @SerialId(2) val pbElem: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val businessType: Int = 0
    ) : ProtoBuf

    @Serializable
    class ConferenceTipsInfo(
        @SerialId(1) val sessionType: Int = 0,
        @SerialId(2) val sessionUin: Long = 0L,
        @SerialId(3) val text: String = ""
    ) : ProtoBuf

    @Serializable
    class CrmElem(
        @SerialId(1) val crmBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val qidianFlag: Int = 0,
        @SerialId(4) val pushFlag: Int = 0,
        @SerialId(5) val countFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class CustomElem(
        @SerialId(1) val desc: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val data: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val enumType: Int /* enum */ = 1,
        @SerialId(4) val ext: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val sound: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class CustomFace(
        @SerialId(1) val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val filePath: String = "",
        @SerialId(3) val shortcut: String = "",
        @SerialId(4) val buffer: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val oldData: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val fileId: Int = 0,
        @SerialId(8) val serverIp: Int = 0,
        @SerialId(9) val serverPort: Int = 0,
        @SerialId(10) val fileType: Int = 0,
        @SerialId(11) val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val useful: Int = 0,
        @SerialId(13) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(14) val thumbUrl: String = "",
        @SerialId(15) val bigUrl: String = "",
        @SerialId(16) val origUrl: String = "",
        @SerialId(17) val bizType: Int = 0,
        @SerialId(18) val repeatIndex: Int = 0,
        @SerialId(19) val repeatImage: Int = 0,
        @SerialId(20) val imageType: Int = 0,
        @SerialId(21) val index: Int = 0,
        @SerialId(22) val width: Int = 0,
        @SerialId(23) val height: Int = 0,
        @SerialId(24) val source: Int = 0,
        @SerialId(25) val size: Int = 0,
        @SerialId(26) val origin: Int = 0,
        @SerialId(27) val thumbWidth: Int = 0,
        @SerialId(28) val thumbHeight: Int = 0,
        @SerialId(29) val showLen: Int = 0,
        @SerialId(30) val downloadLen: Int = 0,
        @SerialId(31) val _400Url: String = "",
        @SerialId(32) val _400Width: Int = 0,
        @SerialId(33) val _400Height: Int = 0,
        @SerialId(34) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class DeliverGiftMsg(
        @SerialId(1) val grayTipContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val animationPackageId: Int = 0,
        @SerialId(3) val animationPackageUrlA: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val animationPackageUrlI: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val remindBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val giftId: Int = 0,
        @SerialId(7) val giftCount: Int = 0,
        @SerialId(8) val animationBrief: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val senderUin: Long = 0L,
        @SerialId(10) val receiverUin: Long = 0L,
        @SerialId(11) val stmessageTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val stmessageSubtitle: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(13) val stmessageMessage: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(14) val stmessageGiftpicid: Int = 0,
        @SerialId(15) val stmessageComefrom: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(16) val stmessageExflag: Int = 0,
        @SerialId(17) val toAllGiftId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(18) val comefromLink: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(19) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(20) val receiverName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(21) val receiverPic: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(22) val stmessageGifturl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class EIMInfo(
        @SerialId(1) val rootId: Long = 0L,
        @SerialId(2) val flag: Int = 0
    ) : ProtoBuf

    @Serializable
    class Elem(
        @SerialId(1) val text: Text? = null,
        @SerialId(2) val face: Face? = null,
        @SerialId(3) val onlineImage: OnlineImage? = null,
        @SerialId(4) val notOnlineImage: NotOnlineImage? = null,
        @SerialId(5) val transElemInfo: TransElem? = null,
        @SerialId(6) val marketFace: MarketFace? = null,
        @SerialId(7) val elemFlags: ElemFlags? = null,
        @SerialId(8) val customFace: CustomFace? = null,
        @SerialId(9) val elemFlags2: ElemFlags2? = null,
        @SerialId(10) val funFace: FunFace? = null,
        @SerialId(11) val secretFile: SecretFileMsg? = null,
        @SerialId(12) val richMsg: RichMsg? = null,
        @SerialId(13) val groupFile: GroupFile? = null,
        @SerialId(14) val pubGroup: PubGroup? = null,
        @SerialId(15) val marketTrans: MarketTrans? = null,
        @SerialId(16) val extraInfo: ExtraInfo? = null,
        @SerialId(17) val shakeWindow: ShakeWindow? = null,
        @SerialId(18) val pubAccount: PubAccount? = null,
        @SerialId(19) val videoFile: VideoFile? = null,
        @SerialId(20) val tipsInfo: TipsInfo? = null,
        @SerialId(21) val anonGroupMsg: AnonymousGroupMsg? = null,
        @SerialId(22) val qqLiveOld: QQLiveOld? = null,
        @SerialId(23) val lifeOnline: LifeOnlineAccount? = null,
        @SerialId(24) val qqwalletMsg: QQWalletMsg? = null,
        @SerialId(25) val crmElem: CrmElem? = null,
        @SerialId(26) val conferenceTipsInfo: ConferenceTipsInfo? = null,
        @SerialId(27) val redbagInfo: RedBagInfo? = null,
        @SerialId(28) val lowVersionTips: LowVersionTips? = null,
        @SerialId(29) val bankcodeCtrlInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(30) val nearByMsg: NearByMessageType? = null,
        @SerialId(31) val customElem: CustomElem? = null,
        @SerialId(32) val locationInfo: LocationInfo? = null,
        @SerialId(33) val pubAccInfo: PubAccInfo? = null,
        @SerialId(34) val smallEmoji: SmallEmoji? = null,
        @SerialId(35) val fsjMsgElem: FSJMessageElem? = null,
        @SerialId(36) val arkApp: ArkAppElem? = null,
        @SerialId(37) val generalFlags: GeneralFlags? = null,
        @SerialId(38) val hcFlashPic: CustomFace? = null,
        @SerialId(39) val deliverGiftMsg: DeliverGiftMsg? = null,
        @SerialId(40) val bitappMsg: BitAppMsg? = null,
        @SerialId(41) val openQqData: OpenQQData? = null,
        @SerialId(42) val apolloMsg: ApolloActMsg? = null,
        @SerialId(43) val groupPubAccInfo: GroupPubAccountInfo? = null,
        @SerialId(44) val blessMsg: BlessingMessage? = null,
        @SerialId(45) val srcMsg: SourceMsg? = null,
        @SerialId(46) val lolaMsg: LolaMsg? = null,
        @SerialId(47) val groupBusinessMsg: GroupBusinessMsg? = null,
        @SerialId(48) val msgWorkflowNotify: WorkflowNotifyMsg? = null,
        @SerialId(49) val patElem: PatsElem? = null,
        @SerialId(50) val groupPostElem: GroupPostElem? = null,
        @SerialId(51) val lightApp: LightAppElem? = null,
        @SerialId(52) val eimInfo: EIMInfo? = null,
        @SerialId(53) val commonElem: CommonElem? = null
    ) : ProtoBuf

    @Serializable
    class ElemFlags(
        @SerialId(1) val flags1: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val businessData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ElemFlags2(
        @SerialId(1) val colorTextId: Int = 0,
        @SerialId(2) val msgId: Long = 0L,
        @SerialId(3) val whisperSessionId: Int = 0,
        @SerialId(4) val pttChangeBit: Int = 0,
        @SerialId(5) val vipStatus: Int = 0,
        @SerialId(6) val compatibleId: Int = 0,
        @SerialId(7) val insts: List<Inst>? = null,
        @SerialId(8) val msgRptCnt: Int = 0,
        @SerialId(9) val srcInst: Inst? = null,
        @SerialId(10) val longtitude: Int = 0,
        @SerialId(11) val latitude: Int = 0,
        @SerialId(12) val customFont: Int = 0,
        @SerialId(13) val pcSupportDef: PcSupportDef? = null,
        @SerialId(14) val crmFlags: Int = 0
    ) : ProtoBuf {
        @Serializable
        class Inst(
            @SerialId(1) val appId: Int = 0,
            @SerialId(2) val instId: Int = 0
        )
    }

    @Serializable
    class ExtraInfo(
        @SerialId(1) val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val groupCard: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val level: Int = 0,
        @SerialId(4) val flags: Int = 0,
        @SerialId(5) val groupMask: Int = 0,
        @SerialId(6) val msgTailId: Int = 0,
        @SerialId(7) val senderTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val apnsTips: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val uin: Long = 0L,
        @SerialId(10) val msgStateFlag: Int = 0,
        @SerialId(11) val apnsSoundType: Int = 0,
        @SerialId(12) val newGroupFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class Face(
        @SerialId(1) val index: Int = 0,
        @SerialId(2) val old: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val buf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class FSJMessageElem(
        @SerialId(1) val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    class FunFace(
        @SerialId(1) val msgTurntable: Turntable? = null,
        @SerialId(2) val msgBomb: Bomb? = null
    ) {
        @Serializable
        class Bomb(
            @SerialId(1) val boolBurst: Boolean = false
        )

        @Serializable
        class Turntable(
            @SerialId(1) val uint64UinList: List<Long>? = null,
            @SerialId(2) val hitUin: Long = 0L,
            @SerialId(3) val hitUinNick: String = ""
        )
    }

    @Serializable
    class GeneralFlags(
        @SerialId(1) val bubbleDiyTextId: Int = 0,
        @SerialId(2) val groupFlagNew: Int = 0,
        @SerialId(3) val uin: Long = 0L,
        @SerialId(4) val rpId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val prpFold: Int = 0,
        @SerialId(6) val longTextFlag: Int = 0,
        @SerialId(7) val longTextResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val groupType: Int = 0,
        @SerialId(9) val toUinFlag: Int = 0,
        @SerialId(10) val glamourLevel: Int = 0,
        @SerialId(11) val memberLevel: Int = 0,
        @SerialId(12) val groupRankSeq: Long = 0L,
        @SerialId(13) val olympicTorch: Int = 0,
        @SerialId(14) val babyqGuideMsgCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(15) val uin32ExpertFlag: Int = 0,
        @SerialId(16) val bubbleSubId: Int = 0,
        @SerialId(17) val pendantId: Long = 0L,
        @SerialId(18) val rpIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(19) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GroupBusinessMsg(
        @SerialId(1) val flags: Int = 0,
        @SerialId(2) val headUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val headClkUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val nickColor: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val rank: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val rankColor: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val rankBgcolor: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GroupFile(
        @SerialId(1) val filename: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fileSize: Long = 0L,
        @SerialId(3) val fileId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val batchId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val mark: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val sequence: Long = 0L,
        @SerialId(8) val batchItemId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val feedMsgTime: Int = 0,
        @SerialId(10) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GroupPostElem(
        @SerialId(1) val transType: Int = 0,
        @SerialId(2) val transMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GroupPubAccountInfo(
        @SerialId(1) val pubAccount: Long = 0L
    ) : ProtoBuf

    @Serializable
    class LifeOnlineAccount(
        @SerialId(1) val uniqueId: Long = 0L,
        @SerialId(2) val op: Int = 0,
        @SerialId(3) val showTime: Int = 0,
        @SerialId(4) val report: Int = 0,
        @SerialId(5) val ack: Int = 0,
        @SerialId(6) val bitmap: Long = 0L,
        @SerialId(7) val gdtImpData: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val gdtCliData: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val viewId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class LightAppElem(
        @SerialId(1) val data: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class LocationInfo(
        @SerialId(1) val longitude: Double = 0.0,
        @SerialId(2) val latitude: Double = 0.0,
        @SerialId(3) val desc: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class LolaMsg(
        @SerialId(1) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val encodeContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val longMsgUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val downloadKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class LowVersionTips(
        @SerialId(1) val businessId: Int = 0,
        @SerialId(2) val sessionType: Int = 0,
        @SerialId(3) val sessionUin: Long = 0L,
        @SerialId(4) val senderUin: Long = 0L,
        @SerialId(5) val text: String = ""
    ) : ProtoBuf

    @Serializable
    class MarketFace(
        @SerialId(1) val faceName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val itemType: Int = 0,
        @SerialId(3) val faceInfo: Int = 0,
        @SerialId(4) val faceId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val tabId: Int = 0,
        @SerialId(6) val subType: Int = 0,
        @SerialId(7) val key: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val param: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val mediaType: Int = 0,
        @SerialId(10) val imageWidth: Int = 0,
        @SerialId(11) val imageHeight: Int = 0,
        @SerialId(12) val mobileparam: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(13) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MarketTrans(
        @SerialId(1) val int32Flag: Int = 0,
        @SerialId(2) val xml: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val ability: Int = 0,
        @SerialId(5) val minAbility: Int = 0
    ) : ProtoBuf

    @Serializable
    class MsgBody(
        @SerialId(1) val richText: RichText = RichText(),
        @SerialId(2) val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val msgEncryptContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MsgBodySubtype4(
        @SerialId(1) val msgNotOnlineFile: NotOnlineFile? = null,
        @SerialId(2) val msgTime: Int = 0
    ) : ProtoBuf

    @Serializable
    class NearByMessageType(
        @SerialId(1) val type: Int = 0,
        @SerialId(2) val identifyType: Int = 0
    ) : ProtoBuf

    @Serializable
    class NotOnlineFile(
        @SerialId(1) val fileType: Int = 0,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val fileSize: Long = 0L,
        @SerialId(7) val note: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val reserved: Int = 0,
        @SerialId(9) val subcmd: Int = 0,
        @SerialId(10) val microCloud: Int = 0,
        @SerialId(11) val bytesFileUrls: List<ByteArray>? = null,
        @SerialId(12) val downloadFlag: Int = 0,
        @SerialId(50) val dangerEvel: Int = 0,
        @SerialId(51) val lifeTime: Int = 0,
        @SerialId(52) val uploadTime: Int = 0,
        @SerialId(53) val absFileType: Int = 0,
        @SerialId(54) val clientType: Int = 0,
        @SerialId(55) val expireTime: Int = 0,
        @SerialId(56) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class NotOnlineImage(
        @SerialId(1) val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fileLen: Int = 0,
        @SerialId(3) val downloadPath: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val imgType: Int = 0,
        @SerialId(6) val previewsImage: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val picMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val picHeight: Int = 0,
        @SerialId(9) val picWidth: Int = 0,
        @SerialId(10) val resId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val thumbUrl: String = "",
        @SerialId(13) val original: Int = 0,
        @SerialId(14) val bigUrl: String = "",
        @SerialId(15) val origUrl: String = "",
        @SerialId(16) val bizType: Int = 0,
        @SerialId(17) val result: Int = 0,
        @SerialId(18) val index: Int = 0,
        @SerialId(19) val opFaceBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(20) val oldPicMd5: Boolean = false,
        @SerialId(21) val thumbWidth: Int = 0,
        @SerialId(22) val thumbHeight: Int = 0,
        @SerialId(23) val fileId: Int = 0,
        @SerialId(24) val showLen: Int = 0,
        @SerialId(25) val downloadLen: Int = 0,
        @SerialId(26) val _400Url: String = "",
        @SerialId(27) val _400Width: Int = 0,
        @SerialId(28) val _400Height: Int = 0,
        @SerialId(29) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class OnlineImage(
        @SerialId(1) val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class OpenQQData(
        @SerialId(1) val carQqData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PatsElem(
        @SerialId(1) val patType: Int = 0,
        @SerialId(2) val patCount: Int = 0
    ) : ProtoBuf

    @Serializable
    class PcSupportDef(
        @SerialId(1) val pcPtlBegin: Int = 0,
        @SerialId(2) val pcPtlEnd: Int = 0,
        @SerialId(3) val macPtlBegin: Int = 0,
        @SerialId(4) val macPtlEnd: Int = 0,
        @SerialId(5) val ptlsSupport: List<Int>? = null,
        @SerialId(6) val ptlsNotSupport: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    class Ptt(
        @SerialId(1) val fileType: Int = 0,
        @SerialId(2) val srcUin: Long = 0L,
        @SerialId(3) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val fileSize: Int = 0,
        @SerialId(7) val reserve: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val fileId: Int = 0,
        @SerialId(9) val serverIp: Int = 0,
        @SerialId(10) val serverPort: Int = 0,
        @SerialId(11) val boolValid: Boolean = false,
        @SerialId(12) val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(13) val shortcut: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(14) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(15) val magicPttIndex: Int = 0,
        @SerialId(16) val voiceSwitch: Int = 0,
        @SerialId(17) val pttUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(18) val groupFileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(19) val time: Int = 0,
        @SerialId(20) val downPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(29) val format: Int = 0,
        @SerialId(30) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(31) val bytesPttUrls: List<ByteArray>? = null,
        @SerialId(32) val downloadFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class PubAccInfo(
        @SerialId(1) val isInterNum: Int = 0,
        @SerialId(2) val ingMsgTemplateId: String = "",
        @SerialId(3) val ingLongMsgUrl: String = "",
        @SerialId(4) val downloadKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PubAccount(
        @SerialId(1) val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val pubAccountUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class PubGroup(
        @SerialId(1) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val gender: Int = 0,
        @SerialId(3) val age: Int = 0,
        @SerialId(4) val distance: Int = 0
    ) : ProtoBuf

    @Serializable
    class QQLiveOld(
        @SerialId(1) val subCmd: Int = 0,
        @SerialId(2) val showText: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val param: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val introduce: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class QQWalletAioBody(
        @SerialId(1) val senduin: Long = 0L,
        @SerialId(2) val sender: QQWalletAioElem? = null,
        @SerialId(3) val receiver: QQWalletAioElem? = null,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(4) val sint32Channelid: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(5) val sint32Templateid: Int = 0,
        @SerialId(6) val resend: Int = 0,
        @SerialId(7) val msgPriority: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(8) val sint32Redtype: Int = 0,
        @SerialId(9) val billno: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(11) val sint32Sessiontype: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(12) val sint32Msgtype: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(13) val sint32Envelopeid: Int = 0,
        @SerialId(14) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(15) val sint32Conftype: Int = 0,
        @ProtoType(ProtoNumberType.SIGNED) @SerialId(16) val sint32MsgFrom: Int = 0,
        @SerialId(17) val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(18) val ingIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(19) val redchannel: Int = 0,
        @SerialId(20) val grapUin: List<Long>? = null,
        @SerialId(21) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class QQWalletAioElem(
        @SerialId(1) val background: Int = 0,
        @SerialId(2) val icon: Int = 0,
        @SerialId(3) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val subtitle: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val content: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val linkurl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val blackstripe: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val notice: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val titleColor: Int = 0,
        @SerialId(10) val subtitleColor: Int = 0,
        @SerialId(11) val actionsPriority: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(13) val nativeIos: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(14) val nativeAndroid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(15) val iconurl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(16) val contentColor: Int = 0,
        @SerialId(17) val contentBgcolor: Int = 0,
        @SerialId(18) val aioImageLeft: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(19) val aioImageRight: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(20) val cftImage: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(21) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class QQWalletMsg(
        @SerialId(1) val aioBody: QQWalletAioBody? = null
    ) : ProtoBuf

    @Serializable
    class RedBagInfo(
        @SerialId(1) val redbagType: Int = 0
    ) : ProtoBuf

    @Serializable
    class RichMsg(
        @SerialId(1) val template1: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val serviceId: Int = 0,
        @SerialId(3) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val rand: Int = 0,
        @SerialId(5) val seq: Int = 0,
        @SerialId(6) val flags: Int = 0
    ) : ProtoBuf

    @Serializable
    class RichText(
        @SerialId(1) val attr: Attr? = null,
        @SerialId(2) val elems: MutableList<Elem> = mutableListOf(),
        @SerialId(3) val notOnlineFile: NotOnlineFile? = null,
        @SerialId(4) val ptt: Ptt? = null,
        @SerialId(5) val tmpPtt: TmpPtt? = null,
        @SerialId(6) val trans211TmpMsg: Trans211TmpMsg? = null
    ) : ProtoBuf

    @Serializable
    class SecretFileMsg(
        @SerialId(1) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fromUin: Long = 0L,
        @SerialId(3) val toUin: Long = 0L,
        @SerialId(4) val status: Int = 0,
        @SerialId(5) val ttl: Int = 0,
        @SerialId(6) val type: Int = 0,
        @SerialId(7) val encryptPreheadLength: Int = 0,
        @SerialId(8) val encryptType: Int = 0,
        @SerialId(9) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val readTimes: Int = 0,
        @SerialId(11) val leftTime: Int = 0,
        @SerialId(12) val notOnlineImage: NotOnlineImage? = null,
        @SerialId(13) val elemFlags2: ElemFlags2? = null,
        @SerialId(14) val opertype: Int = 0,
        @SerialId(15) val fromphonenum: String = ""
    ) : ProtoBuf

    @Serializable
    class ShakeWindow(
        @SerialId(1) val type: Int = 0,
        @SerialId(2) val reserve: Int = 0,
        @SerialId(3) val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class SmallEmoji(
        @SerialId(1) val packIdSum: Int = 0,
        @SerialId(2) val imageType: Int = 0
    ) : ProtoBuf

    @Serializable
    class SourceMsg(
        @SerialId(1) val origSeqs: List<Int>? = null,
        @SerialId(2) val senderUin: Long = 0L,
        @SerialId(3) val time: Int = 0,
        @SerialId(4) val flag: Int = 0,
        @SerialId(5) val elems: List<Elem>? = null,
        @SerialId(6) val type: Int = 0,
        @SerialId(7) val richMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val srcMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val toUin: Long = 0L,
        @SerialId(11) val troopName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class Text(
        @SerialId(1) val str: String = "",
        @SerialId(2) val link: String = "",
        @SerialId(3) val attr6Buf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val attr7Buf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val buf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class TipsInfo(
        @SerialId(1) val text: String = ""
    ) : ProtoBuf

    @Serializable
    class TmpPtt(
        @SerialId(1) val fileType: Int = 0,
        @SerialId(2) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileSize: Int = 0,
        @SerialId(6) val pttTimes: Int = 0,
        @SerialId(7) val userType: Int = 0,
        @SerialId(8) val ptttransFlag: Int = 0,
        @SerialId(9) val busiType: Int = 0,
        @SerialId(10) val msgId: Long = 0L,
        @SerialId(30) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(31) val pttEncodeData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class Trans211TmpMsg(
        @SerialId(1) val msgBody: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val c2cCmd: Int = 0
    ) : ProtoBuf

    @Serializable
    class TransElem(
        @SerialId(1) val elemType: Int = 0,
        @SerialId(2) val elemValue: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class VideoFile(
        @SerialId(1) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val fileFormat: Int = 0,
        @SerialId(5) val fileTime: Int = 0,
        @SerialId(6) val fileSize: Int = 0,
        @SerialId(7) val thumbWidth: Int = 0,
        @SerialId(8) val thumbHeight: Int = 0,
        @SerialId(9) val thumbFileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val source: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val thumbFileSize: Int = 0,
        @SerialId(12) val busiType: Int = 0,
        @SerialId(13) val fromChatType: Int = 0,
        @SerialId(14) val toChatType: Int = 0,
        @SerialId(15) val boolSupportProgressive: Boolean = false,
        @SerialId(16) val fileWidth: Int = 0,
        @SerialId(17) val fileHeight: Int = 0,
        @SerialId(18) val subBusiType: Int = 0,
        @SerialId(19) val videoAttr: Int = 0,
        @SerialId(20) val bytesThumbFileUrls: List<ByteArray>? = null,
        @SerialId(21) val bytesVideoFileUrls: List<ByteArray>? = null,
        @SerialId(22) val thumbDownloadFlag: Int = 0,
        @SerialId(23) val videoDownloadFlag: Int = 0,
        @SerialId(24) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class WorkflowNotifyMsg(
        @SerialId(1) val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val createUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
class ImMsgHead : ProtoBuf {
    @Serializable
    class C2CHead(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val fromUin: Long = 0L,
        @SerialId(3) val ccType: Int = 0,
        @SerialId(4) val ccCmd: Int = 0,
        @SerialId(5) val authPicSig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val authSig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val authBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val serverTime: Int = 0,
        @SerialId(9) val clientTime: Int = 0,
        @SerialId(10) val rand: Int = 0,
        @SerialId(11) val ingPhoneNumber: String = ""
    ) : ProtoBuf

    @Serializable
    class CSHead(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val command: Int = 0,
        @SerialId(3) val seq: Int = 0,
        @SerialId(4) val version: Int = 0,
        @SerialId(5) val retryTimes: Int = 0,
        @SerialId(6) val clientType: Int = 0,
        @SerialId(7) val pubno: Int = 0,
        @SerialId(8) val localid: Int = 0,
        @SerialId(9) val timezone: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(10) val clientIp: Int = 0,
        @SerialId(11) val clientPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(12) val connIp: Int = 0,
        @SerialId(13) val connPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(14) val interfaceIp: Int = 0,
        @SerialId(15) val interfacePort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(16) val actualIp: Int = 0,
        @SerialId(17) val flag: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(18) val timestamp: Int = 0,
        @SerialId(19) val subcmd: Int = 0,
        @SerialId(20) val result: Int = 0,
        @SerialId(21) val appId: Int = 0,
        @SerialId(22) val instanceId: Int = 0,
        @SerialId(23) val sessionId: Long = 0L,
        @SerialId(24) val idcId: Int = 0
    ) : ProtoBuf

    @Serializable
    class DeltaHead(
        @SerialId(1) val totalLen: Long = 0L,
        @SerialId(2) val offset: Long = 0L,
        @SerialId(3) val ackOffset: Long = 0L,
        @SerialId(4) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val ackCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val result: Int = 0,
        @SerialId(7) val flags: Int = 0
    ) : ProtoBuf

    @Serializable
    class Head(
        @SerialId(1) val headType: Int = 0,
        @SerialId(2) val msgCsHead: CSHead? = null,
        @SerialId(3) val msgS2cHead: S2CHead? = null,
        @SerialId(4) val msgHttpconnHead: HttpConnHead? = null,
        @SerialId(5) val paintFlag: Int = 0,
        @SerialId(6) val msgLoginSig: LoginSig? = null,
        @SerialId(7) val msgDeltaHead: DeltaHead? = null,
        @SerialId(8) val msgC2cHead: C2CHead? = null,
        @SerialId(9) val msgSconnHead: SConnHead? = null,
        @SerialId(10) val msgInstCtrl: InstCtrl? = null
    ) : ProtoBuf

    @Serializable
    class HttpConnHead(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val command: Int = 0,
        @SerialId(3) val subCommand: Int = 0,
        @SerialId(4) val seq: Int = 0,
        @SerialId(5) val version: Int = 0,
        @SerialId(6) val retryTimes: Int = 0,
        @SerialId(7) val clientType: Int = 0,
        @SerialId(8) val pubNo: Int = 0,
        @SerialId(9) val localId: Int = 0,
        @SerialId(10) val timeZone: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(11) val clientIp: Int = 0,
        @SerialId(12) val clientPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(13) val qzhttpIp: Int = 0,
        @SerialId(14) val qzhttpPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(15) val sppIp: Int = 0,
        @SerialId(16) val sppPort: Int = 0,
        @SerialId(17) val flag: Int = 0,
        @SerialId(18) val key: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(19) val compressType: Int = 0,
        @SerialId(20) val originSize: Int = 0,
        @SerialId(21) val errorCode: Int = 0,
        @SerialId(22) val msgRedirect: RedirectMsg? = null,
        @SerialId(23) val commandId: Int = 0,
        @SerialId(24) val serviceCmdid: Int = 0,
        @SerialId(25) val msgOidbhead: TransOidbHead? = null
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
        @SerialId(10) val enumDeviceType: Int /* enum */ = 0
    ) : ProtoBuf

    @Serializable
    class LoginSig(
        @SerialId(1) val type: Int = 0,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class RedirectMsg(
        @ProtoType(ProtoNumberType.FIXED) @SerialId(1) val lastRedirectIp: Int = 0,
        @SerialId(2) val lastRedirectPort: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(3) val redirectIp: Int = 0,
        @SerialId(4) val redirectPort: Int = 0,
        @SerialId(5) val redirectCount: Int = 0
    ) : ProtoBuf

    @Serializable
    class S2CHead(
        @SerialId(1) val subMsgtype: Int = 0,
        @SerialId(2) val msgType: Int = 0,
        @SerialId(3) val fromUin: Long = 0L,
        @SerialId(4) val msgId: Int = 0,
        @ProtoType(ProtoNumberType.FIXED) @SerialId(5) val relayIp: Int = 0,
        @SerialId(6) val relayPort: Int = 0,
        @SerialId(7) val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class SConnHead : ProtoBuf

    @Serializable
    class TransOidbHead(
        @SerialId(1) val command: Int = 0,
        @SerialId(2) val serviceType: Int = 0,
        @SerialId(3) val result: Int = 0,
        @SerialId(4) val errorMsg: String = ""
    ) : ProtoBuf
}

@Serializable
class ImReceipt : ProtoBuf {
    @Serializable
    class MsgInfo(
        @SerialId(1) val fromUin: Long = 0L,
        @SerialId(2) val toUin: Long = 0L,
        @SerialId(3) val msgSeq: Int = 0,
        @SerialId(4) val msgRandom: Int = 0
    ) : ProtoBuf

    @Serializable
    class ReceiptInfo(
        @SerialId(1) val readTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    class ReceiptReq(
        @SerialId(1) val command: Int /* enum */ = 1,
        @SerialId(2) val msgInfo: MsgInfo? = null
    ) : ProtoBuf

    @Serializable
    class ReceiptResp(
        @SerialId(1) val command: Int /* enum */ = 1,
        @SerialId(2) val receiptInfo: ReceiptInfo? = null
    ) : ProtoBuf
}

@Serializable
class ObjMsg : ProtoBuf {
    @Serializable
    class MsgContentInfo(
        @SerialId(1) val contentInfoId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val msgFile: MsgFile? = null
    ) : ProtoBuf {
        @Serializable
        class MsgFile(
            @SerialId(1) val busId: Int = 0,
            @SerialId(2) val filePath: ByteArray = EMPTY_BYTE_ARRAY,
            @SerialId(3) val fileSize: Long = 0L,
            @SerialId(4) val fileName: String = "",
            @SerialId(5) val int64DeadTime: Long = 0L,
            @SerialId(6) val fileSha1: ByteArray = EMPTY_BYTE_ARRAY,
            @SerialId(7) val ext: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    class MsgPic(
        @SerialId(1) val smallPicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val originalPicUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val localPicId: Int = 0
    ) : ProtoBuf

    @Serializable
    class ObjMsg(
        @SerialId(1) val msgType: Int = 0,
        @SerialId(2) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val bytesAbstact: List<ByteArray>? = null,
        @SerialId(5) val titleExt: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val msgPic: List<MsgPic>? = null,
        @SerialId(7) val msgContentInfo: List<MsgContentInfo>? = null,
        @SerialId(8) val reportIdShow: Int = 0
    ) : ProtoBuf
}