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

internal class HummerCommelem : ProtoBuf {
    @Serializable
internal class MsgElemInfoServtype1(
        @ProtoId(1) val rewardId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val senderUin: Long = 0L,
        @ProtoId(3) val picType: Int = 0,
        @ProtoId(4) val rewardMoney: Int = 0,
        @ProtoId(5) val url: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val content: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val createTimestamp: Int = 0,
        @ProtoId(8) val status: Int = 0,
        @ProtoId(9) val size: Int = 0,
        @ProtoId(10) val videoDuration: Int = 0,
        @ProtoId(11) val seq: Long = 0L,
        @ProtoId(12) val rewardTypeExt: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype11(
        @ProtoId(1) val resID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val resMD5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val reserveInfo1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val reserveInfo2: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val doodleDataOffset: Int = 0,
        @ProtoId(6) val doodleGifId: Int = 0,
        @ProtoId(7) val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val doodleMd5: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype13(
        @ProtoId(1) val sysHeadId: Int = 0,
        @ProtoId(2) val headFlag: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype14(
        @ProtoId(1) val id: Int = 0,
        @ProtoId(2) val reserveInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype15(
        @ProtoId(1) val vid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val cover: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val summary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val createTime: Long = 0L,
        @ProtoId(6) val commentContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val author: Long = 0L,
        @ProtoId(8) val ctrVersion: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype16(
        @ProtoId(1) val uid: Long = 0L,
        @ProtoId(2) val unionID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val storyID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val thumbUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val videoWidth: Int = 0,
        @ProtoId(8) val videoHeight: Int = 0,
        @ProtoId(9) val sourceName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val sourceActionType: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val sourceActionData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val ctrVersion: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype18(
        @ProtoId(1) val currentAmount: Long = 0L,
        @ProtoId(2) val totalAmount: Long = 0L,
        @ProtoId(3) val listid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val authKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val number: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype19(
        @ProtoId(1) val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype2(
        @ProtoId(1) val pokeType: Int = 0,
        @ProtoId(2) val pokeSummary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val doubleHit: Int = 0,
        @ProtoId(4) val vaspokeId: Int = 0,
        @ProtoId(5) val vaspokeName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val vaspokeMinver: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val pokeStrength: Int = 0,
        @ProtoId(8) val msgType: Int = 0,
        @ProtoId(9) val faceBubbleCount: Int = 0,
        @ProtoId(10) val pokeFlag: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype20(
        @ProtoId(1) val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype21(
        @ProtoId(1) val topicId: Int = 0,
        @ProtoId(2) val confessorUin: Long = 0L,
        @ProtoId(3) val confessorNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val confessorSex: Int = 0,
        @ProtoId(5) val sysmsgFlag: Int = 0,
        @ProtoId(6) val c2cConfessCtx: HummerCommelem.MsgElemInfoServtype21.C2CConfessContext? = null,
        @ProtoId(7) val topic: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val confessTime: Long = 0L,
        @ProtoId(9) val groupConfessMsg: HummerCommelem.MsgElemInfoServtype21.GroupConfessMsg? = null,
        @ProtoId(10) val groupConfessCtx: HummerCommelem.MsgElemInfoServtype21.GroupConfessContext? = null
    ) : ProtoBuf {
        @Serializable
internal class C2CConfessContext(
            @ProtoId(1) val confessorUin: Long = 0L,
            @ProtoId(2) val confessToUin: Long = 0L,
            @ProtoId(3) val sendUin: Long = 0L,
            @ProtoId(4) val confessorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val confess: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val bgType: Int = 0,
            @ProtoId(7) val topicId: Int = 0,
            @ProtoId(8) val confessTime: Long = 0L,
            @ProtoId(9) val confessorSex: Int = 0,
            @ProtoId(10) val bizType: Int = 0,
            @ProtoId(11) val confessNum: Int = 0,
            @ProtoId(12) val confessToSex: Int = 0
        ) : ProtoBuf

        @Serializable
internal class GroupConfessContext(
            @ProtoId(1) val confessorUin: Long = 0L,
            @ProtoId(2) val confessToUin: Long = 0L,
            @ProtoId(3) val sendUin: Long = 0L,
            @ProtoId(4) val confessorSex: Int = 0,
            @ProtoId(5) val confessToNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val topic: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) val topicId: Int = 0,
            @ProtoId(8) val confessTime: Long = 0L,
            @ProtoId(9) val confessToNickType: Int = 0,
            @ProtoId(10) val confessorNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
internal class GroupConfessItem(
            @ProtoId(1) val topicId: Int = 0,
            @ProtoId(2) val confessToUin: Long = 0L,
            @ProtoId(3) val confessToNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) val topic: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) val confessToNickType: Int = 0
        ) : ProtoBuf

        @Serializable
internal class GroupConfessMsg(
            @ProtoId(1) val confessTime: Long = 0L,
            @ProtoId(2) val confessorUin: Long = 0L,
            @ProtoId(3) val confessorSex: Int = 0,
            @ProtoId(4) val sysmsgFlag: Int = 0,
            @ProtoId(5) val confessItems: List<HummerCommelem.MsgElemInfoServtype21.GroupConfessItem>? = null,
            @ProtoId(6) val totalTopicCount: Int = 0
        ) : ProtoBuf
    }

    @Serializable
internal class MsgElemInfoServtype23(
        @ProtoId(1) val faceType: Int = 0,
        @ProtoId(2) val faceBubbleCount: Int = 0,
        @ProtoId(3) val faceSummary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val flag: Int = 0,
        @ProtoId(5) val others: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype24(
        @ProtoId(1) val limitChatEnter: HummerCommelem.MsgElemInfoServtype24.LimitChatEnter? = null,
        @ProtoId(2) val limitChatExit: HummerCommelem.MsgElemInfoServtype24.LimitChatExit? = null
    ) : ProtoBuf {
        @Serializable
internal class LimitChatEnter(
            @ProtoId(1) val tipsWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) val leftChatTime: Int = 0,
            @ProtoId(3) val matchTs: Long = 0L,
            @ProtoId(4) val matchExpiredTime: Int = 0,
            @ProtoId(5) val c2cExpiredTime: Int = 0,
            @ProtoId(6) val readyTs: Long = 0L,
            @ProtoId(7) val matchNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
internal class LimitChatExit(
            @ProtoId(1) val exitMethod: Int = 0,
            @ProtoId(2) val matchTs: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
internal class MsgElemInfoServtype27(
        @ProtoId(1) val videoFile: ImMsgBody.VideoFile? = null
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype29(
        @ProtoId(1) val luckybagMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype3(
        @ProtoId(1) val flashTroopPic: ImMsgBody.CustomFace? = null,
        @ProtoId(2) val flashC2cPic: ImMsgBody.NotOnlineImage? = null
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype31(
        @ProtoId(1) val text: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val ext: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype4(
        @ProtoId(1) val imsgType: Int = 0,
        @ProtoId(4) val stStoryAioObjMsg: HummerCommelem.StoryAioObjMsg? = null
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype5(
        @ProtoId(1) val vid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val cover: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val summary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val createTime: Long = 0L,
        @ProtoId(6) val commentContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val author: Long = 0L
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype8(
        @ProtoId(1) val wifiDeliverGiftMsg: ImMsgBody.DeliverGiftMsg? = null
    ) : ProtoBuf

    @Serializable
internal class MsgElemInfoServtype9(
        @ProtoId(1) val anchorStatus: Int = 0,
        @ProtoId(2) val jumpSchema: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val anchorNickname: String = "",
        @ProtoId(4) val anchorHeadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val liveTitle: String = ""
    ) : ProtoBuf

    @Serializable
internal class StoryAioObjMsg(
        @ProtoId(1) val uiUrl: String = "",
        @ProtoId(2) val jmpUrl: String = ""
    ) : ProtoBuf
}