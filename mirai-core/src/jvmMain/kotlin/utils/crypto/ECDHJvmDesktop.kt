/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import net.mamoe.mirai.utils.decodeBase64
import net.mamoe.mirai.utils.md5
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

internal actual class ECDH actual constructor(actual val keyPair: ECDHKeyPair) {
    actual companion object {
        private const val curveName = "prime256v1" // p-256

        actual val isECDHAvailable: Boolean

        init {
            isECDHAvailable = kotlin.runCatching {
                fun testECDH() {
                    ECDHKeyPairImpl(
                        KeyPairGenerator.getInstance("ECDH")
                            .also { it.initialize(ECGenParameterSpec(curveName)) }
                            .genKeyPair()).let {
                        calculateShareKey(it.privateKey, it.publicKey)
                    }
                }

                if (kotlin.runCatching { testECDH() }.isSuccess) {
                    return@runCatching
                }

                if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
                    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
                }
                Security.addProvider(BouncyCastleProvider())
                testECDH()
            }.onFailure {
                it.printStackTrace()
            }.isSuccess
        }

        actual fun generateKeyPair(initialPublicKey: ECDHPublicKey): ECDHKeyPair {
            if (!isECDHAvailable) {
                return ECDHKeyPair.DefaultStub
            }
            return ECDHKeyPairImpl(
                KeyPairGenerator.getInstance("ECDH")
                    .also { it.initialize(ECGenParameterSpec(curveName)) }
                    .genKeyPair(), initialPublicKey)
        }

        actual fun verifyPublicKey(version: Int, publicKey: String, publicKeySign: String): Boolean {
            val arrayForVerify = "305$version$publicKey".toByteArray()
            val signInstance = Signature.getInstance("SHA256WithRSA")
            signInstance.initVerify(publicKeyForVerify)
            signInstance.update(arrayForVerify)
            return signInstance.verify(publicKeySign.decodeBase64())
        }

        actual fun calculateShareKey(
            privateKey: ECDHPrivateKey,
            publicKey: ECDHPublicKey,
        ): ByteArray {
            val instance = KeyAgreement.getInstance("ECDH", "BC")
            instance.init(privateKey)
            instance.doPhase(publicKey, true)
            return instance.generateSecret().copyOf(16).md5()
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