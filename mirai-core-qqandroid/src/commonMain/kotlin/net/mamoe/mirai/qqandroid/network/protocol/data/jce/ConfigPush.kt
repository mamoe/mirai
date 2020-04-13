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
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.JceId

@Serializable
internal class BigDataChannel(
    @JceId(0) val vBigdataIplists: List<BigDataIpList>,
    @JceId(1) val sBigdataSigSession: ByteArray? = null,
    @JceId(2) val sBigdataKeySession: ByteArray? = null,
    @JceId(3) val uSigUin: Long? = null,
    @JceId(4) val iConnectFlag: Int? = 1,
    @JceId(5) val vBigdataPbBuf: ByteArray? = null
) : JceStruct

@Serializable
internal class BigDataIpInfo(
    @JceId(0) val uType: Long,
    @JceId(1) val sIp: String = "",
    @JceId(2) val uPort: Long
) : JceStruct

@Serializable
internal class BigDataIpList(
    @JceId(0) val uServiceType: Long,
    @JceId(1) val vIplist: List<BigDataIpInfo>,
    @JceId(2) val netSegConfs: List<NetSegConf>? = null,
    @JceId(3) val ufragmentSize: Long? = null
) : JceStruct

@Serializable
internal class ClientLogConfig(
    @JceId(1) val type: Int,
    @JceId(2) val timeStart: TimeStamp? = null,
    @JceId(3) val timeFinish: TimeStamp? = null,
    @JceId(4) val loglevel: Byte? = null,
    @JceId(5) val cookie: Int? = null,
    @JceId(6) val lseq: Long? = null
) : JceStruct

@Serializable
internal class DomainIpChannel(
    @JceId(0) val vDomainIplists: List<DomainIpList>
) : JceStruct

@Serializable
internal class DomainIpInfo(
    @JceId(1) val uIp: Int,
    @JceId(2) val uPort: Int
) : JceStruct

@Serializable
internal class DomainIpList(
    @JceId(0) val uDomainType: Int,
    @JceId(1) val vIplist: List<DomainIpInfo>,
    @JceId(2) val unknown: ByteArray? = null,
    @JceId(4) val int: Int? = null// added
) : JceStruct

@Serializable
internal class _340(
    @JceId(1) val field1315: List<_339>,
    @JceId(3) val field1316: List<_339>,
    @JceId(4) val field1317: Int,
    @JceId(5) val field1318: Byte? = 0,
    @JceId(6) val field1319: Byte? = 0,
    @JceId(7) val field1320: Int? = 1,
    @JceId(8) val field1321: List<_339>? = null,
    @JceId(9) val field1322: List<_339>? = null,
    @JceId(10) val field1323: List<_339>? = null,
    @JceId(11) val field1324: List<_339>? = null,
    @JceId(12) val field1325: List<_339>? = null,
    @JceId(13) val field1326: List<_339>? = null,
    @JceId(14) val netType: Byte? = 0,
    @JceId(15) val heThreshold: Int? = 0,
    @JceId(16) val policyId: String? = ""
) : JceStruct

@Serializable
internal class _339(
    @JceId(1) val field1298: String = "",
    @JceId(2) val field1299: Int = 0,
    @JceId(3) val field1300: Byte = 0,
    @JceId(4) val field1301: Byte = 0,
    @JceId(5) val field1302: Byte? = 0,
    @JceId(6) val field1303: Int? = 8,
    @JceId(7) val field1304: Byte? = 0,
    @JceId(8) val field1305: String = "",
    @JceId(9) val field1306: String = ""
) : JceStruct

@Serializable
internal class FileStoragePushFSSvcList(
    @JceId(0) val vUpLoadList: List<FileStorageServerListInfo>? = listOf(),
    @JceId(1) val vPicDownLoadList: List<FileStorageServerListInfo>? = listOf(),
    @JceId(2) val vGPicDownLoadList: List<FileStorageServerListInfo>? = null,
    @JceId(3) val vQzoneProxyServiceList: List<FileStorageServerListInfo>? = null,
    @JceId(4) val vUrlEncodeServiceList: List<FileStorageServerListInfo>? = null,
    @JceId(5) val bigDataChannel: BigDataChannel? = null,
    @JceId(6) val vVipEmotionList: List<FileStorageServerListInfo>? = null,
    @JceId(7) val vC2CPicDownList: List<FileStorageServerListInfo>? = null,
    @JceId(8) val fmtIPInfo: FmtIPInfo? = null,
    @JceId(9) val domainIpChannel: DomainIpChannel? = null,
    @JceId(10) val pttlist: ByteArray? = null
) : JceStruct

@Serializable
internal class FileStorageServerListInfo(
    @JceId(1) val sIP: String = "",
    @JceId(2) val iPort: Int
) : JceStruct

@Serializable
internal class FmtIPInfo(
    @JceId(0) val sGateIp: String = "",
    @JceId(1) val iGateIpOper: Long
) : JceStruct

@Serializable
internal class NetSegConf(
    @JceId(0) val uint32NetType: Long? = null,
    @JceId(1) val uint32Segsize: Long? = null,
    @JceId(2) val uint32Segnum: Long? = null,
    @JceId(3) val uint32Curconnnum: Long? = null
) : JceStruct

@Suppress("ArrayInDataClass")
@Serializable
internal class PushReq(
    @JceId(1) val type: Int,
    @JceId(2) val jcebuf: ByteArray,
    @JceId(3) val seq: Long
) : JceStruct, Packet

@Serializable
internal class PushResp(
    @JceId(1) val type: Int,
    @JceId(2) val seq: Long,
    @JceId(3) val jcebuf: ByteArray? = null
) : JceStruct

@Serializable
internal class SsoServerList(
    @JceId(1) val v2G3GList: List<SsoServerListInfo>,
    @JceId(3) val vWifiList: List<SsoServerListInfo>,
    @JceId(4) val iReconnect: Int,
    @JceId(5) val testSpeed: Byte? = null,
    @JceId(6) val useNewList: Byte? = null,
    @JceId(7) val iMultiConn: Int? = 1,
    @JceId(8) val vHttp2g3glist: List<SsoServerListInfo>? = null,
    @JceId(9) val vHttpWifilist: List<SsoServerListInfo>? = null
) : JceStruct

@Serializable
internal class SsoServerListInfo(
    @JceId(1) val sIP: String = "",
    @JceId(2) val iPort: Int,
    @JceId(3) val linkType: Byte,
    @JceId(4) val proxy: Byte,
    @JceId(5) val protocolType: Byte? = null,
    @JceId(6) val iTimeOut: Int? = 10
) : JceStruct

@Serializable
internal class TimeStamp(
    @JceId(1) val year: Int,
    @JceId(2) val month: Byte,
    @JceId(3) val day: Byte,
    @JceId(4) val hour: Byte
) : JceStruct
