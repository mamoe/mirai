/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.auth

import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmStatic

public interface BotAuthorization {
    public suspend fun authorize(
        authComponent: BotAuthComponent,
        bot: BotAuthInfo,
    ): BotAuthorizationResult

    public fun calculateSecretsKey(
        bot: BotAuthInfo,
    ): ByteArray {
        return bot.deviceInfo.guid + bot.id.toByteArray()
    }

    public companion object {
        @JvmStatic
        public fun byPassword(password: String): BotAuthorization {
            return byPassword(password.md5())
        }

        @JvmStatic
        public fun byPassword(passwordMd5: ByteArray): BotAuthorization {
            return byPassword(SecretsProtection.EscapedByteBuffer(passwordMd5))
        }

        private fun byPassword(passwordMd5: SecretsProtection.EscapedByteBuffer): BotAuthorization {
            return object : MiraiInternalBotAuthorization {
                override fun calculateSecretsKey1(bot: BotAuthInfo): SecretsProtection.EscapedByteBuffer {
                    return passwordMd5
                }

                override suspend fun authorize(
                    authComponent: MiraiInternalBotAuthComponent,
                    bot: BotAuthInfo
                ): BotAuthorizationResult {
                    return authComponent.authByPassword(passwordMd5)
                }

                override fun toString(): String {
                    return "BotAuthorization.byPassword(<ERASED>)"
                }
            }
        }

        @JvmStatic
        public fun byQRCode(): BotAuthorization {
            return object : BotAuthorization {
                override suspend fun authorize(
                    authComponent: BotAuthComponent,
                    bot: BotAuthInfo
                ): BotAuthorizationResult {
                    return authComponent.authByQRCode()
                }

                override fun toString(): String {
                    return "BotAuthorization.byQRCode()"
                }
            }
        }
    }
}

@NotStableForInheritance
public interface BotAuthorizationResult

@NotStableForInheritance
public interface BotAuthInfo {
    public val id: Long
    public val deviceInfo: DeviceInfo
    public val configuration: BotConfiguration
}

@NotStableForInheritance
public interface BotAuthComponent {
    public suspend fun authByPassword(password: String): BotAuthorizationResult
    public suspend fun authByPassword(passwordMd5: ByteArray): BotAuthorizationResult
    public suspend fun authByQRCode(): BotAuthorizationResult
}


///////////////////////////////////////////////////////////////////////////////////////////////////
//// Internal: for better performance
///////////////////////////////////////////////////////////////////////////////////////////////////

@NotStableForInheritance
@MiraiInternalApi
public interface MiraiInternalBotAuthComponent : BotAuthComponent {
    public suspend fun authByPassword(passwordMd5: SecretsProtection.EscapedByteBuffer): BotAuthorizationResult
}

@NotStableForInheritance
@MiraiInternalApi
public interface MiraiInternalBotAuthorization : BotAuthorization {
    override fun calculateSecretsKey(bot: BotAuthInfo): ByteArray {
        return calculateSecretsKey1(bot).asByteArray
    }

    public fun calculateSecretsKey1(
        bot: BotAuthInfo,
    ): SecretsProtection.EscapedByteBuffer

    public suspend fun authorize(authComponent: MiraiInternalBotAuthComponent, bot: BotAuthInfo): BotAuthorizationResult

    override suspend fun authorize(authComponent: BotAuthComponent, bot: BotAuthInfo): BotAuthorizationResult {
        return authorize(authComponent as MiraiInternalBotAuthComponent, bot)
    }
}


