package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class Generalflags : ProtoBuf {
    @Serializable
internal class ResvAttr(
        @ProtoId(1) @JvmField val globalGroupLevel: Int = 0,
        @ProtoId(2) @JvmField val nearbyCharmLevel: Int = 0,
        @ProtoId(3) @JvmField val redbagMsgSenderUin: Long = 0L,
        @ProtoId(4) @JvmField val titleId: Int = 0,
        @ProtoId(5) @JvmField val robotMsgFlag: Int = 0,
        @ProtoId(6) @JvmField val wantGiftSenderUin: Long = 0L,
        @ProtoId(7) @JvmField val stickerX: Float = 0.0f,
        @ProtoId(8) @JvmField val stickerY: Float = 0.0f,
        @ProtoId(9) @JvmField val stickerWidth: Float = 0.0f,
        @ProtoId(10) @JvmField val stickerHeight: Float = 0.0f,
        @ProtoId(11) @JvmField val stickerRotate: Int = 0,
        @ProtoId(12) @JvmField val stickerHostMsgseq: Long = 0L,
        @ProtoId(13) @JvmField val stickerHostMsguid: Long = 0L,
        @ProtoId(14) @JvmField val stickerHostTime: Long = 0L,
        @ProtoId(15) @JvmField val mobileCustomFont: Int = 0,
        @ProtoId(16) @JvmField val tailKey: Int = 0,
        @ProtoId(17) @JvmField val showTailFlag: Int = 0,
        @ProtoId(18) @JvmField val doutuMsgType: Int = 0,
        @ProtoId(19) @JvmField val doutuCombo: Int = 0,
        @ProtoId(20) @JvmField val customFeatureid: Int = 0,
        @ProtoId(21) @JvmField val goldenMsgType: Int = 0,
        @ProtoId(22) @JvmField val goldenMsgInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(23) @JvmField val botMessageClassId: Int = 0,
        @ProtoId(24) @JvmField val subscriptionUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(25) @JvmField val pendantDiyId: Int = 0,
        @ProtoId(26) @JvmField val timedMessage: Int = 0,
        @ProtoId(27) @JvmField val holidayFlag: Int = 0,
        @ProtoId(29) @JvmField val kplInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(30) @JvmField val faceId: Int = 0,
        @ProtoId(31) @JvmField val diyFontTimestamp: Int = 0,
        @ProtoId(32) @JvmField val redEnvelopeType: Int = 0,
        @ProtoId(33) @JvmField val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(34) @JvmField val reqFontEffectId: Int = 0,
        @ProtoId(35) @JvmField val loveLanguageFlag: Int = 0,
        @ProtoId(36) @JvmField val aioSyncToStoryFlag: Int = 0,
        @ProtoId(37) @JvmField val uploadImageToQzoneFlag: Int = 0,
        @ProtoId(39) @JvmField val uploadImageToQzoneParam: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(40) @JvmField val groupConfessSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(41) @JvmField val subfontId: Long = 0L,
        @ProtoId(42) @JvmField val msgFlagType: Int = 0,
        @ProtoId(43) @JvmField val uint32CustomFeatureid: List<Int>? = null,
        @ProtoId(44) @JvmField val richCardNameVer: Int = 0,
        @ProtoId(47) @JvmField val msgInfoFlag: Int = 0,
        @ProtoId(48) @JvmField val serviceMsgType: Int = 0,
        @ProtoId(49) @JvmField val serviceMsgRemindType: Int = 0,
        @ProtoId(50) @JvmField val serviceMsgName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(51) @JvmField val vipType: Int = 0,
        @ProtoId(52) @JvmField val vipLevel: Int = 0,
        @ProtoId(53) @JvmField val pbPttWaveform: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(54) @JvmField val userBigclubLevel: Int = 0,
        @ProtoId(55) @JvmField val userBigclubFlag: Int = 0,
        @ProtoId(56) @JvmField val nameplate: Int = 0,
        @ProtoId(57) @JvmField val autoReply: Int = 0,
        @ProtoId(58) @JvmField val reqIsBigclubHidden: Int = 0,
        @ProtoId(59) @JvmField val showInMsgList: Int = 0,
        @ProtoId(60) @JvmField val oacMsgExtend: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(61) @JvmField val groupMemberFlagEx2: Int = 0,
        @ProtoId(62) @JvmField val groupRingtoneId: Int = 0,
        @ProtoId(63) @JvmField val robotGeneralTrans: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(64) @JvmField val troopPobingTemplate: Int = 0,
        @ProtoId(65) @JvmField val hudongMark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(66) @JvmField val groupInfoFlagEx3: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class ResvAttrForGiftMsg : ProtoBuf {
    @Serializable
internal class ActivityGiftInfo(
        @ProtoId(1) @JvmField val isActivityGift: Int = 0,
        @ProtoId(2) @JvmField val textColor: String = "",
        @ProtoId(3) @JvmField val text: String = "",
        @ProtoId(4) @JvmField val url: String = ""
    ) : ProtoBuf

    @Serializable
internal class InteractGift(
        @ProtoId(1) @JvmField val interactId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ResvAttr(
        @ProtoId(1) @JvmField val int32SendScore: Int = 0,
        @ProtoId(2) @JvmField val int32RecvScore: Int = 0,
        @ProtoId(3) @JvmField val charmHeroism: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val buttonFlag: Int = 0,
        @ProtoId(5) @JvmField val objColor: Int = 0,
        @ProtoId(6) @JvmField val animationType: Int = 0,
        @ProtoId(7) @JvmField val msgInteractGift: InteractGift? = null,
        @ProtoId(8) @JvmField val activityGiftInfo: ActivityGiftInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class SourceMsg : ProtoBuf {
    @Serializable
internal class ResvAttr(
        @ProtoId(1) @JvmField val richMsg2: ByteArray? = null,
        @ProtoId(2) @JvmField val oriMsgtype: Int? = null,
        @ProtoId(3) @JvmField val origUids: Long? = null // 原来是 list
    ) : ProtoBuf
}

@Serializable
internal class VideoFile : ProtoBuf {
    @Serializable
internal class ResvAttr(
        @ProtoId(1) @JvmField val hotvideoIcon: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val hotvideoTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val hotvideoUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val hotvideoIconSub: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val specialVideoType: Int = 0,
        @ProtoId(6) @JvmField val dynamicText: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val msgTailType: Int = 0,
        @ProtoId(8) @JvmField val redEnvelopeType: Int = 0,
        @ProtoId(9) @JvmField val shortVideoId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val animojiModelId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val longVideoKandianType: Int = 0,
        @ProtoId(12) @JvmField val source: Int = 0
    ) : ProtoBuf
}
