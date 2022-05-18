/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

internal actual interface ECDHPrivateKey
internal actual interface ECDHPublicKey {
    actual fun getEncoded(): ByteArray
}

internal actual class ECDHKeyPairImpl(
    override val privateKey: ECDHPrivateKey,
    override val publicKey: ECDHPublicKey,
    override val maskedShareKey: ByteArray,
    override val maskedPublicKey: ByteArray
) : ECDHKeyPair

/**
 * 椭圆曲线密码, ECDH 加密
 */
internal actual class ECDH actual constructor(keyPair: ECDHKeyPair) {
    actual val keyPair: ECDHKeyPair
        get() = TODO("Not yet implemented")

    /**
     * 由 [keyPair] 的私匙和 [peerPublicKey] 计算 shareKey
     */
    actual fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray {
        TODO("Not yet implemented")
    }

    actual companion object {
        actual val isECDHAvailable: Boolean
            get() = TODO("Not yet implemented")

        /**
         * 由完整的 publicKey ByteArray 得到 [ECDHPublicKey]
         */
        actual fun constructPublicKey(key: ByteArray): ECDHPublicKey {
            TODO("Not yet implemented")
        }

        /**
         * 由完整的 rsaKey 校验 publicKey
         */
        actual fun verifyPublicKey(
            version: Int,
            publicKey: String,
            publicKeySign: String
        ): Boolean {
            TODO("Not yet implemented")
        }

        /**
         * 生成随机密匙对
         */
        actual fun generateKeyPair(initialPublicKey: ECDHPublicKey): ECDHKeyPair {
            TODO("Not yet implemented")
        }

        /**
         * 由一对密匙计算 shareKey
         */
        actual fun calculateShareKey(
            privateKey: ECDHPrivateKey,
            publicKey: ECDHPublicKey
        ): ByteArray {
            TODO("Not yet implemented")
        }

    }

    actual override fun toString(): String {
        TODO("Not yet implemented")
    }

}

internal actual val publicKeyForVerify: ECDHPublicKey
    get() = TODO("Not yet implemented")
