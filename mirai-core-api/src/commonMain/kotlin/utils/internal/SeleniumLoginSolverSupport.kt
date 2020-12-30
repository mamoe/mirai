/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal

import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger

internal val SeleniumLoginSolver: LoginSolver? by lazy {
    try {
        Class.forName("net.mamoe.mirai.selenium.SeleniumLoginSolver")
            .getMethod("getInstance")
            .invoke(null) as? LoginSolver
    } catch (ignore: ClassNotFoundException) {
        null
    } catch (error: Throwable) {
        MiraiLogger.TopLevel.warning("Error in loading mirai-login-solver-selenium, skip", error)
        null
    }
}

// null -> 该情况为 user 确认能自己传入 ticket, 不需要 Selenium 的帮助
// true -> SeleniumLoginSolver 支持
// false-> 无法提供默认滑块验证解决器
internal val isSliderCaptchaSupportKind: Boolean? by lazy {
    if (System.getProperty("mirai.slider.captcha.supported") != null) {
        null
    } else {
        SeleniumLoginSolver != null
    }
}
