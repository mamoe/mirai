/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.LoginSolver.Companion.Default

/**
 * 验证码, 设备锁解决器
 *
 * @see Default
 * @see BotConfiguration.loginSolver
 */
public actual abstract class LoginSolver actual constructor() {
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
        get() = false

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
    public actual abstract suspend fun onSolveUnsafeDeviceLoginVerify(
        bot: Bot,
        url: String
    ): String?

    public actual companion object {
        /**
         * 当前平台默认的 [LoginSolver]。
         *
         * 检测策略:
         * 1. 若是 `mirai-core-api-android` 或 `android.util.Log` 存在, 返回 `null`.
         * 2. 检测 JVM 属性 `mirai.no-desktop`. 若存在, 返回 `StandardCharImageLoginSolver`
         * 3. 检测 JVM 桌面环境, 若支持, 返回 `SwingSolver`
         * 4. 返回 `StandardCharImageLoginSolver`
         *
         * @return `SwingSolver` 或 `StandardCharImageLoginSolver` 或 `null`
         */
        public actual val Default: LoginSolver?
            get() = null

        @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
        @Suppress("unused")
        public actual fun getDefault(): LoginSolver = Default
            ?: error("LoginSolver is not provided by default on your platform. Please specify by BotConfiguration.loginSolver")
    }
}