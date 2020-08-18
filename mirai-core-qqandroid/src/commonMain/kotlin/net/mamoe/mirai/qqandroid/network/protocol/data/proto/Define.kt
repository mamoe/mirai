package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class Common : ProtoBuf {
    @Serializable
    internal class BindInfo(
        @ProtoNumber(1) @JvmField val friUin: Long = 0L,
        @ProtoNumber(2) @JvmField val friNick: String = "",
        @ProtoNumber(3) @JvmField val time: Long = 0L,
        @ProtoNumber(4) @JvmField val bindStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalInfo(
        @ProtoNumber(1) @JvmField val id: Int = 0,
        @ProtoNumber(2) @JvmField val type: Int = 0,
        @ProtoNumber(4) @JvmField val seq: Long = 0,
        @ProtoNumber(5) @JvmField val name: String = "",
        @ProtoNumber(6) @JvmField val newflag: Int = 0,
        @ProtoNumber(7) @JvmField val time: Long = 0L,
        @ProtoNumber(8) @JvmField val msgBindFri: BindInfo? = null,
        @ProtoNumber(11) @JvmField val desc: String = "",
        @ProtoNumber(31) @JvmField val level: Int = 0,
        @ProtoNumber(36) @JvmField val taskinfos: List<MedalTaskInfo>? = null,
        @ProtoNumber(40) @JvmField val point: Int = 0,
        @ProtoNumber(41) @JvmField val pointLevel2: Int = 0,
        @ProtoNumber(42) @JvmField val pointLevel3: Int = 0,
        @ProtoNumber(43) @JvmField val seqLevel2: Long = 0,
        @ProtoNumber(44) @JvmField val seqLevel3: Long = 0,
        @ProtoNumber(45) @JvmField val timeLevel2: Long = 0L,
        @ProtoNumber(46) @JvmField val timeLevel3: Long = 0L,
        @ProtoNumber(47) @JvmField val descLevel2: String = "",
        @ProtoNumber(48) @JvmField val descLevel3: String = "",
        @ProtoNumber(49) @JvmField val endtime: Int = 0,
        @ProtoNumber(50) @JvmField val detailUrl: String = "",
        @ProtoNumber(51) @JvmField val detailUrl2: String = "",
        @ProtoNumber(52) @JvmField val detailUrl3: String = "",
        @ProtoNumber(53) @JvmField val taskDesc: String = "",
        @ProtoNumber(54) @JvmField val taskDesc2: String = "",
        @ProtoNumber(55) @JvmField val taskDesc3: String = "",
        @ProtoNumber(56) @JvmField val levelCount: Int = 0,
        @ProtoNumber(57) @JvmField val noProgress: Int = 0,
        @ProtoNumber(58) @JvmField val resource: String = "",
        @ProtoNumber(59) @JvmField val fromuinLevel: Int = 0,
        @ProtoNumber(60) @JvmField val unread: Int = 0,
        @ProtoNumber(61) @JvmField val unread2: Int = 0,
        @ProtoNumber(62) @JvmField val unread3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalTaskInfo(
        @ProtoNumber(1) @JvmField val taskid: Int = 0,
        @ProtoNumber(32) @JvmField val int32TaskValue: Int = 0,
        @ProtoNumber(33) @JvmField val tarValue: Int = 0,
        @ProtoNumber(34) @JvmField val tarValueLevel2: Int = 0,
        @ProtoNumber(35) @JvmField val tarValueLevel3: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class AppointDefine : ProtoBuf {
    @Serializable
    internal class ADFeedContent(
        @ProtoNumber(1) @JvmField val msgUserInfo: UserInfo? = null,
        @ProtoNumber(2) @JvmField val strPicUrl: List<String> = listOf(),
        @ProtoNumber(3) @JvmField val msgText: RichText? = null,
        @ProtoNumber(4) @JvmField val attendInfo: String = "",
        @ProtoNumber(5) @JvmField val actionUrl: String = "",
        @ProtoNumber(6) @JvmField val publishTime: Int = 0,
        @ProtoNumber(7) @JvmField val msgHotTopicList: HotTopicList? = null,
        @ProtoNumber(8) @JvmField val moreUrl: String = "",
        @ProtoNumber(9) @JvmField val recordDuration: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RichText(
        @ProtoNumber(1) @JvmField val msgElems: List<Elem>? = null
    ) : ProtoBuf

    @Serializable
    internal class RankEvent(
        @ProtoNumber(1) @JvmField val listtype: Int = 0,
        @ProtoNumber(2) @JvmField val notifytype: Int = 0,
        @ProtoNumber(3) @JvmField val eventtime: Int = 0,
        @ProtoNumber(4) @JvmField val seq: Int = 0,
        @ProtoNumber(5) @JvmField val notifyTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Wifi(
        @ProtoNumber(1) @JvmField val mac: Long = 0L,
        @ProtoNumber(2) @JvmField val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class InterestItem(
        @ProtoNumber(1) @JvmField val tagId: Long = 0L,
        @ProtoNumber(2) @JvmField val tagName: String = "",
        @ProtoNumber(3) @JvmField val tagIconUrl: String = "",
        @ProtoNumber(4) @JvmField val tagHref: String = "",
        @ProtoNumber(5) @JvmField val tagBackColor: String = "",
        @ProtoNumber(6) @JvmField val tagFontColor: String = "",
        @ProtoNumber(7) @JvmField val tagVid: String = "",
        @ProtoNumber(8) @JvmField val tagType: Int = 0,
        @ProtoNumber(9) @JvmField val addTime: Int = 0,
        @ProtoNumber(10) @JvmField val tagCategory: String = "",
        @ProtoNumber(11) @JvmField val tagOtherUrl: String = "",
        @ProtoNumber(12) @JvmField val bid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShopID(
        @ProtoNumber(1) @JvmField val shopid: String = "",
        @ProtoNumber(2) @JvmField val sp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FeedComment(
        @ProtoNumber(1) @JvmField val commentId: String = "",
        @ProtoNumber(2) @JvmField val feedId: String = "",
        @ProtoNumber(3) @JvmField val msgPublisherInfo: StrangerInfo? = null,
        @ProtoNumber(4) @JvmField val time: Int = 0,
        @ProtoNumber(6) @JvmField val msgReplyInfo: ReplyInfo? = null,
        @ProtoNumber(7) @JvmField val flag: Int = 0,
        @ProtoNumber(8) @JvmField val msgContent: RichText? = null,
        @ProtoNumber(9) @JvmField val hot: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ADFeed(
        @ProtoNumber(1) @JvmField val taskId: Int = 0,
        @ProtoNumber(2) @JvmField val style: Int = 0,
        @ProtoNumber(3) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Cell(
        @ProtoNumber(1) @JvmField val int32Mcc: Int = -1,
        @ProtoNumber(2) @JvmField val int32Mnc: Int = -1,
        @ProtoNumber(3) @JvmField val int32Lac: Int = -1,
        @ProtoNumber(4) @JvmField val int32Cellid: Int = -1,
        @ProtoNumber(5) @JvmField val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RecentVistorEvent(
        @ProtoNumber(1) @JvmField val eventtype: Int = 0,
        @ProtoNumber(2) @JvmField val eventTinyid: Long = 0L,
        @ProtoNumber(3) @JvmField val unreadCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class OrganizerInfo(
        @ProtoNumber(1) @JvmField val hostName: String = "",
        @ProtoNumber(2) @JvmField val hostUrl: String = "",
        @ProtoNumber(3) @JvmField val hostCover: String = ""
    ) : ProtoBuf

    @Serializable
    internal class InterestTag(
        @ProtoNumber(1) @JvmField val tagType: Int = 0,
        @ProtoNumber(2) @JvmField val msgTagList: List<InterestItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class AppointInfoEx(
        @ProtoNumber(1) @JvmField val feedsPicUrl: String = "",
        @ProtoNumber(2) @JvmField val feedsUrl: String = "",
        @ProtoNumber(3) @JvmField val detailTitle: String = "",
        @ProtoNumber(4) @JvmField val detailDescribe: String = "",
        @ProtoNumber(5) @JvmField val showPublisher: Int = 0,
        @ProtoNumber(6) @JvmField val detailPicUrl: String = "",
        @ProtoNumber(7) @JvmField val detailUrl: String = "",
        @ProtoNumber(8) @JvmField val showAttend: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DateComment(
        @ProtoNumber(1) @JvmField val commentId: String = "",
        @ProtoNumber(2) @JvmField val msgAppointId: AppointID? = null,
        @ProtoNumber(3) @JvmField val msgPublisherInfo: StrangerInfo? = null,
        @ProtoNumber(4) @JvmField val time: Int = 0,
        @ProtoNumber(6) @JvmField val msgReplyInfo: ReplyInfo? = null,
        @ProtoNumber(7) @JvmField val flag: Int = 0,
        @ProtoNumber(8) @JvmField val msgContent: RichText? = null
    ) : ProtoBuf

    @Serializable
    internal class AppointContent(
        @ProtoNumber(1) @JvmField val appointSubject: Int = 0,
        @ProtoNumber(2) @JvmField val payType: Int = 0,
        @ProtoNumber(3) @JvmField val appointDate: Int = 0,
        @ProtoNumber(4) @JvmField val appointGender: Int = 0,
        @ProtoNumber(5) @JvmField val appointIntroduce: String = "",
        @ProtoNumber(6) @JvmField val msgAppointAddress: AddressInfo? = null,
        @ProtoNumber(7) @JvmField val msgTravelInfo: TravelInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class FeedInfo(
        @ProtoNumber(1) @JvmField val feedType: Long = 0L,
        @ProtoNumber(2) @JvmField val feedId: String = "",
        @ProtoNumber(3) @JvmField val msgFeedContent: FeedContent? = null,
        @ProtoNumber(4) @JvmField val msgTopicInfo: NearbyTopic? = null,
        @ProtoNumber(5) @JvmField val publishTime: Long = 0,
        @ProtoNumber(6) @JvmField val praiseCount: Int = 0,
        @ProtoNumber(7) @JvmField val praiseFlag: Int = 0,
        @ProtoNumber(8) @JvmField val msgPraiseUser: List<StrangerInfo>? = null,
        @ProtoNumber(9) @JvmField val commentCount: Int = 0,
        @ProtoNumber(10) @JvmField val msgCommentList: List<FeedComment>? = null,
        @ProtoNumber(11) @JvmField val commentRetAll: Int = 0,
        @ProtoNumber(12) @JvmField val hotFlag: Int = 0,
        @ProtoNumber(13) @JvmField val svrReserved: Long = 0L,
        @ProtoNumber(14) @JvmField val msgHotEntry: HotEntry? = null
    ) : ProtoBuf

    @Serializable
    internal class HotTopicList(
        @ProtoNumber(1) @JvmField val topicList: List<HotTopic>? = null
    ) : ProtoBuf

    @Serializable
    internal class FeedContent(
        @ProtoNumber(1) @JvmField val strPicUrl: List<String> = listOf(),
        @ProtoNumber(2) @JvmField val msgText: RichText? = null,
        @ProtoNumber(3) @JvmField val hrefUrl: String = "",
        @ProtoNumber(5) @JvmField val groupName: String = "",
        @ProtoNumber(6) @JvmField val groupBulletin: String = "",
        @ProtoNumber(7) @JvmField val feedType: Int = 0,
        @ProtoNumber(8) @JvmField val poiId: String = "",
        @ProtoNumber(9) @JvmField val poiTitle: String = "",
        @ProtoNumber(20) @JvmField val effectiveTime: Int = 0,
        @ProtoNumber(21) @JvmField val expiationTime: Int = 0,
        @ProtoNumber(22) @JvmField val msgLocale: LocaleInfo? = null,
        @ProtoNumber(23) @JvmField val feedsIndex: Int = 0,
        @ProtoNumber(24) @JvmField val msgAd: ADFeed? = null,
        @ProtoNumber(25) @JvmField val privateData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TravelInfo(
        @ProtoNumber(1) @JvmField val msgDepartLocale: LocaleInfo? = null,
        @ProtoNumber(2) @JvmField val msgDestination: LocaleInfo? = null,
        @ProtoNumber(3) @JvmField val vehicle: Int = 0,
        @ProtoNumber(4) @JvmField val partnerCount: Int = 0,
        @ProtoNumber(5) @JvmField val placePicUrl: String = "",
        @ProtoNumber(6) @JvmField val placeUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RecentFreshFeed(
        @ProtoNumber(1) @JvmField val freshFeedInfo: List<FreshFeedInfo>? = null,
        @ProtoNumber(2) @JvmField val uid: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class GPS(
        @ProtoNumber(1) @JvmField val int32Lat: Int = 900000000,
        @ProtoNumber(2) @JvmField val int32Lon: Int = 900000000,
        @ProtoNumber(3) @JvmField val int32Alt: Int = -10000000,
        @ProtoNumber(4) @JvmField val int32Type: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AppointID(
        @ProtoNumber(1) @JvmField val requestId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class LocaleInfo(
        @ProtoNumber(1) @JvmField val name: String = "",
        @ProtoNumber(2) @JvmField val country: String = "",
        @ProtoNumber(3) @JvmField val province: String = "",
        @ProtoNumber(4) @JvmField val city: String = "",
        @ProtoNumber(5) @JvmField val region: String = "",
        @ProtoNumber(6) @JvmField val poi: String = "",
        @ProtoNumber(7) @JvmField val msgGps: GPS? = null,
        @ProtoNumber(8) @JvmField val address: String = ""
    ) : ProtoBuf

    @Serializable
    internal class LBSInfo(
        @ProtoNumber(1) @JvmField val msgGps: GPS? = null,
        @ProtoNumber(2) @JvmField val msgWifis: List<Wifi>? = null,
        @ProtoNumber(3) @JvmField val msgCells: List<Cell>? = null
    ) : ProtoBuf

    @Serializable
    internal class FeedEvent(
        @ProtoNumber(1) @JvmField val eventId: Long = 0L,
        @ProtoNumber(2) @JvmField val time: Int = 0,
        @ProtoNumber(3) @JvmField val eventtype: Int = 0,
        @ProtoNumber(4) @JvmField val msgUserInfo: StrangerInfo? = null,
        @ProtoNumber(5) @JvmField val msgFeedInfo: FeedInfo? = null,
        @ProtoNumber(6) @JvmField val eventTips: String = "",
        @ProtoNumber(7) @JvmField val msgComment: FeedComment? = null,
        @ProtoNumber(8) @JvmField val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class FeedsCookie(
        @ProtoNumber(1) @JvmField val strList: List<String> = listOf(),
        @ProtoNumber(2) @JvmField val pose: Int = 0,
        @ProtoNumber(3) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val uint64Topics: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class NearbyTopic(
        @ProtoNumber(1) @JvmField val topicId: Long = 0L,
        @ProtoNumber(2) @JvmField val topic: String = "",
        @ProtoNumber(3) @JvmField val foreword: String = "",
        @ProtoNumber(4) @JvmField val createTime: Int = 0,
        @ProtoNumber(5) @JvmField val updateTime: Int = 0,
        @ProtoNumber(6) @JvmField val hotFlag: Int = 0,
        @ProtoNumber(7) @JvmField val buttonStyle: Int = 0,
        @ProtoNumber(8) @JvmField val buttonSrc: String = "",
        @ProtoNumber(9) @JvmField val backgroundSrc: String = "",
        @ProtoNumber(10) @JvmField val attendeeInfo: String = "",
        @ProtoNumber(11) @JvmField val index: Int = 0,
        @ProtoNumber(12) @JvmField val publishScope: Int = 0,
        @ProtoNumber(13) @JvmField val effectiveTime: Int = 0,
        @ProtoNumber(14) @JvmField val expiationTime: Int = 0,
        @ProtoNumber(15) @JvmField val pushedUsrCount: Int = 0,
        @ProtoNumber(16) @JvmField val timerangeLeft: Int = 0,
        @ProtoNumber(17) @JvmField val timerangeRight: Int = 0,
        @ProtoNumber(18) @JvmField val area: String = ""
    ) : ProtoBuf

    @Serializable
    internal class NearbyEvent(
        @ProtoNumber(1) @JvmField val eventtype: Int = 0,
        @ProtoNumber(2) @JvmField val msgRankevent: RankEvent? = null,
        @ProtoNumber(3) @JvmField val eventUin: Long = 0L,
        @ProtoNumber(4) @JvmField val eventTinyid: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class Feed(
        @ProtoNumber(1) @JvmField val msgUserInfo: PublisherInfo? = null,
        @ProtoNumber(2) @JvmField val msgFeedInfo: FeedInfo? = null,
        @ProtoNumber(3) @JvmField val ownerFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ActivityInfo(
        @ProtoNumber(2) @JvmField val name: String = "",
        @ProtoNumber(3) @JvmField val cover: String = "",
        @ProtoNumber(4) @JvmField val url: String = "",
        @ProtoNumber(5) @JvmField val startTime: Int = 0,
        @ProtoNumber(6) @JvmField val endTime: Int = 0,
        @ProtoNumber(7) @JvmField val locName: String = "",
        @ProtoNumber(8) @JvmField val enroll: Long = 0L,
        @ProtoNumber(9) @JvmField val createUin: Long = 0L,
        @ProtoNumber(10) @JvmField val createTime: Int = 0,
        @ProtoNumber(11) @JvmField val organizerInfo: OrganizerInfo = OrganizerInfo(),
        @ProtoNumber(12) @JvmField val flag: Long? = null
    ) : ProtoBuf

    @Serializable
    internal class HotEntry(
        @ProtoNumber(1) @JvmField val openFlag: Int = 0,
        @ProtoNumber(2) @JvmField val restTime: Int = 0,
        @ProtoNumber(3) @JvmField val foreword: String = "",
        @ProtoNumber(4) @JvmField val backgroundSrc: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UserFeed(
        @ProtoNumber(1) @JvmField val msgUserInfo: PublisherInfo? = null,
        @ProtoNumber(2) @JvmField val msgFeedInfo: FeedInfo? = null,
        @ProtoNumber(3) @JvmField val ownerFlag: Int = 0,
        @ProtoNumber(4) @JvmField val msgActivityInfo: ActivityInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class Elem(
        @ProtoNumber(1) @JvmField val content: String = "",
        @ProtoNumber(2) @JvmField val msgFaceInfo: Face? = null
    ) : ProtoBuf

    @Serializable
    internal class HotFreshFeedList(
        @ProtoNumber(1) @JvmField val msgFeeds: List<HotUserFeed>? = null,
        @ProtoNumber(2) @JvmField val updateTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RptInterestTag(
        @ProtoNumber(1) @JvmField val interestTags: List<InterestTag>? = null
    ) : ProtoBuf

    @Serializable
    internal class AddressInfo(
        @ProtoNumber(1) @JvmField val companyZone: String = "",
        @ProtoNumber(2) @JvmField val companyName: String = "",
        @ProtoNumber(3) @JvmField val companyAddr: String = "",
        @ProtoNumber(4) @JvmField val companyPicUrl: String = "",
        @ProtoNumber(5) @JvmField val companyUrl: String = "",
        @ProtoNumber(6) @JvmField val msgCompanyId: ShopID? = null
    ) : ProtoBuf

    @Serializable
    internal class PublisherInfo(
        @ProtoNumber(1) @JvmField val tinyid: Long = 0L,
        @ProtoNumber(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val age: Int = 0,
        @ProtoNumber(4) @JvmField val gender: Int = 0,
        @ProtoNumber(5) @JvmField val constellation: String = "",
        @ProtoNumber(6) @JvmField val profession: Int = 0,
        @ProtoNumber(7) @JvmField val distance: String = "",
        @ProtoNumber(8) @JvmField val marriage: Int = 0,
        @ProtoNumber(9) @JvmField val vipinfo: String = "",
        @ProtoNumber(10) @JvmField val recommend: Int = 0,
        @ProtoNumber(11) @JvmField val godflag: Int = 0,
        @ProtoNumber(12) @JvmField val chatflag: Int = 0,
        @ProtoNumber(13) @JvmField val chatupCount: Int = 0,
        @ProtoNumber(14) @JvmField val charm: Int = 0,
        @ProtoNumber(15) @JvmField val charmLevel: Int = 0,
        @ProtoNumber(16) @JvmField val pubNumber: Int = 0,
        @ProtoNumber(17) @JvmField val msgCommonLabel: CommonLabel? = null,
        @ProtoNumber(18) @JvmField val recentVistorTime: Int = 0,
        @ProtoNumber(19) @JvmField val strangerDeclare: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20) @JvmField val friendUin: Long = 0L,
        @ProtoNumber(21) @JvmField val historyFlag: Int = 0,
        @ProtoNumber(22) @JvmField val followflag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class HotUserFeed(
        @ProtoNumber(1) @JvmField val feedId: String = "",
        @ProtoNumber(2) @JvmField val praiseCount: Int = 0,
        @ProtoNumber(3) @JvmField val publishUid: Long = 0L,
        @ProtoNumber(4) @JvmField val publishTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FreshFeedInfo(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val time: Int = 0,
        @ProtoNumber(3) @JvmField val feedId: String = "",
        @ProtoNumber(4) @JvmField val feedType: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CommonLabel(
        @ProtoNumber(1) @JvmField val lableId: Int = 0,
        @ProtoNumber(2) @JvmField val lableMsgPre: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val lableMsgLast: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val interstName: List<ByteArray>? = null,
        @ProtoNumber(5) @JvmField val interstType: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    internal class Face(
        @ProtoNumber(1) @JvmField val index: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class StrangerInfo(
        @ProtoNumber(1) @JvmField val tinyid: Long = 0L,
        @ProtoNumber(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val age: Int = 0,
        @ProtoNumber(4) @JvmField val gender: Int = 0,
        @ProtoNumber(5) @JvmField val dating: Int = 0,
        @ProtoNumber(6) @JvmField val listIdx: Int = 0,
        @ProtoNumber(7) @JvmField val constellation: String = "",
        @ProtoNumber(8) @JvmField val profession: Int = 0,
        @ProtoNumber(9) @JvmField val marriage: Int = 0,
        @ProtoNumber(10) @JvmField val vipinfo: String = "",
        @ProtoNumber(11) @JvmField val recommend: Int = 0,
        @ProtoNumber(12) @JvmField val godflag: Int = 0,
        @ProtoNumber(13) @JvmField val charm: Int = 0,
        @ProtoNumber(14) @JvmField val charmLevel: Int = 0,
        @ProtoNumber(15) @JvmField val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class HotTopic(
        @ProtoNumber(1) @JvmField val id: Long = 0L,
        @ProtoNumber(2) @JvmField val title: String = "",
        @ProtoNumber(3) @JvmField val topicType: Long = 0L,
        @ProtoNumber(4) @JvmField val total: Long = 0L,
        @ProtoNumber(5) @JvmField val times: Long = 0L,
        @ProtoNumber(6) @JvmField val historyTimes: Long = 0L,
        @ProtoNumber(7) @JvmField val bgUrl: String = "",
        @ProtoNumber(8) @JvmField val url: String = "",
        @ProtoNumber(9) @JvmField val extraInfo: String = ""
    ) : ProtoBuf

    @Serializable
    internal class DateEvent(
        @ProtoNumber(1) @JvmField val eventId: Long = 0L,
        @ProtoNumber(2) @JvmField val time: Int = 0,
        @ProtoNumber(3) @JvmField val type: Int = 0,
        @ProtoNumber(4) @JvmField val msgUserInfo: StrangerInfo? = null,
        @ProtoNumber(5) @JvmField val msgDateInfo: AppointInfo? = null,
        @ProtoNumber(6) @JvmField val attendIdx: Int = 0,
        @ProtoNumber(7) @JvmField val eventTips: String = "",
        @ProtoNumber(8) @JvmField val msgComment: DateComment? = null,
        @ProtoNumber(9) @JvmField val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AppointInfo(
        @ProtoNumber(1) @JvmField val msgAppointId: AppointID? = null,
        @ProtoNumber(2) @JvmField val msgAppointment: AppointContent? = null,
        @ProtoNumber(3) @JvmField val appointStatus: Int = 0,
        @ProtoNumber(4) @JvmField val joinWording: String = "",
        @ProtoNumber(5) @JvmField val viewWording: String = "",
        @ProtoNumber(6) @JvmField val unreadCount: Int = 0,
        @ProtoNumber(7) @JvmField val owner: Int = 0,
        @ProtoNumber(8) @JvmField val join: Int = 0,
        @ProtoNumber(9) @JvmField val view: Int = 0,
        @ProtoNumber(10) @JvmField val commentWording: String = "",
        @ProtoNumber(11) @JvmField val commentNum: Int = 0,
        @ProtoNumber(12) @JvmField val attendStatus: Int = 0,
        @ProtoNumber(13) @JvmField val msgAppointmentEx: AppointInfoEx? = null
    ) : ProtoBuf

    @Serializable
    internal class UserInfo(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val age: Int = 0,
        @ProtoNumber(4) @JvmField val gender: Int = 0,
        @ProtoNumber(5) @JvmField val avatar: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReplyInfo(
        @ProtoNumber(1) @JvmField val commentId: String = "",
        @ProtoNumber(2) @JvmField val msgStrangerInfo: StrangerInfo? = null
    ) : ProtoBuf
}