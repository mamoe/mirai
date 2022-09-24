/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import kotlin.jvm.JvmField


@Serializable
internal class Guild : ProtoBuf {
    @Serializable
    internal class FirstViewMsg(
        @ProtoNumber(1) @JvmField val pushFlag: Short = 0,
        @ProtoNumber(2) @JvmField val seq: Short = 0,
        @ProtoNumber(3) @JvmField val guildNodes: List<GuildNode> = mutableListOf(),
        @ProtoNumber(4) @JvmField val channelMsgs: List<ChannelMsg> = mutableListOf(),
        @ProtoNumber(5) @JvmField val getMsgTime: Long = 0L,
        @ProtoNumber(6) @JvmField val directMessageGuildNodes: List<GuildNode> = mutableListOf(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "FirstViewMsg(pushFlag=$pushFlag, seq=$seq, guildNodes=$guildNodes, channelMsgs=$channelMsgs, getMsgTime=$getMsgTime, directMessageGuildNodes=$directMessageGuildNodes)"
        }
    }

    @Serializable
    internal class GuildNode(
        @ProtoNumber(1) @JvmField val guildId: Long = 0L,
        @ProtoNumber(2) @JvmField val guildCode: Long = 0L,
        @ProtoNumber(3) @JvmField val channelNodes: List<ChannelNode> = mutableListOf(),
        @ProtoNumber(4) @JvmField val guildName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val peerSource: DirectMessageSource = DirectMessageSource(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "GuildNode(guildId=$guildId, guildCode=$guildCode, channelNodes=$channelNodes, guildName=${guildName.contentToString()}, peerSource=$peerSource)"
        }
    }

