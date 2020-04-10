/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.qqandroid.network.protocol.data.proto.onlinePush0x210

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.AppointDefine
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
internal class SubMsgType0x43 : ProtoBuf {
    @Serializable
    internal class UpdateTips(
        @ProtoId(1) val desc: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

internal class Submsgtype0x101 {
    @Serializable
    class SubMsgType0x27 : ProtoBuf {
        @Serializable
        internal class ClientReport(
            @ProtoId(1) val serviceId: Int = 0,
            @ProtoId(2) val contentId: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgPushPlatform: PushPlatform? = null,
            @ProtoId(2) val msgClientReport: ClientReport? = null
        ) : ProtoBuf

        @Serializable
        internal class PushPlatform(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val title: String = "",
            @ProtoId(3) val desc: String = "",
            @ProtoId(4) val targetUrl: String = "",
            @ProtoId(5) val forwardType: Int = 0,
            @ProtoId(6) val extDataString: String = "",
            @ProtoId(7) val extData: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x102 {
    @Serializable
    class Submsgtype0x102 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val adId: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x103 {
    @Serializable
    class Submsgtype0x103 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val from: Long = 0L,
            @ProtoId(2) val to: Long = 0L,
            @ProtoId(3) val topicId: Int = 0,
            @ProtoId(11) val curCount: Int = 0,
            @ProtoId(12) val totalCount: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x104 {
    @Serializable
    class Submsgtype0x104 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val from: Long = 0L,
            @ProtoId(2) val to: Long = 0L,
            @ProtoId(3) val topicId: Int = 0,
            @ProtoId(11) val wording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x108 {
    @Serializable
    class SubMsgType0x108 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val pushUin: Long = 0L,
            @ProtoId(3) val likeCount: Int = 0,
            @ProtoId(4) val pushTime: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x10f {
    @Serializable
    class Submsgtype0x10f : ProtoBuf {
        @Serializable
        internal class KanDianCoinSettingWording(
            @ProtoId(1) val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val pictureUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val isOpenCoinEntry: Int = 0,
            @ProtoId(2) val canGetCoinCount: Int = 0,
            @ProtoId(3) val coinIconUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val msgSettingWording: KanDianCoinSettingWording? = null,
            @ProtoId(5) val lastCompletedTaskStamp: Long = 0L,
            @ProtoId(6) val dstUin: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x111 {
    @Serializable
    class SubMsgType0x111 : ProtoBuf {
        @Serializable
        internal class AddFriendSource(
            @ProtoId(1) val source: Int = 0,
            @ProtoId(2) val subSource: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MayKnowPerson(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val msgIosSource: AddFriendSource? = null,
            @ProtoId(3) val msgAndroidSource: AddFriendSource? = null,
            @ProtoId(4) val reason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val additive: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val nick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val remark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val country: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val province: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val city: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(11) val age: Int = 0,
            @ProtoId(12) val catelogue: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) val alghrithm: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(14) val richbuffer: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(15) val qzone: Int = 0,
            @ProtoId(16) val gender: Int = 0,
            @ProtoId(17) val mobileName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(18) val token: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Long = 0L,
            @ProtoId(2) val msgAddRecommendPersons: List<MayKnowPerson>? = null,
            @ProtoId(3) val uint64DelUins: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x113 {
    @Serializable
    class SubMsgType0x113 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val int32AppId: Int = 0,
            @ProtoId(2) val int32TaskId: Int = 0,
            @ProtoId(3) val enumTaskOp: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x115 {
    @Serializable
    class SubMsgType0x115 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val toUin: Long = 0L,
            @ProtoId(3) val msgNotifyItem: NotifyItem? = null,
            @ProtoId(4) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NotifyItem(
            @ProtoId(1) val ime: Int = 0,
            @ProtoId(2) val timeoutS: Int = 0,
            @ProtoId(3) val timestamp: Long = 0L,
            @ProtoId(4) val eventType: Int = 0,
            @ProtoId(5) val interval: Int = 0,
            @ProtoId(6) val wording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x116 {
    @Serializable
    class Submsgtype0x116 : ProtoBuf {
        @Serializable
        internal class MemberInfo(
            @ProtoId(1) val memberUin: Long = 0L,
            @ProtoId(2) val inviteTimestamp: Int = 0,
            @ProtoId(3) val terminalType: Int = 0,
            @ProtoId(4) val clientVersion: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgMemberJoin: List<MemberInfo>? = null,
            @ProtoId(2) val msgMemberQuit: List<MemberInfo>? = null,
            @ProtoId(3) val groupId: Int = 0,
            @ProtoId(4) val roomId: Int = 0,
            @ProtoId(5) val inviteListTotalCount: Int = 0,
            @ProtoId(6) val enumEventType: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x117 {
    @Serializable
    class Submsgtype0x117 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val uint32MoudleId: List<Int>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x118 {
    @Serializable
    class Submsgtype0x118 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val pushType: Int = 0,
            @ProtoId(2) val pushData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val timestamp: Int = 0,
            @ProtoId(4) val msgSystemNotify: SystemNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class SystemNotify(
            @ProtoId(1) val msgSummary: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val filterFlag: Int = 0,
            @ProtoId(3) val extendContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val ignorePcActive: Int = 0,
            @ProtoId(5) val filterVersion: Int = 0,
            @ProtoId(6) val countFlag: Int = 0,
            @ProtoId(7) val filterVersionUpperlimitFlag: Int = 0,
            @ProtoId(8) val filterVersionUpperlimit: Int = 0,
            @ProtoId(9) val customSound: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val admnFlag: Int = 0,
            @ProtoId(11) val ignoreWithoutContent: Int = 0,
            @ProtoId(12) val msgTitle: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x119 {
    @Serializable
    class SubMsgType0x119 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val writerUin: Long = 0L,
            @ProtoId(2) val creatorUin: Long = 0L,
            @ProtoId(3) val richContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val optBytesUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val creatorNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11a {
    @Serializable
    class Submsgtype0x11a : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val enumResult: Int /* enum */ = 0,
            @ProtoId(2) val token: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val msgUserData: UserData? = null,
            @ProtoId(5) val enumBizType: Int /* enum */ = 1
        ) : ProtoBuf

        @Serializable
        internal class UserData(
            @ProtoId(1) val ip: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val fixed32Port: List<Int>? = null,
            @ProtoId(3) val ssid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val bssid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val enumPlatform: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11b {
    @Serializable
    class Submsgtype0x11b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val qrSig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val enumBizType: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11c {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) val cmd: Int = 0,
        @ProtoId(2) val timestamp: Int = 0,
        @ProtoId(3) val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


internal class Submsgtype0x11e {
    @Serializable
    class SubMsgType0x11e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val reason: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x11f {
    @Serializable
    class SubMsgType0x11f : ProtoBuf {
        @Serializable
        internal class MediaUserInfo(
            @ProtoId(1) val toUin: Long = 0L,
            @ProtoId(2) val joinState: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val aioType: Int = 0,
            @ProtoId(5) val operUin: Long = 0L,
            @ProtoId(6) val uint64ToUin: List<Long>? = null,
            @ProtoId(7) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val msgSeq: Long = 0L,
            @ProtoId(9) val msgMediaUin: List<MediaUserInfo>? = null,
            @ProtoId(10) val msgPerSetting: PersonalSetting? = null,
            @ProtoId(11) val playMode: Int = 0,
            @ProtoId(99) val mediaType: Int = 0,
            @ProtoId(100) val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PersonalSetting(
            @ProtoId(1) val themeId: Int = 0,
            @ProtoId(2) val playerId: Int = 0,
            @ProtoId(3) val fontId: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x120 {
    @Serializable
    class SubMsgType0x120 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val srcAppId: Int = 0,
            @ProtoId(2) val noticeType: Int = 0,
            @ProtoId(3) val reserve1: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val reserve2: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val reserve3: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val noticeTime: Int = 0,
            @ProtoId(7) val frdUin: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x122 {
    @Serializable
    class GrayTipsResv : ProtoBuf {
        @Serializable
        internal class ResvAttr(
            @ProtoId(1) val friendBannedFlag: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    class Submsgtype0x122 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val busiType: Long = 0L,
            @ProtoId(2) val busiId: Long = 0L,
            @ProtoId(3) val ctrlFlag: Int = 0,
            @ProtoId(4) val c2cType: Int = 0,
            @ProtoId(5) val serviceType: Int = 0,
            @ProtoId(6) val templId: Long = 0L,
            @ProtoId(7) val msgTemplParam: List<TemplParam>? = null,
            @ProtoId(8) val content: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val tipsSeqId: Long = 0L,
            @ProtoId(100) val pbReserv: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class TemplParam(
            @ProtoId(1) val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x123 {
    @Serializable
    class Submsgtype0x123 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val busiType: Long = 0L,
            @ProtoId(2) val busiId: Long = 0L,
            @ProtoId(3) val ctrlFlag: Int = 0,
            @ProtoId(4) val c2cType: Int = 0,
            @ProtoId(5) val serviceType: Int = 0,
            @ProtoId(6) val templId: Long = 0L,
            @ProtoId(7) val templParam: List<TemplParam>? = null,
            @ProtoId(8) val templContent: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class TemplParam(
            @ProtoId(1) val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x125 {
    @Serializable
    class Submsgtype0x125 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val operUin: Long = 0L,
            @ProtoId(5) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val msgSeq: Long = 0L,
            @ProtoId(99) val pushType: Int = 0,
            @ProtoId(100) val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x126 {
    @Serializable
    class Submsgtype0x126 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgSeq: Long = 0L,
            @ProtoId(2) val msgType: Int = 0,
            @ProtoId(3) val msgInfo: String = "",
            @ProtoId(100) val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x127 {
    @Serializable
    class Submsgtype0x127 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val seq: Long = 0L,
            @ProtoId(2) val actionType: Int = 0,
            @ProtoId(3) val friendUin: Long = 0L,
            @ProtoId(4) val operUin: Long = 0L,
            @ProtoId(5) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val joinState: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x128 {
    @Serializable
    class Submsgtype0x128 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val sig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val matchUin: Long = 0L,
            @ProtoId(3) val tipsWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val nick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val timeStamp: Long = 0L,
            @ProtoId(6) val matchExpiredTime: Int = 0,
            @ProtoId(7) val reportId: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x129 {
    @Serializable
    class Submsgtype0x129 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val seq: Long = 0L,
            @ProtoId(2) val actionType: Int = 0,
            @ProtoId(3) val friendUin: Long = 0L,
            @ProtoId(4) val operUin: Long = 0L,
            @ProtoId(5) val grayTips: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val joinState: Int /* enum */ = 1
        ) : ProtoBuf
    }
}


internal class Submsgtype0x1a {
    @Serializable
    class SubMsgType0x1a : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val fromUinInt: Int = 0,
            @ProtoId(3) val toUinInt: Int = 0,
            @ProtoId(4) val status: Int = 0,
            @ProtoId(5) val ttl: Int = 0,
            @ProtoId(6) val desc: String = "",
            @ProtoId(7) val type: Int = 0,
            @ProtoId(8) val captureTimes: Int = 0,
            @ProtoId(9) val fromUin: Long = 0L,
            @ProtoId(10) val toUin: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x26 {
    @Serializable
    class Submsgtype0x26 : ProtoBuf {
        @Serializable
        internal class AppID(
            @ProtoId(1) val appId: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class AppNotifyContent(
            @ProtoId(1) val text: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val optMsgAppNotifyUser: List<AppNotifyUser>? = null
        ) : ProtoBuf

        @Serializable
        internal class AppNotifyUser(
            @ProtoId(1) val optUint64Uin: Long = 0L,
            @ProtoId(2) val optUint32Flag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class AppTip(
            @ProtoId(1) val tipInfoSeq: Int = 0,
            @ProtoId(2) val icon: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val iconTimeStamp: Int = 0,
            @ProtoId(4) val tooltip: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val reportidClick: Int = 0,
            @ProtoId(6) val reportidShow: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class AppTipNotify(
            @ProtoId(1) val msgAppTip: AppTip? = null,
            @ProtoId(2) val action: Int = 0,
            @ProtoId(3) val text: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val notifySeq: Int = 0,
            @ProtoId(5) val neededTipInfoSeq: Int = 0,
            @ProtoId(6) val optMsgAppNotifyContent: AppNotifyContent? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val msgSubcmd0x1PushBody: List<SubCmd0x1UpdateAppUnreadNum>? = null,
            @ProtoId(3) val msgSubcmd0x2PushBody: SubCmd0x2UpdateAppList? = null,
            @ProtoId(4) val msgSubcmd0x3PushBody: SubCmd0x3UpdateDiscussAppInfo? = null,
            @ProtoId(5) val msgSubcmd0x4PushBody: SubCmd0x4UpdateApp? = null
        ) : ProtoBuf {
            @Serializable
            internal class SubCmd0x1UpdateAppUnreadNum(
                @ProtoId(1) val msgAppId: AppID? = null,
                @ProtoId(2) val groupCode: Long = 0L,
                @ProtoType(ProtoNumberType.SIGNED) @ProtoId(3) val sint32UnreadNum: Int = 0,
                @ProtoId(4) val msgAppTipNotify: AppTipNotify? = null,
                @ProtoType(ProtoNumberType.SIGNED) @ProtoId(5) val sint32AlbumCnt: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class SubCmd0x2UpdateAppList(
                @ProtoId(1) val msgAppId: List<AppID>? = null,
                @ProtoId(2) val uint32TimeStamp: List<Int>? = null,
                @ProtoId(3) val groupCode: Long = 0L
            ) : ProtoBuf

            @Serializable
            internal class SubCmd0x3UpdateDiscussAppInfo(
                @ProtoId(1) val msgAppId: AppID? = null,
                @ProtoId(2) val confUin: Long = 0L,
                @ProtoId(3) val msgAppTipNotify: AppTipNotify? = null
            ) : ProtoBuf

            @Serializable
            internal class SubCmd0x4UpdateApp(
                @ProtoId(1) val msgAppId: AppID? = null,
                @ProtoId(2) val groupCode: Long = 0L,
                @ProtoType(ProtoNumberType.SIGNED) @ProtoId(3) val sint32UnreadNum: Int = 0
            ) : ProtoBuf
        }

        @Serializable
        internal class TransferCnt(
            @ProtoId(1) val chainId: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x27 {
    @Serializable
    class SubMsgType0x27 : ProtoBuf {
        @Serializable
        internal class AddGroup(
            @ProtoId(1) val groupid: Int = 0,
            @ProtoId(2) val sortid: Int = 0,
            @ProtoId(3) val groupname: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class AppointmentNotify(
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
        internal class BinaryMsg(
            @ProtoId(1) val opType: Int = 0,
            @ProtoId(2) val opValue: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ChatMatchInfo(
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
        internal class ConfMsgRoamFlag(
            @ProtoId(1) val confid: Long = 0L,
            @ProtoId(2) val flag: Int = 0,
            @ProtoId(3) val timestamp: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class DaRenNotify(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val loginDays: Int = 0,
            @ProtoId(3) val days: Int = 0,
            @ProtoId(4) val isYestodayLogin: Int = 0,
            @ProtoId(5) val isTodayLogin: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DelFriend(
            @ProtoId(1) val uint64Uins: List<Long>? = null
        ) : ProtoBuf

        @Serializable
        internal class DelGroup(
            @ProtoId(1) val groupid: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class FanpaiziNotify(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val fromNick: String = "",
            @ProtoId(3) val tipsContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ForwardBody(
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
        internal class FrdCustomOnlineStatusChange(
            @ProtoId(1) val uin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class FriendGroup(
            @ProtoId(1) val fuin: Long = 0L,
            @ProtoId(2) val uint32OldGroupId: List<Int>? = null,
            @ProtoId(3) val uint32NewGroupId: List<Int>? = null
        ) : ProtoBuf

        @Serializable
        internal class FriendRemark(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val fuin: Long = 0L,
            @ProtoId(3) val rmkName: String = "",
            @ProtoId(4) val groupCode: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class GPS(
            @ProtoId(1) val int32Lat: Int = 900000000,
            @ProtoId(2) val int32Lon: Int = 900000000,
            @ProtoId(3) val int32Alt: Int = -10000000,
            @ProtoId(4) val int32Type: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GroupMemberProfileInfo(
            @ProtoId(1) val field: Int = 0,
            @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class GroupProfileInfo(
            @ProtoId(1) val field: Int = 0,
            @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class GroupSort(
            @ProtoId(1) val groupid: Int = 0,
            @ProtoId(2) val sortid: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GrpMsgRoamFlag(
            @ProtoId(1) val groupcode: Long = 0L,
            @ProtoId(2) val flag: Int = 0,
            @ProtoId(3) val timestamp: Long = 0L
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
            @ProtoId(17) val lastQzoneTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModConfProfile(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val confUin: Int = 0,
            @ProtoId(3) val msgProfileInfos: List<ProfileInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModCustomFace(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val uin: Long = 0L,
            @ProtoId(3) val groupCode: Long = 0L,
            @ProtoId(4) val cmdUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModFrdRoamPriv(
            @ProtoId(1) val msgRoamPriv: List<OneRoamPriv>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModFriendGroup(
            @ProtoId(1) val msgFrdGroup: List<FriendGroup>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModFriendRemark(
            @ProtoId(1) val msgFrdRmk: List<FriendRemark>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModGroupMemberProfile(
            @ProtoId(1) val groupUin: Long = 0L,
            @ProtoId(2) val uin: Long = 0L,
            @ProtoId(3) val msgGroupMemberProfileInfos: List<GroupMemberProfileInfo>? = null,
            @ProtoId(4) val groupCode: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModGroupName(
            @ProtoId(1) val groupid: Int = 0,
            @ProtoId(2) val groupname: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ModGroupProfile(
            @ProtoId(1) val groupUin: Long = 0L,
            @ProtoId(2) val msgGroupProfileInfos: List<GroupProfileInfo>? = null,
            @ProtoId(3) val groupCode: Long = 0L,
            @ProtoId(4) val cmdUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ModGroupSort(
            @ProtoId(1) val msgGroupsort: List<GroupSort>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModLongNick(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ModProfile(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val msgProfileInfos: List<ProfileInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class ModSnsGeneralInfo(
            @ProtoId(1) val msgSnsGeneralInfos: List<SnsUpateBuffer>? = null
        ) : ProtoBuf

        @Serializable
        internal class MQQCampusNotify(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val wording: String = "",
            @ProtoId(3) val target: String = "",
            @ProtoId(4) val type: Int = 0,
            @ProtoId(5) val source: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgModInfos: List<ForwardBody> = listOf()
        ) : ProtoBuf

        @Serializable
        internal class NewComeinUser(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val isFrd: Int = 0,
            @ProtoId(3) val remark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val nick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NewComeinUserNotify(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val boolStrongNotify: Boolean = false,
            @ProtoId(3) val pushTime: Int = 0,
            @ProtoId(4) val msgNewComeinUser: NewComeinUser? = null,
            @ProtoId(5) val msgNewGroup: NewGroup? = null,
            @ProtoId(6) val msgNewGroupUser: NewGroupUser? = null
        ) : ProtoBuf

        @Serializable
        internal class NewGroup(
            @ProtoId(1) val groupCode: Long = 0L,
            @ProtoId(2) val groupName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val ownerUin: Long = 0L,
            @ProtoId(4) val ownerNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val distance: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NewGroupUser(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val int32Sex: Int = 0,
            @ProtoId(3) val int32Age: Int = 0,
            @ProtoId(4) val nick: String = "",
            @ProtoId(5) val distance: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class OneRoamPriv(
            @ProtoId(1) val fuin: Long = 0L,
            @ProtoId(2) val privTag: Int = 0,
            @ProtoId(3) val privValue: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class PraiseRankNotify(
            @ProtoId(11) val isChampion: Int = 0,
            @ProtoId(12) val rankNum: Int = 0,
            @ProtoId(13) val msg: String = ""
        ) : ProtoBuf

        @Serializable
        internal class ProfileInfo(
            @ProtoId(1) val field: Int = 0,
            @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushReportDev(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(4) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val reportMaxNum: Int = 200,
            @ProtoId(6) val sn: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushSearchDev(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgGpsInfo: GPS? = null,
            @ProtoId(3) val devTime: Int = 0,
            @ProtoId(4) val pushTime: Int = 0,
            @ProtoId(5) val din: Long = 0L,
            @ProtoId(6) val data: String = ""
        ) : ProtoBuf

        @Serializable
        internal class QQPayPush(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val boolPayOk: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class SnsUpateBuffer(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val code: Long = 0L,
            @ProtoId(3) val result: Int = 0,
            @ProtoId(400) val msgSnsUpdateItem: List<SnsUpdateItem>? = null,
            @ProtoId(401) val uint32Idlist: List<Int>? = null
        ) : ProtoBuf

        @Serializable
        internal class SnsUpdateFlag(
            @ProtoId(1) val msgUpdateSnsFlag: List<SnsUpdateOneFlag>? = null
        ) : ProtoBuf

        @Serializable
        internal class SnsUpdateItem(
            @ProtoId(1) val updateSnsType: Int = 0,
            @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class SnsUpdateOneFlag(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val id: Long = 0L,
            @ProtoId(3) val flag: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x28 {
    @Serializable
    class SubMsgType0x28 : ProtoBuf {
        @Serializable
        internal class FollowList(
            @ProtoId(1) val puin: Long = 0L,
            @ProtoId(2) val uin: Long = 0L,
            @ProtoId(3) val type: Int = 0,
            @ProtoId(4) val seqno: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val msgRspFollowlist: RspFollowList? = null,
            @ProtoId(3) val msgRspTypelist: RspTypeList? = null
        ) : ProtoBuf

        @Serializable
        internal class RspFollowList(
            @ProtoId(1) val msgFollowlist: List<FollowList>? = null
        ) : ProtoBuf

        @Serializable
        internal class RspTypeList(
            @ProtoId(1) val msgTypelist: List<TypeList>? = null
        ) : ProtoBuf

        @Serializable
        internal class TypeList(
            @ProtoId(1) val puin: Long = 0L,
            @ProtoId(2) val flag: Int = 0,
            @ProtoId(3) val type: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x30 {
    @Serializable
    class SubMsgType0x30 : ProtoBuf {
        @Serializable
        internal class BlockListNotify(
            @ProtoId(1) val msgBlockUinInfo: List<BlockUinInfo>? = null,
            @ProtoId(2) val uint64DelUin: List<Long>? = null
        ) : ProtoBuf

        @Serializable
        internal class BlockUinInfo(
            @ProtoId(1) val blockUin: Long = 0L,
            @ProtoId(2) val sourceId: Int = 0,
            @ProtoId(3) val sourceSubId: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val msgS2cBlocklistNotify: BlockListNotify? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x31 {
    @Serializable
    class Submsgtype0x31 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val flag: Int = 0,
            @ProtoId(2) val uin: Long = 0L,
            @ProtoId(3) val bindUin: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(4) val time: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x35 {
    @Serializable
    class Submsgtype0x35 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val bubbleTimestamp: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3b {
    @Serializable
    class Submsgtype0x3b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val groupCode: Long = 0L,
            @ProtoId(3) val userShowFlag: Int = 0,
            @ProtoId(4) val memberLevelChanged: Int = 0,
            @ProtoId(5) val officemode: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3d {
    @Serializable
    class SttResultPush : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val msgPttResp: TransPttResp? = null
        ) : ProtoBuf

        @Serializable
        internal class TransPttResp(
            @ProtoId(1) val sessionid: Long = 0L,
            @ProtoId(2) val pttType: Int = 0,
            @ProtoId(3) val errorCode: Int = 0,
            @ProtoId(4) val totalLen: Int = 0,
            @ProtoId(5) val seq: Int = 0,
            @ProtoId(6) val pos: Int = 0,
            @ProtoId(7) val len: Int = 0,
            @ProtoId(8) val text: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val senderUin: Long = 0L,
            @ProtoId(10) val receiverUin: Long = 0L,
            @ProtoId(11) val fileID: Int = 0,
            @ProtoId(12) val filemd5: String = "",
            @ProtoId(13) val filePath: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3e {
    @Serializable
    class Submsgtype0x3e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subcmd: Int = 0,
            @ProtoId(2) val random: Int = 0,
            @ProtoId(3) val result: Int = 0,
            @ProtoId(4) val device: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val sid: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x3f {
    @Serializable
    class SubMsgType0x3f : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgPubunikey: List<PubUniKey>? = null
        ) : ProtoBuf

        @Serializable
        internal class PubUniKey(
            @ProtoId(1) val fromPubUin: Long = 0L,
            @ProtoId(2) val qwMsgId: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x40 {
    @Serializable
    class SubMsgType0x40 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val vUuid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val fromUin: Long = 0L,
            @ProtoId(3) val toUin: Long = 0L,
            @ProtoId(4) val state: Int = 0,
            @ProtoId(11) val opertype: Int = 0,
            @ProtoId(12) val fromphonenum: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x41 {
    @Serializable
    class MsgType0x210SubMsgType0x41 : ProtoBuf {
        @Serializable
        internal class GameRsultMsg(
            @ProtoId(1) val gameName: String = "",
            @ProtoId(2) val gamePic: String = "",
            @ProtoId(3) val moreInfo: String = "",
            @ProtoId(4) val msgGameRsts: List<UinResult>? = null,
            @ProtoId(5) val gameSubheading: String = "",
            @ProtoId(6) val uin: Long = 0L,
            @ProtoId(7) val nickname: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class UinResult(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val grade: Int = 0,
            @ProtoId(4) val score: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x42 {
    @Serializable
    class Submsgtype0x42 : ProtoBuf {
        @Serializable
        internal class GameStatusSync(
            @ProtoId(1) val gameAppid: Int = 0,
            @ProtoId(2) val data: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x44 {
    @Serializable
    class Submsgtype0x44 : ProtoBuf {
        @Serializable
        internal class ClearCountMsg(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val time: Int = 0,
            @ProtoId(3) val processflag: Int = 0,
            @ProtoId(4) val updateflag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class FriendSyncMsg(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val fuin: Long = 0L,
            @ProtoId(3) val processtype: Int = 0,
            @ProtoId(4) val time: Int = 0,
            @ProtoId(5) val processflag: Int = 0,
            @ProtoId(6) val sourceid: Int = 0,
            @ProtoId(7) val sourcesubid: Int = 0,
            @ProtoId(8) val strWording: List<String> = listOf()
        ) : ProtoBuf

        @Serializable
        internal class GroupSyncMsg(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgSeq: Long = 0L,
            @ProtoId(3) val grpCode: Long = 0L,
            @ProtoId(4) val gaCode: Long = 0L,
            @ProtoId(5) val optUin1: Long = 0L,
            @ProtoId(6) val optUin2: Long = 0L,
            @ProtoId(7) val msgBuf: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val authKey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val msgStatus: Int = 0,
            @ProtoId(10) val actionUin: Long = 0L,
            @ProtoId(11) val actionTime: Long = 0L,
            @ProtoId(12) val curMaxMemCount: Int = 0,
            @ProtoId(13) val nextMaxMemCount: Int = 0,
            @ProtoId(14) val curMemCount: Int = 0,
            @ProtoId(15) val reqSrcId: Int = 0,
            @ProtoId(16) val reqSrcSubId: Int = 0,
            @ProtoId(17) val inviterRole: Int = 0,
            @ProtoId(18) val extAdminNum: Int = 0,
            @ProtoId(19) val processflag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class ModifySyncMsg(
            @ProtoId(1) val time: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgFriendMsgSync: FriendSyncMsg? = null,
            @ProtoId(2) val msgGroupMsgSync: GroupSyncMsg? = null,
            @ProtoId(3) val msgCleanCountMsg: ClearCountMsg? = null,
            @ProtoId(4) val msgModifyMsgSync: ModifySyncMsg? = null,
            @ProtoId(5) val msgWaitingMsgSync: WaitingSyncMsg? = null
        ) : ProtoBuf

        @Serializable
        internal class WaitingSyncMsg(
            @ProtoId(1) val time: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x48 {
    @Serializable
    internal class RecommendDeviceLock(
        @ProtoId(1) val canCancel: Boolean = false,
        @ProtoId(2) val wording: String = "",
        @ProtoId(3) val title: String = "",
        @ProtoId(4) val secondTitle: String = "",
        @ProtoId(5) val thirdTitle: String = "",
        @ProtoId(6) val wordingList: List<String> = listOf()
    ) : ProtoBuf
}


internal class Submsgtype0x4a {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) val secCmd: Int = 0,
        @ProtoId(2) val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


internal class Submsgtype0x4b {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) val albumid: String = "",
        @ProtoId(2) val coverUrl: String = "",
        @ProtoId(3) val albumName: String = "",
        @ProtoId(4) val opuin: Long = 0L,
        @ProtoType(ProtoNumberType.FIXED) @ProtoId(5) val time: Int = 0,
        @ProtoId(6) val picCnt: Int = 0,
        @ProtoId(7) val pushMsgHelper: String = "",
        @ProtoId(8) val pushMsgAlbum: String = "",
        @ProtoId(9) val usrTotal: Int = 0,
        @ProtoId(10) val uint64User: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    class Submsgtype0x4b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val albumid: String = "",
            @ProtoId(2) val coverUrl: String = "",
            @ProtoId(3) val albumName: String = "",
            @ProtoId(4) val opuin: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(5) val time: Int = 0,
            @ProtoId(6) val picCnt: Int = 0,
            @ProtoId(7) val pushMsgHelper: String = "",
            @ProtoId(8) val pushMsgAlbum: String = "",
            @ProtoId(9) val usrTotal: Int = 0,
            @ProtoId(10) val uint64User: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x4e {
    @Serializable
    class Submsgtype0x4e : ProtoBuf {
        @Serializable
        internal class GroupBulletin(
            @ProtoId(1) val msgContent: List<Content>? = null
        ) : ProtoBuf {
            @Serializable
            internal class Content(
                @ProtoId(1) val feedid: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(2) val uin: Long = 0L,
                @ProtoType(ProtoNumberType.FIXED) @ProtoId(3) val time: Int = 0
            ) : ProtoBuf
        }

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val groupId: Long = 0L,
            @ProtoId(2) val groupCode: Long = 0L,
            @ProtoId(3) val appid: Int = 0,
            @ProtoId(4) val msgGroupBulletin: GroupBulletin? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x54 {
    @Serializable
    class Submsgtype0x54 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val peerType: Int /* enum */ = 1,
            @ProtoId(2) val peerUin: Long = 0L,
            @ProtoId(3) val taskList: List<TaskInfo>? = null
        ) : ProtoBuf {
            @Serializable
            internal class TaskInfo(
                @ProtoId(1) val taskId: Int = 0
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0x60 {
    @Serializable
    class SubMsgType0x60 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val pushcmd: Int = 0,
            @ProtoId(2) val int64Ts: Long = 0L,
            @ProtoId(3) val ssid: String = "",
            @ProtoId(4) val content: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x63 {
    @Serializable
    class Submsgtype0x63 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val roomid: Long = 0L,
            @ProtoId(2) val seq: Int = 0,
            @ProtoId(3) val url: String = "",
            @ProtoId(4) val data: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x65 {
    @Serializable
    class SubMsgType0x65 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val cmd: Int = 0,
            @ProtoId(2) val msgExpiredPkg: MsgExpiredPkg? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgExpiredPkg(
            @ProtoId(1) val platform: Int = 0,
            @ProtoId(2) val expirePkg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val predownPkg: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x66 {
    @Serializable
    class Submsgtype0x66 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val pushData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val timestamp: Int = 0,
            @ProtoId(4) val notifyText: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val pushFlag: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x67 {
    @Serializable
    class Submsgtype0x67 : ProtoBuf {
        @Serializable
        internal class GroupInfo(
            @ProtoId(1) val groupCode: Long = 0L,
            @ProtoId(2) val groupName: String = "",
            @ProtoId(3) val groupMemo: String = "",
            @ProtoId(4) val memberNum: Int = 0,
            @ProtoId(5) val groupType: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgGrpinfo: List<GroupInfo>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x69 {
    @Serializable
    internal class Submsgtype0x69(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val boolDisplayReddot: Boolean = false,
        @ProtoId(3) val number: Int = 0,
        @ProtoId(4) val reason: Int = 0,
        @ProtoId(5) val lastTime: Int = 0,
        @ProtoId(6) val cmdUin: Long = 0L,
        @ProtoId(7) val faceUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val customBuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val expireTime: Int = 0,
        @ProtoId(10) val cmdUinType: Int = 0,
        @ProtoId(11) val reportType: Int = 0,
        @ProtoId(12) val boolTestEnv: Boolean = false
    ) : ProtoBuf
}


internal class Submsgtype0x6b {
    @Serializable
    class SubMsgType0x6b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val toUin: Long = 0L,
            @ProtoId(2) val tipsContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val yesText: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val noText: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x6f {
    @Serializable
    class SubMsgType0x6f : ProtoBuf {
        @Serializable
        internal class AddFriendSource(
            @ProtoId(1) val source: Int = 0,
            @ProtoId(2) val subSource: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class AddQimFriendNotifyToQQ(
            @ProtoId(1) val opType: Int = 0,
            @ProtoId(2) val uin: Long = 0L,
            @ProtoId(3) val gender: Int = 0,
            @ProtoId(4) val smartRemark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val longnick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val storysTotalNum: Long = 0L,
            @ProtoId(7) val caresCount: Long = 0L,
            @ProtoId(8) val fansCount: Long = 0L,
            @ProtoId(9) val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val srcWording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class AddQimNotLoginFrdNotifyToQQ(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val nick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val gender: Int = 0,
            @ProtoId(4) val age: Int = 0,
            @ProtoId(5) val coverstory: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val storysTotalNum: Long = 0L,
            @ProtoId(7) val msgVideoInfo: List<VideoInfo>? = null,
            @ProtoId(8) val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val qqUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class BirthdayReminderPush(
            @ProtoId(2004) val reminderWording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class FanpaiziNotify(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val fromNick: String = "",
            @ProtoId(3) val tipsContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ForwardBody(
            @ProtoId(1) val notifyType: Int = 0,
            @ProtoId(2) val opType: Int = 0,
            @ProtoId(2000) val msgFanpanziNotify: FanpaiziNotify? = null,
            @ProtoId(2001) val msgMcardNotificationLike: MCardNotificationLike? = null,
            @ProtoId(2002) val msgVipInfoNotify: VipInfoNotify? = null,
            @ProtoId(2003) val msgPushLostDevFound: PushLostDevFound? = null,
            @ProtoId(2004) val msgBirthdayReminderPush: BirthdayReminderPush? = null,
            @ProtoId(2005) val msgPushLostDev: PushLostDevFound? = null,
            @ProtoId(2007) val msgBabyqRewardInfo: RewardInfo? = null,
            @ProtoId(2008) val msgHotFriendNotify: HotFriendNotify? = null,
            @ProtoId(2009) val msgPushQimRecommend: QimRecomendMsg? = null,
            @ProtoId(2010) val msgModQimFriend: QimFriendNotify? = null,
            @ProtoId(2011) val msgModQimFriendToQq: QimFriendNotifyToQQ? = null
        ) : ProtoBuf

        @Serializable
        internal class GPS(
            @ProtoId(1) val int32Lat: Int = 900000000,
            @ProtoId(2) val int32Lon: Int = 900000000,
            @ProtoId(3) val int32Alt: Int = -10000000,
            @ProtoId(4) val int32Type: Int = 0
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
            @ProtoId(13) val lastChatTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class MCardNotificationLike(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val counterTotal: Int = 0,
            @ProtoId(3) val counterNew: Int = 0,
            @ProtoId(4) val wording: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgModInfos: List<ForwardBody>? = null
        ) : ProtoBuf

        @Serializable
        internal class PushLostDevFound(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(3) val devTime: Int = 0,
            @ProtoId(6) val din: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class QimFriendNotify(
            @ProtoId(1) val opType: Int = 0,
            @ProtoId(2) val uint64Uins: List<Long>? = null,
            @ProtoId(3) val fansUnreadCount: Long = 0L,
            @ProtoId(4) val fansTotalCount: Long = 0L,
            @ProtoId(5) val pushTime: Long = 0L,
            @ProtoId(6) val bytesMobiles: List<ByteArray>? = null
        ) : ProtoBuf

        @Serializable
        internal class QimFriendNotifyToQQ(
            @ProtoId(1) val notifyType: Int = 0,
            @ProtoId(2) val msgAddNotifyToQq: AddQimFriendNotifyToQQ? = null,
            @ProtoId(3) val msgUpgradeNotify: UpgradeQimFriendNotify? = null,
            @ProtoId(4) val msgAddNotLoginFrdNotifyToQq: AddQimNotLoginFrdNotifyToQQ? = null
        ) : ProtoBuf

        @Serializable
        internal class QimRecomendInfo(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val reason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val gender: Int = 0,
            @ProtoId(5) val longnick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val alghbuff: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val age: Int = 0,
            @ProtoId(8) val source: Int = 0,
            @ProtoId(9) val sourceReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val msgIosSource: AddFriendSource? = null,
            @ProtoId(11) val msgAndroidSource: AddFriendSource? = null
        ) : ProtoBuf

        @Serializable
        internal class QimRecomendMsg(
            @ProtoId(1) val msgRecomendList: List<QimRecomendInfo>? = null,
            @ProtoId(2) val timestamp: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class RewardInfo(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val jmpUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val cookies: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val jmpWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val optWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val optUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val faceAddonId: Long = 0L,
            @ProtoId(9) val iconUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val toastWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(11) val reportType: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class UpgradeQimFriendNotify(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val wording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class VideoInfo(
            @ProtoId(1) val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val videoCoverUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class VipInfoNotify(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val vipLevel: Int = 0,
            @ProtoId(3) val vipIdentify: Int = 0,
            @ProtoId(4) val ext: Int = 0,
            @ProtoId(5) val extString: String = "",
            @ProtoId(6) val redFlag: Int = 0,
            @ProtoId(7) val disableRedEnvelope: Int = 0,
            @ProtoId(8) val redpackId: Int = 0,
            @ProtoId(9) val redpackName: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x71 {
    @Serializable
    class Submsgtype0x71 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgAppInfo: List<ReportAppInfo>? = null,
            @ProtoId(2) val uiUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class RedDisplayInfo(
            @ProtoId(1) val msgRedTypeInfo: List<RedTypeInfo>? = null,
            @ProtoId(2) val msgTabDisplayInfo: RedTypeInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class RedTypeInfo(
            @ProtoId(1) val redType: Int = 0,
            @ProtoId(2) val redContent: String = "",
            @ProtoId(3) val redDesc: String = "",
            @ProtoId(4) val redPriority: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class ReportAppInfo(
            @ProtoId(1) val appId: Int = 0,
            @ProtoId(2) val int32NewFlag: Int = 0,
            @ProtoId(3) val type: Int = 0,
            @ProtoId(4) val buffer: String = "",
            @ProtoId(5) val path: String = "",
            @ProtoId(6) val pushRedTs: Int = 0,
            @ProtoId(7) val mission: String = "",
            @ProtoId(8) val int32Appset: Int = 0,
            @ProtoId(9) val int32Num: Int = 0,
            @ProtoId(10) val iconUrl: String = "",
            @ProtoId(11) val int32IconFlag: Int = 0,
            @ProtoId(12) val int32IconType: Int = 0,
            @ProtoId(13) val duration: Int = 0,
            @ProtoId(14) val msgVersionInfo: ReportVersion? = null,
            @ProtoId(15) val androidAppId: Int = 0,
            @ProtoId(16) val iosAppId: Int = 0,
            @ProtoId(17) val androidPath: String = "",
            @ProtoId(18) val iosPath: String = "",
            @ProtoId(19) val int32MissionLevel: Int = 0,
            @ProtoId(20) val msgDisplayDesc: RedDisplayInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class ReportVersion(
            @ProtoId(1) val int32PlantId: Int = 0,
            @ProtoId(2) val boolAllver: Boolean = false,
            @ProtoId(3) val strVersion: List<String> = listOf()
        ) : ProtoBuf
    }
}


internal class Submsgtype0x72 {
    @Serializable
    class SubMsgType0x72 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val urgency: Int = 0,
            @ProtoId(3) val templateNo: Int = 0,
            @ProtoId(4) val content: String = "",
            @ProtoId(5) val infoDate: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x76 {
    @Serializable
    class SubMsgType0x76 : ProtoBuf {
        @Serializable
        internal class BirthdayNotify(
            @ProtoId(1) val msgOneFriend: List<OneBirthdayFriend>? = null,
            @ProtoId(2) val reserved: Int = 0,
            @ProtoId(3) val giftMsg: List<OneGiftMessage>? = null,
            @ProtoId(4) val topPicUrl: String = "",
            @ProtoId(5) val extend: String = ""
        ) : ProtoBuf

        @Serializable
        internal class GeoGraphicNotify(
            @ProtoId(1) val localCity: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val msgOneFriend: List<OneGeoGraphicFriend>? = null
        ) : ProtoBuf

        @Serializable
        internal class MemorialDayNotify(
            @ProtoId(1) val anniversaryInfo: List<OneMemorialDayInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val boolStrongNotify: Boolean = false,
            @ProtoId(3) val pushTime: Int = 0,
            @ProtoId(4) val msgGeographicNotify: GeoGraphicNotify? = null,
            @ProtoId(5) val msgBirthdayNotify: BirthdayNotify? = null,
            @ProtoId(6) val notifyWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val msgMemorialdayNotify: MemorialDayNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class OneBirthdayFriend(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val boolLunarBirth: Boolean = false,
            @ProtoId(3) val birthMonth: Int = 0,
            @ProtoId(4) val birthDate: Int = 0,
            @ProtoId(5) val msgSendTime: Long = 0L,
            @ProtoId(6) val birthYear: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OneGeoGraphicFriend(
            @ProtoId(1) val uin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class OneGiftMessage(
            @ProtoId(1) val giftId: Int = 0,
            @ProtoId(2) val giftName: String = "",
            @ProtoId(3) val type: Int = 0,
            @ProtoId(4) val giftUrl: String = "",
            @ProtoId(5) val price: Int = 0,
            @ProtoId(6) val playCnt: Int = 0,
            @ProtoId(7) val backgroundColor: String = ""
        ) : ProtoBuf

        @Serializable
        internal class OneMemorialDayInfo(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val type: Long = 0,
            @ProtoId(3) val memorialTime: Int = 0,
            @ProtoId(11) val mainWordingNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(12) val mainWordingEvent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) val subWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(14) val greetings: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(15) val friendGender: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x78 {
    @Serializable
    class Submsgtype0x78 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val version: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x7a {
    @Serializable
    class Submsgtype0x7a : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val content: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val fromUin: Long = 0L,
            @ProtoId(3) val nick: String = "",
            @ProtoId(4) val discussUin: Long = 0L,
            @ProtoId(5) val discussNick: String = "",
            @ProtoId(6) val seq: Long = 0L,
            @ProtoId(7) val atTime: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x7c {
    @Serializable
    class Submsgtype0x7c : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val int32Cmd: Int = 0,
            @ProtoId(3) val stringCmdExt: List<String> = listOf(),
            @ProtoId(4) val seq: Long = 0L,
            @ProtoId(5) val stringSeqExt: List<String> = listOf()
        ) : ProtoBuf
    }
}


internal class Submsgtype0x7e {
    @Serializable
    class Submsgtype0x7e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val notice: String = "",
            @ProtoId(2) val msgOnlinePush: WalletMsgPush? = null
        ) : ProtoBuf

        @Serializable
        internal class WalletMsgPush(
            @ProtoId(1) val action: Int = 0,
            @ProtoId(2) val timestamp: Int = 0,
            @ProtoId(3) val extend: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val serialno: String = "",
            @ProtoId(5) val billno: String = "",
            @ProtoId(6) val appinfo: String = "",
            @ProtoId(7) val amount: Int = 0,
            @ProtoId(8) val jumpurl: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x83 {
    @Serializable
    class SubMsgType0x83 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgParams: List<MsgParams>? = null,
            @ProtoId(2) val seq: Long = 0L,
            @ProtoId(3) val groupId: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class MsgParams(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val fromUin: Long = 0L,
            @ProtoId(3) val toUin: Long = 0L,
            @ProtoId(4) val dataString: String = "",
            @ProtoId(5) val data: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        class MsgRep : ProtoBuf
    }
}


internal class Submsgtype0x85 {
    @Serializable
    class SubMsgType0x85 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val showLastest: Int = 0,
            @ProtoId(2) val senderUin: Long = 0L,
            @ProtoId(3) val receiverUin: Long = 0L,
            @ProtoId(4) val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val authkey: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val pcBody: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val icon: Int = 0,
            @ProtoId(9) val random: Int = 0,
            @ProtoId(10) val redSenderUin: Long = 0L,
            @ProtoId(11) val type: Int = 0,
            @ProtoId(12) val subType: Int = 0,
            @ProtoId(13) val jumpurl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x86 {
    @Serializable
    class SubMsgType0x86 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val notifyFlag: Int = 0,
            @ProtoId(2) val notifyWording: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0x87 {
    @Serializable
    class SubMsgType0x87 : ProtoBuf {
        @Serializable
        internal class CloneInfo(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val remark: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val originNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val showInAio: Int = 0,
            @ProtoId(5) val toUin: Long = 0L,
            @ProtoId(6) val toNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val srcGender: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val friendMsgTypeFlag: Long = 0L,
            @ProtoId(2) val msgMsgNotify: List<MsgNotify>? = null,
            @ProtoId(3) val msgMsgNotifyUnread: MsgNotifyUnread? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgNotify(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val fuin: Long = 0L,
            @ProtoId(3) val time: Int = 0,
            @ProtoId(4) val reqsubtype: Int = 0,
            @ProtoId(5) val maxCount: Int = 0,
            @ProtoId(6) val msgCloneInfo: CloneInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgNotifyUnread(
            @ProtoId(1) val unreadcount: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x89 {
    @Serializable
    class Submsgtype0x89 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val uiUin: Long = 0L,
            @ProtoId(2) val pushRedTs: Int = 0,
            @ProtoId(3) val msgNumRed: List<NumRedBusiInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class NumRedBusiInfo(
            @ProtoId(1) val clientVerBegin: String = "",
            @ProtoId(2) val clientVerEnd: String = "",
            @ProtoId(3) val platId: Int = 0,
            @ProtoId(4) val appId: Int = 0,
            @ProtoId(5) val androidAppId: Int = 0,
            @ProtoId(6) val iosAppId: Int = 0,
            @ProtoId(7) val path: String = "",
            @ProtoId(8) val androidPath: String = "",
            @ProtoId(9) val iosPath: String = "",
            @ProtoId(10) val missionid: String = "",
            @ProtoId(11) val msgid: Long = 0L,
            @ProtoId(12) val status: Int = 0,
            @ProtoId(13) val expireTime: Int = 0,
            @ProtoId(14) val int32Appset: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x8d {
    @Serializable
    class SubMsgType0x8d : ProtoBuf {
        @Serializable
        internal class ChannelNotify(
            @ProtoId(1) val channelId: Long = 0L,
            @ProtoId(2) val channelName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val topArticleIdList: List<Long>? = null
        ) : ProtoBuf

        @Serializable
        internal class CommentFeeds(
            @ProtoId(1) val feedsOwner: Long = 0L,
            @ProtoId(2) val feedsId: Long = 0L,
            @ProtoId(3) val commentUin: Long = 0L,
            @ProtoId(4) val commentId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val replyUin: Long = 0L,
            @ProtoId(6) val replyId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val commentInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val feedsSubject: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class DeleteComment(
            @ProtoId(1) val feedsOwner: Long = 0L,
            @ProtoId(2) val feedsId: Long = 0L,
            @ProtoId(3) val commentUin: Long = 0L,
            @ProtoId(4) val commentId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val replyUin: Long = 0L,
            @ProtoId(6) val replyId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val deleteUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class DeleteFeeds(
            @ProtoId(1) val feedsOwner: Long = 0L,
            @ProtoId(2) val feedsId: Long = 0L,
            @ProtoId(3) val deleteUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class LikeFeeds(
            @ProtoId(1) val feedsOwner: Long = 0L,
            @ProtoId(2) val feedsId: Long = 0L,
            @ProtoId(3) val likeUin: Long = 0L,
            @ProtoId(4) val feedsSubject: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgNotifyInfos: List<NotifyBody>? = null,
            @ProtoId(2) val redSpotNotifyBody: RedSpotNotifyBody? = null
        ) : ProtoBuf

        @Serializable
        internal class NotifyBody(
            @ProtoId(1) val notifyType: Int = 0,
            @ProtoId(2) val seq: Int = 0,
            @ProtoId(3) val pushTime: Int = 0,
            @ProtoId(10) val msgPublishFeeds: PublishFeeds? = null,
            @ProtoId(11) val msgCommentFeeds: CommentFeeds? = null,
            @ProtoId(12) val msgLikeFeeds: LikeFeeds? = null,
            @ProtoId(13) val msgDeleteFeeds: DeleteFeeds? = null,
            @ProtoId(14) val msgDeleteComment: DeleteComment? = null
        ) : ProtoBuf

        @Serializable
        internal class PublishFeeds(
            @ProtoId(1) val feedsOwner: Long = 0L,
            @ProtoId(2) val feedsId: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class RedSpotNotifyBody(
            @ProtoId(1) val time: Int = 0,
            @ProtoId(2) val newChannelList: List<Long>? = null,
            @ProtoId(3) val guideWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val msgChannelNotify: ChannelNotify? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x8f {
    @Serializable
    class Submsgtype0x8f : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgSourceId: SourceID? = null,
            @ProtoId(2) val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val enumMsgType: Int /* enum */ = 1,
            @ProtoId(4) val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val authorUin: Long = 0L,
            @ProtoId(6) val confirmUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class SourceID(
            @ProtoId(1) val sourceType: Int = 0,
            @ProtoId(2) val sourceCode: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0x90 {
    @Serializable
    class SubMsgType0x90 : ProtoBuf {
        @Serializable
        internal class DpNotifyMsgBdoy(
            @ProtoId(1) val pid: Int = 0,
            @ProtoId(2) val din: Long = 0L,
            @ProtoId(3) val msgNotifyInfo: List<NotifyItem>? = null,
            @ProtoId(4) val extendInfo: String = ""
        ) : ProtoBuf

        @Serializable
        internal class Head(
            @ProtoId(1) val cmd: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgHead: Head? = null,
            @ProtoId(2) val msgBody: PushBody? = null
        ) : ProtoBuf

        @Serializable
        internal class NotifyItem(
            @ProtoId(1) val propertyid: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OccupyMicrophoneNotifyMsgBody(
            @ProtoId(1) val uin: Int = 0,
            @ProtoId(2) val din: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class PushBody(
            @ProtoId(1) val msgDpNotifyBody: DpNotifyMsgBdoy? = null,
            @ProtoId(2) val msgOccupyMicrophoneBody: OccupyMicrophoneNotifyMsgBody? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x92 {
    @Serializable
    class SubMsgType0x92 : ProtoBuf {
        @Serializable
        internal class CrmS2CMsgHead(
            @ProtoId(1) val crmSubCmd: Int = 0,
            @ProtoId(2) val headLen: Int = 0,
            @ProtoId(3) val verNo: Int = 0,
            @ProtoId(4) val kfUin: Long = 0L,
            @ProtoId(5) val seq: Int = 0,
            @ProtoId(6) val packNum: Int = 0,
            @ProtoId(7) val curPack: Int = 0,
            @ProtoId(8) val bufSig: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val msgCrmCommonHead: CrmS2CMsgHead? = null,
            @ProtoId(100) val msgPushEmanMsg: S2CPushEmanMsgToC? = null
        ) : ProtoBuf {
            @Serializable
            internal class S2CPushEmanMsgToC(
                @ProtoId(1) val uin: Long = 0L,
                @ProtoId(2) val xml: String = ""
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0x93 {
    @Serializable
    class Submsgtype0x93 : ProtoBuf {
        @Serializable
        internal class LiteMailIndexInfo(
            @ProtoId(1) val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val msgSourceId: SourceID? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgUmcChanged: UnreadMailCountChanged? = null,
            @ProtoId(3) val msgStateChanged: StateChangeNotify? = null
        ) : ProtoBuf

        @Serializable
        internal class SourceID(
            @ProtoId(1) val sourceType: Int = 0,
            @ProtoId(2) val sourceCode: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class StateChangeNotify(
            @ProtoId(1) val msgSourceId: SourceID? = null,
            @ProtoId(2) val feedsId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val enumMsgType: Int /* enum */ = 1,
            @ProtoId(4) val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val reqUin: Long = 0L,
            @ProtoId(6) val msgLiteMailIndex: List<LiteMailIndexInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class UnreadMailCountChanged(
            @ProtoId(1) val msgUmc: UnreadMailCountInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class UnreadMailCountInfo(
            @ProtoId(1) val unreadCount: Int = 0,
            @ProtoId(2) val dataVersion: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x94 {
    @Serializable
    class Submsgtype0x94 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val taskId: Int = 0,
            @ProtoId(2) val folderReddotFlag: Int = 0,
            @ProtoId(3) val discoverReddotFlag: Int = 0,
            @ProtoId(4) val startTs: Int = 0,
            @ProtoId(5) val endTs: Int = 0,
            @ProtoId(6) val periodOfValidity: Int = 0,
            @ProtoId(7) val folderMsg: String = "",
            @ProtoId(8) val discountReddotFlag: Int = 0,
            @ProtoId(9) val nearbyReddotFlag: Int = 0,
            @ProtoId(10) val mineReddotFlag: Int = 0,
            @ProtoId(11) val onlyDiscoverReddotFlag: Int = 0,
            @ProtoId(12) val onlyDiscountReddotFlag: Int = 0,
            @ProtoId(13) val onlyNearbyReddotFlag: Int = 0,
            @ProtoId(14) val onlyMineReddotFlag: Int = 0,
            @ProtoId(15) val taskType: Int = 0,
            @ProtoId(16) val taskInfo: String = "",
            @ProtoId(17) val typeName: String = "",
            @ProtoId(18) val typeColor: String = "",
            @ProtoId(19) val jumpUrl: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x96 {
    @Serializable
    class Submsgtype0x96 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val pushMsg: String = "",
            @ProtoId(2) val pushType: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x97 {
    @Serializable
    class Submsgtype0x97 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val businessUin: String = "",
            @ProtoId(2) val jsonContext: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0x98 {
    @Serializable
    class Submsgtype0x98 : ProtoBuf {
        @Serializable
        internal class ModBlock(
            @ProtoId(1) val op: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val subCmd: Int = 0,
            @ProtoId(3) val msgModBlock: ModBlock? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9b {
    @Serializable
    class SubMsgType0x9b : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val appId: Long = 0L,
            @ProtoId(2) val mainType: Int = 0,
            @ProtoId(3) val subType: Int = 0,
            @ProtoId(4) val extMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val workflowId: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PbOfficeNotify(
            @ProtoId(1) val optUint32MyofficeFlag: Int = 0,
            @ProtoId(2) val uint64Appid: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9d {
    @Serializable
    class SubMsgType0x9d : ProtoBuf {
        @Serializable
        internal class ModuleUpdateNotify(
            @ProtoId(1) val moduleId: Int = 0,
            @ProtoId(2) val moduleVersion: Int = 0,
            @ProtoId(3) val moduleState: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val lolaModuleUpdate: List<ModuleUpdateNotify>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9e {
    @Serializable
    class SubmsgType0x9e : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val url: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val authKey: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0x9f {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) val showLastest: Int = 0,
        @ProtoId(2) val senderUin: Long = 0L,
        @ProtoId(3) val receiverUin: Long = 0L,
        @ProtoId(4) val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val authkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoType(ProtoNumberType.SIGNED) @ProtoId(7) val sint32Sessiontype: Int = 0,
        @ProtoId(8) val groupUin: Long = 0L
    ) : ProtoBuf
}


internal class Submsgtype0xa0 {
    @Serializable
    class Submsgtype0xa0 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val isMassBlessOpen: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0xa1 {
    @Serializable
    class Submsgtype0xa1 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val qid: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(3) val fixed32UpdateTime: Int = 0,
            @ProtoId(4) val teamCreatedDestroied: Int = 0,
            @ProtoId(5) val uint64OfficeFaceChangedUins: List<Long>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xa2 {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) val showLastest: Int = 0,
        @ProtoId(2) val senderUin: Long = 0L,
        @ProtoId(3) val receiverUin: Long = 0L,
        @ProtoId(4) val senderRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val receiverRichContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val authkey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


internal class Submsgtype0xa4 {
    @Serializable
    class Submsgtype0xa4 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val brief: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val url: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xa8 {
    @Serializable
    class SubMsgType0xa8 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val actionType: Int = 0,
            @ProtoId(2) val actionSubType: Int = 0,
            @ProtoId(3) val msgSummary: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val extendContent: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xaa {
    @Serializable
    class SubMsgType0xaa : ProtoBuf {
        @Serializable
        internal class GameTeamMsgBody(
            @ProtoId(1) val gameTeamCmd: Int = 0,
            @ProtoId(2) val msgTurnOverMessage: GameTeamTurnOverMessage? = null,
            @ProtoId(3) val msgStartGameMessage: GameTeamStartGameMessage? = null,
            @ProtoId(4) val msgUpdateTeamMessage: GameTeamUpdateTeamMessage? = null
        ) : ProtoBuf

        @Serializable
        internal class GameTeamStartGameMessage(
            @ProtoId(1) val gamedata: String = "",
            @ProtoId(2) val platformType: Int = 0,
            @ProtoId(3) val title: String = "",
            @ProtoId(4) val summary: String = "",
            @ProtoId(5) val picUrl: String = "",
            @ProtoId(6) val appid: String = "",
            @ProtoId(7) val appStoreId: String = "",
            @ProtoId(8) val packageName: String = "",
            @ProtoId(9) val createMsgTime: Long = 0L,
            @ProtoId(10) val expire: Int = 0,
            @ProtoId(11) val buildTeamTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class GameTeamTurnOverMessage(
            @ProtoId(1) val teamId: String = "",
            @ProtoId(2) val sessionType: Int = 0,
            @ProtoId(3) val sourceUin: String = "",
            @ProtoId(4) val actionUin: String = "",
            @ProtoId(5) val actionType: Int = 0,
            @ProtoId(6) val currentCount: Int = 0,
            @ProtoId(7) val totalCount: Int = 0,
            @ProtoId(8) val createMsgTime: Long = 0L,
            @ProtoId(9) val status: Int = 0,
            @ProtoId(10) val expire: Int = 0,
            @ProtoId(11) val buildTeamTime: Long = 0L,
            @ProtoId(12) val leaderUin: String = "",
            @ProtoId(13) val uin32LeaderStatus: Int = 0,
            @ProtoId(14) val inviteSourceList: List<InviteSource>? = null
        ) : ProtoBuf

        @Serializable
        internal class GameTeamUpdateTeamMessage(
            @ProtoId(1) val teamId: String = "",
            @ProtoId(2) val gameId: String = "",
            @ProtoId(3) val status: Int = 0,
            @ProtoId(4) val modeImg: String = "",
            @ProtoId(5) val currentCount: Int = 0,
            @ProtoId(6) val createMsgTime: Long = 0L,
            @ProtoId(7) val expire: Int = 0,
            @ProtoId(8) val buildTeamTime: Long = 0L,
            @ProtoId(9) val leaderUin: String = "",
            @ProtoId(10) val uin32LeaderStatus: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class InviteSource(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val src: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val cmd: Int = 0,
            @ProtoId(2) val msgGameTeamMsg: GameTeamMsgBody? = null,
            @ProtoId(3) val msgOnlineDocMsg: OnlineDocMsgBody? = null
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocMsgBody(
            @ProtoId(1) val onlineDocCmd: Int = 0,
            @ProtoId(2) val msgPushChangeTitleMessage: OnlineDocPushChangeTitleMessage? = null,
            @ProtoId(3) val msgPushNewPadMessage: OnlineDocPushNewPadMessage? = null,
            @ProtoId(4) val msgPushPreviewToEdit: OnlineDocPushPreviewToEditMessage? = null
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocPushChangeTitleMessage(
            @ProtoId(1) val domainid: Int = 0,
            @ProtoId(2) val localpadid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val lastEditorUin: Long = 0L,
            @ProtoId(5) val lastEditorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val lastEditTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocPushNewPadMessage(
            @ProtoId(1) val padUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val type: Int = 0,
            @ProtoId(3) val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val createTime: Long = 0L,
            @ProtoId(5) val creatorUin: Long = 0L,
            @ProtoId(6) val creatorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val lastEditorUin: Long = 0L,
            @ProtoId(8) val lastEditorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val lastEditTime: Long = 0L,
            @ProtoId(10) val boolPinnedFlag: Boolean = false,
            @ProtoId(11) val lastViewerUin: Long = 0L,
            @ProtoId(12) val lastViewerNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) val lastViewTime: Long = 0L,
            @ProtoId(14) val lastPinnedTime: Long = 0L,
            @ProtoId(15) val currentUserBrowseTime: Long = 0L,
            @ProtoId(16) val hostuserUin: Long = 0L,
            @ProtoId(17) val hostuserNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(18) val lastAuthTime: Long = 0L,
            @ProtoId(19) val policy: Int = 0,
            @ProtoId(20) val rightFlag: Int = 0,
            @ProtoId(21) val domainid: Int = 0,
            @ProtoId(22) val localpadid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(23) val lastUnpinnedTime: Long = 0L,
            @ProtoId(24) val boolDeleteFlag: Boolean = false,
            @ProtoId(25) val lastDeleteTime: Long = 0L,
            @ProtoId(26) val thumbUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(27) val pdid: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class OnlineDocPushPreviewToEditMessage(
            @ProtoId(1) val version: Int = 0,
            @ProtoId(2) val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val padUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val aioSession: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xab {
    @Serializable
    class SubMsgType0xab : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val gc: Long = 0L,
            @ProtoId(3) val rewardId: String = "",
            @ProtoId(4) val rewardStatus: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0xae {
    @Serializable
    class SubMsgType0xae : ProtoBuf {
        @Serializable
        internal class AddFriendSource(
            @ProtoId(1) val source: Int = 0,
            @ProtoId(2) val subSource: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val msgPeopleMayKonw: PushPeopleMayKnow? = null,
            @ProtoId(3) val msgPersonsMayKnow: PushPeopleMayKnowV2? = null
        ) : ProtoBuf

        @Serializable
        internal class PersonMayKnow(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val age: Int = 0,
            @ProtoId(4) val sex: Int = 0,
            @ProtoId(5) val mainReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val soureReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val alghrithm: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val source: Int = 0,
            @ProtoId(9) val msgIosSource: AddFriendSource? = null,
            @ProtoId(10) val msgAndroidSource: AddFriendSource? = null,
            @ProtoId(11) val msg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(12) val gameSource: Int = 0,
            @ProtoId(13) val roleName: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushPeopleMayKnow(
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(1) val fixed32Timestamp: Int = 0,
            @ProtoId(2) val wordingMsg: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class PushPeopleMayKnowV2(
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(1) val fixed32Timestamp: Int = 0,
            @ProtoId(2) val msgFriendList: List<PersonMayKnow>? = null,
            @ProtoId(3) val roleName: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xb1 {
    @Serializable
    class Submsgtype0xb1 : ProtoBuf {
        @Serializable
        internal class DealInviteInfo(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val groupCode: Long = 0L,
            @ProtoId(3) val id: String = "",
            @ProtoId(4) val dealResult: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class InviteInfo(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val groupCode: Long = 0L,
            @ProtoId(3) val expireTime: Int = 0,
            @ProtoId(4) val id: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val notifyType: Int = 0,
            @ProtoId(2) val inviteInfo: InviteInfo? = null,
            @ProtoId(3) val univiteInfo: UninviteInfo? = null,
            @ProtoId(4) val dealInfo: DealInviteInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class UninviteInfo(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val groupCode: Long = 0L,
            @ProtoId(3) val id: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xb3 {
    class SubMsgType0xb3 {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val msgAddFrdNotify: PushAddFrdNotify
        ) : ProtoBuf

        @Serializable
        internal class PushAddFrdNotify(
            @ProtoId(1) val fuin: Long = 0L,
            @ProtoId(2) val fuinBubbleId: Long = 0L,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(3) val fixed32Timestamp: Int = 0,
            @ProtoId(4) val wording: String = "", // 我们已经是好友啦，一起来聊天吧!
            @ProtoId(5) val fuinNick: String = "",
            @ProtoId(6) val sourceId: Int = 0,
            @ProtoId(7) val subsourceId: Int = 0,
            @ProtoId(8) val mobile: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val reqUin: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xb5 {
    @Serializable
    class SubMsgType0xb5 : ProtoBuf {
        @Serializable
        internal class MsgBody(
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
            @ProtoId(10000) val groupCode: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xbe {
    @Serializable
    class SubMsgType0xbe : ProtoBuf {
        @Serializable
        internal class Medal(
            @ProtoId(1) val id: Int = 0,
            @ProtoId(2) val level: Int = 0,
            @ProtoId(3) val type: Int = 0,
            @ProtoId(4) val iconUrl: String = "",
            @ProtoId(5) val flashUrl: String = "",
            @ProtoId(6) val name: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val groupCode: Long = 0L,
            @ProtoId(3) val notifyType: Int = 0,
            @ProtoId(4) val onlineLevel: Int = 0,
            @ProtoId(5) val msgMedalList: List<Medal>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc1 {
    @Serializable
    class Submsgtype0xc1 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val groupid: Long = 0L,
            @ProtoId(2) val memberNum: Int = 0,
            @ProtoId(3) val data: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc3 {
    @Serializable
    class Submsgtype0xc3 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val pushData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val timestamp: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc5 {
    @Serializable
    class Submsgtype0xc5 : ProtoBuf {
        @Serializable
        internal class BBInfo(
            @ProtoId(1) val bbUin: Long = 0L,
            @ProtoId(2) val src: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class BiuBody(
            @ProtoId(1) val biuUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class CommentInfo(
            @ProtoId(2) val commentUin: Long = 0L,
            @ProtoId(3) val commentId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val replyUin: Long = 0L,
            @ProtoId(5) val replyId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val commentContent: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class LikeInfo(
            @ProtoId(2) val likeUin: Long = 0L,
            @ProtoId(3) val op: Int = 0,
            @ProtoId(4) val replyId: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val bid: Int = 0,
            @ProtoId(2) val source: Int = 0,
            @ProtoId(3) val operatorType: Int /* enum */ = 1,
            @ProtoId(4) val articleId: Long = 0L,
            @ProtoId(5) val pushTime: Int = 0,
            @ProtoId(6) val seq: Long = 0L,
            @ProtoId(10) val msgNotifyInfos: NotifyBody? = null,
            @ProtoId(11) val diandianCookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class NotifyBody(
            @ProtoId(1) val msgStyleSheet: StyleSheet? = null,
            @ProtoId(10) val msgCommentArticle: CommentInfo? = null,
            @ProtoId(11) val msgLikeArticle: LikeInfo? = null,
            @ProtoId(12) val msgBbInfo: BBInfo? = null,
            @ProtoId(13) val redPointInfo: List<RedPointInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class RedPointInfo(
            @ProtoId(1) val itemId: Int = 0,
            @ProtoId(2) val redPointItemType: Int /* enum */ = 0,
            @ProtoId(3) val url: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val effectTime: Long = 0L,
            @ProtoId(5) val failureTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class StyleSheet(
            @ProtoId(1) val showFolder: Int = 0,
            @ProtoId(2) val folderRedType: Int /* enum */ = 0,
            @ProtoId(3) val orangeWord: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val summary: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val msgTipBody: TipsBody? = null,
            @ProtoId(6) val showLockScreen: Int = 0,
            @ProtoId(7) val msgType: Int /* enum */ = 0,
            @ProtoId(8) val msgBiuBody: BiuBody? = null,
            @ProtoId(9) val isLow: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class TipsBody(
            @ProtoId(1) val tipsUiType: Int /* enum */ = 0,
            @ProtoId(2) val uin: Long = 0L,
            @ProtoId(3) val iconUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val content: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val schema: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val businessInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc6 {
    @Serializable
    class SubMsgType0xc6 : ProtoBuf {
        @Serializable
        internal class AccountExceptionAlertBody(
            @ProtoId(1) val title: String = "",
            @ProtoId(2) val content: String = "",
            @ProtoId(3) val leftButtonText: String = "",
            @ProtoId(4) val rightButtonText: String = "",
            @ProtoId(5) val rightButtonLink: String = "",
            @ProtoId(6) val leftButtonId: Int = 0,
            @ProtoId(7) val rightButtonId: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val secCmd: Int = 0,
            @ProtoId(2) val msgS2cAccountExceptionNotify: AccountExceptionAlertBody? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc7 {
    @Serializable
    class Submsgtype0xc7 : ProtoBuf {
        @Serializable
        internal class ForwardBody(
            @ProtoId(1) val notifyType: Int = 0,
            @ProtoId(2) val opType: Int = 0,
            @ProtoId(3000) val msgHotFriendNotify: HotFriendNotify? = null,
            @ProtoId(4000) val msgRelationalChainChange: RelationalChainChange? = null
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
        internal class MsgBody(
            @ProtoId(1) val msgModInfos: List<ForwardBody>? = null
        ) : ProtoBuf

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
        internal class RelationalChainInfo(
            @ProtoId(1) val type: Int /* enum */ = 1,
            @ProtoId(2) val attr: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(1002) val intimateInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(91001) val musicSwitch: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(101001) val mutualmarkAlienation: ByteArray = EMPTY_BYTE_ARRAY
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
        internal class ToDegradeItem(
            @ProtoId(1) val type: Int /* enum */ = 1,
            @ProtoId(2) val oldLevel: Int = 0,
            @ProtoId(3) val newLevel: Int = 0,
            @ProtoId(11) val continuityDays: Int = 0,
            @ProtoId(12) val lastActionTime: Long = 0L
        ) : ProtoBuf
    }
}


internal class Mutualmark {
    @Serializable
    class Mutualmark : ProtoBuf {
        @Serializable
        internal class MutualmarkInfo(
            @ProtoId(1) val lastActionTime: Long = 0L,
            @ProtoId(2) val level: Int = 0,
            @ProtoId(3) val lastChangeTime: Long = 0L,
            @ProtoId(4) val continueDays: Int = 0,
            @ProtoId(5) val wildcardWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val notifyTime: Long = 0L,
            @ProtoId(7) val iconStatus: Long = 0L,
            @ProtoId(8) val iconStatusEndTime: Long = 0L,
            @ProtoId(9) val closeFlag: Int = 0,
            @ProtoId(10) val resourceInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ResourceInfo17(
            @ProtoId(1) val dynamicUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val staticUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val cartoonUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val cartoonMd5: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val playCartoon: Int = 0,
            @ProtoId(6) val word: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xc9 {
    @Serializable
    class Submsgtype0xc9 : ProtoBuf {
        @Serializable
        internal class BusinessMsg(
            @ProtoId(1) val msgType: Int /* enum */ = 0,
            @ProtoId(2) val msgData: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val boolTabVisible: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val fromUin: Long = 0L,
            @ProtoId(3) val actionUin: Long = 0L,
            @ProtoId(4) val source: Int /* enum */ = 0,
            @ProtoId(5) val msgBusinessMsg: List<BusinessMsg>? = null,
            @ProtoId(6) val boolNewFriend: Boolean = false
        ) : ProtoBuf
    }
}


internal class Submsgtype0xca {
    @Serializable
    class Submsgtype0xca : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val msgList: List<MsgContent>? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgContent(
            @ProtoId(1) val tag: Long = 0L,
            @ProtoId(2) val msgType: Long = 0L,
            @ProtoId(3) val seq: Long = 0L,
            @ProtoId(4) val content: String = "",
            @ProtoId(5) val actionId: Long = 0L,
            @ProtoId(6) val ts: Long = 0L,
            @ProtoId(7) val expts: Long = 0L,
            @ProtoId(8) val errorMsg: String = "",
            @ProtoId(9) val showSpace: Long = 0L,
            @ProtoId(10) val regionUrl: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xcb {
    @Serializable
    class SubMsgType0xcb : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val anchorStatus: Int = 0,
            @ProtoId(2) val jumpSchema: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val anchorNickname: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val anchorHeadUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val liveWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val liveEndWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val c2cMsgWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val liveWordingType: Int = 0,
            @ProtoId(9) val endWordingType: Int = 0
        ) : ProtoBuf
    }
}


internal class Submsgtype0xcc {
    @Serializable
    class SubMsgType0xcc : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val uin: Long = 0L,
            @ProtoId(4) val unionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val subType: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val feedId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val coverUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xce {
    @Serializable
    class Submsgtype0xce : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val int64StartTime: Long = 0L,
            @ProtoId(2) val int64EndTime: Long = 0L,
            @ProtoId(3) val params: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xcf {
    @Serializable
    class Submsgtype0xcf : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val rsptype: Int = 0,
            @ProtoId(2) val rspbody: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xd0 {
    @Serializable
    class SubMsgType0xd0 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val hotTopicId: Long = 0L,
            @ProtoId(4) val hotTopicName: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val bigVId: Long = 0L,
            @ProtoId(6) val bigVUnionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val pgcType: Int = 0,
            @ProtoId(8) val pgcColumnUnionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val link: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val subType: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(11) val coverUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xd7 {
    @Serializable
    class SubMsgType0xd7 : ProtoBuf {
        @Serializable
        internal class Content(
            @ProtoId(1) val fromUser: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val plainText: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val buluoWord: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val richFreshWord: AppointDefine.RichText? = null
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val msgboxUnreadCount: Int = 0,
            @ProtoId(3) val unreadCount: Int = 0,
            @ProtoId(4) val msgContent: Content? = null,
            @ProtoId(5) val timestamp: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xda {
    @Serializable
    class SubMsgType0xda : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val msgInfo: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val subType: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val versionCtrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val feedId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val unionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val commentId: Int = 0,
            @ProtoId(8) val iconUnionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val coverUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(10) val operType: Int = 0,
            @ProtoId(11) val groupUnionid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(12) val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(13) val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(14) val fromNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(15) val vidUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(16) val extInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdb {
    @Serializable
    class Submsgtype0xdb : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val rsptype: Int = 0,
            @ProtoId(2) val rspbody: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdc {
    @Serializable
    class Submsgtype0xdc : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgList: List<MsgContent>? = null,
            @ProtoId(2) val msgType: Int = 0,
            @ProtoId(3) val msgList0x02: List<MsgContent>? = null,
            @ProtoId(4) val minQqVer: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgContent(
            @ProtoId(1) val masterPri: Long = 0L,
            @ProtoId(2) val subPri: Long = 0L,
            @ProtoId(3) val showTimes: Long = 0L,
            @ProtoId(4) val showBegTs: Long = 0L,
            @ProtoId(5) val expTs: Long = 0L,
            @ProtoId(6) val msgSentTs: Long = 0L,
            @ProtoId(7) val actionId: Long = 0L,
            @ProtoId(8) val wording: String = "",
            @ProtoId(9) val scheme: String = "",
            @ProtoId(10) val regionUrl: String = "",
            @ProtoId(11) val wordingColor: Long = 0L,
            @ProtoId(12) val msgId: Long = 0L,
            @ProtoId(13) val bubbleId: Long = 0L,
            @ProtoId(14) val tips: String = "",
            @ProtoId(15) val gameId: Long = 0L
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdd {
    @Serializable
    class Submsgtype0xdd : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val uint64InviteUin: List<Long>? = null,
            @ProtoId(3) val inviteLeader: Long = 0L,
            @ProtoId(4) val msgPoiInfo: WifiPOIInfo? = null,
            @ProtoId(5) val msgPlayerState: List<PlayerState>? = null
        ) : ProtoBuf {
            @Serializable
            internal class PlayerState(
                @ProtoId(1) val uin: Long = 0L,
                @ProtoId(2) val state: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class SeatsInfo(
                @ProtoId(1) val seatFlag: Int = 0,
                @ProtoId(2) val guestUin: Long = 0L,
                @ProtoId(3) val seatId: Int = 0,
                @ProtoId(4) val seatSeq: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class WifiPOIInfo(
                @ProtoId(1) val uid: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(2) val name: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(3) val faceId: Int = 0,
                @ProtoId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(5) val groupCode: Int = 0,
                @ProtoId(6) val groupUin: Int = 0,
                @ProtoId(7) val visitorNum: Int = 0,
                @ProtoId(8) val wifiPoiType: Int = 0,
                @ProtoId(9) val isMember: Int = 0,
                @ProtoId(10) val distance: Int = 0,
                @ProtoId(11) val msgTabSwitchOff: Int = 0,
                @ProtoId(12) val faceUrl: String = "",
                @ProtoId(13) val hotThemeGroupFlag: Int = 0,
                @ProtoId(14) val bannerUrl: String = "",
                @ProtoId(15) val specialFlag: Int = 0,
                @ProtoId(16) val totalNumLimit: Int = 0,
                @ProtoId(17) val isAdmin: Int = 0,
                @ProtoId(18) val joinGroupUrl: String = "",
                @ProtoId(19) val groupTypeFlag: Int = 0,
                @ProtoId(20) val createrCityId: Int = 0,
                @ProtoId(21) val isUserCreate: Int = 0,
                @ProtoId(22) val ownerUin: Long = 0L,
                @ProtoId(23) val auditFlag: Int = 0,
                @ProtoId(24) val tvPkFlag: Int = 0,
                @ProtoId(25) val subType: Int = 0,
                @ProtoId(26) val lastMsgSeq: Long = 0L,
                @ProtoId(27) val msgSeatsInfo: List<SeatsInfo>? = null,
                @ProtoId(28) val flowerNum: Long = 0L,
                @ProtoId(29) val flowerPoint: Long = 0L,
                @ProtoId(31) val favoritesTime: Long = 0L,
                @ProtoId(32) val favoritesExpired: Int = 0,
                @ProtoId(33) val groupId: Int = 0,
                @ProtoId(34) val praiseNums: Long = 0L,
                @ProtoId(35) val reportPraiseGapTime: Long = 0L,
                @ProtoId(36) val reportPraiseGapFrequency: Long = 0L,
                @ProtoId(37) val getPraiseGapTime: Long = 0L,
                @ProtoId(38) val vistorJoinGroupTime: Long = 0L,
                @ProtoId(39) val groupIsNotExist: Int = 0,
                @ProtoId(40) val guestNum: Int = 0,
                @ProtoId(41) val highQualityFlag: Int = 0,
                @ProtoId(42) val exitGroupCode: Long = 0L,
                @ProtoId(43) val int32Latitude: Int = 0,
                @ProtoId(44) val int32Longitude: Int = 0,
                @ProtoId(45) val smemo: String = "",
                @ProtoId(46) val isAllCountry: Int = 0
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0xde {
    @Serializable
    class Submsgtype0xde : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgType: Int = 0,
            @ProtoId(2) val unionId: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val uid: Long = 0L,
            @ProtoId(4) val vid: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val videoCover: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xdf {
    @Serializable
    class Submsgtype0xdf : ProtoBuf {
        @Serializable
        internal class MsgBody(
            // @ProtoId(1) val msgGameState: ApolloGameStatus.STCMGameMessage? = null,
            @ProtoId(2) val uint32UinList: List<Int>? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe0 {
    @Serializable
    class Submsgtype0xe0 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val pushExt: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe1 {
    @Serializable
    class Submsgtype0xe1 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val pushExt: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe4 {
    @Serializable
    class Submsgtype0xe4 : ProtoBuf {
        @Serializable
        internal class GeoInfo(
            @ProtoId(1) val latitude: Long = 0L,
            @ProtoId(2) val longitude: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class GiftMsg(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val toUin: Long = 0L,
            @ProtoId(3) val productId: Int = 0,
            @ProtoId(4) val giftId: Int = 0,
            @ProtoId(5) val giftNum: Long = 0L,
            @ProtoId(6) val roomid: String = "",
            @ProtoId(7) val giftWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(8) val packageurl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(50) val curAddDuration: Int = 0,
            @ProtoId(51) val allAddDuration: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class LikeMsg(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val toUin: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgMatchPlayer: Player? = null,
            @ProtoId(2) val distance: Int = 0,
            @ProtoId(3) val hint: String = "",
            @ProtoId(4) val countdown: Int = 0,
            @ProtoId(5) val key: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val type: Int = 0,
            @ProtoId(7) val callType: Int = 0,
            @ProtoId(8) val displayDistance: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(9) val msgLike: LikeMsg? = null,
            @ProtoId(10) val msgGift: GiftMsg? = null,
            @ProtoId(11) val msgRoom: Room? = null
        ) : ProtoBuf

        @Serializable
        internal class Player(
            @ProtoId(1) val uin: Long = 0L,
            @ProtoId(2) val nickname: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val logoUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val gender: Int = 0,
            @ProtoId(5) val level: Int = 0,
            @ProtoId(6) val age: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Room(
            @ProtoId(1) val roomId: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xe5 {
    @Serializable
    class Submsgtype0xe5 : ProtoBuf {
        @Serializable
        internal class CrmS2CMsgHead(
            @ProtoId(1) val crmSubCmd: Int = 0,
            @ProtoId(2) val headLen: Int = 0,
            @ProtoId(3) val verNo: Int = 0,
            @ProtoId(4) val kfUin: Long = 0L,
            @ProtoId(5) val seq: Int = 0,
            @ProtoId(6) val packNum: Int = 0,
            @ProtoId(7) val curPack: Int = 0,
            @ProtoId(8) val bufSig: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val subCmd: Int = 0,
            @ProtoId(2) val msgCrmCommonHead: CrmS2CMsgHead? = null,
            @ProtoId(3) val msgS2cCcAgentStatusChangePush: S2CCcAgentStatusChangePush? = null,
            @ProtoId(4) val msgS2cCcConfigChangePush: S2CCcConfigChangePush? = null,
            @ProtoId(5) val msgS2cCcExceptionOccurPush: S2CCcExceptionOccurPush? = null,
            @ProtoId(6) val msgS2cCcTalkingStatusChangePush: S2CCcTalkingStatusChangePush? = null,
            @ProtoId(7) val msgS2cCcAgentActionResultPush: S2CCcAgentActionResultPush? = null,
            @ProtoId(8) val msgS2cCallRecordChangePush: S2CCallRecordChangePush? = null,
            @ProtoId(9) val msgS2cSmsEventPush: S2CSMSEventPush? = null,
            @ProtoId(10) val msgS2cAgentCallStatusEventPush: S2CAgentCallStatusEventPush? = null,
            @ProtoId(11) val msgS2cUserGetCouponForKfextEventPush: S2CUserGetCouponForKFExtEventPush? = null,
            @ProtoId(12) val msgS2cUserGetCouponForCEventPush: S2CUserGetCouponForCEventPush? = null
        ) : ProtoBuf {
            @Serializable
            internal class S2CAgentCallStatusEventPush(
                @ProtoId(1) val type: Int = 0,
                @ProtoId(2) val callStatus: Int = 0,
                @ProtoId(3) val ringAsr: Int = 0,
                @ProtoId(4) val callid: String = "",
                @ProtoId(5) val fromKfext: Long = 0L,
                @ProtoId(6) val timestamp: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCallRecordChangePush(
                @ProtoId(1) val kfext: Long = 0L,
                @ProtoType(ProtoNumberType.FIXED) @ProtoId(2) val fixed64Timestamp: Long = 0L
            ) : ProtoBuf

            @Serializable
            internal class S2CCcAgentActionResultPush(
                @ProtoId(1) val type: Int = 0,
                @ProtoId(2) val callid: String = "",
                @ProtoId(3) val result: Int = 0,
                @ProtoId(4) val timestamp: Int = 0,
                @ProtoId(5) val status: Int = 0,
                @ProtoId(6) val targetName: ByteArray = EMPTY_BYTE_ARRAY,
                @ProtoId(7) val targetKfext: Long = 0L
            ) : ProtoBuf

            @Serializable
            internal class S2CCcAgentStatusChangePush(
                @ProtoId(1) val readyDevice: Int = 0,
                @ProtoId(2) val updateTime: Long = 0L,
                @ProtoId(3) val deviceSubState: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCcConfigChangePush(
                @ProtoId(1) val optype: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCcExceptionOccurPush(
                @ProtoId(1) val optype: Int = 0
            ) : ProtoBuf

            @Serializable
            internal class S2CCcTalkingStatusChangePush(
                @ProtoId(1) val talkingStatus: Int = 0,
                @ProtoId(2) val callid: String = ""
            ) : ProtoBuf

            @Serializable
            internal class S2CSMSEventPush(
                @ProtoId(1) val type: Int = 0,
                @ProtoId(2) val phoneNum: String = "",
                @ProtoId(3) val timestamp: Long = 0L,
                @ProtoId(4) val smsId: String = "",
                @ProtoId(5) val eventMsg: String = ""
            ) : ProtoBuf

            @Serializable
            internal class S2CUserGetCouponForCEventPush(
                @ProtoId(1) val qquin: Long = 0L,
                @ProtoId(2) val kfuin: Long = 0L,
                @ProtoId(3) val couponId: Long = 0L,
                @ProtoId(4) val timestamp: Int = 0,
                @ProtoId(5) val kfext: Long = 0L,
                @ProtoId(6) val tipsContent: String = ""
            ) : ProtoBuf

            @Serializable
            internal class S2CUserGetCouponForKFExtEventPush(
                @ProtoId(1) val channelType: Int = 0,
                @ProtoId(2) val fakeuin: Long = 0L,
                @ProtoId(3) val qquin: Long = 0L,
                @ProtoId(4) val openid: String = "",
                @ProtoId(5) val visitorid: String = "",
                @ProtoId(6) val appid: String = "",
                @ProtoId(7) val qqPubUin: Long = 0L,
                @ProtoId(8) val kfuin: Long = 0L,
                @ProtoId(9) val couponId: Long = 0L,
                @ProtoId(10) val notifyTips: String = "",
                @ProtoId(11) val timestamp: Int = 0,
                @ProtoId(12) val kfext: Long = 0L
            ) : ProtoBuf
        }
    }
}


internal class Submsgtype0xe8 {
    @Serializable
    class Submsgtype0xe8 : ProtoBuf {
        @Serializable
        internal class MsgBody/*(
             @ProtoId(1) val msgItem: ApolloPushMsgInfo.STPushMsgElem? = null
        )*/ : ProtoBuf
    }
}


internal class Submsgtype0xe9 {
    @Serializable
    class SubMsgType0xe9 : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val businessType: Int = 0,
            @ProtoId(2) val business: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xea {
    @Serializable
    class Submsgtype0xea : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val content: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xee {
    @Serializable
    class Submsgtype0xee : ProtoBuf {
        @Serializable
        internal class AccountInfo(
            @ProtoId(1) val id: Long = 0L,
            @ProtoId(2) val name: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val iconUrl: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ContextInfo(
            @ProtoId(1) val id: Long = 0L,
            @ProtoId(2) val title: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val msgPicList: List<PictureInfo>? = null,
            @ProtoId(4) val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val orangeWord: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val brief: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val enumContextType: Int /* enum */ = 0,
            @ProtoId(8) val videoBrief: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class ControlInfo(
            @ProtoId(1) val commentLength: Int = 0,
            @ProtoId(2) val showLine: Int = 0,
            @ProtoId(3) val fontSize: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class ExtraInfo(
            @ProtoId(1) val ext: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val cookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val id: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val seq: Long = 0L,
            @ProtoId(3) val bid: Int = 0,
            @ProtoId(11) val msgNotifyList: List<NotifyInfo>? = null
        ) : ProtoBuf

        @Serializable
        internal class NotifyInfo(
            @ProtoId(1) val msgStyleSheet: StyleSheet? = null,
            @ProtoId(2) val enumApppushType: Int /* enum */ = 0,
            @ProtoId(3) val msgOrdinaryPushInfo: OrdinaryPushInfo? = null,
            @ProtoId(4) val msgSocialPushInfo: SocialPushInfo? = null,
            @ProtoId(5) val msgUgcPushInfo: UGCPushInfo? = null,
            @ProtoId(11) val msgContextInfo: ContextInfo? = null,
            @ProtoId(12) val msgAccountInfo: AccountInfo? = null,
            @ProtoId(13) val msgStatisticsInfo: StatisticsInfo? = null,
            @ProtoId(14) val msgControlInfo: ControlInfo? = null,
            @ProtoId(21) val msgExtraInfo: ExtraInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class OrangeControlInfo(
            @ProtoId(1) val color: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val fontSize: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OrdinaryPushInfo(
            @ProtoId(1) val msgLabelControlInfo: OrangeControlInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class PictureInfo(
            @ProtoId(1) val url: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class SocialPushInfo(
            @ProtoId(1) val feedsId: Long = 0L,
            @ProtoId(2) val biuReason: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val biuComment: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class StatisticsInfo(
            @ProtoId(1) val algorithmId: Long = 0L,
            @ProtoId(2) val strategyId: Long = 0L,
            @ProtoId(3) val folderStatus: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class StyleSheet(
            @ProtoId(1) val enumStyleType: Int /* enum */ = 0,
            @ProtoId(2) val arkEnable: Int = 0,
            @ProtoId(3) val scene: Long = 0L,
            @ProtoId(11) val duration: Int = 0,
            @ProtoId(12) val endTime: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class UGCPushInfo(
            @ProtoId(1) val feedsId: Long = 0L,
            @ProtoId(2) val ugcReason: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}


internal class Submsgtype0xf9 {
    @Serializable
    class Submsgtype0xf9 : ProtoBuf {
        @Serializable
        internal class AdInfo(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val nick: String = "",
            @ProtoId(3) val headUrl: String = "",
            @ProtoId(4) val brief: String = "",
            @ProtoId(5) val action: String = "",
            @ProtoId(6) val flag: Int = 0,
            @ProtoId(7) val serviceID: Int = 0,
            @ProtoId(8) val templateID: Int = 0,
            @ProtoId(9) val url: String = "",
            @ProtoId(10) val msgMsgCommonData: MsgCommonData? = null,
            @ProtoId(11) val msgVideo: List<Video>? = null,
            @ProtoId(12) val pushTime: Int = 0,
            @ProtoId(13) val invalidTime: Int = 0,
            @ProtoId(14) val maxExposureTime: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val zipAdInfo: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgCommonData(
            @ProtoId(1) val adId: String = "",
            @ProtoId(2) val adPosId: String = "",
            @ProtoId(3) val boolBannerShow: Boolean = false,
            @ProtoId(4) val bannertype: Int = 0,
            @ProtoId(5) val jumpType: Int = 0,
            @ProtoId(6) val jumpUrl: String = "",
            @ProtoId(7) val appId: String = "",
            @ProtoId(8) val appName: String = "",
            @ProtoId(9) val packagename: String = "",
            @ProtoId(10) val androidDownloadUrl: String = "",
            @ProtoId(11) val scheme: String = "",
            @ProtoId(12) val iosDownloadUrl: String = "",
            @ProtoId(13) val bannerImgUrl: String = "",
            @ProtoId(14) val bannerText: String = "",
            @ProtoId(15) val bannerButtonText: String = "",
            @ProtoId(16) val boolSilentDownload: Boolean = false,
            @ProtoId(17) val audioSwitchType: Int = 0,
            @ProtoId(18) val preDownloadType: Int = 0,
            @ProtoId(19) val reportLink: String = "",
            @ProtoId(20) val boolHorizontalVideo: Boolean = false,
            @ProtoId(21) val audioFadeinDuration: Int = 0,
            @ProtoId(22) val openJumpUrlGuide: String = "",
            @ProtoId(23) val myappDownloadUrl: String = "",
            @ProtoId(24) val jumpTypeParams: String = "",
            @ProtoId(25) val scrollUpToJump: Int = 0,
            @ProtoId(26) val controlVariable: Int = 0,
            @ProtoId(27) val autoJump: Int = 0,
            @ProtoId(28) val clickLink: String = "",
            @ProtoId(29) val monitorType: Int = 0,
            @ProtoId(30) val shareNick: String = "",
            @ProtoId(31) val shareAdHeadUrl: String = "",
            @ProtoId(32) val shareAdBrief: String = "",
            @ProtoId(33) val shareAdTxt: String = "",
            @ProtoId(34) val shareAdIconUrl: String = "",
            @ProtoId(35) val shareJumpUrl: String = "",
            @ProtoId(36) val controlPluginTime: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Video(
            @ProtoId(1) val layout: Int = 0,
            @ProtoId(2) val cover: String = "",
            @ProtoId(3) val src: String = ""
        ) : ProtoBuf
    }
}


internal class Submsgtype0xfd {
    @Serializable
    class Submsgtype0xfd : ProtoBuf {
        @Serializable
        internal class AdInfo(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val adId: String = "",
            @ProtoId(3) val adSeq: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgBody(
            @ProtoId(1) val msgAdInfo: AdInfo? = null
        ) : ProtoBuf
    }
}


internal class Submsgtype0xfe {
    @Serializable
    class Submsgtype0xfe : ProtoBuf {
        @Serializable
        internal class MsgBody(
            @ProtoId(1) val wording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val innerUnreadNum: Int = 0,
            @ProtoId(3) val boxUnreadNum: Int = 0,
            @ProtoId(4) val updateTime: Int = 0
        ) : ProtoBuf
    }
}
        