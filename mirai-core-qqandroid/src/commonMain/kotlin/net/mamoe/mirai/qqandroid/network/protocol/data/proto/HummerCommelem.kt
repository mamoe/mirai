package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class HummerCommelem : ProtoBuf {
    @Serializable
    internal class MsgElemInfoServtype1(
        @ProtoId(1) @JvmField val rewardId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val senderUin: Long = 0L,
        @ProtoId(3) @JvmField val picType: Int = 0,
        @ProtoId(4) @JvmField val rewardMoney: Int = 0,
        @ProtoId(5) @JvmField val url: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val createTimestamp: Int = 0,
        @ProtoId(8) @JvmField val status: Int = 0,
        @ProtoId(9) @JvmField val size: Int = 0,
        @ProtoId(10) @JvmField val videoDuration: Int = 0,
        @ProtoId(11) @JvmField val seq: Long = 0L,
        @ProtoId(12) @JvmField val rewardTypeExt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype11(
        @ProtoId(1) @JvmField val resID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val resMD5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val reserveInfo1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val reserveInfo2: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val doodleDataOffset: Int = 0,
        @ProtoId(6) @JvmField val doodleGifId: Int = 0,
        @ProtoId(7) @JvmField val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val doodleMd5: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype13(
        @ProtoId(1) @JvmField val sysHeadId: Int = 0,
        @ProtoId(2) @JvmField val headFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype14(
        @ProtoId(1) @JvmField val id: Int = 0,
        @ProtoId(2) @JvmField val reserveInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype15(
        @ProtoId(1) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val cover: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val summary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val createTime: Long = 0L,
        @ProtoId(6) @JvmField val commentContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val author: Long = 0L,
        @ProtoId(8) @JvmField val ctrVersion: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype16(
        @ProtoId(1) @JvmField val uid: Long = 0L,
        @ProtoId(2) @JvmField val unionID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val storyID: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val thumbUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val doodleUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val videoWidth: Int = 0,
        @ProtoId(8) @JvmField val videoHeight: Int = 0,
        @ProtoId(9) @JvmField val sourceName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val sourceActionType: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val sourceActionData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val ctrVersion: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype18(
        @ProtoId(1) @JvmField val currentAmount: Long = 0L,
        @ProtoId(2) @JvmField val totalAmount: Long = 0L,
        @ProtoId(3) @JvmField val listid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val authKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val number: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype19(
        @ProtoId(1) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype2(
        @ProtoId(1) @JvmField val pokeType: Int = 0,
        @ProtoId(2) @JvmField val pokeSummary: String = "",
        @ProtoId(3) @JvmField val doubleHit: Int = 0,
        @ProtoId(4) @JvmField val vaspokeId: Int = 0,
        @ProtoId(5) @JvmField val vaspokeName: String = "",
        @ProtoId(6) @JvmField val vaspokeMinver: String = "",
        @ProtoId(7) @JvmField val pokeStrength: Int = 0,
        @ProtoId(8) @JvmField val msgType: Int = 0,
        @ProtoId(9) @JvmField val faceBubbleCount: Int = 0,
        @ProtoId(10) @JvmField val pokeFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype20(
        @ProtoId(1) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype21(
        @ProtoId(1) @JvmField val topicId: Int = 0,
        @ProtoId(2) @JvmField val confessorUin: Long = 0L,
        @ProtoId(3) @JvmField val confessorNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val confessorSex: Int = 0,
        @ProtoId(5) @JvmField val sysmsgFlag: Int = 0,
        @ProtoId(6) @JvmField val c2cConfessCtx: C2CConfessContext? = null,
        @ProtoId(7) @JvmField val topic: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val confessTime: Long = 0L,
        @ProtoId(9) @JvmField val groupConfessMsg: GroupConfessMsg? = null,
        @ProtoId(10) @JvmField val groupConfessCtx: GroupConfessContext? = null
    ) : ProtoBuf {
        @Serializable
        internal class C2CConfessContext(
            @ProtoId(1) @JvmField val confessorUin: Long = 0L,
            @ProtoId(2) @JvmField val confessToUin: Long = 0L,
            @ProtoId(3) @JvmField val sendUin: Long = 0L,
            @ProtoId(4) @JvmField val confessorNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val confess: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val bgType: Int = 0,
            @ProtoId(7) @JvmField val topicId: Int = 0,
            @ProtoId(8) @JvmField val confessTime: Long = 0L,
            @ProtoId(9) @JvmField val confessorSex: Int = 0,
            @ProtoId(10) @JvmField val bizType: Int = 0,
            @ProtoId(11) @JvmField val confessNum: Int = 0,
            @ProtoId(12) @JvmField val confessToSex: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GroupConfessContext(
            @ProtoId(1) @JvmField val confessorUin: Long = 0L,
            @ProtoId(2) @JvmField val confessToUin: Long = 0L,
            @ProtoId(3) @JvmField val sendUin: Long = 0L,
            @ProtoId(4) @JvmField val confessorSex: Int = 0,
            @ProtoId(5) @JvmField val confessToNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val topic: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(7) @JvmField val topicId: Int = 0,
            @ProtoId(8) @JvmField val confessTime: Long = 0L,
            @ProtoId(9) @JvmField val confessToNickType: Int = 0,
            @ProtoId(10) @JvmField val confessorNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class GroupConfessItem(
            @ProtoId(1) @JvmField val topicId: Int = 0,
            @ProtoId(2) @JvmField val confessToUin: Long = 0L,
            @ProtoId(3) @JvmField val confessToNick: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(4) @JvmField val topic: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(5) @JvmField val confessToNickType: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class GroupConfessMsg(
            @ProtoId(1) @JvmField val confessTime: Long = 0L,
            @ProtoId(2) @JvmField val confessorUin: Long = 0L,
            @ProtoId(3) @JvmField val confessorSex: Int = 0,
            @ProtoId(4) @JvmField val sysmsgFlag: Int = 0,
            @ProtoId(5) @JvmField val confessItems: List<GroupConfessItem>? = null,
            @ProtoId(6) @JvmField val totalTopicCount: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class MsgElemInfoServtype23(
        @ProtoId(1) @JvmField val faceType: Int = 0,
        @ProtoId(2) @JvmField val faceBubbleCount: Int = 0,
        @ProtoId(3) @JvmField val faceSummary: String = "",
        @ProtoId(4) @JvmField val flag: Int = 0,
        @ProtoId(5) @JvmField val others: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype24(
        @ProtoId(1) @JvmField val limitChatEnter: LimitChatEnter? = null,
        @ProtoId(2) @JvmField val limitChatExit: LimitChatExit? = null
    ) : ProtoBuf {
        @Serializable
        internal class LimitChatEnter(
            @ProtoId(1) @JvmField val tipsWording: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(2) @JvmField val leftChatTime: Int = 0,
            @ProtoId(3) @JvmField val matchTs: Long = 0L,
            @ProtoId(4) @JvmField val matchExpiredTime: Int = 0,
            @ProtoId(5) @JvmField val c2cExpiredTime: Int = 0,
            @ProtoId(6) @JvmField val readyTs: Long = 0L,
            @ProtoId(7) @JvmField val matchNick: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class LimitChatExit(
            @ProtoId(1) @JvmField val exitMethod: Int = 0,
            @ProtoId(2) @JvmField val matchTs: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class MsgElemInfoServtype27(
        @ProtoId(1) @JvmField val videoFile: ImMsgBody.VideoFile? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype29(
        @ProtoId(1) @JvmField val luckybagMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype3(
        @ProtoId(1) @JvmField val flashTroopPic: ImMsgBody.CustomFace? = null,
        @ProtoId(2) @JvmField val flashC2cPic: ImMsgBody.NotOnlineImage? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype31(
        @ProtoId(1) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val ext: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype4(
        @ProtoId(1) @JvmField val imsgType: Int = 0,
        @ProtoId(4) @JvmField val stStoryAioObjMsg: StoryAioObjMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype5(
        @ProtoId(1) @JvmField val vid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val cover: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val summary: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val createTime: Long = 0L,
        @ProtoId(6) @JvmField val commentContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val author: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype8(
        @ProtoId(1) @JvmField val wifiDeliverGiftMsg: ImMsgBody.DeliverGiftMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgElemInfoServtype9(
        @ProtoId(1) @JvmField val anchorStatus: Int = 0,
        @ProtoId(2) @JvmField val jumpSchema: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val anchorNickname: String = "",
        @ProtoId(4) @JvmField val anchorHeadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val liveTitle: String = ""
    ) : ProtoBuf

    @Serializable
    internal class StoryAioObjMsg(
        @ProtoId(1) @JvmField val uiUrl: String = "",
        @ProtoId(2) @JvmField val jmpUrl: String = ""
    ) : ProtoBuf
}