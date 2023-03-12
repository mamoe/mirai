/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.auth

import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.RetryLaterException
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmStatic

/**
 * Bot 的登录鉴权方式
 *
 * @see BotFactory.newBot
 *
 * @since 2.15
 */
public interface BotAuthorization {
    /**
     * 此方法控制 Bot 如何进行登录.
     *
     * Bot 只能使用一种登录方式, 但是可以在一种登录方式失败的时候尝试其他登录方式
     *
     * ## 异常类型
     *
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 并可建议系统进行重连或停止 bot (通过 [LoginFailedException.killBot]).
     * 例如抛出 [RetryLaterException] 可让 bot 重新进行一次登录.
     *
     * 抛出任意其他 [Throwable] 将视为鉴权选择器的自身错误.
     *
     * ## 示例代码
     * ```kotlin
     * override suspend fun authorize(
     *      authComponent: BotAuthSession,
     *      bot: BotAuthInfo,
     * ) {
     *      return kotlin.runCatching {
     *          authComponent.authByQRCode()
     *      }.recover {
     *          authComponent.authByPassword("...")
     *      }.getOrThrow()
     * }
     * ```
     */
    public suspend fun authorize(
        session: BotAuthSession,
        info: BotAuthInfo,
    ): BotAuthResult


    /**
     * 计算 `cache/account.secrets` 的加密秘钥
     */
    public fun calculateSecretsKey(
        bot: BotAuthInfo,
    ): ByteArray = bot.deviceInfo.guid + bot.id.toByteArray()

    public companion object {
        @JvmStatic
        public fun byPassword(password: String): BotAuthorization = byPassword(password.md5())

        @JvmStatic
        public fun byPassword(passwordMd5: ByteArray): BotAuthorization = factory.byPassword(passwordMd5)

        @JvmStatic
        public fun byQRCode(): BotAuthorization = factory.byQRCode()

        public operator fun invoke(
            block: suspend (BotAuthSession, BotAuthInfo) -> BotAuthResult
        ): BotAuthorization {
            return object : BotAuthorization {
                override suspend fun authorize(
                    session: BotAuthSession,
                    info: BotAuthInfo
                ): BotAuthResult {
                    return block(session, info)
                }
            }
        }

        private val factory: DefaultBotAuthorizationFactory =
            loadService() // TODO: add services for native after #2511 merged. 
    }
}

@NotStableForInheritance
public interface BotAuthResult

@NotStableForInheritance
public interface BotAuthInfo {
    public val id: Long
    public val deviceInfo: DeviceInfo
    public val configuration: BotConfiguration
}

@NotStableForInheritance
public interface BotAuthSession {
    /**
     * @throws LoginFailedException
     */
    public suspend fun authByPassword(password: String): BotAuthResult

    /**
     * @throws LoginFailedException
     */
    public suspend fun authByPassword(passwordMd5: ByteArray): BotAuthResult

    /**
     * @throws LoginFailedException
     */
    public suspend fun authByQRCode(): BotAuthResult
}


internal interface DefaultBotAuthorizationFactory {
    fun byPassword(passwordMd5: ByteArray): BotAuthorization
    fun byQRCode(): BotAuthorization
}
