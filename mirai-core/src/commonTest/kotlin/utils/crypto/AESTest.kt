/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.mirai.utils.getRandomString
import net.mamoe.mirai.utils.toUHexString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AESTest {
    @Test
    fun `can do crypto`() {
        val random = Random(currentTimeMillis())

        val key = getRandomString(16, random).encodeToByteArray()
        val iv = getRandomString(16, random).encodeToByteArray()
        val currentTime = currentTimeMillis()

        val plainText = buildString {
            append("Use of this source code is governed by the GNU AGPLv3 license ")
            append("that can be found through the following link. ")
            append(currentTime)
        }

        println("AES crypto test: key = ${key.toUHexString()}, iv = ${iv.toUHexString()}, currentTimeMillis = $currentTime")
        val encrypted = aesEncrypt(plainText.encodeToByteArray(), iv, key)
        val decrypted = aesDecrypt(encrypted, iv, key)

        assertEquals(plainText, decrypted.decodeToString())
    }
}