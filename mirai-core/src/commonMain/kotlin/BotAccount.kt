/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("EXPERIMENTAL_API_USAGE", "DEPRECATION_ERROR")

package net.mamoe.mirai.internal

import net.mamoe.mirai.utils.SecretsProtection
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.toUHexString


internal data class BotAccount(
    internal val id: Long,

    val passwordMd5Buffer: SecretsProtection.EscapedByteBuffer, // md5

    val phoneNumber: String = "",
) {
    init {
        check(passwordMd5Buffer.size == 16) {
            "Invalid passwordMd5: size must be 16 but got ${passwordMd5Buffer.size}. passwordMd5=${passwordMd5.toUHexString()}"
        }
    }

    constructor(id: Long, passwordMd5: ByteArray, phoneNumber: String = "") : this(
        id, SecretsProtection.EscapedByteBuffer(SecretsProtection.escape(passwordMd5)), phoneNumber
    )

    constructor(id: Long, passwordPlainText: String, phoneNumber: String = "") : this(
        id,
        passwordPlainText.md5(),
        phoneNumber
    ) {
        require(passwordPlainText.length <= 16) { "Password length must be at most 16." }
    }

    val passwordMd5: ByteArray get() = passwordMd5Buffer.asByteArray


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BotAccount

        if (id != other.id) return false
        if (passwordMd5Buffer.data != other.passwordMd5Buffer.data) return false

        return true
    }


    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + passwordMd5Buffer.hashCode()
        return result
    }
}