/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR", "unused")

package net.mamoe.mirai.internal.network

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.String
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.context.AccountSecrets
import net.mamoe.mirai.internal.network.context.SsoSession
import net.mamoe.mirai.internal.network.protocol.SyncingCacheList
import net.mamoe.mirai.internal.network.protocol.data.jce.FileStoragePushFSSvcList
import net.mamoe.mirai.internal.network.protocol.packet.Tlv
import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.internal.utils.NetworkType
import net.mamoe.mirai.internal.utils.crypto.ECDH
import net.mamoe.mirai.utils.*
import kotlin.random.Random


internal val DEFAULT_GUID = "%4;7t>;28<fc.5*6".toByteArray()

/**
 * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
 */
internal fun getRandomByteArray(length: Int): ByteArray = ByteArray(length) { Random.nextInt(0, 255).toByte() }

// [114.221.148.179:14000, 113.96.13.125:8080, 14.22.3.51:8080, 42.81.172.207:443, 114.221.144.89:80, 125.94.60.148:14000, 42.81.192.226:443, 114.221.148.233:8080, msfwifi.3g.qq.com:8080, 42.81.172.22:80]

internal val DefaultServerList: MutableSet<Pair<String, Int>> =
    "msfwifi.3g.qq.com:8080, 14.215.138.110:8080, 113.96.12.224:8080, 157.255.13.77:14000, 120.232.18.27:443, 183.3.235.162:14000, 163.177.89.195:443, 183.232.94.44:80, 203.205.255.224:8080, 203.205.255.221:8080"
        .split(", ")
        .map {
            val host = it.substringBefore(':')
            val port = it.substringAfter(':').toInt()
            host to port
        }.shuffled().toMutableSet()

/*
 APP ID:
 GetStViaSMSVerifyLogin = 16
 GetStWithoutPasswd = 16


 TICKET ID
 Pskey = 0x10_0000, from oicq/wlogin_sdk/request/WtloginHelper.java:2980
 Skey = 0x1000 from oicq/wlogin_sdk/request/WtloginHelper.java:2986

 DOMAINS
 Pskey: "openmobile.qq.com"
 */
/**
 * holds all the states related to network.
 */
