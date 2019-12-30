package net.mamoe.mirai.qqandroid.utils

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