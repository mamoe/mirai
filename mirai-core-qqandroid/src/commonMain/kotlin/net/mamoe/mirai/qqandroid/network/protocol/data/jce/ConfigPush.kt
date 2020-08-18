package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class BigDataChannel(
    @TarsId(0) @JvmField val vBigdataIplists: List<BigDataIpList>,
    @TarsId(1) @JvmField val sBigdataSigSession: ByteArray? = null,
    @TarsId(2) @JvmField val sBigdataKeySession: ByteArray? = null,
    @TarsId(3) @JvmField val uSigUin: Long? = null,
    @TarsId(4) @JvmField val iConnectFlag: Int? = 1,
    @TarsId(5) @JvmField val vBigdataPbBuf: ByteArray? = null
) : JceStruct

@Serializable
internal class BigDataIpInfo(
    @TarsId(0) @JvmField val uType: Long,
    @TarsId(1) @JvmField val sIp: String = "",
    @TarsId(2) @JvmField val uPort: Long
) : JceStruct

@Serializable
internal class BigDataIpList(
    @TarsId(0) @JvmField val uServiceType: Long,
    @TarsId(1) @JvmField val vIplist: List<BigDataIpInfo>,
    @TarsId(2) @JvmField val netSegConfs: List<NetSegConf>? = null,
    @TarsId(3) @JvmField val ufragmentSize: Long? = null
) : JceStruct

@Serializable
internal class ClientLogConfig(
    @TarsId(1) @JvmField val type: Int,
    @TarsId(2) @JvmField val timeStart: TimeStamp? = null,
    @TarsId(3) @JvmField val timeFinish: TimeStamp? = null,
    @TarsId(4) @JvmField val loglevel: Byte? = null,
    @TarsId(5) @JvmField val cookie: Int? = null,
    @TarsId(6) @JvmField val lseq: Long? = null
) : JceStruct

@Serializable
internal class DomainIpChannel(
    @TarsId(0) @JvmField val vDomainIplists: List<DomainIpList>
) : JceStruct

@Serializable
internal class DomainIpInfo(
    @TarsId(1) @JvmField val uIp: Int,
    @TarsId(2) @JvmField val uPort: Int
) : JceStruct

@Serializable
internal class DomainIpList(
    @TarsId(0) @JvmField val uDomainType: Int,
    @TarsId(1) @JvmField val vIplist: List<DomainIpInfo>,
    @TarsId(2) @JvmField val unknown: ByteArray? = null,
    @TarsId(4) @JvmField val int: Int? = null// added
) : JceStruct

@Serializable
internal class _340(
    @TarsId(1) @JvmField val field1315: List<_339>,
    @TarsId(3) @JvmField val field1316: List<_339>,
    @TarsId(4) @JvmField val field1317: Int,
    @TarsId(5) @JvmField val field1318: Byte? = 0,
    @TarsId(6) @JvmField val field1319: Byte? = 0,
    @TarsId(7) @JvmField val field1320: Int? = 1,
    @TarsId(8) @JvmField val field1321: List<_339>? = null,
    @TarsId(9) @JvmField val field1322: List<_339>? = null,
    @TarsId(10) @JvmField val field1323: List<_339>? = null,
    @TarsId(11) @JvmField val field1324: List<_339>? = null,
    @TarsId(12) @JvmField val field1325: List<_339>? = null,
    @TarsId(13) @JvmField val field1326: List<_339>? = null,
    @TarsId(14) @JvmField val netType: Byte? = 0,
    @TarsId(15) @JvmField val heThreshold: Int? = 0,
    @TarsId(16) @JvmField val policyId: String? = ""
) : JceStruct

@Serializable
internal class _339(
    @TarsId(1) @JvmField val field1298: String = "",
    @TarsId(2) @JvmField val field1299: Int = 0,
    @TarsId(3) @JvmField val field1300: Byte = 0,
    @TarsId(4) @JvmField val field1301: Byte = 0,
    @TarsId(5) @JvmField val field1302: Byte? = 0,
    @TarsId(6) @JvmField val field1303: Int? = 8,
    @TarsId(7) @JvmField val field1304: Byte? = 0,
    @TarsId(8) @JvmField val field1305: String = "",
    @TarsId(9) @JvmField val field1306: String = ""
) : JceStruct

