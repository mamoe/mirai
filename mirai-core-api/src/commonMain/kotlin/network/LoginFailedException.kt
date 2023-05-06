/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.auth.BotAuthorization
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 在 [登录][Bot.login] 失败时抛出, 可正常地中断登录过程.
 */
public sealed class LoginFailedException(
    /**
     * 是否可因此登录失败而关闭 [Bot]. 一般是密码错误, 被冻结等异常时.
     */
    public val killBot: Boolean = false,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
// 实现提示 (仅供网络层实现者参考): `LoginFailedException` 会被包装为 `NetworkException` (`LoginFailedExceptionAsNetworkException`),
// 并在 `bot.login` 时 unwrap.

/**
 * 密码输入错误 (有时候也会是其他错误, 如 `"当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。"`)
 */
public class WrongPasswordException @MiraiInternalApi constructor(
    message: String?
) : LoginFailedException(true, message)

/**
 * 二维码扫码账号与 BOT 账号不一致。
 *
 * @since 2.15
 */
public class InconsistentBotIdException @MiraiInternalApi constructor(
    public val expected: Long,
    public val actual: Long,
    message: String? = null
) : LoginFailedException(
    true,
    message
        ?: "trying to logging in a bot whose id is different from the one provided to BotFactory.newBot, expected=$expected, actual=$actual."
)

/**
 * 无可用服务器
 */
public class NoServerAvailableException @MiraiInternalApi constructor(
    public override val cause: Throwable?
) : LoginFailedException(false, "no server available")

/**
 * 服务器要求稍后重试
 */
public class RetryLaterException @MiraiInternalApi constructor(
    message: String?,
    cause: Throwable? = null,
    killBot: Boolean = false
) : LoginFailedException(killBot, message, cause)

/**
 * 无标准输入或 Kotlin 不支持此输入.
 */
public class NoStandardInputForCaptchaException @MiraiInternalApi constructor(
    public override val cause: Throwable? = null
) : LoginFailedException(true, "no standard input for captcha")

/**
 * 表示在登录过程中, [BotAuthorization] 抛出的异常.
 * @since 2.15
 */
public class BotAuthorizationException @MiraiInternalApi constructor(
    public val authorization: BotAuthorization,
    cause: Throwable?,
) : LoginFailedException(
    killBot = true,
    "BotAuthorization(${authorization}) threw an exception during authorization process. See cause below.",
    cause
)

/**
 * 当前 [LoginSolver] 不支持此验证方式
 *
 * @since 2.15
 */
public open class UnsupportedCaptchaMethodException : LoginFailedException {
    public constructor(killBot: Boolean) : super(killBot)
    public constructor(killBot: Boolean, message: String?) : super(killBot, message)
    public constructor(killBot: Boolean, message: String?, cause: Throwable?) : super(killBot, message, cause)
    public constructor(killBot: Boolean, cause: Throwable?) : super(killBot, cause = cause)
}

/**
 * 需要强制短信验证, 且当前 [LoginSolver] 不支持时抛出.
 * @since 2.13
 */
public class UnsupportedSmsLoginException(message: String?) : UnsupportedCaptchaMethodException(true, message)

/**
 * 无法完成滑块验证
 */
public class UnsupportedSliderCaptchaException(message: String?) : UnsupportedCaptchaMethodException(true, message)

/**
 * 需要二维码登录, 且当前 [LoginSolver] 不支持时抛出
 *
 * @since 2.15
 */
public class UnsupportedQRCodeCaptchaException(message: String?) : UnsupportedCaptchaMethodException(true, message)

/**
 * 非 mirai 实现的异常
 */
public abstract class CustomLoginFailedException : LoginFailedException {
    public constructor(killBot: Boolean) : super(killBot)
    public constructor(killBot: Boolean, message: String?) : super(killBot, message)
    public constructor(killBot: Boolean, message: String?, cause: Throwable?) : super(killBot, message, cause)
    public constructor(killBot: Boolean, cause: Throwable?) : super(killBot, cause = cause)
}