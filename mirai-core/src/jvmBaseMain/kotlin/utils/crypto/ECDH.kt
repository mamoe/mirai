/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("ECDHKt_jvmBase")

package net.mamoe.mirai.internal.utils.crypto

import net.mamoe.mirai.utils.decodeBase64
import net.mamoe.mirai.utils.hexToBytes
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec



@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ECDHPrivateKey = PrivateKey
@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ECDHPublicKey = PublicKey

internal actual class ECDHKeyPairImpl(
    private val delegate: KeyPair,
    initialPublicKey: ECDHPublicKey = defaultInitialPublicKey.key
) : ECDHKeyPair {
    override val privateKey: ECDHPrivateKey get() = delegate.private
    override val publicKey: ECDHPublicKey get() = delegate.public
    override val maskedPublicKey: ByteArray by lazy { publicKey.encoded.copyOfRange(26, 91) }
    override val maskedShareKey: ByteArray by lazy { ECDH.calculateShareKey(privateKey, initialPublicKey) }
}


internal val publicKeyForVerify: ECDHPublicKey by lazy {
    KeyFactory.getInstance("RSA")
        .generatePublic(X509EncodedKeySpec("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuJTW4abQJXeVdAODw1CamZH4QJZChyT08ribet1Gp0wpSabIgyKFZAOxeArcCbknKyBrRY3FFI9HgY1AyItH8DOUe6ajDEb6c+vrgjgeCiOiCVyum4lI5Fmp38iHKH14xap6xGaXcBccdOZNzGT82sPDM2Oc6QYSZpfs8EO7TYT7KSB2gaHz99RQ4A/Lel1Vw0krk+DescN6TgRCaXjSGn268jD7lOO23x5JS1mavsUJtOZpXkK9GqCGSTCTbCwZhI33CpwdQ2EHLhiP5RaXZCio6lksu+d8sKTWU1eEiEb3cQ7nuZXLYH7leeYFoPtbFV4RicIWp0/YG+RP7rLPCwIDAQAB".decodeBase64()))
}

private val signHead = "3059301306072a8648ce3d020106082a8648ce3d030107034200".hexToBytes()

internal actual fun ByteArray.adjustToPublicKey(): ECDHPublicKey {
    return ECDH.constructPublicKey(signHead + this)
}
