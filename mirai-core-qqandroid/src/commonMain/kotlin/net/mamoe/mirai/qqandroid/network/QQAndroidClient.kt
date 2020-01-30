package net.mamoe.mirai.qqandroid.network

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.RawAccountIdUse
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketLogger
import net.mamoe.mirai.qqandroid.network.protocol.packet.Tlv
import net.mamoe.mirai.qqandroid.utils.*
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.cryptor.ECDH
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.cryptor.decryptBy
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.unsafeWeakRef

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
@UseExperimental(MiraiExperimentalAPI::class, MiraiInternalAPI::class)
@PublishedApi
internal open class QQAndroidClient(
    context: Context,
    @MiraiInternalAPI("Be careful. Do not use the id in BotAccount. use client.uin instead")
    val account: BotAccount,


    val ecdh: ECDH = ECDH(),
    val device: DeviceInfo = SystemDeviceInfo(context),
    bot: QQAndroidBot
) {
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
                return mapper(data.decryptBy(value, size).also { PacketLogger.verbose("成功使用 $key 解密") })
            }
        }
        return null
    }

    override fun toString(): String { // net.mamoe.mirai.utils.cryptor.ProtoKt.contentToString
        return "QQAndroidClient(account=$account, ecdh=$ecdh, device=$device, tgtgtKey=${tgtgtKey.contentToString()}, randomKey=${randomKey.contentToString()}, miscBitMap=$miscBitMap, mainSigMap=$mainSigMap, subSigMap=$subSigMap, openAppId=$openAppId, apkVersionName=${apkVersionName.contentToString()}, loginState=$loginState, appClientVersion=$appClientVersion, networkType=$networkType, apkSignatureMd5=${apkSignatureMd5.contentToString()}, protocolVersion=$protocolVersion, apkId=${apkId.contentToString()}, t150=${t150?.contentToString()}, rollbackSig=${rollbackSig?.contentToString()}, ipFromT149=${ipFromT149?.contentToString()}, timeDifference=$timeDifference, uin=$uin, t530=${t530?.contentToString()}, t528=${t528?.contentToString()}, ksid='$ksid', pwdFlag=$pwdFlag, loginExtraData=$loginExtraData, wFastLoginInfo=$wFastLoginInfo, reserveUinInfo=$reserveUinInfo, wLoginSigInfo=$wLoginSigInfo, tlv113=${tlv113?.contentToString()}, qrPushSig=${qrPushSig.contentToString()}, mainDisplayName='$mainDisplayName')"
    }

    var onlineStatus: OnlineStatus = OnlineStatus.ONLINE

    val context: Context by context.unsafeWeakRef()
    val bot: QQAndroidBot by bot.unsafeWeakRef()

    var tgtgtKey: ByteArray = generateTgtgtKey(device.guid)
    val randomKey: ByteArray = getRandomByteArray(16)

    var miscBitMap: Int = 184024956 // 也可能是 150470524 ?
    var mainSigMap: Int = 16724722
    var subSigMap: Int = 0x10400 //=66,560

    private val _ssoSequenceId: AtomicInt = atomic(85600)

    @MiraiInternalAPI("Do not use directly. Get from the lambda param of buildSsoPacket")
    internal fun nextSsoSequenceId() = _ssoSequenceId.addAndGet(2)

    var openAppId: Long = 715019303L

    val apkVersionName: ByteArray = "8.2.0".toByteArray()

    private val messageSequenceId: AtomicInt = atomic(0)
    internal fun atomicNextMessageSequenceId(): Int = messageSequenceId.getAndIncrement()

    val appClientVersion: Int = 0

    var networkType: NetworkType = NetworkType.WIFI

    val apkSignatureMd5: ByteArray = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D".hexToBytes()

    /**
     * 协议版本?, 8.2.0 的为 8001
     */
    val protocolVersion: Short = 8001

    class C2cMessageSyncData {
        var syncCookie: ByteArray? = null
        var pubAccountCookie = EMPTY_BYTE_ARRAY
        var syncFlag: Int = 0
        var msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY
    }

    val c2cMessageSync = C2cMessageSyncData()

    /*
     * 以下登录使用
     */
    @Suppress("SpellCheckingInspection")
    @PublishedApi
    internal val apkId: ByteArray = "com.tencent.mobileqq".toByteArray()

    var outgoingPacketUnknownValue: ByteArray = 0x02B05B8B.toByteArray()
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

    @UseExperimental(RawAccountIdUse::class)
    @Suppress("PropertyName")
    internal var _uin: Long = bot.account.id

    var t530: ByteArray? = null
    var t528: ByteArray? = null
    /**
     * t108 时更新
     */
    var ksid: ByteArray = "|454001228437590|A8.2.0.27f6ea96".toByteArray()
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

