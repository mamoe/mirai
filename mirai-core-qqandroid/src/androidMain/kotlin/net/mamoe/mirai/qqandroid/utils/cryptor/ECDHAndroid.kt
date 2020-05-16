/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils.cryptor

import android.annotation.SuppressLint
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils.md5
import net.mamoe.mirai.utils.MiraiLogger
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement


actual typealias ECDHPrivateKey = PrivateKey
actual typealias ECDHPublicKey = PublicKey

internal actual class ECDHKeyPairImpl(
    private val delegate: KeyPair
) : ECDHKeyPair {
    override val privateKey: ECDHPrivateKey get() = delegate.private
    override val publicKey: ECDHPublicKey get() = delegate.public

    override val initialShareKey: ByteArray = ECDH.calculateShareKey(privateKey, initialPublicKey)
}

@Suppress("FunctionName")
internal actual fun ECDH() = ECDH(ECDH.generateKeyPair())

internal actual class ECDH actual constructor(actual val keyPair: ECDHKeyPair) {
    actual companion object {
        @Suppress("ObjectPropertyName")
        private var _isECDHAvailable: Boolean = false // because `runCatching` has no contract.
        actual val isECDHAvailable: Boolean get() = _isECDHAvailable

        init {
            fun testECDH() {
                ECDHKeyPairImpl(
                    KeyPairGenerator.getInstance("ECDH")
                        .also { it.initialize(ECGenParameterSpec("secp192k1")) }
                        .genKeyPair()).let {
                    calculateShareKey(it.privateKey, it.publicKey)
                }
            }

            @SuppressLint("PrivateApi")
            if (kotlin.runCatching { testECDH() }.isFailure) {
                kotlin.runCatching {
                    val providerName = "BC"

                    if (Security.getProvider(providerName) != null) {
                        Security.removeProvider(providerName)
                    }
                    @Suppress("SpellCheckingInspection")
                    Security.addProvider(
                        Class.forName(
                            "com.android.org.bouncycastle.jce.provider.BouncyCastleProvider",
                            true,
                            ClassLoader.getSystemClassLoader()
                        ).newInstance() as Provider
                    )
                    testECDH()
                    _isECDHAvailable = true
                }.exceptionOrNull()?.let {
                    _isECDHAvailable = false
                    @Suppress("DEPRECATION")
                    MiraiLogger.error(it)
                }
            }
        }

        actual fun generateKeyPair(): ECDHKeyPair {
            if (!isECDHAvailable) {
                return ECDHKeyPair.DefaultStub
            }
            return ECDHKeyPairImpl(KeyPairGenerator.getInstance("ECDH")
                .also { it.initialize(ECGenParameterSpec("secp192k1")) }
                .genKeyPair())
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

    actual override fun toString(): String {
        return "ECDH(keyPair=$keyPair)"
    }
}