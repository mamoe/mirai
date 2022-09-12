/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package net.mamoe.mirai.internal.utils.crypto

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.md5

private val defaultPublicKey =
    "04edb8906046f5bfbe9abbc5a88b37d70a6006bfbabc1f0cd49dfb33505e63efc5d78ee4e0a4595033b93d02096dcd3190279211f7b4f6785079e19004aa0e03bc".hexToBytes()
private val defaultQQShareKey = "c129edba736f4909ecc4ab8e010f46a3".hexToBytes()

@Serializable
internal data class QQEcdhInitialPublicKey(val version: Int = 1, val keyStr: String, val expireTime: Long = 0) {
    @Transient
    internal val key = Ecdh.Instance.importPublicKey(keyStr.hexToBytes())
    companion object {
        internal val default: QQEcdhInitialPublicKey by lazy {
            QQEcdhInitialPublicKey(keyStr = "04EBCA94D733E399B2DB96EACDD3F69A8BB0F74224E2B44E3357812211D2E62EFBC91BB553098E25E33A799ADC7F76FEB208DA7C6522CDB0719A305180CC54A82E")
        }
    }
}

internal expect fun QQEcdhInitialPublicKey.verify(sign: String): Boolean

internal data class QQEcdh(private val initialPublicKey: QQEcdhInitialPublicKey = QQEcdhInitialPublicKey.default) {
    val version: Int = initialPublicKey.version
    private val keyPair = try {
        Ecdh.Instance.generateKeyPair()
    } catch (e:Throwable){
        null
    }
    val publicKey: ByteArray = keyPair?.let {
        Ecdh.Instance.exportPublicKey(it.public)
    } ?: defaultPublicKey

    val initialQQShareKey: ByteArray = keyPair?.let {
        Ecdh.Instance.calculateShareKey(initialPublicKey.key, it.private).copyOf(16).md5()
    } ?: defaultQQShareKey

    val fallbackMode : Boolean = keyPair == null

    /**
     * 由 [keyPair] 的私匙和 [peerKey] 计算 shareKey
     */
    fun calculateQQShareKey(peerKey: Any): ByteArray {
        check (keyPair != null) {
            "cannot calculate QQShareKey in fallback mode"
        }
        return Ecdh.Instance.calculateShareKey(peerKey.cast(), keyPair.private).copyOf(16).md5()
    }
}