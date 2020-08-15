/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR")

package net.mamoe.mirai.qqandroid.network

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.*
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.NoServerAvailableException
import net.mamoe.mirai.qqandroid.BotAccount
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.FileStoragePushFSSvcListFuckKotlin
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketLogger
import net.mamoe.mirai.qqandroid.network.protocol.packet.Tlv
import net.mamoe.mirai.qqandroid.utils.*
import net.mamoe.mirai.qqandroid.utils.cryptor.ECDH
import net.mamoe.mirai.qqandroid.utils.cryptor.TEA
import net.mamoe.mirai.utils.*
import kotlin.random.Random

internal val DeviceInfo.guid: ByteArray get() = generateGuid(androidId, macAddress)

/**
 * Defaults "%4;7t>;28<fc.5*6".toByteArray()
 */
@Suppress("RemoveRedundantQualifierName") // bug
private fun generateGuid(androidId: ByteArray, macAddress: ByteArray): ByteArray =
    net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils.md5(androidId + macAddress)

/**
 * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
 */
internal fun getRandomByteArray(length: Int): ByteArray = ByteArray(length) { Random.nextInt(0, 255).toByte() }

internal object DefaultServerList : Set<Pair<String, Int>> by setOf(
    "42.81.169.46" to 8080,
    "42.81.172.81" to 80,
    "114.221.148.59" to 14000,
    "42.81.172.147" to 443,
    "125.94.60.146" to 80,
    "114.221.144.215" to 80,
    "msfwifi.3g.qq.com" to 8080,
    "42.81.172.22" to 80
)

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
@PublishedApi
internal open class QQAndroidClient(
    context: Context,
    val account: BotAccount,
    val ecdh: ECDH = ECDH(),
    val device: DeviceInfo = SystemDeviceInfo(context),
    bot: QQAndroidBot
) {
    @Suppress("INVISIBLE_MEMBER")
    val subAppId: Long
        get() = bot.configuration.protocol.id

    internal val serverList: MutableList<Pair<String, Int>> = DefaultServerList.toMutableList()

    val keys: Map<String, ByteArray> by lazy {
        mapOf(
            "16 zero" to ByteArray(16),
            "D2 key" to wLoginSigInfo.d2Key,
            "wtSessionTicketKey" to wLoginSigInfo.wtSessionTicketKey,
            "userStKey" to wLoginSigInfo.userStKey,
            "tgtgtKey" to tgtgtKey,
            "tgtKey" to wLoginSigInfo.tgtKey,
            "deviceToken" to wLoginSigInfo.deviceToken,
            "shareKeyCalculatedByConstPubKey" to ecdh.keyPair.initialShareKey
            //"t108" to wLoginSigInfo.t1,
            //"t10c" to t10c,
            //"t163" to t163
        )
    }

    internal inline fun <R> tryDecryptOrNull(data: ByteArray, size: Int = data.size, mapper: (ByteArray) -> R): R? {
        keys.forEach { (key, value) ->
            kotlin.runCatching {
                return mapper(TEA.decrypt(data, value, size).also { PacketLogger.verbose { "成功使用 $key 解密" } })
            }
        }
        return null
    }

    override fun toString(): String { // extremely slow
        return "QQAndroidClient(account=$account, ecdh=$ecdh, device=$device, tgtgtKey=${tgtgtKey.toUHexString()}, randomKey=${randomKey.toUHexString()}, miscBitMap=$miscBitMap, mainSigMap=$mainSigMap, subSigMap=$subSigMap, openAppId=$openAppId, apkVersionName=${apkVersionName.toUHexString()}, loginState=$loginState, appClientVersion=$appClientVersion, networkType=$networkType, apkSignatureMd5=${apkSignatureMd5.toUHexString()}, protocolVersion=$protocolVersion, apkId=${apkId.toUHexString()}, t150=${t150?.value?.toUHexString()}, rollbackSig=${rollbackSig?.toUHexString()}, ipFromT149=${ipFromT149?.toUHexString()}, timeDifference=$timeDifference, uin=$uin, t530=${t530?.toUHexString()}, t528=${t528?.toUHexString()}, ksid='$ksid', pwdFlag=$pwdFlag, loginExtraData=$loginExtraData, wFastLoginInfo=$wFastLoginInfo, reserveUinInfo=$reserveUinInfo, wLoginSigInfo=$wLoginSigInfo, tlv113=${tlv113?.toUHexString()}, qrPushSig=${qrPushSig.toUHexString()}, mainDisplayName='$mainDisplayName')"
    }

    var onlineStatus: OnlineStatus = OnlineStatus.ONLINE

    val context: Context by context.unsafeWeakRef()
    val bot: QQAndroidBot by bot.unsafeWeakRef()

    var tgtgtKey: ByteArray = generateTgtgtKey(device.guid)
    val randomKey: ByteArray = getRandomByteArray(16)

    var miscBitMap: Int = 184024956 // 也可能是 150470524 ?
    private var mainSigMap: Int = 16724722
    var subSigMap: Int = 0x10400 //=66,560

    private val _ssoSequenceId: AtomicInt = atomic(85600)

    lateinit var fileStoragePushFSSvcList: FileStoragePushFSSvcListFuckKotlin

    internal suspend inline fun useNextServers(crossinline block: suspend (host: String, port: Int) -> Unit) {
        if (bot.client.serverList.isEmpty()) {
            throw NoServerAvailableException(null)
        }
        retryCatching(bot.client.serverList.size, except = LoginFailedException::class) {
            val pair = bot.client.serverList.random()
            kotlin.runCatching {
                block(pair.first, pair.second)
                return@retryCatching
            }.getOrElse {
                bot.client.serverList.remove(pair)
                if (it !is LoginFailedException) {
                    // 不要重复打印.
                    bot.logger.warning(it)
                }
                throw it
            }
        }.getOrElse {
            if (it is LoginFailedException) {
                throw it
            }
            bot.client.serverList.addAll(DefaultServerList)
            throw NoServerAvailableException(it)
        }
    }

    @MiraiInternalAPI("Do not use directly. Get from the lambda param of buildSsoPacket")
    internal fun nextSsoSequenceId() = _ssoSequenceId.addAndGet(2)

    var openAppId: Long = 715019303L

    val apkVersionName: ByteArray get() = "8.2.7".toByteArray()
    val buildVer: String get() = "8.2.7.4410" // 8.2.0.1296

    private val messageSequenceId: AtomicInt = atomic(22911)
    internal fun atomicNextMessageSequenceId(): Int = messageSequenceId.getAndAdd(2)


    private val friendSeq: AtomicInt = atomic(22911)
    internal fun getFriendSeq(): Int {
        return friendSeq.value
    }

    internal fun nextFriendSeq(): Int {
        return friendSeq.incrementAndGet()
    }

    internal fun setFriendSeq(compare: Int, id: Int): Boolean {
        return friendSeq.compareAndSet(compare, id % 65535)
    }

    private val requestPacketRequestId: AtomicInt = atomic(1921334513)
    internal fun nextRequestPacketRequestId(): Int = requestPacketRequestId.getAndAdd(2)

    private val highwayDataTransSequenceIdForGroup: AtomicInt = atomic(87017)
    internal fun nextHighwayDataTransSequenceIdForGroup(): Int = highwayDataTransSequenceIdForGroup.getAndAdd(2)

    private val highwayDataTransSequenceIdForFriend: AtomicInt = atomic(43973)
    internal fun nextHighwayDataTransSequenceIdForFriend(): Int = highwayDataTransSequenceIdForFriend.getAndAdd(2)

    private val highwayDataTransSequenceIdForApplyUp: AtomicInt = atomic(77918)
    internal fun nextHighwayDataTransSequenceIdForApplyUp(): Int = highwayDataTransSequenceIdForApplyUp.getAndAdd(2)

    internal val onlinePushCacheList: AtomicResizeCacheList<Short> = AtomicResizeCacheList(20.secondsToMillis)
    internal val pbPushTransMsgCacheList: AtomicResizeCacheList<Int> = AtomicResizeCacheList(20.secondsToMillis)

    val appClientVersion: Int = 0

    var networkType: NetworkType = NetworkType.WIFI

    val apkSignatureMd5: ByteArray = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D".hexToBytes()

    /**
     * 协议版本?, 8.2.7 的为 8001
     */
    val protocolVersion: Short = 8001

    class C2cMessageSyncData {
        var syncCookie: ByteArray? = null
        var pubAccountCookie = EMPTY_BYTE_ARRAY
        var msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY
    }

    val c2cMessageSync = C2cMessageSyncData()

    /*
     * 以下登录使用
     */
    @Suppress("SpellCheckingInspection")
    @PublishedApi
    internal val apkId: ByteArray = "com.tencent.mobileqq".toByteArray()

    var outgoingPacketSessionId: ByteArray = 0x02B05B8B.toByteArray()
    var loginState = 0

    var t150: Tlv? = null
    var rollbackSig: ByteArray? = null
    var ipFromT149: ByteArray? = null

    /**
     * 客户端与服务器时间差
     */
    var timeDifference: Long = 0

    /**
     * 真实 QQ 号. 使用邮箱等登录时则需获取这个 uin 进行后续一些操作.
     *
     * **注意**: 总是使用这个属性, 而不要使用 [BotAccount.id]. 将来它可能会变为 [String]
     */
    val uin: Long get() = _uin

    @Suppress("PropertyName")
    internal var _uin: Long = account.id

    var t530: ByteArray? = null
    var t528: ByteArray? = null

    /**
     * t108 时更新
     */
    var ksid: ByteArray = "|454001228437590|A8.2.7.27f6ea96".toByteArray()

    /**
     * t186
     */
    var pwdFlag: Boolean = false

    /**
     * t537
     */
    var loginExtraData: LoginExtraData? = null
    lateinit var wFastLoginInfo: WFastLoginInfo
    var reserveUinInfo: ReserveUinInfo? = null
    lateinit var wLoginSigInfo: WLoginSigInfo
    var tlv113: ByteArray? = null
    lateinit var qrPushSig: ByteArray

    lateinit var mainDisplayName: ByteArray

    var transportSequenceId = 1

    lateinit var t104: ByteArray
}

