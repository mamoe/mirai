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

import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.internal.network.components.PacketCodec
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.SecretsProtection
import net.mamoe.mirai.utils.md5


internal class BotAccount(
    internal val id: Long,

    val authorization: BotAuthorization,

    val phoneNumber: String = "",
) {
    var passwordMd5Buffer: SecretsProtection.EscapedByteBuffer? = null
    var accountSecretsKeyBuffer: SecretsProtection.EscapedByteBuffer? = null


    constructor(id: Long, passwordMd5: ByteArray, phoneNumber: String = "") : this(
        id, BotAuthorization.Companion.byPassword(passwordMd5), phoneNumber
    )

    constructor(id: Long, passwordPlainText: String, phoneNumber: String = "") : this(
        id,
        passwordPlainText.md5(),
        phoneNumber
    ) {
        require(passwordPlainText.length <= 16) { "Password length must be at most 16." }
    }

    val passwordMd5: ByteArray
        get() {
            passwordMd5Buffer?.let { return it.asByteArray }

            if (PacketCodec.PacketLogger.isWarningEnabled) {
                PacketCodec.PacketLogger.warning(
                    Throwable("BotAccount.passwordMd5 was called but not login as password")
                )
            }

            return EMPTY_BYTE_ARRAY
        }

    val accountSecretsKey: ByteArray
        get() {
            accountSecretsKeyBuffer?.let { return it.asByteArray }
            error("accountSecretsKey not yet available")
        }

}