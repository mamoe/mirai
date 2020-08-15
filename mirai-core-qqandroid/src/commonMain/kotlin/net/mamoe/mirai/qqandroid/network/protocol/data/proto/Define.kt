package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class Common : ProtoBuf {
    @Serializable
    internal class BindInfo(
        @ProtoId(1) @JvmField val friUin: Long = 0L,
        @ProtoId(2) @JvmField val friNick: String = "",
        @ProtoId(3) @JvmField val time: Long = 0L,
        @ProtoId(4) @JvmField val bindStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalInfo(
        @ProtoId(1) @JvmField val id: Int = 0,
        @ProtoId(2) @JvmField val type: Int = 0,
        @ProtoId(4) @JvmField val seq: Long = 0,
        @ProtoId(5) @JvmField val name: String = "",
        @ProtoId(6) @JvmField val newflag: Int = 0,
        @ProtoId(7) @JvmField val time: Long = 0L,
        @ProtoId(8) @JvmField val msgBindFri: BindInfo? = null,
        @ProtoId(11) @JvmField val desc: String = "",
        @ProtoId(31) @JvmField val level: Int = 0,
        @ProtoId(36) @JvmField val taskinfos: List<MedalTaskInfo>? = null,
        @ProtoId(40) @JvmField val point: Int = 0,
        @ProtoId(41) @JvmField val pointLevel2: Int = 0,
        @ProtoId(42) @JvmField val pointLevel3: Int = 0,
        @ProtoId(43) @JvmField val seqLevel2: Long = 0,
        @ProtoId(44) @JvmField val seqLevel3: Long = 0,
        @ProtoId(45) @JvmField val timeLevel2: Long = 0L,
        @ProtoId(46) @JvmField val timeLevel3: Long = 0L,
        @ProtoId(47) @JvmField val descLevel2: String = "",
        @ProtoId(48) @JvmField val descLevel3: String = "",
        @ProtoId(49) @JvmField val endtime: Int = 0,
        @ProtoId(50) @JvmField val detailUrl: String = "",
        @ProtoId(51) @JvmField val detailUrl2: String = "",
        @ProtoId(52) @JvmField val detailUrl3: String = "",
        @ProtoId(53) @JvmField val taskDesc: String = "",
        @ProtoId(54) @JvmField val taskDesc2: String = "",
        @ProtoId(55) @JvmField val taskDesc3: String = "",
        @ProtoId(56) @JvmField val levelCount: Int = 0,
        @ProtoId(57) @JvmField val noProgress: Int = 0,
        @ProtoId(58) @JvmField val resource: String = "",
        @ProtoId(59) @JvmField val fromuinLevel: Int = 0,
        @ProtoId(60) @JvmField val unread: Int = 0,
        @ProtoId(61) @JvmField val unread2: Int = 0,
        @ProtoId(62) @JvmField val unread3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalTaskInfo(
        @ProtoId(1) @JvmField val taskid: Int = 0,
        @ProtoId(32) @JvmField val int32TaskValue: Int = 0,
        @ProtoId(33) @JvmField val tarValue: Int = 0,
        @ProtoId(34) @JvmField val tarValueLevel2: Int = 0,
        @ProtoId(35) @JvmField val tarValueLevel3: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class AppointDefine : ProtoBuf {
    @Serializable
    internal class ADFeedContent(
        @ProtoId(1) @JvmField val msgUserInfo: UserInfo? = null,
        @ProtoId(2) @JvmField val strPicUrl: List<String> = listOf(),
        @ProtoId(3) @JvmField val msgText: RichText? = null,
        @ProtoId(4) @JvmField val attendInfo: String = "",
        @ProtoId(5) @JvmField val actionUrl: String = "",
        @ProtoId(6) @JvmField val publishTime: Int = 0,
        @ProtoId(7) @JvmField val msgHotTopicList: HotTopicList? = null,
        @ProtoId(8) @JvmField val moreUrl: String = "",
        @ProtoId(9) @JvmField val recordDuration: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RichText(
        @ProtoId(1) @JvmField val msgElems: List<Elem>? = null
    ) : ProtoBuf

    @Serializable
    internal class RankEvent(
        @ProtoId(1) @JvmField val listtype: Int = 0,
        @ProtoId(2) @JvmField val notifytype: Int = 0,
        @ProtoId(3) @JvmField val eventtime: Int = 0,
        @ProtoId(4) @JvmField val seq: Int = 0,
        @ProtoId(5) @JvmField val notifyTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Wifi(
        @ProtoId(1) @JvmField val mac: Long = 0L,
        @ProtoId(2) @JvmField val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class InterestItem(
        @ProtoId(1) @JvmField val tagId: Long = 0L,
        @ProtoId(2) @JvmField val tagName: String = "",
        @ProtoId(3) @JvmField val tagIconUrl: String = "",
        @ProtoId(4) @JvmField val tagHref: String = "",
        @ProtoId(5) @JvmField val tagBackColor: String = "",
        @ProtoId(6) @JvmField val tagFontColor: String = "",
        @ProtoId(7) @JvmField val tagVid: String = "",
        @ProtoId(8) @JvmField val tagType: Int = 0,
        @ProtoId(9) @JvmField val addTime: Int = 0,
        @ProtoId(10) @JvmField val tagCategory: String = "",
        @ProtoId(11) @JvmField val tagOtherUrl: String = "",
        @ProtoId(12) @JvmField val bid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShopID(
        @ProtoId(1) @JvmField val shopid: String = "",
        @ProtoId(2) @JvmField val sp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FeedComment(
        @ProtoId(1) @JvmField val commentId: String = "",
        @ProtoId(2) @JvmField val feedId: String = "",
        @ProtoId(3) @JvmField val msgPublisherInfo: StrangerInfo? = null,
        @ProtoId(4) @JvmField val time: Int = 0,
        @ProtoId(6) @JvmField val msgReplyInfo: ReplyInfo? = null,
        @ProtoId(7) @JvmField val flag: Int = 0,
        @ProtoId(8) @JvmField val msgContent: RichText? = null,
        @ProtoId(9) @JvmField val hot: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ADFeed(
        @ProtoId(1) @JvmField val taskId: Int = 0,
        @ProtoId(2) @JvmField val style: Int = 0,
        @ProtoId(3) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Cell(
        @ProtoId(1) @JvmField val int32Mcc: Int = -1,
        @ProtoId(2) @JvmField val int32Mnc: Int = -1,
        @ProtoId(3) @JvmField val int32Lac: Int = -1,
        @ProtoId(4) @JvmField val int32Cellid: Int = -1,
        @ProtoId(5) @JvmField val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RecentVistorEvent(
        @ProtoId(1) @JvmField val eventtype: Int = 0,
        @ProtoId(2) @JvmField val eventTinyid: Long = 0L,
        @ProtoId(3) @JvmField val unreadCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class OrganizerInfo(
        @ProtoId(1) @JvmField val hostName: String = "",
        @ProtoId(2) @JvmField val hostUrl: String = "",
        @ProtoId(3) @JvmField val hostCover: String = ""
    ) : ProtoBuf

    @Serializable
    internal class InterestTag(
        @ProtoId(1) @JvmField val tagType: Int = 0,
        @ProtoId(2) @JvmField val msgTagList: List<InterestItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class AppointInfoEx(
        @ProtoId(1) @JvmField val feedsPicUrl: String = "",
        @ProtoId(2) @JvmField val feedsUrl: String = "",
        @ProtoId(3) @JvmField val detailTitle: String = "",
        @ProtoId(4) @JvmField val detailDescribe: String = "",
        @ProtoId(5) @JvmField val showPublisher: Int = 0,
        @ProtoId(6) @JvmField val detailPicUrl: String = "",
        @ProtoId(7) @JvmField val detailUrl: String = "",
        @ProtoId(8) @JvmField val showAttend: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DateComment(
        @ProtoId(1) @JvmField val commentId: String = "",
        @ProtoId(2) @JvmField val msgAppointId: AppointID? = null,
        @ProtoId(3) @JvmField val msgPublisherInfo: StrangerInfo? = null,
        @ProtoId(4) @JvmField val time: Int = 0,
        @ProtoId(6) @JvmField val msgReplyInfo: ReplyInfo? = null,
        @ProtoId(7) @JvmField val flag: Int = 0,
        @ProtoId(8) @JvmField val msgContent: RichText? = null
    ) : ProtoBuf

    @Serializable
    internal class AppointContent(
        @ProtoId(1) @JvmField val appointSubject: Int = 0,
        @ProtoId(2) @JvmField val payType: Int = 0,
        @ProtoId(3) @JvmField val appointDate: Int = 0,
        @ProtoId(4) @JvmField val appointGender: Int = 0,
        @ProtoId(5) @JvmField val appointIntroduce: String = "",
        @ProtoId(6) @JvmField val msgAppointAddress: AddressInfo? = null,
        @ProtoId(7) @JvmField val msgTravelInfo: TravelInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class FeedInfo(
        @ProtoId(1) @JvmField val feedType: Long = 0L,
        @ProtoId(2) @JvmField val feedId: String = "",
        @ProtoId(3) @JvmField val msgFeedContent: FeedContent? = null,
        @ProtoId(4) @JvmField val msgTopicInfo: NearbyTopic? = null,
        @ProtoId(5) @JvmField val publishTime: Long = 0,
        @ProtoId(6) @JvmField val praiseCount: Int = 0,
        @ProtoId(7) @JvmField val praiseFlag: Int = 0,
        @ProtoId(8) @JvmField val msgPraiseUser: List<StrangerInfo>? = null,
        @ProtoId(9) @JvmField val commentCount: Int = 0,
        @ProtoId(10) @JvmField val msgCommentList: List<FeedComment>? = null,
        @ProtoId(11) @JvmField val commentRetAll: Int = 0,
        @ProtoId(12) @JvmField val hotFlag: Int = 0,
        @ProtoId(13) @JvmField val svrReserved: Long = 0L,
        @ProtoId(14) @JvmField val msgHotEntry: HotEntry? = null
    ) : ProtoBuf

    @Serializable
    internal class HotTopicList(
        @ProtoId(1) @JvmField val topicList: List<HotTopic>? = null
    ) : ProtoBuf

    @Serializable
    internal class FeedContent(
        @ProtoId(1) @JvmField val strPicUrl: List<String> = listOf(),
        @ProtoId(2) @JvmField val msgText: RichText? = null,
        @ProtoId(3) @JvmField val hrefUrl: String = "",
        @ProtoId(5) @JvmField val groupName: String = "",
        @ProtoId(6) @JvmField val groupBulletin: String = "",
        @ProtoId(7) @JvmField val feedType: Int = 0,
        @ProtoId(8) @JvmField val poiId: String = "",
        @ProtoId(9) @JvmField val poiTitle: String = "",
        @ProtoId(20) @JvmField val effectiveTime: Int = 0,
        @ProtoId(21) @JvmField val expiationTime: Int = 0,
        @ProtoId(22) @JvmField val msgLocale: LocaleInfo? = null,
        @ProtoId(23) @JvmField val feedsIndex: Int = 0,
        @ProtoId(24) @JvmField val msgAd: ADFeed? = null,
        @ProtoId(25) @JvmField val privateData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TravelInfo(
        @ProtoId(1) @JvmField val msgDepartLocale: LocaleInfo? = null,
        @ProtoId(2) @JvmField val msgDestination: LocaleInfo? = null,
        @ProtoId(3) @JvmField val vehicle: Int = 0,
        @ProtoId(4) @JvmField val partnerCount: Int = 0,
        @ProtoId(5) @JvmField val placePicUrl: String = "",
        @ProtoId(6) @JvmField val placeUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RecentFreshFeed(
        @ProtoId(1) @JvmField val freshFeedInfo: List<FreshFeedInfo>? = null,
        @ProtoId(2) @JvmField val uid: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class GPS(
        @ProtoId(1) @JvmField val int32Lat: Int = 900000000,
        @ProtoId(2) @JvmField val int32Lon: Int = 900000000,
        @ProtoId(3) @JvmField val int32Alt: Int = -10000000,
        @ProtoId(4) @JvmField val int32Type: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AppointID(
        @ProtoId(1) @JvmField val requestId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class LocaleInfo(
        @ProtoId(1) @JvmField val name: String = "",
        @ProtoId(2) @JvmField val country: String = "",
        @ProtoId(3) @JvmField val province: String = "",
        @ProtoId(4) @JvmField val city: String = "",
        @ProtoId(5) @JvmField val region: String = "",
        @ProtoId(6) @JvmField val poi: String = "",
        @ProtoId(7) @JvmField val msgGps: GPS? = null,
        @ProtoId(8) @JvmField val address: String = ""
    ) : ProtoBuf

    @Serializable
    internal class LBSInfo(
        @ProtoId(1) @JvmField val msgGps: GPS? = null,
        @ProtoId(2) @JvmField val msgWifis: List<Wifi>? = null,
        @ProtoId(3) @JvmField val msgCells: List<Cell>? = null
    ) : ProtoBuf

    @Serializable
    internal class FeedEvent(
        @ProtoId(1) @JvmField val eventId: Long = 0L,
        @ProtoId(2) @JvmField val time: Int = 0,
        @ProtoId(3) @JvmField val eventtype: Int = 0,
        @ProtoId(4) @JvmField val msgUserInfo: StrangerInfo? = null,
        @ProtoId(5) @JvmField val msgFeedInfo: FeedInfo? = null,
        @ProtoId(6) @JvmField val eventTips: String = "",
        @ProtoId(7) @JvmField val msgComment: FeedComment? = null,
        @ProtoId(8) @JvmField val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class FeedsCookie(
        @ProtoId(1) @JvmField val strList: List<String> = listOf(),
        @ProtoId(2) @JvmField val pose: Int = 0,
        @ProtoId(3) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val uint64Topics: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class NearbyTopic(
        @ProtoId(1) @JvmField val topicId: Long = 0L,
        @ProtoId(2) @JvmField val topic: String = "",
        @ProtoId(3) @JvmField val foreword: String = "",
        @ProtoId(4) @JvmField val createTime: Int = 0,
        @ProtoId(5) @JvmField val updateTime: Int = 0,
        @ProtoId(6) @JvmField val hotFlag: Int = 0,
        @ProtoId(7) @JvmField val buttonStyle: Int = 0,
        @ProtoId(8) @JvmField val buttonSrc: String = "",
        @ProtoId(9) @JvmField val backgroundSrc: String = "",
        @ProtoId(10) @JvmField val attendeeInfo: String = "",
        @ProtoId(11) @JvmField val index: Int = 0,
        @ProtoId(12) @JvmField val publishScope: Int = 0,
        @ProtoId(13) @JvmField val effectiveTime: Int = 0,
        @ProtoId(14) @JvmField val expiationTime: Int = 0,
        @ProtoId(15) @JvmField val pushedUsrCount: Int = 0,
        @ProtoId(16) @JvmField val timerangeLeft: Int = 0,
        @ProtoId(17) @JvmField val timerangeRight: Int = 0,
        @ProtoId(18) @JvmField val area: String = ""
    ) : ProtoBuf

    @Serializable
    internal class NearbyEvent(
        @ProtoId(1) @JvmField val eventtype: Int = 0,
        @ProtoId(2) @JvmField val msgRankevent: RankEvent? = null,
        @ProtoId(3) @JvmField val eventUin: Long = 0L,
        @ProtoId(4) @JvmField val eventTinyid: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class Feed(
        @ProtoId(1) @JvmField val msgUserInfo: PublisherInfo? = null,
        @ProtoId(2) @JvmField val msgFeedInfo: FeedInfo? = null,
        @ProtoId(3) @JvmField val ownerFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ActivityInfo(
        @ProtoId(2) @JvmField val name: String = "",
        @ProtoId(3) @JvmField val cover: String = "",
        @ProtoId(4) @JvmField val url: String = "",
        @ProtoId(5) @JvmField val startTime: Int = 0,
        @ProtoId(6) @JvmField val endTime: Int = 0,
        @ProtoId(7) @JvmField val locName: String = "",
        @ProtoId(8) @JvmField val enroll: Long = 0L,
        @ProtoId(9) @JvmField val createUin: Long = 0L,
        @ProtoId(10) @JvmField val createTime: Int = 0,
        @ProtoId(11) @JvmField val organizerInfo: OrganizerInfo = OrganizerInfo(),
        @ProtoId(12) @JvmField val flag: Long? = null
    ) : ProtoBuf

    @Serializable
    internal class HotEntry(
        @ProtoId(1) @JvmField val openFlag: Int = 0,
        @ProtoId(2) @JvmField val restTime: Int = 0,
        @ProtoId(3) @JvmField val foreword: String = "",
        @ProtoId(4) @JvmField val backgroundSrc: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UserFeed(
        @ProtoId(1) @JvmField val msgUserInfo: PublisherInfo? = null,
        @ProtoId(2) @JvmField val msgFeedInfo: FeedInfo? = null,
        @ProtoId(3) @JvmField val ownerFlag: Int = 0,
        @ProtoId(4) @JvmField val msgActivityInfo: ActivityInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class Elem(
        @ProtoId(1) @JvmField val content: String = "",
        @ProtoId(2) @JvmField val msgFaceInfo: Face? = null
    ) : ProtoBuf

    @Serializable
    internal class HotFreshFeedList(
        @ProtoId(1) @JvmField val msgFeeds: List<HotUserFeed>? = null,
        @ProtoId(2) @JvmField val updateTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RptInterestTag(
        @ProtoId(1) @JvmField val interestTags: List<InterestTag>? = null
    ) : ProtoBuf

    @Serializable
    internal class AddressInfo(
        @ProtoId(1) @JvmField val companyZone: String = "",
        @ProtoId(2) @JvmField val companyName: String = "",
        @ProtoId(3) @JvmField val companyAddr: String = "",
        @ProtoId(4) @JvmField val companyPicUrl: String = "",
        @ProtoId(5) @JvmField val companyUrl: String = "",
        @ProtoId(6) @JvmField val msgCompanyId: ShopID? = null
    ) : ProtoBuf

    @Serializable
    internal class PublisherInfo(
        @ProtoId(1) @JvmField val tinyid: Long = 0L,
        @ProtoId(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val age: Int = 0,
        @ProtoId(4) @JvmField val gender: Int = 0,
        @ProtoId(5) @JvmField val constellation: String = "",
        @ProtoId(6) @JvmField val profession: Int = 0,
        @ProtoId(7) @JvmField val distance: String = "",
        @ProtoId(8) @JvmField val marriage: Int = 0,
        @ProtoId(9) @JvmField val vipinfo: String = "",
        @ProtoId(10) @JvmField val recommend: Int = 0,
        @ProtoId(11) @JvmField val godflag: Int = 0,
        @ProtoId(12) @JvmField val chatflag: Int = 0,
        @ProtoId(13) @JvmField val chatupCount: Int = 0,
        @ProtoId(14) @JvmField val charm: Int = 0,
        @ProtoId(15) @JvmField val charmLevel: Int = 0,
        @ProtoId(16) @JvmField val pubNumber: Int = 0,
        @ProtoId(17) @JvmField val msgCommonLabel: CommonLabel? = null,
        @ProtoId(18) @JvmField val recentVistorTime: Int = 0,
        @ProtoId(19) @JvmField val strangerDeclare: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20) @JvmField val friendUin: Long = 0L,
        @ProtoId(21) @JvmField val historyFlag: Int = 0,
        @ProtoId(22) @JvmField val followflag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class HotUserFeed(
        @ProtoId(1) @JvmField val feedId: String = "",
        @ProtoId(2) @JvmField val praiseCount: Int = 0,
        @ProtoId(3) @JvmField val publishUid: Long = 0L,
        @ProtoId(4) @JvmField val publishTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FreshFeedInfo(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val time: Int = 0,
        @ProtoId(3) @JvmField val feedId: String = "",
        @ProtoId(4) @JvmField val feedType: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CommonLabel(
        @ProtoId(1) @JvmField val lableId: Int = 0,
        @ProtoId(2) @JvmField val lableMsgPre: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val lableMsgLast: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val interstName: List<ByteArray>? = null,
        @ProtoId(5) @JvmField val interstType: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    internal class Face(
        @ProtoId(1) @JvmField val index: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class StrangerInfo(
        @ProtoId(1) @JvmField val tinyid: Long = 0L,
        @ProtoId(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val age: Int = 0,
        @ProtoId(4) @JvmField val gender: Int = 0,
        @ProtoId(5) @JvmField val dating: Int = 0,
        @ProtoId(6) @JvmField val listIdx: Int = 0,
        @ProtoId(7) @JvmField val constellation: String = "",
        @ProtoId(8) @JvmField val profession: Int = 0,
        @ProtoId(9) @JvmField val marriage: Int = 0,
        @ProtoId(10) @JvmField val vipinfo: String = "",
        @ProtoId(11) @JvmField val recommend: Int = 0,
        @ProtoId(12) @JvmField val godflag: Int = 0,
        @ProtoId(13) @JvmField val charm: Int = 0,
        @ProtoId(14) @JvmField val charmLevel: Int = 0,
        @ProtoId(15) @JvmField val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class HotTopic(
        @ProtoId(1) @JvmField val id: Long = 0L,
        @ProtoId(2) @JvmField val title: String = "",
        @ProtoId(3) @JvmField val topicType: Long = 0L,
        @ProtoId(4) @JvmField val total: Long = 0L,
        @ProtoId(5) @JvmField val times: Long = 0L,
        @ProtoId(6) @JvmField val historyTimes: Long = 0L,
        @ProtoId(7) @JvmField val bgUrl: String = "",
        @ProtoId(8) @JvmField val url: String = "",
        @ProtoId(9) @JvmField val extraInfo: String = ""
    ) : ProtoBuf

    @Serializable
    internal class DateEvent(
        @ProtoId(1) @JvmField val eventId: Long = 0L,
        @ProtoId(2) @JvmField val time: Int = 0,
        @ProtoId(3) @JvmField val type: Int = 0,
        @ProtoId(4) @JvmField val msgUserInfo: StrangerInfo? = null,
        @ProtoId(5) @JvmField val msgDateInfo: AppointInfo? = null,
        @ProtoId(6) @JvmField val attendIdx: Int = 0,
        @ProtoId(7) @JvmField val eventTips: String = "",
        @ProtoId(8) @JvmField val msgComment: DateComment? = null,
        @ProtoId(9) @JvmField val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AppointInfo(
        @ProtoId(1) @JvmField val msgAppointId: AppointID? = null,
        @ProtoId(2) @JvmField val msgAppointment: AppointContent? = null,
        @ProtoId(3) @JvmField val appointStatus: Int = 0,
        @ProtoId(4) @JvmField val joinWording: String = "",
        @ProtoId(5) @JvmField val viewWording: String = "",
        @ProtoId(6) @JvmField val unreadCount: Int = 0,
        @ProtoId(7) @JvmField val owner: Int = 0,
        @ProtoId(8) @JvmField val join: Int = 0,
        @ProtoId(9) @JvmField val view: Int = 0,
        @ProtoId(10) @JvmField val commentWording: String = "",
        @ProtoId(11) @JvmField val commentNum: Int = 0,
        @ProtoId(12) @JvmField val attendStatus: Int = 0,
        @ProtoId(13) @JvmField val msgAppointmentEx: AppointInfoEx? = null
    ) : ProtoBuf

    @Serializable
    internal class UserInfo(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val age: Int = 0,
        @ProtoId(4) @JvmField val gender: Int = 0,
        @ProtoId(5) @JvmField val avatar: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReplyInfo(
        @ProtoId(1) @JvmField val commentId: String = "",
        @ProtoId(2) @JvmField val msgStrangerInfo: StrangerInfo? = null
    ) : ProtoBuf
}