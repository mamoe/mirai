/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.cryptor

import net.mamoe.mirai.utils.io.chunkedHexToBytes
import net.mamoe.mirai.utils.md5
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement


actual typealias ECDHPrivateKey = PrivateKey
actual typealias ECDHPublicKey = PublicKey

actual class ECDHKeyPair(
    private val delegate: KeyPair?
) {
    actual val privateKey: ECDHPrivateKey get() = delegate?.private ?: error("ECDH is not available")
    actual val publicKey: ECDHPublicKey get() = delegate?.public ?: defaultPublicKey

    actual val initialShareKey: ByteArray = if (delegate == null) {
        defaultShareKey
    } else ECDH.calculateShareKey(privateKey, initialPublicKey)

    companion object {
        internal val defaultPublicKey = "020b03cf3d99541f29ffec281bebbd4ea211292ac1f53d7128".chunkedHexToBytes().adjustToPublicKey()
        internal val defaultShareKey = "4da0f614fc9f29c2054c77048a6566d7".chunkedHexToBytes()
    }
}

@Suppress("FunctionName")
actual fun ECDH() = ECDH(ECDH.generateKeyPair())

actual class ECDH actual constructor(actual val keyPair: ECDHKeyPair) {
    actual companion object {
        private var isECDHAvailable = true

        init {
            isECDHAvailable = kotlin.runCatching {
                Security.addProvider(BouncyCastleProvider())
                generateKeyPair() // try if it is working
            }.isSuccess
        }

        actual fun generateKeyPair(): ECDHKeyPair {
            return if (!isECDHAvailable) {
                ECDHKeyPair(null)
            } else ECDHKeyPair(KeyPairGenerator.getInstance("EC", "BC").apply { initialize(ECGenParameterSpec("secp192k1")) }.genKeyPair())
        }

        actual fun calculateShareKey(
            privateKey: ECDHPrivateKey,
            publicKey: ECDHPublicKey
        ): ByteArray {
            return if (!isECDHAvailable) {
                ECDHKeyPair.defaultShareKey
            } else {
                val instance = KeyAgreement.getInstance("ECDH", "BC")
                instance.init(privateKey)
                instance.doPhase(publicKey, true)
                md5(instance.generateSecret())
            }
        }

        actual fun constructPublicKey(key: ByteArray): ECDHPublicKey {
            return KeyFactory.getInstance("EC", "BC").generatePublic(X509EncodedKeySpec(key))
        }
    }

    actual fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray {
        return calculateShareKey(keyPair.privateKey, peerPublicKey)
    }

    actual override fun toString(): String {
        return "ECDH(keyPair=$keyPair)"
    }
}