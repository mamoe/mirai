/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import net.mamoe.mirai.utils.decodeBase64
import net.mamoe.mirai.utils.encodeBase64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


internal actual fun rsaEncryptWithX509PubKey(input: ByteArray, pubPemKey: ByteArray, seed: Long): ByteArray {
    val encodedKey = pubPemKey.decodeToString()
        .replace("\n", "")
        .removePrefix("-----BEGIN PUBLIC KEY-----")
        .removeSuffix("-----END PUBLIC KEY-----")
        .trim()
        .decodeBase64()

    val keyFactory = KeyFactory.getInstance("RSA")
    val rsaPubKey = keyFactory.generatePublic(X509EncodedKeySpec(encodedKey))


    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, rsaPubKey, SecureRandom().apply { setSeed(seed) })

    return cipher.doFinal(input)
}

internal actual fun rsaDecryptWithPKCS8PrivKey(input: ByteArray, privPemKey: ByteArray, seed: Long): ByteArray {
    val encodedKey = privPemKey.decodeToString()
        .replace("\n", "")
        .removePrefix("-----BEGIN PRIVATE KEY-----")
        .removeSuffix("-----END PRIVATE KEY-----")
        .trim()
        .decodeBase64()

    val keyFactory = KeyFactory.getInstance("RSA")
    val rsaPubKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(encodedKey))


    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, rsaPubKey, SecureRandom().apply { setSeed(seed) })

    return cipher.doFinal(input)
}

internal actual fun generateRSAKeyPair(keySize: Int): RSAKeyPair {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(keySize)

    val keyPair = keyGen.generateKeyPair()
    return RSAKeyPair(
        pubPemKey = buildString {
            appendLine("-----BEGIN PUBLIC KEY-----")
            keyPair.public.encoded.encodeBase64().chunked(64).forEach(::appendLine)
            appendLine("-----END PUBLIC KEY-----")
        }.encodeToByteArray(),
        privPemKey = buildString {
            appendLine("-----BEGIN PRIVATE KEY-----")
            keyPair.private.encoded.encodeBase64().chunked(64).forEach(::appendLine)
            appendLine("-----END PRIVATE KEY-----")
        }.encodeToByteArray()
    )
}