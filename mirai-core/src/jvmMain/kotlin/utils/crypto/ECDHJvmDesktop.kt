/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey


@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ECDHPrivateKey = PrivateKey
@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ECDHPublicKey = PublicKey

internal actual class ECDHKeyPairImpl(
    private val delegate: KeyPair,
    initialPublicKey: ECDHPublicKey = defaultInitialPublicKey.key,
) : ECDHKeyPair {
    override val privateKey: ECDHPrivateKey get() = delegate.private
    override val publicKey: ECDHPublicKey get() = delegate.public
    override val maskedShareKey: ByteArray by lazy { ECDH.calculateShareKey(privateKey, initialPublicKey) }
    override val maskedPublicKey: ByteArray by lazy { publicKey.encoded.copyOfRange(26, 91) }
}

internal actual class ECDH actual constructor(actual val keyPair: ECDHKeyPair) {
    actual companion object {
        init {
            ECDHImpl.BC_PROVIDER
            ECDHImpl.EC_KEY_FACTORY // Init lazy
        }

        actual fun generateKeyPair(initialPublicKey: ECDHPublicKey): ECDHKeyPair {
            return ECDHKeyPairImpl(ECDHImpl.genKeyPair(), initialPublicKey)
        }

        actual fun verifyPublicKey(version: Int, publicKey: String, publicKeySign: String): Boolean {
            return ECDHImpl.verifyPubKey(version, publicKey, publicKeySign)
        }

        actual fun calculateShareKey(
            privateKey: ECDHPrivateKey,
            publicKey: ECDHPublicKey,
        ): ByteArray {
            return ECDHImpl.calcShareKey(privateKey, publicKey)
        }

        actual fun constructPublicKey(key: ByteArray): ECDHPublicKey {
            return ECDHImpl.constructPublicKey(key)
        }
    }

    actual fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray {
        return calculateShareKey(keyPair.privateKey, peerPublicKey)
    }

    actual override fun toString(): String {
        return "ECDH(keyPair=$keyPair)"
    }
}