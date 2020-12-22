/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

internal class HummerCommelem : ProtoBuf {
    @Serializable
    internal class MsgElemInfoServtype1(
        @ProtoNumber(1) @JvmField val rewardId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val senderUin: Long = 0L,
        @ProtoNumber(3) @JvmField val picType: Int = 0,
        @ProtoNumber(4) @JvmField val rewardMoney: Int = 0,
        @ProtoNumber(5) @JvmField val url: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val createTimestamp: Int = 0,
        @ProtoNumber(8) @JvmField val status: Int = 0,
        @ProtoNumber(9) @JvmField val size: Int = 0,
        @ProtoNumber(10) @JvmField val videoDuration: Int = 0,
        @ProtoNumber(11) @JvmField val seq: Long = 0L,
        @ProtoNumber(12) @JvmField val rewardTypeExt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype11(
        @ProtoNumber(1) @JvmField val resID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val resMD5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val reserveInfo1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val reserveInfo2: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val doodleDataOffset: Int = 0,
        @ProtoNumber(6) @JvmField val doodleGifId: Int = 0,
        @ProtoNumber(7) @JvmField val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val doodleMd5: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype13(
        @ProtoNumber(1) @JvmField val sysHeadId: Int = 0,
        @ProtoNumber(2) @JvmField val headFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype14(
        @ProtoNumber(1) @JvmField val id: Int = 0,
        @ProtoNumber(2) @JvmField val reserveInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype15(
        @ProtoNumber(1) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val cover: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val summary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val createTime: Long = 0L,
        @ProtoNumber(6) @JvmField val commentContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val author: Long = 0L,
        @ProtoNumber(8) @JvmField val ctrVersion: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype16(
        @ProtoNumber(1) @JvmField val uid: Long = 0L,
        @ProtoNumber(2) @JvmField val unionID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val storyID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val thumbUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val videoWidth: Int = 0,
        @ProtoNumber(8) @JvmField val videoHeight: Int = 0,
        @ProtoNumber(9) @JvmField val sourceName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val sourceActionType: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val sourceActionData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val ctrVersion: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype18(
        @ProtoNumber(1) @JvmField val currentAmount: Long = 0L,
        @ProtoNumber(2) @JvmField val totalAmount: Long = 0L,
        @ProtoNumber(3) @JvmField val listid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val authKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val number: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype19(
        @ProtoNumber(1) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype2(
        @ProtoNumber(1) @JvmField val pokeType: Int = 0,
        @ProtoNumber(2) @JvmField val pokeSummary: String = "",
        @ProtoNumber(3) @JvmField val doubleHit: Int = 0,
        @ProtoNumber(4) @JvmField val vaspokeId: Int = 0,
        @ProtoNumber(5) @JvmField val vaspokeName: String = "",
        @ProtoNumber(6) @JvmField val vaspokeMinver: String = "",
        @ProtoNumber(7) @JvmField val pokeStrength: Int = 0,
        @ProtoNumber(8) @JvmField val msgType: Int = 0,
        @ProtoNumber(9) @JvmField val faceBubbleCount: Int = 0,
        @ProtoNumber(10) @JvmField val pokeFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype20(
        @ProtoNumber(1) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype21(
        @ProtoNumber(1) @JvmField val topicId: Int = 0,
        @ProtoNumber(2) @JvmField val confessorUin: Long = 0L,
        @ProtoNumber(3) @JvmField val confessorNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val confessorSex: Int = 0,
        @ProtoNumber(5) @JvmField val sysmsgFlag: Int = 0,
        @ProtoNumber(6) @JvmField val c2cConfessCtx: C2CConfessContext? = null,
        @ProtoNumber(7) @JvmField val topic: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val confessTime: Long = 0L,
        @ProtoNumber(9) @JvmField val groupConfessMsg: GroupConfessMsg? = null,
        @ProtoNumber(10) @JvmField val groupConfessCtx: GroupConfessContext? = null
    ) : ProtoBuf {
        @Serializable
        internal class C2CConfessContext(
            @ProtoNumber(1) @JvmField val confessorUin: Long = 0L,
            @ProtoNumber(2) @JvmField val confessToUin: Long = 0L,
            @ProtoNumber(3) @JvmField val sendUin: Long = 0L,
            @ProtoNumber(4) @JvmField val confessorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(5) @JvmField val confess: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(6) @JvmField val bgType: Int = 0,
            @ProtoNumber(7) @JvmField val topicId: Int = 0,
            @ProtoNumber(8) @JvmField val confessTime: Long = 0L,
            @ProtoNumber(9) @JvmField val confessorSex: Int = 0,
            @ProtoNumber(10) @JvmField val bizType: Int = 0,
            @ProtoNumber(11) @JvmField val confessNum: Int = 0,
            @ProtoNumber(12) @JvmField val confessToSex: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GroupConfessContext(
            @ProtoNumber(1) @JvmField val confessorUin: Long = 0L,
            @ProtoNumber(2) @JvmField val confessToUin: Long = 0L,
            @ProtoNumber(3) @JvmField val sendUin: Long = 0L,
            @ProtoNumber(4) @JvmField val confessorSex: Int = 0,
            @ProtoNumber(5) @JvmField val confessToNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(6) @JvmField val topic: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(7) @JvmField val topicId: Int = 0,
            @ProtoNumber(8) @JvmField val confessTime: Long = 0L,
            @ProtoNumber(9) @JvmField val confessToNickType: Int = 0,
            @ProtoNumber(10) @JvmField val confessorNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class GroupConfessItem(
            @ProtoNumber(1) @JvmField val topicId: Int = 0,
            @ProtoNumber(2) @JvmField val confessToUin: Long = 0L,
            @ProtoNumber(3) @JvmField val confessToNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(4) @JvmField val topic: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(5) @JvmField val confessToNickType: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GroupConfessMsg(
            @ProtoNumber(1) @JvmField val confessTime: Long = 0L,
            @ProtoNumber(2) @JvmField val confessorUin: Long = 0L,
            @ProtoNumber(3) @JvmField val confessorSex: Int = 0,
            @ProtoNumber(4) @JvmField val sysmsgFlag: Int = 0,
            @ProtoNumber(5) @JvmField val confessItems: List<GroupConfessItem> = emptyList(),
            @ProtoNumber(6) @JvmField val totalTopicCount: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class MsgElemInfoServtype23(
        @ProtoNumber(1) @JvmField val faceType: Int = 0,
        @ProtoNumber(2) @JvmField val faceBubbleCount: Int = 0,
        @ProtoNumber(3) @JvmField val faceSummary: String = "",
        @ProtoNumber(4) @JvmField val flag: Int = 0,
        @ProtoNumber(5) @JvmField val others: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype24(
        @ProtoNumber(1) @JvmField val limitChatEnter: LimitChatEnter? = null,
        @ProtoNumber(2) @JvmField val limitChatExit: LimitChatExit? = null
    ) : ProtoBuf {
        @Serializable
        internal class LimitChatEnter(
            @ProtoNumber(1) @JvmField val tipsWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(2) @JvmField val leftChatTime: Int = 0,
            @ProtoNumber(3) @JvmField val matchTs: Long = 0L,
            @ProtoNumber(4) @JvmField val matchExpiredTime: Int = 0,
            @ProtoNumber(5) @JvmField val c2cExpiredTime: Int = 0,
            @ProtoNumber(6) @JvmField val readyTs: Long = 0L,
            @ProtoNumber(7) @JvmField val matchNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class LimitChatExit(
            @ProtoNumber(1) @JvmField val exitMethod: Int = 0,
            @ProtoNumber(2) @JvmField val matchTs: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class MsgElemInfoServtype27(
        @ProtoNumber(1) @JvmField val videoFile: ImMsgBody.VideoFile? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype29(
        @ProtoNumber(1) @JvmField val luckybagMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype3(
        @ProtoNumber(1) @JvmField val flashTroopPic: ImMsgBody.CustomFace? = null,
        @ProtoNumber(2) @JvmField val flashC2cPic: ImMsgBody.NotOnlineImage? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype31(
        @ProtoNumber(1) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val ext: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype33(
        @ProtoNumber(1) @JvmField val index: Int = 0,
        @ProtoNumber(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val compat: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val buf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype4(
        @ProtoNumber(1) @JvmField val imsgType: Int = 0,
        @ProtoNumber(4) @JvmField val stStoryAioObjMsg: StoryAioObjMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype5(
        @ProtoNumber(1) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val cover: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val summary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val createTime: Long = 0L,
        @ProtoNumber(6) @JvmField val commentContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val author: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype8(
        @ProtoNumber(1) @JvmField val wifiDeliverGiftMsg: ImMsgBody.DeliverGiftMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype9(
        @ProtoNumber(1) @JvmField val anchorStatus: Int = 0,
        @ProtoNumber(2) @JvmField val jumpSchema: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val anchorNickname: String = "",
        @ProtoNumber(4) @JvmField val anchorHeadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val liveTitle: String = ""
    ) : ProtoBuf

    @Serializable
    internal class StoryAioObjMsg(
        @ProtoNumber(1) @JvmField val uiUrl: String = "",
        @ProtoNumber(2) @JvmField val jmpUrl: String = ""
    ) : ProtoBuf
}