internal open class QQAndroidClient(
    val account: BotAccount,
    override val ecdh: ECDH = ECDH(),
    val device: DeviceInfo,
    accountSecrets: AccountSecrets
) : AccountSecrets by accountSecrets, SsoSession {
    lateinit var _bot: QQAndroidBot
    val bot: QQAndroidBot get() = _bot

    /**
     * 真实 QQ 号. 使用邮箱等登录时则需获取这个 uin 进行后续一些操作.
     *
     * **注意**: 总是使用这个属性, 而不要使用 [BotAccount.id]. 将来它可能会变为 [String]
     */
    val uin: Long get() = _uin

    override var outgoingPacketSessionId: ByteArray = 0x02B05B8B.toByteArray()
    override var loginState = 0

    var onlineStatus: OnlineStatus = OnlineStatus.ONLINE

    var fileStoragePushFSSvcList: FileStoragePushFSSvcList? = null

    @Volatile
    private var _ssoSequenceId: Int = Random.nextInt(100000)

    @Synchronized
    @MiraiInternalApi("Do not use directly. Get from the lambda param of buildSsoPacket")
    internal fun nextSsoSequenceId(): Int {
        _ssoSequenceId += 2
        val new = _ssoSequenceId
        if (new > 100000) {
            _ssoSequenceId = Random.nextInt(100000) + 60000
        }
        return new
    }


    val apkVersionName: ByteArray get() = protocol.ver.toByteArray() //"8.4.18".toByteArray()
    val buildVer: String get() = "8.4.18.4810" // 8.2.0.1296 // 8.4.8.4810 // 8.2.7.4410


    private val sequenceId: AtomicInt = atomic(getRandomUnsignedInt())
    internal fun atomicNextMessageSequenceId(): Int = sequenceId.incrementAndGet()
    internal fun nextRequestPacketRequestId(): Int = sequenceId.incrementAndGet()

    @Volatile
    private var highwayDataTransSequenceId: Int = Random.nextInt(100000)

    @Synchronized
    internal fun nextHighwayDataTransSequenceId(): Int {
        highwayDataTransSequenceId += 1
        val new = highwayDataTransSequenceId
        if (new > 1000000) {
            highwayDataTransSequenceId = Random.nextInt(1060000)
        }
        return new
    }

    internal var strangerSeq: Int = 0

    // TODO: 2021/4/14 investigate whether they can be minimized
    private val friendSeq: AtomicInt = atomic(getRandomUnsignedInt())
    internal fun getFriendSeq(): Int = friendSeq.value

    internal fun nextFriendSeq(): Int = friendSeq.incrementAndGet()

    internal fun setFriendSeq(compare: Int, id: Int): Boolean = friendSeq.compareAndSet(compare, id % 65535)

    internal val groupConfig: GroupConfig = GroupConfig()

    internal class GroupConfig {
        var robotConfigVersion: Int = 0
        var aioKeyWordVersion: Int = 0
        var robotUinRangeList: List<LongRange> = emptyList()

        fun isOfficialRobot(uin: Long): Boolean {
            return robotUinRangeList.any { range -> range.contains(uin) }
        }
    }

    class MessageSvcSyncData {
        val firstNotify: AtomicBoolean = atomic(true)
        var latestMsgNewGroupTime: Long = currentTimeSeconds()
        var latestMsgNewFriendTime: Long = currentTimeSeconds()

        @Volatile
        var syncCookie: ByteArray? = null
        var pubAccountCookie = EMPTY_BYTE_ARRAY
        var msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY


        internal data class PbGetMessageSyncId(
            val uid: Long,
            val sequence: Int,
            val time: Int
        )

        val pbGetMessageCacheList = SyncingCacheList<PbGetMessageSyncId>()

        internal data class SystemMsgNewSyncId(
            val sequence: Long,
            val time: Long
        )

        val systemMsgNewGroupCacheList = SyncingCacheList<SystemMsgNewSyncId>(10)
        val systemMsgNewFriendCacheList = SyncingCacheList<SystemMsgNewSyncId>(10)


        internal data class PbPushTransMsgSyncId(
            val uid: Long,
            val sequence: Int,
            val time: Int
        )

        val pbPushTransMsgCacheList = SyncingCacheList<PbPushTransMsgSyncId>(10)

        internal data class OnlinePushReqPushSyncId(
            val uid: Long,
            val sequence: Short,
            val time: Long
        )

        val onlinePushReqPushCacheList = SyncingCacheList<OnlinePushReqPushSyncId>(50)

        internal data class PendingGroupMessageReceiptSyncId(
            val messageRandom: Int,
        )

        val pendingGroupMessageReceiptCacheList = SyncingCacheList<PendingGroupMessageReceiptSyncId>(50)
    }


    val syncingController = MessageSvcSyncData()

    var t150: Tlv? = null
    var rollbackSig: ByteArray? = null
    var ipFromT149: ByteArray? = null

    /**
     * 客户端与服务器时间差
     */
    var timeDifference: Long = 0

    @Suppress("PropertyName")
    internal var _uin: Long = account.id

    var t530: ByteArray? = null
    var t528: ByteArray? = null

    /**
     * t186
     */
    var pwdFlag: Boolean = false

    lateinit var wFastLoginInfo: WFastLoginInfo
    var reserveUinInfo: ReserveUinInfo? = null
    var t402: ByteArray? = null
    lateinit var t104: ByteArray
}

internal val QQAndroidClient.apkId: ByteArray get() = "com.tencent.mobileqq".toByteArray()
internal val QQAndroidClient.ssoVersion: Int get() = 15
internal val QQAndroidClient.networkType: NetworkType get() = NetworkType.WIFI
internal val QQAndroidClient.appClientVersion: Int get() = 0
internal val QQAndroidClient.mainSigMap: Int get() = protocol.mainSigMap
internal val QQAndroidClient.miscBitMap: Int get() = protocol.miscBitMap // 184024956 // 也可能是 150470524 ?
internal val QQAndroidClient.clientVersion: String get() = "android ${protocol.ver}" // android 8.5.0
internal val QQAndroidClient.protocol get() = MiraiProtocolInternal[bot.configuration.protocol]
internal val QQAndroidClient.sdkVersion: String get() = protocol.sdkVer
internal val QQAndroidClient.buildTime: Long get() = protocol.buildTime
internal val QQAndroidClient.subAppId: Long get() = protocol.id
internal val QQAndroidClient.apkSignatureMd5: ByteArray get() = protocol.sign.hexToBytes() // "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D".hexToBytes()
internal val QQAndroidClient.subSigMap: Int get() = protocol.subSigMap // 0x10400 //=66,560
