/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.network

import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * 在 [登录][Bot.login] 失败时抛出, 可正常地中断登录过程.
 */
sealed class LoginFailedException constructor(
    /**
     * 是否可因此登录失败而关闭 [Bot]. 一般是密码错误, 被冻结等异常时.
     */
    val killBot: Boolean = false,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 密码输入错误 (有时候也会是其他错误, 如 `"当前上网环境异常，请更换网络环境或在常用设备上登录或稍后再试。"`)
 */
class WrongPasswordException(message: String?) : LoginFailedException(true, message)

/**
 * 无可用服务器
 */
class NoServerAvailableException(override val cause: Throwable?) : LoginFailedException(false, "no server available")

/**
 * 无标准输入或 Kotlin 不支持此输入.
 */
class NoStandardInputForCaptchaException(override val cause: Throwable?) :
    LoginFailedException(true, "no standard input for captcha")

/**
 * 需要短信验证时抛出. mirai 目前还不支持短信验证.
 */
@MiraiExperimentalAPI
class UnsupportedSMSLoginException(message: String?) : LoginFailedException(true, message)

/**
 * 非 mirai 实现的异常
 */
abstract class CustomLoginFailedException : LoginFailedException {
    constructor(killBot: Boolean) : super(killBot)
    constructor(killBot: Boolean, message: String?) : super(killBot, message)
    constructor(killBot: Boolean, message: String?, cause: Throwable?) : super(killBot, message, cause)
    constructor(killBot: Boolean, cause: Throwable?) : super(killBot, cause = cause)
}