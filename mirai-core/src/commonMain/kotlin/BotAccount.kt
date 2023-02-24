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
import net.mamoe.mirai.utils.SecretsProtection


internal class BotAccount(
    internal val id: Long,
    val authorization: BotAuthorization,
    val phoneNumber: String = "",
) {
    constructor(
        id: Long, pwd: String, phoneNumber: String = ""
    ) : this(id, BotAuthorization.byPassword(pwd), phoneNumber)

    var accountSecretsKeyBuffer: SecretsProtection.EscapedByteBuffer? = null

    val accountSecretsKey: ByteArray
        get() {
            accountSecretsKeyBuffer?.let { return it.asByteArray }
            error("accountSecretsKey not yet available")
        }

}