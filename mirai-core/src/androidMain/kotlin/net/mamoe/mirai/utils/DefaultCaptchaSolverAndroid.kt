package net.mamoe.mirai.utils

/**
 * 直接抛出异常. 需自行处理验证码
 */
actual var DefaultCaptchaSolver: CaptchaSolver = {
    error("No CaptchaSolver found. BotConfiguration.captchaSolver should be assigned manually")
}