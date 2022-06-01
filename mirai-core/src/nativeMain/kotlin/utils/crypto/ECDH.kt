/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import kotlinx.cinterop.*
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.toUHexString
import openssl.*
import platform.posix.errno
import platform.posix.free

private const val curveId = NID_X9_62_prime256v1

// shared, not freed!
private val group by lazy { EC_GROUP_new_by_curve_name(curveId) ?: error("Failed to get EC_GROUP") }

private val convForm by lazy { EC_GROUP_get_point_conversion_form(group) }

// shared, not freed!
private val bnCtx by lazy { BN_CTX_new() }


internal actual interface ECDHPublicKey : OpenSSLKey {
    val encoded: ByteArray
    fun toPoint(): CPointer<EC_POINT>
}

internal actual interface ECDHPrivateKey : OpenSSLKey {
    fun toBignum(): CPointer<BIGNUM>
}

internal class OpenSslPrivateKey(
    override val hex: String, // use Kotlin's memory
) : ECDHPrivateKey {

    override fun toBignum(): CPointer<BIGNUM> {
        val bn = BN_new() ?: error("Failed BN_new")
        val values = cValuesOf(bn)
        BN_hex2bn(values, hex).let { r ->
            if (r <= 0) error("Failed BN_hex2bn: $r")
        }
        return bn
    }

    companion object {
        fun fromKey(key: CPointer<EC_KEY>): OpenSslPrivateKey {
            val bn = EC_KEY_get0_private_key(key) ?: error("Failed EC_KEY_get0_private_key")
            val hex = try {
                val ptr = BN_bn2hex(bn) ?: error("Failed EC_POINT_bn2point")
                try {
                    ptr.toKString()
                } finally {
                    free(ptr)
                }
            } finally {
                BN_free(bn)
            }
            return OpenSslPrivateKey(hex)
        }
    }
}

internal interface OpenSSLKey {
    val hex: String
}

internal class OpenSslPublicKey(override val hex: String) : ECDHPublicKey {
    override val encoded: ByteArray = hex.hexToBytes()

    override fun toPoint(): CPointer<EC_POINT> {
        val point = EC_POINT_new(group)
        EC_POINT_hex2point(group, hex, point, bnCtx) ?: error("Failed EC_POINT_hex2point")
        return point!!
    }

    companion object {
        fun fromKey(key: CPointer<EC_KEY>): OpenSslPublicKey =
            fromPoint(EC_KEY_get0_public_key(key) ?: error("Failed to get private key"))

        fun fromPoint(point: CPointer<EC_POINT>): OpenSslPublicKey {
            return OpenSslPublicKey(point.toKtHex())
        }
    }
}

internal actual class ECDHKeyPairImpl(
    override val privateKey: OpenSslPrivateKey,
    override val publicKey: OpenSslPublicKey,
    private val initialPublicKey: ECDHPublicKey
) : ECDHKeyPair {

    override val maskedPublicKey: ByteArray by lazy { publicKey.encoded }
    override val maskedShareKey: ByteArray by lazy { ECDH.calculateShareKey(privateKey, initialPublicKey) }

    companion object {
        fun fromKey(
            key: CPointer<EC_KEY>,
            initialPublicKey: ECDHPublicKey = defaultInitialPublicKey.key
        ): ECDHKeyPairImpl {
            return ECDHKeyPairImpl(OpenSslPrivateKey.fromKey(key), OpenSslPublicKey.fromKey(key), initialPublicKey)
        }
    }
}

private fun CPointer<EC_POINT>.toKtHex(): String {
    val ptr = EC_POINT_point2hex(group, this, convForm, bnCtx) ?: error("Failed EC_POINT_point2hex")
    return try {
        ptr.toKString()
    } finally {
        free(ptr)
    }
}


internal actual class ECDH actual constructor(actual val keyPair: ECDHKeyPair) {

    /**
     * 由 [keyPair] 的私匙和 [peerPublicKey] 计算 shareKey
     */
    actual fun calculateShareKeyByPeerPublicKey(peerPublicKey: ECDHPublicKey): ByteArray {
        return calculateShareKey(keyPair.privateKey, peerPublicKey)
    }

    actual companion object {
        actual val isECDHAvailable: Boolean get() = true

        /**
         * 由完整的 publicKey ByteArray 得到 [ECDHPublicKey]
         */
        actual fun constructPublicKey(key: ByteArray): ECDHPublicKey {
            memScoped {
                key.usePinned { pin ->
                    val group = EC_GROUP_new_by_curve_name(curveId)
                        ?: error("Failed to create EC_GROUP")

                    val p = EC_POINT_new(group) ?: error("Failed to create EC_POINT")

                    EC_POINT_hex2point(group, pin.get().toUHexString("").lowercase(), p, bnCtx)

                    return OpenSslPublicKey.fromPoint(p)
                }
            }

        }

        /**
         * 由完整的 rsaKey 校验 publicKey
         */
        actual fun verifyPublicKey(
            version: Int,
            publicKey: String,
            publicKeySign: String
        ): Boolean = true

        /**
         * 生成随机密匙对
         */
        actual fun generateKeyPair(initialPublicKey: ECDHPublicKey): ECDHKeyPair {
            val key: CPointer<EC_KEY> = EC_KEY_new_by_curve_name(curveId)
                ?: throw IllegalStateException("Failed to create key curve, $errno")

            if (1 != EC_KEY_generate_key(key)) {
                throw IllegalStateException("Failed to generate key, $errno")
            }

            try {
                return ECDHKeyPairImpl.fromKey(key, initialPublicKey)
            } finally {
                free(key) // TODO: THIS MAY CAUSE MEMORY LEAK. But EC_KEY_free() will terminate the process for unknown reason.
            }
        }

        fun calculateCanonicalShareKey(privateKey: ECDHPrivateKey, publicKey: ECDHPublicKey): ByteArray {
            check(publicKey is OpenSslPublicKey)
            check(privateKey is OpenSslPrivateKey)

            val k = EC_KEY_new_by_curve_name(curveId) ?: error("Failed to create EC key")
            try {
                val privateBignum = privateKey.toBignum()
                try {
                    EC_KEY_set_private_key(k, privateKey.toBignum()).let { r ->
                        if (r != 1) error("Failed EC_KEY_set_private_key: $r")
                    }

                    val fieldSize = EC_GROUP_get_degree(group)
                    if (fieldSize <= 0) {
                        error("Failed EC_GROUP_get_degree: $fieldSize")
                    }

                    var secretLen = (fieldSize + 7) / 8

                    val publicPoint = publicKey.toPoint()
                    try {
                        ByteArray(secretLen.convert()).usePinned { pin ->
                            secretLen = ECDH_compute_key(pin.addressOf(0), secretLen.convert(), publicPoint, k, null)
                            if (secretLen <= 0) {
                                error("Failed to compute secret")
                            }

                            return pin.get().copyOf(secretLen)
                        }
                    } finally {
                        EC_POINT_free(publicPoint)
                    }
                } finally {
                    BN_free(privateBignum)
                }
            } finally {
                EC_KEY_free(k)
            }
        }

        actual fun calculateShareKey(
            privateKey: ECDHPrivateKey,
            publicKey: ECDHPublicKey
        ): ByteArray = calculateCanonicalShareKey(privateKey, publicKey).copyOf(16).md5()
    }

    actual override fun toString(): String = "ECDH($keyPair)"
}

internal actual fun ByteArray.adjustToPublicKey(): ECDHPublicKey {
    return ECDH.constructPublicKey(this)
}
