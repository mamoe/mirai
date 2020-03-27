/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils.cryptor

import net.mamoe.mirai.utils.io.toUHexString

internal object MultiMsgCryptor {
    private val impl = class_1457()

    fun decrypt(data: ByteArray, offset: Int, length: Int, key: ByteArray): ByteArray {
        return this.impl.method_67425(data, offset, length, key) ?: error("MultiMsgCryptor decypt failed: key=${key.toUHexString()}, data=${data.drop(offset).take(length).toByteArray().toUHexString()}")
    }

    fun decrypt(data: ByteArray, key: ByteArray): ByteArray {
        return this.impl.method_67426(data, key) ?: error("MultiMsgCryptor decrypt failed: key=${key.toUHexString()}, data=${data.toUHexString()}")
    }

    fun enableResultRandom(enabled: Boolean) {
        this.impl.method_67424(enabled)
    }

    fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        return this.impl.method_67427(data, key)
    }
}