    @Serializable
    internal class ChannelNode(
        @ProtoNumber(1) @JvmField val channelId: Long = 0L,
        @ProtoNumber(2) @JvmField val seq: Long = 0L,
        @ProtoNumber(3) @JvmField val cntSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val time: Long = 0L,
        @ProtoNumber(5) @JvmField val memberReadMsgSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val memberReadCntSeq: Long = 0L,
        @ProtoNumber(7) @JvmField val notifyType: Short = 0,
        @ProtoNumber(8) @JvmField val channelName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val channelType: Long = 0L,
        @ProtoNumber(10) @JvmField val meta: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val readMsgMeta: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val eventTime: Short = 0,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelNode(channelId=$channelId, seq=$seq, cntSeq=$cntSeq, time=$time, memberReadMsgSeq=$memberReadMsgSeq, memberReadCntSeq=$memberReadCntSeq, notifyType=$notifyType, channelName=${channelName.contentToString()}, channelType=$channelType, meta=${meta.contentToString()}, readMsgMeta=${readMsgMeta.contentToString()}, eventTime=$eventTime)"
        }
    }

    @Serializable
    internal class DirectMessageSource(
        @ProtoNumber(1) @JvmField val tinyId: Long = 0L,
        @ProtoNumber(2) @JvmField val guildId: Long = 0L,
        @ProtoNumber(3) @JvmField val guildName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val memberName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val nickName: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf {
        override fun toString(): String {
            return "DirectMessageSource(tinyId=$tinyId, guildId=$guildId, guildName=${guildName.contentToString()}, memberName=${memberName.contentToString()}, nickName=${nickName.contentToString()})"
        }
    }

    @Serializable
    internal class ChannelMsg(
        @ProtoNumber(1) @JvmField val guildId: Long = 0L,
        @ProtoNumber(2) @JvmField val channelId: Long = 0L,
        @ProtoNumber(3) @JvmField val result: Short = 0,
        @ProtoNumber(4) @JvmField val rspBeginSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val rspEndSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val msgs: List<ChannelMsgContent> = mutableListOf(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelMsg(guildId=$guildId, channelId=$channelId, result=$result, rspBeginSeq=$rspBeginSeq, rspEndSeq=$rspEndSeq, msgs=$msgs)"
        }
    }

    @Serializable
    internal class ChannelMsgContent(
        @ProtoNumber(1) @JvmField val head: ChannelMsgHead,
        @ProtoNumber(2) @JvmField val ctrlHead: ChannelMsgCtrlHead,
        @ProtoNumber(3) @JvmField val body: MessageBody,
        @ProtoNumber(4) @JvmField val extInfo: ChannelExtInfo,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelMsgContent(head=$head, ctrlHead=$ctrlHead, body=$body, extInfo=$extInfo)"
        }
    }


    @Serializable
    internal class ChannelMsgHead(
        @ProtoNumber(1) @JvmField val routingHead: ChannelRoutingHead,
        @ProtoNumber(2) @JvmField val contentHead: ChannelContentHead,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelMsgHead(routingHead=$routingHead, contentHead=$contentHead)"
        }
    }

    @Serializable
    internal class ChannelMsgCtrlHead(
        @ProtoNumber(1) @JvmField val includeUin: List<ByteArray> = mutableListOf(),
//        @ProtoNumber(2) @JvmField val excludeUin: Long,//bytes?
//        @ProtoNumber(3) @JvmField val featureid: Long,
        @ProtoNumber(4) @JvmField val offlineFlag: Short = 0,
        @ProtoNumber(5) @JvmField val visibility: Short = 0,
        @ProtoNumber(6) @JvmField val ctrlFlag: Long = 0L,
        @ProtoNumber(7) @JvmField val events: List<ChannelEvent> = mutableListOf(),
        @ProtoNumber(8) @JvmField val level: Short = 0,
        @ProtoNumber(9) @JvmField val personalLevels: List<PersonalLevel> = mutableListOf(),
        @ProtoNumber(10) @JvmField val guildSyncSeq: Long = 0L,
        @ProtoNumber(11) @JvmField val memberNum: Short = 0,
        @ProtoNumber(12) @JvmField val channelType: Short = 0,
        @ProtoNumber(13) @JvmField val privateType: Short = 0,

        ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelMsgCtrlHead(includeUin=$includeUin, offlineFlag=$offlineFlag, visibility=$visibility, ctrlFlag=$ctrlFlag, events=$events, level=$level, personalLevels=$personalLevels, guildSyncSeq=$guildSyncSeq, memberNum=$memberNum, channelType=$channelType, privateType=$privateType)"
        }
    }

    @Serializable
    internal class PersonalLevel(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val level: Long = 0L,
    ) : ProtoBuf {
        override fun toString(): String {
            return "PersonalLevel(toUin=$toUin, level=$level)"
        }
    }

    @Serializable
    internal class MessageBody(
        @ProtoNumber(1) @JvmField val richText: ImMsgBody.RichText? = null,
        @ProtoNumber(2) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgEncryptContent: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf {
        override fun toString(): String {
            return "MessageBody(richText=$richText, msgContent=${msgContent.decodeToString()}, msgEncryptContent=${msgEncryptContent.decodeToString()})"
        }
    }

    @Serializable
    internal class ChannelExtInfo(
        @ProtoNumber(1) @JvmField val fromNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val guildName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val channelName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val visibility: Short = 0,
        @ProtoNumber(5) @JvmField val notifyType: Short = 0,
        @ProtoNumber(6) @JvmField val offlineFlag: Short = 0,
        @ProtoNumber(7) @JvmField val nameType: Short = 0,
        @ProtoNumber(8) @JvmField val memberName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val timestamp: Short = 0,
        @ProtoNumber(10) @JvmField val eventVersion: Long = 0L,
        @ProtoNumber(11) @JvmField val events: List<ChannelEvent> = mutableListOf(),
        @ProtoNumber(12) @JvmField val fromRoleInfo: ChannelRole = ChannelRole(),
        @ProtoNumber(13) @JvmField val freqLimitInfo: ChannelFreqLimitInfo = ChannelFreqLimitInfo(),
        @ProtoNumber(14) @JvmField val directMessageMember: List<DirectMessageMember> = mutableListOf(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelExtInfo(fromNick=${fromNick.decodeToString()}, guildName=${guildName.decodeToString()}, channelName=${channelName.decodeToString()}, visibility=$visibility, notifyType=$notifyType, offlineFlag=$offlineFlag, nameType=$nameType, memberName=${memberName.decodeToString()}, timestamp=$timestamp, eventVersion=$eventVersion, events=$events, fromRoleInfo=$fromRoleInfo, freqLimitInfo=$freqLimitInfo, directMessageMember=$directMessageMember)"
        }
    }

    @Serializable
    internal class ChannelEvent(
        @ProtoNumber(1) @JvmField val type: Long = 0L,
        @ProtoNumber(2) @JvmField val version: Long = 0L,
        @ProtoNumber(3) @JvmField val opInfo: ChannelMsgOpInfo = ChannelMsgOpInfo(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelEvent(type=$type, version=$version, opInfo=$opInfo)"
        }
    }

    @Serializable
    internal class ChannelMsgOpInfo(
        @ProtoNumber(1) @JvmField val operatorTinyId: Long = 0L,
        @ProtoNumber(2) @JvmField val operatorRole: Long = 0L,
        @ProtoNumber(3) @JvmField val reason: Long = 0L,
        @ProtoNumber(4) @JvmField val timestamp: Long = 0L,
        @ProtoNumber(5) @JvmField val atType: Long = 0L,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelMsgOpInfo(operatorTinyId=$operatorTinyId, operatorRole=$operatorRole, reason=$reason, timestamp=$timestamp, atType=$atType)"
        }
    }

    @Serializable
    internal class ChannelRole(
        @ProtoNumber(1) @JvmField val id: Long = 0L,
        @ProtoNumber(2) @JvmField val info: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val flag: Short = 0,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelRole(id=$id, info=${info.contentToString()}, flag=$flag)"
        }
    }

    @Serializable
    internal class ChannelFreqLimitInfo(
        @ProtoNumber(1) @JvmField val isLimited: Short = 0,
        @ProtoNumber(2) @JvmField val leftCount: Short = 0,
        @ProtoNumber(3) @JvmField val limitTimestamp: Long = 0L,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelFreqLimitInfo(isLimited=$isLimited, leftCount=$leftCount, limitTimestamp=$limitTimestamp)"
        }
    }

    @Serializable
    internal class DirectMessageMember(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val tinyId: Long = 0L,
        @ProtoNumber(3) @JvmField val sourceGuildId: Long = 0L,
        @ProtoNumber(4) @JvmField val sourceGuildName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val nickName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val memberName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val notifyType: Short = 0,
    ) : ProtoBuf {
        override fun toString(): String {
            return "DirectMessageMember(uin=$uin, tinyId=$tinyId, sourceGuildId=$sourceGuildId, sourceGuildName=${sourceGuildName.contentToString()}, nickName=${nickName.contentToString()}, memberName=${memberName.contentToString()}, notifyType=$notifyType)"
        }
    }

    @Serializable
    internal class ChannelRoutingHead(
        @ProtoNumber(1) @JvmField val guildId: Long = 0L,
        @ProtoNumber(2) @JvmField val channelId: Long = 0L,
        @ProtoNumber(3) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(4) @JvmField val fromTinyId: Long = 0L,
        @ProtoNumber(5) @JvmField val guildCode: Long = 0L,
        @ProtoNumber(6) @JvmField val fromAppid: Long = 0L,
        @ProtoNumber(7) @JvmField val directMessageFlag: Short = 0,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelRoutingHead(guildId=$guildId, channelId=$channelId, fromUin=$fromUin, fromTinyId=$fromTinyId, guildCode=$guildCode, fromAppid=$fromAppid, directMessageFlag=$directMessageFlag)"
        }
    }

    @Serializable
    internal class ChannelContentHead(
        @ProtoNumber(1) @JvmField val type: Long = 0L,
        @ProtoNumber(2) @JvmField val subType: Long = 0L,
        @ProtoNumber(3) @JvmField val random: Long = 0L,
        @ProtoNumber(4) @JvmField val seq: Long = 0L,
        @ProtoNumber(5) @JvmField val cntSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val time: Long = 0L,
        @ProtoNumber(7) @JvmField val meta: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ChannelContentHead(type=$type, subType=$subType, random=$random, seq=$seq, cntSeq=$cntSeq, time=$time, meta=${meta.decodeToString()})"
        }
    }

    @Serializable
    internal class GuildChannelInfo(
        @ProtoNumber(1) @JvmField val channelId: Long = 0L,
        @ProtoNumber(2) @JvmField val channelName: String = "",
        @ProtoNumber(3) @JvmField val creatorUin: Long = 0L,
        @ProtoNumber(4) @JvmField val createTime: Long = 0L,
        @ProtoNumber(5) @JvmField val guildId: Long = 0L,
        @ProtoNumber(6) @JvmField val finalNotifyType: Short = 0,
        @ProtoNumber(7) @JvmField val channelType: Short = 0,
        @ProtoNumber(8) @JvmField val talkPermission: Short = 0,
        //11-14 : MsgInfo
        @ProtoNumber(15) @JvmField val creatorTinyId: Long = 0L,
        @ProtoNumber(22) @JvmField val visibleType: Short = 0,
        @ProtoNumber(28) @JvmField val topMsg: GuildChannelTopMsgInfo = GuildChannelTopMsgInfo(),
        @ProtoNumber(31) @JvmField val currentSlowModeKey: Short = 0,
        @ProtoNumber(32) @JvmField val slowModeInfos: List<GuildChannelSlowModeInfo> = mutableListOf(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "GuildChannelInfo(channelId=$channelId, channelName='$channelName', creatorUin=$creatorUin, createTime=$createTime, guildId=$guildId, finalNotifyType=$finalNotifyType, channelType=$channelType, talkPermission=$talkPermission, creatorTinyId=$creatorTinyId, visibleType=$visibleType, topMsg=$topMsg, currentSlowModeKey=$currentSlowModeKey, slowModeInfos=$slowModeInfos)"
        }
    }

    @Serializable
    internal class GuildChannelTopMsgInfo(
        @ProtoNumber(1) @JvmField val topMsgSeq: Long = 0L,
        @ProtoNumber(2) @JvmField val topMsgTime: Long = 0L,
        @ProtoNumber(3) @JvmField val topMsgOperatorTinyId: Long = 0L,
    ) : ProtoBuf {
        override fun toString(): String {
            return "GuildChannelTopMsgInfo(topMsgSeq=$topMsgSeq, topMsgTime=$topMsgTime, topMsgOperatorTinyId=$topMsgOperatorTinyId)"
        }
    }

    @Serializable
    internal class GuildChannelSlowModeInfo(
        @ProtoNumber(1) @JvmField val slowModeKey: Long = 0L,
        @ProtoNumber(2) @JvmField val speakFrequency: Long = 0L,
        @ProtoNumber(3) @JvmField val slowModeCircle: Long = 0L,
        @ProtoNumber(4) @JvmField val slowModeText: String = "",
    ) : ProtoBuf {
        override fun toString(): String {
            return "GuildChannelSlowModeInfo(slowModeKey=$slowModeKey, speakFrequency=$speakFrequency, slowModeCircle=$slowModeCircle, slowModeText='$slowModeText')"
        }
    }

    @Serializable
    internal class EventBody(
        @ProtoNumber(1) @JvmField val readNotify: ReadNotify? = null,
        @ProtoNumber(2) @JvmField val commGrayTips: CommGrayTips? = null,
        @ProtoNumber(3) @JvmField val createGuild: CreateGuild? = null,
        @ProtoNumber(4) @JvmField val destroyGuild: DestroyGuild? = null,
        @ProtoNumber(5) @JvmField val joinGuild: JoinGuild? = null,
        @ProtoNumber(6) @JvmField val kickOffGuild: KickOffGuild? = null,
        @ProtoNumber(7) @JvmField val quitGuild: QuitGuild? = null,
        @ProtoNumber(8) @JvmField val changeGuildInfo: ChangeGuildInfo? = null,
        @ProtoNumber(9) @JvmField val createChan: CreateChan? = null,
        @ProtoNumber(10) @JvmField val destroyChan: DestroyChan? = null,
        @ProtoNumber(11) @JvmField val changeChanInfo: ChangeChanInfo? = null,
        @ProtoNumber(12) @JvmField val setAdmin: SetAdmin? = null,
        @ProtoNumber(13) @JvmField val setMsgRecvType: SetMsgRecvType? = null,
        @ProtoNumber(14) @JvmField val updateMsg: UpdateMsg? = null,
        @ProtoNumber(17) @JvmField val setTop: SetTop? = null,
        @ProtoNumber(18) @JvmField val switchVoiceChannel: SwitchVoiceChannel? = null,
        @ProtoNumber(21) @JvmField val updateCategory: UpdateCategory? = null,
        @ProtoNumber(22) @JvmField val updateVoiceBlockList: UpdateVoiceBlockList? = null,
        @ProtoNumber(23) @JvmField val setMute: SetMute? = null,
        @ProtoNumber(24) @JvmField val liveStatusChangeRoom: LiveRoomStatusChangeMsg? = null,
        @ProtoNumber(25) @JvmField val switchLiveRoom: SwitchLiveRoom? = null,
        @ProtoNumber(39) @JvmField val msgEvent: List<MsgEvent>? = null,
        @ProtoNumber(40) @JvmField val schedulerMsg: SchedulerMsg? = null,
        @ProtoNumber(41) @JvmField val appChannel: AppChannelMsg? = null,
        @ProtoNumber(44) @JvmField val feedEvent: FeedEvent? = null,
        @ProtoNumber(46) @JvmField val weakMsgAppChannel: AppChannelMsg? = null,
        @ProtoNumber(48) @JvmField val readFeedNotify: ReadFeedNotify? = null,
    ) : ProtoBuf

    @Serializable
    internal class CommGrayTips(
        @ProtoNumber(1) @JvmField val busiType: Long? = null,
        @ProtoNumber(2) @JvmField val busiId: Long? = null,
        @ProtoNumber(3) @JvmField val ctrlFlag: Short? = null,
        @ProtoNumber(4) @JvmField val tempId: Long? = null,
        @ProtoNumber(5) @JvmField val templParam: List<TemplParam>? = null,

        @ProtoNumber(6) @JvmField val content: ByteArray? = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val tipsSeqId: Long? = null,
        @ProtoNumber(100) @JvmField val pbReserv: ByteArray? = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class TemplParam(
        @ProtoNumber(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class DestroyGuild(
        @ProtoNumber(1) @JvmField val operatorId: Long? = null,
        @ProtoNumber(2) @JvmField val guildId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class JoinGuild(
        @ProtoNumber(3) @JvmField val memberId: Long? = null,
        @ProtoNumber(4) @JvmField val memberType: Short? = null,
        @ProtoNumber(5) @JvmField val memberTinyId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class KickOffGuild(
        @ProtoNumber(3) @JvmField val memberId: Long? = null,
        @ProtoNumber(4) @JvmField val setBlack: Short? = null,
        @ProtoNumber(5) @JvmField val memberTinyId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class QuitGuild(
    ) : ProtoBuf

    @Serializable
    internal class ChangeGuildInfo(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val operatorId: Long? = null,
        @ProtoNumber(3) @JvmField val infoSeq: MsgSeq? = null,
        @ProtoNumber(4) @JvmField val faceSeq: MsgSeq? = null,
        @ProtoNumber(5) @JvmField val updateType: Short? = null,
        @ProtoNumber(6) @JvmField val guildInfoFilter: GuildInfoFilter? = null,
        @ProtoNumber(7) @JvmField val guildInfo: GuildInfo? = null,
    ) : ProtoBuf

    @Serializable
    internal class GuildInfo(
        @ProtoNumber(2) @JvmField val guildCode: Long? = null,
        @ProtoNumber(3) @JvmField val ownerId: Long? = null,
        @ProtoNumber(4) @JvmField val createTime: Long? = null,
        @ProtoNumber(5) @JvmField val memberMaxNum: Short? = null,
        @ProtoNumber(6) @JvmField val memberMax: Short? = null,
        @ProtoNumber(7) @JvmField val guildType: Short? = null,
        @ProtoNumber(8) @JvmField val guildName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val robotList: List<Long>? = null,
        @ProtoNumber(10) @JvmField val adminList: List<Long>? = null,
        @ProtoNumber(11) @JvmField val robotMaxNum: Short? = null,
        @ProtoNumber(12) @JvmField val adminMaxNum: Short? = null,
        @ProtoNumber(13) @JvmField val profile: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val faceSeq: Long? = null,
        @ProtoNumber(15) @JvmField val guildStatus: GroupProStatus? = null,
        @ProtoNumber(16) @JvmField val channelNum: Short? = null,
        @ProtoNumber(5002) @JvmField val memberChangeSeq: MsgSeq? = null,
        @ProtoNumber(5003) @JvmField val guildInfoChangeSeq: MsgSeq? = null,
        @ProtoNumber(5004) @JvmField val channelChangeSeq: MsgSeq? = null,
    ) : ProtoBuf

    @Serializable
    internal class GroupProStatus(
        @ProtoNumber(1) @JvmField val isEnable: Short? = null,
        @ProtoNumber(2) @JvmField val isBanned: Short? = null,
        @ProtoNumber(3) @JvmField val isFrozen: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class GuildInfoFilter(
        @ProtoNumber(2) @JvmField val guildCode: Short? = null,
        @ProtoNumber(3) @JvmField val ownerId: Short? = null,
        @ProtoNumber(4) @JvmField val createTime: Short? = null,
        @ProtoNumber(5) @JvmField val memberMaxNum: Short? = null,
        @ProtoNumber(6) @JvmField val memberNum: Short? = null,
        @ProtoNumber(7) @JvmField val guildType: Short? = null,
        @ProtoNumber(8) @JvmField val guildName: Short? = null,
        @ProtoNumber(9) @JvmField val robotList: Short? = null,
        @ProtoNumber(10) @JvmField val adminList: Short? = null,
        @ProtoNumber(11) @JvmField val robotMaxNum: Short? = null,
        @ProtoNumber(12) @JvmField val adminMaxNum: Short? = null,
        @ProtoNumber(13) @JvmField val profile: Short? = null,
        @ProtoNumber(14) @JvmField val faceSeq: Short? = null,
        @ProtoNumber(15) @JvmField val guildStatus: Short? = null,
        @ProtoNumber(16) @JvmField val channelNum: Short? = null,
        @ProtoNumber(5002) @JvmField val memberChangeSeq: Short? = null,
        @ProtoNumber(5003) @JvmField val guildInfoChangeSeq: Short? = null,
        @ProtoNumber(5004) @JvmField val channelChangeSeq: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class CreateChan(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(3) @JvmField val operatorId: Long? = null,
        @ProtoNumber(4) @JvmField val createId: List<ChannelId>? = null,
    ) : ProtoBuf

    @Serializable
    internal class ChangeChanInfo(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
        @ProtoNumber(3) @JvmField val operatorId: Long? = null,
        @ProtoNumber(4) @JvmField val infoSeq: MsgSeq? = null,
        @ProtoNumber(5) @JvmField val updateType: Short? = null,
        @ProtoNumber(6) @JvmField val chanInfoFilter: ChanInfoFilter? = null,
        @ProtoNumber(7) @JvmField val channelInfo: ServChannelInfo? = null,
    ) : ProtoBuf

    @Serializable
    internal class ChanInfoFilter(
        @ProtoNumber(2) @JvmField val channelName: Short? = null,
        @ProtoNumber(3) @JvmField val creatorId: Short? = null,
        @ProtoNumber(4) @JvmField val createTime: Short? = null,
        @ProtoNumber(5) @JvmField val guildId: Short? = null,
        @ProtoNumber(6) @JvmField val msgNotifyType: Short? = null,
        @ProtoNumber(7) @JvmField val channelType: Short? = null,
        @ProtoNumber(8) @JvmField val speakPermission: Short? = null,
        @ProtoNumber(11) @JvmField val lastMsgSeq: Short? = null,
        @ProtoNumber(12) @JvmField val lastCntMsgSeq: Short? = null,
        @ProtoNumber(14) @JvmField val voiceChannelInfoFilter: VoiceChannelInfoFilter? = null,
        @ProtoNumber(15) @JvmField val liveChannelInfoFilter: LiveChannelInfoFilter? = null,
        @ProtoNumber(16) @JvmField val bannedSpeak: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class LiveChannelInfoFilter(
        @ProtoNumber(1) @JvmField val isNeedRoomId: Short? = null,
        @ProtoNumber(2) @JvmField val isNeedAnchorUin: Short? = null,
        @ProtoNumber(3) @JvmField val isNeedName: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class VoiceChannelInfoFilter(
        @ProtoNumber(1) @JvmField val memberMaxNum: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class ServChannelInfo(
        @ProtoNumber(2) @JvmField val channelName: Short? = null,
        @ProtoNumber(3) @JvmField val creatorId: Short? = null,
        @ProtoNumber(4) @JvmField val createTime: Short? = null,
        @ProtoNumber(5) @JvmField val guildId: Short? = null,
        @ProtoNumber(6) @JvmField val msgNotifyType: Short? = null,
        @ProtoNumber(7) @JvmField val channelType: Short? = null,
        @ProtoNumber(8) @JvmField val speakPermission: Short? = null,
        @ProtoNumber(11) @JvmField val lastMsgSeq: Short? = null,
        @ProtoNumber(12) @JvmField val lastCntMsgSeq: Short? = null,
        @ProtoNumber(14) @JvmField val voiceChannelInfo: VoiceChannelInfo? = null,
        @ProtoNumber(15) @JvmField val liveChannelInfo: LiveChannelInfo? = null,
        @ProtoNumber(16) @JvmField val bannedSpeak: Short? = null,

        ) : ProtoBuf

    @Serializable
    internal class VoiceChannelInfo(
        @ProtoNumber(1) @JvmField val memberMaxNum: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class LiveChannelInfo(
        @ProtoNumber(1) @JvmField val roomId: Long? = null,
        @ProtoNumber(2) @JvmField val anchorUin: Long? = null,
        @ProtoNumber(3) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class DestroyChan(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(3) @JvmField val operatorId: Long? = null,
        @ProtoNumber(4) @JvmField val deleteId: List<ChannelId>? = null,
    ) : ProtoBuf

    @Serializable
    internal class ChannelId(
        @ProtoNumber(1) @JvmField val channelId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class SetAdmin(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
        @ProtoNumber(3) @JvmField val operatorId: Long? = null,
        @ProtoNumber(4) @JvmField val adminId: Long? = null,
        @ProtoNumber(5) @JvmField val adminTinyId: Long? = null,
        @ProtoNumber(6) @JvmField val operateType: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class SetMsgRecvType(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
        @ProtoNumber(3) @JvmField val operatorId: Long? = null,
        @ProtoNumber(4) @JvmField val msgNotifyType: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class UpdateMsg(
        @ProtoNumber(1) @JvmField val msgSeq: Long? = null,
        @ProtoNumber(2) @JvmField val origMsgUncountable: Boolean? = null,
        @ProtoNumber(3) @JvmField val eventType: Long? = null,
        @ProtoNumber(4) @JvmField val eventVersion: Long? = null,
        @ProtoNumber(5) @JvmField val operatorTinyId: Long? = null,
        @ProtoNumber(6) @JvmField val operatorRole: Long? = null,
        @ProtoNumber(7) @JvmField val reason: Long? = null,
        @ProtoNumber(8) @JvmField val timestamp: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class SetTop(
        @ProtoNumber(1) @JvmField val action: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class SwitchVoiceChannel(
        @ProtoNumber(1) @JvmField val memberId: Long? = null,
        @ProtoNumber(2) @JvmField val enterDetail: SwitchDetail? = null,
        @ProtoNumber(3) @JvmField val leaveDetail: SwitchDetail? = null,
    ) : ProtoBuf

    @Serializable
    internal class SwitchDetail(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
        @ProtoNumber(3) @JvmField val platform: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class UpdateCategory(
        @ProtoNumber(1) @JvmField val categoryInfo: List<CategoryInfo>? = null,
        @ProtoNumber(2) @JvmField val noClassifyCategoryInfo: CategoryInfo? = null,
    ) : ProtoBuf

    @Serializable
    internal class CategoryInfo(
        @ProtoNumber(1) @JvmField val categoryIndex: Short? = null,
        @ProtoNumber(2) @JvmField val channelInfo: List<CategoryChannelInfo>? = null,
        @ProtoNumber(3) @JvmField val categoryName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val categoryId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class CategoryChannelInfo(
        @ProtoNumber(1) @JvmField val channelIndex: Short? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class UpdateVoiceBlockList(
        @ProtoNumber(1) @JvmField val action: Short? = null,
        @ProtoNumber(2) @JvmField val objectTinyId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class SetMute(
        @ProtoNumber(1) @JvmField val action: Short? = null,
        @ProtoNumber(2) @JvmField val tinyId: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class LiveRoomStatusChangeMsg(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
        @ProtoNumber(3) @JvmField val roomId: Long? = null,
        @ProtoNumber(4) @JvmField val anchorTinyId: Long? = null,
        @ProtoNumber(5) @JvmField val action: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class SwitchLiveRoom(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
//        @ProtoNumber(3) @JvmField val roomId: Long? = null,
//        @ProtoNumber(4) @JvmField val tinyId: Long? = null,
        @ProtoNumber(3) @JvmField val userInfo: SwitchLiveRoomUserInfo? = null,
        @ProtoNumber(4) @JvmField val action: Short? = null,
    ) : ProtoBuf

    @Serializable
    internal class SwitchLiveRoomUserInfo(
        @ProtoNumber(1) @JvmField val tinyId: Long? = null,
        @ProtoNumber(2) @JvmField val nickName: String? = null,
    ) : ProtoBuf

    @Serializable
    internal class MsgEvent(
        @ProtoNumber(1) @JvmField val seq: Long? = null,
        @ProtoNumber(2) @JvmField val eventType: Long? = null,
        @ProtoNumber(3) @JvmField val eventVersion: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class SchedulerMsg(
        @ProtoNumber(1) @JvmField val creatorHeadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val wording: String? = null,
        @ProtoNumber(3) @JvmField val expireTimeMs: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class FeedEvent(
        @ProtoNumber(1) @JvmField val guildId: Long? = null,
        @ProtoNumber(2) @JvmField val channelId: Long? = null,
        @ProtoNumber(3) @JvmField val feedId: String? = null,
        @ProtoNumber(4) @JvmField val msgSummary: String? = null,
        @ProtoNumber(5) @JvmField val eventTime: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class AppChannelMsg(
        @ProtoNumber(1) @JvmField val summary: String? = null,
        @ProtoNumber(2) @JvmField val msg: String? = null,
        @ProtoNumber(3) @JvmField val expireTimeMs: Long? = null,
        @ProtoNumber(4) @JvmField val schemaType: Short? = null,
        @ProtoNumber(5) @JvmField val schema: String? = null,
    ) : ProtoBuf

    @Serializable
    internal class ReadFeedNotify(
        @ProtoNumber(2) @JvmField val reportTime: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class ReadNotify(
        @ProtoNumber(1) @JvmField val channelId: Long? = null,
        @ProtoNumber(2) @JvmField val guildId: Long? = null,
        @ProtoNumber(3) @JvmField val readMsgSeq: MsgSeq? = null,
        @ProtoNumber(4) @JvmField val readCntMsgSeq: MsgSeq? = null,
        @ProtoNumber(5) @JvmField val readMsgMeta: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class MsgSeq(
        @ProtoNumber(1) @JvmField val seq: Long? = null,
        @ProtoNumber(2) @JvmField val time: Long? = null,
    ) : ProtoBuf

    @Serializable
    internal class CreateGuild(
        @ProtoNumber(1) @JvmField val operatorId: Long? = null,
        @ProtoNumber(2) @JvmField val guildId: Long? = null,
    ) : ProtoBuf
}