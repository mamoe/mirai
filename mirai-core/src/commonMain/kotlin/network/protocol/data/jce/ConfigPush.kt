/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class BigDataChannel(
    @TarsId(0) @JvmField val vBigdataIplists: List<BigDataIpList>,
    @TarsId(1) @JvmField val sBigdataSigSession: ByteArray? = null,
    @TarsId(2) @JvmField val sBigdataKeySession: ByteArray? = null,
    @TarsId(3) @JvmField val uSigUin: Long? = null,
    @TarsId(4) @JvmField val iConnectFlag: Int? = 1,
    @TarsId(5) @JvmField val vBigdataPbBuf: ByteArray? = null,
) : JceStruct

@Serializable
internal class BigDataIpInfo(
    @TarsId(0) @JvmField val uType: Long,
    @TarsId(1) @JvmField val sIp: String = "",
    @TarsId(2) @JvmField val uPort: Long,
) : JceStruct

@Serializable
internal class BigDataIpList(
    @TarsId(0) @JvmField val uServiceType: Long,
    @TarsId(1) @JvmField val vIplist: List<BigDataIpInfo>,
    @TarsId(2) @JvmField val netSegConfs: List<NetSegConf>? = null,
    @TarsId(3) @JvmField val ufragmentSize: Long? = null,
) : JceStruct

@Serializable
internal class ClientLogConfig(
    @TarsId(1) @JvmField val type: Int,
    @TarsId(2) @JvmField val timeStart: TimeStamp? = null,
    @TarsId(3) @JvmField val timeFinish: TimeStamp? = null,
    @TarsId(4) @JvmField val loglevel: Byte? = null,
    @TarsId(5) @JvmField val cookie: Int? = null,
    @TarsId(6) @JvmField val lseq: Long? = null,
) : JceStruct

@Serializable
internal class DomainIpChannel(
    @TarsId(0) @JvmField val vDomainIplists: List<DomainIpList>,
) : JceStruct

@Serializable
internal class DomainIpInfo(
    @TarsId(1) @JvmField val uIp: Int,
    @TarsId(2) @JvmField val uPort: Int,
) : JceStruct

@Serializable
internal class DomainIpList(
    @TarsId(0) @JvmField val uDomainType: Int,
    @TarsId(1) @JvmField val vIplist: List<DomainIpInfo>,
    @TarsId(2) @JvmField val unknown: ByteArray? = null,
    @TarsId(4) @JvmField val int: Int? = null, // added
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
    @TarsId(16) @JvmField val policyId: String? = "",
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
    @TarsId(9) @JvmField val field1306: String = "",
) : JceStruct


/**
 * v8.5.5
 */
@Serializable
internal class FileStoragePushFSSvcList(
    @TarsId(0) @JvmField val vUpLoadList: List<FileStorageServerListInfo> = emptyList(),
    @TarsId(1) @JvmField val vPicDownLoadList: List<FileStorageServerListInfo> = emptyList(),
    @TarsId(2) @JvmField val vGPicDownLoadList: List<FileStorageServerListInfo> = emptyList(),
    @TarsId(3) @JvmField val vQzoneProxyServiceList: List<FileStorageServerListInfo> = emptyList(),
    @TarsId(4) @JvmField val vUrlEncodeServiceList: List<FileStorageServerListInfo> = emptyList(),
    @TarsId(5) @JvmField val bigDataChannel: BigDataChannel? = null,
    @TarsId(6) @JvmField val vVipEmotionList: List<FileStorageServerListInfo> = emptyList(),
    @TarsId(7) @JvmField val vC2CPicDownList: List<FileStorageServerListInfo> = emptyList(),
    @TarsId(8) @JvmField val fmtIPInfo: FmtIPInfo? = null,
    @TarsId(9) @JvmField val domainIpChannel: DomainIpChannel? = null,
    @TarsId(10) @JvmField val pttlist: ByteArray? = null,
) : JceStruct

@Serializable
internal class FileStorageServerListInfo(
    @TarsId(1) @JvmField val sIP: String = "",
    @TarsId(2) @JvmField val iPort: Int,
) : JceStruct

