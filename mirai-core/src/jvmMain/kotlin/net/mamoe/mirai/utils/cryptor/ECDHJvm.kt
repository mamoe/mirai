package net.mamoe.mirai.utils.cryptor

import net.mamoe.mirai.utils.md5
import java.security.*
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement


actual typealias ECDHPrivateKey = PrivateKey
actual typealias ECDHPublicKey = PublicKey

actual class ECDHKeyPair(
    private val delegate: KeyPair
) {
    actual val privateKey: ECDHPrivateKey get() = delegate.private
    actual val publicKey: ECDHPublicKey get() = delegate.public

    actual val shareKey: ByteArray = ECDH.calculateShareKey(privateKey, publicKey)
}

@Suppress("FunctionName")
actual fun ECDH() = ECDH(ECDH.generateKeyPair())

actual class ECDH actual constructor(actual val keyPair: ECDHKeyPair) {
    actual companion object {
        actual fun generateKeyPair(): ECDHKeyPair {
            return ECDHKeyPair(KeyPairGenerator.getInstance("ECDH").genKeyPair())
        }

        actual fun calculateShareKey(
            privateKey: ECDHPrivateKey,
            publicKey: ECDHPublicKey
        ): ByteArray {
            val instance = KeyAgreement.getInstance("ECDH", "BC")
            instance.init(privateKey)
            instance.doPhase(publicKey, true)
            return md5(instance.generateSecret())
        }

        actual fun constructPublicKey(key: ByteArray): ECDHPublicKey {
            return KeyFactory.getInstance("EC", "BC").generatePublic(X509EncodedKeySpec(key))
        }
    }

    actual fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray {
        return calculateShareKey(keyPair.privateKey, peerPublicKey)
    }
}