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
import net.mamoe.mirai.utils.recoverCatchingSuppressed
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Provider
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

/**
 * 绕过在Android P之后的版本无法使用EC的限制
 * https://cs.android.com/android/platform/superproject/+/master:libcore/ojluni/src/main/java/sun/security/jca/Providers.java;l=371;bpv=1;bpt=1
 * https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html
 * */
@Suppress("DEPRECATION") // since JDK 9
private class AndroidProvider : Provider("sbAndroid", 1.0, "") {
    override fun getService(type: String?, algorithm: String?): Service? {
        if (type == "KeyFactory" && algorithm == "EC") {
            return object : Service(this, type, algorithm, "", emptyList(), emptyMap()) {
                override fun newInstance(constructorParameter: Any?): Any {
                    return org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi.EC()
                }
            }
        }
        return super.getService(type, algorithm)
    }
}

private val ANDROID_PROVIDER by lazy { AndroidProvider() }
private val ecKf by lazy {
    runCatching { KeyFactory.getInstance("EC", "BC") }
        .recoverCatchingSuppressed { KeyFactory.getInstance("EC", ANDROID_PROVIDER) }
        .getOrThrow()
}

internal actual class ECDH actual constructor(actual val keyPair: ECDHKeyPair) {
    actual companion object {
        private const val curveName = "prime256v1" // p-256

        actual val isECDHAvailable: Boolean

        init {
            isECDHAvailable = kotlin.runCatching {
                ecKf // init

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

        actual fun calculateShareKey(
            privateKey: ECDHPrivateKey,
            publicKey: ECDHPublicKey
        ): ByteArray {
            val instance = KeyAgreement.getInstance("ECDH", "BC")
            instance.init(privateKey)
            instance.doPhase(publicKey, true)
            return instance.generateSecret().copyOf(16).md5()
        }

        actual fun verifyPublicKey(version: Int, publicKey: String, publicKeySign: String): Boolean {
            val arrayForVerify = "305$version$publicKey".toByteArray()
            val signInstance = Signature.getInstance("SHA256WithRSA")
            signInstance.initVerify(publicKeyForVerify)
            signInstance.update(arrayForVerify)
            return signInstance.verify(publicKeySign.decodeBase64())
        }

        actual fun constructPublicKey(key: ByteArray): ECDHPublicKey {
            return ecKf.generatePublic(X509EncodedKeySpec(key))
        }
    }

    actual fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray {
        return calculateShareKey(keyPair.privateKey, peerPublicKey)
    }

    actual override fun toString(): String {
        return "ECDH(keyPair=$keyPair)"
    }
}