@Suppress("RemoveRedundantQualifierName") // bug
internal fun generateTgtgtKey(guid: ByteArray): ByteArray =
    net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils.md5(getRandomByteArray(16) + guid)


internal class ReserveUinInfo(
    val imgType: ByteArray,
    val imgFormat: ByteArray,
    val imgUrl: ByteArray
) {
    override fun toString(): String {
        return "ReserveUinInfo(imgType=${imgType.toUHexString()}, imgFormat=${imgFormat.toUHexString()}, imgUrl=${imgUrl.toUHexString()})"
    }
}

internal class WFastLoginInfo(
    val outA1: ByteReadPacket,
    var adUrl: String = "",
    var iconUrl: String = "",
    var profileUrl: String = "",
    var userJson: String = ""
) {
    override fun toString(): String {
        return "WFastLoginInfo(outA1=$outA1, adUrl='$adUrl', iconUrl='$iconUrl', profileUrl='$profileUrl', userJson='$userJson')"
    }
}

internal class WLoginSimpleInfo(
    val uin: Long, // uin
    val face: Int, // ubyte actually
    val age: Int, // ubyte
    val gender: Int, // ubyte
    val nick: String, // ubyte lv string
    val imgType: ByteArray,
    val imgFormat: ByteArray,
    val imgUrl: ByteArray,
    val mainDisplayName: ByteArray
) {
    override fun toString(): String {
        return "WLoginSimpleInfo(uin=$uin, face=$face, age=$age, gender=$gender, nick='$nick', imgType=${imgType.toUHexString()}, imgFormat=${imgFormat.toUHexString()}, imgUrl=${imgUrl.toUHexString()}, mainDisplayName=${mainDisplayName.toUHexString()})"
    }
}

