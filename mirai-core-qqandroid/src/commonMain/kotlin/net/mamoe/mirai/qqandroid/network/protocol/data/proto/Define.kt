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
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
class Common : ProtoBuf {
    @Serializable
    class BindInfo(
        @ProtoId(1) val friUin: Long = 0L,
        @ProtoId(2) val friNick: String = "",
        @ProtoId(3) val time: Long = 0L,
        @ProtoId(4) val bindStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    class MedalInfo(
        @ProtoId(1) val id: Int = 0,
        @ProtoId(2) val type: Int = 0,
        @ProtoId(4) val seq: Long = 0,
        @ProtoId(5) val name: String = "",
        @ProtoId(6) val newflag: Int = 0,
        @ProtoId(7) val time: Long = 0L,
        @ProtoId(8) val msgBindFri: Common.BindInfo? = null,
        @ProtoId(11) val desc: String = "",
        @ProtoId(31) val level: Int = 0,
        @ProtoId(36) val taskinfos: List<Common.MedalTaskInfo>? = null,
        @ProtoId(40) val point: Int = 0,
        @ProtoId(41) val pointLevel2: Int = 0,
        @ProtoId(42) val pointLevel3: Int = 0,
        @ProtoId(43) val seqLevel2: Long = 0,
        @ProtoId(44) val seqLevel3: Long = 0,
        @ProtoId(45) val timeLevel2: Long = 0L,
        @ProtoId(46) val timeLevel3: Long = 0L,
        @ProtoId(47) val descLevel2: String = "",
        @ProtoId(48) val descLevel3: String = "",
        @ProtoId(49) val endtime: Int = 0,
        @ProtoId(50) val detailUrl: String = "",
        @ProtoId(51) val detailUrl2: String = "",
        @ProtoId(52) val detailUrl3: String = "",
        @ProtoId(53) val taskDesc: String = "",
        @ProtoId(54) val taskDesc2: String = "",
        @ProtoId(55) val taskDesc3: String = "",
        @ProtoId(56) val levelCount: Int = 0,
        @ProtoId(57) val noProgress: Int = 0,
        @ProtoId(58) val resource: String = "",
        @ProtoId(59) val fromuinLevel: Int = 0,
        @ProtoId(60) val unread: Int = 0,
        @ProtoId(61) val unread2: Int = 0,
        @ProtoId(62) val unread3: Int = 0
    ) : ProtoBuf

    @Serializable
    class MedalTaskInfo(
        @ProtoId(1) val taskid: Int = 0,
        @ProtoId(32) val int32TaskValue: Int = 0,
        @ProtoId(33) val tarValue: Int = 0,
        @ProtoId(34) val tarValueLevel2: Int = 0,
        @ProtoId(35) val tarValueLevel3: Int = 0
    ) : ProtoBuf
}

@Serializable
class AppointDefine : ProtoBuf {
    @Serializable
    class ADFeedContent(
        @ProtoId(1) val msgUserInfo: AppointDefine.UserInfo? = null,
        @ProtoId(2) val strPicUrl: List<String> = listOf(),
        @ProtoId(3) val msgText: AppointDefine.RichText? = null,
        @ProtoId(4) val attendInfo: String = "",
        @ProtoId(5) val actionUrl: String = "",
        @ProtoId(6) val publishTime: Int = 0,
        @ProtoId(7) val msgHotTopicList: AppointDefine.HotTopicList? = null,
        @ProtoId(8) val moreUrl: String = "",
        @ProtoId(9) val recordDuration: String = ""
    ) : ProtoBuf

    @Serializable
    class RichText(
        @ProtoId(1) val msgElems: List<AppointDefine.Elem>? = null
    ) : ProtoBuf

    @Serializable
    class RankEvent(
        @ProtoId(1) val listtype: Int = 0,
        @ProtoId(2) val notifytype: Int = 0,
        @ProtoId(3) val eventtime: Int = 0,
        @ProtoId(4) val seq: Int = 0,
        @ProtoId(5) val notifyTips: String = ""
    ) : ProtoBuf

