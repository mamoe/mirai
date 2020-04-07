package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.protobuf.ProtoId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
class SubMsgType0x27 : ProtoBuf {
    @Serializable
    class AddGroup(
        @ProtoId(1) val groupid: Int = 0,
        @ProtoId(2) val sortid: Int = 0,
        @ProtoId(3) val groupname: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class AppointmentNotify(
        @ProtoId(1) val fromUin: Long = 0L,
        @ProtoId(2) val appointId: String = "",
        @ProtoId(3) val notifytype: Int = 0,
        @ProtoId(4) val tipsContent: String = "",
        @ProtoId(5) val unreadCount: Int = 0,
        @ProtoId(6) val joinWording: String = "",
        @ProtoId(7) val viewWording: String = "",
        @ProtoId(8) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val eventInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val nearbyEventInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val feedEventInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class BinaryMsg(
        @ProtoId(1) val opType: Int = 0,
        @ProtoId(2) val opValue: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ChatMatchInfo(
        @ProtoId(1) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val uin: Long = 0L,
        @ProtoId(3) val matchUin: Long = 0L,
        @ProtoId(4) val tipsWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val leftChatTime: Int = 0,
        @ProtoId(6) val timeStamp: Long = 0L,
        @ProtoId(7) val matchExpiredTime: Int = 0,
        @ProtoId(8) val c2cExpiredTime: Int = 0,
        @ProtoId(9) val matchCount: Int = 0,
        @ProtoId(10) val nick: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ConfMsgRoamFlag(
        @ProtoId(1) val confid: Long = 0L,
        @ProtoId(2) val flag: Int = 0,
        @ProtoId(3) val timestamp: Long = 0L
    ) : ProtoBuf

    @Serializable
    class DaRenNotify(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val loginDays: Int = 0,
        @ProtoId(3) val days: Int = 0,
        @ProtoId(4) val isYestodayLogin: Int = 0,
        @ProtoId(5) val isTodayLogin: Int = 0
    ) : ProtoBuf

    @Serializable
    class DelFriend(
        @ProtoId(1) val uint64Uins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    class DelGroup(
        @ProtoId(1) val groupid: Int = 0
    ) : ProtoBuf

    @Serializable
    class FanpaiziNotify(
        @ProtoId(1) val fromUin: Long = 0L,
        @ProtoId(2) val fromNick: String = "",
        @ProtoId(3) val tipsContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ForwardBody(
        @ProtoId(1) val notifyType: Int = 0,
        @ProtoId(2) val opType: Int = 0,
        @ProtoId(3) val msgAddGroup: AddGroup? = null,
        @ProtoId(4) val msgDelGroup: DelGroup? = null,
        @ProtoId(5) val msgModGroupName: ModGroupName? = null,
        @ProtoId(6) val msgModGroupSort: ModGroupSort? = null,
        @ProtoId(7) val msgModFriendGroup: ModFriendGroup? = null,
        @ProtoId(8) val msgModProfile: ModProfile? = null,
        @ProtoId(9) val msgModFriendRemark: ModFriendRemark? = null,
        @ProtoId(10) val msgModLongNick: ModLongNick? = null,
        @ProtoId(11) val msgModCustomFace: ModCustomFace? = null,
        @ProtoId(12) val msgModGroupProfile: ModGroupProfile? = null,
        @ProtoId(13) val msgModGroupMemberProfile: ModGroupMemberProfile? = null,
        @ProtoId(14) val msgDelFriend: DelFriend? = null,
        @ProtoId(15) val msgRoamPriv: ModFrdRoamPriv? = null,
        @ProtoId(16) val msgGrpMsgRoamFlag: GrpMsgRoamFlag? = null,
        @ProtoId(17) val msgConfMsgRoamFlag: ConfMsgRoamFlag? = null,
        @ProtoId(18) val msgModRichLongNick: ModLongNick? = null,
        @ProtoId(19) val msgBinPkg: BinaryMsg? = null,
        @ProtoId(20) val msgModFriendRings: ModSnsGeneralInfo? = null,
        @ProtoId(21) val msgModConfProfile: ModConfProfile? = null,
        @ProtoId(22) val msgModFriendFlag: SnsUpdateFlag? = null,
        @ProtoId(23) val msgAppointmentNotify: AppointmentNotify? = null,
        @ProtoId(25) val msgDarenNotify: DaRenNotify? = null,
        @ProtoId(26) val msgNewComeinUserNotify: NewComeinUserNotify? = null,
        @ProtoId(200) val msgPushSearchDev: PushSearchDev? = null,
        @ProtoId(201) val msgPushReportDev: PushReportDev? = null,
        @ProtoId(202) val msgQqPayPush: QQPayPush? = null,
        @ProtoId(203) val redpointInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(204) val msgHotFriendNotify: HotFriendNotify? = null,
        @ProtoId(205) val msgPraiseRankNotify: PraiseRankNotify? = null,
        @ProtoId(210) val msgCampusNotify: MQQCampusNotify? = null,
        @ProtoId(211) val msgModRichLongNickEx: ModLongNick? = null,
        @ProtoId(212) val msgChatMatchInfo: ChatMatchInfo? = null,
        @ProtoId(214) val msgFrdCustomOnlineStatusChange: FrdCustomOnlineStatusChange? = null,
        @ProtoId(2000) val msgFanpanziNotify: FanpaiziNotify? = null
    ) : ProtoBuf

    @Serializable
    class FrdCustomOnlineStatusChange(
        @ProtoId(1) val uin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class FriendGroup(
        @ProtoId(1) val fuin: Long = 0L,
        @ProtoId(2) val uint32OldGroupId: List<Int>? = null,
        @ProtoId(3) val uint32NewGroupId: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    class FriendRemark(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val fuin: Long = 0L,
        @ProtoId(3) val rmkName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    class GPS(
        @ProtoId(1) val int32Lat: Int = 900000000,
        @ProtoId(2) val int32Lon: Int = 900000000,
        @ProtoId(3) val int32Alt: Int = -10000000,
        @ProtoId(4) val int32Type: Int = 0
    ) : ProtoBuf

    @Serializable
    class GroupMemberProfileInfo(
        @ProtoId(1) val field: Int = 0,
        @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GroupProfileInfo(
        @ProtoId(1) val field: Int = 0,
        @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GroupSort(
        @ProtoId(1) val groupid: Int = 0,
        @ProtoId(2) val sortid: Int = 0
    ) : ProtoBuf

    @Serializable
    class GrpMsgRoamFlag(
        @ProtoId(1) val groupcode: Long = 0L,
        @ProtoId(2) val flag: Int = 0,
        @ProtoId(3) val timestamp: Long = 0L
    ) : ProtoBuf

    @Serializable
    class HotFriendNotify(
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
        @ProtoId(17) val lastQzoneTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    class ModConfProfile(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val confUin: Int = 0,
        @ProtoId(3) val msgProfileInfos: List<ProfileInfo>? = null
    ) : ProtoBuf

    @Serializable
    class ModCustomFace(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val uin: Long = 0L,
        @ProtoId(3) val groupCode: Long = 0L,
        @ProtoId(4) val cmdUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class ModFrdRoamPriv(
        @ProtoId(1) val msgRoamPriv: List<OneRoamPriv>? = null
    ) : ProtoBuf

    @Serializable
    class ModFriendGroup(
        @ProtoId(1) val msgFrdGroup: List<FriendGroup>? = null
    ) : ProtoBuf

    @Serializable
    class ModFriendRemark(
        @ProtoId(1) val msgFrdRmk: List<FriendRemark>? = null
    ) : ProtoBuf

    @Serializable
    class ModGroupMemberProfile(
        @ProtoId(1) val groupUin: Long = 0L,
        @ProtoId(2) val uin: Long = 0L,
        @ProtoId(3) val msgGroupMemberProfileInfos: List<GroupMemberProfileInfo>? = null,
        @ProtoId(4) val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    class ModGroupName(
        @ProtoId(1) val groupid: Int = 0,
        @ProtoId(2) val groupname: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ModGroupProfile(
        @ProtoId(1) val groupUin: Long = 0L,
        @ProtoId(2) val msgGroupProfileInfos: List<GroupProfileInfo>? = null,
        @ProtoId(3) val groupCode: Long = 0L,
        @ProtoId(4) val cmdUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class ModGroupSort(
        @ProtoId(1) val msgGroupsort: List<GroupSort>? = null
    ) : ProtoBuf

    @Serializable
    class ModLongNick(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ModProfile(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val msgProfileInfos: List<ProfileInfo>? = null
    ) : ProtoBuf

    @Serializable
    class ModSnsGeneralInfo(
        @ProtoId(1) val msgSnsGeneralInfos: List<SnsUpateBuffer>? = null
    ) : ProtoBuf

    @Serializable
    class MQQCampusNotify(
        @ProtoId(1) val fromUin: Long = 0L,
        @ProtoId(2) val wording: String = "",
        @ProtoId(3) val target: String = "",
        @ProtoId(4) val type: Int = 0,
        @ProtoId(5) val source: String = ""
    ) : ProtoBuf

    @Serializable
    class MsgBody(
        @ProtoId(1) val msgModInfos: List<ForwardBody>? = null
    ) : ProtoBuf

    @Serializable
    class NewComeinUser(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val isFrd: Int = 0,
        @ProtoId(3) val remark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val nick: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class NewComeinUserNotify(
        @ProtoId(1) val msgType: Int = 0,
        @ProtoId(2) val boolStrongNotify: Boolean = false,
        @ProtoId(3) val pushTime: Int = 0,
        @ProtoId(4) val msgNewComeinUser: NewComeinUser? = null,
        @ProtoId(5) val msgNewGroup: NewGroup? = null,
        @ProtoId(6) val msgNewGroupUser: NewGroupUser? = null
    ) : ProtoBuf

    @Serializable
    class NewGroup(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val groupName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val ownerUin: Long = 0L,
        @ProtoId(4) val ownerNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val distance: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class NewGroupUser(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val int32Sex: Int = 0,
        @ProtoId(3) val int32Age: Int = 0,
        @ProtoId(4) val nick: String = "",
        @ProtoId(5) val distance: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class OneRoamPriv(
        @ProtoId(1) val fuin: Long = 0L,
        @ProtoId(2) val privTag: Int = 0,
        @ProtoId(3) val privValue: Int = 0
    ) : ProtoBuf

    @Serializable
    class PraiseRankNotify(
        @ProtoId(11) val isChampion: Int = 0,
        @ProtoId(12) val rankNum: Int = 0,
        @ProtoId(13) val msg: String = ""
    ) : ProtoBuf

    @Serializable
    class ProfileInfo(
        @ProtoId(1) val field: Int = 0,
        @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PushReportDev(
        @ProtoId(1) val msgType: Int = 0,
        @ProtoId(4) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val reportMaxNum: Int = 200,
        @ProtoId(6) val sn: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PushSearchDev(
        @ProtoId(1) val msgType: Int = 0,
        @ProtoId(2) val msgGpsInfo: GPS? = null,
        @ProtoId(3) val devTime: Int = 0,
        @ProtoId(4) val pushTime: Int = 0,
        @ProtoId(5) val din: Long = 0L,
        @ProtoId(6) val data: String = ""
    ) : ProtoBuf

    @Serializable
    class QQPayPush(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val boolPayOk: Boolean = false
    ) : ProtoBuf

    @Serializable
    class SnsUpateBuffer(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val code: Long = 0L,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(400) val msgSnsUpdateItem: List<SnsUpdateItem>? = null,
        @ProtoId(401) val uint32Idlist: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    class SnsUpdateFlag(
        @ProtoId(1) val msgUpdateSnsFlag: List<SnsUpdateOneFlag>? = null
    ) : ProtoBuf

    @Serializable
    class SnsUpdateItem(
        @ProtoId(1) val updateSnsType: Int = 0,
        @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class SnsUpdateOneFlag(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val id: Long = 0L,
        @ProtoId(3) val flag: Int = 0
    ) : ProtoBuf
}