@Serializable
internal class FmtIPInfo(
    @TarsId(0) @JvmField val sGateIp: String = "",
    @TarsId(1) @JvmField val iGateIpOper: Long,
) : JceStruct

@Serializable
internal class NetSegConf(
    @TarsId(0) @JvmField val uint32NetType: Long? = null,
    @TarsId(1) @JvmField val uint32Segsize: Long? = null,
    @TarsId(2) @JvmField val uint32Segnum: Long? = null,
    @TarsId(3) @JvmField val uint32Curconnnum: Long? = null,
) : JceStruct

@Suppress("ArrayInDataClass")
@Serializable
internal class PushReq(
    @TarsId(1) @JvmField val type: Int,
    @TarsId(2) @JvmField val jcebuf: ByteArray,
    @TarsId(3) @JvmField val seq: Long,
) : JceStruct, Packet

@Serializable
internal data class ServerListPush(
    @TarsId(1) val mobileSSOServerList: List<ServerInfo>,
    @TarsId(3) val wifiSSOServerList: List<ServerInfo>,
    @TarsId(4) val reconnectNeeded: Int = 0,
    //@JvmField @TarsId(5)  val skipped:Byte? = 0,
    //@JvmField @TarsId(6)  val skipped:Byte? = 0,
    //@JvmField @TarsId(7)  val skipped:Int? = 1,
    @TarsId(8) val mobileHttpServerList: List<ServerInfo>,
    @TarsId(9) val wifiHttpServerList: List<ServerInfo>,
    @TarsId(10) val quicServerList: List<ServerInfo>,
    @TarsId(11) val ssoServerListIpv6: List<ServerInfo>,
    @TarsId(12) val httpServerListIpv6: List<ServerInfo>,
    @TarsId(13) val quicServerListIpv6: List<ServerInfo>,
    /**
     * wifi下&1==1则启用
     * 移动数据(mobile)下&2==2则启用
     */
    @TarsId(14) val ipv6ConfigVal: Byte? = 0,
    //@JvmField @TarsId(15) val netTestDelay:Int? = 0,
    @TarsId(16) val configDesc: String? = "",
) : JceStruct {

    @Serializable
    data class ServerInfo(
        @TarsId(1) val host: String,
        @TarsId(2) val port: Int,
        //@JvmField @TarsId(3) val skipped: Byte = 0,
        //@JvmField @TarsId(4) val skipped: Byte = 0,
        /**
         * 2,3->http
         * 0,1->socket
         */
        //@JvmField @TarsId(5) val protocolType: Byte? = 0,
        //@JvmField @TarsId(6) val skipped: Int? = 8,
        //@JvmField @TarsId(7) val skipped: Byte? = 0,
        @TarsId(8) val location: String = "",
        /**
         * cm->China mobile 中国移动
         * uni->China unicom 中国联通
         * others->其他
         */
        @TarsId(9) val ispName: String = "",
    ) : JceStruct {
        override fun toString(): String {
            return "$host:$port"
        }
    }
}

@Serializable
internal class PushResp(
    @TarsId(1) @JvmField val type: Int,
    @TarsId(2) @JvmField val seq: Long,
    @TarsId(3) @JvmField val jcebuf: ByteArray? = null,
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
    @TarsId(9) @JvmField val vHttpWifilist: List<SsoServerListInfo>? = null,
) : JceStruct

@Serializable
internal class SsoServerListInfo(
    @TarsId(1) @JvmField val sIP: String = "",
    @TarsId(2) @JvmField val iPort: Int,
    @TarsId(3) @JvmField val linkType: Byte,
    @TarsId(4) @JvmField val proxy: Byte,
    @TarsId(5) @JvmField val protocolType: Byte? = null,
    @TarsId(6) @JvmField val iTimeOut: Int? = 10,
) : JceStruct

@Serializable
internal class TimeStamp(
    @TarsId(1) @JvmField val year: Int,
    @TarsId(2) @JvmField val month: Byte,
    @TarsId(3) @JvmField val day: Byte,
    @TarsId(4) @JvmField val hour: Byte,
) : JceStruct