    @Serializable
    class Wifi(
        @ProtoId(1) val mac: Long = 0L,
        @ProtoId(2) val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    class InterestItem(
        @ProtoId(1) val tagId: Long = 0L,
        @ProtoId(2) val tagName: String = "",
        @ProtoId(3) val tagIconUrl: String = "",
        @ProtoId(4) val tagHref: String = "",
        @ProtoId(5) val tagBackColor: String = "",
        @ProtoId(6) val tagFontColor: String = "",
        @ProtoId(7) val tagVid: String = "",
        @ProtoId(8) val tagType: Int = 0,
        @ProtoId(9) val addTime: Int = 0,
        @ProtoId(10) val tagCategory: String = "",
        @ProtoId(11) val tagOtherUrl: String = "",
        @ProtoId(12) val bid: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShopID(
        @ProtoId(1) val shopid: String = "",
        @ProtoId(2) val sp: Int = 0
    ) : ProtoBuf

    @Serializable
    class FeedComment(
        @ProtoId(1) val commentId: String = "",
        @ProtoId(2) val feedId: String = "",
        @ProtoId(3) val msgPublisherInfo: AppointDefine.StrangerInfo? = null,
        @ProtoId(4) val time: Int = 0,
        @ProtoId(6) val msgReplyInfo: AppointDefine.ReplyInfo? = null,
        @ProtoId(7) val flag: Int = 0,
        @ProtoId(8) val msgContent: AppointDefine.RichText? = null,
        @ProtoId(9) val hot: Int = 0
    ) : ProtoBuf

    @Serializable
    class ADFeed(
        @ProtoId(1) val taskId: Int = 0,
        @ProtoId(2) val style: Int = 0,
        @ProtoId(3) val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class Cell(
        @ProtoId(1) val int32Mcc: Int = -1,
        @ProtoId(2) val int32Mnc: Int = -1,
        @ProtoId(3) val int32Lac: Int = -1,
        @ProtoId(4) val int32Cellid: Int = -1,
        @ProtoId(5) val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    class RecentVistorEvent(
        @ProtoId(1) val eventtype: Int = 0,
        @ProtoId(2) val eventTinyid: Long = 0L,
        @ProtoId(3) val unreadCount: Int = 0
    ) : ProtoBuf

    @Serializable
    class OrganizerInfo(
        @ProtoId(1) val hostName: String = "",
        @ProtoId(2) val hostUrl: String = "",
        @ProtoId(3) val hostCover: String = ""
    ) : ProtoBuf

    @Serializable
    class InterestTag(
        @ProtoId(1) val tagType: Int = 0,
        @ProtoId(2) val msgTagList: List<AppointDefine.InterestItem>? = null
    ) : ProtoBuf

    @Serializable
    class AppointInfoEx(
        @ProtoId(1) val feedsPicUrl: String = "",
        @ProtoId(2) val feedsUrl: String = "",
        @ProtoId(3) val detailTitle: String = "",
        @ProtoId(4) val detailDescribe: String = "",
        @ProtoId(5) val showPublisher: Int = 0,
        @ProtoId(6) val detailPicUrl: String = "",
        @ProtoId(7) val detailUrl: String = "",
        @ProtoId(8) val showAttend: Int = 0
    ) : ProtoBuf

    @Serializable
    class DateComment(
        @ProtoId(1) val commentId: String = "",
        @ProtoId(2) val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(3) val msgPublisherInfo: AppointDefine.StrangerInfo? = null,
        @ProtoId(4) val time: Int = 0,
        @ProtoId(6) val msgReplyInfo: AppointDefine.ReplyInfo? = null,
        @ProtoId(7) val flag: Int = 0,
        @ProtoId(8) val msgContent: AppointDefine.RichText? = null
    ) : ProtoBuf

    @Serializable
    class AppointContent(
        @ProtoId(1) val appointSubject: Int = 0,
        @ProtoId(2) val payType: Int = 0,
        @ProtoId(3) val appointDate: Int = 0,
        @ProtoId(4) val appointGender: Int = 0,
        @ProtoId(5) val appointIntroduce: String = "",
        @ProtoId(6) val msgAppointAddress: AppointDefine.AddressInfo? = null,
        @ProtoId(7) val msgTravelInfo: AppointDefine.TravelInfo? = null
    ) : ProtoBuf

    @Serializable
    class FeedInfo(
        @ProtoId(1) val feedType: Long = 0L,
        @ProtoId(2) val feedId: String = "",
        @ProtoId(3) val msgFeedContent: AppointDefine.FeedContent? = null,
        @ProtoId(4) val msgTopicInfo: AppointDefine.NearbyTopic? = null,
        @ProtoId(5) val publishTime: Long = 0,
        @ProtoId(6) val praiseCount: Int = 0,
        @ProtoId(7) val praiseFlag: Int = 0,
        @ProtoId(8) val msgPraiseUser: List<AppointDefine.StrangerInfo>? = null,
        @ProtoId(9) val commentCount: Int = 0,
        @ProtoId(10) val msgCommentList: List<AppointDefine.FeedComment>? = null,
        @ProtoId(11) val commentRetAll: Int = 0,
        @ProtoId(12) val hotFlag: Int = 0,
        @ProtoId(13) val svrReserved: Long = 0L,
        @ProtoId(14) val msgHotEntry: AppointDefine.HotEntry? = null
    ) : ProtoBuf

    @Serializable
    class HotTopicList(
        @ProtoId(1) val topicList: List<AppointDefine.HotTopic>? = null
    ) : ProtoBuf

    @Serializable
    class FeedContent(
        @ProtoId(1) val strPicUrl: List<String> = listOf(),
        @ProtoId(2) val msgText: AppointDefine.RichText? = null,
        @ProtoId(3) val hrefUrl: String = "",
        @ProtoId(5) val groupName: String = "",
        @ProtoId(6) val groupBulletin: String = "",
        @ProtoId(7) val feedType: Int = 0,
        @ProtoId(8) val poiId: String = "",
        @ProtoId(9) val poiTitle: String = "",
        @ProtoId(20) val effectiveTime: Int = 0,
        @ProtoId(21) val expiationTime: Int = 0,
        @ProtoId(22) val msgLocale: AppointDefine.LocaleInfo? = null,
        @ProtoId(23) val feedsIndex: Int = 0,
        @ProtoId(24) val msgAd: AppointDefine.ADFeed? = null,
        @ProtoId(25) val privateData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class TravelInfo(
        @ProtoId(1) val msgDepartLocale: AppointDefine.LocaleInfo? = null,
        @ProtoId(2) val msgDestination: AppointDefine.LocaleInfo? = null,
        @ProtoId(3) val vehicle: Int = 0,
        @ProtoId(4) val partnerCount: Int = 0,
        @ProtoId(5) val placePicUrl: String = "",
        @ProtoId(6) val placeUrl: String = ""
    ) : ProtoBuf

    @Serializable
    class RecentFreshFeed(
        @ProtoId(1) val freshFeedInfo: List<AppointDefine.FreshFeedInfo>? = null,
        @ProtoId(2) val uid: Long = 0L
    ) : ProtoBuf

    @Serializable
    class GPS(
        @ProtoId(1) val int32Lat: Int = 900000000,
        @ProtoId(2) val int32Lon: Int = 900000000,
        @ProtoId(3) val int32Alt: Int = -10000000,
        @ProtoId(4) val int32Type: Int = 0
    ) : ProtoBuf

    @Serializable
    class AppointID(
        @ProtoId(1) val requestId: String = ""
    ) : ProtoBuf

    @Serializable
    class LocaleInfo(
        @ProtoId(1) val name: String = "",
        @ProtoId(2) val country: String = "",
        @ProtoId(3) val province: String = "",
        @ProtoId(4) val city: String = "",
        @ProtoId(5) val region: String = "",
        @ProtoId(6) val poi: String = "",
        @ProtoId(7) val msgGps: AppointDefine.GPS? = null,
        @ProtoId(8) val address: String = ""
    ) : ProtoBuf

    @Serializable
    class LBSInfo(
        @ProtoId(1) val msgGps: AppointDefine.GPS? = null,
        @ProtoId(2) val msgWifis: List<AppointDefine.Wifi>? = null,
        @ProtoId(3) val msgCells: List<AppointDefine.Cell>? = null
    ) : ProtoBuf

    @Serializable
    class FeedEvent(
        @ProtoId(1) val eventId: Long = 0L,
        @ProtoId(2) val time: Int = 0,
        @ProtoId(3) val eventtype: Int = 0,
        @ProtoId(4) val msgUserInfo: AppointDefine.StrangerInfo? = null,
        @ProtoId(5) val msgFeedInfo: AppointDefine.FeedInfo? = null,
        @ProtoId(6) val eventTips: String = "",
        @ProtoId(7) val msgComment: AppointDefine.FeedComment? = null,
        @ProtoId(8) val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    class FeedsCookie(
        @ProtoId(1) val strList: List<String> = listOf(),
        @ProtoId(2) val pose: Int = 0,
        @ProtoId(3) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val uint64Topics: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    class NearbyTopic(
        @ProtoId(1) val topicId: Long = 0L,
        @ProtoId(2) val topic: String = "",
        @ProtoId(3) val foreword: String = "",
        @ProtoId(4) val createTime: Int = 0,
        @ProtoId(5) val updateTime: Int = 0,
        @ProtoId(6) val hotFlag: Int = 0,
        @ProtoId(7) val buttonStyle: Int = 0,
        @ProtoId(8) val buttonSrc: String = "",
        @ProtoId(9) val backgroundSrc: String = "",
        @ProtoId(10) val attendeeInfo: String = "",
        @ProtoId(11) val index: Int = 0,
        @ProtoId(12) val publishScope: Int = 0,
        @ProtoId(13) val effectiveTime: Int = 0,
        @ProtoId(14) val expiationTime: Int = 0,
        @ProtoId(15) val pushedUsrCount: Int = 0,
        @ProtoId(16) val timerangeLeft: Int = 0,
        @ProtoId(17) val timerangeRight: Int = 0,
        @ProtoId(18) val area: String = ""
    ) : ProtoBuf

    @Serializable
    class NearbyEvent(
        @ProtoId(1) val eventtype: Int = 0,
        @ProtoId(2) val msgRankevent: AppointDefine.RankEvent? = null,
        @ProtoId(3) val eventUin: Long = 0L,
        @ProtoId(4) val eventTinyid: Long = 0L
    ) : ProtoBuf

    @Serializable
    class Feed(
        @ProtoId(1) val msgUserInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) val msgFeedInfo: AppointDefine.FeedInfo? = null,
        @ProtoId(3) val ownerFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class ActivityInfo(
        @ProtoId(2) val name: String = "",
        @ProtoId(3) val cover: String = "",
        @ProtoId(4) val url: String = "",
        @ProtoId(5) val startTime: Int = 0,
        @ProtoId(6) val endTime: Int = 0,
        @ProtoId(7) val locName: String = "",
        @ProtoId(8) val enroll: Long = 0L,
        @ProtoId(9) val createUin: Long = 0L,
        @ProtoId(10) val createTime: Int = 0,
        @ProtoId(11) val organizerInfo: AppointDefine.OrganizerInfo = OrganizerInfo(),
        @ProtoId(12) val flag: Long? = null
    ) : ProtoBuf

    @Serializable
    class HotEntry(
        @ProtoId(1) val openFlag: Int = 0,
        @ProtoId(2) val restTime: Int = 0,
        @ProtoId(3) val foreword: String = "",
        @ProtoId(4) val backgroundSrc: String = ""
    ) : ProtoBuf

    @Serializable
    class UserFeed(
        @ProtoId(1) val msgUserInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) val msgFeedInfo: AppointDefine.FeedInfo? = null,
        @ProtoId(3) val ownerFlag: Int = 0,
        @ProtoId(4) val msgActivityInfo: AppointDefine.ActivityInfo? = null
    ) : ProtoBuf

    @Serializable
    class Elem(
        @ProtoId(1) val content: String = "",
        @ProtoId(2) val msgFaceInfo: AppointDefine.Face? = null
    ) : ProtoBuf

    @Serializable
    class HotFreshFeedList(
        @ProtoId(1) val msgFeeds: List<AppointDefine.HotUserFeed>? = null,
        @ProtoId(2) val updateTime: Int = 0
    ) : ProtoBuf

    @Serializable
    class RptInterestTag(
        @ProtoId(1) val interestTags: List<AppointDefine.InterestTag>? = null
    ) : ProtoBuf

    @Serializable
    class AddressInfo(
        @ProtoId(1) val companyZone: String = "",
        @ProtoId(2) val companyName: String = "",
        @ProtoId(3) val companyAddr: String = "",
        @ProtoId(4) val companyPicUrl: String = "",
        @ProtoId(5) val companyUrl: String = "",
        @ProtoId(6) val msgCompanyId: AppointDefine.ShopID? = null
    ) : ProtoBuf

    @Serializable
    class PublisherInfo(
        @ProtoId(1) val tinyid: Long = 0L,
        @ProtoId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val age: Int = 0,
        @ProtoId(4) val gender: Int = 0,
        @ProtoId(5) val constellation: String = "",
        @ProtoId(6) val profession: Int = 0,
        @ProtoId(7) val distance: String = "",
        @ProtoId(8) val marriage: Int = 0,
        @ProtoId(9) val vipinfo: String = "",
        @ProtoId(10) val recommend: Int = 0,
        @ProtoId(11) val godflag: Int = 0,
        @ProtoId(12) val chatflag: Int = 0,
        @ProtoId(13) val chatupCount: Int = 0,
        @ProtoId(14) val charm: Int = 0,
        @ProtoId(15) val charmLevel: Int = 0,
        @ProtoId(16) val pubNumber: Int = 0,
        @ProtoId(17) val msgCommonLabel: AppointDefine.CommonLabel? = null,
        @ProtoId(18) val recentVistorTime: Int = 0,
        @ProtoId(19) val strangerDeclare: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20) val friendUin: Long = 0L,
        @ProtoId(21) val historyFlag: Int = 0,
        @ProtoId(22) val followflag: Long = 0L
    ) : ProtoBuf

    @Serializable
    class HotUserFeed(
        @ProtoId(1) val feedId: String = "",
        @ProtoId(2) val praiseCount: Int = 0,
        @ProtoId(3) val publishUid: Long = 0L,
        @ProtoId(4) val publishTime: Int = 0
    ) : ProtoBuf

    @Serializable
    class FreshFeedInfo(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val time: Int = 0,
        @ProtoId(3) val feedId: String = "",
        @ProtoId(4) val feedType: Long = 0L
    ) : ProtoBuf

    @Serializable
    class CommonLabel(
        @ProtoId(1) val lableId: Int = 0,
        @ProtoId(2) val lableMsgPre: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val lableMsgLast: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val interstName: List<ByteArray>? = null,
        @ProtoId(5) val interstType: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    class Face(
        @ProtoId(1) val index: Int = 0
    ) : ProtoBuf

    @Serializable
    class StrangerInfo(
        @ProtoId(1) val tinyid: Long = 0L,
        @ProtoId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val age: Int = 0,
        @ProtoId(4) val gender: Int = 0,
        @ProtoId(5) val dating: Int = 0,
        @ProtoId(6) val listIdx: Int = 0,
        @ProtoId(7) val constellation: String = "",
        @ProtoId(8) val profession: Int = 0,
        @ProtoId(9) val marriage: Int = 0,
        @ProtoId(10) val vipinfo: String = "",
        @ProtoId(11) val recommend: Int = 0,
        @ProtoId(12) val godflag: Int = 0,
        @ProtoId(13) val charm: Int = 0,
        @ProtoId(14) val charmLevel: Int = 0,
        @ProtoId(15) val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class HotTopic(
        @ProtoId(1) val id: Long = 0L,
        @ProtoId(2) val title: String = "",
        @ProtoId(3) val topicType: Long = 0L,
        @ProtoId(4) val total: Long = 0L,
        @ProtoId(5) val times: Long = 0L,
        @ProtoId(6) val historyTimes: Long = 0L,
        @ProtoId(7) val bgUrl: String = "",
        @ProtoId(8) val url: String = "",
        @ProtoId(9) val extraInfo: String = ""
    ) : ProtoBuf

    @Serializable
    class DateEvent(
        @ProtoId(1) val eventId: Long = 0L,
        @ProtoId(2) val time: Int = 0,
        @ProtoId(3) val type: Int = 0,
        @ProtoId(4) val msgUserInfo: AppointDefine.StrangerInfo? = null,
        @ProtoId(5) val msgDateInfo: AppointDefine.AppointInfo? = null,
        @ProtoId(6) val attendIdx: Int = 0,
        @ProtoId(7) val eventTips: String = "",
        @ProtoId(8) val msgComment: AppointDefine.DateComment? = null,
        @ProtoId(9) val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    class AppointInfo(
        @ProtoId(1) val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) val msgAppointment: AppointDefine.AppointContent? = null,
        @ProtoId(3) val appointStatus: Int = 0,
        @ProtoId(4) val joinWording: String = "",
        @ProtoId(5) val viewWording: String = "",
        @ProtoId(6) val unreadCount: Int = 0,
        @ProtoId(7) val owner: Int = 0,
        @ProtoId(8) val join: Int = 0,
        @ProtoId(9) val view: Int = 0,
        @ProtoId(10) val commentWording: String = "",
        @ProtoId(11) val commentNum: Int = 0,
        @ProtoId(12) val attendStatus: Int = 0,
        @ProtoId(13) val msgAppointmentEx: AppointDefine.AppointInfoEx? = null
    ) : ProtoBuf

    @Serializable
    class UserInfo(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val age: Int = 0,
        @ProtoId(4) val gender: Int = 0,
        @ProtoId(5) val avatar: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ReplyInfo(
        @ProtoId(1) val commentId: String = "",
        @ProtoId(2) val msgStrangerInfo: AppointDefine.StrangerInfo? = null
    ) : ProtoBuf
}