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
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class BigDataChannel(
    @JceId(0) @JvmField val vBigdataIplists: List<BigDataIpList>,
    @JceId(1) @JvmField val sBigdataSigSession: ByteArray? = null,
    @JceId(2) @JvmField val sBigdataKeySession: ByteArray? = null,
    @JceId(3) @JvmField val uSigUin: Long? = null,
    @JceId(4) @JvmField val iConnectFlag: Int? = 1,
    @JceId(5) @JvmField val vBigdataPbBuf: ByteArray? = null
) : JceStruct

@Serializable
internal class BigDataIpInfo(
    @JceId(0) @JvmField val uType: Long,
    @JceId(1) @JvmField val sIp: String = "",
    @JceId(2) @JvmField val uPort: Long
) : JceStruct

@Serializable
internal class BigDataIpList(
    @JceId(0) @JvmField val uServiceType: Long,
    @JceId(1) @JvmField val vIplist: List<BigDataIpInfo>,
    @JceId(2) @JvmField val netSegConfs: List<NetSegConf>? = null,
    @JceId(3) @JvmField val ufragmentSize: Long? = null
) : JceStruct

@Serializable
internal class ClientLogConfig(
    @JceId(1) @JvmField val type: Int,
    @JceId(2) @JvmField val timeStart: TimeStamp? = null,
    @JceId(3) @JvmField val timeFinish: TimeStamp? = null,
    @JceId(4) @JvmField val loglevel: Byte? = null,
    @JceId(5) @JvmField val cookie: Int? = null,
    @JceId(6) @JvmField val lseq: Long? = null
) : JceStruct

@Serializable
internal class DomainIpChannel(
    @JceId(0) @JvmField val vDomainIplists: List<DomainIpList>
) : JceStruct

@Serializable
internal class DomainIpInfo(
    @JceId(1) @JvmField val uIp: Int,
    @JceId(2) @JvmField val uPort: Int
) : JceStruct

@Serializable
internal class DomainIpList(
    @JceId(0) @JvmField val uDomainType: Int,
    @JceId(1) @JvmField val vIplist: List<DomainIpInfo>,
    @JceId(2) @JvmField val unknown: ByteArray? = null,
    @JceId(4) @JvmField val int: Int? = null// added
) : JceStruct

@Serializable
internal class _340(
    @JceId(1) @JvmField val field1315: List<_339>,
    @JceId(3) @JvmField val field1316: List<_339>,
    @JceId(4) @JvmField val field1317: Int,
    @JceId(5) @JvmField val field1318: Byte? = 0,
    @JceId(6) @JvmField val field1319: Byte? = 0,
    @JceId(7) @JvmField val field1320: Int? = 1,
    @JceId(8) @JvmField val field1321: List<_339>? = null,
    @JceId(9) @JvmField val field1322: List<_339>? = null,
    @JceId(10) @JvmField val field1323: List<_339>? = null,
    @JceId(11) @JvmField val field1324: List<_339>? = null,
    @JceId(12) @JvmField val field1325: List<_339>? = null,
    @JceId(13) @JvmField val field1326: List<_339>? = null,
    @JceId(14) @JvmField val netType: Byte? = 0,
    @JceId(15) @JvmField val heThreshold: Int? = 0,
    @JceId(16) @JvmField val policyId: String? = ""
) : JceStruct

@Serializable
internal class _339(
    @JceId(1) @JvmField val field1298: String = "",
    @JceId(2) @JvmField val field1299: Int = 0,
    @JceId(3) @JvmField val field1300: Byte = 0,
    @JceId(4) @JvmField val field1301: Byte = 0,
    @JceId(5) @JvmField val field1302: Byte? = 0,
    @JceId(6) @JvmField val field1303: Int? = 8,
    @JceId(7) @JvmField val field1304: Byte? = 0,
    @JceId(8) @JvmField val field1305: String = "",
    @JceId(9) @JvmField val field1306: String = ""
) : JceStruct

