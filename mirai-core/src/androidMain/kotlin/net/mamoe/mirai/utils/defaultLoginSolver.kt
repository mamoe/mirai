package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.Bot

/**
 * 在各平台实现的默认的验证码处理器.
 */
actual var defaultLoginSolver: LoginSolver = object : LoginSolver() {
    override suspend fun onSolvePicCaptcha(bot: Bot, data: IoBuffer): String? {
        error("should be implemented manually by you")
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, data: IoBuffer): String? {
        error("should be implemented manually by you")
    }

    override suspend fun onGetPhoneNumber(): String {
        error("should be implemented manually by you")
    }

    override suspend fun onGetSMSVerifyCode(): String {
        error("should be implemented manually by you")
    }

}