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
    ) : ProtoBuf

    @Serializable
    internal class GuildNode(
        @ProtoNumber(1) @JvmField val guildId: Long = 0L,
        @ProtoNumber(2) @JvmField val guildCode: Long = 0L,
        @ProtoNumber(3) @JvmField val channelNodes: List<ChannelNode> = mutableListOf(),
        @ProtoNumber(4) @JvmField val guildName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val peerSource: DirectMessageSource = DirectMessageSource(),
    ) : ProtoBuf{
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
    ) : ProtoBuf{
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
    ) : ProtoBuf{
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
    ) : ProtoBuf

    @Serializable
    internal class ChannelMsgContent(
        @ProtoNumber(1) @JvmField val head: ChannelMsgHead = ChannelMsgHead(),
        @ProtoNumber(2) @JvmField val ctrlHead: ChannelMsgCtrlHead = ChannelMsgCtrlHead(),
        @ProtoNumber(3) @JvmField val body: MessageBody = MessageBody(),
        @ProtoNumber(4) @JvmField val extInfo: ChannelExtInfo = ChannelExtInfo(),
    ) : ProtoBuf


    @Serializable
    internal class ChannelMsgHead(
        @ProtoNumber(1) @JvmField val routingHead: ChannelRoutingHead = ChannelRoutingHead(),
        @ProtoNumber(2) @JvmField val contentHead: ChannelContentHead = ChannelContentHead(),
    ) : ProtoBuf

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

        ) : ProtoBuf

    @Serializable
    internal class PersonalLevel(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val level: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class MessageBody(
        @ProtoNumber(1) @JvmField val richText: ImMsgBody.RichText = ImMsgBody.RichText(),
        @ProtoNumber(2) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgEncryptContent: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

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
    ) : ProtoBuf

    @Serializable
    internal class ChannelEvent(
        @ProtoNumber(1) @JvmField val type: Long = 0L,
        @ProtoNumber(2) @JvmField val version: Long = 0L,
        @ProtoNumber(3) @JvmField val opInfo: ChannelMsgOpInfo = ChannelMsgOpInfo(),
    ) : ProtoBuf

    @Serializable
    internal class ChannelMsgOpInfo(
        @ProtoNumber(1) @JvmField val operatorTinyId: Long = 0L,
        @ProtoNumber(2) @JvmField val operatorRole: Long = 0L,
        @ProtoNumber(3) @JvmField val reason: Long = 0L,
        @ProtoNumber(4) @JvmField val timestamp: Long = 0L,
        @ProtoNumber(5) @JvmField val atType: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ChannelRole(
        @ProtoNumber(1) @JvmField val id: Long = 0L,
        @ProtoNumber(2) @JvmField val info: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val flag: Short = 0,
    ) : ProtoBuf

    @Serializable
    internal class ChannelFreqLimitInfo(
        @ProtoNumber(1) @JvmField val isLimited: Short = 0,
        @ProtoNumber(2) @JvmField val leftCount: Short = 0,
        @ProtoNumber(3) @JvmField val limitTimestamp: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class DirectMessageMember(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val tinyId: Long = 0L,
        @ProtoNumber(3) @JvmField val sourceGuildId: Long = 0L,
        @ProtoNumber(4) @JvmField val sourceGuildName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val nickName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val memberName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val notifyType: Short = 0,
    ) : ProtoBuf

    @Serializable
    internal class ChannelRoutingHead(
        @ProtoNumber(1) @JvmField val guildId: Long = 0L,
        @ProtoNumber(2) @JvmField val channelId: Long = 0L,
        @ProtoNumber(3) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(4) @JvmField val fromTinyId: Long = 0L,
        @ProtoNumber(5) @JvmField val guildCode: Long = 0L,
        @ProtoNumber(6) @JvmField val fromAppid: Long = 0L,
        @ProtoNumber(7) @JvmField val directMessageFlag: Short = 0,
    ) : ProtoBuf

    @Serializable
    internal class ChannelContentHead(
        @ProtoNumber(1) @JvmField val type: Long = 0L,
        @ProtoNumber(2) @JvmField val subType: Long = 0L,
        @ProtoNumber(3) @JvmField val random: Long = 0L,
        @ProtoNumber(4) @JvmField val seq: Long = 0L,
        @ProtoNumber(5) @JvmField val cntSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val time: Long = 0L,
        @ProtoNumber(7) @JvmField val meta: Long = 0L,
    ) : ProtoBuf
}