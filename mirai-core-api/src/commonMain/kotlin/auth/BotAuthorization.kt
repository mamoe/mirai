/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.auth

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.event.events.BotOfflineEvent
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
    @OptIn(MiraiInternalApi::class)
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

        private val factory: DefaultBotAuthorizationFactory by lazy {
            Mirai // Ensure services loaded
            loadService()
        }
    }
}

@NotStableForInheritance
public interface BotAuthResult

@NotStableForInheritance
public interface BotAuthInfo {
    public val id: Long
    public val deviceInfo: DeviceInfo
    public val configuration: BotConfiguration

    /**
     * 是否是首次登录
     *
     * 首次登录指的是首次调用 [Bot.login] 进行登录，直到登录成功的过程。
     *
     * 若在首次登录过程中多次进入[认证流程][BotAuthorization.authorize]，则这流程些均被视为首次登录。
     *
     * @see Bot.login
     * @see BotAuthorization.authorize
     */
    public val isFirstLogin: Boolean

    /**
     * 导致进入[认证流程][BotAuthorization.authorize]的原因。
     */
    public val reason: AuthReason
}

/**
 * 导致进行[认证流程][BotAuthorization.authorize]的原因
 */
public sealed class AuthReason {
    public abstract val bot: Bot
    public abstract val message: String?

    /**
     * Bot 全新[登录][Bot.login]
     *
     * 全新登录指登录前本地没有任何当前 Bot 的登录缓存信息而进行的登录。
     *
     * 全新登录时将会进入[认证流程][BotAuthorization.authorize]。
     *
     * @see Bot.login
     * @see FastLoginError
     */
    public class FreshLogin @MiraiInternalApi constructor(
        override val bot: Bot,
        override val message: String?
    ) : AuthReason()

    /**
     * Bot 被挤下线
     *
     * 当 Bot 账号在其他客户端使用相同（或相似）协议登录时，Bot 会下线，
     * 被挤下线后当前的登录会话将失效。
     *
     * 当 [BotConfiguration.autoReconnectOnForceOffline] 为 `true` 时，
     * Bot 会尝试重新登录，并会以此原因进入[认证流程][BotAuthorization.authorize]。
     *
     * @see BotConfiguration.autoReconnectOnForceOffline
     * @see BotOfflineEvent.Force
     */
    public class ForceOffline @MiraiInternalApi constructor(
        override val bot: Bot,
        override val message: String?
    ) : AuthReason()

    /**
     * Bot 被服务器断开
     *
     * 因其他原因导致 Bot 被服务器断开。这些原因包括账号被封禁、被其他客户端手动下线等，
     * 被服务器断开下线后当前的登录会话将失效。
     *
     * Bot 会尝试重新登录，并会以此原因进入[认证流程][BotAuthorization.authorize]。
     *
     * @see BotOfflineEvent.MsfOffline
     */
    public class MsfOffline @MiraiInternalApi constructor(
        override val bot: Bot,
        override val message: String?
    ) : AuthReason()

    /**
     * 由网络原因引起的掉线
     *
     * 一般情况下，Bot 被服务器断开后会尝试重新登录。
     *
     * 由网络问题引起的掉线不一定会使当前的登录会话失效，
     * 仅登录会话失效时 Bot 会以此原因进入[认证流程][BotAuthorization.authorize]。
     */
    public class NetworkError @MiraiInternalApi constructor(
        override val bot: Bot,
        override val message: String?
    ) : AuthReason()

    /**
     * 快速登录失败
     *
     * Bot 账号首次 [登录][Bot.login] 成功后，会保存登录缓存信息用于下次登录。
     *
     * 下次登录时，Bot 会首先使用登录缓存信息尝试快速登录，
     * 若快速登录失败，则会以此原因进入[认证流程][BotAuthorization.authorize]。
     *
     * @see BotAuthorization.authorize
     * @see Bot.login
     */
    public class FastLoginError @MiraiInternalApi constructor(
        override val bot: Bot,
        override val message: String?
    ) : AuthReason()

    public class Unknown @MiraiInternalApi constructor(
        override val bot: Bot,
        public val cause: Throwable?
    ) : AuthReason() {
        override val message: String? = cause?.message
    }
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
