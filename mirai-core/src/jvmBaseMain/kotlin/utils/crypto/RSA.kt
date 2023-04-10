/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

internal actual fun rsaEncrypt(input: ByteArray, pemKey: String, seed: Long): ByteArray {
    val encodedKey = Base64.getDecoder().decode(
        pemKey
            .removePrefix("-----BEGIN PUBLIC KEY-----")
            .removeSuffix("-----END PUBLIC KEY-----")
            .trim()
            .replace("\n", "")
    )

    val keyFactory = KeyFactory.getInstance("RSA")
    val rsaPubKey = keyFactory.generatePublic(X509EncodedKeySpec(encodedKey))


    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, rsaPubKey, SecureRandom().apply { setSeed(seed) })

    return cipher.doFinal(input)
}