class ReserveUinInfo(
    val imgType: ByteArray,
    val imgFormat: ByteArray,
    val imgUrl: ByteArray
) {
    override fun toString(): String {
        return "ReserveUinInfo(imgType=${imgType.contentToString()}, imgFormat=${imgFormat.contentToString()}, imgUrl=${imgUrl.contentToString()})"
    }
}

class WFastLoginInfo(
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

class WLoginSimpleInfo(
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
        return "WLoginSimpleInfo(uin=$uin, face=$face, age=$age, gender=$gender, nick='$nick', imgType=${imgType.contentToString()}, imgFormat=${imgFormat.contentToString()}, imgUrl=${imgUrl.contentToString()}, mainDisplayName=${mainDisplayName.contentToString()})"
    }
}

class LoginExtraData(
    val uin: Long,
    val ip: ByteArray,
    val time: Int,
    val version: Int
) {
    override fun toString(): String {
        return "LoginExtraData(uin=$uin, ip=${ip.contentToString()}, time=$time, version=$version)"
    }
}

class WLoginSigInfo(
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
        return "WLoginSigInfo(uin=$uin, encryptA1=${encryptA1.contentToString()}, noPicSig=${noPicSig.contentToString()}, G=${G.contentToString()}, dpwd=${dpwd.contentToString()}, randSeed=${randSeed.contentToString()}, simpleInfo=$simpleInfo, appPri=$appPri, a2ExpiryTime=$a2ExpiryTime, loginBitmap=$loginBitmap, tgt=${tgt.contentToString()}, a2CreationTime=$a2CreationTime, tgtKey=${tgtKey.contentToString()}, userStSig=$userStSig, userStKey=${userStKey.contentToString()}, userStWebSig=$userStWebSig, userA5=$userA5, userA8=$userA8, lsKey=$lsKey, sKey=$sKey, userSig64=$userSig64, openId=${openId.contentToString()}, openKey=$openKey, vKey=$vKey, accessToken=$accessToken, d2=$d2, d2Key=${d2Key.contentToString()}, sid=$sid, aqSig=$aqSig, psKey=${psKeyMap.contentToString()}, superKey=${superKey.contentToString()}, payToken=${payToken.contentToString()}, pf=${pf.contentToString()}, pfKey=${pfKey.contentToString()}, da2=${da2.contentToString()}, wtSessionTicket=$wtSessionTicket, wtSessionTicketKey=${wtSessionTicketKey.contentToString()}, deviceToken=${deviceToken.contentToString()})"
    }
}

class UserStSig(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
class LSKey(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
class UserStWebSig(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
class UserA8(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
class UserA5(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
class SKey(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
class UserSig64(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
class OpenKey(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
class VKey(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
class AccessToken(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
class D2(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
class Sid(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)
class AqSig(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)

class Pt4Token(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)

typealias PSKeyMap = MutableMap<String, PSKey>
typealias Pt4TokenMap = MutableMap<String, Pt4Token>

internal fun parsePSKeyMapAndPt4TokenMap(data: ByteArray, creationTime: Long, expireTime: Long, outPSKeyMap: PSKeyMap, outPt4TokenMap: Pt4TokenMap) =
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

class PSKey(data: ByteArray, creationTime: Long, expireTime: Long) : KeyWithExpiry(data, creationTime, expireTime)

class WtSessionTicket(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)

open class KeyWithExpiry(
    data: ByteArray,
    creationTime: Long,
    val expireTime: Long
) : KeyWithCreationTime(data, creationTime)

open class KeyWithCreationTime(
    val data: ByteArray,
    val creationTime: Long
)