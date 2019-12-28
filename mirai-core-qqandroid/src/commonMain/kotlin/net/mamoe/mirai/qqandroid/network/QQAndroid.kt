package net.mamoe.mirai.qqandroid.network

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.utils.io.chunkedHexToBytes

/**
 * From QQAndroid 8.2.0
 * `oicq.wlogin_sdk.tools.EcdhCrypt`
 *
 * Constant to avoid calculations
 */
interface ECDH {
    object Default : ECDH {
        override val publicKey: ByteArray = "020b03cf3d99541f29ffec281bebbd4ea211292ac1f53d7128".chunkedHexToBytes()
        override val shareKey: ByteArray = "4da0f614fc9f29c2054c77048a6566d7".chunkedHexToBytes()
        override val privateKey: ByteArray = ByteArray(16)
    }

    val publicKey: ByteArray

    val shareKey: ByteArray

    val privateKey: ByteArray
}


/*
 APP ID:
 GetStViaSMSVerifyLogin = 16
 GetStWithoutPasswd = 16
 */


class QQAndroidDevice(
    private val account: BotAccount,
    /**
     * 协议版本?, 8.2.0 的为 8001
     */
    @PublishedApi
    internal val protocolVersion: Short = 8001,

    @PublishedApi
    internal val ecdh: ECDH = ECDH.Default,

    @PublishedApi
    internal val appClientVersion: Int
) {
    val uin: Long get() = account.id
    val password: String get() = account.password

    object Debugging {

    }
}