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
import net.mamoe.mirai.auth.ReAuthCause.*
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.network.RetryLaterException
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.event.events.BotOfflineEvent

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
     * @see BotAuthorization.authorize
     */
    public val isFirstLogin: Boolean

    /**
     * 导致重新进入认证流程的原因
     *
     * 重新进入认证流程一般是第二次或多次进入认证流程，除了 [FastLoginError]。
     *
     * 第一次进入认证流程时该值为 `null`
     *
     */
    public val reAuthCause: ReAuthCause?
}

/**
 * 导致进行重新与服务器认证的原因
 *
 * @see ForceOffline
 * @see MsfOffline
 * @see FastLoginError
 * @see NetworkError
 * @see Unknown
 */
@NotStableForInheritance
public sealed class ReAuthCause(
    public open val bot: Bot,
    public open val message: String?
) {
    /**
     * Bot 被挤下线
     *
     * 当 Bot 在其他客户端使用相同协议登录此账号时，会导致 Bot 掉线。
     *
     * 会因此原因进行登录就说明 [BotConfiguration.autoReconnectOnForceOffline] 为 `true`。若该属性为 `false` 时则 mirai 不会自动在被挤下线时重新登录。
     *
     * 被挤下线后当前的 session 会失效，登录时会重新与服务器认证 Bot 账号信息。
     *
     * @see BotConfiguration.autoReconnectOnForceOffline
     * @see BotOfflineEvent.Force
     */
    public class ForceOffline(
        override val bot: Bot,
        override val message: String?
    ) : ReAuthCause(bot, message)

    /**
     * Bot 被服务器断开
     *
     * 由于其他原因（账号被封禁，被其他客户端手动下线等）导致 Bot 掉线。
     *
     * 一般情况下，被服务器断开后 mirai 会尝试重新登录。
     *
     * 被服务器断开下线后当前的 session 会失效，登录时会重新与服务器认证 Bot 账号信息。
     *
     * @see BotOfflineEvent.MsfOffline
     */
    public class MsfOffline(
        override val bot: Bot,
        override val message: String?
    ) : ReAuthCause(bot, message)

    /**
     * 由其他网络原因引起的掉线
     *
     * 一般情况下，被服务器断开后 mirai 会尝试重新登录。
     *
     * 由网络问题引起的掉线不一定会使当前的 session 失效，
     * 仅 session 失效时与服务器认证。
     */
    public class NetworkError(
        override val bot: Bot,
        override val message: String?
    ) : ReAuthCause(bot, message)

    /**
     * 快速登录失败
     *
     * Bot 账号首次在 mirai [登录][Bot.login] 成功后，会保存登录 session 信息用于下次登录。
     *
     * 下次登录时，mirai 会首先使用 session 尝试快速登录，
     * 若快速登录失败，则会使用 [BotAuthorization] 定义的流程进行登录。
     *
     * 注意：此原因在 _字面意义_ 上并不是“重新”与服务器认证，
     * 因为出现此原因时，是 [BotAuthorization] 第一次进行认证流程。
     *
     * 但严格意义上，mirai 提前尝试使用快速登录进行了认证，
     * 所以 “快速登陆失败后使用 [BotAuthorization] 定义的流程进行登录” 可以被认为是重新与服务器认证。
     *
     * @see BotAuthorization
     * @see Bot.login
     */
    public class FastLoginError(
        override val bot: Bot,
        override val message: String?
    ) : ReAuthCause(bot, message)

    public class Unknown(
        override val bot: Bot,
        public val cause: Throwable?
    ) : ReAuthCause(bot, cause?.message)
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
