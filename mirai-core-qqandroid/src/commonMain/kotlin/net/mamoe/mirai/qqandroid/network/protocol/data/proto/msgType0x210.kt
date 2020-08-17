@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class SubMsgType0x43 : ProtoBuf {
    @Serializable
    internal class UpdateTips(
        @ProtoId(1) @JvmField val desc: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

internal class Submsgtype0x101 {
    internal class SubMsgType0x27 : ProtoBuf {
        @Serializable
        internal class ClientReport(
            @ProtoId(1) @JvmField val serviceId: Int = 0,
            @ProtoId(2) @JvmField val contentId: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgPushPlatform: PushPlatform? = null,
            @ProtoId(2) @JvmField val msgClientReport: ClientReport? = null
        ) : ProtoBuf

        @Serializable
        internal class PushPlatform(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val title: String = "",
            @ProtoId(3) @JvmField val desc: String = "",
            @ProtoId(4) @JvmField val targetUrl: String = "",
            @ProtoId(5) @JvmField val forwardType: Int = 0,
            @ProtoId(6) @JvmField val extDataString: String = "",
            @ProtoId(7) @JvmField val extData: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x102 {
    internal class Submsgtype0x102 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val adId: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x103 {
    internal class Submsgtype0x103 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val from: Long = 0L,
            @ProtoId(2) @JvmField val to: Long = 0L,
            @ProtoId(3) @JvmField val topicId: Int = 0,
            @ProtoId(11) @JvmField val curCount: Int = 0,
            @ProtoId(12) @JvmField val totalCount: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x104 {
    internal class Submsgtype0x104 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val from: Long = 0L,
            @ProtoId(2) @JvmField val to: Long = 0L,
            @ProtoId(3) @JvmField val topicId: Int = 0,
            @ProtoId(11) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x108 {
    internal class SubMsgType0x108 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val pushUin: Long = 0L,
            @ProtoId(3) @JvmField val likeCount: Int = 0,
            @ProtoId(4) @JvmField val pushTime: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x10f {
    internal class Submsgtype0x10f : ProtoBuf {
        @Serializable
        internal class KanDianCoinSettingWording(
            @ProtoId(1) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val pictureUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val isOpenCoinEntry: Int = 0,
            @ProtoId(2) @JvmField val canGetCoinCount: Int = 0,
            @ProtoId(3) @JvmField val coinIconUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val msgSettingWording: KanDianCoinSettingWording? = null,
            @ProtoId(5) @JvmField val lastCompletedTaskStamp: Long = 0L,
            @ProtoId(6) @JvmField val dstUin: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x111 {
    internal class SubMsgType0x111 : ProtoBuf {
        @Serializable
        internal class AddFriendSource(
            @ProtoId(1) @JvmField val source: Int = 0,
            @ProtoId(2) @JvmField val subSource: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MayKnowPerson(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val msgIosSource: AddFriendSource? = null,
            @ProtoId(3) @JvmField val msgAndroidSource: AddFriendSource? = null,
            @ProtoId(4) @JvmField val reason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val additive: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val remark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val country: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val province: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val city: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(11) @JvmField val age: Int = 0,
            @ProtoId(12) @JvmField val catelogue: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) @JvmField val alghrithm: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(14) @JvmField val richbuffer: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(15) @JvmField val qzone: Int = 0,
            @ProtoId(16) @JvmField val gender: Int = 0,
            @ProtoId(17) @JvmField val mobileName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(18) @JvmField val token: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Long = 0L,
            @ProtoId(2) @JvmField val msgAddRecommendPersons: List<MayKnowPerson>? = null,
            @ProtoId(3) @JvmField val uint64DelUins: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x113 {
    internal class SubMsgType0x113 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val int32AppId: Int = 0,
            @ProtoId(2) @JvmField val int32TaskId: Int = 0,
            @ProtoId(3) @JvmField val enumTaskOp: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x115 {
    internal class SubMsgType0x115 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val toUin: Long = 0L,
            @ProtoId(3) @JvmField val msgNotifyItem: NotifyItem? = null,
            @ProtoId(4) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NotifyItem(
            @ProtoId(1) @JvmField val ime: Int = 0,
            @ProtoId(2) @JvmField val timeoutS: Int = 0,
            @ProtoId(3) @JvmField val timestamp: Long = 0L,
            @ProtoId(4) @JvmField val eventType: Int = 0,
            @ProtoId(5) @JvmField val interval: Int = 0,
            @ProtoId(6) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x116 {
    internal class Submsgtype0x116 : ProtoBuf {
        @Serializable
        internal class MemberInfo(
            @ProtoId(1) @JvmField val memberUin: Long = 0L,
            @ProtoId(2) @JvmField val inviteTimestamp: Int = 0,
            @ProtoId(3) @JvmField val terminalType: Int = 0,
            @ProtoId(4) @JvmField val clientVersion: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgMemberJoin: List<MemberInfo>? = null,
            @ProtoId(2) @JvmField val msgMemberQuit: List<MemberInfo>? = null,
            @ProtoId(3) @JvmField val groupId: Int = 0,
            @ProtoId(4) @JvmField val roomId: Int = 0,
            @ProtoId(5) @JvmField val inviteListTotalCount: Int = 0,
            @ProtoId(6) @JvmField val enumEventType: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x117 {
    internal class Submsgtype0x117 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val uint32MoudleId: List<Int>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x118 {
    internal class Submsgtype0x118 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val pushType: Int = 0,
            @ProtoId(2) @JvmField val pushData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val timestamp: Int = 0,
            @ProtoId(4) @JvmField val msgSystemNotify: SystemNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class SystemNotify(
            @ProtoId(1) @JvmField val msgSummary: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val filterFlag: Int = 0,
            @ProtoId(3) @JvmField val extendContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val ignorePcActive: Int = 0,
            @ProtoId(5) @JvmField val filterVersion: Int = 0,
            @ProtoId(6) @JvmField val countFlag: Int = 0,
            @ProtoId(7) @JvmField val filterVersionUpperlimitFlag: Int = 0,
            @ProtoId(8) @JvmField val filterVersionUpperlimit: Int = 0,
            @ProtoId(9) @JvmField val customSound: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val admnFlag: Int = 0,
            @ProtoId(11) @JvmField val ignoreWithoutContent: Int = 0,
            @ProtoId(12) @JvmField val msgTitle: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x119 {
    internal class SubMsgType0x119 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val writerUin: Long = 0L,
            @ProtoId(2) @JvmField val creatorUin: Long = 0L,
            @ProtoId(3) @JvmField val richContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val creatorNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11a {
    internal class Submsgtype0x11a : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val enumResult: Int /* enum */ = 0,
            @ProtoId(2) @JvmField val token: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val msgUserData: UserData? = null,
            @ProtoId(5) @JvmField val enumBizType: Int /* enum */ = 1
        ) : ProtoBuf

        @Serializable
        internal class UserData(
            @ProtoId(1) @JvmField val ip: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val fixed32Port: List<Int>? = null,
            @ProtoId(3) @JvmField val ssid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val bssid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val enumPlatform: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11b {
    internal class Submsgtype0x11b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val qrSig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val enumBizType: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11c {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) @JvmField val cmd: Int = 0,
        @ProtoId(2) @JvmField val timestamp: Int = 0,
        @ProtoId(3) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


internal class Submsgtype0x11e {
    internal class SubMsgType0x11e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val reason: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11f {
    internal class SubMsgType0x11f : ProtoBuf {
        @Serializable
        internal class MediaUserInfo(
            @ProtoId(1) @JvmField val toUin: Long = 0L,
            @ProtoId(2) @JvmField val joinState: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val aioType: Int = 0,
            @ProtoId(5) @JvmField val operUin: Long = 0L,
            @ProtoId(6) @JvmField val uint64ToUin: List<Long>? = null,
            @ProtoId(7) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val msgSeq: Long = 0L,
            @ProtoId(9) @JvmField val msgMediaUin: List<MediaUserInfo>? = null,
            @ProtoId(10) @JvmField val msgPerSetting: PersonalSetting? = null,
            @ProtoId(11) @JvmField val playMode: Int = 0,
            @ProtoId(99) @JvmField val mediaType: Int = 0,
            @ProtoId(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PersonalSetting(
            @ProtoId(1) @JvmField val themeId: Int = 0,
            @ProtoId(2) @JvmField val playerId: Int = 0,
            @ProtoId(3) @JvmField val fontId: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x120 {
    internal class SubMsgType0x120 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val srcAppId: Int = 0,
            @ProtoId(2) @JvmField val noticeType: Int = 0,
            @ProtoId(3) @JvmField val reserve1: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val reserve2: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val reserve3: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val noticeTime: Int = 0,
            @ProtoId(7) @JvmField val frdUin: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x122 {
    internal class GrayTipsResv : ProtoBuf {
        @Serializable
        internal class ResvAttr(
            @ProtoId(1) @JvmField val friendBannedFlag: Int = 0
        ) : ProtoBuf
    }

    internal class Submsgtype0x122 : ProtoBuf {
        @Serializable
        internal class MsgBody(
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
        internal class TemplParam(
            @ProtoId(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x123 {
    internal class Submsgtype0x123 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val busiType: Long = 0L,
            @ProtoId(2) @JvmField val busiId: Long = 0L,
            @ProtoId(3) @JvmField val ctrlFlag: Int = 0,
            @ProtoId(4) @JvmField val c2cType: Int = 0,
            @ProtoId(5) @JvmField val serviceType: Int = 0,
            @ProtoId(6) @JvmField val templId: Long = 0L,
            @ProtoId(7) @JvmField val templParam: List<TemplParam>? = null,
            @ProtoId(8) @JvmField val templContent: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class TemplParam(
            @ProtoId(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x125 {
    internal class Submsgtype0x125 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val operUin: Long = 0L,
            @ProtoId(5) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val msgSeq: Long = 0L,
            @ProtoId(99) @JvmField val pushType: Int = 0,
            @ProtoId(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x126 {
    internal class Submsgtype0x126 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgSeq: Long = 0L,
            @ProtoId(2) @JvmField val msgType: Int = 0,
            @ProtoId(3) @JvmField val msgInfo: String = "",
            @ProtoId(100) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x127 {
    internal class Submsgtype0x127 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val seq: Long = 0L,
            @ProtoId(2) @JvmField val actionType: Int = 0,
            @ProtoId(3) @JvmField val friendUin: Long = 0L,
            @ProtoId(4) @JvmField val operUin: Long = 0L,
            @ProtoId(5) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val joinState: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x128 {
    internal class Submsgtype0x128 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val matchUin: Long = 0L,
            @ProtoId(3) @JvmField val tipsWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val timeStamp: Long = 0L,
            @ProtoId(6) @JvmField val matchExpiredTime: Int = 0,
            @ProtoId(7) @JvmField val reportId: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x129 {
    internal class Submsgtype0x129 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val seq: Long = 0L,
            @ProtoId(2) @JvmField val actionType: Int = 0,
            @ProtoId(3) @JvmField val friendUin: Long = 0L,
            @ProtoId(4) @JvmField val operUin: Long = 0L,
            @ProtoId(5) @JvmField val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val joinState: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


/*
internal class Submsgtype0x1a {
    internal class SubMsgType0x1a : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val fromUinInt: Int = 0,
            @ProtoId(3) @JvmField val toUinInt: Int = 0,
            @ProtoId(4) @JvmField val status: Int = 0,
            @ProtoId(5) @JvmField val ttl: Int = 0,
            @ProtoId(6) @JvmField val desc: String = "",
            @ProtoId(7) @JvmField val type: Int = 0,
            @ProtoId(8) @JvmField val captureTimes: Int = 0,
            @ProtoId(9) @JvmField val fromUin: Long = 0L,
            @ProtoId(10) @JvmField val toUin: Long = 0L
        ) : ProtoBuf
    }
}*/


internal class Submsgtype0x26 {
    internal class Submsgtype0x26 : ProtoBuf {
        @Serializable
        internal class AppID(
            @ProtoId(1) @JvmField val appId: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class AppNotifyContent(
            @ProtoId(1) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val optMsgAppNotifyUser: List<AppNotifyUser>? = null
        ) : ProtoBuf

        @Serializable
        internal class AppNotifyUser(
            @ProtoId(1) @JvmField val optUint64Uin: Long = 0L,
            @ProtoId(2) @JvmField val optUint32Flag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class AppTip(
            @ProtoId(1) @JvmField val tipInfoSeq: Int = 0,
            @ProtoId(2) @JvmField val icon: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val iconTimeStamp: Int = 0,
            @ProtoId(4) @JvmField val tooltip: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val reportidClick: Int = 0,
            @ProtoId(6) @JvmField val reportidShow: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class AppTipNotify(
            @ProtoId(1) @JvmField val msgAppTip: AppTip? = null,
            @ProtoId(2) @JvmField val action: Int = 0,
            @ProtoId(3) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val notifySeq: Int = 0,
            @ProtoId(5) @JvmField val neededTipInfoSeq: Int = 0,
            @ProtoId(6) @JvmField val optMsgAppNotifyContent: AppNotifyContent? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val msgSubcmd0x1PushBody: List<SubCmd0x1UpdateAppUnreadNum>? = null,
            @ProtoId(3) @JvmField val msgSubcmd0x2PushBody: SubCmd0x2UpdateAppList? = null,
            @ProtoId(4) @JvmField val msgSubcmd0x3PushBody: SubCmd0x3UpdateDiscussAppInfo? = null,
            @ProtoId(5) @JvmField val msgSubcmd0x4PushBody: SubCmd0x4UpdateApp? = null
        ) : ProtoBuf {
            @Serializable
            internal class SubCmd0x1UpdateAppUnreadNum(
                @ProtoId(1) @JvmField val msgAppId: AppID? = null,
                @ProtoId(2) @JvmField val groupCode: Long = 0L,
                @ProtoType(ProtoNumberType.SIGNED) @ProtoId(3) @JvmField val sint32UnreadNum: Int = 0,
                @ProtoId(4) @JvmField val msgAppTipNotify: AppTipNotify? = null,
                @ProtoType(ProtoNumberType.SIGNED) @ProtoId(5) @JvmField val sint32AlbumCnt: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class SubCmd0x2UpdateAppList(
                @ProtoId(1) @JvmField val msgAppId: List<AppID>? = null,
                @ProtoId(2) @JvmField val uint32TimeStamp: List<Int>? = null,
                @ProtoId(3) @JvmField val groupCode: Long = 0L
            ) : ProtoBuf

            @Serializable
            internal class SubCmd0x3UpdateDiscussAppInfo(
                @ProtoId(1) @JvmField val msgAppId: AppID? = null,
                @ProtoId(2) @JvmField val confUin: Long = 0L,
                @ProtoId(3) @JvmField val msgAppTipNotify: AppTipNotify? = null
            ) : ProtoBuf

            @Serializable
            internal class SubCmd0x4UpdateApp(
                @ProtoId(1) @JvmField val msgAppId: AppID? = null,
                @ProtoId(2) @JvmField val groupCode: Long = 0L,
                @ProtoType(ProtoNumberType.SIGNED) @ProtoId(3) @JvmField val sint32UnreadNum: Int = 0
            ) : ProtoBuf
        }

        @Serializable
        internal class TransferCnt(
            @ProtoId(1) @JvmField val chainId: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x27 {
    internal class SubMsgType0x27 : ProtoBuf {
        @Serializable
        internal class AddGroup(
            @ProtoId(1) @JvmField val groupid: Int = 0,
            @ProtoId(2) @JvmField val sortid: Int = 0,
            @ProtoId(3) @JvmField val groupname: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class AppointmentNotify(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val appointId: String = "",
            @ProtoId(3) @JvmField val notifytype: Int = 0,
            @ProtoId(4) @JvmField val tipsContent: String = "",
            @ProtoId(5) @JvmField val unreadCount: Int = 0,
            @ProtoId(6) @JvmField val joinWording: String = "",
            @ProtoId(7) @JvmField val viewWording: String = "",
            @ProtoId(8) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val eventInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val nearbyEventInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(11) @JvmField val feedEventInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class BinaryMsg(
            @ProtoId(1) @JvmField val opType: Int = 0,
            @ProtoId(2) @JvmField val opValue: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ChatMatchInfo(
            @ProtoId(1) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val uin: Long = 0L,
            @ProtoId(3) @JvmField val matchUin: Long = 0L,
            @ProtoId(4) @JvmField val tipsWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val leftChatTime: Int = 0,
            @ProtoId(6) @JvmField val timeStamp: Long = 0L,
            @ProtoId(7) @JvmField val matchExpiredTime: Int = 0,
            @ProtoId(8) @JvmField val c2cExpiredTime: Int = 0,
            @ProtoId(9) @JvmField val matchCount: Int = 0,
            @ProtoId(10) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ConfMsgRoamFlag(
            @ProtoId(1) @JvmField val confid: Long = 0L,
            @ProtoId(2) @JvmField val flag: Int = 0,
            @ProtoId(3) @JvmField val timestamp: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class DaRenNotify(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val loginDays: Int = 0,
            @ProtoId(3) @JvmField val days: Int = 0,
            @ProtoId(4) @JvmField val isYestodayLogin: Int = 0,
            @ProtoId(5) @JvmField val isTodayLogin: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DelFriend(
            @ProtoId(1) @JvmField val uint64Uins: List<Long>? = null
        ) : ProtoBuf

        @Serializable
        internal class DelGroup(
            @ProtoId(1) @JvmField val groupid: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class FanpaiziNotify(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val fromNick: String = "",
            @ProtoId(3) @JvmField val tipsContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ForwardBody(
            @ProtoId(1) @JvmField val notifyType: Int = 0,
            @ProtoId(2) @JvmField val opType: Int = 0,
            @ProtoId(3) @JvmField val msgAddGroup: AddGroup? = null,
            @ProtoId(4) @JvmField val msgDelGroup: DelGroup? = null,
            @ProtoId(5) @JvmField val msgModGroupName: ModGroupName? = null,
            @ProtoId(6) @JvmField val msgModGroupSort: ModGroupSort? = null,
            @ProtoId(7) @JvmField val msgModFriendGroup: ModFriendGroup? = null,
            @ProtoId(8) @JvmField val msgModProfile: ModProfile? = null,
            @ProtoId(9) @JvmField val msgModFriendRemark: ModFriendRemark? = null,
            @ProtoId(10) @JvmField val msgModLongNick: ModLongNick? = null,
            @ProtoId(11) @JvmField val msgModCustomFace: ModCustomFace? = null,
            @ProtoId(12) @JvmField val msgModGroupProfile: ModGroupProfile? = null,
            @ProtoId(13) @JvmField val msgModGroupMemberProfile: ModGroupMemberProfile? = null,
            @ProtoId(14) @JvmField val msgDelFriend: DelFriend? = null,
            @ProtoId(15) @JvmField val msgRoamPriv: ModFrdRoamPriv? = null,
            @ProtoId(16) @JvmField val msgGrpMsgRoamFlag: GrpMsgRoamFlag? = null,
            @ProtoId(17) @JvmField val msgConfMsgRoamFlag: ConfMsgRoamFlag? = null,
            @ProtoId(18) @JvmField val msgModRichLongNick: ModLongNick? = null,
            @ProtoId(19) @JvmField val msgBinPkg: BinaryMsg? = null,
            @ProtoId(20) @JvmField val msgModFriendRings: ModSnsGeneralInfo? = null,
            @ProtoId(21) @JvmField val msgModConfProfile: ModConfProfile? = null,
            @ProtoId(22) @JvmField val msgModFriendFlag: SnsUpdateFlag? = null,
            @ProtoId(23) @JvmField val msgAppointmentNotify: AppointmentNotify? = null,
            @ProtoId(25) @JvmField val msgDarenNotify: DaRenNotify? = null,
            @ProtoId(26) @JvmField val msgNewComeinUserNotify: NewComeinUserNotify? = null,
            @ProtoId(200) @JvmField val msgPushSearchDev: PushSearchDev? = null,
            @ProtoId(201) @JvmField val msgPushReportDev: PushReportDev? = null,
            @ProtoId(202) @JvmField val msgQqPayPush: QQPayPush? = null,
            @ProtoId(203) @JvmField val redpointInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(204) @JvmField val msgHotFriendNotify: HotFriendNotify? = null,
            @ProtoId(205) @JvmField val msgPraiseRankNotify: PraiseRankNotify? = null,
            @ProtoId(210) @JvmField val msgCampusNotify: MQQCampusNotify? = null,
            @ProtoId(211) @JvmField val msgModRichLongNickEx: ModLongNick? = null,
            @ProtoId(212) @JvmField val msgChatMatchInfo: ChatMatchInfo? = null,
            @ProtoId(214) @JvmField val msgFrdCustomOnlineStatusChange: FrdCustomOnlineStatusChange? = null,
            @ProtoId(2000) @JvmField val msgFanpanziNotify: FanpaiziNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class FrdCustomOnlineStatusChange(
            @ProtoId(1) @JvmField val uin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class FriendGroup(
            @ProtoId(1) @JvmField val fuin: Long = 0L,
            @ProtoId(2) @JvmField val uint32OldGroupId: List<Int>? = null,
            @ProtoId(3) @JvmField val uint32NewGroupId: List<Int>? = null
        ) : ProtoBuf

        @Serializable
        internal class FriendRemark(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val fuin: Long = 0L,
            @ProtoId(3) @JvmField val rmkName: String = "",
            @ProtoId(4) @JvmField val groupCode: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class GPS(
            @ProtoId(1) @JvmField val int32Lat: Int = 900000000,
            @ProtoId(2) @JvmField val int32Lon: Int = 900000000,
            @ProtoId(3) @JvmField val int32Alt: Int = -10000000,
            @ProtoId(4) @JvmField val int32Type: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GroupMemberProfileInfo(
            @ProtoId(1) @JvmField val field: Int = 0,
            @ProtoId(2) @JvmField val value: String = ""
        ) : ProtoBuf

        @Serializable
        internal class GroupProfileInfo(
            @ProtoId(1) @JvmField val field: Int = 0,
            @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class GroupSort(
            @ProtoId(1) @JvmField val groupid: Int = 0,
            @ProtoId(2) @JvmField val sortid: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GrpMsgRoamFlag(
            @ProtoId(1) @JvmField val groupcode: Long = 0L,
            @ProtoId(2) @JvmField val flag: Int = 0,
            @ProtoId(3) @JvmField val timestamp: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class HotFriendNotify(
            @ProtoId(1) @JvmField val dstUin: Long = 0L,
            @ProtoId(2) @JvmField val praiseHotLevel: Int = 0,
            @ProtoId(3) @JvmField val chatHotLevel: Int = 0,
            @ProtoId(4) @JvmField val praiseHotDays: Int = 0,
            @ProtoId(5) @JvmField val chatHotDays: Int = 0,
            @ProtoId(6) @JvmField val closeLevel: Int = 0,
            @ProtoId(7) @JvmField val closeDays: Int = 0,
            @ProtoId(8) @JvmField val praiseFlag: Int = 0,
            @ProtoId(9) @JvmField val chatFlag: Int = 0,
            @ProtoId(10) @JvmField val closeFlag: Int = 0,
            @ProtoId(11) @JvmField val notifyTime: Long = 0L,
            @ProtoId(12) @JvmField val lastPraiseTime: Long = 0L,
            @ProtoId(13) @JvmField val lastChatTime: Long = 0L,
            @ProtoId(14) @JvmField val qzoneHotLevel: Int = 0,
            @ProtoId(15) @JvmField val qzoneHotDays: Int = 0,
            @ProtoId(16) @JvmField val qzoneFlag: Int = 0,
            @ProtoId(17) @JvmField val lastQzoneTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModConfProfile(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val confUin: Int = 0,
            @ProtoId(3) @JvmField val msgProfileInfos: List<ProfileInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModCustomFace(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val uin: Long = 0L,
            @ProtoId(3) @JvmField val groupCode: Long = 0L,
            @ProtoId(4) @JvmField val cmdUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModFrdRoamPriv(
            @ProtoId(1) @JvmField val msgRoamPriv: List<OneRoamPriv>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModFriendGroup(
            @ProtoId(1) @JvmField val msgFrdGroup: List<FriendGroup>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModFriendRemark(
            @ProtoId(1) @JvmField val msgFrdRmk: List<FriendRemark>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModGroupMemberProfile(
            @ProtoId(1) @JvmField val groupUin: Long = 0L,
            @ProtoId(2) @JvmField val uin: Long = 0L,
            @ProtoId(3) @JvmField val msgGroupMemberProfileInfos: List<GroupMemberProfileInfo>? = null,
            @ProtoId(4) @JvmField val groupCode: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModGroupName(
            @ProtoId(1) @JvmField val groupid: Int = 0,
            @ProtoId(2) @JvmField val groupname: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ModGroupProfile(
            @ProtoId(1) @JvmField val groupUin: Long = 0L,
            @ProtoId(2) @JvmField val msgGroupProfileInfos: List<GroupProfileInfo>? = null,
            @ProtoId(3) @JvmField val groupCode: Long = 0L,
            @ProtoId(4) @JvmField val cmdUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModGroupSort(
            @ProtoId(1) @JvmField val msgGroupsort: List<GroupSort>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModLongNick(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ModProfile(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val msgProfileInfos: List<ProfileInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModSnsGeneralInfo(
            @ProtoId(1) @JvmField val msgSnsGeneralInfos: List<SnsUpateBuffer>? = null
        ) : ProtoBuf

        @Serializable
        internal class MQQCampusNotify(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val wording: String = "",
            @ProtoId(3) @JvmField val target: String = "",
            @ProtoId(4) @JvmField val type: Int = 0,
            @ProtoId(5) @JvmField val source: String = ""
        ) : ProtoBuf

        @Serializable
        internal class SubMsgType0x27MsgBody(
            @ProtoId(1) @JvmField val msgModInfos: List<ForwardBody> = listOf()
        ) : ProtoBuf

        @Serializable
        internal class NewComeinUser(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val isFrd: Int = 0,
            @ProtoId(3) @JvmField val remark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NewComeinUserNotify(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val boolStrongNotify: Boolean = false,
            @ProtoId(3) @JvmField val pushTime: Int = 0,
            @ProtoId(4) @JvmField val msgNewComeinUser: NewComeinUser? = null,
            @ProtoId(5) @JvmField val msgNewGroup: NewGroup? = null,
            @ProtoId(6) @JvmField val msgNewGroupUser: NewGroupUser? = null
        ) : ProtoBuf

        @Serializable
        internal class NewGroup(
            @ProtoId(1) @JvmField val groupCode: Long = 0L,
            @ProtoId(2) @JvmField val groupName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val ownerUin: Long = 0L,
            @ProtoId(4) @JvmField val ownerNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val distance: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NewGroupUser(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val int32Sex: Int = 0,
            @ProtoId(3) @JvmField val int32Age: Int = 0,
            @ProtoId(4) @JvmField val nick: String = "",
            @ProtoId(5) @JvmField val distance: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class OneRoamPriv(
            @ProtoId(1) @JvmField val fuin: Long = 0L,
            @ProtoId(2) @JvmField val privTag: Int = 0,
            @ProtoId(3) @JvmField val privValue: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class PraiseRankNotify(
            @ProtoId(11) @JvmField val isChampion: Int = 0,
            @ProtoId(12) @JvmField val rankNum: Int = 0,
            @ProtoId(13) @JvmField val msg: String = ""
        ) : ProtoBuf

        @Serializable
        internal class ProfileInfo(
            @ProtoId(1) @JvmField val field: Int = 0,
            @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushReportDev(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(4) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val reportMaxNum: Int = 200,
            @ProtoId(6) @JvmField val sn: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushSearchDev(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgGpsInfo: GPS? = null,
            @ProtoId(3) @JvmField val devTime: Int = 0,
            @ProtoId(4) @JvmField val pushTime: Int = 0,
            @ProtoId(5) @JvmField val din: Long = 0L,
            @ProtoId(6) @JvmField val data: String = ""
        ) : ProtoBuf

        @Serializable
        internal class QQPayPush(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val boolPayOk: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class SnsUpateBuffer(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val code: Long = 0L,
            @ProtoId(3) @JvmField val result: Int = 0,
            @ProtoId(400) @JvmField val msgSnsUpdateItem: List<SnsUpdateItem>? = null,
            @ProtoId(401) @JvmField val uint32Idlist: List<Int>? = null
        ) : ProtoBuf

        @Serializable
        internal class SnsUpdateFlag(
            @ProtoId(1) @JvmField val msgUpdateSnsFlag: List<SnsUpdateOneFlag>? = null
        ) : ProtoBuf

        @Serializable
        internal class SnsUpdateItem(
            @ProtoId(1) @JvmField val updateSnsType: Int = 0,
            @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class SnsUpdateOneFlag(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val id: Long = 0L,
            @ProtoId(3) @JvmField val flag: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x28 {
    internal class SubMsgType0x28 : ProtoBuf {
        @Serializable
        internal class FollowList(
            @ProtoId(1) @JvmField val puin: Long = 0L,
            @ProtoId(2) @JvmField val uin: Long = 0L,
            @ProtoId(3) @JvmField val type: Int = 0,
            @ProtoId(4) @JvmField val seqno: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val msgRspFollowlist: RspFollowList? = null,
            @ProtoId(3) @JvmField val msgRspTypelist: RspTypeList? = null
        ) : ProtoBuf

        @Serializable
        internal class RspFollowList(
            @ProtoId(1) @JvmField val msgFollowlist: List<FollowList>? = null
        ) : ProtoBuf

        @Serializable
        internal class RspTypeList(
            @ProtoId(1) @JvmField val msgTypelist: List<TypeList>? = null
        ) : ProtoBuf

        @Serializable
        internal class TypeList(
            @ProtoId(1) @JvmField val puin: Long = 0L,
            @ProtoId(2) @JvmField val flag: Int = 0,
            @ProtoId(3) @JvmField val type: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x30 {
    internal class SubMsgType0x30 : ProtoBuf {
        @Serializable
        internal class BlockListNotify(
            @ProtoId(1) @JvmField val msgBlockUinInfo: List<BlockUinInfo>? = null,
            @ProtoId(2) @JvmField val uint64DelUin: List<Long>? = null
        ) : ProtoBuf

        @Serializable
        internal class BlockUinInfo(
            @ProtoId(1) @JvmField val blockUin: Long = 0L,
            @ProtoId(2) @JvmField val sourceId: Int = 0,
            @ProtoId(3) @JvmField val sourceSubId: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val msgS2cBlocklistNotify: BlockListNotify? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x31 {
    internal class Submsgtype0x31 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val flag: Int = 0,
            @ProtoId(2) @JvmField val uin: Long = 0L,
            @ProtoId(3) @JvmField val bindUin: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(4) @JvmField val time: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x35 {
    internal class Submsgtype0x35 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val bubbleTimestamp: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3b {
    internal class Submsgtype0x3b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val groupCode: Long = 0L,
            @ProtoId(3) @JvmField val userShowFlag: Int = 0,
            @ProtoId(4) @JvmField val memberLevelChanged: Int = 0,
            @ProtoId(5) @JvmField val officemode: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3d {
    internal class SttResultPush : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val msgPttResp: TransPttResp? = null
        ) : ProtoBuf

        @Serializable
        internal class TransPttResp(
            @ProtoId(1) @JvmField val sessionid: Long = 0L,
            @ProtoId(2) @JvmField val pttType: Int = 0,
            @ProtoId(3) @JvmField val errorCode: Int = 0,
            @ProtoId(4) @JvmField val totalLen: Int = 0,
            @ProtoId(5) @JvmField val seq: Int = 0,
            @ProtoId(6) @JvmField val pos: Int = 0,
            @ProtoId(7) @JvmField val len: Int = 0,
            @ProtoId(8) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val senderUin: Long = 0L,
            @ProtoId(10) @JvmField val receiverUin: Long = 0L,
            @ProtoId(11) @JvmField val fileID: Int = 0,
            @ProtoId(12) @JvmField val filemd5: String = "",
            @ProtoId(13) @JvmField val filePath: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3e {
    internal class Submsgtype0x3e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subcmd: Int = 0,
            @ProtoId(2) @JvmField val random: Int = 0,
            @ProtoId(3) @JvmField val result: Int = 0,
            @ProtoId(4) @JvmField val device: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val sid: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3f {
    internal class SubMsgType0x3f : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgPubunikey: List<PubUniKey>? = null
        ) : ProtoBuf

        @Serializable
        internal class PubUniKey(
            @ProtoId(1) @JvmField val fromPubUin: Long = 0L,
            @ProtoId(2) @JvmField val qwMsgId: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x40 {
    internal class SubMsgType0x40 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val vUuid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val fromUin: Long = 0L,
            @ProtoId(3) @JvmField val toUin: Long = 0L,
            @ProtoId(4) @JvmField val state: Int = 0,
            @ProtoId(11) @JvmField val opertype: Int = 0,
            @ProtoId(12) @JvmField val fromphonenum: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x41 {
    internal class MsgType0x210SubMsgType0x41 : ProtoBuf {
        @Serializable
        internal class GameRsultMsg(
            @ProtoId(1) @JvmField val gameName: String = "",
            @ProtoId(2) @JvmField val gamePic: String = "",
            @ProtoId(3) @JvmField val moreInfo: String = "",
            @ProtoId(4) @JvmField val msgGameRsts: List<UinResult>? = null,
            @ProtoId(5) @JvmField val gameSubheading: String = "",
            @ProtoId(6) @JvmField val uin: Long = 0L,
            @ProtoId(7) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class UinResult(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val grade: Int = 0,
            @ProtoId(4) @JvmField val score: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x42 {
    internal class Submsgtype0x42 : ProtoBuf {
        @Serializable
        internal class GameStatusSync(
            @ProtoId(1) @JvmField val gameAppid: Int = 0,
            @ProtoId(2) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x44 {
    internal class Submsgtype0x44 : ProtoBuf {
        @Serializable
        internal class ClearCountMsg(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val time: Int = 0,
            @ProtoId(3) @JvmField val processflag: Int = 0,
            @ProtoId(4) @JvmField val updateflag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class FriendSyncMsg(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val fuin: Long = 0L,
            @ProtoId(3) @JvmField val processtype: Int = 0,
            @ProtoId(4) @JvmField val time: Int = 0,
            @ProtoId(5) @JvmField val processflag: Int = 0,
            @ProtoId(6) @JvmField val sourceid: Int = 0,
            @ProtoId(7) @JvmField val sourcesubid: Int = 0,
            @ProtoId(8) @JvmField val strWording: List<String> = listOf()
        ) : ProtoBuf

        @Serializable
        internal class GroupSyncMsg(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgSeq: Long = 0L,
            @ProtoId(3) @JvmField val grpCode: Long = 0L,
            @ProtoId(4) @JvmField val gaCode: Long = 0L,
            @ProtoId(5) @JvmField val optUin1: Long = 0L,
            @ProtoId(6) @JvmField val optUin2: Long = 0L,
            @ProtoId(7) @JvmField val msgBuf: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val authKey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val msgStatus: Int = 0,
            @ProtoId(10) @JvmField val actionUin: Long = 0L,
            @ProtoId(11) @JvmField val actionTime: Long = 0L,
            @ProtoId(12) @JvmField val curMaxMemCount: Int = 0,
            @ProtoId(13) @JvmField val nextMaxMemCount: Int = 0,
            @ProtoId(14) @JvmField val curMemCount: Int = 0,
            @ProtoId(15) @JvmField val reqSrcId: Int = 0,
            @ProtoId(16) @JvmField val reqSrcSubId: Int = 0,
            @ProtoId(17) @JvmField val inviterRole: Int = 0,
            @ProtoId(18) @JvmField val extAdminNum: Int = 0,
            @ProtoId(19) @JvmField val processflag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class ModifySyncMsg(
            @ProtoId(1) @JvmField val time: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgFriendMsgSync: FriendSyncMsg? = null,
            @ProtoId(2) @JvmField val msgGroupMsgSync: GroupSyncMsg? = null,
            @ProtoId(3) @JvmField val msgCleanCountMsg: ClearCountMsg? = null,
            @ProtoId(4) @JvmField val msgModifyMsgSync: ModifySyncMsg? = null,
            @ProtoId(5) @JvmField val msgWaitingMsgSync: WaitingSyncMsg? = null
        ) : ProtoBuf

        @Serializable
        internal class WaitingSyncMsg(
            @ProtoId(1) @JvmField val time: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x48 {
    @Serializable
    internal class RecommendDeviceLock(
        @ProtoId(1) @JvmField val canCancel: Boolean = false,
        @ProtoId(2) @JvmField val wording: String = "",
        @ProtoId(3) @JvmField val title: String = "",
        @ProtoId(4) @JvmField val secondTitle: String = "",
        @ProtoId(5) @JvmField val thirdTitle: String = "",
        @ProtoId(6) @JvmField val wordingList: List<String> = listOf()
    ) : ProtoBuf
}


internal class Submsgtype0x4a {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) @JvmField val secCmd: Int = 0,
        @ProtoId(2) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


internal class Submsgtype0x4b {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) @JvmField val albumid: String = "",
        @ProtoId(2) @JvmField val coverUrl: String = "",
        @ProtoId(3) @JvmField val albumName: String = "",
        @ProtoId(4) @JvmField val opuin: Long = 0L,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(5) @JvmField val time: Int = 0,
        @ProtoId(6) @JvmField val picCnt: Int = 0,
        @ProtoId(7) @JvmField val pushMsgHelper: String = "",
        @ProtoId(8) @JvmField val pushMsgAlbum: String = "",
        @ProtoId(9) @JvmField val usrTotal: Int = 0,
        @ProtoId(10) @JvmField val uint64User: List<Long>? = null
    ) : ProtoBuf

    internal class Submsgtype0x4b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val albumid: String = "",
            @ProtoId(2) @JvmField val coverUrl: String = "",
            @ProtoId(3) @JvmField val albumName: String = "",
            @ProtoId(4) @JvmField val opuin: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(5) @JvmField val time: Int = 0,
            @ProtoId(6) @JvmField val picCnt: Int = 0,
            @ProtoId(7) @JvmField val pushMsgHelper: String = "",
            @ProtoId(8) @JvmField val pushMsgAlbum: String = "",
            @ProtoId(9) @JvmField val usrTotal: Int = 0,
            @ProtoId(10) @JvmField val uint64User: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x4e {
    internal class Submsgtype0x4e : ProtoBuf {
        @Serializable
        internal class GroupBulletin(
            @ProtoId(1) @JvmField val msgContent: List<Content>? = null
        ) : ProtoBuf {
            @Serializable
            internal class Content(
                @ProtoId(1) @JvmField val feedid: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(2) @JvmField val uin: Long = 0L,
                @ProtoType(ProtoNumberType.FIXED) @ProtoId(3) @JvmField val time: Int = 0
            ) : ProtoBuf
        }

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val groupId: Long = 0L,
            @ProtoId(2) @JvmField val groupCode: Long = 0L,
            @ProtoId(3) @JvmField val appid: Int = 0,
            @ProtoId(4) @JvmField val msgGroupBulletin: GroupBulletin? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x54 {
    internal class Submsgtype0x54 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val peerType: Int /* enum */ = 1,
            @ProtoId(2) @JvmField val peerUin: Long = 0L,
            @ProtoId(3) @JvmField val taskList: List<TaskInfo>? = null
        ) : ProtoBuf {
            @Serializable
            internal class TaskInfo(
                @ProtoId(1) @JvmField val taskId: Int = 0
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0x60 {
    internal class SubMsgType0x60 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val pushcmd: Int = 0,
            @ProtoId(2) @JvmField val int64Ts: Long = 0L,
            @ProtoId(3) @JvmField val ssid: String = "",
            @ProtoId(4) @JvmField val content: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x63 {
    internal class Submsgtype0x63 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val roomid: Long = 0L,
            @ProtoId(2) @JvmField val seq: Int = 0,
            @ProtoId(3) @JvmField val url: String = "",
            @ProtoId(4) @JvmField val data: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x65 {
    internal class SubMsgType0x65 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val cmd: Int = 0,
            @ProtoId(2) @JvmField val msgExpiredPkg: MsgExpiredPkg? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgExpiredPkg(
            @ProtoId(1) @JvmField val platform: Int = 0,
            @ProtoId(2) @JvmField val expirePkg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val predownPkg: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x66 {
    internal class Submsgtype0x66 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val pushData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val timestamp: Int = 0,
            @ProtoId(4) @JvmField val notifyText: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val pushFlag: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x67 {
    internal class Submsgtype0x67 : ProtoBuf {
        @Serializable
        internal class GroupInfo(
            @ProtoId(1) @JvmField val groupCode: Long = 0L,
            @ProtoId(2) @JvmField val groupName: String = "",
            @ProtoId(3) @JvmField val groupMemo: String = "",
            @ProtoId(4) @JvmField val memberNum: Int = 0,
            @ProtoId(5) @JvmField val groupType: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgGrpinfo: List<GroupInfo>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x69 {
    @Serializable
    internal class Submsgtype0x69(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val boolDisplayReddot: Boolean = false,
        @ProtoId(3) @JvmField val number: Int = 0,
        @ProtoId(4) @JvmField val reason: Int = 0,
        @ProtoId(5) @JvmField val lastTime: Int = 0,
        @ProtoId(6) @JvmField val cmdUin: Long = 0L,
        @ProtoId(7) @JvmField val faceUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val customBuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val expireTime: Int = 0,
        @ProtoId(10) @JvmField val cmdUinType: Int = 0,
        @ProtoId(11) @JvmField val reportType: Int = 0,
        @ProtoId(12) @JvmField val boolTestEnv: Boolean = false
    ) : ProtoBuf
}


internal class Submsgtype0x6b {
    internal class SubMsgType0x6b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val toUin: Long = 0L,
            @ProtoId(2) @JvmField val tipsContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val yesText: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val noText: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x6f {
    internal class SubMsgType0x6f : ProtoBuf {
        @Serializable
        internal class AddFriendSource(
            @ProtoId(1) @JvmField val source: Int = 0,
            @ProtoId(2) @JvmField val subSource: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class AddQimFriendNotifyToQQ(
            @ProtoId(1) @JvmField val opType: Int = 0,
            @ProtoId(2) @JvmField val uin: Long = 0L,
            @ProtoId(3) @JvmField val gender: Int = 0,
            @ProtoId(4) @JvmField val smartRemark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val longnick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val storysTotalNum: Long = 0L,
            @ProtoId(7) @JvmField val caresCount: Long = 0L,
            @ProtoId(8) @JvmField val fansCount: Long = 0L,
            @ProtoId(9) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val srcWording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class AddQimNotLoginFrdNotifyToQQ(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val gender: Int = 0,
            @ProtoId(4) @JvmField val age: Int = 0,
            @ProtoId(5) @JvmField val coverstory: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val storysTotalNum: Long = 0L,
            @ProtoId(7) @JvmField val msgVideoInfo: List<VideoInfo>? = null,
            @ProtoId(8) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val qqUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class BirthdayReminderPush(
            @ProtoId(2004) @JvmField val reminderWording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class FanpaiziNotify(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val fromNick: String = "",
            @ProtoId(3) @JvmField val tipsContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ForwardBody(
            @ProtoId(1) @JvmField val notifyType: Int = 0,
            @ProtoId(2) @JvmField val opType: Int = 0,
            @ProtoId(2000) @JvmField val msgFanpanziNotify: FanpaiziNotify? = null,
            @ProtoId(2001) @JvmField val msgMcardNotificationLike: MCardNotificationLike? = null,
            @ProtoId(2002) @JvmField val msgVipInfoNotify: VipInfoNotify? = null,
            @ProtoId(2003) @JvmField val msgPushLostDevFound: PushLostDevFound? = null,
            @ProtoId(2004) @JvmField val msgBirthdayReminderPush: BirthdayReminderPush? = null,
            @ProtoId(2005) @JvmField val msgPushLostDev: PushLostDevFound? = null,
            @ProtoId(2007) @JvmField val msgBabyqRewardInfo: RewardInfo? = null,
            @ProtoId(2008) @JvmField val msgHotFriendNotify: HotFriendNotify? = null,
            @ProtoId(2009) @JvmField val msgPushQimRecommend: QimRecomendMsg? = null,
            @ProtoId(2010) @JvmField val msgModQimFriend: QimFriendNotify? = null,
            @ProtoId(2011) @JvmField val msgModQimFriendToQq: QimFriendNotifyToQQ? = null
        ) : ProtoBuf

        @Serializable
        internal class GPS(
            @ProtoId(1) @JvmField val int32Lat: Int = 900000000,
            @ProtoId(2) @JvmField val int32Lon: Int = 900000000,
            @ProtoId(3) @JvmField val int32Alt: Int = -10000000,
            @ProtoId(4) @JvmField val int32Type: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class HotFriendNotify(
            @ProtoId(1) @JvmField val dstUin: Long = 0L,
            @ProtoId(2) @JvmField val praiseHotLevel: Int = 0,
            @ProtoId(3) @JvmField val chatHotLevel: Int = 0,
            @ProtoId(4) @JvmField val praiseHotDays: Int = 0,
            @ProtoId(5) @JvmField val chatHotDays: Int = 0,
            @ProtoId(6) @JvmField val closeLevel: Int = 0,
            @ProtoId(7) @JvmField val closeDays: Int = 0,
            @ProtoId(8) @JvmField val praiseFlag: Int = 0,
            @ProtoId(9) @JvmField val chatFlag: Int = 0,
            @ProtoId(10) @JvmField val closeFlag: Int = 0,
            @ProtoId(11) @JvmField val notifyTime: Long = 0L,
            @ProtoId(12) @JvmField val lastPraiseTime: Long = 0L,
            @ProtoId(13) @JvmField val lastChatTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class MCardNotificationLike(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val counterTotal: Int = 0,
            @ProtoId(3) @JvmField val counterNew: Int = 0,
            @ProtoId(4) @JvmField val wording: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgModInfos: List<ForwardBody>? = null
        ) : ProtoBuf

        @Serializable
        internal class PushLostDevFound(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(3) @JvmField val devTime: Int = 0,
            @ProtoId(6) @JvmField val din: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class QimFriendNotify(
            @ProtoId(1) @JvmField val opType: Int = 0,
            @ProtoId(2) @JvmField val uint64Uins: List<Long>? = null,
            @ProtoId(3) @JvmField val fansUnreadCount: Long = 0L,
            @ProtoId(4) @JvmField val fansTotalCount: Long = 0L,
            @ProtoId(5) @JvmField val pushTime: Long = 0L,
            @ProtoId(6) @JvmField val bytesMobiles: List<ByteArray>? = null
        ) : ProtoBuf

        @Serializable
        internal class QimFriendNotifyToQQ(
            @ProtoId(1) @JvmField val notifyType: Int = 0,
            @ProtoId(2) @JvmField val msgAddNotifyToQq: AddQimFriendNotifyToQQ? = null,
            @ProtoId(3) @JvmField val msgUpgradeNotify: UpgradeQimFriendNotify? = null,
            @ProtoId(4) @JvmField val msgAddNotLoginFrdNotifyToQq: AddQimNotLoginFrdNotifyToQQ? = null
        ) : ProtoBuf

        @Serializable
        internal class QimRecomendInfo(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val reason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val gender: Int = 0,
            @ProtoId(5) @JvmField val longnick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val alghbuff: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val age: Int = 0,
            @ProtoId(8) @JvmField val source: Int = 0,
            @ProtoId(9) @JvmField val sourceReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val msgIosSource: AddFriendSource? = null,
            @ProtoId(11) @JvmField val msgAndroidSource: AddFriendSource? = null
        ) : ProtoBuf

        @Serializable
        internal class QimRecomendMsg(
            @ProtoId(1) @JvmField val msgRecomendList: List<QimRecomendInfo>? = null,
            @ProtoId(2) @JvmField val timestamp: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class RewardInfo(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val jmpUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val cookies: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val jmpWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val optWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val optUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val faceAddonId: Long = 0L,
            @ProtoId(9) @JvmField val iconUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val toastWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(11) @JvmField val reportType: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class UpgradeQimFriendNotify(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class VideoInfo(
            @ProtoId(1) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val videoCoverUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class VipInfoNotify(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val vipLevel: Int = 0,
            @ProtoId(3) @JvmField val vipIdentify: Int = 0,
            @ProtoId(4) @JvmField val ext: Int = 0,
            @ProtoId(5) @JvmField val extString: String = "",
            @ProtoId(6) @JvmField val redFlag: Int = 0,
            @ProtoId(7) @JvmField val disableRedEnvelope: Int = 0,
            @ProtoId(8) @JvmField val redpackId: Int = 0,
            @ProtoId(9) @JvmField val redpackName: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x71 {
    internal class Submsgtype0x71 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgAppInfo: List<ReportAppInfo>? = null,
            @ProtoId(2) @JvmField val uiUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class RedDisplayInfo(
            @ProtoId(1) @JvmField val msgRedTypeInfo: List<RedTypeInfo>? = null,
            @ProtoId(2) @JvmField val msgTabDisplayInfo: RedTypeInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class RedTypeInfo(
            @ProtoId(1) @JvmField val redType: Int = 0,
            @ProtoId(2) @JvmField val redContent: String = "",
            @ProtoId(3) @JvmField val redDesc: String = "",
            @ProtoId(4) @JvmField val redPriority: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class ReportAppInfo(
            @ProtoId(1) @JvmField val appId: Int = 0,
            @ProtoId(2) @JvmField val int32NewFlag: Int = 0,
            @ProtoId(3) @JvmField val type: Int = 0,
            @ProtoId(4) @JvmField val buffer: String = "",
            @ProtoId(5) @JvmField val path: String = "",
            @ProtoId(6) @JvmField val pushRedTs: Int = 0,
            @ProtoId(7) @JvmField val mission: String = "",
            @ProtoId(8) @JvmField val int32Appset: Int = 0,
            @ProtoId(9) @JvmField val int32Num: Int = 0,
            @ProtoId(10) @JvmField val iconUrl: String = "",
            @ProtoId(11) @JvmField val int32IconFlag: Int = 0,
            @ProtoId(12) @JvmField val int32IconType: Int = 0,
            @ProtoId(13) @JvmField val duration: Int = 0,
            @ProtoId(14) @JvmField val msgVersionInfo: ReportVersion? = null,
            @ProtoId(15) @JvmField val androidAppId: Int = 0,
            @ProtoId(16) @JvmField val iosAppId: Int = 0,
            @ProtoId(17) @JvmField val androidPath: String = "",
            @ProtoId(18) @JvmField val iosPath: String = "",
            @ProtoId(19) @JvmField val int32MissionLevel: Int = 0,
            @ProtoId(20) @JvmField val msgDisplayDesc: RedDisplayInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class ReportVersion(
            @ProtoId(1) @JvmField val int32PlantId: Int = 0,
            @ProtoId(2) @JvmField val boolAllver: Boolean = false,
            @ProtoId(3) @JvmField val strVersion: List<String> = listOf()
        ) : ProtoBuf
    }
}


internal class Submsgtype0x72 {
    internal class SubMsgType0x72 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val urgency: Int = 0,
            @ProtoId(3) @JvmField val templateNo: Int = 0,
            @ProtoId(4) @JvmField val content: String = "",
            @ProtoId(5) @JvmField val infoDate: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x76 {
    internal class SubMsgType0x76 : ProtoBuf {
        @Serializable
        internal class BirthdayNotify(
            @ProtoId(1) @JvmField val msgOneFriend: List<OneBirthdayFriend>? = null,
            @ProtoId(2) @JvmField val reserved: Int = 0,
            @ProtoId(3) @JvmField val giftMsg: List<OneGiftMessage>? = null,
            @ProtoId(4) @JvmField val topPicUrl: String = "",
            @ProtoId(5) @JvmField val extend: String = ""
        ) : ProtoBuf

        @Serializable
        internal class GeoGraphicNotify(
            @ProtoId(1) @JvmField val localCity: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val msgOneFriend: List<OneGeoGraphicFriend>? = null
        ) : ProtoBuf

        @Serializable
        internal class MemorialDayNotify(
            @ProtoId(1) @JvmField val anniversaryInfo: List<OneMemorialDayInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val boolStrongNotify: Boolean = false,
            @ProtoId(3) @JvmField val pushTime: Int = 0,
            @ProtoId(4) @JvmField val msgGeographicNotify: GeoGraphicNotify? = null,
            @ProtoId(5) @JvmField val msgBirthdayNotify: BirthdayNotify? = null,
            @ProtoId(6) @JvmField val notifyWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val msgMemorialdayNotify: MemorialDayNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class OneBirthdayFriend(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val boolLunarBirth: Boolean = false,
            @ProtoId(3) @JvmField val birthMonth: Int = 0,
            @ProtoId(4) @JvmField val birthDate: Int = 0,
            @ProtoId(5) @JvmField val msgSendTime: Long = 0L,
            @ProtoId(6) @JvmField val birthYear: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OneGeoGraphicFriend(
            @ProtoId(1) @JvmField val uin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class OneGiftMessage(
            @ProtoId(1) @JvmField val giftId: Int = 0,
            @ProtoId(2) @JvmField val giftName: String = "",
            @ProtoId(3) @JvmField val type: Int = 0,
            @ProtoId(4) @JvmField val giftUrl: String = "",
            @ProtoId(5) @JvmField val price: Int = 0,
            @ProtoId(6) @JvmField val playCnt: Int = 0,
            @ProtoId(7) @JvmField val backgroundColor: String = ""
        ) : ProtoBuf

        @Serializable
        internal class OneMemorialDayInfo(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val type: Long = 0,
            @ProtoId(3) @JvmField val memorialTime: Int = 0,
            @ProtoId(11) @JvmField val mainWordingNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(12) @JvmField val mainWordingEvent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) @JvmField val subWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(14) @JvmField val greetings: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(15) @JvmField val friendGender: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x78 {
    internal class Submsgtype0x78 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val version: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x7a {
    internal class Submsgtype0x7a : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val fromUin: Long = 0L,
            @ProtoId(3) @JvmField val nick: String = "",
            @ProtoId(4) @JvmField val discussUin: Long = 0L,
            @ProtoId(5) @JvmField val discussNick: String = "",
            @ProtoId(6) @JvmField val seq: Long = 0L,
            @ProtoId(7) @JvmField val atTime: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x7c {
    internal class Submsgtype0x7c : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val int32Cmd: Int = 0,
            @ProtoId(3) @JvmField val stringCmdExt: List<String> = listOf(),
            @ProtoId(4) @JvmField val seq: Long = 0L,
            @ProtoId(5) @JvmField val stringSeqExt: List<String> = listOf()
        ) : ProtoBuf
    }
}


internal class Submsgtype0x7e {
    internal class Submsgtype0x7e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val notice: String = "",
            @ProtoId(2) @JvmField val msgOnlinePush: WalletMsgPush? = null
        ) : ProtoBuf

        @Serializable
        internal class WalletMsgPush(
            @ProtoId(1) @JvmField val action: Int = 0,
            @ProtoId(2) @JvmField val timestamp: Int = 0,
            @ProtoId(3) @JvmField val extend: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val serialno: String = "",
            @ProtoId(5) @JvmField val billno: String = "",
            @ProtoId(6) @JvmField val appinfo: String = "",
            @ProtoId(7) @JvmField val amount: Int = 0,
            @ProtoId(8) @JvmField val jumpurl: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x83 {
    internal class SubMsgType0x83 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgParams: List<MsgParams>? = null,
            @ProtoId(2) @JvmField val seq: Long = 0L,
            @ProtoId(3) @JvmField val groupId: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class MsgParams(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val fromUin: Long = 0L,
            @ProtoId(3) @JvmField val toUin: Long = 0L,
            @ProtoId(4) @JvmField val dataString: String = "",
            @ProtoId(5) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        internal class MsgRep : ProtoBuf
    }
}


internal class Submsgtype0x85 {
    internal class SubMsgType0x85 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val showLastest: Int = 0,
            @ProtoId(2) @JvmField val senderUin: Long = 0L,
            @ProtoId(3) @JvmField val receiverUin: Long = 0L,
            @ProtoId(4) @JvmField val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val authkey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val icon: Int = 0,
            @ProtoId(9) @JvmField val random: Int = 0,
            @ProtoId(10) @JvmField val redSenderUin: Long = 0L,
            @ProtoId(11) @JvmField val type: Int = 0,
            @ProtoId(12) @JvmField val subType: Int = 0,
            @ProtoId(13) @JvmField val jumpurl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x86 {
    internal class SubMsgType0x86 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val notifyFlag: Int = 0,
            @ProtoId(2) @JvmField val notifyWording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x87 {
    internal class SubMsgType0x87 : ProtoBuf {
        @Serializable
        internal class CloneInfo(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val remark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val originNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val showInAio: Int = 0,
            @ProtoId(5) @JvmField val toUin: Long = 0L,
            @ProtoId(6) @JvmField val toNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val srcGender: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val friendMsgTypeFlag: Long = 0L,
            @ProtoId(2) @JvmField val msgMsgNotify: List<MsgNotify>? = null,
            @ProtoId(3) @JvmField val msgMsgNotifyUnread: MsgNotifyUnread? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgNotify(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val fuin: Long = 0L,
            @ProtoId(3) @JvmField val time: Int = 0,
            @ProtoId(4) @JvmField val reqsubtype: Int = 0,
            @ProtoId(5) @JvmField val maxCount: Int = 0,
            @ProtoId(6) @JvmField val msgCloneInfo: CloneInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgNotifyUnread(
            @ProtoId(1) @JvmField val unreadcount: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x89 {
    internal class Submsgtype0x89 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val uiUin: Long = 0L,
            @ProtoId(2) @JvmField val pushRedTs: Int = 0,
            @ProtoId(3) @JvmField val msgNumRed: List<NumRedBusiInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class NumRedBusiInfo(
            @ProtoId(1) @JvmField val clientVerBegin: String = "",
            @ProtoId(2) @JvmField val clientVerEnd: String = "",
            @ProtoId(3) @JvmField val platId: Int = 0,
            @ProtoId(4) @JvmField val appId: Int = 0,
            @ProtoId(5) @JvmField val androidAppId: Int = 0,
            @ProtoId(6) @JvmField val iosAppId: Int = 0,
            @ProtoId(7) @JvmField val path: String = "",
            @ProtoId(8) @JvmField val androidPath: String = "",
            @ProtoId(9) @JvmField val iosPath: String = "",
            @ProtoId(10) @JvmField val missionid: String = "",
            @ProtoId(11) @JvmField val msgid: Long = 0L,
            @ProtoId(12) @JvmField val status: Int = 0,
            @ProtoId(13) @JvmField val expireTime: Int = 0,
            @ProtoId(14) @JvmField val int32Appset: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x8d {
    internal class SubMsgType0x8d : ProtoBuf {
        @Serializable
        internal class ChannelNotify(
            @ProtoId(1) @JvmField val channelId: Long = 0L,
            @ProtoId(2) @JvmField val channelName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val topArticleIdList: List<Long>? = null
        ) : ProtoBuf

        @Serializable
        internal class CommentFeeds(
            @ProtoId(1) @JvmField val feedsOwner: Long = 0L,
            @ProtoId(2) @JvmField val feedsId: Long = 0L,
            @ProtoId(3) @JvmField val commentUin: Long = 0L,
            @ProtoId(4) @JvmField val commentId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val replyUin: Long = 0L,
            @ProtoId(6) @JvmField val replyId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val commentInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val feedsSubject: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class DeleteComment(
            @ProtoId(1) @JvmField val feedsOwner: Long = 0L,
            @ProtoId(2) @JvmField val feedsId: Long = 0L,
            @ProtoId(3) @JvmField val commentUin: Long = 0L,
            @ProtoId(4) @JvmField val commentId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val replyUin: Long = 0L,
            @ProtoId(6) @JvmField val replyId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val deleteUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class DeleteFeeds(
            @ProtoId(1) @JvmField val feedsOwner: Long = 0L,
            @ProtoId(2) @JvmField val feedsId: Long = 0L,
            @ProtoId(3) @JvmField val deleteUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class LikeFeeds(
            @ProtoId(1) @JvmField val feedsOwner: Long = 0L,
            @ProtoId(2) @JvmField val feedsId: Long = 0L,
            @ProtoId(3) @JvmField val likeUin: Long = 0L,
            @ProtoId(4) @JvmField val feedsSubject: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgNotifyInfos: List<NotifyBody>? = null,
            @ProtoId(2) @JvmField val redSpotNotifyBody: RedSpotNotifyBody? = null
        ) : ProtoBuf

        @Serializable
        internal class NotifyBody(
            @ProtoId(1) @JvmField val notifyType: Int = 0,
            @ProtoId(2) @JvmField val seq: Int = 0,
            @ProtoId(3) @JvmField val pushTime: Int = 0,
            @ProtoId(10) @JvmField val msgPublishFeeds: PublishFeeds? = null,
            @ProtoId(11) @JvmField val msgCommentFeeds: CommentFeeds? = null,
            @ProtoId(12) @JvmField val msgLikeFeeds: LikeFeeds? = null,
            @ProtoId(13) @JvmField val msgDeleteFeeds: DeleteFeeds? = null,
            @ProtoId(14) @JvmField val msgDeleteComment: DeleteComment? = null
        ) : ProtoBuf

        @Serializable
        internal class PublishFeeds(
            @ProtoId(1) @JvmField val feedsOwner: Long = 0L,
            @ProtoId(2) @JvmField val feedsId: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class RedSpotNotifyBody(
            @ProtoId(1) @JvmField val time: Int = 0,
            @ProtoId(2) @JvmField val newChannelList: List<Long>? = null,
            @ProtoId(3) @JvmField val guideWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val msgChannelNotify: ChannelNotify? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x8f {
    internal class Submsgtype0x8f : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgSourceId: SourceID? = null,
            @ProtoId(2) @JvmField val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val enumMsgType: Int /* enum */ = 1,
            @ProtoId(4) @JvmField val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val authorUin: Long = 0L,
            @ProtoId(6) @JvmField val confirmUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class SourceID(
            @ProtoId(1) @JvmField val sourceType: Int = 0,
            @ProtoId(2) @JvmField val sourceCode: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x90 {
    internal class SubMsgType0x90 : ProtoBuf {
        @Serializable
        internal class DpNotifyMsgBdoy(
            @ProtoId(1) @JvmField val pid: Int = 0,
            @ProtoId(2) @JvmField val din: Long = 0L,
            @ProtoId(3) @JvmField val msgNotifyInfo: List<NotifyItem>? = null,
            @ProtoId(4) @JvmField val extendInfo: String = ""
        ) : ProtoBuf

        @Serializable
        internal class Head(
            @ProtoId(1) @JvmField val cmd: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgHead: Head? = null,
            @ProtoId(2) @JvmField val msgBody: PushBody? = null
        ) : ProtoBuf

        @Serializable
        internal class NotifyItem(
            @ProtoId(1) @JvmField val propertyid: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OccupyMicrophoneNotifyMsgBody(
            @ProtoId(1) @JvmField val uin: Int = 0,
            @ProtoId(2) @JvmField val din: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class PushBody(
            @ProtoId(1) @JvmField val msgDpNotifyBody: DpNotifyMsgBdoy? = null,
            @ProtoId(2) @JvmField val msgOccupyMicrophoneBody: OccupyMicrophoneNotifyMsgBody? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x92 {
    internal class SubMsgType0x92 : ProtoBuf {
        @Serializable
        internal class CrmS2CMsgHead(
            @ProtoId(1) @JvmField val crmSubCmd: Int = 0,
            @ProtoId(2) @JvmField val headLen: Int = 0,
            @ProtoId(3) @JvmField val verNo: Int = 0,
            @ProtoId(4) @JvmField val kfUin: Long = 0L,
            @ProtoId(5) @JvmField val seq: Int = 0,
            @ProtoId(6) @JvmField val packNum: Int = 0,
            @ProtoId(7) @JvmField val curPack: Int = 0,
            @ProtoId(8) @JvmField val bufSig: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val msgCrmCommonHead: CrmS2CMsgHead? = null,
            @ProtoId(100) @JvmField val msgPushEmanMsg: S2CPushEmanMsgToC? = null
        ) : ProtoBuf {
            @Serializable
            internal class S2CPushEmanMsgToC(
                @ProtoId(1) @JvmField val uin: Long = 0L,
                @ProtoId(2) @JvmField val xml: String = ""
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0x93 {
    internal class Submsgtype0x93 : ProtoBuf {
        @Serializable
        internal class LiteMailIndexInfo(
            @ProtoId(1) @JvmField val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val msgSourceId: SourceID? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgUmcChanged: UnreadMailCountChanged? = null,
            @ProtoId(3) @JvmField val msgStateChanged: StateChangeNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class SourceID(
            @ProtoId(1) @JvmField val sourceType: Int = 0,
            @ProtoId(2) @JvmField val sourceCode: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class StateChangeNotify(
            @ProtoId(1) @JvmField val msgSourceId: SourceID? = null,
            @ProtoId(2) @JvmField val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val enumMsgType: Int /* enum */ = 1,
            @ProtoId(4) @JvmField val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val reqUin: Long = 0L,
            @ProtoId(6) @JvmField val msgLiteMailIndex: List<LiteMailIndexInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class UnreadMailCountChanged(
            @ProtoId(1) @JvmField val msgUmc: UnreadMailCountInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class UnreadMailCountInfo(
            @ProtoId(1) @JvmField val unreadCount: Int = 0,
            @ProtoId(2) @JvmField val dataVersion: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x94 {
    internal class Submsgtype0x94 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val taskId: Int = 0,
            @ProtoId(2) @JvmField val folderReddotFlag: Int = 0,
            @ProtoId(3) @JvmField val discoverReddotFlag: Int = 0,
            @ProtoId(4) @JvmField val startTs: Int = 0,
            @ProtoId(5) @JvmField val endTs: Int = 0,
            @ProtoId(6) @JvmField val periodOfValidity: Int = 0,
            @ProtoId(7) @JvmField val folderMsg: String = "",
            @ProtoId(8) @JvmField val discountReddotFlag: Int = 0,
            @ProtoId(9) @JvmField val nearbyReddotFlag: Int = 0,
            @ProtoId(10) @JvmField val mineReddotFlag: Int = 0,
            @ProtoId(11) @JvmField val onlyDiscoverReddotFlag: Int = 0,
            @ProtoId(12) @JvmField val onlyDiscountReddotFlag: Int = 0,
            @ProtoId(13) @JvmField val onlyNearbyReddotFlag: Int = 0,
            @ProtoId(14) @JvmField val onlyMineReddotFlag: Int = 0,
            @ProtoId(15) @JvmField val taskType: Int = 0,
            @ProtoId(16) @JvmField val taskInfo: String = "",
            @ProtoId(17) @JvmField val typeName: String = "",
            @ProtoId(18) @JvmField val typeColor: String = "",
            @ProtoId(19) @JvmField val jumpUrl: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x96 {
    internal class Submsgtype0x96 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val pushMsg: String = "",
            @ProtoId(2) @JvmField val pushType: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x97 {
    internal class Submsgtype0x97 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val businessUin: String = "",
            @ProtoId(2) @JvmField val jsonContext: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x98 {
    internal class Submsgtype0x98 : ProtoBuf {
        @Serializable
        internal class ModBlock(
            @ProtoId(1) @JvmField val op: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val subCmd: Int = 0,
            @ProtoId(3) @JvmField val msgModBlock: ModBlock? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9b {
    internal class SubMsgType0x9b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val appId: Long = 0L,
            @ProtoId(2) @JvmField val mainType: Int = 0,
            @ProtoId(3) @JvmField val subType: Int = 0,
            @ProtoId(4) @JvmField val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val workflowId: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PbOfficeNotify(
            @ProtoId(1) @JvmField val optUint32MyofficeFlag: Int = 0,
            @ProtoId(2) @JvmField val uint64Appid: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9d {
    internal class SubMsgType0x9d : ProtoBuf {
        @Serializable
        internal class ModuleUpdateNotify(
            @ProtoId(1) @JvmField val moduleId: Int = 0,
            @ProtoId(2) @JvmField val moduleVersion: Int = 0,
            @ProtoId(3) @JvmField val moduleState: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val lolaModuleUpdate: List<ModuleUpdateNotify>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9e {
    internal class SubmsgType0x9e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val url: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val authKey: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9f {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) @JvmField val showLastest: Int = 0,
        @ProtoId(2) @JvmField val senderUin: Long = 0L,
        @ProtoId(3) @JvmField val receiverUin: Long = 0L,
        @ProtoId(4) @JvmField val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(7) @JvmField val sint32Sessiontype: Int = 0,
        @ProtoId(8) @JvmField val groupUin: Long = 0L
    ) : ProtoBuf
}


internal class Submsgtype0xa0 {
    internal class Submsgtype0xa0 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val isMassBlessOpen: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0xa1 {
    internal class Submsgtype0xa1 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val qid: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(3) @JvmField val fixed32UpdateTime: Int = 0,
            @ProtoId(4) @JvmField val teamCreatedDestroied: Int = 0,
            @ProtoId(5) @JvmField val uint64OfficeFaceChangedUins: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xa2 {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) @JvmField val showLastest: Int = 0,
        @ProtoId(2) @JvmField val senderUin: Long = 0L,
        @ProtoId(3) @JvmField val receiverUin: Long = 0L,
        @ProtoId(4) @JvmField val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val authkey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


internal class Submsgtype0xa4 {
    internal class Submsgtype0xa4 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val brief: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val url: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xa8 {
    internal class SubMsgType0xa8 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val actionType: Int = 0,
            @ProtoId(2) @JvmField val actionSubType: Int = 0,
            @ProtoId(3) @JvmField val msgSummary: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val extendContent: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xaa {
    internal class SubMsgType0xaa : ProtoBuf {
        @Serializable
        internal class GameTeamMsgBody(
            @ProtoId(1) @JvmField val gameTeamCmd: Int = 0,
            @ProtoId(2) @JvmField val msgTurnOverMessage: GameTeamTurnOverMessage? = null,
            @ProtoId(3) @JvmField val msgStartGameMessage: GameTeamStartGameMessage? = null,
            @ProtoId(4) @JvmField val msgUpdateTeamMessage: GameTeamUpdateTeamMessage? = null
        ) : ProtoBuf

        @Serializable
        internal class GameTeamStartGameMessage(
            @ProtoId(1) @JvmField val gamedata: String = "",
            @ProtoId(2) @JvmField val platformType: Int = 0,
            @ProtoId(3) @JvmField val title: String = "",
            @ProtoId(4) @JvmField val summary: String = "",
            @ProtoId(5) @JvmField val picUrl: String = "",
            @ProtoId(6) @JvmField val appid: String = "",
            @ProtoId(7) @JvmField val appStoreId: String = "",
            @ProtoId(8) @JvmField val packageName: String = "",
            @ProtoId(9) @JvmField val createMsgTime: Long = 0L,
            @ProtoId(10) @JvmField val expire: Int = 0,
            @ProtoId(11) @JvmField val buildTeamTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class GameTeamTurnOverMessage(
            @ProtoId(1) @JvmField val teamId: String = "",
            @ProtoId(2) @JvmField val sessionType: Int = 0,
            @ProtoId(3) @JvmField val sourceUin: String = "",
            @ProtoId(4) @JvmField val actionUin: String = "",
            @ProtoId(5) @JvmField val actionType: Int = 0,
            @ProtoId(6) @JvmField val currentCount: Int = 0,
            @ProtoId(7) @JvmField val totalCount: Int = 0,
            @ProtoId(8) @JvmField val createMsgTime: Long = 0L,
            @ProtoId(9) @JvmField val status: Int = 0,
            @ProtoId(10) @JvmField val expire: Int = 0,
            @ProtoId(11) @JvmField val buildTeamTime: Long = 0L,
            @ProtoId(12) @JvmField val leaderUin: String = "",
            @ProtoId(13) @JvmField val uin32LeaderStatus: Int = 0,
            @ProtoId(14) @JvmField val inviteSourceList: List<InviteSource>? = null
        ) : ProtoBuf

        @Serializable
        internal class GameTeamUpdateTeamMessage(
            @ProtoId(1) @JvmField val teamId: String = "",
            @ProtoId(2) @JvmField val gameId: String = "",
            @ProtoId(3) @JvmField val status: Int = 0,
            @ProtoId(4) @JvmField val modeImg: String = "",
            @ProtoId(5) @JvmField val currentCount: Int = 0,
            @ProtoId(6) @JvmField val createMsgTime: Long = 0L,
            @ProtoId(7) @JvmField val expire: Int = 0,
            @ProtoId(8) @JvmField val buildTeamTime: Long = 0L,
            @ProtoId(9) @JvmField val leaderUin: String = "",
            @ProtoId(10) @JvmField val uin32LeaderStatus: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class InviteSource(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val src: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val cmd: Int = 0,
            @ProtoId(2) @JvmField val msgGameTeamMsg: GameTeamMsgBody? = null,
            @ProtoId(3) @JvmField val msgOnlineDocMsg: OnlineDocMsgBody? = null
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocMsgBody(
            @ProtoId(1) @JvmField val onlineDocCmd: Int = 0,
            @ProtoId(2) @JvmField val msgPushChangeTitleMessage: OnlineDocPushChangeTitleMessage? = null,
            @ProtoId(3) @JvmField val msgPushNewPadMessage: OnlineDocPushNewPadMessage? = null,
            @ProtoId(4) @JvmField val msgPushPreviewToEdit: OnlineDocPushPreviewToEditMessage? = null
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocPushChangeTitleMessage(
            @ProtoId(1) @JvmField val domainid: Int = 0,
            @ProtoId(2) @JvmField val localpadid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val lastEditorUin: Long = 0L,
            @ProtoId(5) @JvmField val lastEditorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val lastEditTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocPushNewPadMessage(
            @ProtoId(1) @JvmField val padUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val type: Int = 0,
            @ProtoId(3) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val createTime: Long = 0L,
            @ProtoId(5) @JvmField val creatorUin: Long = 0L,
            @ProtoId(6) @JvmField val creatorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val lastEditorUin: Long = 0L,
            @ProtoId(8) @JvmField val lastEditorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val lastEditTime: Long = 0L,
            @ProtoId(10) @JvmField val boolPinnedFlag: Boolean = false,
            @ProtoId(11) @JvmField val lastViewerUin: Long = 0L,
            @ProtoId(12) @JvmField val lastViewerNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) @JvmField val lastViewTime: Long = 0L,
            @ProtoId(14) @JvmField val lastPinnedTime: Long = 0L,
            @ProtoId(15) @JvmField val currentUserBrowseTime: Long = 0L,
            @ProtoId(16) @JvmField val hostuserUin: Long = 0L,
            @ProtoId(17) @JvmField val hostuserNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(18) @JvmField val lastAuthTime: Long = 0L,
            @ProtoId(19) @JvmField val policy: Int = 0,
            @ProtoId(20) @JvmField val rightFlag: Int = 0,
            @ProtoId(21) @JvmField val domainid: Int = 0,
            @ProtoId(22) @JvmField val localpadid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(23) @JvmField val lastUnpinnedTime: Long = 0L,
            @ProtoId(24) @JvmField val boolDeleteFlag: Boolean = false,
            @ProtoId(25) @JvmField val lastDeleteTime: Long = 0L,
            @ProtoId(26) @JvmField val thumbUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(27) @JvmField val pdid: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocPushPreviewToEditMessage(
            @ProtoId(1) @JvmField val version: Int = 0,
            @ProtoId(2) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val padUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val aioSession: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xab {
    internal class SubMsgType0xab : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val gc: Long = 0L,
            @ProtoId(3) @JvmField val rewardId: String = "",
            @ProtoId(4) @JvmField val rewardStatus: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0xae {
    internal class SubMsgType0xae : ProtoBuf {
        @Serializable
        internal class AddFriendSource(
            @ProtoId(1) @JvmField val source: Int = 0,
            @ProtoId(2) @JvmField val subSource: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val msgPeopleMayKonw: PushPeopleMayKnow? = null,
            @ProtoId(3) @JvmField val msgPersonsMayKnow: PushPeopleMayKnowV2? = null
        ) : ProtoBuf

        @Serializable
        internal class PersonMayKnow(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val age: Int = 0,
            @ProtoId(4) @JvmField val sex: Int = 0,
            @ProtoId(5) @JvmField val mainReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val soureReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val alghrithm: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val source: Int = 0,
            @ProtoId(9) @JvmField val msgIosSource: AddFriendSource? = null,
            @ProtoId(10) @JvmField val msgAndroidSource: AddFriendSource? = null,
            @ProtoId(11) @JvmField val msg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(12) @JvmField val gameSource: Int = 0,
            @ProtoId(13) @JvmField val roleName: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushPeopleMayKnow(
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(1) @JvmField val fixed32Timestamp: Int = 0,
            @ProtoId(2) @JvmField val wordingMsg: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushPeopleMayKnowV2(
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(1) @JvmField val fixed32Timestamp: Int = 0,
            @ProtoId(2) @JvmField val msgFriendList: List<PersonMayKnow>? = null,
            @ProtoId(3) @JvmField val roleName: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xb1 {
    internal class Submsgtype0xb1 : ProtoBuf {
        @Serializable
        internal class DealInviteInfo(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val groupCode: Long = 0L,
            @ProtoId(3) @JvmField val id: String = "",
            @ProtoId(4) @JvmField val dealResult: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class InviteInfo(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val groupCode: Long = 0L,
            @ProtoId(3) @JvmField val expireTime: Int = 0,
            @ProtoId(4) @JvmField val id: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val notifyType: Int = 0,
            @ProtoId(2) @JvmField val inviteInfo: InviteInfo? = null,
            @ProtoId(3) @JvmField val univiteInfo: UninviteInfo? = null,
            @ProtoId(4) @JvmField val dealInfo: DealInviteInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class UninviteInfo(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val groupCode: Long = 0L,
            @ProtoId(3) @JvmField val id: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xb3 {
    class SubMsgType0xb3 {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val msgAddFrdNotify: PushAddFrdNotify
        ) : ProtoBuf

        @Serializable
        internal class PushAddFrdNotify(
            @ProtoId(1) @JvmField val fuin: Long = 0L,
            @ProtoId(2) @JvmField val fuinBubbleId: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(3) @JvmField val fixed32Timestamp: Int = 0,
            @ProtoId(4) @JvmField val wording: String = "", // 我们已经是好友啦，一起来聊天吧!
            @ProtoId(5) @JvmField val fuinNick: String = "",
            @ProtoId(6) @JvmField val sourceId: Int = 0,
            @ProtoId(7) @JvmField val subsourceId: Int = 0,
            @ProtoId(8) @JvmField val mobile: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val reqUin: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xb5 {
    internal class SubMsgType0xb5 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val grayTipContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val animationPackageId: Int = 0,
            @ProtoId(3) @JvmField val animationPackageUrlA: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val animationPackageUrlI: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val remindBrief: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val giftId: Int = 0,
            @ProtoId(7) @JvmField val giftCount: Int = 0,
            @ProtoId(8) @JvmField val animationBrief: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val senderUin: Long = 0L,
            @ProtoId(10) @JvmField val receiverUin: Long = 0L,
            @ProtoId(11) @JvmField val stmessageTitle: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(12) @JvmField val stmessageSubtitle: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) @JvmField val stmessageMessage: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(14) @JvmField val stmessageGiftpicid: Int = 0,
            @ProtoId(15) @JvmField val stmessageComefrom: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(16) @JvmField val stmessageExflag: Int = 0,
            @ProtoId(17) @JvmField val toAllGiftId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10000) @JvmField val groupCode: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xbe {
    internal class SubMsgType0xbe : ProtoBuf {
        @Serializable
        internal class Medal(
            @ProtoId(1) @JvmField val id: Int = 0,
            @ProtoId(2) @JvmField val level: Int = 0,
            @ProtoId(3) @JvmField val type: Int = 0,
            @ProtoId(4) @JvmField val iconUrl: String = "",
            @ProtoId(5) @JvmField val flashUrl: String = "",
            @ProtoId(6) @JvmField val name: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val groupCode: Long = 0L,
            @ProtoId(3) @JvmField val notifyType: Int = 0,
            @ProtoId(4) @JvmField val onlineLevel: Int = 0,
            @ProtoId(5) @JvmField val msgMedalList: List<Medal>? = null
        ) : ProtoBuf
    }
}


/*
internal class Submsgtype0xc1 {
    internal class Submsgtype0xc1 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val groupid: Long = 0L,
            @ProtoId(2) @JvmField val memberNum: Int = 0,
            @ProtoId(3) @JvmField val data: String = ""
        ) : ProtoBuf
    }
}
*/

internal class Submsgtype0xc3 {
    internal class Submsgtype0xc3 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val pushData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val timestamp: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc5 {
    internal class Submsgtype0xc5 : ProtoBuf {
        @Serializable
        internal class BBInfo(
            @ProtoId(1) @JvmField val bbUin: Long = 0L,
            @ProtoId(2) @JvmField val src: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class BiuBody(
            @ProtoId(1) @JvmField val biuUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class CommentInfo(
            @ProtoId(2) @JvmField val commentUin: Long = 0L,
            @ProtoId(3) @JvmField val commentId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val replyUin: Long = 0L,
            @ProtoId(5) @JvmField val replyId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val commentContent: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class LikeInfo(
            @ProtoId(2) @JvmField val likeUin: Long = 0L,
            @ProtoId(3) @JvmField val op: Int = 0,
            @ProtoId(4) @JvmField val replyId: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val bid: Int = 0,
            @ProtoId(2) @JvmField val source: Int = 0,
            @ProtoId(3) @JvmField val operatorType: Int /* enum */ = 1,
            @ProtoId(4) @JvmField val articleId: Long = 0L,
            @ProtoId(5) @JvmField val pushTime: Int = 0,
            @ProtoId(6) @JvmField val seq: Long = 0L,
            @ProtoId(10) @JvmField val msgNotifyInfos: NotifyBody? = null,
            @ProtoId(11) @JvmField val diandianCookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NotifyBody(
            @ProtoId(1) @JvmField val msgStyleSheet: StyleSheet? = null,
            @ProtoId(10) @JvmField val msgCommentArticle: CommentInfo? = null,
            @ProtoId(11) @JvmField val msgLikeArticle: LikeInfo? = null,
            @ProtoId(12) @JvmField val msgBbInfo: BBInfo? = null,
            @ProtoId(13) @JvmField val redPointInfo: List<RedPointInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class RedPointInfo(
            @ProtoId(1) @JvmField val itemId: Int = 0,
            @ProtoId(2) @JvmField val redPointItemType: Int /* enum */ = 0,
            @ProtoId(3) @JvmField val url: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val effectTime: Long = 0L,
            @ProtoId(5) @JvmField val failureTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class StyleSheet(
            @ProtoId(1) @JvmField val showFolder: Int = 0,
            @ProtoId(2) @JvmField val folderRedType: Int /* enum */ = 0,
            @ProtoId(3) @JvmField val orangeWord: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val summary: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val msgTipBody: TipsBody? = null,
            @ProtoId(6) @JvmField val showLockScreen: Int = 0,
            @ProtoId(7) @JvmField val msgType: Int /* enum */ = 0,
            @ProtoId(8) @JvmField val msgBiuBody: BiuBody? = null,
            @ProtoId(9) @JvmField val isLow: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class TipsBody(
            @ProtoId(1) @JvmField val tipsUiType: Int /* enum */ = 0,
            @ProtoId(2) @JvmField val uin: Long = 0L,
            @ProtoId(3) @JvmField val iconUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val schema: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val businessInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc6 {
    internal class SubMsgType0xc6 : ProtoBuf {
        @Serializable
        internal class AccountExceptionAlertBody(
            @ProtoId(1) @JvmField val title: String = "",
            @ProtoId(2) @JvmField val content: String = "",
            @ProtoId(3) @JvmField val leftButtonText: String = "",
            @ProtoId(4) @JvmField val rightButtonText: String = "",
            @ProtoId(5) @JvmField val rightButtonLink: String = "",
            @ProtoId(6) @JvmField val leftButtonId: Int = 0,
            @ProtoId(7) @JvmField val rightButtonId: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val secCmd: Int = 0,
            @ProtoId(2) @JvmField val msgS2cAccountExceptionNotify: AccountExceptionAlertBody? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc7 {
    internal class Submsgtype0xc7 : ProtoBuf {
        @Serializable
        internal class ForwardBody(
            @ProtoId(1) @JvmField val notifyType: Int = 0,
            @ProtoId(2) @JvmField val opType: Int = 0,
            @ProtoId(3000) @JvmField val msgHotFriendNotify: HotFriendNotify? = null,
            @ProtoId(4000) @JvmField val msgRelationalChainChange: RelationalChainChange? = null
        ) : ProtoBuf

        @Serializable
        internal class FriendShipFlagNotify(
            @ProtoId(1) @JvmField val dstUin: Long = 0L,
            @ProtoId(2) @JvmField val level1: Int = 0,
            @ProtoId(3) @JvmField val level2: Int = 0,
            @ProtoId(4) @JvmField val continuityDays: Int = 0,
            @ProtoId(5) @JvmField val chatFlag: Int = 0,
            @ProtoId(6) @JvmField val lastChatTime: Long = 0L,
            @ProtoId(7) @JvmField val notifyTime: Long = 0L,
            @ProtoId(8) @JvmField val seq: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class HotFriendNotify(
            @ProtoId(1) @JvmField val dstUin: Long = 0L,
            @ProtoId(2) @JvmField val praiseHotLevel: Int = 0,
            @ProtoId(3) @JvmField val chatHotLevel: Int = 0,
            @ProtoId(4) @JvmField val praiseHotDays: Int = 0,
            @ProtoId(5) @JvmField val chatHotDays: Int = 0,
            @ProtoId(6) @JvmField val closeLevel: Int = 0,
            @ProtoId(7) @JvmField val closeDays: Int = 0,
            @ProtoId(8) @JvmField val praiseFlag: Int = 0,
            @ProtoId(9) @JvmField val chatFlag: Int = 0,
            @ProtoId(10) @JvmField val closeFlag: Int = 0,
            @ProtoId(11) @JvmField val notifyTime: Long = 0L,
            @ProtoId(12) @JvmField val lastPraiseTime: Long = 0L,
            @ProtoId(13) @JvmField val lastChatTime: Long = 0L,
            @ProtoId(14) @JvmField val qzoneHotLevel: Int = 0,
            @ProtoId(15) @JvmField val qzoneHotDays: Int = 0,
            @ProtoId(16) @JvmField val qzoneFlag: Int = 0,
            @ProtoId(17) @JvmField val lastQzoneTime: Long = 0L,
            @ProtoId(51) @JvmField val showRecheckEntry: Int = 0,
            @ProtoId(52) @JvmField val wildcardWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(100) @JvmField val loverFlag: Int = 0,
            @ProtoId(200) @JvmField val keyHotLevel: Int = 0,
            @ProtoId(201) @JvmField val keyHotDays: Int = 0,
            @ProtoId(202) @JvmField val keyFlag: Int = 0,
            @ProtoId(203) @JvmField val lastKeyTime: Long = 0L,
            @ProtoId(204) @JvmField val keyWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(205) @JvmField val keyTransFlag: Int = 0,
            @ProtoId(206) @JvmField val loverKeyBusinessType: Int = 0,
            @ProtoId(207) @JvmField val loverKeySubBusinessType: Int = 0,
            @ProtoId(208) @JvmField val loverKeyMainWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(209) @JvmField val loverKeyLinkWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(300) @JvmField val boatLevel: Int = 0,
            @ProtoId(301) @JvmField val boatDays: Int = 0,
            @ProtoId(302) @JvmField val boatFlag: Int = 0,
            @ProtoId(303) @JvmField val lastBoatTime: Int = 0,
            @ProtoId(304) @JvmField val boatWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(400) @JvmField val notifyType: Int = 0,
            @ProtoId(401) @JvmField val msgFriendshipFlagNotify: FriendShipFlagNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgModInfos: List<ForwardBody>? = null
        ) : ProtoBuf

        @Serializable
        internal class RelationalChainChange(
            @ProtoId(1) @JvmField val appid: Long = 0L,
            @ProtoId(2) @JvmField val srcUin: Long = 0L,
            @ProtoId(3) @JvmField val dstUin: Long = 0L,
            @ProtoId(4) @JvmField val changeType: Int /* enum */ = 1,
            @ProtoId(5) @JvmField val msgRelationalChainInfoOld: RelationalChainInfo? = null,
            @ProtoId(6) @JvmField val msgRelationalChainInfoNew: RelationalChainInfo? = null,
            @ProtoId(7) @JvmField val msgToDegradeInfo: ToDegradeInfo? = null,
            @ProtoId(20) @JvmField val relationalChainInfos: List<RelationalChainInfos>? = null,
            @ProtoId(100) @JvmField val uint32FeatureId: List<Int>? = null
        ) : ProtoBuf

        @Serializable
        internal class RelationalChainInfo(
            @ProtoId(1) @JvmField val type: Int /* enum */ = 1,
            @ProtoId(2) @JvmField val attr: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(1002) @JvmField val intimateInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(91001) @JvmField val musicSwitch: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(101001) @JvmField val mutualmarkAlienation: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class RelationalChainInfos(
            @ProtoId(1) @JvmField val msgRelationalChainInfoOld: RelationalChainInfo? = null,
            @ProtoId(2) @JvmField val msgRelationalChainInfoNew: RelationalChainInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class ToDegradeInfo(
            @ProtoId(1) @JvmField val toDegradeItem: List<ToDegradeItem>? = null,
            @ProtoId(2) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val notifyTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ToDegradeItem(
            @ProtoId(1) @JvmField val type: Int /* enum */ = 1,
            @ProtoId(2) @JvmField val oldLevel: Int = 0,
            @ProtoId(3) @JvmField val newLevel: Int = 0,
            @ProtoId(11) @JvmField val continuityDays: Int = 0,
            @ProtoId(12) @JvmField val lastActionTime: Long = 0L
        ) : ProtoBuf
    }
}

internal class Mutualmark {
    class Mutualmark : ProtoBuf {
        @Serializable
        internal class MutualmarkInfo(
            @ProtoId(1) @JvmField val lastActionTime: Long = 0L,
            @ProtoId(2) @JvmField val level: Int = 0,
            @ProtoId(3) @JvmField val lastChangeTime: Long = 0L,
            @ProtoId(4) @JvmField val continueDays: Int = 0,
            @ProtoId(5) @JvmField val wildcardWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val notifyTime: Long = 0L,
            @ProtoId(7) @JvmField val iconStatus: Long = 0L,
            @ProtoId(8) @JvmField val iconStatusEndTime: Long = 0L,
            @ProtoId(9) @JvmField val closeFlag: Int = 0,
            @ProtoId(10) @JvmField val resourceInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ResourceInfo17(
            @ProtoId(1) @JvmField val dynamicUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val staticUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val cartoonUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val cartoonMd5: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val playCartoon: Int = 0,
            @ProtoId(6) @JvmField val word: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc9 {
    class Submsgtype0xc9 : ProtoBuf {
        @Serializable
        internal class BusinessMsg(
            @ProtoId(1) @JvmField val msgType: Int /* enum */ = 0,
            @ProtoId(2) @JvmField val msgData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val boolTabVisible: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val fromUin: Long = 0L,
            @ProtoId(3) @JvmField val actionUin: Long = 0L,
            @ProtoId(4) @JvmField val source: Int /* enum */ = 0,
            @ProtoId(5) @JvmField val msgBusinessMsg: List<BusinessMsg>? = null,
            @ProtoId(6) @JvmField val boolNewFriend: Boolean = false
        ) : ProtoBuf
    }
}


internal class Submsgtype0xca {
    class Submsgtype0xca : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val msgList: List<MsgContent>? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgContent(
            @ProtoId(1) @JvmField val tag: Long = 0L,
            @ProtoId(2) @JvmField val msgType: Long = 0L,
            @ProtoId(3) @JvmField val seq: Long = 0L,
            @ProtoId(4) @JvmField val content: String = "",
            @ProtoId(5) @JvmField val actionId: Long = 0L,
            @ProtoId(6) @JvmField val ts: Long = 0L,
            @ProtoId(7) @JvmField val expts: Long = 0L,
            @ProtoId(8) @JvmField val errorMsg: String = "",
            @ProtoId(9) @JvmField val showSpace: Long = 0L,
            @ProtoId(10) @JvmField val regionUrl: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xcb {
    internal class SubMsgType0xcb : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val anchorStatus: Int = 0,
            @ProtoId(2) @JvmField val jumpSchema: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val anchorNickname: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val anchorHeadUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val liveWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val liveEndWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val c2cMsgWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val liveWordingType: Int = 0,
            @ProtoId(9) @JvmField val endWordingType: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0xcc {
    internal class SubMsgType0xcc : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val uin: Long = 0L,
            @ProtoId(4) @JvmField val unionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val subType: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val feedId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val coverUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xce {
    internal class Submsgtype0xce : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val int64StartTime: Long = 0L,
            @ProtoId(2) @JvmField val int64EndTime: Long = 0L,
            @ProtoId(3) @JvmField val params: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xcf {
    internal class Submsgtype0xcf : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val rsptype: Int = 0,
            @ProtoId(2) @JvmField val rspbody: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xd0 {
    internal class SubMsgType0xd0 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val hotTopicId: Long = 0L,
            @ProtoId(4) @JvmField val hotTopicName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val bigVId: Long = 0L,
            @ProtoId(6) @JvmField val bigVUnionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val pgcType: Int = 0,
            @ProtoId(8) @JvmField val pgcColumnUnionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val link: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val subType: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(11) @JvmField val coverUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xd7 {
    internal class SubMsgType0xd7 : ProtoBuf {
        @Serializable
        internal class Content(
            @ProtoId(1) @JvmField val fromUser: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val plainText: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val buluoWord: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val richFreshWord: AppointDefine.RichText? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val msgboxUnreadCount: Int = 0,
            @ProtoId(3) @JvmField val unreadCount: Int = 0,
            @ProtoId(4) @JvmField val msgContent: Content? = null,
            @ProtoId(5) @JvmField val timestamp: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xda {
    internal class SubMsgType0xda : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val subType: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val feedId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val unionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val commentId: Int = 0,
            @ProtoId(8) @JvmField val iconUnionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val coverUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) @JvmField val operType: Int = 0,
            @ProtoId(11) @JvmField val groupUnionid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(12) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) @JvmField val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(14) @JvmField val fromNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(15) @JvmField val vidUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(16) @JvmField val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdb {
    internal class Submsgtype0xdb : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val rsptype: Int = 0,
            @ProtoId(2) @JvmField val rspbody: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdc {
    internal class Submsgtype0xdc : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgList: List<MsgContent>? = null,
            @ProtoId(2) @JvmField val msgType: Int = 0,
            @ProtoId(3) @JvmField val msgList0x02: List<MsgContent>? = null,
            @ProtoId(4) @JvmField val minQqVer: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgContent(
            @ProtoId(1) @JvmField val masterPri: Long = 0L,
            @ProtoId(2) @JvmField val subPri: Long = 0L,
            @ProtoId(3) @JvmField val showTimes: Long = 0L,
            @ProtoId(4) @JvmField val showBegTs: Long = 0L,
            @ProtoId(5) @JvmField val expTs: Long = 0L,
            @ProtoId(6) @JvmField val msgSentTs: Long = 0L,
            @ProtoId(7) @JvmField val actionId: Long = 0L,
            @ProtoId(8) @JvmField val wording: String = "",
            @ProtoId(9) @JvmField val scheme: String = "",
            @ProtoId(10) @JvmField val regionUrl: String = "",
            @ProtoId(11) @JvmField val wordingColor: Long = 0L,
            @ProtoId(12) @JvmField val msgId: Long = 0L,
            @ProtoId(13) @JvmField val bubbleId: Long = 0L,
            @ProtoId(14) @JvmField val tips: String = "",
            @ProtoId(15) @JvmField val gameId: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdd {
    internal class Submsgtype0xdd : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val uint64InviteUin: List<Long>? = null,
            @ProtoId(3) @JvmField val inviteLeader: Long = 0L,
            @ProtoId(4) @JvmField val msgPoiInfo: WifiPOIInfo? = null,
            @ProtoId(5) @JvmField val msgPlayerState: List<PlayerState>? = null
        ) : ProtoBuf {
            @Serializable
            internal class PlayerState(
                @ProtoId(1) @JvmField val uin: Long = 0L,
                @ProtoId(2) @JvmField val state: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class SeatsInfo(
                @ProtoId(1) @JvmField val seatFlag: Int = 0,
                @ProtoId(2) @JvmField val guestUin: Long = 0L,
                @ProtoId(3) @JvmField val seatId: Int = 0,
                @ProtoId(4) @JvmField val seatSeq: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class WifiPOIInfo(
                @ProtoId(1) @JvmField val uid: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(3) @JvmField val faceId: Int = 0,
                @ProtoId(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(5) @JvmField val groupCode: Int = 0,
                @ProtoId(6) @JvmField val groupUin: Int = 0,
                @ProtoId(7) @JvmField val visitorNum: Int = 0,
                @ProtoId(8) @JvmField val wifiPoiType: Int = 0,
                @ProtoId(9) @JvmField val isMember: Int = 0,
                @ProtoId(10) @JvmField val distance: Int = 0,
                @ProtoId(11) @JvmField val msgTabSwitchOff: Int = 0,
                @ProtoId(12) @JvmField val faceUrl: String = "",
                @ProtoId(13) @JvmField val hotThemeGroupFlag: Int = 0,
                @ProtoId(14) @JvmField val bannerUrl: String = "",
                @ProtoId(15) @JvmField val specialFlag: Int = 0,
                @ProtoId(16) @JvmField val totalNumLimit: Int = 0,
                @ProtoId(17) @JvmField val isAdmin: Int = 0,
                @ProtoId(18) @JvmField val joinGroupUrl: String = "",
                @ProtoId(19) @JvmField val groupTypeFlag: Int = 0,
                @ProtoId(20) @JvmField val createrCityId: Int = 0,
                @ProtoId(21) @JvmField val isUserCreate: Int = 0,
                @ProtoId(22) @JvmField val ownerUin: Long = 0L,
                @ProtoId(23) @JvmField val auditFlag: Int = 0,
                @ProtoId(24) @JvmField val tvPkFlag: Int = 0,
                @ProtoId(25) @JvmField val subType: Int = 0,
                @ProtoId(26) @JvmField val lastMsgSeq: Long = 0L,
                @ProtoId(27) @JvmField val msgSeatsInfo: List<SeatsInfo>? = null,
                @ProtoId(28) @JvmField val flowerNum: Long = 0L,
                @ProtoId(29) @JvmField val flowerPoint: Long = 0L,
                @ProtoId(31) @JvmField val favoritesTime: Long = 0L,
                @ProtoId(32) @JvmField val favoritesExpired: Int = 0,
                @ProtoId(33) @JvmField val groupId: Int = 0,
                @ProtoId(34) @JvmField val praiseNums: Long = 0L,
                @ProtoId(35) @JvmField val reportPraiseGapTime: Long = 0L,
                @ProtoId(36) @JvmField val reportPraiseGapFrequency: Long = 0L,
                @ProtoId(37) @JvmField val getPraiseGapTime: Long = 0L,
                @ProtoId(38) @JvmField val vistorJoinGroupTime: Long = 0L,
                @ProtoId(39) @JvmField val groupIsNotExist: Int = 0,
                @ProtoId(40) @JvmField val guestNum: Int = 0,
                @ProtoId(41) @JvmField val highQualityFlag: Int = 0,
                @ProtoId(42) @JvmField val exitGroupCode: Long = 0L,
                @ProtoId(43) @JvmField val int32Latitude: Int = 0,
                @ProtoId(44) @JvmField val int32Longitude: Int = 0,
                @ProtoId(45) @JvmField val smemo: String = "",
                @ProtoId(46) @JvmField val isAllCountry: Int = 0
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0xde {
    internal class Submsgtype0xde : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgType: Int = 0,
            @ProtoId(2) @JvmField val unionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val uid: Long = 0L,
            @ProtoId(4) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val videoCover: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdf {
    internal class Submsgtype0xdf : ProtoBuf {
        @Serializable
        internal class MsgBody(
            // @ProtoId(1) @JvmField val msgGameState: ApolloGameStatus.STCMGameMessage? = null,
            @ProtoId(2) @JvmField val uint32UinList: List<Int>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe0 {
    internal class Submsgtype0xe0 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val pushExt: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe1 {
    internal class Submsgtype0xe1 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val pushExt: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe4 {
    internal class Submsgtype0xe4 : ProtoBuf {
        @Serializable
        internal class GeoInfo(
            @ProtoId(1) @JvmField val latitude: Long = 0L,
            @ProtoId(2) @JvmField val longitude: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class GiftMsg(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val toUin: Long = 0L,
            @ProtoId(3) @JvmField val productId: Int = 0,
            @ProtoId(4) @JvmField val giftId: Int = 0,
            @ProtoId(5) @JvmField val giftNum: Long = 0L,
            @ProtoId(6) @JvmField val roomid: String = "",
            @ProtoId(7) @JvmField val giftWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) @JvmField val packageurl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(50) @JvmField val curAddDuration: Int = 0,
            @ProtoId(51) @JvmField val allAddDuration: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class LikeMsg(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val toUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgMatchPlayer: Player? = null,
            @ProtoId(2) @JvmField val distance: Int = 0,
            @ProtoId(3) @JvmField val hint: String = "",
            @ProtoId(4) @JvmField val countdown: Int = 0,
            @ProtoId(5) @JvmField val key: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val type: Int = 0,
            @ProtoId(7) @JvmField val callType: Int = 0,
            @ProtoId(8) @JvmField val displayDistance: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) @JvmField val msgLike: LikeMsg? = null,
            @ProtoId(10) @JvmField val msgGift: GiftMsg? = null,
            @ProtoId(11) @JvmField val msgRoom: Room? = null
        ) : ProtoBuf

        @Serializable
        internal class Player(
            @ProtoId(1) @JvmField val uin: Long = 0L,
            @ProtoId(2) @JvmField val nickname: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val logoUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val gender: Int = 0,
            @ProtoId(5) @JvmField val level: Int = 0,
            @ProtoId(6) @JvmField val age: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Room(
            @ProtoId(1) @JvmField val roomId: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe5 {
    internal class Submsgtype0xe5 : ProtoBuf {
        @Serializable
        internal class CrmS2CMsgHead(
            @ProtoId(1) @JvmField val crmSubCmd: Int = 0,
            @ProtoId(2) @JvmField val headLen: Int = 0,
            @ProtoId(3) @JvmField val verNo: Int = 0,
            @ProtoId(4) @JvmField val kfUin: Long = 0L,
            @ProtoId(5) @JvmField val seq: Int = 0,
            @ProtoId(6) @JvmField val packNum: Int = 0,
            @ProtoId(7) @JvmField val curPack: Int = 0,
            @ProtoId(8) @JvmField val bufSig: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val subCmd: Int = 0,
            @ProtoId(2) @JvmField val msgCrmCommonHead: CrmS2CMsgHead? = null,
            @ProtoId(3) @JvmField val msgS2cCcAgentStatusChangePush: S2CCcAgentStatusChangePush? = null,
            @ProtoId(4) @JvmField val msgS2cCcConfigChangePush: S2CCcConfigChangePush? = null,
            @ProtoId(5) @JvmField val msgS2cCcExceptionOccurPush: S2CCcExceptionOccurPush? = null,
            @ProtoId(6) @JvmField val msgS2cCcTalkingStatusChangePush: S2CCcTalkingStatusChangePush? = null,
            @ProtoId(7) @JvmField val msgS2cCcAgentActionResultPush: S2CCcAgentActionResultPush? = null,
            @ProtoId(8) @JvmField val msgS2cCallRecordChangePush: S2CCallRecordChangePush? = null,
            @ProtoId(9) @JvmField val msgS2cSmsEventPush: S2CSMSEventPush? = null,
            @ProtoId(10) @JvmField val msgS2cAgentCallStatusEventPush: S2CAgentCallStatusEventPush? = null,
            @ProtoId(11) @JvmField val msgS2cUserGetCouponForKfextEventPush: S2CUserGetCouponForKFExtEventPush? = null,
            @ProtoId(12) @JvmField val msgS2cUserGetCouponForCEventPush: S2CUserGetCouponForCEventPush? = null
        ) : ProtoBuf {
            @Serializable
            internal class S2CAgentCallStatusEventPush(
                @ProtoId(1) @JvmField val type: Int = 0,
                @ProtoId(2) @JvmField val callStatus: Int = 0,
                @ProtoId(3) @JvmField val ringAsr: Int = 0,
                @ProtoId(4) @JvmField val callid: String = "",
                @ProtoId(5) @JvmField val fromKfext: Long = 0L,
                @ProtoId(6) @JvmField val timestamp: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCallRecordChangePush(
                @ProtoId(1) @JvmField val kfext: Long = 0L,
                @ProtoType(ProtoNumberType.FIXED) @ProtoId(2) @JvmField val fixed64Timestamp: Long = 0L
            ) : ProtoBuf

            @Serializable
            internal class S2CCcAgentActionResultPush(
                @ProtoId(1) @JvmField val type: Int = 0,
                @ProtoId(2) @JvmField val callid: String = "",
                @ProtoId(3) @JvmField val result: Int = 0,
                @ProtoId(4) @JvmField val timestamp: Int = 0,
                @ProtoId(5) @JvmField val status: Int = 0,
                @ProtoId(6) @JvmField val targetName: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(7) @JvmField val targetKfext: Long = 0L
            ) : ProtoBuf

            @Serializable
            internal class S2CCcAgentStatusChangePush(
                @ProtoId(1) @JvmField val readyDevice: Int = 0,
                @ProtoId(2) @JvmField val updateTime: Long = 0L,
                @ProtoId(3) @JvmField val deviceSubState: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCcConfigChangePush(
                @ProtoId(1) @JvmField val optype: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCcExceptionOccurPush(
                @ProtoId(1) @JvmField val optype: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCcTalkingStatusChangePush(
                @ProtoId(1) @JvmField val talkingStatus: Int = 0,
                @ProtoId(2) @JvmField val callid: String = ""
            ) : ProtoBuf

            @Serializable
            internal class S2CSMSEventPush(
                @ProtoId(1) @JvmField val type: Int = 0,
                @ProtoId(2) @JvmField val phoneNum: String = "",
                @ProtoId(3) @JvmField val timestamp: Long = 0L,
                @ProtoId(4) @JvmField val smsId: String = "",
                @ProtoId(5) @JvmField val eventMsg: String = ""
            ) : ProtoBuf

            @Serializable
            internal class S2CUserGetCouponForCEventPush(
                @ProtoId(1) @JvmField val qquin: Long = 0L,
                @ProtoId(2) @JvmField val kfuin: Long = 0L,
                @ProtoId(3) @JvmField val couponId: Long = 0L,
                @ProtoId(4) @JvmField val timestamp: Int = 0,
                @ProtoId(5) @JvmField val kfext: Long = 0L,
                @ProtoId(6) @JvmField val tipsContent: String = ""
            ) : ProtoBuf

            @Serializable
            internal class S2CUserGetCouponForKFExtEventPush(
                @ProtoId(1) @JvmField val channelType: Int = 0,
                @ProtoId(2) @JvmField val fakeuin: Long = 0L,
                @ProtoId(3) @JvmField val qquin: Long = 0L,
                @ProtoId(4) @JvmField val openid: String = "",
                @ProtoId(5) @JvmField val visitorid: String = "",
                @ProtoId(6) @JvmField val appid: String = "",
                @ProtoId(7) @JvmField val qqPubUin: Long = 0L,
                @ProtoId(8) @JvmField val kfuin: Long = 0L,
                @ProtoId(9) @JvmField val couponId: Long = 0L,
                @ProtoId(10) @JvmField val notifyTips: String = "",
                @ProtoId(11) @JvmField val timestamp: Int = 0,
                @ProtoId(12) @JvmField val kfext: Long = 0L
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0xe8 {
    internal class Submsgtype0xe8 : ProtoBuf {
        @Serializable
        internal class MsgBody/*(
             @ProtoId(1) @JvmField val msgItem: ApolloPushMsgInfo.STPushMsgElem? = null
        )*/ : ProtoBuf
    }
}


internal class Submsgtype0xe9 {
    internal class SubMsgType0xe9 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val businessType: Int = 0,
            @ProtoId(2) @JvmField val business: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xea {
    internal class Submsgtype0xea : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xee {
    internal class Submsgtype0xee : ProtoBuf {
        @Serializable
        internal class AccountInfo(
            @ProtoId(1) @JvmField val id: Long = 0L,
            @ProtoId(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val iconUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ContextInfo(
            @ProtoId(1) @JvmField val id: Long = 0L,
            @ProtoId(2) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val msgPicList: List<PictureInfo>? = null,
            @ProtoId(4) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val orangeWord: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val brief: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val enumContextType: Int /* enum */ = 0,
            @ProtoId(8) @JvmField val videoBrief: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ControlInfo(
            @ProtoId(1) @JvmField val commentLength: Int = 0,
            @ProtoId(2) @JvmField val showLine: Int = 0,
            @ProtoId(3) @JvmField val fontSize: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class ExtraInfo(
            @ProtoId(1) @JvmField val ext: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val id: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val seq: Long = 0L,
            @ProtoId(3) @JvmField val bid: Int = 0,
            @ProtoId(11) @JvmField val msgNotifyList: List<NotifyInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class NotifyInfo(
            @ProtoId(1) @JvmField val msgStyleSheet: StyleSheet? = null,
            @ProtoId(2) @JvmField val enumApppushType: Int /* enum */ = 0,
            @ProtoId(3) @JvmField val msgOrdinaryPushInfo: OrdinaryPushInfo? = null,
            @ProtoId(4) @JvmField val msgSocialPushInfo: SocialPushInfo? = null,
            @ProtoId(5) @JvmField val msgUgcPushInfo: UGCPushInfo? = null,
            @ProtoId(11) @JvmField val msgContextInfo: ContextInfo? = null,
            @ProtoId(12) @JvmField val msgAccountInfo: AccountInfo? = null,
            @ProtoId(13) @JvmField val msgStatisticsInfo: StatisticsInfo? = null,
            @ProtoId(14) @JvmField val msgControlInfo: ControlInfo? = null,
            @ProtoId(21) @JvmField val msgExtraInfo: ExtraInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class OrangeControlInfo(
            @ProtoId(1) @JvmField val color: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val fontSize: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OrdinaryPushInfo(
            @ProtoId(1) @JvmField val msgLabelControlInfo: OrangeControlInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class PictureInfo(
            @ProtoId(1) @JvmField val url: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class SocialPushInfo(
            @ProtoId(1) @JvmField val feedsId: Long = 0L,
            @ProtoId(2) @JvmField val biuReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val biuComment: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class StatisticsInfo(
            @ProtoId(1) @JvmField val algorithmId: Long = 0L,
            @ProtoId(2) @JvmField val strategyId: Long = 0L,
            @ProtoId(3) @JvmField val folderStatus: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class StyleSheet(
            @ProtoId(1) @JvmField val enumStyleType: Int /* enum */ = 0,
            @ProtoId(2) @JvmField val arkEnable: Int = 0,
            @ProtoId(3) @JvmField val scene: Long = 0L,
            @ProtoId(11) @JvmField val duration: Int = 0,
            @ProtoId(12) @JvmField val endTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class UGCPushInfo(
            @ProtoId(1) @JvmField val feedsId: Long = 0L,
            @ProtoId(2) @JvmField val ugcReason: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xf9 {
    internal class Submsgtype0xf9 : ProtoBuf {
        @Serializable
        internal class AdInfo(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val nick: String = "",
            @ProtoId(3) @JvmField val headUrl: String = "",
            @ProtoId(4) @JvmField val brief: String = "",
            @ProtoId(5) @JvmField val action: String = "",
            @ProtoId(6) @JvmField val flag: Int = 0,
            @ProtoId(7) @JvmField val serviceID: Int = 0,
            @ProtoId(8) @JvmField val templateID: Int = 0,
            @ProtoId(9) @JvmField val url: String = "",
            @ProtoId(10) @JvmField val msgMsgCommonData: MsgCommonData? = null,
            @ProtoId(11) @JvmField val msgVideo: List<Video>? = null,
            @ProtoId(12) @JvmField val pushTime: Int = 0,
            @ProtoId(13) @JvmField val invalidTime: Int = 0,
            @ProtoId(14) @JvmField val maxExposureTime: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val zipAdInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgCommonData(
            @ProtoId(1) @JvmField val adId: String = "",
            @ProtoId(2) @JvmField val adPosId: String = "",
            @ProtoId(3) @JvmField val boolBannerShow: Boolean = false,
            @ProtoId(4) @JvmField val bannertype: Int = 0,
            @ProtoId(5) @JvmField val jumpType: Int = 0,
            @ProtoId(6) @JvmField val jumpUrl: String = "",
            @ProtoId(7) @JvmField val appId: String = "",
            @ProtoId(8) @JvmField val appName: String = "",
            @ProtoId(9) @JvmField val packagename: String = "",
            @ProtoId(10) @JvmField val androidDownloadUrl: String = "",
            @ProtoId(11) @JvmField val scheme: String = "",
            @ProtoId(12) @JvmField val iosDownloadUrl: String = "",
            @ProtoId(13) @JvmField val bannerImgUrl: String = "",
            @ProtoId(14) @JvmField val bannerText: String = "",
            @ProtoId(15) @JvmField val bannerButtonText: String = "",
            @ProtoId(16) @JvmField val boolSilentDownload: Boolean = false,
            @ProtoId(17) @JvmField val audioSwitchType: Int = 0,
            @ProtoId(18) @JvmField val preDownloadType: Int = 0,
            @ProtoId(19) @JvmField val reportLink: String = "",
            @ProtoId(20) @JvmField val boolHorizontalVideo: Boolean = false,
            @ProtoId(21) @JvmField val audioFadeinDuration: Int = 0,
            @ProtoId(22) @JvmField val openJumpUrlGuide: String = "",
            @ProtoId(23) @JvmField val myappDownloadUrl: String = "",
            @ProtoId(24) @JvmField val jumpTypeParams: String = "",
            @ProtoId(25) @JvmField val scrollUpToJump: Int = 0,
            @ProtoId(26) @JvmField val controlVariable: Int = 0,
            @ProtoId(27) @JvmField val autoJump: Int = 0,
            @ProtoId(28) @JvmField val clickLink: String = "",
            @ProtoId(29) @JvmField val monitorType: Int = 0,
            @ProtoId(30) @JvmField val shareNick: String = "",
            @ProtoId(31) @JvmField val shareAdHeadUrl: String = "",
            @ProtoId(32) @JvmField val shareAdBrief: String = "",
            @ProtoId(33) @JvmField val shareAdTxt: String = "",
            @ProtoId(34) @JvmField val shareAdIconUrl: String = "",
            @ProtoId(35) @JvmField val shareJumpUrl: String = "",
            @ProtoId(36) @JvmField val controlPluginTime: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Video(
            @ProtoId(1) @JvmField val layout: Int = 0,
            @ProtoId(2) @JvmField val cover: String = "",
            @ProtoId(3) @JvmField val src: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xfd {
    internal class Submsgtype0xfd : ProtoBuf {
        @Serializable
        internal class AdInfo(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val adId: String = "",
            @ProtoId(3) @JvmField val adSeq: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val msgAdInfo: AdInfo? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xfe {
    internal class Submsgtype0xfe : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val innerUnreadNum: Int = 0,
            @ProtoId(3) @JvmField val boxUnreadNum: Int = 0,
            @ProtoId(4) @JvmField val updateTime: Int = 0
        ) : ProtoBuf
    }
}
        