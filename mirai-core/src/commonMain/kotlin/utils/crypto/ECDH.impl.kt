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
import javax.crypto.Cipher
import javax.crypto.KeyAgreement

internal object ECDHImpl {
    /*
    Android Source Code Comment:
    https://cs.android.com/android/platform/superproject/+/master:libcore/ojluni/src/main/java/sun/security/jca/Providers.java;l=347-354;bpv=1;bpt=1
    // Applications may install their own BC provider, only the algorithms from the system
    // provider are deprecated.
    */

    @JvmStatic
    val BC_PROVIDER: Provider by lazy {
        BouncyCastleProvider()
    }


    @JvmStatic
    val EC_KEY_FACTORY: KeyFactory by lazy {
        KeyFactory.getInstance("EC", BC_PROVIDER)
    }

    @JvmStatic
    fun newCipher(): Cipher {
        return Cipher.getInstance("ECIES", BC_PROVIDER)
    }

    @JvmStatic
    fun genKeyPair(
        curveName: String = "prime256v1", // p-256
    ): KeyPair {
        return KeyPairGenerator.getInstance("ECDH", BC_PROVIDER)
            .also { it.initialize(ECGenParameterSpec(curveName)) }
            .genKeyPair()
    }

    @JvmStatic
    fun constructPublicKey(key: ByteArray): PublicKey {
        return EC_KEY_FACTORY.generatePublic(X509EncodedKeySpec(key))
    }

    @JvmStatic
    fun calcShareKey(
        priKey: PrivateKey,
        pubKey: PublicKey,
    ): ByteArray {
        val instance = KeyAgreement.getInstance("ECDH", BC_PROVIDER)
        instance.init(priKey)
        instance.doPhase(pubKey, true)
        return instance.generateSecret().copyOf(16).md5()
    }

    @JvmStatic
    fun verifyPubKey(version: Int, publicKey: String, publicKeySign: String): Boolean {
        val arrayForVerify = "305$version$publicKey".toByteArray()
        val signInstance = Signature.getInstance("SHA256WithRSA")
        signInstance.initVerify(publicKeyForVerify)
        signInstance.update(arrayForVerify)
        return signInstance.verify(publicKeySign.decodeBase64())
    }
}



@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ECDHPrivateKey = PrivateKey

@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ECDHPublicKey = PublicKey

internal class ECDHKeyPairImpl(
    private val delegate: KeyPair,
    initialPublicKey: ECDHPublicKey = defaultInitialPublicKey.key,
) : ECDHKeyPair {
    override val privateKey: ECDHPrivateKey get() = delegate.private
    override val publicKey: ECDHPublicKey get() = delegate.public
    override val maskedShareKey: ByteArray by lazy { ECDH.calculateShareKey(privateKey, initialPublicKey) }
    override val maskedPublicKey: ByteArray by lazy { publicKey.encoded.copyOfRange(26, 91) }
}

private val publicKeyForVerify by lazy {
    KeyFactory.getInstance("RSA")
        .generatePublic(X509EncodedKeySpec("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuJTW4abQJXeVdAODw1CamZH4QJZChyT08ribet1Gp0wpSabIgyKFZAOxeArcCbknKyBrRY3FFI9HgY1AyItH8DOUe6ajDEb6c+vrgjgeCiOiCVyum4lI5Fmp38iHKH14xap6xGaXcBccdOZNzGT82sPDM2Oc6QYSZpfs8EO7TYT7KSB2gaHz99RQ4A/Lel1Vw0krk+DescN6TgRCaXjSGn268jD7lOO23x5JS1mavsUJtOZpXkK9GqCGSTCTbCwZhI33CpwdQ2EHLhiP5RaXZCio6lksu+d8sKTWU1eEiEb3cQ7nuZXLYH7leeYFoPtbFV4RicIWp0/YG+RP7rLPCwIDAQAB".decodeBase64()))
}
