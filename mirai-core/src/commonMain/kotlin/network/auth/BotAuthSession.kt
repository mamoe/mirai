/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.auth

import net.mamoe.mirai.auth.BotAuthInfo
import net.mamoe.mirai.auth.BotAuthResult
import net.mamoe.mirai.auth.BotAuthSession
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.utils.SecretsProtection
import net.mamoe.mirai.utils.md5


// With SecretsProtection support
internal abstract class BotAuthSessionInternal : BotAuthSession {

    final override suspend fun authByPassword(password: String): BotAuthResult {
        return authByPassword(password.md5())
    }

    final override suspend fun authByPassword(passwordMd5: ByteArray): BotAuthResult {
        return authByPassword(SecretsProtection.EscapedByteBuffer(passwordMd5))
    }

    abstract suspend fun authByPassword(passwordMd5: SecretsProtection.EscapedByteBuffer): BotAuthResult
}

// With SecretsProtection support
internal abstract class BotAuthorizationWithSecretsProtection : BotAuthorization {
    final override fun calculateSecretsKey(bot: BotAuthInfo): ByteArray {
        return calculateSecretsKeyImpl(bot).asByteArray
    }

    abstract fun calculateSecretsKeyImpl(
        bot: BotAuthInfo,
    ): SecretsProtection.EscapedByteBuffer

    abstract suspend fun authorize(session: BotAuthSessionInternal, bot: BotAuthInfo): BotAuthResult

    final override suspend fun authorize(session: BotAuthSession, info: BotAuthInfo): BotAuthResult {
        return authorize(session as BotAuthSessionInternal, info)
    }
}


