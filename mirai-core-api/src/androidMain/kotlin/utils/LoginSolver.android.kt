/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.utils.isSliderCaptchaSupportKind
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.LoginSolver.Companion.Default


/**
 * 验证码, 设备锁解决器
 *
 * @see Default
 * @see BotConfiguration.loginSolver
 */
public actual abstract class LoginSolver public actual constructor() {
    /**
     * 处理图片验证码.
     *
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 抛出任意其他 [Exception] 将视为异常终止
     *
     * @throws LoginFailedException
     */
    public actual abstract suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String?

    /**
     * 为 `true` 表示支持滑动验证码, 遇到滑动验证码时 mirai 会请求 [onSolveSliderCaptcha].
     * 否则会跳过滑动验证码并告诉服务器此客户端不支持, 有可能导致登录失败
     */
    public actual open val isSliderCaptchaSupported: Boolean
        get() = isSliderCaptchaSupportKind ?: true

    /**
     * 处理滑动验证码.
     *
     * 返回 `null` 以表示无法处理验证码, 将会刷新验证码或重试登录.
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 抛出任意其他 [Exception] 将视为异常终止
     *
     * @throws LoginFailedException
     * @return 验证码解决成功后获得的 ticket.
     */
    public actual abstract suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String?

    /**
     * 处理不安全设备验证.
     *
     * 返回值保留给将来使用. 目前在处理完成后返回任意内容 (包含 `null`) 均视为处理成功.
     * 抛出一个 [LoginFailedException] 以正常地终止登录, 抛出任意其他 [Exception] 将视为异常终止.
     *
     * @return 任意内容. 返回值保留以供未来更新.
     * @throws LoginFailedException
     */
    public actual abstract suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String?

    public actual companion object {
        /**
         * 当前平台默认的 [LoginSolver]. Android 端没有默认验证码实现, [Default] 总为 `null`.
         */
        @JvmField
        public actual val Default: LoginSolver? = null

        @Suppress("unused")
        @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
        @DeprecatedSinceMirai(hiddenSince = "2.0") // maybe 2.0
        public actual fun getDefault(): LoginSolver = Default
            ?: error("LoginSolver is not provided by default on your platform. Please specify by BotConfiguration.loginSolver")
    }

}