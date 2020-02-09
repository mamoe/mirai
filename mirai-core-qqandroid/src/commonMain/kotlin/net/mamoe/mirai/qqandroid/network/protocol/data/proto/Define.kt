/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
class Common : ProtoBuf {
    @Serializable
    class BindInfo(
        @SerialId(1) val friUin: Long = 0L,
        @SerialId(2) val friNick: String = "",
        @SerialId(3) val time: Long = 0L,
        @SerialId(4) val bindStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    class MedalInfo(
        @SerialId(1) val id: Int = 0,
        @SerialId(2) val type: Int = 0,
        @SerialId(4) val seq: Long = 0,
        @SerialId(5) val name: String = "",
        @SerialId(6) val newflag: Int = 0,
        @SerialId(7) val time: Long = 0L,
        @SerialId(8) val msgBindFri: Common.BindInfo? = null,
        @SerialId(11) val desc: String = "",
        @SerialId(31) val level: Int = 0,
        @SerialId(36) val taskinfos: List<Common.MedalTaskInfo>? = null,
        @SerialId(40) val point: Int = 0,
        @SerialId(41) val pointLevel2: Int = 0,
        @SerialId(42) val pointLevel3: Int = 0,
        @SerialId(43) val seqLevel2: Long = 0,
        @SerialId(44) val seqLevel3: Long = 0,
        @SerialId(45) val timeLevel2: Long = 0L,
        @SerialId(46) val timeLevel3: Long = 0L,
        @SerialId(47) val descLevel2: String = "",
        @SerialId(48) val descLevel3: String = "",
        @SerialId(49) val endtime: Int = 0,
        @SerialId(50) val detailUrl: String = "",
        @SerialId(51) val detailUrl2: String = "",
        @SerialId(52) val detailUrl3: String = "",
        @SerialId(53) val taskDesc: String = "",
        @SerialId(54) val taskDesc2: String = "",
        @SerialId(55) val taskDesc3: String = "",
        @SerialId(56) val levelCount: Int = 0,
        @SerialId(57) val noProgress: Int = 0,
        @SerialId(58) val resource: String = "",
        @SerialId(59) val fromuinLevel: Int = 0,
        @SerialId(60) val unread: Int = 0,
        @SerialId(61) val unread2: Int = 0,
        @SerialId(62) val unread3: Int = 0
    ) : ProtoBuf

    @Serializable
    class MedalTaskInfo(
        @SerialId(1) val taskid: Int = 0,
        @SerialId(32) val int32TaskValue: Int = 0,
        @SerialId(33) val tarValue: Int = 0,
        @SerialId(34) val tarValueLevel2: Int = 0,
        @SerialId(35) val tarValueLevel3: Int = 0
    ) : ProtoBuf
}

@Serializable
class AppointDefine : ProtoBuf {
    @Serializable
    class ADFeedContent(
        @SerialId(1) val msgUserInfo: AppointDefine.UserInfo? = null,
        @SerialId(2) val strPicUrl: List<String> = listOf(),
        @SerialId(3) val msgText: AppointDefine.RichText? = null,
        @SerialId(4) val attendInfo: String = "",
        @SerialId(5) val actionUrl: String = "",
        @SerialId(6) val publishTime: Int = 0,
        @SerialId(7) val msgHotTopicList: AppointDefine.HotTopicList? = null,
        @SerialId(8) val moreUrl: String = "",
        @SerialId(9) val recordDuration: String = ""
    ) : ProtoBuf

    @Serializable
    class RichText(
        @SerialId(1) val msgElems: List<AppointDefine.Elem>? = null
    ) : ProtoBuf

    @Serializable
    class RankEvent(
        @SerialId(1) val listtype: Int = 0,
        @SerialId(2) val notifytype: Int = 0,
        @SerialId(3) val eventtime: Int = 0,
        @SerialId(4) val seq: Int = 0,
        @SerialId(5) val notifyTips: String = ""
    ) : ProtoBuf