@Serializable
internal class FileStoragePushFSSvcListFuckKotlin(
    @JceId(0) @JvmField val vUpLoadList: List<FileStorageServerListInfo>? = listOf(),
    @JceId(1) @JvmField val vPicDownLoadList: List<FileStorageServerListInfo>? = listOf(),
    @JceId(2) @JvmField val vGPicDownLoadList: List<FileStorageServerListInfo>? = null,
    @JceId(3) @JvmField val vQzoneProxyServiceList: List<FileStorageServerListInfo>? = null,
    @JceId(4) @JvmField val vUrlEncodeServiceList: List<FileStorageServerListInfo>? = null,
    @JceId(5) @JvmField val bigDataChannel: BigDataChannel? = null,
    @JceId(6) @JvmField val vVipEmotionList: List<FileStorageServerListInfo>? = null,
    @JceId(7) @JvmField val vC2CPicDownList: List<FileStorageServerListInfo>? = null,
    @JceId(8) @JvmField val fmtIPInfo: FmtIPInfo? = null,
    @JceId(9) @JvmField val domainIpChannel: DomainIpChannel? = null,
    @JceId(10) @JvmField val pttlist: ByteArray? = null
) : JceStruct

@Serializable
internal class FileStorageServerListInfo(
    @JceId(1) @JvmField val sIP: String = "",
    @JceId(2) @JvmField val iPort: Int
) : JceStruct

@Serializable
internal class FmtIPInfo(
    @JceId(0) @JvmField val sGateIp: String = "",
    @JceId(1) @JvmField val iGateIpOper: Long
) : JceStruct

@Serializable
internal class NetSegConf(
    @JceId(0) @JvmField val uint32NetType: Long? = null,
    @JceId(1) @JvmField val uint32Segsize: Long? = null,
    @JceId(2) @JvmField val uint32Segnum: Long? = null,
    @JceId(3) @JvmField val uint32Curconnnum: Long? = null
) : JceStruct

@Suppress("ArrayInDataClass")
@Serializable
internal class PushReq(
    @JceId(1) @JvmField val type: Int,
    @JceId(2) @JvmField val jcebuf: ByteArray,
    @JceId(3) @JvmField val seq: Long
) : JceStruct, Packet

@Serializable
internal class PushResp(
    @JceId(1) @JvmField val type: Int,
    @JceId(2) @JvmField val seq: Long,
    @JceId(3) @JvmField val jcebuf: ByteArray? = null
) : JceStruct

@Serializable
internal class SsoServerList(
    @JceId(1) @JvmField val v2G3GList: List<SsoServerListInfo>,
    @JceId(3) @JvmField val vWifiList: List<SsoServerListInfo>,
    @JceId(4) @JvmField val iReconnect: Int,
    @JceId(5) @JvmField val testSpeed: Byte? = null,
    @JceId(6) @JvmField val useNewList: Byte? = null,
    @JceId(7) @JvmField val iMultiConn: Int? = 1,
    @JceId(8) @JvmField val vHttp2g3glist: List<SsoServerListInfo>? = null,
    @JceId(9) @JvmField val vHttpWifilist: List<SsoServerListInfo>? = null
) : JceStruct

@Serializable
internal class SsoServerListInfo(
    @JceId(1) @JvmField val sIP: String = "",
    @JceId(2) @JvmField val iPort: Int,
    @JceId(3) @JvmField val linkType: Byte,
    @JceId(4) @JvmField val proxy: Byte,
    @JceId(5) @JvmField val protocolType: Byte? = null,
    @JceId(6) @JvmField val iTimeOut: Int? = 10
) : JceStruct

@Serializable
internal class TimeStamp(
    @JceId(1) @JvmField val year: Int,
    @JceId(2) @JvmField val month: Byte,
    @JceId(3) @JvmField val day: Byte,
    @JceId(4) @JvmField val hour: Byte
) : JceStruct
