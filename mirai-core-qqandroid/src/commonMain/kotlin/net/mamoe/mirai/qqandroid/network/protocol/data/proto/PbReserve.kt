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
class Generalflags : ProtoBuf {
    @Serializable
    class ResvAttr(
        @SerialId(1) val globalGroupLevel: Int = 0,
        @SerialId(2) val nearbyCharmLevel: Int = 0,
        @SerialId(3) val redbagMsgSenderUin: Long = 0L,
        @SerialId(4) val titleId: Int = 0,
        @SerialId(5) val robotMsgFlag: Int = 0,
        @SerialId(6) val wantGiftSenderUin: Long = 0L,
        @SerialId(7) val stickerX: Float = 0.0f,
        @SerialId(8) val stickerY: Float = 0.0f,
        @SerialId(9) val stickerWidth: Float = 0.0f,
        @SerialId(10) val stickerHeight: Float = 0.0f,
        @SerialId(11) val stickerRotate: Int = 0,
        @SerialId(12) val stickerHostMsgseq: Long = 0L,
        @SerialId(13) val stickerHostMsguid: Long = 0L,
        @SerialId(14) val stickerHostTime: Long = 0L,
        @SerialId(15) val mobileCustomFont: Int = 0,
        @SerialId(16) val tailKey: Int = 0,
        @SerialId(17) val showTailFlag: Int = 0,
        @SerialId(18) val doutuMsgType: Int = 0,
        @SerialId(19) val doutuCombo: Int = 0,
        @SerialId(20) val customFeatureid: Int = 0,
        @SerialId(21) val goldenMsgType: Int = 0,
        @SerialId(22) val goldenMsgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(23) val botMessageClassId: Int = 0,
        @SerialId(24) val subscriptionUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(25) val pendantDiyId: Int = 0,
        @SerialId(26) val timedMessage: Int = 0,
        @SerialId(27) val holidayFlag: Int = 0,
        @SerialId(29) val kplInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(30) val faceId: Int = 0,
        @SerialId(31) val diyFontTimestamp: Int = 0,
        @SerialId(32) val redEnvelopeType: Int = 0,
        @SerialId(33) val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(34) val reqFontEffectId: Int = 0,
        @SerialId(35) val loveLanguageFlag: Int = 0,
        @SerialId(36) val aioSyncToStoryFlag: Int = 0,
        @SerialId(37) val uploadImageToQzoneFlag: Int = 0,
        @SerialId(39) val uploadImageToQzoneParam: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(40) val groupConfessSig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(41) val subfontId: Long = 0L,
        @SerialId(42) val msgFlagType: Int = 0,
        @SerialId(43) val uint32CustomFeatureid: List<Int>? = null,
        @SerialId(44) val richCardNameVer: Int = 0,
        @SerialId(47) val msgInfoFlag: Int = 0,
        @SerialId(48) val serviceMsgType: Int = 0,
        @SerialId(49) val serviceMsgRemindType: Int = 0,
        @SerialId(50) val serviceMsgName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(51) val vipType: Int = 0,
        @SerialId(52) val vipLevel: Int = 0,
        @SerialId(53) val pbPttWaveform: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(54) val userBigclubLevel: Int = 0,
        @SerialId(55) val userBigclubFlag: Int = 0,
        @SerialId(56) val nameplate: Int = 0,
        @SerialId(57) val autoReply: Int = 0,
        @SerialId(58) val reqIsBigclubHidden: Int = 0,
        @SerialId(59) val showInMsgList: Int = 0,
        @SerialId(60) val oacMsgExtend: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(61) val groupMemberFlagEx2: Int = 0,
        @SerialId(62) val groupRingtoneId: Int = 0,
        @SerialId(63) val robotGeneralTrans: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(64) val troopPobingTemplate: Int = 0,
        @SerialId(65) val hudongMark: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(66) val groupInfoFlagEx3: Int = 0
    ) : ProtoBuf
}

@Serializable
class ResvAttrForGiftMsg : ProtoBuf {
    @Serializable
    class ActivityGiftInfo(
        @SerialId(1) val isActivityGift: Int = 0,
        @SerialId(2) val textColor: String = "",
        @SerialId(3) val text: String = "",
        @SerialId(4) val url: String = ""
    ) : ProtoBuf

    @Serializable
    class InteractGift(
        @SerialId(1) val interactId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ResvAttr(
        @SerialId(1) val int32SendScore: Int = 0,
        @SerialId(2) val int32RecvScore: Int = 0,
        @SerialId(3) val charmHeroism: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val buttonFlag: Int = 0,
        @SerialId(5) val objColor: Int = 0,
        @SerialId(6) val animationType: Int = 0,
        @SerialId(7) val msgInteractGift: ResvAttrForGiftMsg.InteractGift? = null,
        @SerialId(8) val activityGiftInfo: ResvAttrForGiftMsg.ActivityGiftInfo? = null
    ) : ProtoBuf
}

@Serializable
class SourceMsg : ProtoBuf {
    @Serializable
    class ResvAttr(
        @SerialId(1) val richMsg2: ByteArray? = null,
        @SerialId(2) val oriMsgtype: Int? = null,
        @SerialId(3) val origUids: Long? = null // 原来是 list
    ) : ProtoBuf
}

@Serializable
class VideoFile : ProtoBuf {
    @Serializable
    class ResvAttr(
        @SerialId(1) val hotvideoIcon: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val hotvideoTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val hotvideoUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val hotvideoIconSub: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val specialVideoType: Int = 0,
        @SerialId(6) val dynamicText: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val msgTailType: Int = 0,
        @SerialId(8) val redEnvelopeType: Int = 0,
        @SerialId(9) val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val animojiModelId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val longVideoKandianType: Int = 0,
        @SerialId(12) val source: Int = 0
    ) : ProtoBuf
}