    @Serializable
    class Wifi(
        @SerialId(1) val mac: Long = 0L,
        @SerialId(2) val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    class InterestItem(
        @SerialId(1) val tagId: Long = 0L,
        @SerialId(2) val tagName: String = "",
        @SerialId(3) val tagIconUrl: String = "",
        @SerialId(4) val tagHref: String = "",
        @SerialId(5) val tagBackColor: String = "",
        @SerialId(6) val tagFontColor: String = "",
        @SerialId(7) val tagVid: String = "",
        @SerialId(8) val tagType: Int = 0,
        @SerialId(9) val addTime: Int = 0,
        @SerialId(10) val tagCategory: String = "",
        @SerialId(11) val tagOtherUrl: String = "",
        @SerialId(12) val bid: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShopID(
        @SerialId(1) val shopid: String = "",
        @SerialId(2) val sp: Int = 0
    ) : ProtoBuf

    @Serializable
    class FeedComment(
        @SerialId(1) val commentId: String = "",
        @SerialId(2) val feedId: String = "",
        @SerialId(3) val msgPublisherInfo: AppointDefine.StrangerInfo? = null,
        @SerialId(4) val time: Int = 0,
        @SerialId(6) val msgReplyInfo: AppointDefine.ReplyInfo? = null,
        @SerialId(7) val flag: Int = 0,
        @SerialId(8) val msgContent: AppointDefine.RichText? = null,
        @SerialId(9) val hot: Int = 0
    ) : ProtoBuf

    @Serializable
    class ADFeed(
        @SerialId(1) val taskId: Int = 0,
        @SerialId(2) val style: Int = 0,
        @SerialId(3) val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class Cell(
        @SerialId(1) val int32Mcc: Int = -1,
        @SerialId(2) val int32Mnc: Int = -1,
        @SerialId(3) val int32Lac: Int = -1,
        @SerialId(4) val int32Cellid: Int = -1,
        @SerialId(5) val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    class RecentVistorEvent(
        @SerialId(1) val eventtype: Int = 0,
        @SerialId(2) val eventTinyid: Long = 0L,
        @SerialId(3) val unreadCount: Int = 0
    ) : ProtoBuf

    @Serializable
    class OrganizerInfo(
        @SerialId(1) val hostName: String = "",
        @SerialId(2) val hostUrl: String = "",
        @SerialId(3) val hostCover: String = ""
    ) : ProtoBuf

    @Serializable
    class InterestTag(
        @SerialId(1) val tagType: Int = 0,
        @SerialId(2) val msgTagList: List<AppointDefine.InterestItem>? = null
    ) : ProtoBuf

    @Serializable
    class AppointInfoEx(
        @SerialId(1) val feedsPicUrl: String = "",
        @SerialId(2) val feedsUrl: String = "",
        @SerialId(3) val detailTitle: String = "",
        @SerialId(4) val detailDescribe: String = "",
        @SerialId(5) val showPublisher: Int = 0,
        @SerialId(6) val detailPicUrl: String = "",
        @SerialId(7) val detailUrl: String = "",
        @SerialId(8) val showAttend: Int = 0
    ) : ProtoBuf

    @Serializable
    class DateComment(
        @SerialId(1) val commentId: String = "",
        @SerialId(2) val msgAppointId: AppointDefine.AppointID? = null,
        @SerialId(3) val msgPublisherInfo: AppointDefine.StrangerInfo? = null,
        @SerialId(4) val time: Int = 0,
        @SerialId(6) val msgReplyInfo: AppointDefine.ReplyInfo? = null,
        @SerialId(7) val flag: Int = 0,
        @SerialId(8) val msgContent: AppointDefine.RichText? = null
    ) : ProtoBuf

    @Serializable
    class AppointContent(
        @SerialId(1) val appointSubject: Int = 0,
        @SerialId(2) val payType: Int = 0,
        @SerialId(3) val appointDate: Int = 0,
        @SerialId(4) val appointGender: Int = 0,
        @SerialId(5) val appointIntroduce: String = "",
        @SerialId(6) val msgAppointAddress: AppointDefine.AddressInfo? = null,
        @SerialId(7) val msgTravelInfo: AppointDefine.TravelInfo? = null
    ) : ProtoBuf

    @Serializable
    class FeedInfo(
        @SerialId(1) val feedType: Long = 0L,
        @SerialId(2) val feedId: String = "",
        @SerialId(3) val msgFeedContent: AppointDefine.FeedContent? = null,
        @SerialId(4) val msgTopicInfo: AppointDefine.NearbyTopic? = null,
        @SerialId(5) val publishTime: Long = 0,
        @SerialId(6) val praiseCount: Int = 0,
        @SerialId(7) val praiseFlag: Int = 0,
        @SerialId(8) val msgPraiseUser: List<AppointDefine.StrangerInfo>? = null,
        @SerialId(9) val commentCount: Int = 0,
        @SerialId(10) val msgCommentList: List<AppointDefine.FeedComment>? = null,
        @SerialId(11) val commentRetAll: Int = 0,
        @SerialId(12) val hotFlag: Int = 0,
        @SerialId(13) val svrReserved: Long = 0L,
        @SerialId(14) val msgHotEntry: AppointDefine.HotEntry? = null
    ) : ProtoBuf

    @Serializable
    class HotTopicList(
        @SerialId(1) val topicList: List<AppointDefine.HotTopic>? = null
    ) : ProtoBuf

    @Serializable
    class FeedContent(
        @SerialId(1) val strPicUrl: List<String> = listOf(),
        @SerialId(2) val msgText: AppointDefine.RichText? = null,
        @SerialId(3) val hrefUrl: String = "",
        @SerialId(5) val groupName: String = "",
        @SerialId(6) val groupBulletin: String = "",
        @SerialId(7) val feedType: Int = 0,
        @SerialId(8) val poiId: String = "",
        @SerialId(9) val poiTitle: String = "",
        @SerialId(20) val effectiveTime: Int = 0,
        @SerialId(21) val expiationTime: Int = 0,
        @SerialId(22) val msgLocale: AppointDefine.LocaleInfo? = null,
        @SerialId(23) val feedsIndex: Int = 0,
        @SerialId(24) val msgAd: AppointDefine.ADFeed? = null,
        @SerialId(25) val privateData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class TravelInfo(
        @SerialId(1) val msgDepartLocale: AppointDefine.LocaleInfo? = null,
        @SerialId(2) val msgDestination: AppointDefine.LocaleInfo? = null,
        @SerialId(3) val vehicle: Int = 0,
        @SerialId(4) val partnerCount: Int = 0,
        @SerialId(5) val placePicUrl: String = "",
        @SerialId(6) val placeUrl: String = ""
    ) : ProtoBuf

    @Serializable
    class RecentFreshFeed(
        @SerialId(1) val freshFeedInfo: List<AppointDefine.FreshFeedInfo>? = null,
        @SerialId(2) val uid: Long = 0L
    ) : ProtoBuf

    @Serializable
    class GPS(
        @SerialId(1) val int32Lat: Int = 900000000,
        @SerialId(2) val int32Lon: Int = 900000000,
        @SerialId(3) val int32Alt: Int = -10000000,
        @SerialId(4) val int32Type: Int = 0
    ) : ProtoBuf

    @Serializable
    class AppointID(
        @SerialId(1) val requestId: String = ""
    ) : ProtoBuf

    @Serializable
    class LocaleInfo(
        @SerialId(1) val name: String = "",
        @SerialId(2) val country: String = "",
        @SerialId(3) val province: String = "",
        @SerialId(4) val city: String = "",
        @SerialId(5) val region: String = "",
        @SerialId(6) val poi: String = "",
        @SerialId(7) val msgGps: AppointDefine.GPS? = null,
        @SerialId(8) val address: String = ""
    ) : ProtoBuf

    @Serializable
    class LBSInfo(
        @SerialId(1) val msgGps: AppointDefine.GPS? = null,
        @SerialId(2) val msgWifis: List<AppointDefine.Wifi>? = null,
        @SerialId(3) val msgCells: List<AppointDefine.Cell>? = null
    ) : ProtoBuf

    @Serializable
    class FeedEvent(
        @SerialId(1) val eventId: Long = 0L,
        @SerialId(2) val time: Int = 0,
        @SerialId(3) val eventtype: Int = 0,
        @SerialId(4) val msgUserInfo: AppointDefine.StrangerInfo? = null,
        @SerialId(5) val msgFeedInfo: AppointDefine.FeedInfo? = null,
        @SerialId(6) val eventTips: String = "",
        @SerialId(7) val msgComment: AppointDefine.FeedComment? = null,
        @SerialId(8) val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    class FeedsCookie(
        @SerialId(1) val strList: List<String> = listOf(),
        @SerialId(2) val pose: Int = 0,
        @SerialId(3) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val uint64Topics: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    class NearbyTopic(
        @SerialId(1) val topicId: Long = 0L,
        @SerialId(2) val topic: String = "",
        @SerialId(3) val foreword: String = "",
        @SerialId(4) val createTime: Int = 0,
        @SerialId(5) val updateTime: Int = 0,
        @SerialId(6) val hotFlag: Int = 0,
        @SerialId(7) val buttonStyle: Int = 0,
        @SerialId(8) val buttonSrc: String = "",
        @SerialId(9) val backgroundSrc: String = "",
        @SerialId(10) val attendeeInfo: String = "",
        @SerialId(11) val index: Int = 0,
        @SerialId(12) val publishScope: Int = 0,
        @SerialId(13) val effectiveTime: Int = 0,
        @SerialId(14) val expiationTime: Int = 0,
        @SerialId(15) val pushedUsrCount: Int = 0,
        @SerialId(16) val timerangeLeft: Int = 0,
        @SerialId(17) val timerangeRight: Int = 0,
        @SerialId(18) val area: String = ""
    ) : ProtoBuf

    @Serializable
    class NearbyEvent(
        @SerialId(1) val eventtype: Int = 0,
        @SerialId(2) val msgRankevent: AppointDefine.RankEvent? = null,
        @SerialId(3) val eventUin: Long = 0L,
        @SerialId(4) val eventTinyid: Long = 0L
    ) : ProtoBuf

    @Serializable
    class Feed(
        @SerialId(1) val msgUserInfo: AppointDefine.PublisherInfo? = null,
        @SerialId(2) val msgFeedInfo: AppointDefine.FeedInfo? = null,
        @SerialId(3) val ownerFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class ActivityInfo(
        @SerialId(2) val name: String = "",
        @SerialId(3) val cover: String = "",
        @SerialId(4) val url: String = "",
        @SerialId(5) val startTime: Int = 0,
        @SerialId(6) val endTime: Int = 0,
        @SerialId(7) val locName: String = "",
        @SerialId(8) val enroll: Long = 0L,
        @SerialId(9) val createUin: Long = 0L,
        @SerialId(10) val createTime: Int = 0,
        @SerialId(11) val organizerInfo: AppointDefine.OrganizerInfo = OrganizerInfo(),
        @SerialId(12) val flag: Long? = null
    ) : ProtoBuf

    @Serializable
    class HotEntry(
        @SerialId(1) val openFlag: Int = 0,
        @SerialId(2) val restTime: Int = 0,
        @SerialId(3) val foreword: String = "",
        @SerialId(4) val backgroundSrc: String = ""
    ) : ProtoBuf

    @Serializable
    class UserFeed(
        @SerialId(1) val msgUserInfo: AppointDefine.PublisherInfo? = null,
        @SerialId(2) val msgFeedInfo: AppointDefine.FeedInfo? = null,
        @SerialId(3) val ownerFlag: Int = 0,
        @SerialId(4) val msgActivityInfo: AppointDefine.ActivityInfo? = null
    ) : ProtoBuf

    @Serializable
    class Elem(
        @SerialId(1) val content: String = "",
        @SerialId(2) val msgFaceInfo: AppointDefine.Face? = null
    ) : ProtoBuf

    @Serializable
    class HotFreshFeedList(
        @SerialId(1) val msgFeeds: List<AppointDefine.HotUserFeed>? = null,
        @SerialId(2) val updateTime: Int = 0
    ) : ProtoBuf

    @Serializable
    class RptInterestTag(
        @SerialId(1) val interestTags: List<AppointDefine.InterestTag>? = null
    ) : ProtoBuf

    @Serializable
    class AddressInfo(
        @SerialId(1) val companyZone: String = "",
        @SerialId(2) val companyName: String = "",
        @SerialId(3) val companyAddr: String = "",
        @SerialId(4) val companyPicUrl: String = "",
        @SerialId(5) val companyUrl: String = "",
        @SerialId(6) val msgCompanyId: AppointDefine.ShopID? = null
    ) : ProtoBuf

    @Serializable
    class PublisherInfo(
        @SerialId(1) val tinyid: Long = 0L,
        @SerialId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val age: Int = 0,
        @SerialId(4) val gender: Int = 0,
        @SerialId(5) val constellation: String = "",
        @SerialId(6) val profession: Int = 0,
        @SerialId(7) val distance: String = "",
        @SerialId(8) val marriage: Int = 0,
        @SerialId(9) val vipinfo: String = "",
        @SerialId(10) val recommend: Int = 0,
        @SerialId(11) val godflag: Int = 0,
        @SerialId(12) val chatflag: Int = 0,
        @SerialId(13) val chatupCount: Int = 0,
        @SerialId(14) val charm: Int = 0,
        @SerialId(15) val charmLevel: Int = 0,
        @SerialId(16) val pubNumber: Int = 0,
        @SerialId(17) val msgCommonLabel: AppointDefine.CommonLabel? = null,
        @SerialId(18) val recentVistorTime: Int = 0,
        @SerialId(19) val strangerDeclare: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(20) val friendUin: Long = 0L,
        @SerialId(21) val historyFlag: Int = 0,
        @SerialId(22) val followflag: Long = 0L
    ) : ProtoBuf

    @Serializable
    class HotUserFeed(
        @SerialId(1) val feedId: String = "",
        @SerialId(2) val praiseCount: Int = 0,
        @SerialId(3) val publishUid: Long = 0L,
        @SerialId(4) val publishTime: Int = 0
    ) : ProtoBuf

    @Serializable
    class FreshFeedInfo(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val time: Int = 0,
        @SerialId(3) val feedId: String = "",
        @SerialId(4) val feedType: Long = 0L
    ) : ProtoBuf

    @Serializable
    class CommonLabel(
        @SerialId(1) val lableId: Int = 0,
        @SerialId(2) val lableMsgPre: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val lableMsgLast: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val interstName: List<ByteArray>? = null,
        @SerialId(5) val interstType: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    class Face(
        @SerialId(1) val index: Int = 0
    ) : ProtoBuf

    @Serializable
    class StrangerInfo(
        @SerialId(1) val tinyid: Long = 0L,
        @SerialId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val age: Int = 0,
        @SerialId(4) val gender: Int = 0,
        @SerialId(5) val dating: Int = 0,
        @SerialId(6) val listIdx: Int = 0,
        @SerialId(7) val constellation: String = "",
        @SerialId(8) val profession: Int = 0,
        @SerialId(9) val marriage: Int = 0,
        @SerialId(10) val vipinfo: String = "",
        @SerialId(11) val recommend: Int = 0,
        @SerialId(12) val godflag: Int = 0,
        @SerialId(13) val charm: Int = 0,
        @SerialId(14) val charmLevel: Int = 0,
        @SerialId(15) val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class HotTopic(
        @SerialId(1) val id: Long = 0L,
        @SerialId(2) val title: String = "",
        @SerialId(3) val topicType: Long = 0L,
        @SerialId(4) val total: Long = 0L,
        @SerialId(5) val times: Long = 0L,
        @SerialId(6) val historyTimes: Long = 0L,
        @SerialId(7) val bgUrl: String = "",
        @SerialId(8) val url: String = "",
        @SerialId(9) val extraInfo: String = ""
    ) : ProtoBuf

    @Serializable
    class DateEvent(
        @SerialId(1) val eventId: Long = 0L,
        @SerialId(2) val time: Int = 0,
        @SerialId(3) val type: Int = 0,
        @SerialId(4) val msgUserInfo: AppointDefine.StrangerInfo? = null,
        @SerialId(5) val msgDateInfo: AppointDefine.AppointInfo? = null,
        @SerialId(6) val attendIdx: Int = 0,
        @SerialId(7) val eventTips: String = "",
        @SerialId(8) val msgComment: AppointDefine.DateComment? = null,
        @SerialId(9) val cancelEventId: Long = 0L
    ) : ProtoBuf

    @Serializable
    class AppointInfo(
        @SerialId(1) val msgAppointId: AppointDefine.AppointID? = null,
        @SerialId(2) val msgAppointment: AppointDefine.AppointContent? = null,
        @SerialId(3) val appointStatus: Int = 0,
        @SerialId(4) val joinWording: String = "",
        @SerialId(5) val viewWording: String = "",
        @SerialId(6) val unreadCount: Int = 0,
        @SerialId(7) val owner: Int = 0,
        @SerialId(8) val join: Int = 0,
        @SerialId(9) val view: Int = 0,
        @SerialId(10) val commentWording: String = "",
        @SerialId(11) val commentNum: Int = 0,
        @SerialId(12) val attendStatus: Int = 0,
        @SerialId(13) val msgAppointmentEx: AppointDefine.AppointInfoEx? = null
    ) : ProtoBuf

    @Serializable
    class UserInfo(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val age: Int = 0,
        @SerialId(4) val gender: Int = 0,
        @SerialId(5) val avatar: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ReplyInfo(
        @SerialId(1) val commentId: String = "",
        @SerialId(2) val msgStrangerInfo: AppointDefine.StrangerInfo? = null
    ) : ProtoBuf
}