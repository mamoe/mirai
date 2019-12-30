package net.mamoe.mirai.qqandroid.network

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.qqandroid.utils.*

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

    var miscBitMap: Int = 150470524
    var mainSigMap: Int = 16724722
    var subSigMap: Int = 0x10400 //=66,560

    var ssoSequenceId: Int = 0
    var openAppId: Long = 715019303L

    var ipv6NetType: Int = TODO()

    val apkVersionName: ByteArray = "8.2.0".toByteArray()

    val appClientVersion: Int = TODO()

    val apkSignatureMd5: ByteArray = TODO()

    /**
     * 协议版本?, 8.2.0 的为 8001
     */
    val protocolVersion: Short = 8001

    @Suppress("SpellCheckingInspection")
    @PublishedApi
    internal val apkId: ByteArray = "com.tencent.mobileqq".toByteArray()
}

