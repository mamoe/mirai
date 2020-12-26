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

internal class Generalflags : ProtoBuf {
    @Serializable
internal class ResvAttr(
        @ProtoNumber(1) @JvmField val globalGroupLevel: Int = 0,
        @ProtoNumber(2) @JvmField val nearbyCharmLevel: Int = 0,
        @ProtoNumber(3) @JvmField val redbagMsgSenderUin: Long = 0L,
        @ProtoNumber(4) @JvmField val titleId: Int = 0,
        @ProtoNumber(5) @JvmField val robotMsgFlag: Int = 0,
        @ProtoNumber(6) @JvmField val wantGiftSenderUin: Long = 0L,
        @ProtoNumber(7) @JvmField val stickerX: Float = 0.0f,
        @ProtoNumber(8) @JvmField val stickerY: Float = 0.0f,
        @ProtoNumber(9) @JvmField val stickerWidth: Float = 0.0f,
        @ProtoNumber(10) @JvmField val stickerHeight: Float = 0.0f,
        @ProtoNumber(11) @JvmField val stickerRotate: Int = 0,
        @ProtoNumber(12) @JvmField val stickerHostMsgseq: Long = 0L,
        @ProtoNumber(13) @JvmField val stickerHostMsguid: Long = 0L,
        @ProtoNumber(14) @JvmField val stickerHostTime: Long = 0L,
        @ProtoNumber(15) @JvmField val mobileCustomFont: Int = 0,
        @ProtoNumber(16) @JvmField val tailKey: Int = 0,
        @ProtoNumber(17) @JvmField val showTailFlag: Int = 0,
        @ProtoNumber(18) @JvmField val doutuMsgType: Int = 0,
        @ProtoNumber(19) @JvmField val doutuCombo: Int = 0,
        @ProtoNumber(20) @JvmField val customFeatureid: Int = 0,
        @ProtoNumber(21) @JvmField val goldenMsgType: Int = 0,
        @ProtoNumber(22) @JvmField val goldenMsgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(23) @JvmField val botMessageClassId: Int = 0,
        @ProtoNumber(24) @JvmField val subscriptionUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(25) @JvmField val pendantDiyId: Int = 0,
        @ProtoNumber(26) @JvmField val timedMessage: Int = 0,
        @ProtoNumber(27) @JvmField val holidayFlag: Int = 0,
        @ProtoNumber(29) @JvmField val kplInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(30) @JvmField val faceId: Int = 0,
        @ProtoNumber(31) @JvmField val diyFontTimestamp: Int = 0,
        @ProtoNumber(32) @JvmField val redEnvelopeType: Int = 0,
        @ProtoNumber(33) @JvmField val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(34) @JvmField val reqFontEffectId: Int = 0,
        @ProtoNumber(35) @JvmField val loveLanguageFlag: Int = 0,
        @ProtoNumber(36) @JvmField val aioSyncToStoryFlag: Int = 0,
        @ProtoNumber(37) @JvmField val uploadImageToQzoneFlag: Int = 0,
        @ProtoNumber(39) @JvmField val uploadImageToQzoneParam: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(40) @JvmField val groupConfessSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(41) @JvmField val subfontId: Long = 0L,
        @ProtoNumber(42) @JvmField val msgFlagType: Int = 0,
        @ProtoNumber(43) @JvmField val uint32CustomFeatureid: List<Int> = emptyList(),
        @ProtoNumber(44) @JvmField val richCardNameVer: Int = 0,
        @ProtoNumber(47) @JvmField val msgInfoFlag: Int = 0,
        @ProtoNumber(48) @JvmField val serviceMsgType: Int = 0,
        @ProtoNumber(49) @JvmField val serviceMsgRemindType: Int = 0,
        @ProtoNumber(50) @JvmField val serviceMsgName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(51) @JvmField val vipType: Int = 0,
        @ProtoNumber(52) @JvmField val vipLevel: Int = 0,
        @ProtoNumber(53) @JvmField val pbPttWaveform: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(54) @JvmField val userBigclubLevel: Int = 0,
        @ProtoNumber(55) @JvmField val userBigclubFlag: Int = 0,
        @ProtoNumber(56) @JvmField val nameplate: Int = 0,
        @ProtoNumber(57) @JvmField val autoReply: Int = 0,
        @ProtoNumber(58) @JvmField val reqIsBigclubHidden: Int = 0,
        @ProtoNumber(59) @JvmField val showInMsgList: Int = 0,
        @ProtoNumber(60) @JvmField val oacMsgExtend: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(61) @JvmField val groupMemberFlagEx2: Int = 0,
        @ProtoNumber(62) @JvmField val groupRingtoneId: Int = 0,
        @ProtoNumber(63) @JvmField val robotGeneralTrans: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(64) @JvmField val troopPobingTemplate: Int = 0,
        @ProtoNumber(65) @JvmField val hudongMark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(66) @JvmField val groupInfoFlagEx3: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class ResvAttrForGiftMsg : ProtoBuf {
    @Serializable
internal class ActivityGiftInfo(
        @ProtoNumber(1) @JvmField val isActivityGift: Int = 0,
        @ProtoNumber(2) @JvmField val textColor: String = "",
        @ProtoNumber(3) @JvmField val text: String = "",
        @ProtoNumber(4) @JvmField val url: String = ""
    ) : ProtoBuf

    @Serializable
internal class InteractGift(
        @ProtoNumber(1) @JvmField val interactId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ResvAttr(
        @ProtoNumber(1) @JvmField val int32SendScore: Int = 0,
        @ProtoNumber(2) @JvmField val int32RecvScore: Int = 0,
        @ProtoNumber(3) @JvmField val charmHeroism: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val buttonFlag: Int = 0,
        @ProtoNumber(5) @JvmField val objColor: Int = 0,
        @ProtoNumber(6) @JvmField val animationType: Int = 0,
        @ProtoNumber(7) @JvmField val msgInteractGift: InteractGift? = null,
        @ProtoNumber(8) @JvmField val activityGiftInfo: ActivityGiftInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class SourceMsg : ProtoBuf {
    @Serializable
internal class ResvAttr(
        @ProtoNumber(1) @JvmField val richMsg2: ByteArray? = null,
        @ProtoNumber(2) @JvmField val oriMsgtype: Int? = null,
        @ProtoNumber(3) @JvmField val origUids: List<Long>? = null
    ) : ProtoBuf
}

@Serializable
internal class VideoFile : ProtoBuf {
    @Serializable
internal class ResvAttr(
        @ProtoNumber(1) @JvmField val hotvideoIcon: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val hotvideoTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val hotvideoUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val hotvideoIconSub: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val specialVideoType: Int = 0,
        @ProtoNumber(6) @JvmField val dynamicText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val msgTailType: Int = 0,
        @ProtoNumber(8) @JvmField val redEnvelopeType: Int = 0,
        @ProtoNumber(9) @JvmField val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val animojiModelId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val longVideoKandianType: Int = 0,
        @ProtoNumber(12) @JvmField val source: Int = 0
    ) : ProtoBuf
}
