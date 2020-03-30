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
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

internal class Generalflags : ProtoBuf {
    @Serializable
    class ResvAttr(
        @ProtoId(1) val globalGroupLevel: Int = 0,
        @ProtoId(2) val nearbyCharmLevel: Int = 0,
        @ProtoId(3) val redbagMsgSenderUin: Long = 0L,
        @ProtoId(4) val titleId: Int = 0,
        @ProtoId(5) val robotMsgFlag: Int = 0,
        @ProtoId(6) val wantGiftSenderUin: Long = 0L,
        @ProtoId(7) val stickerX: Float = 0.0f,
        @ProtoId(8) val stickerY: Float = 0.0f,
        @ProtoId(9) val stickerWidth: Float = 0.0f,
        @ProtoId(10) val stickerHeight: Float = 0.0f,
        @ProtoId(11) val stickerRotate: Int = 0,
        @ProtoId(12) val stickerHostMsgseq: Long = 0L,
        @ProtoId(13) val stickerHostMsguid: Long = 0L,
        @ProtoId(14) val stickerHostTime: Long = 0L,
        @ProtoId(15) val mobileCustomFont: Int = 0,
        @ProtoId(16) val tailKey: Int = 0,
        @ProtoId(17) val showTailFlag: Int = 0,
        @ProtoId(18) val doutuMsgType: Int = 0,
        @ProtoId(19) val doutuCombo: Int = 0,
        @ProtoId(20) val customFeatureid: Int = 0,
        @ProtoId(21) val goldenMsgType: Int = 0,
        @ProtoId(22) val goldenMsgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(23) val botMessageClassId: Int = 0,
        @ProtoId(24) val subscriptionUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(25) val pendantDiyId: Int = 0,
        @ProtoId(26) val timedMessage: Int = 0,
        @ProtoId(27) val holidayFlag: Int = 0,
        @ProtoId(29) val kplInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(30) val faceId: Int = 0,
        @ProtoId(31) val diyFontTimestamp: Int = 0,
        @ProtoId(32) val redEnvelopeType: Int = 0,
        @ProtoId(33) val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(34) val reqFontEffectId: Int = 0,
        @ProtoId(35) val loveLanguageFlag: Int = 0,
        @ProtoId(36) val aioSyncToStoryFlag: Int = 0,
        @ProtoId(37) val uploadImageToQzoneFlag: Int = 0,
        @ProtoId(39) val uploadImageToQzoneParam: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(40) val groupConfessSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(41) val subfontId: Long = 0L,
        @ProtoId(42) val msgFlagType: Int = 0,
        @ProtoId(43) val uint32CustomFeatureid: List<Int>? = null,
        @ProtoId(44) val richCardNameVer: Int = 0,
        @ProtoId(47) val msgInfoFlag: Int = 0,
        @ProtoId(48) val serviceMsgType: Int = 0,
        @ProtoId(49) val serviceMsgRemindType: Int = 0,
        @ProtoId(50) val serviceMsgName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(51) val vipType: Int = 0,
        @ProtoId(52) val vipLevel: Int = 0,
        @ProtoId(53) val pbPttWaveform: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(54) val userBigclubLevel: Int = 0,
        @ProtoId(55) val userBigclubFlag: Int = 0,
        @ProtoId(56) val nameplate: Int = 0,
        @ProtoId(57) val autoReply: Int = 0,
        @ProtoId(58) val reqIsBigclubHidden: Int = 0,
        @ProtoId(59) val showInMsgList: Int = 0,
        @ProtoId(60) val oacMsgExtend: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(61) val groupMemberFlagEx2: Int = 0,
        @ProtoId(62) val groupRingtoneId: Int = 0,
        @ProtoId(63) val robotGeneralTrans: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(64) val troopPobingTemplate: Int = 0,
        @ProtoId(65) val hudongMark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(66) val groupInfoFlagEx3: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class ResvAttrForGiftMsg : ProtoBuf {
    @Serializable
    class ActivityGiftInfo(
        @ProtoId(1) val isActivityGift: Int = 0,
        @ProtoId(2) val textColor: String = "",
        @ProtoId(3) val text: String = "",
        @ProtoId(4) val url: String = ""
    ) : ProtoBuf

    @Serializable
    class InteractGift(
        @ProtoId(1) val interactId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ResvAttr(
        @ProtoId(1) val int32SendScore: Int = 0,
        @ProtoId(2) val int32RecvScore: Int = 0,
        @ProtoId(3) val charmHeroism: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val buttonFlag: Int = 0,
        @ProtoId(5) val objColor: Int = 0,
        @ProtoId(6) val animationType: Int = 0,
        @ProtoId(7) val msgInteractGift: InteractGift? = null,
        @ProtoId(8) val activityGiftInfo: ActivityGiftInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class SourceMsg : ProtoBuf {
    @Serializable
    class ResvAttr(
        @ProtoId(1) val richMsg2: ByteArray? = null,
        @ProtoId(2) val oriMsgtype: Int? = null,
        @ProtoId(3) val origUids: Long? = null // 原来是 list
    ) : ProtoBuf
}

@Serializable
internal class VideoFile : ProtoBuf {
    @Serializable
    class ResvAttr(
        @ProtoId(1) val hotvideoIcon: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val hotvideoTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val hotvideoUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val hotvideoIconSub: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val specialVideoType: Int = 0,
        @ProtoId(6) val dynamicText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val msgTailType: Int = 0,
        @ProtoId(8) val redEnvelopeType: Int = 0,
        @ProtoId(9) val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val animojiModelId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val longVideoKandianType: Int = 0,
        @ProtoId(12) val source: Int = 0
    ) : ProtoBuf
}
