/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.utils.TestOnly


internal class BotAccount(
    internal val id: Long,
    authorization: BotAuthorization,
) {
    var authorization: BotAuthorization = authorization
        // FIXME: Making this mutable is very bad. 
        //  But I had to do this because the current test framework is bad, and I don't have time to do a major rewrite. 
        @TestOnly set

    @TestOnly // to be compatible with your local tests :)
    constructor(
        id: Long, pwd: String
    ) : this(id, BotAuthorization.byPassword(pwd))

    var accountSecretsKeyBuffer: SecretsProtection.EscapedByteBuffer? = null

    val accountSecretsKey: ByteArray
        get() {
            accountSecretsKeyBuffer?.let { return it.asByteArray }
            error("accountSecretsKey not yet available")
        }

}