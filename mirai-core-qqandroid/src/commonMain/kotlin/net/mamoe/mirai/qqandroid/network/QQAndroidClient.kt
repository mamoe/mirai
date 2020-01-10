package net.mamoe.mirai.qqandroid.network

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.utils.Context
import net.mamoe.mirai.qqandroid.utils.DeviceInfo
import net.mamoe.mirai.qqandroid.utils.NetworkType
import net.mamoe.mirai.qqandroid.utils.SystemDeviceInfo
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.cryptor.ECDH
import net.mamoe.mirai.utils.io.hexToBytes
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

@PublishedApi
internal open class QQAndroidClient(
    context: Context,
    @MiraiInternalAPI("Be careful. Do not use the id in BotAccount. use client.uin instead")
    val account: BotAccount,

    val ecdh: ECDH = ECDH(),
    val device: DeviceInfo = SystemDeviceInfo(context),
    bot: QQAndroidBot
) {
    val context by context.unsafeWeakRef()
    val bot: QQAndroidBot by bot.unsafeWeakRef()

    val tgtgtKey: ByteArray = ByteArray(16) // generateTgtgtKey(device.guid)
    val randomKey: ByteArray = ByteArray(16) // 加密使用

    var miscBitMap: Int = 184024956 // 也可能是 150470524 ?
    var mainSigMap: Int = 16724722
    var subSigMap: Int = 0x10400 //=66,560

    private val _ssoSequenceId: AtomicInt = atomic(85600)

    @MiraiInternalAPI("Do not use directly. Get from the lambda param of buildSsoPacket")
    internal fun nextSsoSequenceId() = _ssoSequenceId.addAndGet(2)

    var openAppId: Long = 715019303L

    val apkVersionName: ByteArray = "8.2.0".toByteArray()

    var loginState = 0

    val appClientVersion: Int = 0

    var networkType: NetworkType = NetworkType.WIFI

    val apkSignatureMd5: ByteArray = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D".hexToBytes()

    /**
     * 协议版本?, 8.2.0 的为 8001
     */
    val protocolVersion: Short = 8001

    @Suppress("SpellCheckingInspection")
    @PublishedApi
    internal val apkId: ByteArray = "com.tencent.mobileqq".toByteArray()

    /*
     * 以下登录使用
     */


    var t150: ByteArray? = null
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
    @UseExperimental(MiraiExperimentalAPI::class, MiraiInternalAPI::class)
    var uin: Long = bot.account.id
    var t530: ByteArray? = null
    var t528: ByteArray? = null
    /**
     * t108 时更新
     */
    var ksid: String = "|454001228437590|A8.2.0.27f6ea96"
    /**
     * t186
     */
    var pwdFlag: Boolean = false
    /**
     * t537
     */
    var loginExtraData: LoginExtraData? = null
    lateinit var wFastLoginInfo: WFastLoginInfo
    lateinit var reserveUinInfo: ReserveUinInfo
    var wLoginSigInfo: WLoginSigInfo? = null
    var tlv113: ByteArray? = null
    lateinit var qrPushSig: ByteArray

    lateinit var mainDisplayName: String
}

class ReserveUinInfo(
    val imgType: ByteArray,
    val imgFormat: ByteArray,
    val imgUrl: ByteArray
)

class WFastLoginInfo(
    val outA1: ByteReadPacket,
    var adUrl: String = "",
    var iconUrl: String = "",
    var profileUrl: String = "",
    var userJson: String = ""
)

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
)

class LoginExtraData(
    val uin: Long,
    val ip: ByteArray,
    val time: Int,
    val version: Int
)

class WLoginSigInfo(
    val uin: Long,
    val encryptA1: ByteArray, // sigInfo[0]
    val noPicSig: ByteArray, // sigInfo[1]
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
    val psKey: PSKey,
    val superKey: ByteArray,
    val payToken: ByteArray,
    val pf: ByteArray,
    val pfKey: ByteArray,
    val da2: ByteArray,
    //  val pt4Token: ByteArray,
    val wtSessionTicket: WtSessionTicket,
    val wtSessionTicketKey: ByteArray,
    val deviceToken: ByteArray
)

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
class PSKey(data: ByteArray, creationTime: Long) : KeyWithCreationTime(data, creationTime)
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