@Serializable
internal class FileStoragePushFSSvcListFuckKotlin(
    @TarsId(0) @JvmField val vUpLoadList: List<FileStorageServerListInfo>? = listOf(),
    @TarsId(1) @JvmField val vPicDownLoadList: List<FileStorageServerListInfo>? = listOf(),
    @TarsId(2) @JvmField val vGPicDownLoadList: List<FileStorageServerListInfo>? = null,
    @TarsId(3) @JvmField val vQzoneProxyServiceList: List<FileStorageServerListInfo>? = null,
    @TarsId(4) @JvmField val vUrlEncodeServiceList: List<FileStorageServerListInfo>? = null,
    @TarsId(5) @JvmField val bigDataChannel: BigDataChannel? = null,
    @TarsId(6) @JvmField val vVipEmotionList: List<FileStorageServerListInfo>? = null,
    @TarsId(7) @JvmField val vC2CPicDownList: List<FileStorageServerListInfo>? = null,
    @TarsId(8) @JvmField val fmtIPInfo: FmtIPInfo? = null,
    @TarsId(9) @JvmField val domainIpChannel: DomainIpChannel? = null,
    @TarsId(10) @JvmField val pttlist: ByteArray? = null
) : JceStruct

@Serializable
internal class FileStorageServerListInfo(
    @TarsId(1) @JvmField val sIP: String = "",
    @TarsId(2) @JvmField val iPort: Int
) : JceStruct

@Serializable
internal class FmtIPInfo(
    @TarsId(0) @JvmField val sGateIp: String = "",
    @TarsId(1) @JvmField val iGateIpOper: Long
) : JceStruct

@Serializable
internal class NetSegConf(
    @TarsId(0) @JvmField val uint32NetType: Long? = null,
    @TarsId(1) @JvmField val uint32Segsize: Long? = null,
    @TarsId(2) @JvmField val uint32Segnum: Long? = null,
    @TarsId(3) @JvmField val uint32Curconnnum: Long? = null
) : JceStruct

@Suppress("ArrayInDataClass")
@Serializable
internal class PushReq(
    @TarsId(1) @JvmField val type: Int,
    @TarsId(2) @JvmField val jcebuf: ByteArray,
    @TarsId(3) @JvmField val seq: Long
) : JceStruct, Packet

@Serializable
internal class PushResp(
    @TarsId(1) @JvmField val type: Int,
    @TarsId(2) @JvmField val seq: Long,
    @TarsId(3) @JvmField val jcebuf: ByteArray? = null
) : JceStruct

@Serializable
internal class SsoServerList(
    @TarsId(1) @JvmField val v2G3GList: List<SsoServerListInfo>,
    @TarsId(3) @JvmField val vWifiList: List<SsoServerListInfo>,
    @TarsId(4) @JvmField val iReconnect: Int,
    @TarsId(5) @JvmField val testSpeed: Byte? = null,
    @TarsId(6) @JvmField val useNewList: Byte? = null,
    @TarsId(7) @JvmField val iMultiConn: Int? = 1,
    @TarsId(8) @JvmField val vHttp2g3glist: List<SsoServerListInfo>? = null,
    @TarsId(9) @JvmField val vHttpWifilist: List<SsoServerListInfo>? = null
) : JceStruct

@Serializable
internal class SsoServerListInfo(
    @TarsId(1) @JvmField val sIP: String = "",
    @TarsId(2) @JvmField val iPort: Int,
    @TarsId(3) @JvmField val linkType: Byte,
    @TarsId(4) @JvmField val proxy: Byte,
    @TarsId(5) @JvmField val protocolType: Byte? = null,
    @TarsId(6) @JvmField val iTimeOut: Int? = 10
) : JceStruct

@Serializable
internal class TimeStamp(
    @TarsId(1) @JvmField val year: Int,
    @TarsId(2) @JvmField val month: Byte,
    @TarsId(3) @JvmField val day: Byte,
    @TarsId(4) @JvmField val hour: Byte
) : JceStruct
