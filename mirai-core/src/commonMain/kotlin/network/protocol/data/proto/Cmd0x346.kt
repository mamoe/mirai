/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import kotlin.jvm.JvmField

@Serializable
internal class Cmd0x346 : ProtoBuf {
    @Serializable
    internal class AddrList(
        @JvmField @ProtoNumber(2) val strIp: List<String> = emptyList(),
        @JvmField @ProtoNumber(3) val strDomain: String = "",
        @JvmField @ProtoNumber(4) val port: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class ApplyCleanTrafficReq : ProtoBuf

    @Serializable
    internal class ApplyCleanTrafficRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ApplyCopyFromReq(
        @JvmField @ProtoNumber(10) val srcUin: Long = 0L,
        @JvmField @ProtoNumber(20) val srcGroup: Long = 0L,
        @JvmField @ProtoNumber(30) val srcSvcid: Int = 0,
        @JvmField @ProtoNumber(40) val srcParentfolder: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(50) val srcUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(60) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val dstUin: Long = 0L,
        @JvmField @ProtoNumber(80) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(90) val fileName: String = "",
        @JvmField @ProtoNumber(100) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(110) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyCopyFromRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(40) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyCopyToReq(
        @JvmField @ProtoNumber(10) val dstId: Long = 0L,
        @JvmField @ProtoNumber(20) val dstUin: Long = 0L,
        @JvmField @ProtoNumber(30) val dstSvcid: Int = 0,
        @JvmField @ProtoNumber(40) val srcUin: Long = 0L,
        @JvmField @ProtoNumber(50) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(60) val fileName: String = "",
        @JvmField @ProtoNumber(70) val localFilepath: String = "",
        @JvmField @ProtoNumber(80) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class ApplyCopyToRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val fileKey: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ApplyDownloadAbsReq(
        @JvmField @ProtoNumber(10) val uin: Long = 0L,
        @JvmField @ProtoNumber(20) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class ApplyDownloadAbsRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val msgDownloadInfo: Cmd0x346.DownloadInfo? = null,
    ) : ProtoBuf

    @Serializable
    internal class ApplyDownloadReq(
        @JvmField @ProtoNumber(10) val uin: Long = 0L,
        @JvmField @ProtoNumber(20) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(30) val ownerType: Int = 0,
        @JvmField @ProtoNumber(500) val extUintype: Int = 0,
        @JvmField @ProtoNumber(501) val needHttpsUrl: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class ApplyDownloadRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val msgDownloadInfo: Cmd0x346.DownloadInfo? = null,
        @JvmField @ProtoNumber(40) val msgFileInfo: Cmd0x346.FileInfo? = null,
    ) : ProtoBuf

    @Serializable
    internal class ApplyForwardFileReq(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(40) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(50) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyForwardFileRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(40) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(50) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class ApplyGetTrafficReq : ProtoBuf

    @Serializable
    internal class ApplyGetTrafficRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val useFileSize: Long = 0L,
        @JvmField @ProtoNumber(40) val useFileNum: Int = 0,
        @JvmField @ProtoNumber(50) val allFileSize: Long = 0L,
        @JvmField @ProtoNumber(60) val allFileNum: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class ApplyListDownloadReq(
        @JvmField @ProtoNumber(10) val uin: Long = 0L,
        @JvmField @ProtoNumber(20) val beginIndex: Int = 0,
        @JvmField @ProtoNumber(30) val reqCount: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class ApplyListDownloadRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val totalCount: Int = 0,
        @JvmField @ProtoNumber(40) val beginIndex: Int = 0,
        @JvmField @ProtoNumber(50) val rspCount: Int = 0,
        @JvmField @ProtoNumber(60) val isEnd: Int = 0,
        @JvmField @ProtoNumber(70) val msgFileList: List<Cmd0x346.FileInfo> = emptyList(),
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadHitReq(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(40) val fileName: String = "",
        @JvmField @ProtoNumber(50) val _10mMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(60) val localFilepath: String = "",
        @JvmField @ProtoNumber(70) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(80) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadHitReqV2(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(40) val fileName: String = "",
        @JvmField @ProtoNumber(50) val _10mMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(60) val _3sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(80) val localFilepath: String = "",
        @JvmField @ProtoNumber(90) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(100) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadHitReqV3(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(40) val fileName: String = "",
        @JvmField @ProtoNumber(50) val _10mMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(60) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val localFilepath: String = "",
        @JvmField @ProtoNumber(80) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(90) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadHitRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val uploadIp: String = "",
        @JvmField @ProtoNumber(40) val uploadPort: Int = 0,
        @JvmField @ProtoNumber(50) val uploadDomain: String = "",
        @JvmField @ProtoNumber(60) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val uploadKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(80) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(90) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(100) val uploadDns: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadHitRspV2(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val uploadIp: String = "",
        @JvmField @ProtoNumber(40) val uploadPort: Int = 0,
        @JvmField @ProtoNumber(50) val uploadDomain: String = "",
        @JvmField @ProtoNumber(60) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val uploadKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(80) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(90) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(100) val uploadHttpsPort: Int = 443,
        @JvmField @ProtoNumber(110) val uploadHttpsDomain: String = "",
        @JvmField @ProtoNumber(120) val uploadDns: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadHitRspV3(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val uploadIp: String = "",
        @JvmField @ProtoNumber(40) val uploadPort: Int = 0,
        @JvmField @ProtoNumber(50) val uploadDomain: String = "",
        @JvmField @ProtoNumber(60) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val uploadKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(80) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(90) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(100) val uploadDns: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadReq(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val fileType: Int = 0,
        @JvmField @ProtoNumber(40) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(50) val fileName: ByteArray = EMPTY_BYTE_ARRAY, //String = "",
        @JvmField @ProtoNumber(60) val _10mMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val localFilepath: String = "",
        @JvmField @ProtoNumber(80) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(90) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadReqV2(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(40) val fileName: String = "",
        @JvmField @ProtoNumber(50) val _10mMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(60) val _3sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val localFilepath: String = "",
        @JvmField @ProtoNumber(80) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(90) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadReqV3(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(40) val fileName: String = "",
        @JvmField @ProtoNumber(50) val _10mMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(60) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(70) val localFilepath: String = "",
        @JvmField @ProtoNumber(80) val dangerLevel: Int = 0,
        @JvmField @ProtoNumber(90) val totalSpace: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(40) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(50) val uploadedSize: Long = 0L,
        @JvmField @ProtoNumber(60) val uploadIp: String = "",
        @JvmField @ProtoNumber(70) val uploadDomain: String = "",
        @JvmField @ProtoNumber(80) val uploadPort: Int = 0,
        @JvmField @ProtoNumber(90) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(100) val uploadKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(110) val boolFileExist: Boolean = false,
        @JvmField @ProtoNumber(120) val packSize: Int = 0,
        @JvmField @ProtoNumber(130) val strUploadipList: List<String> = emptyList(),
        @JvmField @ProtoNumber(140) val uploadDns: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadRspV2(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(40) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(50) val uploadedSize: Long = 0L,
        @JvmField @ProtoNumber(60) val uploadIp: String = "",
        @JvmField @ProtoNumber(70) val uploadDomain: String = "",
        @JvmField @ProtoNumber(80) val uploadPort: Int = 0,
        @JvmField @ProtoNumber(90) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(100) val uploadKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(110) val boolFileExist: Boolean = false,
        @JvmField @ProtoNumber(120) val packSize: Int = 0,
        @JvmField @ProtoNumber(130) val strUploadipList: List<String> = emptyList(),
        @JvmField @ProtoNumber(140) val httpsvrApiVer: Int = 0,
        @JvmField @ProtoNumber(141) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(142) val uploadHttpsPort: Int = 443,
        @JvmField @ProtoNumber(143) val uploadHttpsDomain: String = "",
        @JvmField @ProtoNumber(150) val uploadDns: String = "",
        @JvmField @ProtoNumber(160) val uploadLanip: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ApplyUploadRspV3(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(40) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(50) val uploadedSize: Long = 0L,
        @JvmField @ProtoNumber(60) val uploadIp: String = "",
        @JvmField @ProtoNumber(70) val uploadDomain: String = "",
        @JvmField @ProtoNumber(80) val uploadPort: Int = 0,
        @JvmField @ProtoNumber(90) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(100) val uploadKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(110) val boolFileExist: Boolean = false,
        @JvmField @ProtoNumber(120) val packSize: Int = 0,
        @JvmField @ProtoNumber(130) val strUploadipList: List<String> = emptyList(),
        @JvmField @ProtoNumber(140) val uploadHttpsPort: Int = 443,
        @JvmField @ProtoNumber(150) val uploadHttpsDomain: String = "",
        @JvmField @ProtoNumber(160) val uploadDns: String = "",
        @JvmField @ProtoNumber(170) val uploadLanip: String = "",
    ) : ProtoBuf

    @Serializable
    internal class DeleteFileReq(
        @JvmField @ProtoNumber(10) val uin: Long = 0L,
        @JvmField @ProtoNumber(20) val peerUin: Long = 0L,
        @JvmField @ProtoNumber(30) val deleteType: Int = 0,
        @JvmField @ProtoNumber(40) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class DeleteFileRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
    ) : ProtoBuf

    @Serializable
    internal class DelMessageReq(
        @JvmField @ProtoNumber(1) val uinSender: Long = 0L,
        @JvmField @ProtoNumber(2) val uinReceiver: Long = 0L,
        @JvmField @ProtoNumber(10) val msgTime: Int = 0,
        @JvmField @ProtoNumber(20) val msgRandom: Int = 0,
        @JvmField @ProtoNumber(30) val msgSeqNo: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class DownloadInfo(
        @JvmField @ProtoNumber(10) val downloadKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(20) val downloadIp: String = "",
        @JvmField @ProtoNumber(30) val downloadDomain: String = "",
        @JvmField @ProtoNumber(40) val port: Int = 0,
        @JvmField @ProtoNumber(50) val downloadUrl: String = "",
        @JvmField @ProtoNumber(60) val strDownloadipList: List<String> = emptyList(),
        @JvmField @ProtoNumber(70) val cookie: String = "",
        @JvmField @ProtoNumber(80) val httpsPort: Int = 443,
        @JvmField @ProtoNumber(90) val httpsDownloadDomain: String = "",
        @JvmField @ProtoNumber(110) val downloadDns: String = "",
    ) : ProtoBuf

    @Serializable
    internal class DownloadSuccReq(
        @JvmField @ProtoNumber(10) val uin: Long = 0L,
        @JvmField @ProtoNumber(20) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class DownloadSuccRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val int32DownStat: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class ExtensionReq(
        @JvmField @ProtoNumber(1) val id: Long = 0L,
        @JvmField @ProtoNumber(2) val type: Long = 0L,
        @JvmField @ProtoNumber(3) val dstPhonenum: String = "",
        @JvmField @ProtoNumber(4) val int32PhoneConvertType: Int = 0,
        @JvmField @ProtoNumber(20) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(100) val routeId: Long = 0L,
        @JvmField @ProtoNumber(90100) val msgDelMessageReq: Cmd0x346.DelMessageReq? = null,
        @JvmField @ProtoNumber(90200) val downloadUrlType: Int = 0,
        @JvmField @ProtoNumber(90300) val pttFormat: Int = 0,
        @JvmField @ProtoNumber(90400) val isNeedInnerIp: Int = 0,
        @JvmField @ProtoNumber(90500) val netType: Int = 255,
        @JvmField @ProtoNumber(90600) val voiceType: Int = 0,
        @JvmField @ProtoNumber(90700) val fileType: Int = 0,
        @JvmField @ProtoNumber(90800) val pttTime: Int = 0,
        @JvmField @ProtoNumber(90900) val bdhCmdid: Int = 0,
        @JvmField @ProtoNumber(91000) val reqTransferType: Int = 0,
        @JvmField @ProtoNumber(91100) val isAuto: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class ExtensionRsp(
        @JvmField @ProtoNumber(1) val transferType: Int = 0,
        @JvmField @ProtoNumber(2) val channelType: Int = 0,
        @JvmField @ProtoNumber(3) val allowRetry: Int = 0,
        @JvmField @ProtoNumber(4) val serverAddrIpv6List: Cmd0x346.AddrList? = null,
    ) : ProtoBuf

    @Serializable
    internal class FileInfo(
        @JvmField @ProtoNumber(1) val uin: Long = 0L,
        @JvmField @ProtoNumber(2) val dangerEvel: Int = 0,
        @JvmField @ProtoNumber(3) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(4) val lifeTime: Int = 0,
        @JvmField @ProtoNumber(5) val uploadTime: Int = 0,
        @JvmField @ProtoNumber(6) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val fileName: String = "",
        @JvmField @ProtoNumber(90) val absFileType: Int = 0,
        @JvmField @ProtoNumber(100) val _10mMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(101) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(110) val clientType: Int = 0,
        @JvmField @ProtoNumber(120) val ownerUin: Long = 0L,
        @JvmField @ProtoNumber(121) val peerUin: Long = 0L,
        @JvmField @ProtoNumber(130) val expireTime: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class FileQueryReq(
        @JvmField @ProtoNumber(10) val uin: Long = 0L,
        @JvmField @ProtoNumber(20) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class FileQueryRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val msgFileInfo: Cmd0x346.FileInfo? = null,
    ) : ProtoBuf

    @Serializable
    internal class RecallFileReq(
        @JvmField @ProtoNumber(1) val uin: Long = 0L,
        @JvmField @ProtoNumber(2) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class RecallFileRsp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
    ) : ProtoBuf

    @Serializable
    internal class RecvListQueryReq(
        @JvmField @ProtoNumber(1) val uin: Long = 0L,
        @JvmField @ProtoNumber(2) val beginIndex: Int = 0,
        @JvmField @ProtoNumber(3) val reqCount: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class RecvListQueryRsp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val fileTotCount: Int = 0,
        @JvmField @ProtoNumber(4) val beginIndex: Int = 0,
        @JvmField @ProtoNumber(5) val rspFileCount: Int = 0,
        @JvmField @ProtoNumber(6) val isEnd: Int = 0,
        @JvmField @ProtoNumber(7) val msgFileList: List<Cmd0x346.FileInfo> = emptyList(),
    ) : ProtoBuf

    @Serializable
    internal class RenewFileReq(
        @JvmField @ProtoNumber(1) val uin: Long = 0L,
        @JvmField @ProtoNumber(2) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val addTtl: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class RenewFileRsp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val cmd: Int = 0,
        @JvmField @ProtoNumber(2) val seq: Int = 0,
        @JvmField @ProtoNumber(3) val msgRecvListQueryReq: Cmd0x346.RecvListQueryReq? = null,
        @JvmField @ProtoNumber(4) val msgSendListQueryReq: Cmd0x346.SendListQueryReq? = null,
        @JvmField @ProtoNumber(5) val msgRenewFileReq: Cmd0x346.RenewFileReq? = null,
        @JvmField @ProtoNumber(6) val msgRecallFileReq: Cmd0x346.RecallFileReq? = null,
        @JvmField @ProtoNumber(7) val msgApplyUploadReq: Cmd0x346.ApplyUploadReq? = null,
        @JvmField @ProtoNumber(8) val msgApplyUploadHitReq: Cmd0x346.ApplyUploadHitReq? = null,
        @JvmField @ProtoNumber(9) val msgApplyForwardFileReq: Cmd0x346.ApplyForwardFileReq? = null,
        @JvmField @ProtoNumber(10) val msgUploadSuccReq: Cmd0x346.UploadSuccReq? = null,
        @JvmField @ProtoNumber(11) val msgDeleteFileReq: Cmd0x346.DeleteFileReq? = null,
        @JvmField @ProtoNumber(12) val msgDownloadSuccReq: Cmd0x346.DownloadSuccReq? = null,
        @JvmField @ProtoNumber(13) val msgApplyDownloadAbsReq: Cmd0x346.ApplyDownloadAbsReq? = null,
        @JvmField @ProtoNumber(14) val msgApplyDownloadReq: Cmd0x346.ApplyDownloadReq? = null,
        @JvmField @ProtoNumber(15) val msgApplyListDownloadReq: Cmd0x346.ApplyListDownloadReq? = null,
        @JvmField @ProtoNumber(16) val msgFileQueryReq: Cmd0x346.FileQueryReq? = null,
        @JvmField @ProtoNumber(17) val msgApplyCopyFromReq: Cmd0x346.ApplyCopyFromReq? = null,
        @JvmField @ProtoNumber(18) val msgApplyUploadReqV2: Cmd0x346.ApplyUploadReqV2? = null,
        @JvmField @ProtoNumber(19) val msgApplyUploadReqV3: Cmd0x346.ApplyUploadReqV3? = null,
        @JvmField @ProtoNumber(20) val msgApplyUploadHitReqV2: Cmd0x346.ApplyUploadHitReqV2? = null,
        @JvmField @ProtoNumber(21) val msgApplyUploadHitReqV3: Cmd0x346.ApplyUploadHitReqV3? = null,
        @JvmField @ProtoNumber(101) val businessId: Int = 0,
        @JvmField @ProtoNumber(102) val clientType: Int = 0,
        @JvmField @ProtoNumber(90000) val msgApplyCopyToReq: Cmd0x346.ApplyCopyToReq? = null,
        @JvmField @ProtoNumber(90001) val msgApplyCleanTrafficReq: Cmd0x346.ApplyCleanTrafficReq? = null,
        @JvmField @ProtoNumber(90002) val msgApplyGetTrafficReq: Cmd0x346.ApplyGetTrafficReq? = null,
        @JvmField @ProtoNumber(99999) val msgExtensionReq: Cmd0x346.ExtensionReq? = null,
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val cmd: Int = 0,
        @JvmField @ProtoNumber(2) val seq: Int = 0,
        @JvmField @ProtoNumber(3) val msgRecvListQueryRsp: Cmd0x346.RecvListQueryRsp? = null,
        @JvmField @ProtoNumber(4) val msgSendListQueryRsp: Cmd0x346.SendListQueryRsp? = null,
        @JvmField @ProtoNumber(5) val msgRenewFileRsp: Cmd0x346.RenewFileRsp? = null,
        @JvmField @ProtoNumber(6) val msgRecallFileRsp: Cmd0x346.RecallFileRsp? = null,
        @JvmField @ProtoNumber(7) val msgApplyUploadRsp: Cmd0x346.ApplyUploadRsp? = null,
        @JvmField @ProtoNumber(8) val msgApplyUploadHitRsp: Cmd0x346.ApplyUploadHitRsp? = null,
        @JvmField @ProtoNumber(9) val msgApplyForwardFileRsp: Cmd0x346.ApplyForwardFileRsp? = null,
        @JvmField @ProtoNumber(10) val msgUploadSuccRsp: Cmd0x346.UploadSuccRsp? = null,
        @JvmField @ProtoNumber(11) val msgDeleteFileRsp: Cmd0x346.DeleteFileRsp? = null,
        @JvmField @ProtoNumber(12) val msgDownloadSuccRsp: Cmd0x346.DownloadSuccRsp? = null,
        @JvmField @ProtoNumber(13) val msgApplyDownloadAbsRsp: Cmd0x346.ApplyDownloadAbsRsp? = null,
        @JvmField @ProtoNumber(14) val msgApplyDownloadRsp: Cmd0x346.ApplyDownloadRsp? = null,
        @JvmField @ProtoNumber(15) val msgApplyListDownloadRsp: Cmd0x346.ApplyListDownloadRsp? = null,
        @JvmField @ProtoNumber(16) val msgFileQueryRsp: Cmd0x346.FileQueryRsp? = null,
        @JvmField @ProtoNumber(17) val msgApplyCopyFromRsp: Cmd0x346.ApplyCopyFromRsp? = null,
        @JvmField @ProtoNumber(18) val msgApplyUploadRspV2: Cmd0x346.ApplyUploadRspV2? = null,
        @JvmField @ProtoNumber(19) val msgApplyUploadRspV3: Cmd0x346.ApplyUploadRspV3? = null,
        @JvmField @ProtoNumber(20) val msgApplyUploadHitRspV2: Cmd0x346.ApplyUploadHitRspV2? = null,
        @JvmField @ProtoNumber(21) val msgApplyUploadHitRspV3: Cmd0x346.ApplyUploadHitRspV3? = null,
        @JvmField @ProtoNumber(90000) val msgApplyCopyToRsp: Cmd0x346.ApplyCopyToRsp? = null,
        @JvmField @ProtoNumber(90001) val msgApplyCleanTrafficRsp: Cmd0x346.ApplyCleanTrafficRsp? = null,
        @JvmField @ProtoNumber(90002) val msgApplyGetTrafficRsp: Cmd0x346.ApplyGetTrafficRsp? = null,
        @JvmField @ProtoNumber(99999) val msgExtensionRsp: Cmd0x346.ExtensionRsp? = null,
    ) : ProtoBuf

    @Serializable
    internal class SendListQueryReq(
        @JvmField @ProtoNumber(1) val uin: Long = 0L,
        @JvmField @ProtoNumber(2) val beginIndex: Int = 0,
        @JvmField @ProtoNumber(3) val reqCount: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class SendListQueryRsp(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val fileTotCount: Int = 0,
        @JvmField @ProtoNumber(4) val beginIndex: Int = 0,
        @JvmField @ProtoNumber(5) val rspFileCount: Int = 0,
        @JvmField @ProtoNumber(6) val isEnd: Int = 0,
        @JvmField @ProtoNumber(7) val totLimit: Long = 0L,
        @JvmField @ProtoNumber(8) val usedLimit: Long = 0L,
        @JvmField @ProtoNumber(9) val msgFileList: List<Cmd0x346.FileInfo> = emptyList(),
    ) : ProtoBuf

    @Serializable
    internal class UploadSuccReq(
        @JvmField @ProtoNumber(10) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(20) val recverUin: Long = 0L,
        @JvmField @ProtoNumber(30) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
    ) : ProtoBuf

    @Serializable
    internal class UploadSuccRsp(
        @JvmField @ProtoNumber(10) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(20) val retMsg: String = "",
        @JvmField @ProtoNumber(30) val msgFileInfo: Cmd0x346.FileInfo? = null,
    ) : ProtoBuf
}
        