package net.mamoe.mirai.qqandroid.network

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.qqandroid.utils.*
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.hexToBytes

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
    val context: Context,
    val account: BotAccount,

    val ecdh: ECDH = ECDH.Default,
    val device: DeviceInfo = SystemDeviceInfo(context)
) {
    val tgtgtKey: ByteArray = generateTgtgtKey(device.guid)

    var miscBitMap: Int = 184024956 // 也可能是 150470524 ?
    var mainSigMap: Int = 16724722
    var subSigMap: Int = 0x10400 //=66,560

    private val _ssoSequenceId: AtomicInt = atomic(85600)

    @MiraiInternalAPI("Do not use directly. Get from the lambda param of buildSsoPacket")
    internal fun nextSsoSequenceId() = _ssoSequenceId.addAndGet(2)

    var openAppId: Long = 715019303L

    val apkVersionName: ByteArray = "8.2.0".toByteArray()

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
}

