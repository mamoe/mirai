/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

internal data class RSAKeyPair(
    val pubPemKey: ByteArray,
    val privPemKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RSAKeyPair

        if (!pubPemKey.contentEquals(other.pubPemKey)) return false
        return privPemKey.contentEquals(other.privPemKey)
    }

    override fun hashCode(): Int {
        var result = pubPemKey.contentHashCode()
        result = 31 * result + privPemKey.contentHashCode()
        return result
    }
}

internal expect fun generateRSAKeyPair(keySize: Int): RSAKeyPair

internal expect fun rsaEncryptWithX509PubKey(input: ByteArray, pubPemKey: ByteArray, seed: Long): ByteArray

internal expect fun rsaDecryptWithPKCS8PrivKey(input: ByteArray, privPemKey: ByteArray, seed: Long): ByteArray