/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class BigDataChannel(
    @ProtoId(0) val vBigdataIplists: List<BigDataIpList>,
    @ProtoId(1) val sBigdataSigSession: ByteArray? = null,
    @ProtoId(2) val sBigdataKeySession: ByteArray? = null,
    @ProtoId(3) val uSigUin: Long? = null,
    @ProtoId(4) val iConnectFlag: Int? = 1,
    @ProtoId(5) val vBigdataPbBuf: ByteArray? = null
) : JceStruct

@Serializable
internal class BigDataIpInfo(
    @ProtoId(0) val uType: Long,
    @ProtoId(1) val sIp: String = "",
    @ProtoId(2) val uPort: Long
) : JceStruct

@Serializable
internal class BigDataIpList(
    @ProtoId(0) val uServiceType: Long,
    @ProtoId(1) val vIplist: List<BigDataIpInfo>,
    @ProtoId(2) val netSegConfs: List<NetSegConf>? = null,
    @ProtoId(3) val ufragmentSize: Long? = null
) : JceStruct

@Serializable
internal class ClientLogConfig(
    @ProtoId(1) val type: Int,
    @ProtoId(2) val timeStart: TimeStamp? = null,
    @ProtoId(3) val timeFinish: TimeStamp? = null,
    @ProtoId(4) val loglevel: Byte? = null,
    @ProtoId(5) val cookie: Int? = null,
    @ProtoId(6) val lseq: Long? = null
) : JceStruct

@Serializable
internal class DomainIpChannel(
    @ProtoId(0) val vDomainIplists: List<DomainIpList>
) : JceStruct

@Serializable
internal class DomainIpInfo(
    @ProtoId(1) val uIp: Int,
    @ProtoId(2) val uPort: Int
) : JceStruct

@Serializable
internal class DomainIpList(
    @ProtoId(0) val uDomainType: Int,
    @ProtoId(1) val vIplist: List<DomainIpInfo>
) : JceStruct

@Serializable
internal class FileStoragePushFSSvcList(
    @ProtoId(0) val vUpLoadList: List<FileStorageServerListInfo>,
    @ProtoId(1) val vPicDownLoadList: List<FileStorageServerListInfo>,
    @ProtoId(2) val vGPicDownLoadList: List<FileStorageServerListInfo>? = null,
    @ProtoId(3) val vQzoneProxyServiceList: List<FileStorageServerListInfo>? = null,
    @ProtoId(4) val vUrlEncodeServiceList: List<FileStorageServerListInfo>? = null,
    @ProtoId(5) val bigDataChannel: BigDataChannel? = null,
    @ProtoId(6) val vVipEmotionList: List<FileStorageServerListInfo>? = null,
    @ProtoId(7) val vC2CPicDownList: List<FileStorageServerListInfo>? = null,
    @ProtoId(8) val fmtIPInfo: FmtIPInfo? = null,
    @ProtoId(9) val domainIpChannel: DomainIpChannel? = null,
    @ProtoId(10) val pttlist: ByteArray? = null
) : JceStruct

@Serializable
internal class FileStorageServerListInfo(
    @ProtoId(1) val sIP: String = "",
    @ProtoId(2) val iPort: Int
) : JceStruct

@Serializable
internal class FmtIPInfo(
    @ProtoId(0) val sGateIp: String = "",
    @ProtoId(1) val iGateIpOper: Long
) : JceStruct

@Serializable
internal class NetSegConf(
    @ProtoId(0) val uint32NetType: Long? = null,
    @ProtoId(1) val uint32Segsize: Long? = null,
    @ProtoId(2) val uint32Segnum: Long? = null,
    @ProtoId(3) val uint32Curconnnum: Long? = null
) : JceStruct

@Suppress("ArrayInDataClass")
@Serializable
internal data class PushReq(
    @ProtoId(1) val type: Int,
    @ProtoId(2) val jcebuf: ByteArray,
    @ProtoId(3) val seq: Long
) : JceStruct, Packet

@Serializable
internal class PushResp(
    @ProtoId(1) val type: Int,
    @ProtoId(2) val seq: Long,
    @ProtoId(3) val jcebuf: ByteArray? = null
) : JceStruct

@Serializable
internal class SsoServerList(
    @ProtoId(1) val v2G3GList: List<SsoServerListInfo>,
    @ProtoId(3) val vWifiList: List<SsoServerListInfo>,
    @ProtoId(4) val iReconnect: Int,
    @ProtoId(5) val testSpeed: Byte? = null,
    @ProtoId(6) val useNewList: Byte? = null,
    @ProtoId(7) val iMultiConn: Int? = 1,
    @ProtoId(8) val vHttp2g3glist: List<SsoServerListInfo>? = null,
    @ProtoId(9) val vHttpWifilist: List<SsoServerListInfo>? = null
) : JceStruct

@Serializable
internal class SsoServerListInfo(
    @ProtoId(1) val sIP: String = "",
    @ProtoId(2) val iPort: Int,
    @ProtoId(3) val linkType: Byte,
    @ProtoId(4) val proxy: Byte,
    @ProtoId(5) val protocolType: Byte? = null,
    @ProtoId(6) val iTimeOut: Int? = 10
) : JceStruct

@Serializable
internal class TimeStamp(
    @ProtoId(1) val year: Int,
    @ProtoId(2) val month: Byte,
    @ProtoId(3) val day: Byte,
    @ProtoId(4) val hour: Byte
) : JceStruct
