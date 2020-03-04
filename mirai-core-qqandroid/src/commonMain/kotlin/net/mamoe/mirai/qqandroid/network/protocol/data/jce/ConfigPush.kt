/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class BigDataChannel(
    @SerialId(0) val vBigdataIplists: List<BigDataIpList>,
    @SerialId(1) val sBigdataSigSession: ByteArray? = null,
    @SerialId(2) val sBigdataKeySession: ByteArray? = null,
    @SerialId(3) val uSigUin: Long? = null,
    @SerialId(4) val iConnectFlag: Int? = 1,
    @SerialId(5) val vBigdataPbBuf: ByteArray? = null
) : JceStruct

@Serializable
internal class BigDataIpInfo(
    @SerialId(0) val uType: Long,
    @SerialId(1) val sIp: String = "",
    @SerialId(2) val uPort: Long
) : JceStruct

@Serializable
internal class BigDataIpList(
    @SerialId(0) val uServiceType: Long,
    @SerialId(1) val vIplist: List<BigDataIpInfo>,
    @SerialId(2) val netSegConfs: List<NetSegConf>? = null,
    @SerialId(3) val ufragmentSize: Long? = null
) : JceStruct

@Serializable
internal class ClientLogConfig(
    @SerialId(1) val type: Int,
    @SerialId(2) val timeStart: TimeStamp? = null,
    @SerialId(3) val timeFinish: TimeStamp? = null,
    @SerialId(4) val loglevel: Byte? = null,
    @SerialId(5) val cookie: Int? = null,
    @SerialId(6) val lseq: Long? = null
) : JceStruct

@Serializable
internal class DomainIpChannel(
    @SerialId(0) val vDomainIplists: List<DomainIpList>
) : JceStruct

@Serializable
internal class DomainIpInfo(
    @SerialId(1) val uIp: Int,
    @SerialId(2) val uPort: Int
) : JceStruct

@Serializable
internal class DomainIpList(
    @SerialId(0) val uDomainType: Int,
    @SerialId(1) val vIplist: List<DomainIpInfo>
) : JceStruct

@Serializable
internal class FileStoragePushFSSvcList(
    @SerialId(0) val vUpLoadList: List<FileStorageServerListInfo>,
    @SerialId(1) val vPicDownLoadList: List<FileStorageServerListInfo>,
    @SerialId(2) val vGPicDownLoadList: List<FileStorageServerListInfo>? = null,
    @SerialId(3) val vQzoneProxyServiceList: List<FileStorageServerListInfo>? = null,
    @SerialId(4) val vUrlEncodeServiceList: List<FileStorageServerListInfo>? = null,
    @SerialId(5) val bigDataChannel: BigDataChannel? = null,
    @SerialId(6) val vVipEmotionList: List<FileStorageServerListInfo>? = null,
    @SerialId(7) val vC2CPicDownList: List<FileStorageServerListInfo>? = null,
    @SerialId(8) val fmtIPInfo: FmtIPInfo? = null,
    @SerialId(9) val domainIpChannel: DomainIpChannel? = null,
    @SerialId(10) val pttlist: ByteArray? = null
) : JceStruct

@Serializable
internal class FileStorageServerListInfo(
    @SerialId(1) val sIP: String = "",
    @SerialId(2) val iPort: Int
) : JceStruct

@Serializable
internal class FmtIPInfo(
    @SerialId(0) val sGateIp: String = "",
    @SerialId(1) val iGateIpOper: Long
) : JceStruct

@Serializable
internal class NetSegConf(
    @SerialId(0) val uint32NetType: Long? = null,
    @SerialId(1) val uint32Segsize: Long? = null,
    @SerialId(2) val uint32Segnum: Long? = null,
    @SerialId(3) val uint32Curconnnum: Long? = null
) : JceStruct

@Suppress("ArrayInDataClass")
@Serializable
internal data class PushReq(
    @SerialId(1) val type: Int,
    @SerialId(2) val jcebuf: ByteArray,
    @SerialId(3) val seq: Long
) : JceStruct, Packet

@Serializable
internal class PushResp(
    @SerialId(1) val type: Int,
    @SerialId(2) val seq: Long,
    @SerialId(3) val jcebuf: ByteArray? = null
) : JceStruct

@Serializable
internal class SsoServerList(
    @SerialId(1) val v2G3GList: List<SsoServerListInfo>,
    @SerialId(3) val vWifiList: List<SsoServerListInfo>,
    @SerialId(4) val iReconnect: Int,
    @SerialId(5) val testSpeed: Byte? = null,
    @SerialId(6) val useNewList: Byte? = null,
    @SerialId(7) val iMultiConn: Int? = 1,
    @SerialId(8) val vHttp2g3glist: List<SsoServerListInfo>? = null,
    @SerialId(9) val vHttpWifilist: List<SsoServerListInfo>? = null
) : JceStruct

@Serializable
internal class SsoServerListInfo(
    @SerialId(1) val sIP: String = "",
    @SerialId(2) val iPort: Int,
    @SerialId(3) val linkType: Byte,
    @SerialId(4) val proxy: Byte,
    @SerialId(5) val protocolType: Byte? = null,
    @SerialId(6) val iTimeOut: Int? = 10
) : JceStruct

@Serializable
internal class TimeStamp(
    @SerialId(1) val year: Int,
    @SerialId(2) val month: Byte,
    @SerialId(3) val day: Byte,
    @SerialId(4) val hour: Byte
) : JceStruct
