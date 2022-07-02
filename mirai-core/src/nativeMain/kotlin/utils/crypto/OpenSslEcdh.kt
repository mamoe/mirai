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
import openssl.*
import platform.posix.errno
import kotlin.native.internal.createCleaner

private const val curveId = NID_X9_62_prime256v1
private val group by lazy { EC_GROUP_new_by_curve_name(curveId) ?: error("Failed to get EC_GROUP") }
private val convForm by lazy { EC_GROUP_get_point_conversion_form(group) }
private val bnCtx by lazy { BN_CTX_new() }


internal class OpenSslECPublicKey private constructor(val point: CPointer<EC_POINT>) {
    @Suppress("unused")
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(point) {
        EC_POINT_free(it)
    }

    fun export(): ByteArray {
        val len = EC_POINT_point2oct(group, point, convForm, null, 0, null)
        val bytes = ByteArray(len.convert())
        bytes.usePinned {
            EC_POINT_point2oct(group, point, convForm, it.addressOf(0).reinterpret(), len, bnCtx)
        }
        return bytes
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? OpenSslECPublicKey)?.let {
            EC_POINT_cmp(group, point, it.point, bnCtx) == 0
        } ?: false
    }

    override fun hashCode(): Int {
        return export().hashCode()
    }

    companion object {
        fun copyFrom(source: CPointer<EC_POINT>): OpenSslECPublicKey {
            return OpenSslECPublicKey(EC_POINT_dup(source, group) ?: error("Failed to dup a EC_POINT"))
        }

        fun import(encoded: ByteArray): OpenSslECPublicKey {
            val point = EC_POINT_new(group) ?: error("Failed to create EC_POINT")
            encoded.usePinned {
                EC_POINT_oct2point(group, point, it.addressOf(0).reinterpret(), it.get().size.convert(), bnCtx)
            }
            return OpenSslECPublicKey(point)
        }
    }
}

internal class OpenSslECPrivateKey private constructor(val bn: CPointer<BIGNUM>) {
    @Suppress("unused")
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(bn) {
        BN_free(it)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? OpenSslECPrivateKey)?.let {
            BN_cmp(bn, other.bn) == 0
        } ?: false
    }

    fun export(): ByteArray {
        val len = (BN_num_bits(bn)+7)/8
        val bytes = ByteArray(len)
        bytes.usePinned {
            BN_bn2bin(bn, it.addressOf(0).reinterpret())
        }
        return bytes
    }

    override fun hashCode(): Int {
        return export().hashCode()
    }

    companion object {
        fun copyFrom(source: CPointer<BIGNUM>): OpenSslECPrivateKey {
            return OpenSslECPrivateKey(BN_dup(source) ?: error("Failed to dup a BIGNUM"))
        }
    }
}

internal class OpenSslEcdh : Ecdh<OpenSslECPublicKey, OpenSslECPrivateKey> {
    override fun generateKeyPair(): EcdhKeyPair<OpenSslECPublicKey, OpenSslECPrivateKey> {
        val key: CPointer<EC_KEY> = EC_KEY_new_by_curve_name(curveId)
            ?: throw IllegalStateException("Failed to create key curve, $errno")
        try {
            if (1 != EC_KEY_generate_key(key)) {
                throw IllegalStateException("Failed to generate key, $errno")
            }
            val public =
                OpenSslECPublicKey.copyFrom(EC_KEY_get0_public_key(key) ?: error("Failed EC_key_get0_public_key"))
            val private =
                OpenSslECPrivateKey.copyFrom(EC_KEY_get0_private_key(key) ?: error("Failed EC_KEY_get0_private_key"))
            return EcdhKeyPair(public, private)
        } finally {
            EC_KEY_free(key)
        }
    }

    override fun calculateShareKey(peerKey: OpenSslECPublicKey, privateKey: OpenSslECPrivateKey): ByteArray {
        val k = EC_KEY_new_by_curve_name(curveId) ?: error("Failed to create EC key")
        try {
            EC_KEY_set_private_key(k, privateKey.bn).let { r ->
                if (r != 1) error("Failed EC_KEY_set_private_key: $r")
            }
            val fieldSize = EC_GROUP_get_degree(group)
            if (fieldSize <= 0) {
                error("Failed EC_GROUP_get_degree: $fieldSize")
            }
            var secretLen = (fieldSize + 7) / 8
            ByteArray(secretLen.convert()).usePinned { pin ->
                secretLen = ECDH_compute_key(pin.addressOf(0), secretLen.convert(), peerKey.point, k, null)
                if (secretLen <= 0) {
                    error("Failed to compute secret")
                }
                return pin.get().copyOf(secretLen)
            }
        } finally {
            EC_KEY_free(k)
        }
    }

    override fun importPublicKey(encoded: ByteArray): OpenSslECPublicKey {
        return OpenSslECPublicKey.import(encoded)
    }

    override fun exportPublicKey(key: OpenSslECPublicKey): ByteArray {
        return key.export()
    }
}