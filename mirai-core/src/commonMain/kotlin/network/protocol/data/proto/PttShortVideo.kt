/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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

@Serializable
internal class PttShortVideo : ProtoBuf {
    @Serializable
    internal class ServerListInfo(
        @JvmField @ProtoNumber(1) val upIp: Int = 0,
        @JvmField @ProtoNumber(2) val upPort: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CodecConfigReq(
        @JvmField @ProtoNumber(1) val platformChipinfo: String = "",
        @JvmField @ProtoNumber(2) val osVersion: String = "",
        @JvmField @ProtoNumber(3) val deviceName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class DataHole(
        @JvmField @ProtoNumber(1) val begin: Long = 0L,
        @JvmField @ProtoNumber(2) val end: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ExtensionReq(
        @JvmField @ProtoNumber(1) val subBusiType: Int = 0,
        @JvmField @ProtoNumber(2) val userCnt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoAddr(
        @JvmField @ProtoNumber(1) val hostType: Int = 0,
        @JvmField @ProtoNumber(10) val strHost: List<String> = emptyList(),
        @JvmField @ProtoNumber(11) val urlArgs: String = "",
        @JvmField @ProtoNumber(21) val strHostIpv6: List<String> = emptyList(),
        @JvmField @ProtoNumber(22) val strDomain: List<String> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoDeleteReq(
        @JvmField @ProtoNumber(1) val fromuin: Long = 0L,
        @JvmField @ProtoNumber(2) val touin: Long = 0L,
        @JvmField @ProtoNumber(3) val chatType: Int = 0,
        @JvmField @ProtoNumber(4) val clientType: Int = 0,
        @JvmField @ProtoNumber(5) val fileid: String = "",
        @JvmField @ProtoNumber(6) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(7) val agentType: Int = 0,
        @JvmField @ProtoNumber(8) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(9) val businessType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoDeleteResp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoDownloadReq(
        @JvmField @ProtoNumber(1) val fromuin: Long = 0L,
        @JvmField @ProtoNumber(2) val touin: Long = 0L,
        @JvmField @ProtoNumber(3) val chatType: Int = 0,
        @JvmField @ProtoNumber(4) val clientType: Int = 0,
        @JvmField @ProtoNumber(5) val fileid: String = "",
        @JvmField @ProtoNumber(6) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(7) val agentType: Int = 0,
        @JvmField @ProtoNumber(8) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(9) val businessType: Int = 0,
        @JvmField @ProtoNumber(10) val fileType: Int = 0,
        @JvmField @ProtoNumber(11) val downType: Int = 0,
        @JvmField @ProtoNumber(12) val sceneType: Int = 0,
        @JvmField @ProtoNumber(13) val needInnerAddr: Int = 0,
        @JvmField @ProtoNumber(14) val reqTransferType: Int = 0,
        @JvmField @ProtoNumber(15) val reqHostType: Int = 0,
        @JvmField @ProtoNumber(20) val flagSupportLargeSize: Int = 0,
        @JvmField @ProtoNumber(30) val flagClientQuicProtoEnable: Int = 0,
        @JvmField @ProtoNumber(31) val targetCodecFormat: Int = 0,
        @JvmField @ProtoNumber(32) val msgCodecConfig: CodecConfigReq? = null,
        @JvmField @ProtoNumber(33) val sourceCodecFormat: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoDownloadResp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val sameAreaOutAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(4) val diffAreaOutAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(5) val downloadkey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(6) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val sameAreaInnerAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(8) val diffAreaInnerAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(9) val msgDownloadAddr: PttShortVideoAddr? = null,
        @JvmField @ProtoNumber(10) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(30) val flagServerQuicProtoEnable: Int = 0,
        @JvmField @ProtoNumber(31) val serverQuicPara: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(32) val codecFormat: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoFileInfo(
        @JvmField @ProtoNumber(1) val fileName: String = "",
        @JvmField @ProtoNumber(2) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val thumbFileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(4) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(5) val fileResLength: Int = 0,
        @JvmField @ProtoNumber(6) val fileResWidth: Int = 0,
        @JvmField @ProtoNumber(7) val fileFormat: Int = 0,
        @JvmField @ProtoNumber(8) val fileTime: Int = 0,
        @JvmField @ProtoNumber(9) val thumbFileSize: Long = 0L,
        @JvmField @ProtoNumber(10) val decryptVideoMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(11) val decryptFileSize: Long = 0L,
        @JvmField @ProtoNumber(12) val decryptThumbMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(13) val decryptThumbSize: Long = 0L,
        @JvmField @ProtoNumber(14) val extend: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoFileInfoExtend(
        @JvmField @ProtoNumber(1) val bitRate: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoIpList(
        @JvmField @ProtoNumber(1) val ip: Int = 0,
        @JvmField @ProtoNumber(2) val port: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoRetweetReq(
        @JvmField @ProtoNumber(1) val fromUin: Long = 0L,
        @JvmField @ProtoNumber(2) val toUin: Long = 0L,
        @JvmField @ProtoNumber(3) val fromChatType: Int = 0,
        @JvmField @ProtoNumber(4) val toChatType: Int = 0,
        @JvmField @ProtoNumber(5) val fromBusiType: Int = 0,
        @JvmField @ProtoNumber(6) val toBusiType: Int = 0,
        @JvmField @ProtoNumber(7) val clientType: Int = 0,
        @JvmField @ProtoNumber(8) val msgPttShortVideoFileInfo: PttShortVideoFileInfo? = null,
        @JvmField @ProtoNumber(9) val agentType: Int = 0,
        @JvmField @ProtoNumber(10) val fileid: String = "",
        @JvmField @ProtoNumber(11) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(20) val flagSupportLargeSize: Int = 0,
        @JvmField @ProtoNumber(21) val codecFormat: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoRetweetResp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val sameAreaOutAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(4) val diffAreaOutAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(5) val fileid: String = "",
        @JvmField @ProtoNumber(6) val ukey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val fileExist: Int = 0,
        @JvmField @ProtoNumber(8) val sameAreaInnerAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(9) val diffAreaInnerAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(10) val dataHole: List<DataHole> = emptyList(),
        @JvmField @ProtoNumber(11) val isHotFile: Int = 0,
        @JvmField @ProtoNumber(12) val longVideoCarryWatchPointType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoUploadReq(
        @JvmField @ProtoNumber(1) val fromuin: Long = 0L,
        @JvmField @ProtoNumber(2) val touin: Long = 0L,
        @JvmField @ProtoNumber(3) val chatType: Int = 0,
        @JvmField @ProtoNumber(4) val clientType: Int = 0,
        @JvmField @ProtoNumber(5) val msgPttShortVideoFileInfo: PttShortVideoFileInfo? = null,
        @JvmField @ProtoNumber(6) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(7) val agentType: Int = 0,
        @JvmField @ProtoNumber(8) val businessType: Int = 0,
        @JvmField @ProtoNumber(9) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(10) val subBusinessType: Int = 0,
        @JvmField @ProtoNumber(20) val flagSupportLargeSize: Int = 0,
        @JvmField @ProtoNumber(21) val codecFormat: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PttShortVideoUploadResp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val sameAreaOutAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(4) val diffAreaOutAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(5) val fileid: String = "",
        @JvmField @ProtoNumber(6) val ukey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val fileExist: Int = 0,
        @JvmField @ProtoNumber(8) val sameAreaInnerAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(9) val diffAreaInnerAddr: List<PttShortVideoIpList> = emptyList(),
        @JvmField @ProtoNumber(10) val dataHole: List<DataHole> = emptyList(),
        @JvmField @ProtoNumber(11) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(12) val isHotFile: Int = 0,
        @JvmField @ProtoNumber(13) val longVideoCarryWatchPointType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QuicParameter(
        @JvmField @ProtoNumber(1) val enableQuic: Int = 0,
        @JvmField @ProtoNumber(2) val encryptionVer: Int = 1,
        @JvmField @ProtoNumber(3) val fecVer: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val cmd: Int = 0,
        @JvmField @ProtoNumber(2) val seq: Int = 0,
        @JvmField @ProtoNumber(3) val msgPttShortVideoUploadReq: PttShortVideoUploadReq? = null,
        @JvmField @ProtoNumber(4) val msgPttShortVideoDownloadReq: PttShortVideoDownloadReq? = null,
        @JvmField @ProtoNumber(5) val msgShortVideoRetweetReq: List<PttShortVideoRetweetReq> = emptyList(),
        @JvmField @ProtoNumber(6) val msgShortVideoDeleteReq: List<PttShortVideoDeleteReq> = emptyList(),
        @JvmField @ProtoNumber(100) val msgExtensionReq: List<ExtensionReq> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val cmd: Int = 0,
        @JvmField @ProtoNumber(2) val seq: Int = 0,
        @JvmField @ProtoNumber(3) val msgPttShortVideoUploadResp: PttShortVideoUploadResp? = null,
        @JvmField @ProtoNumber(4) val msgPttShortVideoDownloadResp: PttShortVideoDownloadResp? = null,
        @JvmField @ProtoNumber(5) val msgShortVideoRetweetResp: List<PttShortVideoRetweetResp> = emptyList(),
        @JvmField @ProtoNumber(6) val msgShortVideoDeleteResp: List<PttShortVideoDeleteResp> = emptyList(),
        @JvmField @ProtoNumber(100) val changeChannel: Int = 0,
        @JvmField @ProtoNumber(101) val allowRetry: Int = 0
    ) : ProtoBuf
}