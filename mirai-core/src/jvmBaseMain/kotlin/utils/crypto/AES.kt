/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal actual fun aesEncrypt(input: ByteArray, iv: ByteArray, key: ByteArray): ByteArray {
    return doAES(input, iv, key, Cipher.ENCRYPT_MODE)
}

internal actual fun aesDecrypt(input: ByteArray, iv: ByteArray, key: ByteArray): ByteArray {
    return doAES(input, iv, key, Cipher.DECRYPT_MODE)
}

private fun doAES(input: ByteArray, iv: ByteArray, key: ByteArray, opMode: Int): ByteArray {
    val keySpec = SecretKeySpec(key, "AES")

    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(opMode, keySpec, IvParameterSpec(iv))

    return cipher.doFinal(input)
}