internal class LoginExtraData(
    val uin: Long,
    val ip: ByteArray,
    val time: Int,
    val version: Int
) {
    override fun toString(): String {
        return "LoginExtraData(uin=$uin, ip=${ip.toUHexString()}, time=$time, version=$version)"
    }
}

internal class WLoginSigInfo(
    val uin: Long,
    val encryptA1: ByteArray?, // sigInfo[0]
    val noPicSig: ByteArray?, // sigInfo[1]
    val G: ByteArray, // sigInfo[2]
    val dpwd: ByteArray,
    val randSeed: ByteArray,

    val simpleInfo: WLoginSimpleInfo,

    val appPri: Long,
    val a2ExpiryTime: Long,
    val loginBitmap: Long,
    val tgt: ByteArray,
    val a2CreationTime: Long,
    val tgtKey: ByteArray,
    val userStSig: UserStSig,
    /**
     * TransEmpPacket 加密使用
     */
    val userStKey: ByteArray,
    val userStWebSig: UserStWebSig,
    val userA5: UserA5,
    val userA8: UserA8,
    val lsKey: LSKey,
    val sKey: SKey,
    val userSig64: UserSig64,
    val openId: ByteArray,
    val openKey: OpenKey,
    val vKey: VKey,
    val accessToken: AccessToken,
    val d2: D2,
    val d2Key: ByteArray,
    val sid: Sid,
    val aqSig: AqSig,
    val psKeyMap: PSKeyMap,
    val pt4TokenMap: Pt4TokenMap,
    val superKey: ByteArray,
    val payToken: ByteArray,
    val pf: ByteArray,
    val pfKey: ByteArray,
    val da2: ByteArray,
    //  val pt4Token: ByteArray,
    val wtSessionTicket: WtSessionTicket,
    val wtSessionTicketKey: ByteArray,
    val deviceToken: ByteArray
) {
    override fun toString(): String {
        return "WLoginSigInfo(uin=$uin, encryptA1=${encryptA1?.toUHexString()}, noPicSig=${noPicSig?.toUHexString()}, G=${G.toUHexString()}, dpwd=${dpwd.toUHexString()}, randSeed=${randSeed.toUHexString()}, simpleInfo=$simpleInfo, appPri=$appPri, a2ExpiryTime=$a2ExpiryTime, loginBitmap=$loginBitmap, tgt=${tgt.toUHexString()}, a2CreationTime=$a2CreationTime, tgtKey=${tgtKey.toUHexString()}, userStSig=$userStSig, userStKey=${userStKey.toUHexString()}, userStWebSig=$userStWebSig, userA5=$userA5, userA8=$userA8, lsKey=$lsKey, sKey=$sKey, userSig64=$userSig64, openId=${openId.toUHexString()}, openKey=$openKey, vKey=$vKey, accessToken=$accessToken, d2=$d2, d2Key=${d2Key.toUHexString()}, sid=$sid, aqSig=$aqSig, psKey=$psKeyMap, superKey=${superKey.toUHexString()}, payToken=${payToken.toUHexString()}, pf=${pf.toUHexString()}, pfKey=${pfKey.toUHexString()}, da2=${da2.toUHexString()}, wtSessionTicket=$wtSessionTicket, wtSessionTicketKey=${wtSessionTicketKey.toUHexString()}, deviceToken=${deviceToken.toUHexString()})"
    }
}

