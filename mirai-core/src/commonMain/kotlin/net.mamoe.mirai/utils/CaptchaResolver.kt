package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer

/**
 * 让用户处理验证码
 *
 * @return 用户输入得到的验证码. 非 null 时一定 `length==4`.
 */
internal expect suspend fun solveCaptcha(captchaBuffer: IoBuffer): String?
