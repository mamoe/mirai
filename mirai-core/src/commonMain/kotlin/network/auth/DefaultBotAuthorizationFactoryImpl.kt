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
import net.mamoe.mirai.utils.SecretsProtection.EscapedByteBuffer

/**
 * Provides default [BotAuthorization.byPassword] implementation.
 * @see net.mamoe.mirai.auth.DefaultBotAuthorizationFactory
 */
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")
internal class DefaultBotAuthorizationFactoryImpl :
    net.mamoe.mirai.auth.DefaultBotAuthorizationFactory {
    override fun byPassword(passwordMd5: ByteArray): BotAuthorization {
        val buffer = EscapedByteBuffer(passwordMd5)
        return byPassword(buffer) // Avoid referring passwordMd5(ByteArray)
    }

    private fun byPassword(buffer: EscapedByteBuffer): BotAuthorization {
        return object : BotAuthorizationWithSecretsProtection() {
            override fun calculateSecretsKeyImpl(bot: BotAuthInfo): EscapedByteBuffer = buffer

            override suspend fun authorize(
                session: BotAuthSessionInternal,
                bot: BotAuthInfo
            ): BotAuthResult = session.authByPassword(buffer)

            override fun toString(): String = "BotAuthorization.byPassword(<ERASED>)"
        }
    }

    override fun byQRCode(): BotAuthorization {
        return object : BotAuthorization {
            override suspend fun authorize(session: BotAuthSession, info: BotAuthInfo): BotAuthResult =
                session.authByQRCode()

            override fun toString(): String = "BotAuthorization.byQRCode()"
        }
    }
}