internal class UserStSig(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class LSKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserStWebSig(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserA8(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserA5(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class SKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class UserSig64(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class OpenKey(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class VKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class AccessToken(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
internal class D2(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
internal class Sid(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class AqSig(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)

internal class Pt4Token(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal typealias PSKeyMap = MutableMap<String, PSKey>
internal typealias Pt4TokenMap = MutableMap<String, Pt4Token>

internal inline fun Input.readUShortLVString(): String = kotlinx.io.core.String(this.readUShortLVByteArray())

internal inline fun Input.readUShortLVByteArray(): ByteArray = this.readBytes(this.readUShort().toInt())

internal fun parsePSKeyMapAndPt4TokenMap(
    data: ByteArray,
    creationTime: Long,
    expireTime: Long,
    outPSKeyMap: PSKeyMap,
    outPt4TokenMap: Pt4TokenMap
) =
    data.read {
        repeat(readShort().toInt()) {
            val domain = readUShortLVString()
            val psKey = readUShortLVByteArray()
            val pt4token = readUShortLVByteArray()

            when {
                psKey.isNotEmpty() -> outPSKeyMap[domain] = PSKey(psKey, creationTime, expireTime)
                pt4token.isNotEmpty() -> outPt4TokenMap[domain] = Pt4Token(pt4token, creationTime, expireTime)
            }
        }
    }

internal class PSKey(data: ByteArray, creationTime: Long, expireTime: Long) :
    KeyWithExpiry(data, creationTime, expireTime)

internal class WtSessionTicket(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)

internal open class KeyWithExpiry(
    data: ByteArray,
    creationTime: Long,
    val expireTime: Long
) : KeyWithCreationTime(data, creationTime) {
    override fun toString(): String {
        return "KeyWithExpiry(data=${data.toUHexString()}, creationTime=$creationTime)"
    }
}

internal open class KeyWithCreationTime(
    val data: ByteArray,
    val creationTime: Long
) {
    override fun toString(): String {
        return "KeyWithCreationTime(data=${data.toUHexString()}, creationTime=$creationTime)"
    }
}